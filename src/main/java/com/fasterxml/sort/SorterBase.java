package com.fasterxml.sort;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.sort.util.SegmentedBuffer;

public abstract class SorterBase<T>
    implements SortingState
{
    /* each entry (in buffer) takes about 4 bytes on 32-bit machine; but let's be
     * conservative and use 8 as base, plus size of object itself.
     */
    private final static long ENTRY_SLOT_SIZE = 8L;
    
    /*
    /********************************************************************** 
    /* Configuration
    /********************************************************************** 
     */
    
    protected final SortConfig _config;
    
    /**
     * Factory used for reading intermediate sorted files.
     */
    protected DataReaderFactory<T> _readerFactory;
    
    /**
     * Factory used for writing intermediate sorted files.
     */
    protected DataWriterFactory<T> _writerFactory;

    /**
     * Comparator to use for sorting entries; defaults to 'C
     */
    protected Comparator<T> _comparator;
    
    /*
    /********************************************************************** 
    /* State
    /********************************************************************** 
     */
    
    protected SortingState.Phase _phase;

    protected int _presortFileCount;
    
    protected int _sortRoundCount;

    protected int _currentSortRound;
    
    protected final AtomicBoolean _cancelRequest = new AtomicBoolean(false);
    
    protected Exception _cancelForException;
    
    /*
    /********************************************************************** 
    /* Construction
    /********************************************************************** 
     */

    protected SorterBase(SortConfig config,
                         DataReaderFactory<T> readerFactory,
                         DataWriterFactory<T> writerFactory,
                         Comparator<T> comparator)
    {
        _config = config;

        _readerFactory = readerFactory;
        _writerFactory = writerFactory;
        _comparator = comparator;
        
        _phase = null;
    }

    protected SorterBase() {
        this(new SortConfig());
    }
    
    protected SorterBase(SortConfig config) {
        this(config, null, null, null);
    }

    /*
    /********************************************************************** 
    /* SortingState implementation
    /********************************************************************** 
     */
    
    @Override
    public void cancel() {
        _cancelForException = null;
        _cancelRequest.set(true);
    }

    @Override
    public void cancel(RuntimeException e) {
        _cancelForException = e;
        _cancelRequest.set(true);
    }
    
    @Override
    public void cancel(IOException e) {
        _cancelForException = e;
        _cancelRequest.set(true);
    }

    @Override
    public Phase getPhase() {
        return _phase;
    }
    
    @Override
    public int getNumberOfSortRounds() {
        return _sortRoundCount;
    }

    @Override
    public int getNumberOfPreSortFiles() {
        return _presortFileCount;
    }
    
    @Override
    public int getSortRound() {
        return _currentSortRound;
    }
    
    @Override
    public boolean isCompleted() {
        return (_phase == SortingState.Phase.COMPLETE);
    }

    @Override
    public boolean isPreSorting() {
        return (_phase == SortingState.Phase.PRE_SORTING);
    }

    @Override
    public boolean isSorting() {
        return (_phase == SortingState.Phase.SORTING);
    }

    /*
    /********************************************************************** 
    /* Internal methods, pre-sorting
    /********************************************************************** 
     */

    /**
     * Helper method that will fill given buffer with data read using
     * given reader, obeying given memory usage constraints.
     */
    protected Object[] _readMax(DataReader<T> inputReader, SegmentedBuffer buffer,
            long memoryToUse, T firstItem)
        throws IOException
    {
        // how much memory do we expect largest remaining entry to take?
        int ptr = 0;
        Object[] segment = buffer.resetAndStart();
        int segmentLength = segment.length;
        long minMemoryNeeded;

        if (firstItem != null) {
            segment[ptr++] = firstItem;
            long firstSize = ENTRY_SLOT_SIZE + inputReader.estimateSizeInBytes(firstItem);
            minMemoryNeeded = Math.max(firstSize, 256L);
        } else  {
            minMemoryNeeded = 256L;
        }

        // reduce mem amount by buffer cost too:
        memoryToUse -= (ENTRY_SLOT_SIZE * segmentLength);
        
        while (true) {
            T value = inputReader.readNext();
            if (value == null) {
                break;
            }
            long size = inputReader.estimateSizeInBytes(value);
            if (size > minMemoryNeeded) {
                minMemoryNeeded = size;
            }
            if (ptr >= segmentLength) {
                segment = buffer.appendCompletedChunk(segment);
                segmentLength = segment.length;
                memoryToUse -= (ENTRY_SLOT_SIZE * segmentLength);
                ptr = 0;
            }
            segment[ptr++] = value;
            memoryToUse -= size;
            if (memoryToUse < minMemoryNeeded) {
                break;
            }
        }
        return buffer.completeAndClearBuffer(segment, ptr);
    }
    
    protected void _presort(DataReader<T> inputReader, SegmentedBuffer buffer, T nextValue,
            List<File> presorted)
        throws IOException
    {
        do {
            Object[] items = _readMax(inputReader, buffer, _config.getMaxMemoryUsage(), nextValue);
            Arrays.sort(items, _rawComparator());
            presorted.add(_writePresorted(items));
            nextValue = inputReader.readNext();
        } while (nextValue != null);
    }

    @SuppressWarnings("resource")
    protected File _writePresorted(Object[] items) throws IOException
    {
        File tmp = _config.getTempFileProvider().provide();
        @SuppressWarnings("unchecked")
        DataWriter<Object> writer = (DataWriter<Object>) _writerFactory.constructWriter(new FileOutputStream(tmp));
        boolean closed = false;
        try {
            ++_presortFileCount;
            for (int i = 0, end = items.length; i < end; ++i) {
                writer.writeEntry(items[i]);
                // to further reduce transient mem usage, clear out the ref
                items[i] = null;
            }
            closed = true;
            writer.close();
        } finally {
            if (!closed) {
                // better swallow since most likely we are getting an exception already...
                try { writer.close(); } catch (IOException e) { }
            }
        }
        return tmp;
    }
    
    /*
    /********************************************************************** 
    /* Internal methods, sorting, output
    /********************************************************************** 
     */

    /**
     * Main-level merge method that sorts the given input and writes to final output.
     */
    protected void merge(List<File> presorted, DataWriter<T> resultWriter)
        throws IOException
    {
        List<File> inputs = merge(presorted);
        // and then last around to produce the result file
        _merge(inputs, resultWriter);
    }

    /**
     * Main-level merge method that sorts the given input.
     * @return List of files that are individually sorted and ready for final merge.
     */
    protected List<File> merge(List<File> presorted)
        throws IOException
    {
        // Ok, let's see how many rounds we should have...
        final int mergeFactor = _config.getMergeFactor();
        _sortRoundCount = _calculateRoundCount(presorted.size(), mergeFactor);
        _currentSortRound = 0;

        // first intermediate rounds
        List<File> inputs = presorted;
        while (inputs.size() > mergeFactor) {
            ArrayList<File> outputs = new ArrayList<File>(1 + ((inputs.size() + mergeFactor - 1) / mergeFactor));
            for (int offset = 0, end = inputs.size(); offset < end; offset += mergeFactor) {
                int localEnd = Math.min(offset + mergeFactor, end);
                outputs.add(_merge(inputs.subList(offset, localEnd)));
            }
            ++_currentSortRound;
            // and then switch result files to be input files
            inputs = outputs;
        }
        return inputs;
    }

    protected void _writeAll(DataWriter<T> resultWriter, Object[] items)
        throws IOException
    {
        // need to go through acrobatics, due to type erasure... works, if ugly:
        @SuppressWarnings("unchecked")
        DataWriter<Object> writer = (DataWriter<Object>) resultWriter;
        for (Object item : items) {
            writer.writeEntry(item);
        }
    }

    @SuppressWarnings("resource")
    protected File _merge(List<File> inputs)
        throws IOException
    {
        File resultFile = _config.getTempFileProvider().provide();
        _merge(inputs, _writerFactory.constructWriter(new FileOutputStream(resultFile)));
        return resultFile;
    }

    protected void _merge(List<File> inputs, DataWriter<T> writer)
        throws IOException
    {
        DataReader<T> merger = null;
        try {
            merger = _createMergeReader(inputs);
            T value;
            while ((value = merger.readNext()) != null) {
                writer.writeEntry(value);
            }
            merger.close(); // usually not necessary (reader should close on eof) but...
            merger = null;
            writer.close();
        } finally {
            if (merger != null) {
                try { merger.close(); } catch (IOException e) { }
            }
            for (File input : inputs) {
                input.delete();
            }
        }
    }

    protected DataReader<T> _createMergeReader(List<File> inputs) throws IOException {
        ArrayList<DataReader<T>> readers = new ArrayList<DataReader<T>>(inputs.size());
        for (File mergedInput : inputs) {
            readers.add(_readerFactory.constructReader(new FileInputStream(mergedInput)));
        }
        return Merger.mergedReader(_comparator, readers);
    }
    
    /*
    /********************************************************************** 
    /* Internal methods, other
    /********************************************************************** 
     */

    protected static int _calculateRoundCount(int files, int mergeFactor)
    {
        int count = 1;
        while (files > mergeFactor) {
            ++count;
            files = (files + mergeFactor - 1) / mergeFactor;
        }
        return count;
    }
    
    protected boolean _checkForCancel() throws IOException
    {
        return _checkForCancel(null);
    }

    protected boolean _checkForCancel(Collection<File> tmpFilesToDelete) throws IOException
    {
        if (!_cancelRequest.get()) {
            return false;
        }
        if (tmpFilesToDelete != null) {
            for (File f : tmpFilesToDelete) {
                f.delete();
            }
        }
        if (_cancelForException != null) {
            // can only be an IOException or RuntimeException, so
            if (_cancelForException instanceof RuntimeException) {
                throw (RuntimeException) _cancelForException;
            }
            throw (IOException) _cancelForException;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected Comparator<Object> _rawComparator() {
        return (Comparator<Object>) _comparator;
    }
}
