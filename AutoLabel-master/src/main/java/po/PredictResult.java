package po;

public class PredictResult implements Comparable<PredictResult> {
	public double postProbability;
	public Integer sampleKey;
	public String labelKey;
	public String labelName;
	public String rawContent;
	public int weight;

	@Override
	public int compareTo(PredictResult o) {
		double d = this.postProbability - o.postProbability;
		if (d < 0)
			return 1;
		else if (d > 0)
			return -1;
		else
			return 0;
	}
}
