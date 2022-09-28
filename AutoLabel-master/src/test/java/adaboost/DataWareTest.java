package adaboost;

import org.junit.Test;

import segment.SegmentType;
import system.DataWare;
import system.SystemConf;

public class DataWareTest {

	@Test
	public void testData() {
		if (!SystemConf.hasLoaded())
			SystemConf.loadSystemParams("autolabel.properties");
		DataWare dw = new DataWare(10, SegmentType.JIEBA);
		dw.prepareData();
		dw.initData();
	}
}
