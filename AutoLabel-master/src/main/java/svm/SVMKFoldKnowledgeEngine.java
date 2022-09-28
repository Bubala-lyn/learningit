package svm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dao.DbChannel;
import dao.DbFactory;
import system.Constant;
import system.SystemConf;

public class SVMKFoldKnowledgeEngine {

	DbChannel dao = DbFactory.getDbChannel();
	Object[] matrix = new Object[2];
	List<String> smatrix;

	public SVMKFoldKnowledgeEngine() {
	}

	public void flushToFile(String _prex) {
		execSVM();

		List<Integer> testKeys = dao
				.getAllTestQuestionIdsFromMysubject(SystemConf.getValueByCode("selectAllTestQIdsFromMysubject"));
		Map<Integer, List<String>> rightDisMap = dao
				.getAllQuestionWeightFromTrain(SystemConf.getValueByCode("selectQuestionWeightFromTrainSql"));
		Map<Integer, String> contentMap = dao.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTestSql"));
		Map<Integer, String[]> testSeqMap = new HashMap<Integer, String[]>();
		Iterator<Integer> iter2 = contentMap.keySet().iterator();
		while (iter2.hasNext()) {
			Integer qId = iter2.next();
			String content = contentMap.get(qId);
			String[] words = content.split(",");
			testSeqMap.put(qId, words);
		}
		FileUtils.write(testKeys, _prex + ".txt");
		FileUtils.write(testKeys, rightDisMap, _prex + ".txt");
		SVMKnowledgeEngine.flushData(smatrix, matrix, testKeys, testSeqMap, _prex);
	}

	private void execSVM() {
		ArrayList<ArrayList<String>> content_datas = dao
				.getFilterContent(SystemConf.getValueByCode("selectFilterContentSql"), Constant.KEY3, 1);
		ArrayList<ArrayList<String>> item_datas = dao.getFilterContent(SystemConf.getValueByCode("selectFilterItemSql"),
				Constant.KEY3, 1);
		// List<String> kList = dao.getAllValidKnowledges();
		Map<String, String> qkMap = dao.getAllValidKnowledgeQuestionFromTrain2();

		// 所有词字典["化学","酸碱中和",...,"反应"]
		smatrix = dao.getPostList();
		int[][] x_matrix = new int[content_datas.size()][smatrix.size()];
		int[] y_matrix = new int[content_datas.size()];
		for (int i = 0; i < content_datas.size(); i++) {
			ArrayList<String> data = content_datas.get(i);
			String qid = data.get(0);
			for (int j = 0; j < item_datas.size(); j++) {
				ArrayList<String> i_data = item_datas.get(j);
				if (i_data.get(0).equals(qid))
					data.addAll(i_data);
			}
			int k = 0;
			int[] bz = new int[smatrix.size()];
			for (String s : smatrix)
				bz[k++] = data.indexOf(s) == -1 ? 0 : 1;
			x_matrix[i] = bz;
			y_matrix[i] = Integer.parseInt(qkMap.get(qid));
		}
		matrix[0] = x_matrix;
		matrix[1] = y_matrix;
	}
}