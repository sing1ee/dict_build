package com.fasterxml.sort.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;

import com.fasterxml.sort.DataReader;

/**
 * Base implementation for {@link DataReader} that uses a
 * {@link BlockingQueue} for getting input.
 * The only missing part is implementation for
 * {@link #estimateSizeInBytes(Object)}, since there is no way
 * to provide a meaningful estimate without knowing object type.
 */
public abstract class BlockingQueueReader<E>
    extends DataReader<E>
{
    protected final BlockingQueue<E> _queue;
    
    protected final E _endMarker;

    protected boolean _closed;

    @Deprecated
    public BlockingQueueReader(BlockingQueue<E> q) {
        this(q, null);
    }
    
    /**
     * @param q Queue to read entries from
     * @param endMarker Value that is used to signal end-of-input; when this value
     *   is gotten from queue, reader assumes that no more input is coming and
     *   will return <code>null</code> from {@link #readNext}.
     */
    public BlockingQueueReader(BlockingQueue<E> q, E endMarker) {
        _queue = q;
        _endMarker = endMarker;
    }
    
    @Override
    public void close() throws IOException {
        _closed = true;
    }

    @Override
    public abstract int estimateSizeInBytes(E item);

    @Override
    public E readNext() throws IOException {
        if (_closed) {
            return null;
        }
        try {
            E value = _queue.take();
            if (value == _endMarker) {
                _closed = true;
                return null;
            }
            return value;
        } catch (InterruptedException e) {
            InterruptedIOException ie = new InterruptedIOException();
            ie.initCause(e);
            throw ie;
        }
    }
}
