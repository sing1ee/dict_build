package com.fasterxml.sort.std;

import java.io.*;

import com.fasterxml.sort.*;

public class RawTextLineWriter
    extends DataWriter<byte[]>
{
    protected final static byte[] STD_LINEFEED_CR = new byte[] { '\r' };
    protected final static byte[] STD_LINEFEED_LF = new byte[] { '\n' };
    protected final static byte[] STD_LINEFEED_CRLF = new byte[] { '\r', '\n' };

    protected final static byte[] DEFAULT_LINEFEED = STD_LINEFEED_LF;
    
    protected final OutputStream _out;

    /**
     * Linefeed used after entries
     */
    protected final byte[] _lf;
    
    protected boolean _closed = false;

    public RawTextLineWriter(OutputStream out) {
        this(out, DEFAULT_LINEFEED);
    }

    public RawTextLineWriter(OutputStream out, byte[] linefeed)
    {
        _out = out;
        _lf = linefeed;
    }

    /**
     * Convenience method for instantiating factory to create instances of
     * this {@link DataWriter}.
     */
    public static Factory factory() {
        return new Factory();
    }

    /**
     * Convenience method for instantiating factory to create instances of
     * this {@link DataWriter}.
     */
    public static Factory factory(byte[] linefeed) {
        return new Factory(linefeed);
    }
    
    @Override
    public void close() throws IOException {
        if (!_closed) {
            _closed = true;
            _out.close();
        }
    }

    @Override
    public void writeEntry(byte[] item) throws IOException
    {
        if (_closed) {
            throw new IOException("Can not write using closed DataWriter");
        }
        _out.write(item);
        if (_lf != null) {
            _out.write(_lf);
        }
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */
    
    /**
     * Basic factory implementation. The only noteworthy things are:
     * <ul>
     * <li>Ability to configure linefeed to use (including none, pass null)</li>
     * <li>Writer uses {@link BufferedOutputStream} by default (can be disabled)
     *  </ul>
     */
    public static class Factory
        extends DataWriterFactory<byte[]>
    {
        protected final byte[] _linefeed;
        protected final boolean _addBuffering;
        
        public Factory() {
            this(DEFAULT_LINEFEED);
        }

        public Factory(byte[] linefeed) {
            this(linefeed, true);
        }

        public Factory(byte[] linefeed, boolean addBuffering) {
            _linefeed = linefeed;
            _addBuffering = addBuffering;
        }
        
        @Override
        public DataWriter<byte[]> constructWriter(OutputStream out) {
            if (_addBuffering) {
                if (!(out instanceof BufferedOutputStream)) {
                    out = new BufferedOutputStream(out);
                }
            }
            return new RawTextLineWriter(out, _linefeed);
        }
    }
}
