/**
 * 
 */
package dict.build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * @author Jennifer
 *
 */
public class PosProbability {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		File dictFile = new File("sogou.dic");
		File ppFile = new File(dictFile.getParentFile(), "dict/build/pos_prop.txt");
		try(BufferedReader br = Files.newReader(dictFile, Charsets.UTF_8);
				BufferedWriter pw = Files.newWriter(ppFile, Charsets.UTF_8);
				) {
			String line = null;
			Map<String, CounterMap> pp = Maps.newHashMap();
			while (null != (line = br.readLine())) {
				String[] seg = line.split("\t");
//				int freq = Integer.parseInt(seg[2]);
				int freq = 1;
				for (int i = 0; i < seg[0].length(); ++i) {
					String label = null;
					if (0 == i) {
						label = "S";
					} else if (seg[0].length() - 1 == i) {
						label = "E";
					} else {
						label = "M";
					}
					String key = seg[0].substring(i, i + 1);
					if (pp.containsKey(key)) {
						pp.get(key).incrby(label, freq);
					} else {
						CounterMap cm = new CounterMap();
						cm.incrby(label, freq);
						pp.put(key, cm);
					}
				}
			}
			String[] labels = new String[]{"S", "M", "E"};
			for (String key : pp.keySet()) {
				int total = 0;
				for (String l : labels) {
					total += pp.get(key).get(l);
				}
				if (0 == total) 
					continue;
				StringBuilder bui = new StringBuilder();
				bui.append(key);
				for (String l : labels) {
					bui.append("\t").append(pp.get(key).get(l) * 1.0 / total);
				}
				bui.append("\n");
				pw.write(bui.toString());
			}
		}
	}
}
