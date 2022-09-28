package adaboost;

import java.util.List;
import java.util.ArrayList;

import utils.ReadJSON;

// 对外部输入的试题列表进行知识点预测
public class PredictClientTest {
	public static void main(String[] args) {
		KFoldKnowledgeEngine kfk = new KFoldKnowledgeEngine(5); // topK : 5
		System.out.println("==============Knowledge infer start==============");
		List<String> contents = ReadJSON.contents();
		List<String> result = kfk.inferByInput(contents);
		System.out.println("预测的结果：");
		System.out.println(result);
		System.out.println("==============Knowledge infer end==============");
	}
}
