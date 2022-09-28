package adaboost;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dao.DbFactory;
import po.KnowledgeNode;
import system.SystemConf;

public abstract class AbstractAdaBoost {

	Map<String, KnowledgeNode> nodes;

	public AbstractAdaBoost(Map<String, KnowledgeNode> kNodes) {
		this.nodes = kNodes;
	}

	public AbstractAdaBoost() {
	}

	/**
	 * 判断两个知识点距离 (1)属同一节点则为1 (2)属于同一章为2 (3)既不属于同一章也不属于同一节，则为3
	 * 
	 * @param key1
	 * @param key2
	 * @return
	 */
	private final int distance(String key1, String key2) {
		int distance = 0;
		KnowledgeNode n1 = nodes.get(key1);
		KnowledgeNode n2 = nodes.get(key2);
		while (n1 != null && n2 != null && !n1.key.equals(n2.key)) {
			distance++;
			if (n1.pKey == null || n2.pKey == null)
				break;
			n1 = nodes.get(n1.pKey);
			n2 = nodes.get(n2.pKey);
		}
		return distance;
	}

	// !如果是宽松的模式，则提高补偿精确度
	public double reckonPresion(List<String> errorLabels, List<String> missHitLabels, boolean isLoose) {
		double presion = 0;
		HashMap<String, Double> map = new HashMap<String, Double>();
		for (String mk : missHitLabels) {
			for (String ek : errorLabels) {
				int d = distance(mk, ek);
				// System.out.println(mk + "," + ek + " distance:" + d);
				if (d == 1)
					map.put(mk + ek, isLoose ? 0.5 : 0.2); // 同一节
				else if (d == 2)
					map.put(mk + ek, isLoose ? 0.2 : 0.1); // 同一章
			}
		}
		for (String mk : missHitLabels) {
			double p = 0;
			String key = null;
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				String str = iter.next();
				if (str.contains(mk) && p < map.get(str)) {
					p = map.get(str);
					key = str;
				}
			}
			if (key != null)
				map.remove(key);
			presion += p;
		}
		return presion;
	}
	public final void updateAdaBoostProbability(Map<String, Double> beltaMap) {
		DbFactory.getDbChannel().insertOrUpdateCommand(
				"update t_knowledge_mathematics_word_probability set adaboost_pro=? where wordkid=?", 2, 1, beltaMap);
	}

	public final Map<String, Double> getBeltaMap() {
		return DbFactory.getDbChannel()
				.getMapResult(SystemConf.getValueByCode("selectKnowledgeWordAdaBoostProbabilitySql"));
	}

	public final Map<String, Double> getConditionMap() {
		return DbFactory.getDbChannel().getMapResult(SystemConf.getValueByCode("selectKnowledgeWordProbabilitySql"));
	}

	public final List<String> getLabelKeys() {
		return DbFactory.getDbChannel().getAllValidTrainKnowledges();
	}

	public final Map<String, Double> getLabelProMap() {
		return DbFactory.getDbChannel().getMapResult(SystemConf.getValueByCode("selectKnowledgeProbabilitySql"));
	}

	public final Map<String, String> getLabelNameMap() {
		return DbFactory.getDbChannel().getAllKnowledgeNames();
	}
}