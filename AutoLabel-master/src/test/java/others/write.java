package others;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class write {

	public static void main(String[] args) {
		String filename="D://test.txt";
		int[][] matrix ={{5,6,7},{8,9,10}};
		write(filename,matrix);
	}
	
	private  static void write(String filename, int[][] matrix) {
		BufferedWriter buff = null;
		try {
			buff = new BufferedWriter(new FileWriter(new File(filename)));
			buff.write(10 + "\n");
			for (int i = 0; i < matrix.length; i++) {
				int[] t = matrix[i];
				for (int j = 0; j < t.length; j++) {
					if (j != t.length - 1)
						buff.write(t[j] + "\t");
					else
						buff.write(t[j] + "\n");
				}
			}
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
