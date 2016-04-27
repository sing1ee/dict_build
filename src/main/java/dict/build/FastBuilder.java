package dict.build;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.std.TextFileSorter;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * 
 * @author Jennifer
 * 
 */
public class FastBuilder {

	/**
	 * Let's limit maximum memory used for pre-sorting when invoked from
	 * command-line to be 256 megs
	 */
	public final static long MAX_HEAP_FOR_PRESORT = 256L * 1024 * 1024;

	/**
	 * Also just in case our calculations are wrong, require 10 megs for
	 * pre-sort anyway (if invoked from CLI)
	 */
	public final static long MIN_HEAP_FOR_PRESORT = 10L * 1024 * 1024;
	
	
	public final static String stopwords = "的很了么呢是嘛个都也比还这于不与才上用就好在和对挺去后没说";
	
	
	/**
	 * 输入的字符是否是汉字
	 * @param a char
	 * @return boolean
	 */
	public static boolean isChinese(char a) { 
	     int v = (int)a; 
	     return (v >=19968 && v <= 171941); 	
	}
	
	public static boolean allChs(String s){
		if (null == s || "".equals(s.trim())) return false;
		for (int i = 0; i < s.length(); i++) {
			if (!isChinese(s.charAt(i))) return false;
		}
		return true;
	}
	
	public TreeMap<String, double[]> loadPosprop() {
		
		TreeMap<String, double[]> prop = Maps.newTreeMap();
		try {
            System.out.println(FastBuilder.class.getResourceAsStream("/pos_prop.txt"));
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/pos_prop.txt")));
            String l = null;
            while (null != (l = br.readLine())) {
				String[] seg = l.split("\t");
				prop.put(seg[0], new double[]{Double.parseDouble(seg[1]), Double.parseDouble(seg[2]), Double.parseDouble(seg[3])});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	public String parse(String filepath) {

		File in = new File(filepath);
		File out = new File(in.getParentFile(), "out.data");

		try (BufferedReader ir = Files.newReader(in, Charsets.UTF_8);
				BufferedWriter ow = Files.newWriter(out, Charsets.UTF_8);) {
			String line = null;
			while (null != (line = ir.readLine())) {
				String[] seg = line.split(",");
				StringBuilder bui = new StringBuilder();
				for (int i = 6; i < seg.length; ++i) {
					bui.append(seg[i]);
				}
				bui.append("\n");
				ow.write(bui.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.getAbsolutePath();
	}

	private String reverse(String raw) {
		StringBuilder bui = new StringBuilder();
		for (int i = raw.length() - 1; i >= 0; --i)
			bui.append(raw.charAt(i));
		return bui.toString();
	}

	public void sortFile(File in, File out) {
		try {
			long availMem = Runtime.getRuntime().maxMemory()
					- (40 * 1024 * 1024);
			long maxMem = (availMem >> 1);
			if (maxMem > MAX_HEAP_FOR_PRESORT) {
				maxMem = MAX_HEAP_FOR_PRESORT;
			} else if (maxMem < MIN_HEAP_FOR_PRESORT) {
				maxMem = MIN_HEAP_FOR_PRESORT;
			}
			final TextFileSorter sorter = new TextFileSorter(
					new SortConfig().withMaxMemoryUsage(maxMem));
			sorter.sort(new FileInputStream(in), new PrintStream(out));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String genLeft(String rawTextFile, int maxLen, int memSize) {

		File rawFile = new File(rawTextFile);

		File dir = rawFile.getParentFile();

		File ngramFile = new File(dir, "ngram_left.data");
		File ngramSort = new File(dir, "sort_ngram_left.data");
		File ngramfreq = new File(dir, "freq_ngram_left.data");
		File ngramFreqSort = new File(dir, "freq_ngram_left_sort.data");

		try (BufferedReader breader = Files.newReader(rawFile, Charsets.UTF_8);
				BufferedWriter writer = Files.newWriter(ngramFile,
						Charsets.UTF_8);
				BufferedWriter freqWriter = Files.newWriter(ngramfreq,
						Charsets.UTF_8);) {
			String line = null;
			while (null != (line = breader.readLine())) {
				line = line.replaceAll("[" + stopwords + "]", " ")
						.replaceAll("\\p{Punct}", " ")
						.replaceAll("\\pP", " ")
						.replaceAll("　", " ")
						.replaceAll("\\p{Blank}", " ")
						.replaceAll("\\p{Space}", " ")
						.replaceAll("\\p{Cntrl}", " ");
				for (String sen : Splitter.on(" ").omitEmptyStrings()
						.splitToList(line)) {
					sen = reverse(sen.trim());
					if (!allChs(sen)) continue;
					sen = "$" + sen + "$";
					for (int i = 1; i < sen.length() - 1; ++i) {
						writer.write(sen.substring(i, Math.min(maxLen + i,  sen.length())) + "\n");
					}
				}
			}
			sortFile(ngramFile, ngramSort);

			try(BufferedReader nsr = Files.newReader(ngramSort, Charsets.UTF_8)) {
				String first = null;
				String curr = null;
				Map<String, CounterMap> stat = Maps.newHashMap();
				while (null != (curr = nsr.readLine())) {
					if (null == first) {
						for (int i = 1; i < curr.length(); ++i) {
							String w = curr.substring(0, i);
							String suffix = curr.substring(i).substring(0, 1);
							if (stat.containsKey(w)) {
								stat.get(w).incr(suffix);
							} else {
								CounterMap cm = new CounterMap();
								cm.incr(suffix);
								stat.put(w, cm);
							}
						}
						first = curr.substring(0, 1);
					} else {
						if (!curr.startsWith(first)) {

							StringBuilder builder = new StringBuilder();
							for (String w : stat.keySet()) {
								CounterMap cm = stat.get(w);
								int freq = 0;
								double re = 0;
								for (String k : cm.countAll().keySet()) {
									freq += cm.get(k);
								}
								for (String k : cm.countAll().keySet()) {
									double p = cm.get(k) * 1.0 / freq;
									re += -1 * Math.log(p) / Math.log(2) * p;
								}
								builder.append(reverse(w)).append("\t").append(re).append("\n");
							}
							freqWriter.write(builder.toString());
							stat.clear();
							first = curr.substring(0, 1);
						}
						for (int i = 1; i < curr.length(); ++i) {
							String w = curr.substring(0, i);
							String suffix = curr.substring(i).substring(0, 1);
							if (stat.containsKey(w)) {
								stat.get(w).incr(suffix);
							} else {
								CounterMap cm = new CounterMap();
								cm.incr(suffix);
								stat.put(w, cm);
							}
						}
					}
				}
			}
			
			sortFile(ngramfreq, ngramFreqSort);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ngramFreqSort.getAbsolutePath();
	}

	public String genFreqRight(String rawTextFile, int maxLen, int memSize) {

		File rawFile = new File(rawTextFile);

		File dir = rawFile.getParentFile();

		File ngramFile = new File(dir, "ngram.data");
		File ngramSort = new File(dir, "ngram_sort.data");
		File ngramfreq = new File(dir, "freq_ngram.data");
		File ngramfreqSort = new File(dir, "freq_ngram_sort.data");

		try (BufferedReader breader = Files.newReader(rawFile, Charsets.UTF_8);
				BufferedWriter writer = Files.newWriter(ngramFile,
						Charsets.UTF_8);
				BufferedWriter freqWriter = Files.newWriter(ngramfreq,
						Charsets.UTF_8);) {
			String line = null;
			while (null != (line = breader.readLine())) {
				line = line.replaceAll("["+stopwords+"]", " ")
						.replaceAll("\\p{Punct}", " ")
						.replaceAll("\\pP", " ")
						.replaceAll("　", " ")
						.replaceAll("\\p{Blank}", " ")
						.replaceAll("\\p{Space}", " ")
						.replaceAll("\\p{Cntrl}", " ");
				for (String sen : Splitter.on(" ").omitEmptyStrings()
						.splitToList(line)) {
					sen = sen.trim();
					if (!allChs(sen)) continue;
					sen = "$" + sen + "$";
					for (int i = 1; i < sen.length() - 1; ++i) {
						writer.write(sen.substring(i, Math.min(maxLen + i,  sen.length())) + "\n");
					}
				}
			}
			System.out.println("gen sorting...");
			sortFile(ngramFile, ngramSort);
			
			try(BufferedReader nsr = Files.newReader(ngramSort, Charsets.UTF_8)) {
				String first = null;
				String curr = null;
				Map<String, CounterMap> stat = Maps.newHashMap();
				while (null != (curr = nsr.readLine())) {
					if (null == first) {
						for (int i = 1; i < curr.length(); ++i) {
							String w = curr.substring(0, i);
							String suffix = curr.substring(i).substring(0, 1);
							if (stat.containsKey(w)) {
								stat.get(w).incr(suffix);
							} else {
								CounterMap cm = new CounterMap();
								cm.incr(suffix);
								stat.put(w, cm);
							}
						}
						first = curr.substring(0, 1);
					} else {
						if (!curr.startsWith(first)) {

							StringBuilder builder = new StringBuilder();
							for (String w : stat.keySet()) {
								CounterMap cm = stat.get(w);
								int freq = 0;
								double re = 0;
								for (String k : cm.countAll().keySet()) {
									freq += cm.get(k);
								}
								for (String k : cm.countAll().keySet()) {
									double p = cm.get(k) * 1.0 / freq;
									re += -1 * Math.log(p) / Math.log(2) * p;
								}
								builder.append(w).append("\t").append(freq).append("\t").append(re).append("\n");
							}
							freqWriter.write(builder.toString());
							stat.clear();
							first = curr.substring(0, 1);
						}
						for (int i = 1; i < curr.length(); ++i) {
							String w = curr.substring(0, i);
							String suffix = curr.substring(i).substring(0, 1);
							if (stat.containsKey(w)) {
								stat.get(w).incr(suffix);
							} else {
								CounterMap cm = new CounterMap();
								cm.incr(suffix);
								stat.put(w, cm);
							}
						}
					}
				}
			}
			
			sortFile(ngramfreq, ngramfreqSort);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ngramfreqSort.getAbsolutePath();
	}

	public String mergeEntropy(String freqRight, String left) {


		File frFile = new File(freqRight);
		File lFile = new File(left);
		File mergeTmp = new File(frFile.getParentFile(), "merge.tmp");
		File mergeTmp2 = new File(frFile.getParentFile(), "merge.tmp2");
		File mergeFile = new File(frFile.getParentFile(), "merge_entropy.data");

		try (BufferedReader rr = Files.newReader(frFile, Charsets.UTF_8);
				BufferedReader lr = Files.newReader(lFile, Charsets.UTF_8);
				BufferedWriter mw = Files.newWriter(mergeTmp, Charsets.UTF_8);
				BufferedWriter mf = Files.newWriter(mergeFile, Charsets.UTF_8);) {
			String line = null;
			while (null != (line = rr.readLine())) {
				mw.write(line + "\n");
			}
			line = null;
			while (null != (line = lr.readLine())) {
				mw.write(line + "\n");
			}

			sortFile(mergeTmp, mergeTmp2);

			BufferedReader br = Files.newReader(mergeTmp2, Charsets.UTF_8);

			String line1 = null;
			String line2 = null;
			line1 = br.readLine();
			line2 = br.readLine();
			while (true) {

				if (null == line1 || null == line2)
					break;
				String[] seg1 = line1.split("\t");
				String[] seg2 = line2.split("\t");
				if (!seg1[0].equals(seg2[0])) {
					line1 = new String(line2.getBytes());
					line2 = br.readLine();
					continue;
				}
				if (seg1.length < 2) {
					line1 = new String(line2.getBytes());
					line2 = br.readLine();
					continue;
				}
				line1 = br.readLine();
				line2 = br.readLine();
				
				if (seg1.length < 3 && seg2.length < 3) 
					continue;
				double le = seg1.length == 2 ? Double.parseDouble(seg1[1])
						: Double.parseDouble(seg2[1]);
				double re = seg1.length == 3 ? Double.parseDouble(seg1[2])
						: Double.parseDouble(seg2[2]);
				int freq = seg1.length == 3 ? Integer.parseInt(seg1[1])
						: Integer.parseInt(seg2[1]);
				double e = Math.min(le, re);
				mf.write(seg1[0] + "\t" + freq + "\t" + e + "\n");

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mergeFile.toString();
	}
	
	public static boolean allLetterOrNumber(String w) {

		for (char c : w.toLowerCase().toCharArray()) {
			boolean letter = c >= 'a' && c <= 'z';
			boolean digit = c >= '0' && c <= '9';
			if (!letter && !digit) return false;
		}
		return true;
	}

	public void extractWords(String freqFile, String entropyFile) {

		
		TreeMap<String, double[]> posProp = this.loadPosprop();
		
		TernaryTree freq = new TernaryTree();

		File ffile = new File(freqFile);
		File efile = new File(entropyFile);
		File wfile = new File(efile.getParentFile(), "words.data");
		File wsfile = new File(efile.getParentFile(), "words_sort.data");

		try (BufferedReader fr = Files.newReader(ffile, Charsets.UTF_8);
				BufferedReader er = Files.newReader(efile, Charsets.UTF_8);
				BufferedWriter ww = Files.newWriter(wfile, Charsets.UTF_8);) {

			String line = null;
			long total = 0;
			while (null != (line = fr.readLine())) {
				String[] seg = line.split("\t");
				if (seg.length < 3) continue;
				freq.insert(seg[0], Integer.parseInt(seg[1]));
				total += 1;
			}
			line = null;
			while (null != (line = er.readLine())) {
				String[] seg = line.split("\t");
				if (3 != seg.length)
					continue;
				String w = seg[0];
				if (allLetterOrNumber(w)) {
					continue;
				}
				int f = Integer.parseInt(seg[1]);
				double e = Double.parseDouble(seg[2]);
				long max = -1;
				for (int s = 1; s < w.length(); ++s) {
					String lw = w.substring(0, s);
					String rw = w.substring(s);
					
					int lf = freq.search(lw);
					int rf = freq.search(rw);
					
					if (-1 == lf || -1 == rf) continue;
					
					long ff = lf * rf;
					if (ff > max)
						max = ff;
				}
				double pf = f * total / max;
				double pmi = Math.log(pf) / Math.log(2);
				if (Double.isNaN(pmi)) continue;
				double pp = -1;
				if (null != posProp.get(w.subSequence(0, 1)) && null != posProp.get(w.subSequence(w.length() - 1, w.length())))
					pp = Math.min(posProp.get(w.subSequence(0, 1))[0], posProp.get(w.subSequence(w.length() - 1, w.length()))[2]);
				if (pmi < 1 || e < 2 || pp < 0.1)
					continue;
				ww.write(w + "\t" + f + "\t" + pmi + "\t" + e + "\t"  + pp + "\n");
			}
			
			try {
				long availMem = Runtime.getRuntime().maxMemory()
						- (40 * 1024 * 1024);
				long maxMem = (availMem >> 1);
				if (maxMem > MAX_HEAP_FOR_PRESORT) {
					maxMem = MAX_HEAP_FOR_PRESORT;
				} else if (maxMem < MIN_HEAP_FOR_PRESORT) {
					maxMem = MIN_HEAP_FOR_PRESORT;
				}
				final SplitFileSorter sorter = new SplitFileSorter(
						new SortConfig().withMaxMemoryUsage(maxMem));
				sorter.sort(new FileInputStream(wfile), new PrintStream(wsfile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
