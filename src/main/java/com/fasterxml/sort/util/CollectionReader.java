package com.fasterxml.sort.util;

import java.io.IOException;
import java.util.*;

import com.fasterxml.sort.DataReader;

/**
 * Simple {@link DataReader} implementation that can be used to
 * serve items from a {@link Collection} (or {@link Iterator}).
 * Note that implementation of {@link #estimateSizeInBytes} is
 * naive and returns 1 for all items; it must be redefined if
 * memory limits are to be enforced, or alternatively
 * <code>Sorter</code> should be configured with maximum number of
 * items to use as memory limit.
 */
public class CollectionReader<T> extends DataReader<T>
{
    protected Iterator<T> _items;

    public CollectionReader(Collection<T> items) {
        this(items.iterator());
    }

    public CollectionReader(Iterator<T> items) {
        _items = items;
    }
    
    @Override
    public T readNext()
    {
        if (_items == null) {
            return null;
        }
        if (!_items.hasNext()) {
            _items = null;
            return null;
        }
        return _items.next();
    }

    @Override
    public int estimateSizeInBytes(T item) {
        return 1;
    }
    
    @Override
    public void close() throws IOException {
        // no-op
    }

}
