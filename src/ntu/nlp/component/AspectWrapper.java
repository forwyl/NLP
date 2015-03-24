package ntu.nlp.component;

import java.util.HashSet;
import java.util.Set;

public class AspectWrapper implements Comparable<AspectWrapper>{

	private int serial;
	private WordWithEmotion aspect;
	private Set<WordWithEmotion> opinion = new HashSet<WordWithEmotion>();

//--------------------------------Getter and Setter--------------------------------
	
	public int getSerial() {
		return serial;
	}
	public void setSerial(int serial) {
		this.serial = serial;
	}
	public WordWithEmotion getAspect() {
		return aspect;
	}
	public void setAspect(WordWithEmotion aspect) {
		this.aspect = aspect;
	}
	public Set<WordWithEmotion> getOpinionSet() {
		return opinion;
	}
	public void addOpinionToSet(WordWithEmotion opinion) {
		this.opinion.add(opinion);
	}
	@Override
	public int compareTo(AspectWrapper aw) {
		return this.getSerial() - aw.getSerial();
	}
		
}
