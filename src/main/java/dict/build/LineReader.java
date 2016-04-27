package dict.build;

import java.io.*;
import java.util.Arrays;

import com.fasterxml.sort.*;

/**
 * Efficient reader for data that consists of text lines, i.e. character
 * data separated by one of standard line feeds (CR, LF or CR+LF).
 * For efficiency no decoding is done
 */
public class LineReader
    extends DataReader<String>
{
    
    protected final BufferedReader _br;

    
    public LineReader(InputStream in)
    {
        _br = new BufferedReader(new InputStreamReader(in));
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
       _br.close();
    }

    @Override
    public int estimateSizeInBytes(String item)
    {
        // Wild guess: array objects take at least 8 bytes, probably 12 or 16.
        // And size of actual array storage rounded up to 4-byte alignment. So:

        int bytes = item.getBytes().length;
        bytes = ((bytes + 3) >> 2) << 2;
        return 16 + bytes;
    }

    @Override
    public String readNext() throws IOException
    {
        
    	return _br.readLine();
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */
    
    public static class Factory
        extends DataReaderFactory<String>
    {
        @Override
        public DataReader<String> constructReader(InputStream in) {
            return new LineReader(in);
        }
    }        
}
