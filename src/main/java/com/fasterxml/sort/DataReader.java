package com.fasterxml.sort;

import java.io.IOException;

public abstract class DataReader<T>
{
    /**
     * Method for reading the next data item; will return
     * null to indicate end of input, otherwise return a non-null
     * item.
     */
    public abstract T readNext() throws IOException;

    /**
     * Method that should estimate memory usage of given item, for purpose
     * of limiting amount of data kept in memory during pre-sorting phase.
     */
    public abstract int estimateSizeInBytes(T item);
    
    /**
     * Method for closing the reader. Note that reader needs to ensure
     * that it is ok to call close multiple times. Reader may also
     * close underlying resources as soon as it has reached end of input.
     */
    public abstract void close() throws IOException;
}
