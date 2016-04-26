package com.fasterxml.sort;

import java.io.IOException;

public abstract class DataWriter<T>
{
    public abstract void writeEntry(T item) throws IOException;

    public abstract void close() throws IOException;
}
