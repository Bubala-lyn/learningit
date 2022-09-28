package adaboost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

import dao.DbChannel;
import dao.DbFactory;
import po.KnowledgeNode;
import system.SystemConf;
import system.Constant;
import system.Context;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import train.Train;

import utils.WriteJSON;
import utils.FileOperation;

public class KFoldKnowledgeEngine {

	int k = 10;
	int topK;
	int epoch;
	int tsize; // 每份的题目数量
	DbChannel dao;
	SegmentType segmentType = SegmentType.JIEBA; // 默认使用结巴分词
	SegmentationService segmentService;
	Train t;
	AdaBoostKnowledgeEngine boost;
	List<List<Double>> metrics = new ArrayList<List<Double>>(); // K折模型的性能指标

	/**
	 * 
	 * @param epoch
	 *            每一次实验的训练轮次
	 * @param topK
	 *            推断的知识点数量
	 */
	public KFoldKnowledgeEngine(int epoch, int topK, boolean isNeedResegment) {
		this.epoch = epoch;
		this.topK = topK;
		this.dao = DbFactory.getDbChannel();
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		this.t = new Train(segmentType, segmentService);
		this.tsize = dao.getAllRecords(Constant.T_SOURCE_CONTENT) / (k + 1); // !需要从原始数据中先分出一份做测试集
		if (isNeedResegment)
			t.segment(); // 对原始数据进行分词
	}

	public KFoldKnowledgeEngine(int topK) {
		this.topK = topK;
	}
	
	public void train() {
		List<String> qidStrList = kFoldDataSplit();
		String testQidStr = qidStrList.remove(0); // 把第一份固定作为测试集

		this.t.setCount(tsize * (k - 1));

		System.out.println("每份数据量：" + tsize);
		// K折验证实验
		for(int i = 0;i < k; i++){
			System.out.println("第" + (i + 1) + "折实验======>");
			// 每次把其中一份作为验证集
			dataWarm(qidStrList.get(i), testQidStr);
			t.updateAndCompute();
			startAdaBoost();
			saveProbability(i);
			predict();
		}
		calcGeneralError();
		testModel(testQidStr);
		chooseBestModel();
	}
	public void predict() {
		Map<Integer, String> contentMap = dao.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTestSql"));
		Map<String, KnowledgeNode> knMap = dao.getDistanceMap();
		if (boost == null)
			boost = new AdaBoostKnowledgeEngine(knMap);
		List<Integer> testKeys = new ArrayList<Integer>();
		Map<Integer, String[]> testSeqMap = new HashMap<Integer, String[]>();
		Iterator<Integer> iter2 = contentMap.keySet().iterator();
		while (iter2.hasNext()) {
			Integer qId = iter2.next();
			String content = contentMap.get(qId);
			String[] words = content.split(",");
			testSeqMap.put(qId, words);
			testKeys.add(qId);
		}
		Map<Integer, List<String>> rightDisMap = dao
				.getAllQuestionWeightFromTrain(SystemConf.getValueByCode("selectQuestionWeightFromTrainSql"));
		List<Double> result = boost.validate(topK, testKeys, testSeqMap, rightDisMap, false);
		metrics.add(result); // 缓存每折训练的模型性能
	}

	public List<String> inferByInput(List<String> contents) {
		// 1. 读取输入的题目
		// 1.1 假设这些题目都是纯文本
		// 2. 题目处理（分词）
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		HashMap<Integer, String[]> contentMap = new HashMap<Integer, String[]>();
		int i = 0;
		for (String content : contents) {
			String[] cwords = segmentService.segment(content).split(" ");
			contentMap.put(i++, cwords);
		}
		// 3. 调用 boost 程序
		if (boost == null)
			boost = new AdaBoostKnowledgeEngine(null);
		return boost.predictRightNow(topK, contentMap);
	}

	// K折交叉实验：原始数据 - 测试集 - 验证集 = 训练集
	// 测试：原始数据 - 测试集 = 训练集
	private void dataWarm(String qidStr, String exQidStr) {
		System.out.println("开始分数据...");
		dao.truncate(Constant.T_TRAIN_CONTENT);// 训练题目题干数据
		dao.truncate(Constant.T_TRAIN_CONTENTSEG);// 训练题目题干数据
		dao.truncate(Constant.T_TRAIN_ITEM);// 训练题目选项数据
		dao.truncate(Constant.T_TRAIN_ITEMSEG);// 训练题目选项数据
		dao.truncate(Constant.T_TEST_CONTENT);// 测试题目题干数据
		dao.truncate(Constant.T_TEST_ITEM);// 测试题目选项数据
		dao.truncate(Constant.T_TEST_WORD);// content测试数据
		// 划分训练集和验证集
		// 训练集
		String trainSql = "insert into t_question_content_mathematics_train(questionId,content) select questionId,concat('<document>', IFNULL(content,''),IFNULL(answer,''),IFNULL(analyse,''), '</document>') as content from t_question_content_mathematics_copy1 where questionId not in " + qidStr + "and questionId not in " + exQidStr;
		String trainSql2 = "insert into t_question_item_mathematics_train(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId not in " + qidStr + "and questionId not in " + exQidStr;
		String trainSql3 = "insert into t_question_content_mathematics_seg_train(questionId,raw_content,seg_content,after_filter_content,length,item) select questionId,raw_content,seg_content,after_filter_content,length,item from t_question_content_mathematics_seg where questionId not in " + qidStr + "and questionId not in " + exQidStr;
		String trainSql4 = "insert into t_question_item_mathematics_seg_train(itemId,questionId,raw_content,seg_content,after_filter_content,length) select itemId,questionId,raw_content,seg_content,after_filter_content,length from t_question_item_mathematics_seg where questionId not in " + qidStr + "and questionId not in " + exQidStr;

		// 原始数据插入测试集
		String testSql = "insert into t_question_content_mathematics_test(questionId,content) select questionId,concat('<document>', IFNULL(content,''),IFNULL(answer,''),IFNULL(analyse,''), '</document>') as content from t_question_content_mathematics_copy1 where questionId in "
				+ qidStr;
		String testSql2 = "insert into t_question_item_mathematics_test(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId in "
				+ qidStr;
		dao.update(trainSql);
		dao.update(trainSql2);
		dao.update(trainSql3);
		dao.update(trainSql4);
		dao.update(testSql);
		dao.update(testSql2);

		System.out.println("训练集数据量：" + dao.getAllRecords(Constant.T_TRAIN_CONTENT));
		System.out.println("验证集数据量：" + dao.getAllRecords(Constant.T_TEST_CONTENT));

		// 将验证集样本分词
		List<Integer> testKeys = dao.getAllQuestionIds(Constant.T_TEST_CONTENT);
		HashMap<Integer, String> contentMap = new HashMap<Integer, String>();
		for (Integer qid : testKeys) {
			contentMap.put(qid, dao.getTextByQid(true, qid));
		}
		Iterator<Integer> iter2 = contentMap.keySet().iterator();
		while (iter2.hasNext()) {
			Integer qId = iter2.next();
			String content = contentMap.get(qId);
			String cwords = segmentService.segment(content).replace(" ", ",");
			contentMap.put(qId, cwords);
		}
		dao.insert(SystemConf.getValueByCode("insertTestWordSql"), contentMap);
	}

	private void startAdaBoost() {
		Map<Integer, String> questionMap = dao
				.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTrainSql"));
		Map<Integer, List<Integer>> labelSampleDisMap = dao.getAllValidKnowledgeQuestionFromTrain();
		Map<Integer, String[]> trainSeqMap = new HashMap<Integer, String[]>();
		List<Integer> trainKeys = new ArrayList<Integer>();
		Iterator<Integer> iter = questionMap.keySet().iterator();
		while (iter.hasNext()) {
			Integer qId = iter.next();
			String content = questionMap.get(qId);
			String[] words = content.split(",");
			trainSeqMap.put(qId, words);
			trainKeys.add(qId);
		}
		Collections.sort(trainKeys);
		List<String> labelKeys = dao.getAllValidTrainKnowledges();
		Map<String, Double> condProMap = dao
				.getMapResult(SystemConf.getValueByCode("selectKnowledgeWordProbabilitySql"));
		Map<String, Double> labelProMap = dao.getMapResult(SystemConf.getValueByCode("selectKnowledgeProbabilitySql"));
		Map<Integer, List<String>> rightDisMap = dao
				.getAllQuestionWeightFromTrain(SystemConf.getValueByCode("selectQuestionWeightFromTrainSql"));
		Map<String, KnowledgeNode> knMap = dao.getDistanceMap();
		boost = new AdaBoostKnowledgeEngine(epoch, topK, trainKeys, labelKeys, trainSeqMap, condProMap, labelProMap,
				rightDisMap, knMap, labelSampleDisMap);
		boost.train();
	}

	// 训练集数据随机拆分成 K + 1 份
	private List<String> kFoldDataSplit() {
		List<String> qidStrList = new ArrayList<String>();
		for (int i = 0; i< k + 1; i++) {
			String cond = "";
			if (i > 0)
				cond = "where questionId not in" + String.join("and questionId not in ", qidStrList);
			if (i < k)
				cond += " Order By rand() Limit " + tsize;
			List<String> qids = dao.getQuestionIdsWithCond(cond);
			String qidStr = "(" + String.join(",", qids) + ")";
			qidStrList.add(qidStr);
		}
		return qidStrList;
	}

	// 缓存每折计算的概率
	private void saveProbability(int k) {
			// 1. 知识点的先验概率
			Map < String, Double > labelProMap = boost.getLabelProMap();
			WriteJSON.knowledgePro(labelProMap, k);
			// 2. 条件概率
			Map < String, Double > condProMap = boost.getConditionMap();
			WriteJSON.wordCondPro(condProMap, k);
			// 3. adaboost 条件概率
			Map < String, Double > beltaMap = boost.getBeltaMap();
			WriteJSON.wordAdaboostPro(beltaMap, k);
	}

	// 模型测试
	private void testModel(String testQidStr) {
		System.out.println("在完整训练集上训练，在测试集上计算模型性能======>");
		this.t.setCount(tsize * k);
		dataWarm(testQidStr, "(0)");
		t.updateAndCompute();
		startAdaBoost();
		saveProbability(k);
		predict();
	}

	// 计算 k 折的平均误差，作为该模型的泛化误差
	private void calcGeneralError() {
		double sumP = 0.0, sumR = 0.0, sumF = 0.0, avgP, avgR, avgF;
		for (List<Double> metric: metrics) {
			sumP += metric.get(0);
			sumR += metric.get(1);
			sumF += metric.get(2);
		}
		avgP = sumP / k;
		avgR = sumR / k;
		avgF = sumF / k;

		System.out.println(k + "折交叉实验后，模型的平均精确度：" + avgP + " 平均召回率：" + avgR + " 平均F1分数：" + avgF);
	}

	// 根据各个模型的性能选择最优模型
	private void chooseBestModel() {
		// 保留最优的概率，用于知识点推断
		System.out.println("根据精确度选择最优模型");
		Double maxP = 0.0;
		Integer maxK = 0;
		for (int i = 0; i < metrics.size(); i++) {
			List<Double> metric = metrics.get(i);
			Double p = metric.get(0);
			if (p > maxP) {
				maxP = p;
				maxK = i;
			}
		}
		System.out.println("第" + maxK + "折模型的精确度最高：" + maxP);
		new FileOperation().copyFile2Resources("knowledgePro" + maxK + ".json", "knowledgePro.json");
		new FileOperation().copyFile2Resources("wordCondPro" + maxK + ".json", "wordCondPro.json");
		new FileOperation().copyFile2Resources("wordAdaboostPro" + maxK + ".json", "wordAdaboostPro.json");
	}
}