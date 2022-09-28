package adaboost;

import system.SystemConf;

// 连接 测试集数据库<testbank6> 进行预测和评估
public class PredictClientTest2 {
	public static void main(String[] args) {
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		KnowledgeEngine kfk = new KnowledgeEngine(5); // topK : 3  epoch : 5
		System.out.println("==============predict start==============");
		kfk.predict(false /* isLoose */);
		System.out.println("==============predict end==============");
	}
}
