package test;


public class Tmp implements Comparable<Tmp> {

	String key;
	String kName;
	double value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getkName() {
		return kName;
	}

	public void setkName(String kName) {
		this.kName = kName;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return key + ":" + value;
	}

	public int compareTo(Tmp o) {
		return 0;
	}
}
