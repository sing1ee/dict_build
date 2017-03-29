package dict.build;

@Deprecated
public class TernaryNode {

	/** character of this node. */
	private char splitchar;

	/** whether this character is the end of a word. */
	private int value;

	/** low child of this node. */
	private TernaryNode lokid;

	/** equal child of this node. */
	private TernaryNode eqkid;

	/** high child of this node. */
	private TernaryNode hikid;

	/**
	 * This will create a new <code>TernaryNode</code> with the supplied
	 * character.
	 * 
	 * @param c
	 *            <code>char</code>
	 */
	public TernaryNode(final char c) {
		this.splitchar = c;
	}

	/**
	 * This returns the splitchar of this <code>TernaryNode</code>.
	 * 
	 * @return <code>char</code>
	 */
	public char getSplitChar() {
		return this.splitchar;
	}

	/**
	 * This sets the splitchar for this <code>TernaryNode</code>.
	 * 
	 * @param c
	 *            <code>char</code>
	 */
	public void setSplitChar(final char c) {
		this.splitchar = c;
	}

	/**
	 * This returns the endOfWord for this <code>TernaryNode</code>.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean isEndOfWord() {
		return this.value > 0;
	}

	public int getValue() {
		return this.value;
	}

	/**
	 * This sets the endOfWord for this <code>TernaryNode</code>.
	 * 
	 * @param b
	 *            <code>boolean</code>
	 */
	public void setEndOfWord(final int b) {
		this.value = b;
	}

	/**
	 * This returns the lokid of this <code>TernaryNode</code>.
	 * 
	 * @return <code>TernaryNode</code>
	 */
	public TernaryNode getLokid() {
		return this.lokid;
	}

	/**
	 * This sets the lokid of this <code>TernaryNode</code>.
	 * 
	 * @param node
	 *            <code>TernaryNode</code>
	 */
	public void setLokid(final TernaryNode node) {
		this.lokid = node;
	}

	/**
	 * This returns the eqkid of this <code>TernaryNode</code>.
	 * 
	 * @return <code>TernaryNode</code>
	 */
	public TernaryNode getEqkid() {
		return this.eqkid;
	}

	/**
	 * This sets the eqkid of this <code>TernaryNode</code>.
	 * 
	 * @param node
	 *            <code>TernaryNode</code>
	 */
	public void setEqkid(final TernaryNode node) {
		this.eqkid = node;
	}

	/**
	 * This returns the hikid of this <code>TernaryNode</code>.
	 * 
	 * @return <code>TernaryNode</code>
	 */
	public TernaryNode getHikid() {
		return this.hikid;
	}

	/**
	 * This sets the hikid of this <code>TernaryNode</code>.
	 * 
	 * @param node
	 *            <code>TernaryNode</code>
	 */
	public void setHikid(final TernaryNode node) {
		this.hikid = node;
	}
}