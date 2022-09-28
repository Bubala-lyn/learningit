package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.DbChannel;
import dao.DbFactory;
import po.Result;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import system.SystemConf;
import train.TrainEngine;

public class BatchTest {
	static TrainEngine engine = null;
	static DbChannel dbChannel = DbFactory.getDbChannel();
	SegmentationService service = SegmentationFactory.createSegmentation(SegmentType.JIEBA);
	static {
		SystemConf.loadSystemParams("autolabel.properties");
		engine = new TrainEngine(SegmentType.HANLP);
	}

	public static void main(String[] args) {
		BatchTest test = new BatchTest();
		test.batchlabel(5);
	}

	public int batchlabel(int topK) {
		System.out.println("开始执行");
		String insertSql1 = "insert into t_paper_autolabel_result(questionId,knowledgeId,knowledgeName,precentage) values(?,?,?,?)";
		String insertSql2 = "insert into t_paper_autolabel_rp(questionId,presion,recall,fvalue) values(?,?,?,?)";
		// 用于计算f-value值
		HashMap<Integer, List<String>> rightMap = dbChannel.getKnowledges("t_paper_autolabel_right");
		HashMap<Integer, List<Result>> resultMap = new HashMap<Integer, List<Result>>();
		HashMap<Integer, String> contentMap = new HashMap<Integer, String>();
		try {

			dbChannel.delete("t_paper_autolabel_rp", true);
			dbChannel.delete("t_paper_autolabel_result", true);

			List<Integer> qids = dbChannel.getAllQuestionIds("t_question_content_mathematics_test");

			if (qids == null || qids.isEmpty())
				return -1;

			StringBuffer sb = new StringBuffer("");
			for (Integer qid : qids) {
				contentMap.put(qid, dbChannel.getTextByQid(true, qid));
				sb.append(qid).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			// 删除t_paper_question_mysubject_word_train表中测试集中的记录
			dbChannel.update(
					"delete from t_question_mathematics_word where questionId in(" + sb.toString() + ")");

			System.out.println("分词开始");
			HashMap<Integer, String> segMap = getSegMap(contentMap);
			System.out.println("分词结束");
			// 更新t_paper_question_mysubject_word_train,存放questionId,words(一个题目的完整词组)
			dbChannel.insert(SystemConf.getValueByCode("insertTrainWordSql"), segMap);

			HashMap<Integer, double[]> weightMap = getWeightMap(segMap);

			System.out.println("打标签开始");
			for (Integer qid : qids) {

				List<Result> results = engine.execute(qid, contentMap.get(qid));
				// List<Result> results = engine.execute(qid , segMap.get(qid),
				// weightMap.get(qid));

				List<Result> rs = new ArrayList<>();
				double sum = 0.0;
				for (int i = 0; i < topK && i < results.size(); i++) {
					if (results.get(i).getValue() == 0.0)
						break;
					sum += results.get(i).getValue();
					results.get(i).setRawContent(contentMap.get(qid));
					rs.add(results.get(i));
				}
				double t = 0;
				for (int i = 0; i < rs.size(); i++) {
					Result r = rs.get(i);
					double p = Math.floor(r.getValue() * 100 / sum * 10) / 10;
					if (i != rs.size() - 1) {
						t += p;
						r.setPercentage(p);
					} else
						r.setPercentage(Math.floor((100 - t) * 10) / 10);
				}
				resultMap.put(qid, rs);
			}
			System.out.println("打标签结束");
			System.out.println("插入开始");
			dbChannel.insertMap(insertSql1, resultMap);
			System.out.println("插入结束");
			HashMap<Integer, List<String>> recomMap = dbChannel.getKnowledges("t_paper_autolabel_result");
			compute(insertSql2, qids, recomMap, rightMap);
			System.out.println("结束执行");
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	private void compute(String insertSql, List<Integer> qids, HashMap<Integer, List<String>> recomMap,
			HashMap<Integer, List<String>> rightMap) {
		System.out.println("开始计算");
		HashMap<Integer, List<Double>> pbmap = new HashMap<Integer, List<Double>>();
		for (Integer id : qids) {
			List<String> rightList = rightMap.get(id);
			if (rightList == null || rightList.isEmpty())
				continue;
			List<String> recomList = recomMap.get(id);
			double presion = 0.0, recall = 0.0, fvalue = 0.0;
			if (rightList.size() == 1) {
				presion = recomList.contains(rightList.get(0)) ? 100 : 0.0;
				recall = recomList.contains(rightList.get(0)) ? 20 : 0;
				fvalue = recomList.contains(rightList.get(0)) ? 2 * presion * recall / (presion + recall) : 0;
			} else {
				int size1 = rightList.size();
				int size3 = recomList.size();
				rightList.retainAll(recomList);
				int size2 = rightList.size();
				presion = (double) size2 * 100 / size1;
				recall = (double) size2 * 100 / size3;
				fvalue = size2 == 0 ? 0 : 2 * presion * recall / (presion + recall);
			}
			List<Double> list = new ArrayList<Double>(3);
			list.add(presion);
			list.add(recall);
			list.add(fvalue);
			pbmap.put(id, list);
		}
		System.out.println("结束计算");
		System.out.println("开始插入....");
		dbChannel.insertPresionAndRecallAndFvalue(insertSql, pbmap);
		System.out.println("结束插入....");
	}

	private HashMap<Integer, String> getSegMap(HashMap<Integer, String> contentMap) {
		HashMap<Integer, String> segMap = new HashMap<Integer, String>();
		for (Map.Entry<Integer, String> entry : contentMap.entrySet()) {
			String rawstr = entry.getValue();
			String content = service.segment(rawstr);
			content = content.substring(0, content.length() - 1);
			segMap.put(entry.getKey(), content.replace(" ", ","));
		}
		return segMap;
	}

	private HashMap<Integer, double[]> getWeightMap(HashMap<Integer, String> segMap) {
		HashMap<Integer, double[]> weightMap = new HashMap<Integer, double[]>();
		for (Map.Entry<Integer, String> entry : segMap.entrySet()) {
			String[] strs = entry.getValue().split(",");
			double weight[] = dbChannel.getTfidfWeight(strs);
			weightMap.put(entry.getKey(), weight);
		}
		return weightMap;
	}
}