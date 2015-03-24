package ntu.nlp.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.nlp.component.Comment;
import ntu.nlp.component.Word;
import ntu.nlp.component.WordInDataset;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputParser {

	private static final Logger LOG = LoggerFactory.getLogger(InputParser.class);
	
	public static List<Comment> readFile(String location) {
		
		List<Comment> commentList = new ArrayList<Comment>();
		List<String> list = null;
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(int i=0; i<list.size(); i+=2){
			Comment comment = new Comment();
			int rank = Integer.parseInt(list.get(i).trim());
			String nextLine = list.get(i+1).trim();
			int serial = Integer.parseInt(nextLine.substring(0, nextLine.indexOf("|")).trim());
			String msg = nextLine.substring(nextLine.indexOf("|")+1).trim()	;
			
			comment.setRank(rank);
			comment.setSerial(serial);
			comment.setMsg(msg);
			commentList.add(comment);
			
//			LOG.debug("[serial]"+ serial + ",[rank]"+ rank + ",[msg]" + msg);
			
		}
		
		LOG.debug("total data amount:"+commentList.size());
		return commentList;
		
	}
	
	public static List<Word> readBasicWordList(String location){
		
		//_index here are 1-based
		final int serialIndex = 1;
		final int rankIndex = 2;
		final int wordIndex = 3;
		final int typeIndex = 4;
		List<Word> wordList = new ArrayList<Word>();
		List<String> list = null;
		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readBasicWordList error!");
			return null;
		}		
		
		for(int i=1; i<list.size(); i++){
			String ary[] = list.get(i).split(",");
			Word w = new Word();
			w.setSerial(Integer.parseInt(ary[serialIndex-1]));
			w.setRank(Integer.parseInt(ary[rankIndex-1]));
			w.setWord(ary[wordIndex-1]);
			w.setType(ary[typeIndex-1]);
			wordList.add(w);
		}
		
		return wordList;
		
	}
	
	public static List<String> readTypeList(String location){
		
		List<String> typeList = new ArrayList<String>();
		List<String> list = null;
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readTypeList error!");
			return null;
		}
		
		for(String s: list){
			String ary[] = s.split(",");
			for(String typeStr: ary){
				typeList.add(typeStr.trim());
			}
		}
		
		return typeList;
		
	}
	
	public static Map<String, Integer>readNTUSD(String location){

		Map<String, Integer> ntuMap = new HashMap<String, Integer>();
		List<String> list = null;		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readNTUSD error!");
			return null;
		}
		
		for(int i=1; i<list.size(); i++){
			String line = list.get(i);
			String key = line.substring(0, line.lastIndexOf(",")).trim();
			Integer value = Integer.parseInt(line.substring(line.lastIndexOf(",")+1).trim());
			ntuMap.put(key, value);
		}
		
		return ntuMap;
		
	}
	
}
