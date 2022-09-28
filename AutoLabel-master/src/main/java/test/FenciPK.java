package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;

import segment.Hanlp;
import segment.Thulac;
import system.SystemConf;
import utils.CommonUtils;

public class FenciPK {

	static SqlDao dao = new SqlDao();
	static{
		SystemConf.loadSystemParams("autolabel.properties");
	}
	@Test
	public void testThulac() throws IOException{
		String raw = dao.getTextByQid(true, 9278144);
		System.out.println(raw);
		String str = new Thulac(false,true).segment(raw.replace("、", ",").replace("．", ","));
		System.out.println(str);
		str = CommonUtils.filter(" ", str);
		System.out.println(str);
	}
	
	@Test
	public void testJieba(){
		CommonUtils.loadCache();
		JiebaSegmenter segmenter = new JiebaSegmenter();
		String text = dao.getTextByQid(true, 10323061);
		WordDictionary wd = WordDictionary.getInstance();
		wd.loadUserDict(Paths.get(SystemConf.getValueByCode("kwFile")));
		text = CommonUtils.filter(" ", segmenter.sentenceProcess(text));
		System.out.println(text);
	}
	
	@Test
	public void testHanlp(){
		SystemConf.loadSystemParams("autolabel.properties");
		Hanlp h = new Hanlp();
		String text = getTextByQid(9434564);
		String s = h.segment(text);
		System.out.println(s);
	}
	

	@Test
	public void parseKeyWords() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(SystemConf.getValueByCode("kwFile"))));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("new.txt")));
		String str;
		while ((str = br.readLine()) != null) {
			String[] newStr = str.split("\\|");
			for (String s : newStr)
				bw.write(s + "\n");
		}
		br.close();
		bw.close();
	}
	
	public String getTextByQid(int questionId) {
		String contentSql,itemSql;
		contentSql = "select questionId,content from t_question_content_mysubject where questionId=" + questionId;
		itemSql = "select questionId,content from t_question_item_mysubject where questionId=" + questionId;
		return getTextBySql(contentSql, itemSql);
	}
	
	private String getTextBySql(String contentSql, String itemSql) {
		ArrayList<ArrayList<String>> all = dao.getContentsAndQuestionIdsList(contentSql);
		ArrayList<String> qids = all.get(0);
		ArrayList<ArrayList<String>> allItems = dao.getContentsAndQuestionIdsList(itemSql);
		ArrayList<String> itemQids = allItems.get(0);
		ArrayList<String> content = all.get(1);
		ArrayList<String> itemContent = allItems.get(1);
		String tmp = "";
		for (int i = 0; i < qids.size(); i++) {
			tmp = CommonUtils.translate(qids.get(i), content.get(i));
			tmp = CommonUtils.assemble(qids.get(i), tmp, itemQids, itemContent);
		}
		System.out.println("原始文本:" + tmp);
		return tmp;
	}
	
	public static void main(String[] args) throws IOException {
		Hanlp h = new Hanlp();
		Thulac t = new Thulac(false, true);
		JiebaSegmenter segmenter = new JiebaSegmenter();
		WordDictionary wd = WordDictionary.getInstance();
		wd.loadUserDict(Paths.get(SystemConf.getValueByCode("kwFile")));
		String str = "相比之下，端对端的有监督模型在最近几年里越来越受到人们的关注";
		//System.out.println(ToAnalysis.parse(str));
		System.out.println(segmenter.sentenceProcess(str));
		System.out.println(h.segment(str));
		System.out.println(t.segment(str));
	}
}
