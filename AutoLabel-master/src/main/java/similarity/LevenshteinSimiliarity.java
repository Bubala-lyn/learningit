package similarity;

public class LevenshteinSimiliarity extends SimiliarityAlgorithm {
	private String source;
	private String target;

	public LevenshteinSimiliarity() {
	}

	public LevenshteinSimiliarity(String source, String target) {
		this.source = source;
		this.target = target;
	}

	public double getSimiliarity() {
		int len1 = source.length();
		int len2 = target.length();

		String str1 = source;
		String str2 = target;

		int[][] dif = new int[len1 + 1][len2 + 1];

		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}

		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 取三个值中最小的
				dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1, dif[i - 1][j] + 1);
			}
		}
		// 计算相似度
		double similarity = 1 - (double) dif[len1][len2] / Math.max(str1.length(), str2.length());
		return similarity;
	}

	private static int min(int... is) {
		int min = is[0];
		for (int i : is) {
			if (min > i) {
				min = i;
			}
		}
		return min;
	}
}
