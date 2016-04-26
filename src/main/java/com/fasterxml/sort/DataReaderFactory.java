package com.fasterxml.sort;

import java.io.*;

public abstract class DataReaderFactory<T>
{
    public abstract DataReader<T> constructReader(InputStream in) throws IOException;
}