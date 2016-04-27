package dict.build;

import java.io.*;

import com.fasterxml.sort.*;

public class LineWriter
    extends DataWriter<String>
{
    
    protected final BufferedWriter _out;


    public LineWriter(OutputStream out) {
    	_out = new BufferedWriter(new OutputStreamWriter(out));
    }


    public static Factory factory() {
        return new Factory();
    }

    
    @Override
    public void close() throws IOException {
    	_out.close();
    }

    @Override
    public void writeEntry(String item) throws IOException
    {
    	_out.write(item + "\n");
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
        extends DataWriterFactory<String>
    {
        public Factory() {
        }


        
        @Override
        public DataWriter<String> constructWriter(OutputStream out) {
            return new LineWriter(out);
        }
    }
}
