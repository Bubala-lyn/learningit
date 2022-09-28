package adaboost;

import org.junit.Test;

import bayes.NBKFoldKnowledgeEngine;
import system.SystemConf;

public class NBClientTest {
	@Test
	public void testForKnowledge() {
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		NBKFoldKnowledgeEngine kfk = new NBKFoldKnowledgeEngine(3);
		kfk.predict();
		kfk = new NBKFoldKnowledgeEngine(5);
		kfk.predict();
	}
}
