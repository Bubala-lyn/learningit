package adaboost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dao.DbChannel;
import dao.DbFactory;
import po.KnowledgeNode;
import system.SystemConf;
import system.Constant;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import train.Train;

public class KnowledgeEngine {

	int topK; // 知识点数量？
	int epoch; // 训练轮次
	int tsize; // 题目数量
	DbChannel dao = DbFactory.getDbChannel(); // 连接训练集
	DbChannel dao2 = DbFactory.getDbChannel(SystemConf.getValueByCode("url2")); // 连接验证集
	SegmentType segmentType = SegmentType.JIEBA;
	SegmentationService segmentService;
	Train t;
	AdaBoostKnowledgeEngine boost;

	public KnowledgeEngine(int epoch, int topK) {
		this.epoch = epoch;
		this.topK = topK;
	}
	
	public KnowledgeEngine(int topK) {
		this.topK = topK;
	}

	public void train() {
		trainDataWarm();
		t.updateAndCompute();
		startAdaBoost();
	}
	// 验证
	public void predict(boolean isLoose) {
		System.out.println("isLoose: " + isLoose);
		predictDataWarm();
		Map<Integer, String> contentMap = dao2.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTestSql"));
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
		Map<Integer, List<String>> rightDisMap = dao2
				.getAllQuestionWeightFromTrain(SystemConf.getValueByCode("selectQuestionWeightFromTestSql"));
		boost.predict(topK, testKeys, testSeqMap, rightDisMap, isLoose);
	}

	// 训练集分词
	private void trainDataWarm() {
		
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		this.t = new Train(segmentType, segmentService);
		this.tsize = dao.getAllRecords(Constant.T_SOURCE_CONTENT);
		this.t.setCount(tsize);
		t.segment();
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
	// 验证集分词
	private void predictDataWarm() {
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		dao2.delete(Constant.T_TEST_WORD, true);// content测试数据
		// 将测试集样本分词
		List<Integer> testKeys = dao2.getAllQuestionIds(Constant.T_TEST_CONTENT);
		HashMap<Integer, String> contentMap = new HashMap<Integer, String>();
		for (Integer qid : testKeys) {
			contentMap.put(qid, dao2.getTextByQid(true, qid));
		}
		Iterator<Integer> iter2 = contentMap.keySet().iterator();
		while (iter2.hasNext()) {
			Integer qId = iter2.next();
			String content = contentMap.get(qId);
			String cwords = segmentService.segment(content).replace(" ", ",");
			contentMap.put(qId, cwords);
		}
		dao2.insert(SystemConf.getValueByCode("insertTestWordSql"), contentMap);
	}
}