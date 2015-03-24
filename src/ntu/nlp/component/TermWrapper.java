package ntu.nlp.component;

import java.util.List;

import tw.cheyingwu.ckip.Term;

public class TermWrapper {
	
	private int serial;
	private int rank;
	private List<Term> term;
	
	//--------------------------------------- [Getter and Setter] -----------------------------------------
	
	public int getSerial() {
		return serial;
	}
	public void setSerial(int serial) {
		this.serial = serial;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public List<Term> getTerm() {
		return term;
	}
	public void setTerm(List<Term> term) {
		this.term = term;
	}

}
