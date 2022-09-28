package adaboost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dao.DbChannel;
import dao.DbFactory;
import po.BaseResult;
import po.KWeight;
import po.KnowledgeNode;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import system.Constant;
import system.SystemConf;
import train.Train;

public class KFoldWeightEngine {

	private int k;
	private int epoch;
	private int times;
	private int tsize;
	private DbChannel dao = DbFactory.getDbChannel();
	private SegmentationService segmentService;
	private Train t;
	AdaBoostWeightEngine boost;
	Map<Integer, List<KWeight>> rightDisMap;

	/**
	 * 
	 * @param k
	 *            k折
	 * @param times
	 *            测试次数
	 * @param epoch
	 *            每一次测试的训练轮次
	 * @param segmentType
	 *            分词工具
	 * @param isNeedSegment
	 *            是否重新分词
	 */
	public KFoldWeightEngine(int k, int times, int epoch, SegmentType segmentType, boolean isNeedResegment) {
		this.k = k;
		this.times = times;
		this.epoch = epoch;
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		this.t = new Train(segmentType, segmentService);
		this.tsize = dao.getAllRecords(Constant.T_SOURCE_CONTENT) / k;
		this.rightDisMap = dao.getAllQuestionWeight();
		this.t.setCount(tsize);
		if (isNeedResegment)
			t.segment();
	}

	public void train() {
		for (int i = 0; i < times; i++) {

			dataWarm();

			t.updateAndCompute();

			startAdaBoost();
		}
	}

	private void dataWarm() {
		Random r = new Random();
		k = k > 10 ? 10 : k < 5 ? 5 : k;
		List<String> labelKeys = dao.getAllValidTrainKnowledges(); // 获取不重复知识点list
		// modified at 2021-1-10
		// 参数null会导致报错,不知道为啥要传递null,参考其他程序设置了一个参数
		// Map<Integer, List<Integer>> map = dao.getAllValidKnowledgeQuestionFromSource(null);
		Map<Integer, List<Integer>> map = dao.getAllValidKnowledgeQuestionFromSource(SystemConf.getValueByCode("selectAllSegmentData"));
		// 测试集
		Set<Integer> tQus = new HashSet<Integer>();
		for (String kid : labelKeys) {
			List<Integer> qs = map.get(Integer.parseInt(kid));
			int size = qs.size() / k, preSize = tQus.size(), modCnt = 0;
			while (tQus.size() < tsize && modCnt < size) {
				tQus.add(qs.get(r.nextInt(qs.size())));
				modCnt = tQus.size() - preSize;
			}
		}
		while (tQus.size() < tsize) {
			int index = r.nextInt(labelKeys.size());
			String randKey = labelKeys.get(index);
			List<Integer> randQs = map.get(Integer.parseInt(randKey));
			while (tQus.size() < tsize) {
				tQus.add(randQs.get(r.nextInt(randQs.size())));
			}
		}
		String qidStr = "(";
		Iterator<Integer> iter = tQus.iterator();
		while (iter.hasNext()) {
			qidStr += String.valueOf(iter.next()) + ",";
		}
		qidStr = qidStr.substring(0, qidStr.length() - 1) + ")";
		System.out.println("tQus size: "+tQus.size()+";tsize: "+tsize+";qidStr: "+qidStr);
		dao.delete(Constant.T_TRAIN_CONTENT, true);// 训练题目题干数据
		dao.delete(Constant.T_TRAIN_CONTENTSEG, true);// 训练题目题干数据
		dao.delete(Constant.T_TRAIN_ITEM, true);// 训练题目选项数据
		dao.delete(Constant.T_TRAIN_ITEMSEG, true);// 训练题目选项数据
		dao.delete(Constant.T_TEST_CONTENT, true);// 测试题目题干数据
		dao.delete(Constant.T_TEST_ITEM, true);// 测试题目选项数据
		dao.delete(Constant.T_TEST_WORD, true);// content测试数据
		// 划分训练集和测试集
		// 训练集
		String trainSql = "insert into t_paper_question_content_mysubject_train(questionId,content) select questionId,content from t_question_content_mathematics_copy1 where questionId not in "
				+ qidStr;
		String trainSql2 = "insert into t_paper_question_item_mysubject_train(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId not in "
				+ qidStr;
		String trainSql3 = "insert into t_question_content_mathematics_seg(questionId,raw_content,seg_content,after_filter_content,length,item) select questionId,raw_content,seg_content,after_filter_content,length,item from t_question_content_mathematics_seg where questionId not in "
				+ qidStr;
		String trainSql4 = "insert into t_question_item_mathematics_seg(itemId,questionId,raw_content,seg_content,after_filter_content,length) select itemId,questionId,raw_content,seg_content,after_filter_content,length from t_question_item_mathematics_seg where questionId not in "
				+ qidStr;
		// 插入测试集
		String testSql = "insert into t_question_content_mathematics_test(questionId,content) select questionId,content from t_question_content_mathematics_copy1 where questionId in "
				+ qidStr;
		String testSql2 = "insert into t_question_item_mathematics_test(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId in "
				+ qidStr;
		dao.update(trainSql);
		dao.update(trainSql2);
		dao.update(trainSql3);
		dao.update(trainSql4);
		dao.update(testSql);
		dao.update(testSql2);

		// 将测试集样本分词
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
		List<String> labelKeys = dao.getAllValidTrainKnowledges();
		Map<Integer, String> questionMap = dao
				.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTrainSql"));
		Map<Integer, List<Integer>> labelSampleDisMap = dao.getAllValidKnowledgeQuestionFromTrain();
		Map<String, Double> condProMap = dao
				.getMapResult(SystemConf.getValueByCode("selectKnowledgeWordProbabilitySql"));
		Map<String, Double> labelProMap = dao.getMapResult(SystemConf.getValueByCode("selectKnowledgeProbabilitySql"));
		// modified at 2021-1-10
		// 因为不知道存储的是怎样的sql语句,所以忽略了该功能
		// Map<String, KnowledgeNode> knMap = dao.getDistanceMap();
		Map<String, KnowledgeNode> knMap = null;

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

		boost = new AdaBoostWeightEngine(epoch, trainKeys, labelKeys, trainSeqMap, condProMap, labelProMap,
				rightDisMap, knMap, labelSampleDisMap);
		boost.train();
	}

	protected void batchPredict() {
		Map<Integer, String> contentMap = dao.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTestSql"));
		List<Integer> testKeys = new ArrayList<Integer>();
		Map<Integer, String[]> testSeqMap = new HashMap<Integer, String[]>();
		Iterator<Integer> iter2 = contentMap.keySet().iterator();
		while (iter2.hasNext()) {
			Integer qId = iter2.next();
			String content = contentMap.get(qId);
			String[] words = content.split(",");
			testSeqMap.put(qId, words); // 问题id：问题分词列表
			testKeys.add(qId); // 问题id
		}
		boost.predict(testKeys, testSeqMap, rightDisMap);
		//return boost.predict(testKeys, testSeqMap, rightDisMap);
	}

	public BaseResult[] prefdictResult(Integer sampleKey, List<String> labels) {
		if (boost == null)
			boost = new AdaBoostWeightEngine();
		Map<Integer, String> contentMap = dao.getAllQuestions(SystemConf.getValueByCode("selectSingleQuestionFromTestSql").replace("?", String.valueOf(sampleKey)));
		if(contentMap == null) return null;
		String content = contentMap.get(sampleKey);
		if(content == null) return null;
		String[] words = content.split(",");
		return boost.prefdictResult(sampleKey, words, labels);
	}
}