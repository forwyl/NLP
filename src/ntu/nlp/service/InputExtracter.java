package ntu.nlp.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.nlp.component.Word;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputExtracter {

	private static final Logger LOG = LoggerFactory.getLogger(InputExtracter.class);
	
	public static Map<String, Integer> extractWordPolarity(String location){
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<String> list = null;		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(int i=1; i<list.size(); i++){
			String[] ary= list.get(i).split(",");
			map.put(ary[0], Integer.parseInt(ary[4]));
		}
		
		return map;
	}
	
	public static Map<String, Integer> extractWordPolarityByCount(String location){
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<String> list = null;		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(String s: list){
			String[] ary= s.split(",");
			int i= 1;
			int value = 0;
			while(i<ary.length && Integer.parseInt(ary[i])>-2){
				value += Integer.parseInt(ary[i]);
				i++;
			}
			LOG.debug("term : " + ary[0] + ",value: " + value);
			map.put(ary[0], value);
		}
		
		return map;
	}
	
	public static List<String> extractPosStrings(String location){

		List<String> resultList = new ArrayList<String>();
		List<String> list = null;
		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(String str: list){
			if(str.contains(",1,1,1")){
				resultList.add(str.substring(0, str.indexOf(",")));
			}
		}
		
		return resultList;
	}
	
	public static List<String> extractNegStrings(String location){

		List<String> resultList = new ArrayList<String>();
		List<String> list = null;
		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(String str: list){
			if(str.contains(",-1,-1,-1")){
				resultList.add(str.substring(0, str.indexOf(",")));
			}
		}
		
		return resultList;
	}	
	
	public static List<List<Word>> extractWord(String location){
		
		List<List<Word>> lineList = new ArrayList<List<Word>>();
		List<String> list = null;
		
		try {
			list = FileUtils.readLines(new File(location), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
			return null;
		}
		
		for(int i=0; i<list.size(); i++){
			String line = list.get(i);
			String serial = line.substring(0, line.indexOf("|"));
			String seg = line.substring(line.indexOf("|")+1).trim();
			List<Word> wordList = new ArrayList<Word>();
			for(String item: seg.split(" ")){
				int index = item.lastIndexOf("/");
				Word word = new Word();
				word.setSerial(Integer.parseInt(serial));
				word.setWord(item.substring(0, index));
				word.setType(item.substring(index+1));
				wordList.add(word);
			}
			lineList.add(wordList);
		}
		
		return lineList;
	}
	
}
