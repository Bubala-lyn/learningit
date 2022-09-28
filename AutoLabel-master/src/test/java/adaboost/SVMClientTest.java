package adaboost;

import org.junit.Test;

import svm.SVMKFoldKnowledgeEngine;
import system.SystemConf;

public class SVMClientTest {

	final static String[] fileName = { "1", "2", "3", "4", "5" };

	@Test
	public void writeData() {
		System.out.println("########################Data Started##########################");
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		SVMKFoldKnowledgeEngine kfk = new SVMKFoldKnowledgeEngine();
		kfk.flushToFile(fileName[3]);
		System.out.println("########################Data Ended##########################");
	}

	// @Test
	// public void testPresion() {
	// System.out.println("########################Testing
	// Started##########################");
	// if (!SystemConf.hasLoaded())
	// SystemConf.loadSystemParams("autolabel.properties");
	// double p = SVMKnowledgeEngine.predictPresion(FileUtils.read(fileName[6] +
	// ".txt"),
	// FileUtils.read2(fileName[6] + ".txt"), fileName[6], true);
	// System.out.println("第" + fileName[6] + "个结果->" + p);
	// System.out.println("########################Testing
	// Ended##########################");
	// }
}
