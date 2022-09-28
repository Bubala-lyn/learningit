package segment;

import java.util.ArrayList;

import system.SystemConf;
import test.SqlDao;
import utils.CommonUtils;

public class Length {

	public static void main(String[] args) {
		SystemConf.loadSystemParams("autolabel.properties");
		SqlDao dao = new SqlDao();
		String sql = "select questionId,content from t_question_content_geography where questionId>2676389 and questionId<2677389";
		String itemSql = "select questionId,content from t_question_item_geography where questionId in(select questionId from t_question_content_geography where questionId>2676389 and questionId<2677389)";
		ArrayList<ArrayList<String>> all = dao.getContentsAndQuestionIdsList(sql);
		ArrayList<ArrayList<String>> allItems = dao.getContentsAndQuestionIdsList(itemSql);
		ArrayList<String> qids = all.get(0);
		ArrayList<String> itemQids = allItems.get(0);
		ArrayList<String> content = all.get(1);
		ArrayList<String> itemContent = allItems.get(1);
		String lenStr = "";
		for (int i = 0; i < qids.size(); i++) {
			String tmp = CommonUtils.translate(qids.get(i), content.get(i));
			tmp = CommonUtils.assemble(qids.get(i), tmp, itemQids, itemContent);
			lenStr+=tmp;
		}
		System.out.println(lenStr.length()/qids.size());
	}
}
