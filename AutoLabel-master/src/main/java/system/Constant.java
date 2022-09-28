package system;
public interface Constant {
	final static int MAXTHREADS = 0x2;
	final static long min = 1000 * 60;
	final static long hour = 1000 * 60 * 60;
	final static int[] step = new int[] { 0, 10, 14, 18, 23, 29, 37, 47, 59, 74, 93, 117, 10000 };
	
	final static String[] KEY1 = { "questionId", "content" };
	final static String[] KEY2 = { "itemId", "content" };
	final static String[] KEY3 = { "questionId", "after_filter_content" };
	
	final static String T_SOURCE_CONTENT = SystemConf.getValueByCode("sourceContentTable");
	final static String T_SOURCE_ITEM = SystemConf.getValueByCode("sourceItemTable");
	final static String T_TRAIN_CONTENT = SystemConf.getValueByCode("trainContentTable");
	final static String T_TRAIN_ITEM = SystemConf.getValueByCode("trainItemTable");
	final static String T_TEST_CONTENT = SystemConf.getValueByCode("testContentTable");
	final static String T_TEST_ITEM = SystemConf.getValueByCode("testItemTable");
	
	final static String T_TRAIN_CONTENTCOUNT = SystemConf.getValueByCode("trainContentCountTable");
	final static String T_TRAIN_WORD = SystemConf.getValueByCode("trainWordTable");
	
	final static String T_TEST_WORD = SystemConf.getValueByCode("testWordTable");
	
	
	final static String T_CONTENTSEG = SystemConf.getValueByCode("sourceContentSegTable");
	final static String T_ITEMSEG = SystemConf.getValueByCode("sourceItemSegTable");
	
	final static String T_TRAIN_CONTENTSEG = SystemConf.getValueByCode("trainContentSegTable");
	final static String T_TRAIN_ITEMSEG = SystemConf.getValueByCode("trainItemSegTable");

	final static String T_TRAIN_RESULT = SystemConf.getValueByCode("trainResultTable");
	
	final static String T_KNOWLEDGEPROBABILITY = SystemConf.getValueByCode("knowledgeProbabilityTable");
	final static String T_KNOWLEDGEWORDPROBABILITY = SystemConf.getValueByCode("knowledgeWordProbability");
	final static String T_KNOWLEDGEWEIGHT = SystemConf.getValueByCode("knowledgeWeightTable");

}
