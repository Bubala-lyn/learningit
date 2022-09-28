package bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dao.DbChannel;
import dao.DbFactory;
import po.KnowledgeNode;
import system.SystemConf;

public class NBKFoldKnowledgeEngine {

	int topK;
	DbChannel dao = DbFactory.getDbChannel();
	NBKnowledgeEngine engine;

	public NBKFoldKnowledgeEngine(int topK) {
		this.topK = topK;
	}

	/**
	 * 预测样本集的准确度
	 * 
	 * @return
	 */
	public void predict() {
		if (engine == null) {
			Map<String, KnowledgeNode> knMap = dao.getDistanceMap();
			engine = new NBKnowledgeEngine(knMap);
		}
		Map<Integer, String> contentMap = dao.getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTestSql"));
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
		engine.predict(topK, testKeys, testSeqMap, rightDisMap);
	}
}