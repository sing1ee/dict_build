package com.fasterxml.sort.util;

import java.util.Comparator;

/**
 * Simple {@link Comparator} implementation that can be used
 * when items to compare have "natural" sorting order that
 * can be used via {@link Comparable} interface.
 */
public class NaturalComparator<T extends Comparable<T>>
    implements Comparator<T>
{
    @Override
    public int compare(T arg0, T arg1) {
        return arg0.compareTo(arg1);
    }
}
