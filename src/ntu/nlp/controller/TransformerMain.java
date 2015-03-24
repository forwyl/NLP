package ntu.nlp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ntu.nlp.component.Word;
import ntu.nlp.component.WordInDataset;
import ntu.nlp.component.WordWithEmotion;
import ntu.nlp.service.CompareDealer;
import ntu.nlp.service.IODealer;
import ntu.nlp.service.InputParser;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerMain {
	
	static{
		PropertyConfigurator.configure("conf/log4j.properties");
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(TransformerMain.class);
	
	private static final String WORD_LOC = "input_files/ckip_word_list.txt";
	private static final String CKIP_RAW_LOC = "input_files/ckip_raw_result.csv";
	private static final String FIRST_STAGE_LOC = "input_files/1stStage.csv";
	private static final String SECOND_STAGE_LOC = "input_files/2ndStage.csv";
	private static final String THIRD_STAGE_LOC = "input_files/3rdStage.csv";
	private static final String NTU_SD_LOC = "input_files/ntusd40k.csv";
	
	private static List<Word> firstStage(String ckipRawLoc, String wordLoc, Boolean isWrite){
		
		List<Word> wordList = InputParser.readBasicWordList(ckipRawLoc);
		LOG.debug("=================================================");
		List<String>  typeList = InputParser.readTypeList(wordLoc);
		List<Word> resultList = CompareDealer.filterType(wordList, typeList, isWrite);
		
		return resultList;
		
	}
	
	private static List<WordWithEmotion> secondStage(String thirdStageLoc, String ntusdLoc, Boolean isWrite){
		
		List<Word> wordList = InputParser.readBasicWordList(thirdStageLoc);
		Map<String, Integer> ntusdMap = InputParser.readNTUSD(ntusdLoc);
		List<WordWithEmotion> wordInNTUList = CompareDealer.ntusdRanking(wordList, ntusdMap, isWrite);
		
		return wordInNTUList;
		
	}
	
	private static Map<String , WordInDataset> thirdStage(String firstStageLoc, Boolean isWrite){
		
		List<Word> wordList = InputParser.readBasicWordList(firstStageLoc);
		Map<String , WordInDataset> widMap = CompareDealer.arrangeDataset(wordList, isWrite);
		
		return widMap;
		
	}	
	
	
	private static List<WordInDataset> genAspect(List<WordInDataset> widList, Boolean isWrite){
		
		List<WordInDataset> aspectList = new ArrayList<WordInDataset>();
		for(WordInDataset wid: widList){
			if(wid.getType().equals("N")){
				aspectList.add(wid);
			}
		}
		
		Collections.sort(aspectList, new WordInDataset().new CompareWordInDatasetByCounter());
		
		if(isWrite){
			List<String> lines = new ArrayList<String>();
			lines.add("Serial,Rank,Word,Type,PositiveNum,NegativeNum,TotalNum,PositiveSerial,NegativeSerial");
			for(WordInDataset wid: aspectList){
				StringBuffer buf = new StringBuffer();
				buf.append(wid.getSerial()+","+wid.getRank()+","+wid.getWord()+","+wid.getType()+","+wid.getPosCounter()+","
									 + wid.getNegCounter()+","+wid.getTotalCounter());
				buf.append(",");
				if(wid.getPosCounter()>1 && wid.getPosList().size()>0){
					for(WordInDataset posWid : wid.getPosList()){
						buf.append(posWid.getSerial()+"_");
					}
				}
				buf.append(",");
				if(wid.getNegCounter()>1 && wid.getNegList().size()>0){
					for(WordInDataset negWid : wid.getNegList()){
						buf.append(negWid.getSerial()+"_");
					}
				}				
				lines.add(buf.toString());
			}
			
			List<String> compactlines = new ArrayList<String>();
			for(WordInDataset w: aspectList){
				compactlines.add(w.getWord());
			}
			
			try {
				IODealer.writeFile(lines, ".csv");
				IODealer.writeFile(compactlines, ".txt");
			} catch (IOException e) {
				LOG.error("Generate CSV file in genAspect error!");
			}
		}
		
		return aspectList;
		
	}
	
	private static List<WordInDataset> genEmotion(List<WordInDataset> widList, List<WordWithEmotion> ntuList, Boolean isWrite){
		
		List<WordInDataset> emotionList = CompareDealer.emotionCal(widList, ntuList);
		
		if(isWrite){
			List<String> lines = new ArrayList<String>();
			lines.add("Serial,Rank,Word,Type,PositiveNum,NegativeNum,TotalNum,PositiveSerial,NegativeSerial");
			for(WordInDataset wid: emotionList){
				StringBuffer buf = new StringBuffer();
				buf.append(wid.getSerial()+","+wid.getRank()+","+wid.getWord()+","+wid.getType()+","+wid.getPosCounter()+","
									 + wid.getNegCounter()+","+wid.getTotalCounter());
				buf.append(",");
				if(wid.getPosCounter()>1 && wid.getPosList().size()>0){
					for(WordInDataset posWid : wid.getPosList()){
						buf.append(posWid.getSerial()+"_");
					}
				}
				buf.append(",");
				if(wid.getNegCounter()>1 && wid.getNegList().size()>0){
					for(WordInDataset negWid : wid.getNegList()){
						buf.append(negWid.getSerial()+"_");
					}
				}				
				lines.add(buf.toString());
			}
			
			List<String> compactlines = new ArrayList<String>();
			for(WordInDataset w: emotionList){
				compactlines.add(w.getWord());
			}
			
			try {
				IODealer.writeFile(lines, ".csv");
				IODealer.writeFile(compactlines, ".txt");
			} catch (IOException e) {
				LOG.error("Generate CSV file in genAspect error!");
			}
		}
		
		return emotionList;
		
	}
	
	public static void main(String[] args) {
		
		LOG.info("=====================START TRANSFORM=====================");
//		firstStage(CKIP_RAW_LOC, WORD_LOC, Boolean.TRUE);
//		thirdStage(FIRST_STAGE_LOC, Boolean.TRUE);
		secondStage(THIRD_STAGE_LOC, NTU_SD_LOC, Boolean.TRUE);

		Map<String, WordInDataset> map = thirdStage(FIRST_STAGE_LOC, Boolean.FALSE);
		List<WordInDataset> widList = new ArrayList<WordInDataset>(map.values());
//		genAspect(widList, Boolean.TRUE);
		List<WordWithEmotion> ntuList = secondStage(THIRD_STAGE_LOC, NTU_SD_LOC, Boolean.TRUE);
		genEmotion(widList, ntuList, Boolean.TRUE);
		
	}

}
