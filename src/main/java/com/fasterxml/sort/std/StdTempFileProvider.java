package com.fasterxml.sort.std;

import java.io.*;

import com.fasterxml.sort.TempFileProvider;

/**
 * Default {@link TempFileProvider} implementation which uses JDK default
 * temporary file generation mechanism.
 * 
 * @author tatu
 */
public class StdTempFileProvider
    implements TempFileProvider
{
    /**
     * Default temporary file prefix to use.
     */
    public final static String DEFAULT_PREFIX = "j-merge-sort-";

    /**
     * Default temporary file suffix to use.
     */
    public final static String DEFAULT_SUFFIX = ".tmp";
    
    protected final String _prefix;
    protected final String _suffix;
    
    public StdTempFileProvider() { this(DEFAULT_PREFIX, DEFAULT_SUFFIX); }
    public StdTempFileProvider(String prefix, String suffix) {
        _prefix = prefix;
        _suffix = suffix;
    }
    
    @Override
    public File provide() throws IOException
    {
        File f = File.createTempFile(_prefix, _suffix);
        f.deleteOnExit();
        return f;
    }
}
