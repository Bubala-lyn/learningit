package svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import system.SystemConf;

public class FileUtils {

	public static void write(int lineSize, int[][] matrix, String fileName) {
		BufferedWriter buff = null;
		try {
			checkExists(SystemConf.getValueByCode("localPath"), fileName, true);
			buff = new BufferedWriter(new FileWriter(new File(SystemConf.getValueByCode("localPath"), fileName)));
			buff.write(lineSize + "\n");
			for (int i = 0; i < matrix.length; i++) {
				int[] t = matrix[i];
				for (int j = 0; j < t.length; j++) {
					if (j != t.length - 1)
						buff.write(t[j] + "|");
					else
						buff.write(t[j] + "\n");
				}
			}
			buff.close();
			FTPUtils.storeFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void write(int[] matrix, String fileName) {
		BufferedWriter buff = null;
		try {
			checkExists(SystemConf.getValueByCode("localPath"), fileName, true);
			buff = new BufferedWriter(new FileWriter(new File(SystemConf.getValueByCode("localPath"), fileName)));
			for (int i = 0; i < matrix.length; i++) {
				buff.write(matrix[i] + "\n");
			}
			buff.close();
			FTPUtils.storeFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void write(List<Integer> testKeys, String fileName) {
		BufferedWriter buff = null;
		try {
			checkExists(SystemConf.getValueByCode("localPath"), "list-" + fileName, true);
			buff = new BufferedWriter(
					new FileWriter(new File(SystemConf.getValueByCode("localPath"), "list-" + fileName)));
			for (int i = 0; i < testKeys.size(); i++) {
				buff.write(testKeys.get(i) + "\n");
			}
			buff.close();
			FTPUtils.storeFile("list-" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void write(List<Integer> testKeys, Map<Integer, List<String>> rightDisMap, String fileName) {
		BufferedWriter buff = null;
		try {
			checkExists(SystemConf.getValueByCode("localPath"), "map-" + fileName, true);
			buff = new BufferedWriter(
					new FileWriter(new File(SystemConf.getValueByCode("localPath"), "map-" + fileName)));
			for (int i = 0; i < testKeys.size(); i++) {
				Integer key = testKeys.get(i);
				List<String> list = rightDisMap.get(key);
				String str = key + "|";
				for (int j = 0; j < list.size(); j++) {
					if (j != list.size() - 1)
						str += list.get(j) + "|";
					else
						str += list.get(j);
				}
				str += "\n";
				buff.write(str);
			}
			buff.close();
			FTPUtils.storeFile("map-" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int[] read(int lineSize, String fileName, boolean isLocalFile) {
		BufferedReader buff = null;
		String line;
		int[] res = new int[lineSize];
		int index = 0;
		try {
			//如果不在本地则从服务器拉
			if(!isLocalFile){
			    checkExists(SystemConf.getValueByCode("localPath"), fileName, false);
			    FTPUtils.retrieveFile(fileName);
			}
			buff = new BufferedReader(new FileReader(new File(SystemConf.getValueByCode("localPath"), fileName)));
			while ((line = buff.readLine()) != null) {
				res[index++] = Integer.parseInt(line);
			}
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public static List<Integer> read(String fileName) {
		BufferedReader buff = null;
		String line;
		List<Integer> testKeys = null;
		try {
			testKeys = new ArrayList<Integer>();
			buff = new BufferedReader(
					new FileReader(new File(SystemConf.getValueByCode("localPath"), "list-" + fileName)));
			while ((line = buff.readLine()) != null) {
				testKeys.add(Integer.parseInt(line));
			}
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testKeys;
	}

	public static Map<Integer, List<String>> read2(String fileName) {
		BufferedReader buff = null;
		String line;
		Map<Integer, List<String>> map = null;
		try {
			map = new HashMap<Integer, List<String>>();
			buff = new BufferedReader(
					new FileReader(new File(SystemConf.getValueByCode("localPath"), "map-" + fileName)));
			while ((line = buff.readLine()) != null) {
				List<String> list = new ArrayList<String>();
				String[] strs = line.split("\\|");
				Integer key = Integer.parseInt(strs[0]);
				for (int i = 1; i < strs.length; i++) {
					list.add(strs[i]);
				}
				map.put(key, list);
			}
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	static void checkExists(String path, String fileName, boolean autoCreate) throws IOException {
		File file = new File(path, fileName);
		if (file.exists())
			file.delete();
		if (autoCreate)
			file.createNewFile();
	}

	public static void main(String[] args) throws IOException {
		checkExists("D://", "x.txt", true);
	}
}
