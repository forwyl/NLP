package ntu.nlp.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ntu.nlp.component.AspectWrapper;
import ntu.nlp.component.Word;
import ntu.nlp.component.WordWithEmotion;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import edu.fudan.util.exception.LoadModelException;

public class FnlpService {

	private static final Logger LOG = LoggerFactory.getLogger(FnlpService.class);
	
	public static Set<AspectWrapper> extractAspectOpinion(List<List<Word>> list){
		
		Set<AspectWrapper> awSet = new HashSet<AspectWrapper>();
		
		for(List<Word> wordList : list){
			wordLoop: for(int i=0; i<wordList.size(); i++){
				Word word = wordList.get(i);
				for(AspectWrapper aw : awSet){
					if(aw.getAspect().getWord().equals(word.getWord()) && aw.getAspect().getSerial()==word.getSerial()){
						continue wordLoop;
					}
				}
				if(word.getType().contains("名")){
					AspectWrapper aw = new AspectWrapper();
					aw.setSerial(word.getSerial());
					WordWithEmotion wweAspect = new WordWithEmotion();
					wweAspect.setSerial(word.getSerial());
					wweAspect.setWord(word.getWord());
					wweAspect.setType(word.getType());
					aw.setAspect(wweAspect);
					LOG.info("[Aspect Serial]：" + aw.getSerial() + ", [Aspect]：" + word.getWord());
					Set<WordWithEmotion> opSet = findOpinion(i, wordList);
					for(WordWithEmotion wwe: opSet){
						LOG.info("[Op]：" + wwe.getWord());
						aw.addOpinionToSet(wwe);
					}
					awSet.add(aw);
				}//end of if
			}			
		}
		
		return awSet;
	}
	
	public static List<String> posTransform(String location) throws LoadModelException{

		CWSTagger cws = new CWSTagger("models/seg.m");
		POSTagger tag = new POSTagger(cws,"models/pos.m");
		List<String> list = null;
		List<String> resultList = new ArrayList<String>();
		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
		}
		
		for(String line: list){
			if(line.length()>2){
				String prefix = line.substring(0, line.indexOf("|")+1);
				String comment = line.substring(line.indexOf("|")+1).trim();
				LOG.info("[原始]：" + comment);
				String result = tag.tag(comment);
				LOG.info("[結果]：" + result);
				resultList.add(prefix + result);
			}
		}
		
		return resultList;		
	}
	
	private static Set<WordWithEmotion> findOpinion(int index, List<Word> list){
		String[] opinionAry = {"形容词", "形谓词"};  //, "动词"
		String[] adAry = {"副词"};	
		Set<WordWithEmotion> resultSet = new HashSet<WordWithEmotion>();

/*		
		int j=index-1;
		outer_backward: while(j > 0 && !list.get(j).getWord().contains("。")){ //backward
			Word preW = list.get(j);
			for(String opinion: opinionAry){
				if(preW.getType().contains(opinion)){
					Word prev = list.get(j-1);
					if(prev.getType().contains("副词")){
						String s = prev.getWord()+preW.getWord();
						resultList.add(s);
					}else{
						resultList.add(preW.getWord());
					}
					break outer_backward;
				}
			}
			j--;
		}
*/		
		int k=index+1;
		outer_foreward: while(k < list.size()&& !list.get(k).getWord().contains("。")){ //foreward
			Word foreW = list.get(k);
			for(String opinion: opinionAry){
				if(foreW.getType().contains(opinion)){
					Word prev = list.get(k-1);
					WordWithEmotion wwe = new WordWithEmotion();
					wwe.setSerial(foreW.getSerial());
					if(prev.getType().contains("副词")){
						wwe.setWord(prev.getWord()+foreW.getWord());
						wwe.setType(prev.getType() + "_" + foreW.getType());
						resultSet.add(wwe);
					}else{
						wwe.setWord(foreW.getWord());
						wwe.setType(foreW.getType());
						resultSet.add(wwe);						
					}
//					break outer_foreward;
				}
			}
			k++;
		}
		
		return resultSet;
	}
	
}
