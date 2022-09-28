package executor;
import java.util.ArrayList;

import dao.DbFactory;
import system.Context;
import system.SystemConf;
import utils.CommonUtils;
import utils.FileOperation;


public class BatchInsert implements Runnable {
	private String insertsql;
	private String textData;
	private String[] datas;
	private String[][] word_datas;
	private ArrayList<ArrayList<String>> content_raw_datas;
	private int runWhich;

	
	public BatchInsert(String insertsql, String[] datas, ArrayList<ArrayList<String>> content_raw_datas) {
		this.insertsql = insertsql;
		this.datas = datas;
		this.content_raw_datas = content_raw_datas;
		this.runWhich = 0;
	}

	public BatchInsert(String insertsql, String[] datas, String textData) {
		this.insertsql = insertsql;
		this.datas = datas;
		this.textData = textData;
		this.runWhich = 1;
	}

	public BatchInsert(String insertsql, String[][] word_datas) {
		this.insertsql = insertsql;
		this.word_datas = word_datas;
		this.runWhich = 2;
	}

	@Override
	public void run() {
		switch (runWhich) {
		case 0:
			runTextSegmentation();
			break;
		case 1:
			runMatch();
			break;
		case 2:
			runCount();
			break;
		default:
			break;
		}
	}

	private void runMatch() {
		int i = 0, j = 0;
		String[][] insert_datas = new String[datas.length][3];
		for (String result : datas) {
			String[] items = result.split(":");
			for (String item : items) {
				insert_datas[i][j++] = item;
			}
			j = 0;
			i++;
		}
		new FileOperation().write(SystemConf.getValueByCode("result"), textData);
		if (Context.isTestInService())
			DbFactory.getDbChannel().insert(insertsql, insert_datas);
	}

	private void  runTextSegmentation() {
		//itemId,seg_content,length,raw_content,after_filter_content
		//questionId,seg_content,length,raw_content,after_filter_content
		String[][] insert_datas = new String[datas.length][5];//加了一列原始文本，原来是3，长度加一
		for (int i = 0; i < datas.length; i++) {
			String filterStr = CommonUtils.filter(" ",datas[i]);
			insert_datas[i][0] = content_raw_datas.get(i).get(0);
			insert_datas[i][1] = datas[i];
			insert_datas[i][2] = filterStr.trim().split(" ").length + "";
			insert_datas[i][3] = injectRawContent(content_raw_datas.get(i));
			insert_datas[i][4] = filterStr;
		}
		DbFactory.getDbChannel().insert(insertsql, insert_datas);
	}

	private void runCount() {
		DbFactory.getDbChannel().insert(insertsql, word_datas);
	}
	
	private String injectRawContent(ArrayList<String> arrayList) {
		String raw = "";
		if(arrayList != null){
			for(int i=1;i<arrayList.size();i++)
				raw+=arrayList.get(i);
		}
		return raw;
	}
}
