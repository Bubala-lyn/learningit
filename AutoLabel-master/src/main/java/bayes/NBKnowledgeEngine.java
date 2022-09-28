package bayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import adaboost.AbstractAdaBoost;
import po.KnowledgeNode;
import po.PredictResult;

public class NBKnowledgeEngine extends AbstractAdaBoost {

	int sampleNum;
	int labelNum;
	int topK;
	double[] εs;// 整体误分类率
	boolean earlyStop = false;// 判断是否应该提前终止
	List<Integer> sampleKeys;
	List<String> labelKeys;
	Map<Integer, String[]> sequenceMap;
	Map<String, Double> condProMap;
	Map<String, Double> labelProMap;
	Map<Integer, List<String>> sampleRightDisMap;
	Map<Integer, List<Integer>> labelSampleDisMap;
	List<String> words;

	public NBKnowledgeEngine(Map<String, KnowledgeNode> knMap) {
		super(knMap);
	}

	/**
	 * 
	 * @param topK
	 *            推断知识点前k大概率
	 * @param sampleKeys
	 *            所有样本
	 * @param labelKeys
	 *            所有标签
	 * @param sequenceMap
	 *            样本分词结果集合
	 * @param condProMap
	 *            p(x|c)条件概率
	 * @param labelProMap
	 *            标签先验概率分布
	 * @param sampleRightDisMap
	 *            样本的正确概率分布
	 * @param knMap
	 *            知识点结构
	 * @param labelSampleDisMap
	 *            知识点包含的样本
	 */
	public NBKnowledgeEngine(int epoch, int topK, List<Integer> sampleKeys, List<String> labelKeys,
			Map<Integer, String[]> sequenceMap, Map<String, Double> condProMap, Map<String, Double> labelProMap,
			Map<Integer, List<String>> sampleRightDisMap, Map<String, KnowledgeNode> knMap,
			Map<Integer, List<Integer>> labelSampleDisMap) {
		super(knMap);
		this.topK = topK;
		this.sampleKeys = sampleKeys;
		this.labelKeys = labelKeys;
		this.sequenceMap = sequenceMap;
		this.condProMap = condProMap;
		this.labelProMap = labelProMap;
		this.sampleRightDisMap = sampleRightDisMap;
		this.labelSampleDisMap = labelSampleDisMap;
		this.sampleNum = sampleKeys.size();
		this.labelNum = labelKeys.size();
	}

	private PredictResult[][] produceResults(int num, List<Integer> keys, Map<Integer, String[]> seqMap) {
		PredictResult[][] res = new PredictResult[num][labelNum];
		for (int i = 0; i < num; i++) {
			Integer sampleKey = keys.get(i);
			String[] words = seqMap.get(sampleKey);
			HashSet<String> wordSet = new HashSet<String>(Arrays.asList(words));
			for (int j = 0; j < labelNum; j++) {
				String labelKey = labelKeys.get(j);
				double p = Math.exp(labelProMap.get(labelKey));
				for (String word : wordSet) {
					String key = labelKey + "_" + word;
					if (condProMap.containsKey(key))
						p += Math.exp(condProMap.get(key));
				}
				PredictResult r = new PredictResult();
				r.postProbability = p;
				r.labelKey = labelKey;
				r.sampleKey = sampleKey;
				res[i][j] = r;
			}
		}
		return res;
	}

	private HashMap<Integer, ClassifyResult> getClassifyResult(int num, PredictResult[][] res,
			Map<Integer, List<String>> rightDisMap) {
		HashMap<Integer, ClassifyResult> map = new HashMap<Integer, ClassifyResult>();
		HashMap<Integer, List<PredictResult>> predictResultMap = getClassifyResultMap(num, res);
		for (int i = 0; i < num; i++) {
			Integer key = res[i][0].sampleKey;
			map.put(key, reckonErrorRate(predictResultMap.get(key), rightDisMap.get(key)));
		}
		return map;
	}

	private HashMap<Integer, List<PredictResult>> getClassifyResultMap(int num, PredictResult[][] res) {
		HashMap<Integer, List<PredictResult>> map = new HashMap<Integer, List<PredictResult>>();
		for (int i = 0; i < num; i++) {
			PredictResult[] rs = res[i];
			Arrays.sort(rs);
			// 取前topK个推断标签判断
			List<PredictResult> inferPredictResultList = new ArrayList<>(topK);
			int j = 0;
			while (j < topK)
				inferPredictResultList.add(rs[j++]);
			map.put(rs[0].sampleKey, inferPredictResultList);
		}
		return map;
	}

	private double calculateErrorRate(List<Integer> sampleKeys,
			HashMap<Integer, ClassifyResult> sampleStates) {
		double errorRate = 0.0d;
		for (int i = 0; i < sampleKeys.size(); i++) {
			ClassifyResult state = sampleStates.get(sampleKeys.get(i));
			if (state.classifyError) {
				errorRate += 1 - state.precision;
			}
		}
		return errorRate / sampleKeys.size();
	}

	/**
	 * @param inferList
	 * @param rightList
	 * @return
	 */
	private ClassifyResult reckonErrorRate(List<PredictResult> inferPredictResultList, List<String> rightLabels) {
		ClassifyResult state = new ClassifyResult();
		state.classifyError = true;
		state.quesKey = inferPredictResultList.get(0).sampleKey;

		boolean isContains = false;
		List<String> errorLabels = new ArrayList<>();
		contain: for (PredictResult r : inferPredictResultList) {
			for (String key : rightLabels) {
				if (key.equals(r.labelKey)) {
					isContains = true;
					break contain;
				}
			}
		}
		if (isContains) {
			// 分类正确
			state.classifyError = false;
			if (rightLabels.size() != 1) {
				int size = 0;
				List<String> hitLabels = new ArrayList<String>();
				List<String> missHitLabels = new ArrayList<String>();
				for (PredictResult r : inferPredictResultList) {
					boolean isError = true;
					for (String key : rightLabels) {
						if (key.equals(r.labelKey)) {
							size++;
							isError = false;
							hitLabels.add(key);
							break;
						}
					}
					if (isError)
						errorLabels.add(r.labelKey);
				}

				for (String key : rightLabels) {
					if (!hitLabels.contains(key))
						missHitLabels.add(key);
				}

				if (size < rightLabels.size() && size < inferPredictResultList.size()) {
					state.classifyError = true;
					state.rightLabels = rightLabels;
					state.errorLabels = errorLabels;

					// 推荐{A,B,C},正确{A,D,E},则errorLabels为{B,C},missHitLabels为{D,E}
					state.precision = size / (double) rightLabels.size() + reckonPresion(errorLabels, missHitLabels, false);
					state.recall = (double) size / (double) topK;
					state.fvalue = state.precision + state.recall == 0.0 ? 0
							: 2 * state.precision * state.recall / (state.precision + state.recall);
				}
			} else {
				state.precision = 1;
				state.recall = 1;
				state.fvalue = 1;
			}
		} else {
			state.classifyError = true;
			state.rightLabels = rightLabels;
			for (PredictResult r : inferPredictResultList) {
				errorLabels.add(r.labelKey);
			}
			state.errorLabels = errorLabels;
			state.precision = reckonPresion(errorLabels, rightLabels, false);
			state.recall = 0;
			state.fvalue = 0;
		}
		return state;
	}

	public PredictResult[][] getPredictResult(int topK, List<Integer> testKeys, Map<Integer, String[]> testSeqMap) {
		this.topK = topK;
		this.condProMap = getConditionMap();
		this.labelKeys = getLabelKeys();
		this.labelProMap = getLabelProMap();
		this.labelNum = this.labelKeys.size();
		return produceResults(testKeys.size(), testKeys, testSeqMap);
	}

	public void predict(int topK, List<Integer> testKeys, Map<Integer, String[]> testSeqMap,
			Map<Integer, List<String>> rightDisMap) {
		PredictResult[][] res = getPredictResult(topK, testKeys, testSeqMap);
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(testKeys.size(), res, rightDisMap);

		double sumR = 0.0, sumF = 0.0;
		Iterator<Integer> itor = sampleStates.keySet().iterator();
		while (itor.hasNext()) {
			ClassifyResult r = sampleStates.get(itor.next());
			sumR += r.recall;
			sumF += r.fvalue;
		}
		double presion = 1 - calculateErrorRate(testKeys, sampleStates);
		double recall = sumR / (double) testKeys.size();
		double fvalue = sumF / (double) testKeys.size();
		System.out.println("准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);
	}

	class ClassifyResult {
		Integer quesKey;
		double precision;
		double recall;
		double fvalue;
		boolean classifyError = false;
		List<String> errorLabels = null;
		List<String> rightLabels = null;
	}
}