package com.fasterxml.sort;

import java.io.IOException;
import java.util.Iterator;

/**
 * We need an unchecked exception to work with {@link Iterator}, and
 * want a specific subtype to catch.
 */
public class IterableSorterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IterableSorterException(IOException cause) {
        super(cause);
    }
}