package dict.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.Sorter;

/**
 * Basic {@link Sorter} implementation that operates on text line input.
 */
public class SplitFileSorter extends Sorter<String>
{
    /**
     * Let's limit maximum memory used for pre-sorting when invoked from command-line to be
     * 256 megs
     */
    public final static long MAX_HEAP_FOR_PRESORT = 256L * 1024 * 1024;

    /**
     * Also just in case our calculations are wrong, require 10 megs for pre-sort anyway
     * (if invoked from CLI)
     */
    public final static long MIN_HEAP_FOR_PRESORT = 10L * 1024 * 1024;
    
    public SplitFileSorter() {
        this(new SortConfig());
    }
    
    public SplitFileSorter(SortConfig config)
    {
        super(config,
                LineReader.factory(), LineWriter.factory(),
                new SplitStringComparator());
    }

    /*
    /********************************************************************** 
    /* Main method for simple command-line operation for line-based
    /* sorting using default ISO-8859-1 collation (i.e. byte-by-byte sorting)
    /********************************************************************** 
     */
    
    public static void main(String[] args) throws Exception
    {
        if (args.length > 1) {
            System.err.println("Usage: java "+SplitFileSorter.class.getName()+" [input-file]");
            System.err.println("(where input-file is optional; if missing, read from STDIN)");
            System.exit(1);
        }
        
        // One more thing: use 50% of memory (but no more than 200 megs) for pre-sort
        // minor tweak: consider first 40 megs to go for other overhead...
        long availMem = Runtime.getRuntime().maxMemory() - (40 * 1024 * 1024);
        long maxMem = (availMem >> 1);
        if (maxMem > MAX_HEAP_FOR_PRESORT) {
            maxMem = MAX_HEAP_FOR_PRESORT;
        } else if (maxMem < MIN_HEAP_FOR_PRESORT) {
            maxMem = MIN_HEAP_FOR_PRESORT;
        }
        final SplitFileSorter sorter = new SplitFileSorter(new SortConfig().withMaxMemoryUsage(maxMem));
        final InputStream in;
        
        if (args.length == 0) {
            in = System.in;
        } else {
            File input = new File(args[0]);
            if (!input.exists() || input.isDirectory()) {
                System.err.println("File '"+input.getAbsolutePath()+"' does not exist (or is not file)");
                System.exit(2);
            }
            in = new FileInputStream(input);
        }

        // To be able to print out progress, need to spin one additional thread...
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                try {
                    while (!sorter.isCompleted()) {
                        Thread.sleep(5000L);
                        if (sorter.isPreSorting()) {
                            System.err.printf(" pre-sorting: %d files written\n", sorter.getNumberOfPreSortFiles());
                        } else if (sorter.isSorting()) {
                            System.err.printf(" sorting, round: %d/%d\n",
                                    sorter.getSortRound(), sorter.getNumberOfSortRounds());
                        }
                    }
                    double secs = (System.currentTimeMillis() - start) / 1000.0;
                    System.err.printf("Completed: took %.1f seconds.\n", secs);
                } catch (InterruptedException e) {
                    double secs = (System.currentTimeMillis() - start) / 1000.0;
                    System.err.printf("[INTERRUPTED] -- took %.1f seconds.\n", secs);
                }
            } 
        }).start();
        sorter.sort(in, System.out);
    }
}
