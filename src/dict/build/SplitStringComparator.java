package dict.build;

import java.util.Comparator;

/**
 * Simple implementation of comparator for byte arrays which
 * will compare using <code>unsigned</code> byte values (meaning
 * that 0xFF is creator than 0x00, for example).
 */
public class SplitStringComparator
    implements Comparator<String>
{
    @Override
    public int compare(String o1, String o2)
    {
    	String[] seg1 = o1.split("\t");
    	String[] seg2 = o2.split("\t");
    	if (4 > seg1.length || 4 > seg2.length) return 1;
    	Double d1 = Double.parseDouble(seg1[1]);
    	Double d2 = Double.parseDouble(seg2[1]);
    	return d2.compareTo(d1);
    }

}
