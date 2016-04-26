package com.fasterxml.sort.std;

import java.util.Comparator;

/**
 * Basic comparator implementation that works on types that implement
 * {@link Comparator}.
 */
public class StdComparator<T extends Comparable<T>> implements Comparator<T>
{
    @Override
    public int compare(T object1, T object2) {
        if (object1 == object2) return 0;
        if (object1 == null) return -1;
        return object1.compareTo(object2);
    }

}
