package dict.build;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <code>TernaryTree</code> is an implementation of a ternary tree. Methods are
 * provided for inserting strings and searching for strings. The algorithms in
 * this class are all recursive, and have not been optimized for any particular
 * purpose. Data which is inserted is not sorted before insertion, however data
 * can be inserted beginning with the median of the supplied data.
 * 
 * @author Middleware Services
 * @version $Revision$ $Date$
 */
@Deprecated
public class TernaryTree {

	/** File system line separator. */
	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	/** root node of the ternary tree. */
	private TernaryNode root;

	/** Default Constructor. */
	public TernaryTree() {
	}

	/**
	 * This will insert the supplied word into the <code>TernaryTree</code>.
	 * 
	 * @param word
	 *            <code>String</code> to insert
	 */
	public void insert(final String word, final int value) {
		if (word != null) {
			this.root = insertNode(this.root, word, 0, value);
		}
	}

	/**
	 * This will return true if the supplied word has been inserted into the
	 * <code>TernaryTree</code>.
	 * 
	 * @param word
	 *            <code>String</code> to search for
	 * 
	 * @return <code>boolean</code> - whether word was found
	 */
	public int search(final String word) {
		return  this.searchNode(this.root, word, 0);
	}

	/**
	 * This will return an array of strings which partially match the supplied
	 * word. word should be of the format '.e.e.e' Where the '.' character
	 * represents any valid character. Possible results from this query include:
	 * Helene, delete, or severe Note that no substring matching occurs, results
	 * only include strings of the same length. If the supplied word does not
	 * contain the '.' character, then a regular search is performed.
	 * 
	 * @param word
	 *            <code>String</code> to search for
	 * 
	 * @return <code>String[]</code> - of matching words
	 */
	public String[] partialSearch(final String word) {
		String[] results = null;
		final List<String> matches = this.partialSearchNode(this.root,
				new ArrayList<String>(), "", word, 0);
		if (matches == null) {
			results = new String[] {};
		} else {
			results = matches.toArray(new String[matches.size()]);
		}
		return results;
	}

	/**
	 * This will return an array of strings which are near to the supplied word
	 * by the supplied distance. For the query nearSearch("fisher", 2): Possible
	 * results include: cipher, either, fishery, kosher, sister. If the supplied
	 * distance is not > 0, then a regular search is performed.
	 * 
	 * @param word
	 *            <code>String</code> to search for
	 * @param distance
	 *            <code>int</code> for valid match
	 * 
	 * @return <code>String[]</code> - of matching words
	 */
	public String[] nearSearch(final String word, final int distance) {
		String[] results = null;
		final List<String> matches = this.nearSearchNode(this.root, distance,
				new ArrayList<String>(), "", word, 0);
		if (matches == null) {
			results = new String[] {};
		} else {
			results = matches.toArray(new String[matches.size()]);
		}
		return results;
	}

	/**
	 * This will return a list of all the words in this <code>
	 * TernaryTree</code>. This is a very expensive operation, every node in the
	 * tree is traversed. The returned list cannot be modified.
	 * 
	 * @return <code>String[]</code> - of words
	 */
	public List<String> getWords() {
		final List<String> words = this.traverseNode(this.root, "",
				new ArrayList<String>());
		return Collections.unmodifiableList(words);
	}

	/**
	 * This will print an ASCII representation of this <code>TernaryTree</code>
	 * to the supplied <code>PrintWriter</code>. This is a very expensive
	 * operation, every node in the tree is traversed. The output produced is
	 * hard to read, but it should give an indication of whether or not your
	 * tree is balanced.
	 * 
	 * @param out
	 *            <code>PrintWriter</code> to print to
	 * @throws IOException
	 *             if an error occurs
	 */
	public void print(final Writer out) throws IOException {
		out.write(printNode(this.root, "", 0));
	}

	/**
	 * This will recursively insert a word into the <code>TernaryTree</code> one
	 * node at a time beginning at the supplied node.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to put character in
	 * @param word
	 *            <code>String</code> to be inserted
	 * @param index
	 *            <code>int</code> of character in word
	 * 
	 * @return <code>TernaryNode</code> - to insert
	 */
	private TernaryNode insertNode(TernaryNode node, final String word,
			final int index, final int value) {
		if (index < word.length()) {
			final char c = word.charAt(index);
			if (node == null) {
				node = new TernaryNode(c);
			}

			final char split = node.getSplitChar();
			if (c < split) {
				node.setLokid(insertNode(node.getLokid(), word, index, value));
			} else if (c == split) {
				if (index == word.length() - 1) {
					node.setEndOfWord(value);
				}
				node.setEqkid(insertNode(node.getEqkid(), word, index + 1,
						value));
			} else {
				node.setHikid(insertNode(node.getHikid(), word, index, value));
			}
		}
		return node;
	}

	/**
	 * This will recursively search for a word in the <code>TernaryTree</code>
	 * one node at a time beginning at the supplied node.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to search in
	 * @param word
	 *            <code>String</code> to search for
	 * @param index
	 *            <code>int</code> of character in word
	 * 
	 * @return <code>boolean</code> - whether or not word was found
	 */
	private int searchNode(final TernaryNode node, final String word,
			final int index) {
		if (node != null && index < word.length()) {
			final char c = word.charAt(index);
			final char split = node.getSplitChar();
			if (c < split) {
				return searchNode(node.getLokid(), word, index);
			} else if (c > split) {
				return searchNode(node.getHikid(), word, index);
			} else {
				if (index == word.length() - 1) {
					if (node.isEndOfWord()) {
						return node.getValue();
					}
				} else {
					return searchNode(node.getEqkid(), word, index + 1);
				}
			}
		}
		return -1;
	}

	/**
	 * This will recursively search for a partial word in the <code>
	 * TernaryTree</code> one node at a time beginning at the supplied node.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to search in
	 * @param matches
	 *            <code>ArrayList</code> of partial matches
	 * @param match
	 *            <code>String</code> the current word being examined
	 * @param word
	 *            <code>String</code> to search for
	 * @param index
	 *            <code>int</code> of character in word
	 * 
	 * @return <code>ArrayList</code> - of matches
	 */
	private List<String> partialSearchNode(final TernaryNode node,
			List<String> matches, final String match, final String word,
			final int index) {
		if (node != null && index < word.length()) {
			final char c = word.charAt(index);
			final char split = node.getSplitChar();
			if (c == '.' || c < split) {
				matches = partialSearchNode(node.getLokid(), matches, match,
						word, index);
			}
			if (c == '.' || c == split) {
				if (index == word.length() - 1) {
					if (node.isEndOfWord()) {
						matches.add(match + split);
					}
				} else {
					matches = partialSearchNode(node.getEqkid(), matches, match
							+ split, word, index + 1);
				}
			}
			if (c == '.' || c > split) {
				matches = partialSearchNode(node.getHikid(), matches, match,
						word, index);
			}
		}
		return matches;
	}

	/**
	 * This will recursively search for a near match word in the <code>
	 * TernaryTree</code> one node at a time beginning at the supplied node.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to search in
	 * @param distance
	 *            <code>int</code> of a valid match, must be > 0
	 * @param matches
	 *            <code>ArrayList</code> of near matches
	 * @param match
	 *            <code>String</code> the current word being examined
	 * @param word
	 *            <code>String</code> to search for
	 * @param index
	 *            <code>int</code> of character in word
	 * 
	 * @return <code>ArrayList</code> - of matches
	 */
	private List<String> nearSearchNode(final TernaryNode node,
			final int distance, List<String> matches, final String match,
			final String word, final int index) {
		if (node != null && distance >= 0) {

			final char c;
			if (index < word.length()) {
				c = word.charAt(index);
			} else {
				c = (char) -1;
			}

			final char split = node.getSplitChar();

			if (distance > 0 || c < split) {
				matches = nearSearchNode(node.getLokid(), distance, matches,
						match, word, index);
			}

			final String newMatch = match + split;
			if (c == split) {

				if (node.isEndOfWord() && distance >= 0
						&& newMatch.length() + distance >= word.length()) {
					matches.add(newMatch);
				}

				matches = nearSearchNode(node.getEqkid(), distance, matches,
						newMatch, word, index + 1);
			} else {

				if (node.isEndOfWord() && distance - 1 >= 0
						&& newMatch.length() + distance - 1 >= word.length()) {
					matches.add(newMatch);
				}

				matches = nearSearchNode(node.getEqkid(), distance - 1,
						matches, newMatch, word, index + 1);
			}

			if (distance > 0 || c > split) {
				matches = nearSearchNode(node.getHikid(), distance, matches,
						match, word, index);
			}
		}
		return matches;
	}

	/**
	 * This will recursively traverse every node in the <code>TernaryTree</code>
	 * one node at a time beginning at the supplied node. The result is a string
	 * representing every word, which is delimited by the LINE_SEPARATOR
	 * character.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to begin traversing
	 * @param s
	 *            <code>String</code> of words found at the supplied node
	 * @param words
	 *            <code>ArrayList</code> which will be returned (recursive
	 *            function)
	 * 
	 * @return <code>String</code> - containing all words from the supplied node
	 */
	private List<String> traverseNode(final TernaryNode node, final String s,
			List<String> words) {
		if (node != null) {

			words = this.traverseNode(node.getLokid(), s, words);

			final String c = String.valueOf(node.getSplitChar());
			if (node.getEqkid() != null) {
				words = this.traverseNode(node.getEqkid(), s + c, words);
			}

			if (node.isEndOfWord()) {
				words.add(s + c);
			}

			words = this.traverseNode(node.getHikid(), s, words);
		}
		return words;
	}

	/**
	 * This will recursively traverse every node in the <code>TernaryTree</code>
	 * one node at a time beginning at the supplied node. The result is an ASCII
	 * string representation of the tree beginning at the supplied node.
	 * 
	 * @param node
	 *            <code>TernaryNode</code> to begin traversing
	 * @param s
	 *            <code>String</code> of words found at the supplied node
	 * @param depth
	 *            <code>int</code> of the current node
	 * 
	 * @return <code>String</code> - containing all words from the supplied node
	 */
	private String printNode(final TernaryNode node, final String s,
			final int depth) {
		final StringBuffer buffer = new StringBuffer();
		if (node != null) {
			buffer.append(this.printNode(node.getLokid(), " <-", depth + 1));

			final String c = String.valueOf(node.getSplitChar());
			final StringBuffer eq = new StringBuffer();
			if (node.getEqkid() != null) {
				eq.append(this.printNode(node.getEqkid(), s + c + "--",
						depth + 1));
			} else {
				int count = (new StringTokenizer(s, "--")).countTokens();
				if (count > 0) {
					count--;
				}
				for (int i = 1; i < depth - count - 1; i++) {
					eq.append("   ");
				}
				eq.append(s).append(c).append(TernaryTree.LINE_SEPARATOR);
			}
			buffer.append(eq);

			buffer.append(this.printNode(node.getHikid(), " >-", depth + 1));
		}
		return buffer.toString();
	}
	
	public static void main(String[] args) {
		TernaryTree tt = new TernaryTree();
		tt.insert("a", 1);
		tt.insert("aa", 2);
		tt.insert("aaa", 3);
		tt.insert("aaaa", 4);
		System.out.println(tt.search("aaa"));
	}
}