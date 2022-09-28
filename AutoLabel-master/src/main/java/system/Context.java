package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Context {
	private static int pageSize = 1000;
	private static int matchstep = 4000;
	public static int rate = 80;

	private static boolean testInService = true;
	private static HashSet<String> stopWords = new HashSet<String>();
	private static HashSet<String> keyWords = new HashSet<String>();
	private static HashSet<String> punctuationWords = new HashSet<String>();
	private static Map<String, Set<String>> synonym = new HashMap<>();
	private static Map<String, ArrayList<String>> caches = new HashMap<>();
	private static Map<String, ArrayList<String>> sortCaches = new HashMap<>();
	private static Map<String, String> deleteDatas = new HashMap<>();
	private static Map<String, ArrayList<String>> items = new HashMap<>();
	private static Map<String, Integer> wordCount = new HashMap<>();
	private static ExecutorService applicationExecutor = Executors.newSingleThreadExecutor();

	public static int getPageSize() {
		return pageSize;
	}

	public static int getMatchstep() {
		return matchstep;
	}

	public static int getRate() {
		return rate;
	}

	public static boolean isTestInService() {
		return testInService;
	}

	public static Map<String, ArrayList<String>> getCaches() {
		return caches;
	}

	public static Map<String, Set<String>> getSynonym() {
		return synonym;
	}

	public static Map<String, ArrayList<String>> getItems() {
		return items;
	}

	public static Map<String, String> getDeleteDatas() {
		return deleteDatas;
	}

	public static Map<String, Integer> getWordCount() {
		return wordCount;
	}

	public static Map<String, ArrayList<String>> getSortCaches() {
		return sortCaches;
	}

	public static ExecutorService getApplicationExecutor() {
		return applicationExecutor;
	}

	public static HashSet<String> getStopWords() {
		return stopWords;
	}

	public static HashSet<String> getKeyWords() {
		return keyWords;
	}

	public static HashSet<String> getPunctuationWords() {
		return punctuationWords;
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
