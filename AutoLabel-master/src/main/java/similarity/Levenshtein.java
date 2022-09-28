package similarity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import system.Context;
import utils.CommonUtils;

public class Levenshtein extends SimiliarityAlgorithm{
	private String source;
	private String target;
	public Levenshtein(){}
	public Levenshtein(String source,String target){
		this.source = source;
		this.target = target;
	}
	public double getSimiliarity() {
		List<String> str1 = Arrays.asList(source.split(" "));
		List<String> str2 = Arrays.asList(target.split(" "));
		if(str1.size() == 1 && str2.size() == 1)
			return -1;
		else {
			if(str1.size() >= str2.size()) {
				if(str1.size() >11 && str2.size() > 11 && str1.size() > str2.size()*1.25)
					return 0;
				return levenshteinDistance(str2,str1,str2.size()-1,str1.size()-1,rate);
			}
			else {
				if(str1.size() >11 && str2.size() > 11 && str2.size() > str1.size()*1.25)
					return 0;
				return levenshteinDistance(str1,str2,str1.size()-1,str2.size()-1,rate);
			}
		}
	}
	
	private double levenshteinDistance(List<String> str1,List<String> str2,int minlength,int maxlength,int rate) {//str1长度短，str2长度长
		double left_top,top,left;
		double levenshteinDistance = (double)maxlength*(100-rate)/100;
		double[] matrix_before = new double[maxlength+1];
		double[] matrix_after = new double[maxlength+1];
		boolean flag = (minlength >= 24) ;
		int line = 1;
		for (int i = 1; i <=maxlength; i++)
			matrix_before[i] = (double)i;
		for(int i=1;i<=minlength;i++) {
			if(!(line%2 == 0)) {
				matrix_after[0] = i;
				for(int j=1;j<=maxlength;j++) {
					left = matrix_after[j-1];
					top = matrix_before[j];
					left_top = matrix_before[j-1];
					double min = left > top ? top :left;
					if(min >= left_top){
						matrix_after[j] = left_top + (flag ? (str1.get(i).equals(str2.get(j)) ? 0 : 1) : getWeight(str1.get(i), str2.get(j)));
					}else{
						matrix_after[j] = min + 1;
					}
				}
				if(matrix_after[maxlength-minlength+line] > levenshteinDistance)
					return 0;
			}else {
				matrix_before[0] = i;
				for(int j=1;j<=maxlength;j++) {
					left = matrix_before[j-1];
					top = matrix_after[j];
					left_top = matrix_after[j-1];
					double min = left > top ? top :left;
					if(min >= left_top){
						matrix_before[j] = left_top + (flag ? (str1.get(i).equals(str2.get(j)) ? 0 : 1) : getWeight(str1.get(i), str2.get(j)));
					}else{
						matrix_before[j] = min + 1;
					}
				}
				if(matrix_before[maxlength-minlength+line] > levenshteinDistance)
					return 0;
			}
			line++;
		}
		if(!(line%2 == 0)) 
			levenshteinDistance = matrix_before[maxlength];
		else
			levenshteinDistance = matrix_after[maxlength];
		return 1-(double)levenshteinDistance/maxlength;
	}
	
	private static double getWeight(String str1 , String str2) {
		if(Context.getSynonym().isEmpty()) CommonUtils.synonymInit();
		Set<String> str1set = Context.getSynonym().get(str1);
		Set<String> str2set = Context.getSynonym().get(str2);
		Set<String> result = new HashSet<>();
		if(str1set ==null || str2set == null)
			return str1.equals(str2) ? 0f : 1f;
		double weight = 0f;
		if(str1set.size() > 1 && str2set.size() > 1)
			weight = 0.5f;
		else 
			weight = 0.2f;
		result.addAll(str1set);
		result.retainAll(str2set);
		if(result.size()>0)
			return weight;
		return 1f;
	}
}