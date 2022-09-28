package executor;
import java.util.ArrayList;

import system.Context;

public class ExecutorMatch implements Runnable{
	private ArrayList<ArrayList<String>> datas;
	private int blockOneStart,blockTwoStart,maxlength;
	private String item;
	private int startIndex;
	public ExecutorMatch(ArrayList<ArrayList<String>> datas,int blockOneStart,int blockTwoStart,String item,int startIndex) {
		// TODO Auto-generated constructor stub
		this.datas = datas;
		this.blockOneStart = blockOneStart;
		this.blockTwoStart = blockTwoStart;
		this.item = item;
		this.maxlength = datas.size();
		this.startIndex = startIndex;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		StringBuilder sBuilder = new StringBuilder();
		StringBuilder sWriter =new StringBuilder();
		long startTime = System.currentTimeMillis();
		int jStart = 0;
		if(startIndex > blockTwoStart)
			jStart = startIndex % Context.getMatchstep();
		if(blockOneStart == blockTwoStart)
			jStart++;
		for(int i = 0;(i + blockOneStart)<maxlength && i < Context.getMatchstep();i++) {
			ArrayList<String> c1 = Context.getSortCaches().get(datas.get(i+blockOneStart).get(0));
			for(int j = (jStart > i  ? jStart : i + 1);(j + blockTwoStart)<maxlength && j < Context.getMatchstep();j++) {
				ArrayList<String> c2 = Context.getSortCaches().get(datas.get(j + blockTwoStart).get(0));
				if(Context.getDeleteDatas().get(c2.get(0)) != null)
					continue;
				if(item.equals("1")) {
					if(Context.getCaches().get(c1.get(0)).size() > 11 && Context.getCaches().get(c2.get(0)).size() >= (int)(Context.getCaches().get(c1.get(0)).size()*1.25))
						//当待匹配的文本长度大于匹配文本的1.25倍，则认为两个文本不会相似
						break;
				}else {
					if(c1.size() > 11 && c2.size() >(int)(c1.size() * 1.25))
						break;
				}
				
				int isContinue = whetherToRun(c1, c2);
				if(isContinue == 0) continue;
				if (isContinue == 1) {
					sBuilder.append(c1.get(0)+":"+c2.get(0)+":85"+"\n");
					Context.getDeleteDatas().put(c2.get(0),"");
					if(item.equals("1"))
						sWriter.append(Context.getCaches().get(c1.get(0))+"\n"+Context.getCaches().get(c2.get(0))+"\n\n");
					else
						sWriter.append(datas.get(i + blockOneStart)+"\n"+datas.get(j + blockTwoStart)+"\n\n");
					continue;
				}
				
				int content_result = 0;
				int item_result = 0;
//				content_result = Levenshtein.calculate(c1,c2,Context.getRate());
//				if(item.equals("1") && content_result > Context.getRate()) {
//					item_result = Levenshtein.calculate(Context.getSortCaches().get(c1.get(0)+"i"),Context.getSortCaches().get(c2.get(0)+"i"),Context.getRate());
//				}
				if(item_result != -1)
					content_result = (content_result + item_result)/2;
				//若发现有相似度大于80%的则保留下来
				if(content_result > Context.getRate()) {								
					sBuilder.append(c1.get(0)+":"+c2.get(0)+":"+content_result+"\n");
					Context.getDeleteDatas().put(c2.get(0),"");
					if(item.equals("1"))
						sWriter.append(Context.getCaches().get(c1.get(0))+"\n"+Context.getCaches().get(c2.get(0))+"\n\n");
					else
						sWriter.append(datas.get(i + blockOneStart)+"\n"+datas.get(j + blockTwoStart)+"\n\n");
				}
			}
		}
		long endTime = System.currentTimeMillis();
		if(item.equals("1"))
			System.out.println("选择题:"+blockOneStart+":"+blockTwoStart+"匹配需要:"+(endTime-startTime)/1000+"秒");
		else
			System.out.println("填空题:"+blockOneStart+":"+blockTwoStart+"匹配需要:"+(endTime-startTime)/1000+"秒");
		insertServiceAndFile(sBuilder, sWriter);
	}
	
	private void insertServiceAndFile(StringBuilder sBuilder,StringBuilder sWriter) {
		//当sBuilder长度大于1时，表明有需要写入的数据
		if(sBuilder.toString().length() >1) {							
			String insertsql =null;
			if(item.equals("0"))
				insertsql = "insert into t_question_content_biology_result(questionId1,questionId2,content_similarity) values(?,?,?)";
			else 
				insertsql = "insert into t_question_content_biology_result(questionId1,questionId2,content_similarity,item) values(?,?,?,1)";
			Context.getApplicationExecutor().execute(new BatchInsert(insertsql, sBuilder.toString().split("\n"),sWriter.toString()));
		}
	}

	private int whetherToRun(ArrayList<String> c1,ArrayList<String> c2) {
		if(c1.size() < 24)
			return 2;
		ArrayList<String> t1 = new ArrayList<>();
		ArrayList<String> t2 = new ArrayList<>();
		t1.addAll(c1);
		t2.addAll(c2);
		t1.retainAll(t2);
		if(t1.size() > (c1.size()*0.8))
			return 1;
		if(t1.size() < (c1.size()*0.5))
			return 0;
		return 3;
	}
}
