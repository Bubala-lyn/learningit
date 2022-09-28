package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import segment.Thulac;
import similarity.CosineSimiliarity;
import similarity.Levenshtein;
import similarity.LevenshteinSimiliarity;
import similarity.SimiliarityAlgorithm;
import system.Constant;

public class SimiliarityAlgorithmPK {

	SqlDao dao = new SqlDao();
	int topK = 10;//前topK个最相似的题目
	int k = 5;//前k个tfidf值最大的分词，-1时不规定分词的个数
	int questionId = 9225575;//9353205来自测试集
	
	public static void main(String[] args) throws IOException {
		SimiliarityAlgorithmPK pk = new SimiliarityAlgorithmPK();
		pk.topKSimiliarityQuestionsFromTrain(new Levenshtein());
		pk.topKSimiliarityQuestionsFromTrain(new LevenshteinSimiliarity());
		pk.topKSimiliarityQuestionsFromTrain(new CosineSimiliarity());
	}
	
	/**
	 * 给定questionId,从测试集中找到最相似的前k个题目
	 * @param topK 前k个题目最相似题目
	 * @param k 前k个tfidf值最大的词
	 * @param questionI
	 * @throws IOException 
	 */
	public void topKSimiliarityQuestionsFromTrain(SimiliarityAlgorithm algorithm) throws IOException{
		if(topK <=0 || k<=0) return;
		System.out.println("算法"+algorithm.getClass().getSimpleName()+"比较结果：");
		
		String text = dao.getTextByQid(true, questionId);
		//分词
		text =  new Thulac(false,true).segment(text.replace("、", ","));
		
		Map<String,Object> obj = dao.getKeyWordsByQIdAndTfidf(text, k, questionId);
		//遍历训练集
		List<Integer> qids =  dao.getAllQuestionIds(Constant.T_TRAIN_CONTENT);
		//从训练集选择指定题目里前k个tfidf值最大的词
		List<Tmp> tmps = new ArrayList<Tmp>();
		int i=0;
		for(int qid : qids){
			Map<String,Object> tmp = dao.getKeyWordsByQIdAndTfidf(null,k, qid);
			double dis = getSimiliarity(algorithm,obj,tmp);
			if(dis == 0.0) continue;
			Tmp t = new Tmp();
			t.setValue(dis);
			t.setKey(String.valueOf(qid));
			tmps.add(t);
		}
		this.sort(tmps);
		i=0;
		while(i<topK) System.out.println(tmps.get(i++).toString());
		System.out.println("-------------------------------------");
	}

	private void sort(List<Tmp> tmps) {
		Collections.sort(tmps, new Comparator<Tmp>() {
			@Override
			public int compare(Tmp o1, Tmp o2) {
				if(o1.getValue()==o2.getValue()) return Integer.valueOf(o1.getKey())-Integer.valueOf(o2.getKey())>0?-1:1;
				return o1.getValue()-o2.getValue()>0?-1:1;
			}
		});
	}

	private double getSimiliarity(SimiliarityAlgorithm algorithm,Map<String, Object> m1, Map<String, Object> m2) {
		String str1 = (String)m1.get("text");
		String str2 = (String)m2.get("text");
		if(algorithm instanceof Levenshtein){
			algorithm = new Levenshtein(str1,str2);
		}else if(algorithm instanceof LevenshteinSimiliarity){
			algorithm = new LevenshteinSimiliarity(str1,str2);
		}else if(algorithm instanceof CosineSimiliarity){
			algorithm = new CosineSimiliarity(str1,str2,(Map<String,Double>)m1.get("tfidfKV"),(Map<String,Double>)m2.get("tfidfKV"));
		}
		return algorithm.getSimiliarity();
	}
}