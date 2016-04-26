package com.fasterxml.sort;

import java.io.IOException;

/**
 * Interface that defines how calling application can interact with a {@link Sorter}; both
 * by accessing progress information and by requesting cancellation if necessary.
 * It is implemented by {@link Sorter}.
 */
public interface SortingState
{
    /**
     * Different phases that sorter goes through
     */
    public enum Phase {
        PRE_SORTING,
        SORTING,
        COMPLETE
    }

    /*
    /************************************************************************
    /* Accessors
    /************************************************************************
     */

    public Phase getPhase();
    
    /**
     * Accessor for determining whether sorter is in its in-memory pre-sorting phase.
     */
    public boolean isPreSorting();
    
    /**
     * Accessor for determining whether sorter is in regular merge-sort phase or not.
     */
    public boolean isSorting();

    /**
     * Accessor for determining whether sorting has been successfully completed or not.
     */
    public boolean isCompleted();

    /**
     * Accessor for checking how many pre-sort files were created during
     * pre-sort phase. Can be zero if the whole data fit in memory during
     * pre-sorting.
     */
    public int getNumberOfPreSortFiles();
    
    /**
     * Accessor for checking which sorting round sorter is doing: for pre-sort
     * it basically means number of segment (0-based) that is being processed
     * in-memory, for regular sort it is number of (0-based) sorting round.
     */
    public int getSortRound();

    /**
     * Accessor for figuring out how many regular sorting rounds need to be taken to
     * complete sorting, if known. If information is not known, will return -1.
     * This information generally becomes available after pre-sorting round.
     */
    public int getNumberOfSortRounds();
    
    /*
    /************************************************************************
    /* Cancellation
    /************************************************************************
     */

    /**
     * Method that can be used to try to cancel executing sort operation.
     * No exception will be thrown; sorting will just be stopped as soon as
     * sorting thread notices request.
     */
    public void cancel();
    
    /**
     * Method that can be used to try to cancel executing sort operation.
     * Exception object can be specified; if non-null instance is given,
     * it will be thrown to indicate erroneous result, otherwise sorting is
     * just interrupted but execution returns normally.
     */
    public void cancel(RuntimeException e);

    /**
     * Method that can be used to try to cancel executing sort operation.
     * Exception object can be specified; if non-null instance is given,
     * it will be thrown to indicate erroneous result, otherwise sorting is
     * just interrupted but execution returns normally.
     */
    public void cancel(IOException e);
}
