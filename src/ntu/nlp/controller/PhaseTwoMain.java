package ntu.nlp.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ntu.nlp.component.AspectWrapper;
import ntu.nlp.component.EmotionEnum;
import ntu.nlp.component.Word;
import ntu.nlp.component.WordWithEmotion;
import ntu.nlp.service.CompareDealer;
import ntu.nlp.service.FnlpService;
import ntu.nlp.service.IODealer;
import ntu.nlp.service.InputExtracter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fudan.util.exception.LoadModelException;

public class PhaseTwoMain {

	private static final Logger LOG = LoggerFactory.getLogger(PhaseTwoMain.class);
	private static final String INPUT_LOC = "input_files/hotel_test.txt";
	private static final String POS_LOC = "input_files/pos_fnlp_result.txt";
	private static final String FLAG_LOC = "input_files/opinionflag.csv";
	private static final String FLAG_NEW_LOC = "input_files/opinionflag2_cn.csv";
	private static final String POS_TRAIN_LOC = "input_files/pos_hotel_training_cn.txt";
	private static final String NEG_TRAIN_LOC = "input_files/neg_hotel_training_cn.txt";
	private static final String POS_TRAIN_RESULT_LOC = "input_files/pos_fnlp_train.txt";
	private static final String NEG_TRAIN_RESULT_LOC = "input_files/neg_fnlp_train.txt";
	private static final String[] ASPECT_Ary={"酒店","房间","设施","早餐","环境","服务员","价格","态度","感觉","位置",
		"前台","宾馆","交通","服务","床","客人","味道","地方","餐厅","空调","大堂","电视","电梯","卫生","总体","房价","地毯",
		"电话","设备","客房","人员","硬件","评价","时间","楼层","地理","点评","卫生间","速度","毛巾","员工","水","问题",
		"宽带","质量","东西","小姐","品种","热水","饭店"};
	
	private static void evaluateResult(){
		Map<String, Integer> polarityMap = InputExtracter.extractWordPolarity(FLAG_NEW_LOC);
		List<List<Word>> posLineList = InputExtracter.extractWord(POS_TRAIN_RESULT_LOC);
		Set<AspectWrapper> posAwSet = FnlpService.extractAspectOpinion(posLineList);
		List<List<Word>> negLineList = InputExtracter.extractWord(NEG_TRAIN_RESULT_LOC);
		Set<AspectWrapper> negAwSet = FnlpService.extractAspectOpinion(negLineList);
		Map<Integer, List<AspectWrapper>> posCommentMap = transformCommentMap(posAwSet, polarityMap);
		Map<Integer, List<AspectWrapper>> negCommentMap = transformCommentMap(negAwSet, polarityMap);
		LOG.info("Positive hit ratio: " + evaluateCount(posCommentMap, EmotionEnum.POSITIVE));
		LOG.info("Negative hit ratio: "+ evaluateCount(negCommentMap, EmotionEnum.NEGATIVE));		
	}
	
	private static void genTrainFile() throws LoadModelException, IOException{
		List<String> posList = FnlpService.posTransform(POS_TRAIN_LOC);
		List<String> negList = FnlpService.posTransform(NEG_TRAIN_LOC);
		IODealer.writeFile(posList, ".txt");
		IODealer.writeFile(negList, ".txt");
	}
	
	private static void genResult() throws IOException{
		
		Map<String, Integer> polarityMap = InputExtracter.extractWordPolarity(FLAG_NEW_LOC);
		List<List<Word>> lineList = InputExtracter.extractWord(POS_LOC);
		Set<AspectWrapper> awSet = FnlpService.extractAspectOpinion(lineList);
		Map<Integer, List<AspectWrapper>> commentMap = transformCommentMap(awSet, polarityMap);

		int posCounter = 0;
		int negCounter = 0;
		List<String> lines = new ArrayList<String>();
		StringBuffer posBuf = new StringBuffer();
		StringBuffer negBuf = new StringBuffer();
		for(Integer serial: commentMap.keySet()){
			posCounter = 0;
			negCounter = 0;
			posBuf.delete(0, posBuf.length());
			negBuf.delete(0, negBuf.length());
			for(AspectWrapper aw: commentMap.get(serial)){
				if(aw.getAspect().getEmotion() != null){
					if(aw.getAspect().getEmotion() == EmotionEnum.POSITIVE){
						posCounter++;
						for(String str: ASPECT_Ary){
							if(aw.getAspect().getWord().contains(str)){
								posBuf.append(aw.getAspect().getWord() + " ");								
							}
						}
					}else if(aw.getAspect().getEmotion() == EmotionEnum.NEGATIVE){
						negCounter++;
						for(String str: ASPECT_Ary){
							if(aw.getAspect().getWord().contains(str)){
								posBuf.append(aw.getAspect().getWord() + " ");								
							}
						}
					}
				}
			} //End of AspectWrapper list for-loop
			
			//Begin writing task
			lines.add("" + serial);
			lines.add("" + posBuf.toString());
			lines.add("" + negBuf.toString());
			if(negCounter>posCounter){
				lines.add("2");
			}else{
				lines.add("1");
			}				
		} //End of commentMap for-loop
		
		IODealer.writeFile(lines, ".txt");
	}
	
	private static void genPosNeg(){
		
		List<String> posList = InputExtracter.extractPosStrings(FLAG_LOC);
		List<String> negList = InputExtracter.extractNegStrings(FLAG_LOC);
		List<String> list = null;
		try {
			list = FileUtils.readLines(new File(INPUT_LOC), "UTF-8");
		} catch (IOException e) {
			LOG.error("readFile error!");
		}
		Map<String, Integer> posMap = CompareDealer.countFreq(posList, list);
		Map<String, Integer> negMap = CompareDealer.countFreq(negList, list);		
		List<Entry<String,Integer>> topPosList = new ArrayList<Entry<String,Integer>>(posMap.entrySet());
		List<Entry<String,Integer>> topNegList = new ArrayList<Entry<String,Integer>>(negMap.entrySet());
		Collections.sort(topPosList, new Comparator<Entry<String, Integer>>() {    
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {    
                return (o2.getValue() - o1.getValue());    
            }});
		Collections.sort(topNegList, new Comparator<Entry<String, Integer>>() {    
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {    
                return (o2.getValue() - o1.getValue());    
            }});
		for(int i=0; i<10; i++){
			Entry<String, Integer> e = topPosList.get(i);
			LOG.info("[POS] key: " + e.getKey() + ", value:" + e.getValue());
		}
		for(int j=0; j<10; j++){
			Entry<String, Integer> e = topNegList.get(j);
			LOG.info("[NEG] key: " + e.getKey() + ", value:" + e.getValue());			
		}
		
	}
		
	private static void genAspectOpinionFile() throws IOException{
		List<List<Word>> lineList = InputExtracter.extractWord(POS_LOC);
		Set<AspectWrapper> awSet = FnlpService.extractAspectOpinion(lineList);
		List<String> lines = new ArrayList<String>();
		StringBuffer buf = new StringBuffer();
		lines.add("Serial,Aspect,Opinion(List)");
		for(AspectWrapper aw : awSet){
			buf.append(aw.getSerial()+"," + aw.getAspect().getWord() + ",");
			for(WordWithEmotion wwe: aw.getOpinionSet()){
				buf.append(wwe.getWord()+"_");
			}
			lines.add(buf.toString());
			buf.delete(0, buf.length());
		}
		
		IODealer.writeFile(lines, ".csv");
		
	}
	
	private static void genPosFile() throws LoadModelException, IOException{
		List<String> reulstList = FnlpService.posTransform(INPUT_LOC);
		IODealer.writeFile(reulstList, ".txt");		
	}
	
	private static Map<Integer, List<AspectWrapper>> transformCommentMap(Set<AspectWrapper> awSet, Map<String, Integer> polarityMap){
		
		Set<String> termSet = polarityMap.keySet();
		int posCounter = 0;
		int negCounter = 0;
		int intPosDocCounter = 0;
		int inNegDocCounter = 0;		
		Map<Integer, List<AspectWrapper>> commentMap = new TreeMap<Integer, List<AspectWrapper>>();
		
		List<List<Word>> posLineList = InputExtracter.extractWord(POS_TRAIN_RESULT_LOC);
		List<List<Word>> negLineList = InputExtracter.extractWord(NEG_TRAIN_RESULT_LOC);
		
		for(AspectWrapper aw : awSet){
			for(WordWithEmotion wweOpinion : aw.getOpinionSet()){
				for(String term : termSet){
					if(wweOpinion.getWord().contains(term)){
						if(polarityMap.get(term)>0){
							wweOpinion.setEmotion(EmotionEnum.POSITIVE);
							posCounter++;
						}else if(polarityMap.get(term)<0){
							wweOpinion.setEmotion(EmotionEnum.NEGATIVE);
							negCounter++;
						}
					}
				}
			}
/*
			//TODO
			for(WordWithEmotion wweOpinion : aw.getOpinionSet()){
				if(wweOpinion.getEmotion() == null){
					for(List<Word> wordList: posLineList){
						for(Word w : wordList){
							if(wweOpinion.getWord().equals(w)){
								intPosDocCounter++;								
							}
						}
					}
					for(List<Word> wordList: negLineList){
						for(Word w : wordList){
							if(wweOpinion.getWord().equals(w)){
								inNegDocCounter++;								
							}
						}
					}
					if(inNegDocCounter>intPosDocCounter){
						wweOpinion.setEmotion(EmotionEnum.NEGATIVE);
						negCounter++;
					}else{
						wweOpinion.setEmotion(EmotionEnum.POSITIVE);
						posCounter++;
					}
					intPosDocCounter = 0;
					inNegDocCounter = 0;
				}//End of outer if		
			}//End of outer for-loop		
*/
			if(negCounter !=0 | posCounter != 0){
				if(negCounter>posCounter){
					aw.getAspect().setEmotion(EmotionEnum.NEGATIVE);
				}else{
					aw.getAspect().setEmotion(EmotionEnum.POSITIVE);
				}				
			}
			if(commentMap.get(aw.getSerial()) != null){
				commentMap.get(aw.getSerial()).add(aw);
			}else{
				List<AspectWrapper> asList = new ArrayList<AspectWrapper>();
				asList.add(aw);
				commentMap.put(aw.getSerial(), asList);
			}
			posCounter = 0;
			negCounter = 0;
		}
		
		return commentMap;
	}
	
	private static float evaluateCount(Map<Integer, List<AspectWrapper>> commentMap, EmotionEnum emotionType){
		
		int posCounter = 0;
		int negCounter = 0;
		int totalPosCounter = 0;
		int totalNegCounter = 0;
		for(Integer serial: commentMap.keySet()){
			posCounter = 0;
			negCounter = 0;
			for(AspectWrapper aw: commentMap.get(serial)){
				if(aw.getAspect().getEmotion() != null){
					if(aw.getAspect().getEmotion() == EmotionEnum.POSITIVE){
						posCounter++;
					}else if(aw.getAspect().getEmotion() == EmotionEnum.NEGATIVE){
						negCounter++;
					}
				}
			} //End of AspectWrapper list for-loop
			
			if(negCounter>posCounter){
				totalNegCounter++;
			}else{
				totalPosCounter++;
			}				
		} //End of commentMap for-loop
		
		if(emotionType==EmotionEnum.POSITIVE){
			LOG.info("[POSITIVE Result]: TP:" + totalPosCounter + ",FN:" + totalNegCounter);
			return (float)totalPosCounter/(float)(totalPosCounter+totalNegCounter);
		}else if(emotionType==EmotionEnum.NEGATIVE){
			LOG.info("[NEGATIVE Result]: FP:" + totalPosCounter + ",TN:" + totalNegCounter);
			return (float)totalNegCounter/(float)(totalPosCounter+totalNegCounter);
		}else{
			throw new IllegalStateException("Emotion Type Error!");
		}
		
	}
	public static void main(String args[]) throws Exception {
		genResult();
//		genAspectOpinionFile();
//		evaluateResult();
	}
	
}
