/**
 * 
 */
package dict.build;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jennifer
 * 
 */
public class CounterMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3903452740943758085L;

	private Map<String, Integer> count = new ConcurrentHashMap<String, Integer>();

	public CounterMap() {
	}

	public CounterMap(int capacitySize) {
		count = new ConcurrentHashMap<String, Integer>(capacitySize);
	}

	public void incr(String key) {
		if (count.containsKey(key)) {
			count.put(key, count.get(key) + 1);
		} else {
			count.put(key, 1);
		}
	}
	
	public void incrby(String key, int delta) {
		if (count.containsKey(key)) {
			count.put(key, count.get(key) + delta);
		} else {
			count.put(key, delta);
		}
	}
	
	public int get(String key) {
		Integer value =  count.get(key);
		if (null == value)
			return 0;
		return value;
	}
	
	public Map<String, Integer> countAll() {
		return count;
	}
}
