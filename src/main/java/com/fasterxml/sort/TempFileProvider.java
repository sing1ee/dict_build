package com.fasterxml.sort;

import java.io.File;
import java.io.IOException;

/**
 * Interface used for object that can handle constructing of temporary files that are
 * needed during sort and non-final merge phases.
 * 
 * @author tatu
 *
 */
public interface TempFileProvider
{
    public File provide() throws IOException;
}
