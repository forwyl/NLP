package ntu.nlp.component;

public class WordWithEmotion extends Word{

	private EmotionEnum emotion;

	public EmotionEnum getEmotion() {
		return emotion;
	}

	public void setEmotion(EmotionEnum emotion) {
		this.emotion = emotion;
	}	

	public void setEmotionByNTUInt(int ntuInt) {
		
		switch(ntuInt){
			case 1:
				this.emotion = EmotionEnum.POSITIVE;
				break;
			case -1:
				this.emotion = EmotionEnum.NEGATIVE;
				break;
			default:
				throw new IllegalArgumentException("IllegalArgument In NTU");
				
		}
	}
	
}
