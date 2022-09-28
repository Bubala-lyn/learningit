package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import po.KWeight;
import po.KnowledgeNode;
import po.Result;

public interface DbChannel {

	public int getAllRecords(String table);

	public ArrayList<ArrayList<String>> getFilterContent(String sql, String[] args, int runWhich);

	public void insert(String sql, String[][] datas);

	public void insert(String sql, HashMap<Integer, String> datas);

	public void delete(String table, boolean autoIncrementFlush);

	public void truncate(String table);

	public void update(String sql);

	public List<String> getAllValidKnowledges();

	public Map<String, Double> getMapResult(String sql);

	public int insertKnowledgeProbability(String sql, int count);

	public void insertOrUpdateCommand(String sql, int keyIndex, int valueIndex, Map<String, Double> map);

	public Map<String, ArrayList<String>> getAllFenciFromWord();

	public Map<String, Integer> getAllValidKnowledgeQuestionCount();

	public List<String> getAllWordsFromCount();
	
	public List<String> getAllValidTrainKnowledges();
	
	public List<Integer> getAllQuestionIds(String table);
	
	public int[] getAllTestQuestionIds();

	public List<String> getQuestionIdsWithCond(String cond);
	
	public List<Integer> getAllTestQuestionIdsFromMysubject(String sql);
	
	public HashMap<Integer, List<String>> getKnowledges(String table);
	
	public void insertMap(String sql, HashMap<Integer, List<Result>> map);
	
	public void insertPresionAndRecallAndFvalue(String sql, HashMap<Integer, List<Double>> map);
	
	public ArrayList<ArrayList<String>> getContentsAndQuestionIdsList(String sql);
	
	public String getTextByQid(boolean isTest, int qid);
	
	public List<String> getPostList();
	
	public Map<Integer, List<Integer>> getAllValidKnowledgeQuestionFromSource(String sql);
	
	public Map<Integer, List<Integer>> getAllValidKnowledgeQuestionFromTrain();
	
	public Map<String, String> getAllValidKnowledgeQuestionFromTrain2();

	public Map<String, String> getKnowledgeNameByKnowledgeIds(List<Result> results);

	public Map<String, String> getAllKnowledgeNames();

	public double[] getTfidfWeight(String[] strs);

	public Map<String, Object> getKeyWordsByQIdAndTfidf(String str, int k, int qid);

	public Map<Integer, String> getAllQuestions(String sql);

	public Map<Integer, List<String>> getAllQuestionWeightFromTrain(String sql);
	
	public Map<Integer, List<KWeight>> getAllQuestionWeight();

	public double getMaxTFIDFByWords(String content);

	public String filterWords(double maxTFIDF, String content, int belta);

	Map<String, KnowledgeNode> getDistanceMap();

	public int stamp();

	public void close();
}
