package infomgag.personality;

import java.util.HashMap;
import java.util.Map;

import es.csic.iiia.fabregues.dip.board.Power;

public class Personality {
	
	private int aggression;
	private int naiveness;
	private int pessimism;	
	private int impulsiveness;
	private boolean trustworthiness;
	private Map<String, Double> trustDict = new HashMap<>();
	private Map<String, Double> likeabilityDict = new HashMap<>();
	private double trustIncreaseFactor;
	private double trustDecreaseFactor;
	public double trustThreshold = 1;
	private double likeabilityIncreaseFactor;
	private double likeabilityDecreaseFactor;

	public enum PersonalityType{
		
		CHOLERIC	(0.5,	0.9),
		SANGUINE	(0.9,	0.05),
		MELANCHOLIC	(0.05,	0.8),
		PHLEGMATIC	(0.3,	0.5);
		
		private double trustIncreaseFactor;
		private double trustDecreaseFactor;
		
		private double likeabilityIncreaseFactor;
		private double likeabilityDecreaseFactor;

		private PersonalityType(
				double trustIncreaseFactor,
				double trustDecreaseFactor){
			this.trustIncreaseFactor = trustIncreaseFactor;
			this.trustDecreaseFactor = trustDecreaseFactor;
			this.likeabilityIncreaseFactor = likeabilityIncreaseFactor;
			this.likeabilityDecreaseFactor = likeabilityDecreaseFactor;
		}

		public double getTrustIncreaseFactor() {
			return trustIncreaseFactor;
		}

		public double getTrustDecreaseFactor() {
			return trustDecreaseFactor;
		}
		
	}
	
	public enum Effect{
		NEUTRAL,
		POSITIVE,
		NEGATIVE
	}
	
	public Personality(PersonalityType personalityType){
		this.trustIncreaseFactor = personalityType.getTrustIncreaseFactor();
		this.trustDecreaseFactor = personalityType.getTrustDecreaseFactor();
		
	}
	
	public int updateTrust(String powerName, Effect effect){
		double newVal = 0;
		double oldVal = this.trustDict.get(powerName);
		
		switch(effect){
		case NEUTRAL:
			newVal = oldVal;
			break;
		case POSITIVE:
			newVal = oldVal + this.trustIncreaseFactor * (2 - oldVal);
			break;
		case NEGATIVE:
			newVal = oldVal + this.trustDecreaseFactor * (0 - oldVal);
			break;
		default:
			break;
		}
		
		this.trustDict.put(powerName, newVal);
		
		return 0;
	}
	
	public int updateLikeability(String powerName, Effect effect){
		double newVal = 0;
		double oldVal = this.likeabilityDict.get(powerName);
		
		switch(effect){
		case NEUTRAL:
			newVal = oldVal;
			break;
		case POSITIVE:
			newVal = oldVal + this.likeabilityIncreaseFactor * (2 - oldVal);
			break;
		case NEGATIVE:
			newVal = oldVal + this.likeabilityDecreaseFactor * (0 - oldVal);
			break;
		default:
			break;
		}
		
		this.likeabilityDict.put(powerName, newVal);
		
		return 0;
	}
	
	public double getTrustVal(String powerName){
		return this.trustDict.get(powerName);
	}
	
	public double getLikeabilityVal(String powerName){
		return this.likeabilityDict.get(powerName);
	}
	
}
