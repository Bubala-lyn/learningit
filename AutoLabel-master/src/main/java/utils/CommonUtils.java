package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import system.Context;
import system.SystemConf;

public class CommonUtils {

	public static void loadCache() {
		stopWordsInit();
		punctuationWordsInit();
		keyWordsInit();
	}

	public static void clearCache() {
		Context.getStopWords().clear();
		Context.getPunctuationWords().clear();
		Context.getKeyWords().clear();
		Context.getCaches().clear();
		Context.getSortCaches().clear();
		Context.getDeleteDatas().clear();
		Context.getItems().clear();
		Context.getSynonym().clear();
	}

	public static void synonymInit() {
		String[] lines = new FileOperation("utf-8").read("HCTC.txt");
		int linenumber = 0;
		for (String line : lines) {
			if (line.charAt(7) == '=') {
				String[] keys = line.split(" ");
				if (keys.length > 2) {
					for (int i = 1; i < keys.length; i++) {
						Set<String> set = Context.getSynonym().get(keys[i]) == null ? new HashSet<>()
								: Context.getSynonym().get(keys[i]);
						set.add((linenumber++) + "");
						Context.getSynonym().put(keys[i], set);
					}
				}
			}
		}
	}

	public static void keyWordsInit() {
		BufferedReader stopreader = null;
		String tempt = null;
		try {
			stopreader = new BufferedReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("keyWords.txt")
			));
			while ((tempt = stopreader.readLine()) != null) {
				Context.getKeyWords().add(tempt);
			}
			stopreader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("关键词加载异常");
		}
	}

	public static void punctuationWordsInit() {
		BufferedReader stopreader = null;
		String tempt = null;
		try {
			stopreader = new BufferedReader(new InputStreamReader(
					CommonUtils.class.getClassLoader().getResourceAsStream("punctuation.txt")
			));
			while ((tempt = stopreader.readLine()) != null) {
				Context.getPunctuationWords().add(tempt);
			}
			stopreader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("特殊符号加载异常");
		}
	}

	public static void stopWordsInit() {
		BufferedReader stopreader = null;
		String tempt = null;
		try {
			stopreader = new BufferedReader(new InputStreamReader(
					CommonUtils.class.getClassLoader().getResourceAsStream("stopWords.txt")
			));
			while ((tempt = stopreader.readLine()) != null) {
				Context.getStopWords().add(tempt);
			}
			stopreader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("停用词加载异常");
		}
	}

	public static void itemsMapInit(ArrayList<ArrayList<String>> items_datas) {
		Map<String, ArrayList<String>> items = Context.getItems();
		for (int j = 0; j < items_datas.size(); j++) {
			ArrayList<String> item = items_datas.get(j);
			String key = item.get(0);
			if (items.get(key) == null) {
				items.put(key, item);
			} else {
				item.remove(0);
				items.get(key).addAll(item);
			}
		}
	}

	public static final String getTextOnly(String questionId, String xml) {
		SAXParserFactory saxfac = SAXParserFactory.newInstance();
		Xml2Text xto = new Xml2Text();
		try {
			InputStream in = new ByteArrayInputStream(xml.getBytes("utf-8"));
			SAXParser sax = saxfac.newSAXParser();
			sax.parse(in, xto);
		} catch (Exception e) {
			System.out.println("异常题目编号:" + questionId + "\n异常文本:" + xml);
			e.printStackTrace();
		}
		return xto.getText();
	}

	public static final String translate(String questionId, String input) {
		if (input == null || "".equals(input.trim()))
			return "";
		String text = new String();
		if (input.indexOf("<text>") != -1)
			text = getTextOnly(questionId, input);
		text = text.replace("nbsp", "").replace("amp", "").replace("&", "").replace("_", "").replace(";", "")
				.replace("-", "");
		return text;
	}

	public static final String assemble(String qid, String tmp, ArrayList<String> itemQids,
			ArrayList<String> itemContent) {
		for (int i = 0; i < itemQids.size(); i++) {
			if (itemQids.get(i).equals(qid))
				tmp += "," + translate(qid, itemContent.get(i));
		}
		return tmp;
	}

	public static final String filter(String sepRex, String datas) {// 过滤
		String str = "";
		String[] strs = datas.split(sepRex);
		strs = strs.length == 0 ? null : strs;
		if (strs != null)
			for (String s : datas.split(sepRex)) {
				String fs = filter(s.replaceAll(" +", "").replace("　　", "").trim());
				if (fs.equals("") || fs.length() == 1)
					continue;
				str += fs + sepRex;
			}
		return str;
	}

	public static final String filter(String sepRex, List<String> list) {
		String str = "";
		if (list != null && list.size() > 0) {
			for (String s : list) {
				String fs = filter(s);
				str += "".equals(fs.trim()) ? "" : fs + sepRex;
			}
		}
		return str;
	}

	private static final String filter(String s) {
		if (Context.getPunctuationWords().isEmpty())
			punctuationWordsInit();
		if (Context.getKeyWords().isEmpty())
			keyWordsInit();
		if (Context.getStopWords().isEmpty())
			stopWordsInit();
		String str = s.trim();
		// 去掉标点符号
		if (Context.getPunctuationWords() != null) {
			for (String p : Context.getPunctuationWords())
				if (str.contains(p))
					str = str.replace(p, "");
		}
		// 如果是关键词，直接返回
		boolean flag = false;// 被分错的关键词，比如盐酸遇到氢氧化钠->盐酸遇 氢氧化钠
		if (Context.getKeyWords() != null) {
			for (String k : Context.getKeyWords()) {
				if (str.equals(k))
					return str;
				if (str.contains(k))
					flag = true;
			}
		}

		// 去掉停词
		if (Context.getStopWords() != null) {
			for (String p : Context.getStopWords()) {
				if (p.equals(str))
					return "";
				if (flag)
					str = str.replace(p, "");
			}
		}
		return str.replace(" ", "");
	}

	public static Vector<String> getRaw(ArrayList<String> word, int maxLength) {
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 1; i < word.size(); i++)
			sBuilder.append(word.get(i) + " ");
		String ans = sBuilder.toString();
		Vector<String> ans_vec = new Vector<String>();
		if (ans == null)
			return ans_vec;
		if (ans.length() < maxLength) {
			ans_vec.add(ans);
		} else {
			Pattern p = Pattern.compile(".*?[。？！；;;!?]");
			Matcher m = p.matcher(ans);
			int num = 0, pos = 0;
			String tmp;
			while (m.find()) {
				tmp = m.group(0);
				if (num + tmp.length() > maxLength) {
					ans_vec.add(ans.substring(pos, pos + num));
					pos += num;
					num = tmp.length();
				} else {
					num += tmp.length();
				}
			}
			if (pos != ans.length())
				ans_vec.add(ans.substring(pos));
		}
		return ans_vec;
	}
}
