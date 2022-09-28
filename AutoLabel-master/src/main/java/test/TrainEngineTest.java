package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.DbChannel;
import dao.DbFactory;
import po.Result;
import segment.SegmentType;
import system.SystemConf;
import train.TrainEngine;
import utils.CommonUtils;

public class TrainEngineTest {
	private static TrainEngine engine = null;
	static DbChannel dbChannel = DbFactory.getDbChannel();
	static int[] testQuestionId = new int[101];
	int qid;
	static {
		SystemConf.loadSystemParams("autolabel.properties");
		testQuestionId = dbChannel.getAllTestQuestionIds(); 
		engine = new TrainEngine(SegmentType.HANLP);
	}
	
	public static void main(String[] args) {
		TrainEngineTest test = new TrainEngineTest();
		List<Result> all = test.execute(1000178277);
		if(all != null){
			int flushNums = 0,topK=10;
			List<Result> rr = new ArrayList<>();
			double sum = 0.0;
			for(int i=flushNums;i<flushNums+topK;i++){
				sum += all.get(i).getValue();
				rr.add(all.get(i));
			}
			
			for(Result r : rr) r.setPercentage(r.getValue()*100/sum);
			
			for (Result r : rr)
				System.out.println(r.getKnowledgeId() + "->" + r.getKnowledgeName() + "->" + r.getPercentage());
		}else{
			System.out.println("无");
		}
		
	}
	
	/**
	 * qid<0从t_question_content_mysubject_test随机取一个questionId
	 * @param qid
	 * @param source
	 * @return
	 * @throws IOException
	 */
	private List<Result> execute(int qid){
		return engine.execute(qid, getTextByQid(qid)); 
	}
	
	
	/**
	 * 从t_question_content_mysubject 还是 t_question_content_mysubject_test表取数据
	 * @param questionId
	 * @param source
	 * @return
	 */
	public String getTextByQid(int questionId) {
		String contentSql,itemSql;
		contentSql = "select questionId,content from t_question_content_mathematics_test where questionId=" + questionId;
		itemSql = "select questionId,content from t_question_item_mathematics_test where questionId=" + questionId;
		return getTextBySql(contentSql, itemSql);
	}
	
	
	private String getTextBySql(String contentSql, String itemSql) {
		ArrayList<ArrayList<String>> all = dbChannel.getContentsAndQuestionIdsList(contentSql);
		ArrayList<String> qids = all.get(0);
		ArrayList<ArrayList<String>> allItems = dbChannel.getContentsAndQuestionIdsList(itemSql);
		ArrayList<String> itemQids = allItems.get(0);
		ArrayList<String> content = all.get(1);
		ArrayList<String> itemContent = allItems.get(1);
		String tmp = "";
		for (int i = 0; i < qids.size(); i++) {
			tmp = CommonUtils.translate(qids.get(i), content.get(i));
			tmp = CommonUtils.assemble(qids.get(i), tmp, itemQids, itemContent);
		}
		System.out.println("原始文本:" + tmp);
		return tmp;
	}
}
