/**
 * 
 */
package dict.build;

/**
 * @author zhangcheng
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("rawpath");
			return;
		}
		
		String rawpath = null;
		if (args.length > 0) {
			rawpath = args[0];
		}
		
		String left = null;
		String right = null;
		String entropyfile = null;

		FastBuilder builder = new FastBuilder();

		if (null == right)
			right = builder.genFreqRight(rawpath, 6, 10 * 1024);
		if (null == left)
			left = builder.genLeft(rawpath, 6, 10 * 1024);
		if (null == entropyfile)
			entropyfile = builder.mergeEntropy(right, left);

		builder.extractWords(right, entropyfile);
	}
}
