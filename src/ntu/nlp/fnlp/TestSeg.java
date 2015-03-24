package ntu.nlp.fnlp;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import edu.fudan.nlp.parser.dep.DependencyTree;
import edu.fudan.nlp.parser.dep.JointParser;
import edu.fudan.util.exception.LoadModelException;



public class TestSeg {

	private static JointParser parser;
	private static POSTagger tag;
	
	public static void main(String[] args) throws Exception {
		pos();
	}

	private static void pos() throws LoadModelException{
		CWSTagger cws = new CWSTagger("models/seg.m");
		tag = new POSTagger(cws,"models/pos.m");
		
		System.out.println("得到支持的词性标签集合");
		System.out.println(tag.getSupportedTags());
		System.out.println(tag.getSupportedTags().size());
		System.out.println("\n");
		
		String str = "媒体计算研究所成立了，高级数据挖掘很难。乐phone很好！";
		String s = tag.tag(str);
		System.out.println("处理未分词的句子");
		System.out.println(s);
	
		System.out.println("使用英文标签");
		tag.SetTagType("en");		
		System.out.println(tag.getSupportedTags());
		System.out.println(tag.getSupportedTags().size());
		s = tag.tag(str);
		System.out.println(s);		
		System.out.println();
	}
	
	private static void dp() throws Exception{
		parser = new JointParser("models/dep.m");
		System.out.println("得到支持的依存关系类型集合");
		System.out.println(parser.getSupportedTypes());		
		String word = "设施陈旧，看上去不干净。在携程订的酒店中，这间的感觉最不好。";
		test(word);		
	}
	
	/**
	 * 只输入句子，不带词性
	 * @throws Exception 
	 */
	private static void test(String word) throws Exception {		
		POSTagger tag = new POSTagger("models/seg.m","models/pos.m");
		String[][] s = tag.tag2Array(word);
		try {
			DependencyTree tree = parser.parse2T(s[0],s[1]);
			System.out.println("[Left]");
			for(DependencyTree t: tree.leftChilds){
				System.out.println(t.toString());

			}
			System.out.println("[Right]");
			for(DependencyTree t: tree.rightChilds){
				System.out.println(t.toString());
				System.out.println("[Parent]" + t.getParent().toString());
				System.out.println("==========================");
			}
			System.out.println("-------------------------------------------");
			System.out.println(tree.toString());
			String stree = parser.parse2String(s[0],s[1],true);
			System.out.println(stree);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}	

}
