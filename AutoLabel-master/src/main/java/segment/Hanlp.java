package segment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import system.SystemConf;
import utils.CommonUtils;

public class Hanlp implements SegmentationService{

	private Segment seg = HanLP.newSegment().enableCustomDictionary(true);
	static{
		BufferedReader br = null;
		try {
			String str;
			br = new BufferedReader(new FileReader(new File(SystemConf.getValueByCode("kwFile"))));
		    while((str=br.readLine())!=null){
		    	CustomDictionary.add(str);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	@Override
	public String[] segment(ArrayList<ArrayList<String>> words) throws Exception {
		Vector<String> vec = null;
		ArrayList<String> list = new ArrayList<>();
		for (int k = 0; k < words.size(); k++) {
			vec = CommonUtils.getRaw(words.get(k), maxLength);
			if (vec.size() == 0)
				break;
			list.add(segment(vec.get(0)));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	private String getString(List<Term> list) {
		StringBuffer sb = new StringBuffer("");
		for(Term t:list) sb.append(t.word+" ");
		return sb.toString().trim();
	}

	@Override
	public String segment(String text) {
		return CommonUtils.filter(" ", getString(seg.seg(text)).toString());
	}
}
