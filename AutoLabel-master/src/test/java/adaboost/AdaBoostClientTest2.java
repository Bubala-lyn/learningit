package adaboost;

import org.junit.Test;

import segment.SegmentType;
import system.SystemConf;

// K折训练
public class AdaBoostClientTest2 {
	@Test
	public void testForKnowledge2() {
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		KFoldKnowledgeEngine kfk = new KFoldKnowledgeEngine(10, 5, true /* needSegment */); // epoch : 5 topK : 3
		System.out.println("==============train start==============");
		kfk.train();
		System.out.println("==============train end==============");
	}
}
