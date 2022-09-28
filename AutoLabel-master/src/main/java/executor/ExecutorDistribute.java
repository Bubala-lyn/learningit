package executor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.DbFactory;
import system.Context;


public class ExecutorDistribute implements Runnable{
	private String table,item;
	private int minlength,maxlength,startlength;

	public ExecutorDistribute(String table,String item,int minlength,int maxlength,int startlength) {
		this.item = item;
		this.table = table;
		this.startlength = startlength;
		this.minlength = minlength;
		this.maxlength = maxlength;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String content_sql = "select questionId,thulac_content"
				+ " from " + table 
				+ " where item =" + item
				+ " and length between " + minlength + " and " + maxlength
				+ " order by length";
		String[] args = {"questionId","thulac_content"};
		ArrayList<ArrayList<String>> datas = DbFactory.getDbChannel().getFilterContent(content_sql,args,1);
		ExecutorService exe = Executors.newFixedThreadPool(10);	//程序为每个匹配都分配固定大小的线程
		
		int startIndex = -1;
		for(int i = 0;i < datas.size();i++) {
			if(startIndex == -1 && datas.get(i).size() > startlength)
				startIndex = i;
			ArrayList<String> value;
			String key = datas.get(i).get(0);
			if(item.equals("1") && Context.getCaches().get(key) == null) {
				ArrayList<String> ivalue = Context.getItems().get(datas.get(i).get(0));
				value = combine(datas.get(i), ivalue);
				Context.getCaches().put(key,value);
				putSortWord(key, datas.get(i));
				try {
					putSortWord(key+"i", Context.getItems().get(key));
				} catch (Exception e) {
					ArrayList<String> temp =new ArrayList<>();
					temp.add(key);
					Context.getSortCaches().put(key+"i", temp);
				}
			}else if (item.equals("0") && Context.getSortCaches().get(key) == null) {
				putSortWord(key, datas.get(i));
			}
		}
		
		if(item.equals("1"))
			System.out.println(minlength+"到"+maxlength+"长度的选择题开始匹配");
		else
			System.out.println(minlength+"到"+maxlength+"长度的填空题开始匹配");
		
		for (int i = 0; i < datas.size(); i+=Context.getMatchstep())
			for(int j = i; j < datas.size(); j+=Context.getMatchstep())
				if(startIndex <= (j + Context.getMatchstep()))
					exe.execute(new ExecutorMatch(datas,i,j,item,startIndex));
		exe.shutdown();
		while(!exe.isTerminated())
			Context.sleep(1000);
	}

	private ArrayList<String> combine(ArrayList<String> strList1,ArrayList<String> strList2) {
		if(strList2 == null)
			return strList1;
		ArrayList<String> sum = new ArrayList<>(strList2.size()-1);
		ArrayList<String> List2 = new ArrayList<>();
		List2.addAll(strList2);
		List2.remove(0);
		sum.addAll(strList1);
		sum.addAll(List2);
		return sum;
	}
	
	private void putSortWord(String key,ArrayList<String> unSortValue) {
		ArrayList<String> value =new ArrayList<>();
		value.addAll(unSortValue);
		Collections.sort(value,new Comparator<String>() {
			@Override
			public int compare(String obj1, String obj2) {
				if(obj1.equals(value.get(0)) || obj2.equals(value.get(0)))
					return 0;
				return Context.getWordCount().get(obj1) - Context.getWordCount().get(obj2);
			}
		});
		Context.getSortCaches().put(key, value);
	}
}
