package adaboost;

import org.junit.Test;

import segment.SegmentType;
import system.SystemConf;

public class AdaBoostClientTest {
	@Test
	public void testForKnowledge() {
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		KnowledgeEngine kfk = new KnowledgeEngine(5, 3); // topK : 3  epoch : 5
		System.out.println("==============train start==============");
		kfk.train();
		System.out.println("==============train end==============");
		System.out.println("==============predict start==============");
		// ! false - 使用和训练集一样的补偿精确度，不进行额外的补偿
		// ! true - 使用更高的补偿：同一章+0.5，同一节+0.8
		kfk.predict(false);
		System.out.println("==============predict end==============");
		// kfk = new KnowledgeEngine(5);
		// kfk.predict();
	}

	// @Test
	// public void testWeight() {
	// 	System.out.println("==============AdaBoostClientTest.testWeight start==============");
	// 	if (!SystemConf.hasLoaded())
	// 		SystemConf.loadSystemParams("autolabel.properties");
	// 	KFoldWeightEngine kfwe = new KFoldWeightEngine(10, 1, 5, SegmentType.JIEBA, true);
	// 	System.out.println("==============train==============");
	// 	kfwe.train();
	// 	System.out.println("==============batchPredict==============");
	// 	kfwe.batchPredict();
	// 	System.out.println("==============AdaBoostClientTest.testWeight end==============");
	// }
}
