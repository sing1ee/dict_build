package com.fasterxml.sort.util;

import java.util.Arrays;

/**
 * Helper class used instead of a standard JDK list or buffer,
 * to avoid constant re-allocations.
 */
public class SegmentedBuffer
{
    // // // Config constants

    /**
     * Let's start with relatively small chunks
     */
    final static int INITIAL_CHUNK_SIZE = 1024;

    /**
     * Also: let's expand by doubling up until 16k entry chunks (which is 64k 
     * in size for 32-bit machines)
     */
    final static int MAX_CHUNK_SIZE = (1 << 14);

    // // // Data storage

    private Node _bufferHead;

    private Node _bufferTail;

    /**
     * Number of total buffered entries in this buffer, counting all instances
     * within linked list formed by following {@link #_bufferHead}.
     */
    private int _bufferedEntryCount;

    // // // Simple reuse

    /**
     * Reusable Object array, stored here after buffer has been released having
     * been used previously.
     */
    private Object[] _freeBuffer;

    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */

    public SegmentedBuffer() { }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    /**
     * Method called to start buffering process. Will ensure that the buffer
     * is empty, and then return an object array to start chunking content on
     */
    public Object[] resetAndStart()
    {
        if (_bufferedEntryCount > 0) {
            _reset();
        }
        if (_freeBuffer == null) {
            return new Object[INITIAL_CHUNK_SIZE];
        }
        return _freeBuffer;
    }

    /**
     * Method called to add a full Object array as a chunk buffered within
     * this buffer, and to obtain a new array to fill. Caller is not to use
     * the array it gives; but to use the returned array for continued
     * buffering.
     *
     * @param fullChunk Completed chunk that the caller is requesting
     *   to append to this buffer. It is generally chunk that was
     *   returned by an earlier call to {@link #resetAndStart} or
     *   {@link #appendCompletedChunk} (although this is not required or
     *   enforced)
     *
     * @return New chunk buffer for caller to fill
     */
    public Object[] appendCompletedChunk(Object[] fullChunk)
    {
        Node next = new Node(fullChunk);
        if (_bufferHead == null) { // first chunk
            _bufferHead = _bufferTail = next;
        } else { // have something already
            _bufferTail.linkNext(next);
            _bufferTail = next;
        }
        int len = fullChunk.length;
        _bufferedEntryCount += len;
        // double the size for small chunks
        if (len < MAX_CHUNK_SIZE) {
            len += len;
        } else { // but by +25% for larger (to limit overhead)
            len += (len >> 2);
        }
        return new Object[len];
    }

    /**
     * Method called to indicate that the buffering process is now
     * complete; and to construct a combined exactly-sized result
     * array. Additionally the buffer itself will be reset to
     * reduce memory retention.
     *<p>
     * Resulting array will be of generic <code>Object[]</code> type:
     * if a typed array is needed, use the method with additional
     * type argument.
     */
    public Object[] completeAndClearBuffer(Object[] lastChunk, int lastChunkEntries)
    {
        int totalSize = lastChunkEntries + _bufferedEntryCount;
        Object[] result = new Object[totalSize];
        _copyTo(result, totalSize, lastChunk, lastChunkEntries);
        // [Issue-5]: should reduce mem usage here
        _reset();
        return result;
    }
        
    /**
     * Helper method that can be used to check how much free capacity
     * will this instance start with. Can be used to choose the best
     * instance to reuse, based on size of reusable object chunk
     * buffer holds reference to.
     */
    public int initialCapacity()
    {
        return (_freeBuffer == null) ? 0 : _freeBuffer.length;
    }

    /**
     * Method that can be used to check how many Objects have been buffered
     * within this buffer.
     */
    public int bufferedSize() { return _bufferedEntryCount; }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void _reset()
    {
        // can we reuse the last (and thereby biggest) array for next time?
        if (_bufferedEntryCount > 0) {
            if (_bufferTail != null) {
                Object[] obs = _bufferTail.getData();
                // also, let's clear it of contents as well, just in case
                Arrays.fill(obs, null);
                _freeBuffer = obs;
            }
            // either way, must discard current contents
            _bufferHead = _bufferTail = null;
            _bufferedEntryCount = 0;
        }
    }

    private final void _copyTo(Object resultArray, int totalSize,
                                 Object[] lastChunk, int lastChunkEntries)
    {
        int ptr = 0;

        for (Node n = _bufferHead; n != null; n = n.next()) {
            Object[] curr = n.getData();
            int len = curr.length;
            System.arraycopy(curr, 0, resultArray, ptr, len);
            ptr += len;
        }
        System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
        ptr += lastChunkEntries;

        // sanity check (could have failed earlier due to out-of-bounds, too)
        if (ptr != totalSize) {
            throw new IllegalStateException("Should have gotten "+totalSize+" entries, got "+ptr);
        }
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    /**
     * Helper class used to store actual data, in a linked list.
     */
    private final static class Node
    {
        /**
         * Data stored in this node. Array is considered to be full.
         */
        private final Object[] _data;

        private Node _next;

        public Node(Object[] data) {
            _data = data;
        }

        public Object[] getData() { return _data; }

        public Node next() { return _next; }

        public void linkNext(Node next)
        {
            if (_next != null) { // sanity check
                throw new IllegalStateException();
            }
            _next = next;
        }
    }
}
