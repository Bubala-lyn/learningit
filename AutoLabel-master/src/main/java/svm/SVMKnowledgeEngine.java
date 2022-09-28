package svm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import po.PredictResult;
import system.SystemConf;

public class SVMKnowledgeEngine {

	/**
	 * 
	 * @param smatrix
	 *            所有文本的词集
	 * @param matrix
	 *            训练集和标签集
	 * @param keys
	 *            测试集
	 * @param seqMap
	 *            测试集单词序列
	 */
	public static void flushData(List<String> smatrix, Object[] matrix, List<Integer> keys,
			Map<Integer, String[]> seqMap, String _prex) {
		int num = keys.size();
		int[][] test_matrix = new int[keys.size()][smatrix.size()];
		for (int i = 0; i < num; i++) {
			Integer sampleKey = keys.get(i);
			String[] words = seqMap.get(sampleKey);
			List<String> wList = Arrays.asList(words);

			int[] bz = new int[smatrix.size()];
			int k = 0;
			for (String s : smatrix)
				bz[k++] = wList.indexOf(s) == -1 ? 0 : 1;
			test_matrix[i] = bz;
		}

		int[][] x = (int[][]) matrix[0];
		int[] y = (int[]) matrix[1];
		FileUtils.write(smatrix.size(), x, _prex + "-" + SystemConf.getValueByCode("xFile"));// 训练集
		FileUtils.write(y, _prex + "-" + SystemConf.getValueByCode("yFile"));// 标签集
		FileUtils.write(smatrix.size(), test_matrix, _prex + "-" + SystemConf.getValueByCode("zFile"));// 测试集
		System.out.println("Write datas finished....");
	}

	/**
	 * 
	 * @param testKeys
	 *            测试集
	 * @param rightDisMap
	 *            测试集的原始标签map
	 * @return
	 */
	public static double predictPresion(List<Integer> testKeys, Map<Integer, List<String>> rightDisMap, String _prex,
			boolean isLocalFile) {
		int num = testKeys.size();
		PredictResult[][] res = new PredictResult[testKeys.size()][1];
		int[] result = FileUtils.read(num, _prex + "-" + SystemConf.getValueByCode("rFile"), isLocalFile);
		for (int i = 0; i < num; i++) {
			PredictResult r = new PredictResult();
			r.labelKey = String.valueOf(result[i]);
			r.sampleKey = testKeys.get(i);
			res[i][0] = r;
		}

		double errorRate = 0.0d;
		for (int i = 0; i < testKeys.size(); i++) {
			PredictResult rs = res[i][0];
			errorRate += 1;
			for (String label : rightDisMap.get(rs.sampleKey)) {
				if (label.equals(rs.labelKey)) {
					errorRate -= 1;
					break;
				}
			}
		}
		return 1 - errorRate / testKeys.size();
	}
}