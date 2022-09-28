package segment;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;

import system.SystemConf;
import utils.CommonUtils;


/**
 * 没有词性标注和关键字提取
 * @author Galois
 *
 */
public class Jieba implements SegmentationService{

	JiebaSegmenter segmenter = new JiebaSegmenter();
    
    public Jieba(){
    	 WordDictionary wd = WordDictionary.getInstance();
 		//  wd.loadUserDict(Paths.get(SystemConf.getValueByCode("kwFile")));
    }
    
	public String[] segment(ArrayList<ArrayList<String>> words){
		ArrayList<String> list = new ArrayList<>();
		Vector<String> vec = null;
		for (int k = 0; k < words.size(); k++) {
			vec = CommonUtils.getRaw(words.get(k), maxLength);
			if (vec.size() == 0) break;
			list.add(getString(segmenter.sentenceProcess(vec.get(0))));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	private String getString(List<String> list) {
		StringBuffer sb = new StringBuffer("");
		for(String str:list) sb.append(str+" ");
		return sb.toString().trim();
	}

	@Override
	public String segment(String text) {
		return CommonUtils.filter(" ", getString(segmenter.sentenceProcess(text)));
	}
}
