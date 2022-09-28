package test;

import segment.SegmentType; // 分词工具类型
import system.SystemConf;
import train.Train;

public class TrainTest {

	public static void main(String[] args) {
		SystemConf.loadSystemParams("autolabel.properties");
		Train t = new Train(SegmentType.JIEBA);
		t.start();
	}
}
