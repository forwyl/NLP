package ntu.nlp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.nlp.component.EmotionEnum;
import ntu.nlp.component.Word;
import ntu.nlp.component.WordInDataset;
import ntu.nlp.component.WordWithEmotion;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompareDealer {

	private static final Logger LOG = LoggerFactory.getLogger(CompareDealer.class);
	
	public static Map<String, Integer> countFreq(List<String> termList, List<String> fileLines){
		
		Map<String, Integer> resultMap = new HashMap<String, Integer>();

		int counter = 0;
		for(String term : termList){
			for(String line : fileLines){
				counter += StringUtils.countMatches(line, term);
			}
			LOG.debug("term: " + term + ", counter: " + counter);
			resultMap.put(term, counter);
			counter = 0;
		}
		
		return resultMap;		
	}
	
	public static Map<String , WordInDataset> arrangeDataset(List<Word> wordList, Boolean isWrite){

		Map<String , WordInDataset>widMap = new HashMap<String, WordInDataset>();
		
		outer:for(Word w: wordList){
			int rank = w.getRank();
			LOG.info("Serial in arrangeDataset:" + w.getSerial() + ",rank:" + rank + ",Word: " + w.getWord());
			WordInDataset currentWord = new WordInDataset();
			currentWord.setSerial(w.getSerial());
			currentWord.setRank(rank);
			currentWord.setWord(w.getWord());
			currentWord.setType(w.getType());
			if(rank == 1){
				currentWord.addPosCounter();
			}else if(rank == 2){
				currentWord.addNegCounter();
			}else{
				throw new IllegalArgumentException("Illegal rank value:" + rank);
			}
			currentWord.setTotalCounter();
			
			for(String wordKey: widMap.keySet()){
				if(widMap.get(wordKey).getWord().trim().equals(w.getWord().trim())){ // check whether it has existed
					WordInDataset previousWord = widMap.get(wordKey);
					switch(rank){ // check positive(1) or negative(2)
						case 1: //
							previousWord.addPosCounter();
							if(previousWord.getPosCounter()>1){
								previousWord.addToPosList(currentWord);
							}
							break;
						case 2: //
							previousWord.addNegCounter();
							if(previousWord.getNegCounter()>1){
								previousWord.addToNegList(currentWord);
							}
							break;
						default:
							throw new IllegalArgumentException("Illegal rank value in switch block:" + rank);
					}
					
					//update rank type and total amount
					if(previousWord.getRank() != 3 && previousWord.getRank() != rank){
						previousWord.setRank(3);
					}
					previousWord.setTotalCounter();
					
					widMap.put(wordKey, previousWord);
					continue outer;				
				}				
			}//End of inner for-loop
			
			widMap.put(currentWord.getWord().trim(), currentWord);
		}
		
		List<WordInDataset> widList = new ArrayList<WordInDataset>(widMap.values());
		Collections.sort(widList);
		
		
		if(isWrite){
			List<String> lines = new ArrayList<String>();
			lines.add("Serial,Rank,Word,Type,PositiveNum,NegativeNum,TotalNum,PositiveSerial,NegativeSerial");
			for(WordInDataset wid: widList){
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
			
			try {
				IODealer.writeFile(lines, ".csv");
			} catch (IOException e) {
				LOG.error("Generate CSV file in arrangeDataset error!");
			}
		}
		
		return widMap;
		
	}
	
	public static List<WordWithEmotion> ntusdRanking(List<Word> wordList, Map<String, Integer> ntuMap, Boolean isWrite){
		
		List<WordWithEmotion> ntuList = new ArrayList<WordWithEmotion>();
		
		listLoop: for(Word word : wordList){
			WordWithEmotion wordInNTU = new WordWithEmotion();
			wordInNTU.setSerial(word.getSerial());
			wordInNTU.setRank(word.getRank());
			wordInNTU.setWord(word.getWord());
			wordInNTU.setType(word.getType());
			wordInNTU.setEmotion(EmotionEnum.NONE);
			LOG.info("Serial in ntusdRanking:" + wordInNTU.getSerial() + ",rank:" + wordInNTU.getRank() + ",Word: " + wordInNTU.getWord());
			for(String strKey: ntuMap.keySet()){
				if(word.getWord().equals(strKey)){
					wordInNTU.setEmotionByNTUInt(ntuMap.get(strKey).intValue());
					ntuList.add(wordInNTU);
					LOG.debug( "[InNTU]" + wordInNTU.getWord()+ " , " +  wordInNTU.getEmotion().toString());
					continue listLoop;
				}
			}
			ntuList.add(wordInNTU);
		}
		
		if(isWrite){
			List<String> lines = new ArrayList<String>();
			lines.add("Serial,Rank,Word,Type,Emotion");
			for(WordWithEmotion wordInNTU: ntuList){
				lines.add(wordInNTU.getSerial()+","+wordInNTU.getRank()+","+wordInNTU.getWord()+","+
								 wordInNTU.getType()+","+wordInNTU.getEmotion().toString());
			}
			
			try {
				IODealer.writeFile(lines, ".csv");
			} catch (IOException e) {
				LOG.error("write in ntusdRanking error!");
			}
		}
		
		return ntuList;
		
	}
		
	public static List<Word> filterType(List<Word> wordList, List<String> typeList, Boolean isWrite){
		
		List<Word> reultWordList = new ArrayList<Word>();
		
		for(Word word: wordList){
			if(word.getType().length() > 0){
				for(String type : typeList){
					if(word.getType().trim().equals(type.trim())){
						reultWordList.add(word);
						LOG.info(word.getSerial()+","+word.getRank()+","+word.getWord()+","+word.getType()); // test
					}
				}
//				if(typeMap.containsKey(word.getType().trim().substring(0, 1))){
//					reultWordList.add(word);
//					LOG.info(word.getSerial()+","+word.getRank()+","+word.getWord()+","+word.getType()); // test
//				}				
			}
		}
		
		if(isWrite){
			List<String> lines = new ArrayList<String>();
			for(Word word: reultWordList){
				lines.add(word.getSerial()+","+word.getRank()+","+word.getWord()+","+word.getType());
			}
			try {
				IODealer.writeFile(lines, ".csv");
			} catch (IOException e) {
				LOG.error("write in filterType error");
			}
		}
		
		return reultWordList;
		
	}
	
	public static List<WordInDataset> emotionCal(List<WordInDataset> widList, List<WordWithEmotion> ntuList){
		
		List<WordInDataset> resultList = new ArrayList<WordInDataset>();
		outer: for(WordInDataset wid : widList){
			for(WordWithEmotion ntu: ntuList){
				if(wid.getWord().trim().equals(ntu.getWord().trim()) && ntu.getEmotion() != EmotionEnum.NONE){
					resultList.add(wid);
					continue outer;
				}
			}
		}
		
		Collections.sort(resultList, new WordInDataset().new CompareWordInDatasetByCounter());
		
		return resultList;
		
	}
	
}
