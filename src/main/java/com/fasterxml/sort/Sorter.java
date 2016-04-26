package com.fasterxml.sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Main entry point for sorting functionality; object that drives
 * the sorting process from pre-sort to final output.
 * Instances are not thread-safe, although they are reusable.
 * Since the cost of creating new instances is trivial, there is usually
 * no benefit from reusing instances, other than possible convenience.
 */
public class Sorter<T> extends IteratingSorter<T>
{
    /**
     * @param config Configuration for the sorter
     * @param readerFactory Factory used for creating readers for pre-sorted data;
     *   as well as for input if an {@link InputStream} is passed as source
     * @param writerFactory Factory used for creating writers for storing pre-sorted data;
     *   as well as for results if an {@link OutputStream} is passed as destination.
     */
    public Sorter(SortConfig config,
                  DataReaderFactory<T> readerFactory,
                  DataWriterFactory<T> writerFactory,
                  Comparator<T> comparator)
    {
        super(config, readerFactory, writerFactory, comparator);
    }

    public Sorter() {
        super();
    }

    public Sorter(SortConfig config) {
        super(config);
    }

    protected Sorter<T> withReaderFactory(DataReaderFactory<T> f) {
        return new Sorter<T>(_config, f, _writerFactory, _comparator);
    }

    protected Sorter<T> withWriterFactory(DataWriterFactory<T> f) {
        return new Sorter<T>(_config, _readerFactory, f, _comparator);
    }

    protected Sorter<T> withComparator(Comparator<T> cmp) {
        return new Sorter<T>(_config, _readerFactory, _writerFactory, cmp);
    }


    /*
    /********************************************************************** 
    /* Main sorting API
    /********************************************************************** 
     */

    /**
     * Method that will perform full sort on specified input, writing results
     * into specified destination. Data conversions needed are done
     * using {@link DataReaderFactory} and {@link DataWriterFactory} configured
     * for this sorter.
     */
    public void sort(InputStream source, OutputStream destination)
        throws IOException
    {
        sort(_readerFactory.constructReader(source),
                _writerFactory.constructWriter(destination));
    }

    /**
     * Method that will perform full sort on input data read using given
     * {@link DataReader}, and written out using specified {@link DataWriter}.
     * Conversions to and from intermediate sort files is done
     * using {@link DataReaderFactory} and {@link DataWriterFactory} configured
     * for this sorter.
     * 
     * @return true if sorting completed successfully; false if it was cancelled
     */
    public boolean sort(DataReader<T> inputReader, DataWriter<T> resultWriter)
        throws IOException
    {
        Iterator<T> it = super.sort(inputReader);
        if(it == null) {
            return false;
        }
        try {
            while(it.hasNext()) {
                T value = it.next();
                resultWriter.writeEntry(value);
            }
            resultWriter.close();
        } finally {
            super.close();
        }
        return true;
    }
}
