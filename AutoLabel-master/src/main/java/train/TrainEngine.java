package train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dao.DbChannel;
import dao.DbFactory;
import po.Result;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import system.Context;
import system.SystemConf;

public class TrainEngine {

	int belta = 30;
	SegmentationService service;
	Map<String, Double> proMap = null;
	Map<String, Double> modiproMap = null;
	List<String> kids = null;
	Map<String, Double> kpmap = null;
	DbChannel dbChannel;

	public TrainEngine() {
		this(SegmentType.THULAC);
	}

	public TrainEngine(SegmentType type) {
		dbChannel = DbFactory.getDbChannel();
		this.service = SegmentationFactory.createSegmentation(type);
		proMap = dbChannel.getMapResult(SystemConf.getValueByCode("selectKnowledgeWordProbabilitySql"));
		modiproMap = dbChannel.getMapResult(SystemConf.getValueByCode("selectKnowledgeWordModProbabilitySql"));
		kpmap = dbChannel.getMapResult(SystemConf.getValueByCode("selectKnowledgeProbabilitySql"));
		kids = dbChannel.getAllValidKnowledges();
	}

	public List<Result> execute(int questionId, String rawContent) {
		String strs[] = null;
		List<Result> rtns = null;
		List<Result> results = new ArrayList<Result>();
		String content = service.segment(rawContent);

		printf(content, false);

		// 将max(tfidf)/tfidf值超过30的全部去掉
		double maxTFIDF = dbChannel.getMaxTFIDFByWords(content);
		String str = dbChannel.filterWords(maxTFIDF, content, belta);

		printf(str, true);

		if (str == null || str.length() == 0)
			return null;

		strs = str.split(",");

		// double weight[] = dbChannel.getTfidfWeight(strs);
		boolean isKeyWord[] = new boolean[strs.length];

		int i;
		// List<String> kids = SystemConfiguration.getAllKids();
		for (String kid : kids) {
			if (!kpmap.containsKey(kid))
				continue;
			Result r = new Result();
			r.setQuestionId(questionId);
			r.setRawContent(rawContent);
			r.setSegContent(content);
			r.setKnowledgeId(kid);
			double d = Math.exp(kpmap.get(kid));
			// double d2 = 0;
			for (i = 0; i < strs.length; i++) {
				if (proMap.containsKey(kid + "_" + strs[i])) {
					if (Context.getKeyWords().contains(strs[i]))
						isKeyWord[i] = true;
					d += Math.exp(proMap.get(kid + "_" + strs[i]));
				}
			}
			r.setValue(d);
			results.add(r);
		}
		if (results.size() < 1)
			return null;
		TreeMap<String, Result> treeMap = new TreeMap<String, Result>();
		Map<String, String> kNames = dbChannel.getKnowledgeNameByKnowledgeIds(results);
		if (kNames != null) {
			for (Result rr : results) {
				rr.setKnowledgeName(kNames.get(rr.getKnowledgeId()));
				treeMap.put(rr.getKnowledgeName(), rr);
			}
			List<Map.Entry<String, Result>> list = new ArrayList<Map.Entry<String, Result>>(treeMap.entrySet());
			// 降序
			Collections.sort(list, new Comparator<Map.Entry<String, Result>>() {
				@Override
				public int compare(Map.Entry<String, Result> o1, Map.Entry<String, Result> o2) {
					return o1.getValue().getValue() - o2.getValue().getValue() >= 0 ? -1 : 1;
				}
			});
			rtns = new ArrayList<Result>(list.size());
			for (i = 0; i < list.size(); i++) {
				Map.Entry<String, Result> mapping = list.get(i);
				Result r = mapping.getValue();
				rtns.add(r);
			}
		} else {
			rtns = null;
		}
		return rtns;
	}

	private void printf(String content, boolean flag) {
		String[] strs;
		if (!flag)
			strs = content.split(" ");
		else
			strs = content.split(",");
		String seg = "";
		for (int i = 0; i < strs.length; i++)
			seg += "'" + strs[i] + "',";
		seg = seg.substring(0, seg.length() - 1);
		if (!flag)
			System.out.println("过滤前分词结果->" + seg);
		else
			System.out.println("过滤后分词结果->" + seg);
	}

	public List<Result> execute(int questionId, String content, double weight[]) {
		List<Result> rtns = null;
		List<Result> results = new ArrayList<Result>();
		String[] strs = content.split(",");
		int i;
		boolean isKeyWord[] = new boolean[strs.length];
		for (i = 0; i < strs.length; i++) {
			if (Context.getKeyWords().contains(strs[i])) {
				isKeyWord[i] = true;
			}
		}

		// List<String> kids = SystemConfiguration.getAllKids();
		for (String kid : kids) {
			if (!kpmap.containsKey(kid))
				continue;
			Result r = new Result();
			r.setQuestionId(questionId);
			r.setSegContent(content);
			r.setKnowledgeId(kid);
			double d = Math.exp(kpmap.get(kid));
			for (i = 0; i < strs.length; i++) {
				if (proMap.containsKey(kid + "_" + strs[i])) {
					d += Math.exp(proMap.get(kid + "_" + strs[i]));
				}
			}
			r.setValue(d);
			results.add(r);
		}
		if (results.size() < 1)
			return null;
		TreeMap<String, Result> treeMap = new TreeMap<String, Result>();
		Map<String, String> kNames = dbChannel.getKnowledgeNameByKnowledgeIds(results);
		if (kNames != null) {
			for (Result rr : results) {
				rr.setKnowledgeName(kNames.get(rr.getKnowledgeId()));
				treeMap.put(rr.getKnowledgeName(), rr);
			}
			List<Map.Entry<String, Result>> list = new ArrayList<Map.Entry<String, Result>>(treeMap.entrySet());
			// 降序
			Collections.sort(list, new Comparator<Map.Entry<String, Result>>() {
				@Override
				public int compare(Map.Entry<String, Result> o1, Map.Entry<String, Result> o2) {
					return o1.getValue().getValue() - o2.getValue().getValue() >= 0 ? -1 : 1;
				}
			});
			rtns = new ArrayList<Result>(list.size());
			for (i = 0; i < list.size(); i++) {
				Map.Entry<String, Result> mapping = list.get(i);
				Result r = mapping.getValue();
				rtns.add(r);
			}
		} else {
			rtns = null;
		}
		return rtns;
	}
}