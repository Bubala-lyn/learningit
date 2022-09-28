package adaboost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import po.KnowledgeNode;
import po.PredictResult;

import system.SystemConf;
import utils.FileOperation;
import utils.ReadJSON;

public class AdaBoostKnowledgeEngine extends AbstractAdaBoost {

	int sampleNum;
	int labelNum;
	int epoch;
	int topK;
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
	Map<Integer, List<String>> sampleRightDisMap;
	Map<Integer, List<Integer>> labelSampleDisMap;
	List<String> words;

	AdaBoostKnowledgeEngine(Map<String, KnowledgeNode> knMap) {
		super(knMap);
	}

	AdaBoostKnowledgeEngine(int epoch, int topK, List<Integer> sampleKeys, List<String> labelKeys,
			Map<Integer, String[]> sequenceMap, Map<String, Double> condProMap, Map<String, Double> labelProMap,
			Map<Integer, List<String>> sampleRightDisMap, Map<String, KnowledgeNode> knMap,
			Map<Integer, List<Integer>> labelSampleDisMap) {
		super(knMap);
		this.epoch = epoch;
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
		this.ω = new double[this.sampleNum];
		this.init();
	}

	/**
	 * 开始迭代训练
	 */
	public void train() {
		for (; ep < epoch; ep++) {
			System.out.println("第" + (ep + 1) + "次训练开始....");
			if (earlyStop)
				break;
			PredictResult[][] res = produceResults(sampleNum, sampleKeys, sequenceMap);
			updateWeightAndBelta(res);
			System.out.println("第" + (ep + 1) + "次训练结束....");
		}
		updateAdaBoostProbability(beltaMap);
	}

	/**
	 * PredictResult: 每一道待测试题目对每一个知识点的后验概率
	 * [i][j]: i 测试集数量范围; j 知识点数量范围
	 * [][].postProbability 后验概率
	 * [][].labelKey 知识点 id
	 * [][].sampleKey 待测试题目 id
	 */
	private PredictResult[][] produceResults(int num, List<Integer> keys, Map<Integer, String[]> seqMap) {
		// System.out.println("测试集数量：" + num);
		// System.out.println("知识点数量：" + labelNum);
		// System.out.println("测试集：" + keys);
		// System.out.println("测试集分词Map：" + seqMap);
		PredictResult[][] res = new PredictResult[num][labelNum];
		for (int i = 0; i < num; i++) {
			Integer sampleKey = keys.get(i);
			String[] words = seqMap.get(sampleKey);
			HashSet<String> wordSet = new HashSet<String>(Arrays.asList(words));
			for (int j = 0; j < labelNum; j++) {
				String labelKey = labelKeys.get(j);
				double p = 0.0;
				if (labelProMap.containsKey(labelKey)) {
					p = Math.exp(labelProMap.get(labelKey));
				} else {
					p = Math.exp(0.0);
				}
				for (String word : wordSet) {
					String key = labelKey + "_" + word;
					if (condProMap.containsKey(key))
						p += Math.exp(beltaMap.get(key) * condProMap.get(key));
				}
				PredictResult r = new PredictResult();
				r.postProbability = p;
				r.labelKey = labelKey;
				r.sampleKey = sampleKey;
				// !概率最大的就是这个测试对象的预测知识点
				// System.out.println("测试对象：" + sampleKey + " 对于知识点：" + labelKey + " 的后验概率：" + p);
				res[i][j] = r;
			}
		}
		return res;
	}

	private void updateWeightAndBelta(PredictResult[][] res) {
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(sampleNum, res, sampleRightDisMap, false);
		// 更新每一行参数
		updateEverySingleSample(sampleKeys, sampleStates);
	}

	private HashMap<Integer, ClassifyResult> getClassifyResult(int num, PredictResult[][] res,
			Map<Integer, List<String>> rightDisMap, boolean isLoose) {
		HashMap<Integer, ClassifyResult> map = new HashMap<Integer, ClassifyResult>();
		// predictResultMap eg: {3996636=[po.PredictResult@6adede5, po.PredictResult@2d928643, po.PredictResult@5025a98f]}
		HashMap<Integer, List<PredictResult>> predictResultMap = getClassifyResultMap(num, res);
		for (int i = 0; i < num; i++) {
			Integer key = res[i][0].sampleKey; // 试题id
			map.put(key, reckonErrorRate(predictResultMap.get(key), rightDisMap.get(key), isLoose));
		}
		return map;
	}

	// 根据概率获取前topK个标签
	private HashMap<Integer, List<PredictResult>> getClassifyResultMap(int num, PredictResult[][] res) {
		HashMap<Integer, List<PredictResult>> map = new HashMap<Integer, List<PredictResult>>();
		for (int i = 0; i < num; i++) {
			PredictResult[] rs = res[i];
			Arrays.sort(rs); // 概率倒序排序
			// 取前topK个推断标签判断
			List<PredictResult> inferPredictResultList = new ArrayList<>(topK);
			int j = 0;
			while (j < topK)
				inferPredictResultList.add(rs[j++]);
			map.put(rs[0].sampleKey, inferPredictResultList);
		}
		return map;
	}

	// 计算误差率
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
	private ClassifyResult reckonErrorRate(List<PredictResult> inferPredictResultList, List<String> rightLabels, boolean isLoose) {
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
					state.precision = size / (double) rightLabels.size() + reckonPresion(errorLabels, missHitLabels, isLoose);
					state.recall = (double) size / (double) topK;
					state.fvalue = state.precision + state.recall == 0.0 ? 0
							: 2 * state.precision * state.recall / (state.precision + state.recall);
				}
				// 之所以不考虑其他情况是
				// 因为 计算误差的时候只考虑分类失败
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
			state.precision = reckonPresion(errorLabels, rightLabels, isLoose);
			state.recall = 0;
			state.fvalue = 0;
		}
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

	// 输出结果
	private void writePredictResult(int num, PredictResult[][] res,
			Map<Integer, List<String>> rightDisMap) {
		// predictResultMap eg: {3996636=[po.PredictResult@6adede5, po.PredictResult@2d928643, po.PredictResult@5025a98f]}
		HashMap<Integer, List<PredictResult>> predictResultMap = getClassifyResultMap(num, res);
		String result = "qid,predictKids,defaultKids\r\n";
		for (int i = 0; i < num; i++) {
			Integer key = res[i][0].sampleKey; // 试题id
			List<PredictResult> inferPredictResultList = predictResultMap.get(key);
			List<String> predictKids = new ArrayList<String>(); // 当前试题的预测知识点id列表
			List<String> defaultKids = rightDisMap.get(key); // 试题标注的知识点id列表
			for (PredictResult r : inferPredictResultList) {
				predictKids.add(r.labelKey);
			}
			result += key + "," + String.join("|", predictKids) + "," + String.join("|", defaultKids);
			result += "\r\n";
		}
		new FileOperation().write(SystemConf.getValueByCode("result"), result);
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

	public PredictResult[][] getPredictResult(int topK, List<Integer> testKeys, Map<Integer, String[]> testSeqMap) {
		this.topK = topK;
		this.condProMap = getConditionMap(); // wordKid,probability 条件概率
		this.beltaMap = getBeltaMap(); // wordKid,adaboost_pro
		this.labelKeys = getLabelKeys(); // DISTINCT knowledgeId
		this.labelProMap = getLabelProMap(); // knowledgeId,probability 知识点概率
		this.labelNum = this.labelKeys.size();
		return produceResults(testKeys.size(), testKeys, testSeqMap);
	}
	// testKeys：qids
	// testSeqMap：<qid, words>
	// rightDisMap: <qid, [kid,...]> eg: {3996636=[47630]}
	public void predict(int topK, List<Integer> testKeys, Map<Integer, String[]> testSeqMap,
			Map<Integer, List<String>> rightDisMap, boolean isLoose) {
		this.topK = topK;
		this.condProMap = ReadJSON.wordCondPro(); // wordKid,probability 条件概率
		this.beltaMap = ReadJSON.wordAdaboostPro(); // wordKid,adaboost_pro
		this.labelKeys = ReadJSON.knowledges(); // DISTINCT knowledgeId
		this.labelProMap = ReadJSON.knowledgePro(); // knowledgeId,probability 知识点概率
		this.labelNum = this.labelKeys.size(); // 知识点数量
		PredictResult[][] res = produceResults(testKeys.size(), testKeys, testSeqMap);
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(testKeys.size(), res, rightDisMap, isLoose);
		// !res 预测的结果--打印出来
		writePredictResult(testKeys.size(), res, rightDisMap);

		double totalLength = 0;
		Iterator<Integer> iter =  testSeqMap.keySet().iterator();
		while(iter.hasNext()){
			totalLength += testSeqMap.get(iter.next()).length;
		}
		double avgLen = (double) totalLength / testSeqMap.size();

		double sumR = 0.0, sumF = 0.0, presion, recall, fvalue;

		List<Integer> lessList = new ArrayList<Integer>();
		List<Integer> moreList = new ArrayList<Integer>();
		List<Integer> totalList = new ArrayList<Integer>();

		Iterator<Integer> itor = sampleStates.keySet().iterator();
		while (itor.hasNext()) {
			Integer qid = itor.next();
			if (testSeqMap.get(qid).length < avgLen && !lessList.contains(qid))
				lessList.add(qid);
			if (testSeqMap.get(qid).length >= avgLen && !moreList.contains(qid))
				moreList.add(qid);
			if (!totalList.contains(qid))
				totalList.add(qid);
		}
		for (Integer qid : lessList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, lessList, sampleStates);
		recall = sumR / (double) lessList.size();
		fvalue = sumF / (double) lessList.size();
		System.out.println("小于平均长度的准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);

		sumR = 0;
		sumF = 0;
		for (Integer qid : moreList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, moreList, sampleStates);
		recall = sumR / (double) moreList.size();
		fvalue = sumF / (double) moreList.size();
		System.out.println("大于平均长度的准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);

		// !不考虑平均长度
		sumR = 0.0;
		sumF = 0.0;
		for (Integer qid : totalList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, totalList, sampleStates);
		recall = sumR / (double) totalList.size();
		fvalue = sumF / (double) totalList.size();
		System.out.println("准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);	
	}

	// testKeys：qids
	// testSeqMap：<qid, words>
	// rightDisMap: <qid, [kid,...]> eg: {3996636=[47630]}
	public List<Double> validate(int topK, List<Integer> testKeys, Map<Integer, String[]> testSeqMap,
			Map<Integer, List<String>> rightDisMap, boolean isLoose) {
		this.topK = topK;
		this.condProMap = getConditionMap(); // wordKid,probability 条件概率
		this.beltaMap = getBeltaMap(); // wordKid,adaboost_pro
		this.labelKeys = getLabelKeys(); // DISTINCT knowledgeId
		this.labelProMap = getLabelProMap(); // knowledgeId,probability 知识点概率
		this.labelNum = this.labelKeys.size();
		PredictResult[][] res = produceResults(testKeys.size(), testKeys, testSeqMap);
		HashMap<Integer, ClassifyResult> sampleStates = getClassifyResult(testKeys.size(), res, rightDisMap, isLoose);

		double totalLength = 0;
		Iterator<Integer> iter =  testSeqMap.keySet().iterator();
		while(iter.hasNext()){
			totalLength += testSeqMap.get(iter.next()).length;
		}
		double avgLen = (double) totalLength / testSeqMap.size();

		double sumR = 0.0, sumF = 0.0, presion, recall, fvalue;

		List<Integer> lessList = new ArrayList<Integer>();
		List<Integer> moreList = new ArrayList<Integer>();
		List<Integer> totalList = new ArrayList<Integer>();

		Iterator<Integer> itor = sampleStates.keySet().iterator();
		while (itor.hasNext()) {
			Integer qid = itor.next();
			if (testSeqMap.get(qid).length < avgLen && !lessList.contains(qid))
				lessList.add(qid);
			if (testSeqMap.get(qid).length >= avgLen && !moreList.contains(qid))
				moreList.add(qid);
			if (!totalList.contains(qid))
				totalList.add(qid);
		}
		for (Integer qid : lessList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, lessList, sampleStates);
		recall = sumR / (double) lessList.size();
		fvalue = sumF / (double) lessList.size();
		System.out.println("小于平均长度的准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);

		sumR = 0;
		sumF = 0;
		for (Integer qid : moreList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, moreList, sampleStates);
		recall = sumR / (double) moreList.size();
		fvalue = sumF / (double) moreList.size();
		System.out.println("大于平均长度的准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);

		// !不考虑平均长度
		sumR = 0.0;
		sumF = 0.0;
		for (Integer qid : totalList) {
			ClassifyResult r = sampleStates.get(qid);
			sumR += r.recall;
			sumF += r.fvalue;
		}
		presion = 1 - calculateErrorRate(false, totalList, sampleStates);
		recall = sumR / (double) totalList.size();
		fvalue = sumF / (double) totalList.size();
		System.out.println("准确率为->" + presion + ",召回率为->" + recall + ",f1-值->" + fvalue);	
		
		List<Double> metrics = new ArrayList<Double>();
		metrics.add(presion);
		metrics.add(recall);
		metrics.add(fvalue);
		return metrics;
	}

	/**
	 * 预测知识点
	 * @param topK 预测的知识点数量
	 * @param testSeqMap 试题分词map
	 * @return result 预测的知识点结果
	 */
	public List<String> predictRightNow(int topK, Map<Integer, String[]> testSeqMap) {
		this.topK = topK;
		this.condProMap = ReadJSON.wordCondPro(); // wordKid,probability 条件概率
		this.beltaMap = ReadJSON.wordAdaboostPro(); // wordKid,adaboost_pro
		this.labelKeys = ReadJSON.knowledges(); // DISTINCT knowledgeId
		this.labelProMap = ReadJSON.knowledgePro(); // knowledgeId,probability 知识点概率
		this.labelNum = this.labelKeys.size(); // 知识点数量
		Map<String, String> labelNameMap = ReadJSON.knowledgesName();

		int num = testSeqMap.size();
		PredictResult[][] res = new PredictResult[num][labelNum];

		for (int i = 0; i < num; i++) {
			String[] words = testSeqMap.get(i);
			HashSet<String> wordSet = new HashSet<String>(Arrays.asList(words));
			for (int j = 0; j < labelNum; j++) {
				String labelKey = labelKeys.get(j);
				double p = 0.0;
				if (labelProMap.containsKey(labelKey)) {
					p = Math.exp(labelProMap.get(labelKey));
				} else {
					p = Math.exp(0.0);
				}
				for (String word : wordSet) {
					String key = labelKey + "_" + word;
					if (condProMap.containsKey(key))
						p += Math.exp(beltaMap.get(key) * condProMap.get(key));
				}
				PredictResult r = new PredictResult();
				r.postProbability = p;
				r.labelKey = labelKey;
				r.sampleKey = i;
				// !概率最大的就是这个测试对象的预测知识点
				// System.out.println("测试对象索引：" + i + " 对于知识点：" + labelKey + " 的后验概率：" + p);
				res[i][j] = r;
			}
		}
		// 预测结果
		HashMap<Integer, List<PredictResult>> predictResultMap = getClassifyResultMap(num, res);
		List<String> predictKids = new ArrayList<String>();
		for (int i = 0; i < num; i++) {
			Integer key = res[i][0].sampleKey; // 试题索引
			List<PredictResult> inferPredictResultList = predictResultMap.get(key);
			List<String> ids = new ArrayList<String>(); // 当前试题的预测知识点id列表
			for (PredictResult r : inferPredictResultList) {
				ids.add(labelNameMap.get(r.labelKey));
			}
			predictKids.add(String.join("|", ids));
		}
		return predictKids;
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