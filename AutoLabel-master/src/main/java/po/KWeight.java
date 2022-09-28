package po;

public class KWeight implements Comparable<KWeight>{
	public int realProbability;
	public String realKey;
	
	@Override
	public int compareTo(KWeight o) {
		int d = this.realProbability - o.realProbability;
		if (d < 0)
			return 1;
		else if (d > 0)
			return -1;
		else
			return 0;
	}
}