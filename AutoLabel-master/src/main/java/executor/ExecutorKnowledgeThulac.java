package executor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.DbFactory;
import segment.Thulac;

public class ExecutorKnowledgeThulac implements Runnable {
	private String querysql;
	private String insertsql;
	private String beginThulac;

	public ExecutorKnowledgeThulac(String table, int startindex, int endindex) {
		querysql = "select distinct knowledgeId,knowledgeName from " + table + " where knowledgeId in(select knowledgeId from paper_knowledge_question "
				 + "where questionId in(select questionId from  paper_question_content_mathematics))order by knowledgeId and status=1 limit " + startindex + " , "
				 + endindex;
		beginThulac = "表: "+table+"["+ startindex + "," + (startindex+endindex) + "]之间的数据开始分词……";
		insertsql = "insert into " + table + "_thulac (knowledgeId,thulac_content,length) values (?,?,?)";
	}

	@Override
	public void run() {
		System.out.println(beginThulac);
		String[] args = { "knowledgeId", "knowledgeName" };
		ArrayList<ArrayList<String>> content_raw_datas = DbFactory.getDbChannel().getFilterContent(querysql, args, 0);
		try {
			String[] thulac_datas = new Thulac(false,false).segment(content_raw_datas);
			ExecutorService exe = Executors.newSingleThreadExecutor();
			exe.execute(new BatchInsert(insertsql, thulac_datas, content_raw_datas));
			exe.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
