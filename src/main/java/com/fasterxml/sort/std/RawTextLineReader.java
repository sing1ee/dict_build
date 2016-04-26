package com.fasterxml.sort.std;

import java.io.*;
import java.util.Arrays;

import com.fasterxml.sort.*;

/**
 * Efficient reader for data that consists of text lines, i.e. character
 * data separated by one of standard line feeds (CR, LF or CR+LF).
 * For efficiency no decoding is done
 */
public class RawTextLineReader
    extends DataReader<byte[]>
{
    protected final static byte BYTE_CR = (byte) '\r';
    protected final static byte BYTE_LF = (byte) '\n';
    
    protected final InputStream _in;

    protected boolean _closed = false;
    
    protected byte[] _inputBuffer = new byte[16000];
    protected int _inputPtr = 0;
    protected int _inputEnd = 0;
    
    /**
     * Marker we set if the last line ended with a CR, since it
     * may be followed by a trailing LF as part of two-byte linefeed.
     */
    protected boolean _hadCR = false;

    protected ByteArrayOutputStream _tmpBytes;
    
    public RawTextLineReader(InputStream in)
    {
        _in = in;
    }

    /**
     * Convenience method for instantiating factory to create instances of
     * this {@link DataReader}.
     */
    public static Factory factory() {
        return new Factory();
    }    
    
    @Override
    public void close() throws IOException
    {
        if (!_closed) {
            _closed = true;
            _in.close();
        }
    }

    @Override
    public int estimateSizeInBytes(byte[] item)
    {
        // Wild guess: array objects take at least 8 bytes, probably 12 or 16.
        // And size of actual array storage rounded up to 4-byte alignment. So:

        int bytes = item.length;
        bytes = ((bytes + 3) >> 2) << 2;
        return 16 + bytes;
    }

    @Override
    public byte[] readNext() throws IOException
    {
        if (_closed) {
            return null;
        }
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                close();
                return null;
            }
        }

        // first thing(s) first: skip a linefeed we might have
        if (_hadCR) {
            if (!_skipLF()) {
                return null;
            }
        }

        // set the start point after our call to _skipLF() so that if a linefeed is skipped, we also skip it in Arrays.copyOfRange below
        final int start = _inputPtr;

        // then common case: we find full row:
        final int end = _inputEnd;
        while (_inputPtr < end) {
            byte b = _inputBuffer[_inputPtr++];
            if (b == BYTE_CR || b == BYTE_LF) {
                _hadCR = (b == BYTE_CR);
                return Arrays.copyOfRange(_inputBuffer, start, _inputPtr-1);
            }
        }
        // but if not, need to buffer
        return _readNextSlow(start);
    }

    protected final byte[] _readNextSlow(int start) throws IOException
    {
        ByteArrayOutputStream bytes = _tmpBytes;
        if (bytes == null) {
            _tmpBytes = bytes = new ByteArrayOutputStream();
        } else {
            bytes.reset();
        }
        // add stuff we have seen so far, and...
        bytes.write(_inputBuffer, start, _inputEnd - start);

        main_loop:        
        while (true) {
            if (!_loadMore()) {
                close();
                break;
            }
            for (int i = 0, end = _inputEnd; i < end; ++i) {
                byte b = _inputBuffer[_inputPtr++];
                if (b == BYTE_CR || b == BYTE_LF) {
                    _hadCR = (b == BYTE_CR);
                    bytes.write(_inputBuffer, 0, _inputPtr-1);
                    break main_loop;
                }
            }
        }
        return bytes.toByteArray();
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected boolean _loadMore() throws IOException
    {
        int count = _in.read(_inputBuffer);
        if (count < 0) {
            return false;
        }
        _inputPtr = 0;
        _inputEnd = count;
        return true;
    }

    protected boolean _skipLF() throws IOException
    {
        _hadCR = false;
        if (_inputBuffer[_inputPtr] == BYTE_LF) {
            ++_inputPtr;
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    close();
                    return false;
                }
            }
        }
        return true;
    }
    
    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */
    
    public static class Factory
        extends DataReaderFactory<byte[]>
    {
        @Override
        public DataReader<byte[]> constructReader(InputStream in) {
            return new RawTextLineReader(in);
        }
    }        
}
