package com.fasterxml.sort.util;

import java.util.Iterator;

public class CastingIterator<T> implements Iterator<T> {
    private final Iterator<Object> _it;

    public CastingIterator(Iterator<Object> it) {
        _it = it;
    }

    @Override
    public boolean hasNext() {
        return _it.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T)_it.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}