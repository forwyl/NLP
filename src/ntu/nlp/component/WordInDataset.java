package ntu.nlp.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WordInDataset  extends Word implements Comparable<WordInDataset>{
	
	private int posCounter = 0;
	private int negCounter = 0;
	private int totalCounter = 0;
	private List<WordInDataset> posList;
	private List<WordInDataset> negList;
	
	@Override
	public int compareTo(WordInDataset w) {
		return this.getSerial() - w.getSerial();
	}
	
	//-------------------Getter and Setter-------------------------	
	public int getPosCounter() {
		return posCounter;
	}
	public void addPosCounter() {
		this.posCounter++;
		if(posCounter==2){
			this.posList = new ArrayList<WordInDataset>();
		}
	}
	public int getNegCounter() {
		return negCounter;
	}
	public void addNegCounter() {
		this.negCounter++;
		if(negCounter==2){
			this.negList = new ArrayList<WordInDataset>();
		}
	}
	public List<WordInDataset> getPosList() {
		return posList;
	}
	public void addToPosList(WordInDataset wid) {
		this.posList.add(wid);
	}
	public List<WordInDataset> getNegList() {
		return negList;
	}
	public void addToNegList(WordInDataset wid) {
		this.negList.add(wid);
	}
	public int getTotalCounter() {
		return totalCounter;
	}
	public void setTotalCounter() {
		this.totalCounter = this.posCounter + this.negCounter;
	}
	
	public class CompareWordInDatasetByCounter implements Comparator<WordInDataset> {

		@Override
		public int compare(WordInDataset wid1, WordInDataset wid2) {
		    //Dec order
			if (wid1.getTotalCounter() < wid2.getTotalCounter()) {
			   return 1;
		   }else if (wid1.getTotalCounter() > wid2.getTotalCounter()){
			   return -1;
		   }else{
			   return 0;
		   }
		}
		
	}
	
}
