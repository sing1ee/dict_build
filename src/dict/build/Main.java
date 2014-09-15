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
		
		FastBuilder builder = new FastBuilder();

		String freqRight = builder.genFreqRight(args[0], 6, 10 * 1024);
		String left = builder.genLeft(args[0], 6, 10 * 1024);
		
		String entropyfile = builder.mergeEntropy(freqRight, left);

		builder.extractWords(freqRight, entropyfile);
	}
}
