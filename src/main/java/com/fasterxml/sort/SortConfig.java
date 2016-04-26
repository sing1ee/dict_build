package com.fasterxml.sort;

import com.fasterxml.sort.std.StdTempFileProvider;

/**
 * Configuration object used for changing details of sorting
 * process. Default settings are usable, so often
 * instance is created without arguments and used as is.
 */
public class SortConfig
{
    /**
     * By default we will use 40 megs for pre-sorting.
     */
    public final static long DEFAULT_MEMORY_USAGE = 40 * 1024 * 1024;

    /**
     * Default merge sort is 16-way sort (using 16 input files concurrently)
     */
    public final static int DEFAULT_MERGE_FACTOR = 16;

    protected int _mergeFactor;

    protected long _maxMemoryUsage;
    
    protected TempFileProvider _tempFileProvider;

    /*
    /************************************************************************
    /* Construction
    /************************************************************************
     */

    public SortConfig()
    {
        _mergeFactor = DEFAULT_MERGE_FACTOR;
        _maxMemoryUsage = DEFAULT_MEMORY_USAGE;
        _tempFileProvider = new StdTempFileProvider();
    }

    protected SortConfig(SortConfig base, int mergeFactor) {
        _maxMemoryUsage = base._maxMemoryUsage;
        _mergeFactor = mergeFactor;
        _tempFileProvider = base._tempFileProvider;
    }
    
    protected SortConfig(SortConfig base, long maxMem) {
        _maxMemoryUsage = maxMem;
        _mergeFactor = base._mergeFactor;
        _tempFileProvider = base._tempFileProvider;
    }

    protected SortConfig(SortConfig base, TempFileProvider prov) {
        _mergeFactor = base._mergeFactor;
        _maxMemoryUsage = base._maxMemoryUsage;
        _tempFileProvider = prov;
    }
    
    /*
    /************************************************************************
    /* Accessors
    /************************************************************************
     */

    public int getMergeFactor() { return _mergeFactor; }
    
    public long getMaxMemoryUsage() { return _maxMemoryUsage; }

    public TempFileProvider getTempFileProvider() { return _tempFileProvider; }
    
    /*
    /************************************************************************
    /* Fluent construction methods
    /************************************************************************
     */
    
    /**
     * Method for constructing configuration instance that defines that maximum amount
     * of memory to use for pre-sorting. This is generally a crude approximation and
     * implementations make best effort to honor it.
     * 
     * @param maxMem Maximum memory that pre-sorted should use for in-memory sorting
     * @return New 
     */
    public SortConfig withMaxMemoryUsage(long maxMem)
    {
        if (maxMem == _maxMemoryUsage) {
            return this;
        }
        return new SortConfig(this, maxMem);
    }

    public SortConfig withTempFileProvider(TempFileProvider provider)
    {
        if (provider == _tempFileProvider) {
            return this;
        }
        return new SortConfig(this, provider);
    }

}
