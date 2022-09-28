package segment;

public class SegmentationFactory {

	public static final SegmentationService createSegmentation(SegmentType segmentType) {
		if (segmentType == SegmentType.THULAC) {
			return new Thulac(false, true);
		} else if (segmentType == SegmentType.JIEBA) {
			return new Jieba();
		} else if (segmentType == SegmentType.HANLP) {
			return new Hanlp();
		}
		return new Hanlp();
	}

}
