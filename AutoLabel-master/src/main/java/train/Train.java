package train;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.DbChannel;
import dao.DbFactory;
import executor.TextSegmentation;
import segment.SegmentType;
import segment.SegmentationFactory;
import segment.SegmentationService;
import system.Constant;
import system.Context;
import system.SystemConf;
import utils.CommonUtils;

public class Train {
	static DbChannel dbChannel = DbFactory.getDbChannel();
	static final DecimalFormat df = new DecimalFormat("######0.00000000");
	static HashMap<Integer, String> wordSet;
	int questionCount;
	SegmentType segmentType;
	SegmentationService segmentService;

	public Train() {
		this(SegmentType.THULAC);
	}

	/**
	 * 分词工具
	 * 
	 * @param segmentType
	 */
	public Train(SegmentType segmentType) {
		this.segmentType = segmentType;
		this.segmentService = SegmentationFactory.createSegmentation(segmentType);
	}

	public Train(SegmentType segmentType, SegmentationService segmentService) {
		this.segmentType = segmentType;
		this.segmentService = segmentService;
	}

	public Train(SegmentationService segmentService) {
		this(SegmentType.OTHERS);
		this.segmentService = segmentService;
	}

	public void start() {
		// 分词存库
		segment();
		// 计算概率存库
		updateAndCompute();
	}

	/**
	 * 分词可以单独执行
	 */
	public void segment() {
		// 清空相关缓存
		cleanAndInit();
		System.out.println("segment start...");
		// 由于每个线程占据内存较多，所以线程开启数量由不同机器决定，所以线程数量由命令行决定
		ExecutorService exe = Executors.newFixedThreadPool(Constant.MAXTHREADS);
		int j;
		int total = dbChannel.getAllRecords(Constant.T_SOURCE_CONTENT);
		// 每个线程分配固定数量的题目进行匹配,分页处理
		int pageSize = Context.getPageSize(); // 1000
		int totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		for (j = 0; j < totalPage; j++) {
			// 采用某种分词工具（如结巴）
			// 对题目内容先查询，再分词，然后插入题目分词结果表中
			exe.execute(new TextSegmentation(segmentService, Constant.T_SOURCE_CONTENT, Constant.KEY1,
					SystemConf.getValueByCode("selectContentSql"), SystemConf.getValueByCode("insertSegContentSql"),
					j * pageSize, pageSize));
			Context.sleep(3000);
		}
		// 题目选项处理？
		total = dbChannel.getAllRecords(Constant.T_SOURCE_ITEM);
		totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		for (j = 0; j < totalPage; j++) {
			// 和上面对 题目内容 的处理是一样的
			exe.execute(new TextSegmentation(segmentService, Constant.T_SOURCE_ITEM, Constant.KEY2,
					SystemConf.getValueByCode("selectItemSql"), SystemConf.getValueByCode("insertSegItemSql"),
					j * pageSize, pageSize));
			Context.sleep(3000);
		}
		exe.shutdown();
		// 判断线程是否运行结束
		while (!exe.isTerminated())
			Context.sleep(3000);
 		// 更新题目选项分词结果表的questionId
		dbChannel.update(SystemConf.getValueByCode("updateSegItemSql"));
		// 更新题目内容分词结果表的length
		dbChannel.update(SystemConf.getValueByCode("updateSegContentSql"));

		System.out.println("segment end...");
	}

	/**
	 * 计算更新概率值
	 */
	public void updateAndCompute() {
		System.out.println("compute probability start...");
		// 和 segment 一样，先清除数据库缓存
		dbChannel.truncate(Constant.T_TRAIN_CONTENTCOUNT);// 分词结果合并
		dbChannel.truncate(Constant.T_TRAIN_WORD);// 完整的分词后结果
		dbChannel.truncate(Constant.T_KNOWLEDGEPROBABILITY); // 知识点概率
		dbChannel.truncate(Constant.T_KNOWLEDGEWORDPROBABILITY);// 分词|知识点条件概率
		// 获取训练集题目的数量
		int total = dbChannel.getAllRecords(Constant.T_TRAIN_CONTENT);
		// 朴素贝叶斯两种计算模型：1.多元分布模型 2.伯努利模型 本文采用第二种
		// 计算p(k),知识点先验概率近似等于属于某个知识点的文档数/总的文档数,插入到表 t_knowledge_mathematics_probability
		dbChannel.insertKnowledgeProbability(SystemConf.getValueByCode("insertKnowledgeProbabilitySql"), total);
		// 更新item_thulac的questionId,将有选项的题目和无选项的题目分开，方便之后的计算
		// 更新 t_question_content_mathematics_count 表的tf、idf数据
		String[][] datas = count();
		// 计算熵，与tfidf一并更新
		// datas = calculateEntropy(datas);暂时不计算
		dbChannel.insert(SystemConf.getValueByCode("insertTrainContentCountSql"), datas);

		// 更新 t_question_mathematics_word,存放questionId,words(一个题目的完整词组)
		dbChannel.insert(SystemConf.getValueByCode("insertTrainWordSql"), wordSet);

		long s = System.currentTimeMillis();
		// 计算每个单词对于知识点的贡献 存入 t_knowledge_mathematics_word_probability 表
		dbChannel.insertOrUpdateCommand(SystemConf.getValueByCode("insertKnowledgeWordProbabilitySql"), 1, 2,
				computeProbability());
		System.out.println("computeProbability用时：" + (System.currentTimeMillis() - s));

		// s = System.currentTimeMillis();
		// 计算每个单词对于知识点的modified_pro贡献 存入 t_knowledge_word_probability表
		// dbChannel.insertOrUpdateCommand(SystemConf.getValueByCode("updateKnowledgeWordProbabilitySql"),
		// 2, 1,
		// computeModifiedProbability());
		// System.out.println("computeModifiedProbability用时：" +
		// (System.currentTimeMillis() - s));

		// 更新分词类型（清华-0、结巴-1、hanlp-2）
		// dbChannel.update(SystemConf.getValueByCode("updateSegmentTypeSql").replace("?",
		// String.valueOf(segmentType.ordinal())));

		Context.getApplicationExecutor().shutdown();

		System.out.println("compute probability end...");
	}

	private void cleanAndInit() {
		System.out.println("clear cache start...");
		// 清除缓存
		CommonUtils.clearCache();
		// 加载停用词
		CommonUtils.loadCache();
		// 清除数据库数据
		dbChannel.truncate(Constant.T_CONTENTSEG);// content分词结果
		dbChannel.truncate(Constant.T_ITEMSEG);// item分词结果
		dbChannel.truncate(Constant.T_KNOWLEDGEPROBABILITY);// 知识点分布
		System.out.println("clear cache end...");
	}

	// !在训练集上 count
	private String[][] count() {
		Map<String, Integer> count = new HashMap<>();
		HashMap<String, HashSet<String>> questionCountMap = new HashMap<>();
		wordSet = new HashMap<Integer, String>();
		ArrayList<ArrayList<String>> content_datas = dbChannel
				.getFilterContent(SystemConf.getValueByCode("selectFilterContentSql"), Constant.KEY3, 1);
		ArrayList<ArrayList<String>> item_datas = dbChannel
				.getFilterContent(SystemConf.getValueByCode("selectFilterItemSql"), Constant.KEY3, 1);
		int i;
		HashSet<String> set = null;
		for (i = 0; i < content_datas.size(); i++) {
			ArrayList<String> data = content_datas.get(i);
			for (int j = 1; j < data.size(); j++) {
				String key = data.get(j).trim();
				Integer value = count.get(key);
				if ("".equals(key))
					continue;
				if (value == null)
					count.put(key, 1);
				else
					count.put(key, ++value);

				// 统计包含词的文档数
				if (!questionCountMap.containsKey(key)) {
					set = new HashSet<String>();
					set.add(data.get(0));
					questionCountMap.put(key, set);
				} else {
					set = questionCountMap.get(key);
					set.add(data.get(0));
					questionCountMap.put(key, set);
				}
				// 将分过的词统计整合到一起key:questionId,value:分词结果，如[下列,函数,正确,是]
				if (!wordSet.containsKey(Integer.parseInt(data.get(0))))
					wordSet.put(Integer.parseInt(data.get(0)), key);
				else
					wordSet.put(Integer.parseInt(data.get(0)), wordSet.get(Integer.parseInt(data.get(0))) + "," + key);
			}
		}

		for (i = 0; i < item_datas.size(); i++) {
			ArrayList<String> data = item_datas.get(i);
			for (int j = 1; j < data.size(); j++) {
				String key = data.get(j);// word
				Integer value = count.get(key);
				if (value == null)
					count.put(key, 1);
				else
					count.put(key, ++value);

				// 统计包含词的文档数
				if (!questionCountMap.containsKey(key)) {
					set = new HashSet<String>();
					set.add(data.get(0));
					questionCountMap.put(key, set);
				} else {
					set = questionCountMap.get(key);
					set.add(data.get(0));
					questionCountMap.put(key, set);
				}

				// 同上
				if (!wordSet.containsKey(Integer.parseInt(data.get(0))))// questionId
					wordSet.put(Integer.parseInt(data.get(0)), key);
				else
					wordSet.put(Integer.parseInt(data.get(0)), wordSet.get(Integer.parseInt(data.get(0))) + "," + key);
			}
		}

		String[][] datas = new String[count.size()][8];
		try {
			i = 0;
			for (Map.Entry<String, Integer> entry : count.entrySet()) {
				datas[i][0] = entry.getKey();
				datas[i][1] = entry.getValue().toString();
				datas[i][2] = questionCountMap.get(entry.getKey()).size() + "";// 包含该词的文档总数:questionCount
				datas[i][3] = String.valueOf(questionCount);// 总文档数:allQuestions
				datas[i][4] = entry.getValue().toString();// TF
				datas[i][5] = Math.log((double) Integer.valueOf(datas[i][3]) / Integer.valueOf(datas[i][2]) + 1) + "";// idf
				datas[i][6] = df.format(Double.valueOf(datas[i][4]) * Double.valueOf(datas[i][5])).toString();// tf*idf
				datas[i][7] = "0";
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("第" + i + "道题出现异常");
		}
		count = null;// for GC
		content_datas = null;
		item_datas = null;
		questionCountMap = null;
		return datas;
	}

	// /**
	// * 计算加权信息熵 词t的信息熵计算公式：H(t)=1/logN *sum{(ftk/nt)*log(nt/ftk)}
	// * N为总的文档数,ftk表示词条t在文档k中出现的次数，nt表示词条t出现的总次数 信息熵越大越能说明该词条区分度不好
	// * 信息熵越小越代表该词条的区分度好
	// *
	// * @return
	// */
	// private String[][] calculateEntropy(String[][] datas) {
	// for (int i = 0; i < datas.length; i++) {
	// String[] data = datas[i];
	// String word = data[0];// 词条t
	// int word_sum = Integer.parseInt(data[1]);// 出现的总次数
	// double hw_sub = 0;
	// for (Map.Entry<Integer, String> entry : wordSet.entrySet()) {
	// String[] contents = entry.getValue().split(",");
	// int f_sum = 0;
	// for (String w : contents) {
	// if (w.equals(word))
	// f_sum++;
	// }
	// if (f_sum != 0)
	// hw_sub += (double) f_sum * Math.log(word_sum / f_sum) / word_sum;
	// }
	// hw_sub *= 1.0 / Math.log(questionCount);
	// data[7] = String.valueOf(hw_sub);
	// }
	// return datas;
	// }

	/**
	 * 计算每个单词对于知识点的贡献 存入 t_knowledge_mathematics_word_probability 表
	 * 采用文档型计算p(w|k),近似等于(1+某个知识点下包含词w的文档数)/(1+总的文档数) w1 w2 w3 ... wm k1 p11 p12
	 * p13 ... p1m k2 ... p2m ... ... ... kn ... pn-1m pnm
	 * 
	 * 1.加载所有知识点和分词 2.找到所有知识点对应的题目分词后的结果
	 * 3.遍历所有的知识点\分词,统计该词语在包含该知识点的题目数,用此个数去除总的题目数
	 */
	private Map<String, Double> computeProbability() {
		Map<String, Double> result = null;// key:kid_浓硫酸,value:同时出现的概率
		List<String> allKids = dbChannel.getAllValidTrainKnowledges();
		List<String> allWords = dbChannel.getAllWordsFromCount();
		Map<String, Integer> ktCountMap = dbChannel.getAllValidKnowledgeQuestionCount(); // 训练集题目数量map
		Map<String, ArrayList<String>> mapQ = dbChannel.getAllFenciFromWord();// key:knowledgeId,value:题目下的分词
		if (allKids != null && allWords != null && mapQ != null) {
			result = new HashMap<String, Double>();
			for (String kid : allKids) {
				for (String word : allWords) {
					int tmp = 0;
					ArrayList<String> fenciList = mapQ.get(kid);
					if (fenciList != null) {
						for (String fenci : fenciList)
							tmp += fenci.indexOf(word) > 0 ? 1 : 0;
					}
					result.put(kid + "_" + word, Double.valueOf(
							ktCountMap.containsKey(kid) ? df.format((double) tmp / ktCountMap.get(kid)) : "0"));
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * 计算每个单词对知识点的贡献率=出现的次数/分词的总数*知识点权重*p(s|k),即条件概率
	 * 1.遍历t_question_mysubject_word_train 2.找到每个题目对应的知识点权重
	 * 3.遍历t_knowledge_word_probability数据
	 * 
	 */
	// private Map<String, Double> computeModifiedProbability() {
	// Map<String, Double> returnMap = new HashMap<String, Double>();
	// Map<Integer, String> questionMap = dbChannel
	// .getAllQuestions(SystemConf.getValueByCode("selectQuestionsFromTrainSql"));
	// Map<Integer, List<String>> weightMap = dbChannel
	// .getAllQuestionWeightFromTrain(SystemConf.getValueByCode("selectQuestionWeightFromTrainSql"));
	// Map<String, Double> wordKnowledgeMap = dbChannel
	// .getMapResult(SystemConf.getValueByCode("selectKnowledgeWordProbabilitySql"));
	// Iterator<Integer> iter = questionMap.keySet().iterator();
	// while (iter.hasNext()) {// 遍历每个题
	// Integer qId = iter.next();// questionId
	// String content = questionMap.get(qId);
	// String[] words = content.split(",");
	// List<String> weightList = weightMap.get(qId);// knowledgeId-weight
	// Set<String> wordSet = new HashSet<String>(Arrays.asList(words));
	// Map<String, Double> wordCountMap = getWordMap(words);//
	// word-count,分词后每个词的比重
	//
	// Iterator<String> iter2 = wordSet.iterator();
	// while (iter2.hasNext()) {// 每个单词
	// String word = iter2.next();// word
	// for (int i = 0; i < weightList.size(); i += 2) {
	// String knowledge_word = weightList.get(i) + "_" + word;//
	// knowledgeId_word
	// if (wordKnowledgeMap.containsKey(knowledge_word) &&
	// wordCountMap.containsKey(word)) {
	// // 分词后每个词的比重*p(s|k)*知识点比重
	// // 此处由于不考虑每个知识点在题目中的权重，故而先不考虑
	// double d = wordCountMap.get(word) * wordKnowledgeMap.get(knowledge_word);
	// returnMap.put(knowledge_word, d);
	// }
	// }
	// }
	// }
	// return returnMap;
	// }

	public void setCount(int cnt) {
		this.questionCount = cnt;
	}

	// private Map<String, Double> getWordMap(String[] words) {
	// 	double d = (double) 1 / words.length;
	// 	Map<String, Double> map = new HashMap<String, Double>();
	// 	for (String s : words)
	// 		if (map.containsKey(s))
	// 			map.put(s, map.get(s) + d);
	// 		else
	// 			map.put(s, d);
	// 	return map;
	// }
}