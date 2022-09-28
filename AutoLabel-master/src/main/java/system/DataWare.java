package system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import bayes.NBKnowledgeEngine;
import dao.DbChannel;
import dao.DbFactory;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import train.Train;

public class DataWare {

	int k;
	int topK;
	int tsize;
	DbChannel dao = DbFactory.getDbChannel();
	SegmentationService segmentService;
	Train t;
	NBKnowledgeEngine engine;

	/**
	 * 
	 * @param kfold
	 *            k折
	 * @param times
	 *            测试次数
	 * @param epoch
	 *            每一次测试的训练轮次
	 * @param topK
	 *            推荐前topK个知识点
	 * @param segmentType
	 *            分词工具
	 * @param isNeedSegment
	 *            是否重新分词
	 */
	public DataWare(int kfold, SegmentType segmentType) {
		this.k = kfold > 10 ? 10 : kfold < 5 ? 5 : kfold;
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
		int size = dao.getAllRecords(Constant.T_SOURCE_CONTENT);
		this.t = new Train(segmentType, segmentService);
		this.t.setCount(size * (k - 1) / k);
		this.tsize = size / k;
	}

	public void prepareData() {
		System.out.println("数据准备开始...........");

		t.segment();
	}

	public void initData() {
		Random r = new Random();
		Map<Integer, List<Integer>> map = dao.getAllValidKnowledgeQuestionFromSource(SystemConf.getValueByCode("selectAllSegmentData"));
		List<String> labelKeys = dao.getAllValidTrainKnowledges();
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
		dao.delete(Constant.T_TRAIN_CONTENT, true);// 训练题目题干数据
		dao.delete(Constant.T_TRAIN_CONTENTSEG, true);// 训练题目题干数据
		dao.delete(Constant.T_TRAIN_ITEM, true);// 训练题目选项数据
		dao.delete(Constant.T_TRAIN_ITEMSEG, true);// 训练题目选项数据
		dao.delete(Constant.T_TEST_CONTENT, true);// 测试题目题干数据
		dao.delete(Constant.T_TEST_ITEM, true);// 测试题目选项数据
		dao.delete(Constant.T_TEST_WORD, true);// content测试数据

		// 训练集
		String trainSql  = "insert into t_paper_question_content_mysubject_train(questionId,content) select questionId,content from t_question_content_mathematics_copy1 where questionId not in "
				+ qidStr;
		String trainSql2 = "insert into t_paper_question_item_mysubject_train(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId not in "
				+ qidStr;
		String trainSql3 = "insert into t_question_content_mathematics_seg(questionId,raw_content,seg_content,after_filter_content,length,item) select questionId,raw_content,seg_content,after_filter_content,length,item from t_question_content_mathematics_seg where questionId not in "
				+ qidStr;
		String trainSql4 = "insert into t_question_item_mathematics_seg(itemId,questionId,raw_content,seg_content,after_filter_content,length) select itemId,questionId,raw_content,seg_content,after_filter_content,length from t_question_item_mathematics_seg where questionId not in "
				+ qidStr;
		// 插入测试集
		String testSql   = "insert into t_question_content_mathematics_test(questionId,content) select questionId,content from t_question_content_mathematics_copy1 where questionId in "
				+ qidStr;
		String testSql2  = "insert into t_question_item_mathematics_test(itemId,questionId,content) select itemId,questionId,content from t_question_item_mathematics_copy1 where questionId in "
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
		
		t.updateAndCompute();

		System.out.println("数据准备结束...........");
	}
}