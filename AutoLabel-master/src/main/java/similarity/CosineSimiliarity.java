package similarity;

import java.util.Map;

public class CosineSimiliarity extends SimiliarityAlgorithm{
	private String source;
	private String target;
	private Map<String,Double> m1;
	private Map<String,Double> m2;
	public CosineSimiliarity(){}
	public CosineSimiliarity(String source,String target,Map<String,Double> m1,Map<String,Double> m2){
		this.source = source;
		this.target = target;
		this.m1 = m1;
		this.m2 = m2;
	}
	/** 
     * 计算测试样本向量和训练样本向量的相识度 
     * sim(D1,D2)=(D1*D2)/(|D1|*|D2|) 
     * 例：D1(a 30;b 20;c 20;d 10) D2(a 40;c 30;d 20; e 10) 
     * D1*D2 = 30*40 + 20*0 + 20*30 + 10*20 + 0*10 = 2000 
     * |D1| = sqrt(30*30+20*20+20*20+10*10) = sqrt(1800) 
     * |D2| = sqrt(40*40+30*30+20*20+10*10) = sqrt(3000) 
     * sim = 0.86; 
     * @return 向量之间的相识度，以向量夹角余弦计算 
     */  
	@Override
	public double getSimiliarity() {
		String [] s1 = source.split(" ");
		String [] s2 = target.split(" ");
		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		for(String str:s1) if(m1.containsKey(str) && m2.containsKey(str)) d1 += m1.get(str)*m2.get(str);
		for(String str:s1) if(m1.containsKey(str)) d2 += m1.get(str)*m1.get(str);
		for(String str:s2) if(m2.containsKey(str)) d3 += m2.get(str)*m2.get(str);
		return (d2!=0 && d3!=0)?d1/Math.sqrt(d2*d3):0;
	}
}
