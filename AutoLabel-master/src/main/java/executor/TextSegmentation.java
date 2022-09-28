package executor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.DbFactory;
import segment.SegmentationService;

/**
 * 分词
 * 
 * @author Galois
 *
 */
public class TextSegmentation implements Runnable {
	private String querysql;
	private String insertsql;
	private String beginThulac;
	private String args[];
	private SegmentationService segService;

	public TextSegmentation(SegmentationService segService, String table, String args[], String querysql,
			String insertsql, int startindex, int endindex) {
		this.segService = segService;
		this.querysql = querysql + startindex + " , " + endindex;
		this.insertsql = insertsql;
		this.args = args;
		beginThulac = "表: " + table + "[" + startindex + "," + (startindex + endindex) + "]之间的数据开始分词……";
	}

	@Override
	public void run() {
		System.out.println(beginThulac);
		String datas[] = null;
		ArrayList<ArrayList<String>> content_raw_datas = DbFactory.getDbChannel().getFilterContent(querysql, args, 0);
		try {
			datas = segService.segment(content_raw_datas); // 使用分词工具进行分词
			ExecutorService exe = Executors.newSingleThreadExecutor();
			exe.execute(new BatchInsert(insertsql, datas, content_raw_datas)); // 原始数据和分词结果入库
			exe.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
