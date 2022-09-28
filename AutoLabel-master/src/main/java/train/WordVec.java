package train;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WordVec {

	/**
	 * 根据词向量找到相似度最为接近的1个题目
	 * 
	 * @param wordMap
	 * @param ws
	 * @param content
	 * @return
	 */
	public Integer getSimilaryQuestions(Map<Integer, double[]> wordMap, List<String> ws, String content) {
		Integer qid = null;
		// String content = segService.segment(rawContent);
		// content = content.substring(0, content.length()-1);
		double sum = 0;
		double wc[] = new double[ws.size()];
		String strs[] = content.split(" ");
		for (String s : strs) {
			int index = ws.indexOf(s);
			if (index != -1) {
				wc[index]++;
				sum++;
			}
		}
		for (int i = 0; i < wc.length; i++)
			wc[i] /= sum;
		double min = Double.MAX_VALUE;
		Iterator<Entry<Integer, double[]>> iter = wordMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, double[]> entry = iter.next();
			iter.remove();// help for GC
			double[] wcc = entry.getValue();
			double s = getSimilarity(wcc, wc);
			if (min > s) {
				qid = entry.getKey();
				min = s;
			}
		}
		return qid;
	}

	private double getSimilarity(double s[], double t[]) {
		int i = 0, size = s.length;
		double d1 = 0, d2 = 0, d3 = 0;
		// (d1+d2)/d3;
		for (; i < size; i++) {
			d1 += s[i] * t[i];
			d2 += s[i] * s[i];
			d3 += t[i] * t[i];
		}
		d2 *= d3;
		d2 = Math.sqrt(d2);
		return d1 / d2;
	}
}