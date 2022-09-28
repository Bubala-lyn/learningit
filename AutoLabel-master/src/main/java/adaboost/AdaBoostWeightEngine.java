package adaboost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import po.BaseResult;
import po.KWeight;
import po.KnowledgeNode;

public class AdaBoostWeightEngine extends AbstractAdaBoost {

	int sampleNum;
	int labelNum;
	int epoch;
	int ep = 0;// 轮次
	double[] ω;// 词权重
	double[] εs;// 整体误分类率
	boolean earlyStop = false;// 判断是否应该提前终止
	List<Integer> sampleKeys;
	List<String> labelKeys;
	Map<String, Double> beltaMap;// 样本(词分布)权重
	Map<Integer, String[]> sequenceMap;
	Map<String, Double> condProMap;
	Map<String, Double> labelProMap;
	Map<Integer, List<KWeight>> sampleRightDisMap;
	Map<Integer, List<Integer>> labelSampleDisMap;
	List<String> words;

	protected AdaBoostWeightEngine() {
	}

	/**
	 * 
	 * @param epoch
	 *            迭代次数
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
	AdaBoostWeightEngine(int epoch, List<Integer> sampleKeys, List<String> labelKeys,
			Map<Integer, String[]> sequenceMap, Map<String, Double> condProMap, Map<String, Double> labelProMap,
			Map<Integer, List<KWeight>> sampleRightDisMap, Map<String, KnowledgeNode> knMap,
			Map<Integer, List<Integer>> labelSampleDisMap) {
		super(knMap);
		this.epoch = epoch;
		this.sampleKeys = sampleKeys;
		this.labelKeys = labelKeys;
		this.sequenceMap = sequenceMap;
		this.condProMap = condProMap;
		this.labelProMap = labelProMap;
		this.sampleRightDisMap = sampleRightDisMap;
		this.labelSampleDisMap = labelSampleDisMap;
		this.sampleNum = sampleKeys.size();
		this.labelNum = labelKeys.size();
		this.ω = new double[this.sampleNum];
		this.init();
	}

	/**
	 * 开始迭代训练
	 */
	public void train() {
		for (; ep < epoch; ep++) {
			if (earlyStop)
				break;
			Result[][] res = produceResults(sampleKeys, sequenceMap);
			updateWeightAndBelta(res);
		}
		updateAdaBoostProbability(beltaMap);
	}

	private Result[][] produceResults(List<Integer> keys, Map<Integer, String[]> seqMap) {
		int num = keys.size();
		Result[][] res = new Result[num][];
		for (int i = 0; i < num; i++) {
			Integer sampleKey = keys.get(i);
			String[] words = seqMap.get(sampleKey);
			HashSet<String> wordSet = new HashSet<String>(Arrays.asList(words));
			List<KWeight> rightLabels = sampleRightDisMap.get(sampleKey);
			res[i] = new Result[rightLabels.size()];
			for (int j = 0; j < rightLabels.size(); j++) {
				String labelKey = rightLabels.get(j).realKey;
				double p = Math.exp(labelProMap.get(labelKey));
				for (String word : wordSet) {
					String key = labelKey + "_" + word;
					if (condProMap.containsKey(key))
						p += Math.exp(beltaMap.get(key) * condProMap.get(key));
				}
				Result r = new Result();
				r.postProbability = p;
				r.labelKey = labelKey;
				r.sampleKey = sampleKey;
				res[i][j] = r;
			}
		}
		return res;
	}

	private void updateWeightAndBelta(Result[][] res) {
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(sampleNum, res, sampleRightDisMap);
		// 更新每一行参数
		updateEverySingleSample(sampleKeys, sampleStates);
	}

	private HashMap<Integer, ClassifyResult> getClassifyResult(int num, Result[][] res,
			Map<Integer, List<KWeight>> rightDisMap) {
		HashMap<Integer, ClassifyResult> map = new HashMap<Integer, ClassifyResult>();
		for (int i = 0; i < num; i++) {
			Result[] rs = res[i];
			map.put(rs[0].sampleKey, reckonErrorRate(Arrays.asList(rs), rightDisMap.get(rs[0].sampleKey)));
		}
		return map;
	}

	private double calculateErrorRate(boolean isTrain, List<Integer> sampleKeys,
			HashMap<Integer, ClassifyResult> sampleStates) {
		double errorRate = 0.0d;
		for (int i = 0; i < sampleKeys.size(); i++) {
			ClassifyResult state = sampleStates.get(sampleKeys.get(i));
			if (state.classifyError) {
				if (isTrain)
					errorRate += ω[i];
				else
					errorRate += 1 - state.precision;
			}
		}
		if (errorRate >= 0.50)
			earlyStop = true;
		return isTrain ? errorRate : errorRate / sampleKeys.size();
	}

	/**
	 * @param inferList
	 * @param rightList
	 * @return
	 */
	private ClassifyResult reckonErrorRate(List<Result> inferResultList, List<KWeight> rightLabels) {
		ClassifyResult state = new ClassifyResult();
		state.classifyError = true;
		state.quesKey = inferResultList.get(0).sampleKey;

		List<String> rLabels = new ArrayList<>();
		List<String> errorLabels = new ArrayList<>();

		double d = 0;
		int score = 0, s1 = 0, s2 = 0, s3 = 0;
		for (Result r : inferResultList) {
			if (r == null)
				break;
			d += r.postProbability;
		}

		for (int i = 0; i < inferResultList.size(); i++) {
			int sc = (int) (inferResultList.get(i).postProbability * 100 / d);
			if (i == inferResultList.size() - 1)
				sc = 100 - score;
			else
				score += sc;
			KWeight kw = null;
			for (KWeight bean : rightLabels) {
				if (bean.realKey.equals(inferResultList.get(i).labelKey)) {
					kw = bean;
					break;
				}
			}
			s1 += sc * sc;
			s2 += sc * kw.realProbability;
			s3 += kw.realProbability * kw.realProbability;
			if (sc < kw.realProbability)
				rLabels.add(kw.realKey);
			else if (sc > kw.realProbability)
				errorLabels.add(kw.realKey);
		}
		s1 *= s3;

		double cosin = (double) s2 / (double) Math.sqrt(s1);
		if (cosin > 0.96)
			state.classifyError = false;
		state.precision = cosin;
		state.errorLabels = errorLabels;
		state.rightLabels = rLabels;
		return state;
	}

	private void updateEverySingleSample(List<Integer> sampleKeys, HashMap<Integer, ClassifyResult> sampleStates) {
		double ε = calculateErrorRate(true, sampleKeys, sampleStates);
		εs[ep] = ε;

		double β = 0.5 * Math.log((1 - ε) / ε);
		double zt = 0.0;
		// 批量更新条件概率分布权值和样本分布
		int j = 0;
		HashMap<String, Integer> addMap = new HashMap<String, Integer>();
		HashMap<String, Integer> plusMap = new HashMap<String, Integer>();
		for (int i = 0; i < sampleKeys.size(); i++) {
			ClassifyResult state = sampleStates.get(sampleKeys.get(i));
			if (state.classifyError) {
				j++;
				List<String> rLables = state.rightLabels;
				List<String> errLables = state.errorLabels;
				String[] words = sequenceMap.get(sampleKeys.get(i));

				// 提高分类正确的分词权重
				for (String labelKey : rLables) {
					for (String word : words) {
						String key = labelKey + "_" + word;
						if (condProMap.containsKey(key) && !addMap.containsKey(key)) {
							addMap.put(key, 1);
							beltaMap.put(key, beltaMap.get(key) * Math.exp((1 - state.precision) * β));
						}
					}
				}
				// 降低分类错误的分词权重
				for (String labelKey : errLables) {
					for (String word : words) {
						String key = labelKey + "_" + word;
						if (condProMap.containsKey(key) && !plusMap.containsKey(key)) {
							plusMap.put(key, 1);
							beltaMap.put(key, beltaMap.get(key) * Math.exp((state.precision - 1) * β));
						}
					}
				}
				ω[i] *= Math.exp(β);
			} else {
				ω[i] *= Math.exp(-β);
			}
			zt += ω[i];
		}
		System.out.format("错误分类个数={%d}, 错误率={%.3f}\n", j, (double) j / sampleNum);

		// 更新样本权重
		for (int i = 0; i < this.sampleNum; i++) {
			ω[i] /= zt;
		}
	}

	private void init() {
		εs = new double[epoch];
		for (int i = 0; i < sampleNum; i++)
			ω[i] = 1.0 / sampleNum;

		beltaMap = new HashMap<String, Double>();
		Iterator<String> iter = condProMap.keySet().iterator();
		while (iter.hasNext()) {
			beltaMap.put(iter.next(), 1.0);
		}
		updateAdaBoostProbability(beltaMap);
	}

	class ClassifyResult {
		Integer quesKey;
		double precision;
		boolean classifyError = false;
		List<String> errorLabels = null;
		List<String> rightLabels = null;
	}

	class Result extends BaseResult implements Comparable<Result> {
		double postProbability;
		Integer sampleKey;
		String labelKey;

		@Override
		public int compareTo(Result o) {
			double d = this.postProbability - o.postProbability;
			if (d < 0)
				return 1;
			else if (d > 0)
				return -1;
			else
				return 0;
		}
	}

	/**
	 * 测试
	 * 
	 * @param testMap
	 * @param sampleRightDisMap
	 */
	protected void predict(List<Integer> testKeys, Map<Integer, String[]> testSeqMap,
			Map<Integer, List<KWeight>> rightDisMap) {
		this.condProMap = getConditionMap(); // 分词|知识点条件概率
		this.beltaMap = getBeltaMap(); // adaboost概率
		this.labelKeys = getLabelKeys(); // 不重复知识点list
		this.labelNum = this.labelKeys.size(); // 标签数量

		Result[][] res = produceResults(testKeys, testSeqMap); // 预测结果
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(testKeys.size(), res, rightDisMap);

		int totalLength = 0;
		Iterator<Integer> iter = testSeqMap.keySet().iterator();
		while (iter.hasNext()) {
			totalLength += testSeqMap.get(iter.next()).length;
		}
		int avgLen = totalLength / testSeqMap.size();

		double presion;

		List<Integer> lessList = new ArrayList<Integer>();
		List<Integer> moreList = new ArrayList<Integer>();

		Iterator<Integer> itor = sampleStates.keySet().iterator();
		while (itor.hasNext()) {
			Integer qid = itor.next();
			if (testSeqMap.get(qid).length < avgLen && !lessList.contains(qid))
				lessList.add(qid);
			if (testSeqMap.get(qid).length >= avgLen && !moreList.contains(qid))
				moreList.add(qid);
		}

		presion = 1 - calculateErrorRate(false, lessList, sampleStates);
		System.out.println("小于平均长度的准确率为->" + presion);

		presion = 1 - calculateErrorRate(false, moreList, sampleStates);
		System.out.println("大于平均长度的准确率为->" + presion);

		// return 1 - calculateErrorRate(false, testKeys, sampleStates);
	}

	protected BaseResult[] prefdictResult(Integer sampleKey, String[] words, List<String> labels) {
		Map<String, Double> condProMap = getConditionMap();
		Map<String, Double> beltaMap = getBeltaMap();
		Map<String, Double> labelProMap = getLabelProMap();
		double d = 0;
		int score = 0;
		Result[] inferResultList = new Result[labels.size()];
		HashSet<String> wordSet = new HashSet<String>(Arrays.asList(words));
		for (int j = 0; j < labels.size(); j++) {
			String labelKey = labels.get(j);
			double p = Math.exp(labelProMap.get(labelKey));
			for (String word : wordSet) {
				String key = labelKey + "_" + word;
				if (condProMap.containsKey(key))
					p += Math.exp(beltaMap.get(key) * condProMap.get(key));
			}
			Result r = new Result();
			r.postProbability = p;
			r.labelKey = labelKey;
			inferResultList[j] = r;
			d += r.postProbability;
		}
		for (int i = 0; i < inferResultList.length; i++) {
			int sc = (int) (inferResultList[i].postProbability * 100 / d);
			if (i == inferResultList.length - 1)
				sc = 100 - score;
			else
				score += sc;
			inferResultList[i].weight = sc;
		}
		return inferResultList;
	}
}