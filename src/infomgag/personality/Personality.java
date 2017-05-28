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
	private double trustThreshold = 1;
	private double likeabilityIncreaseFactor;
	private double likeabilityDecreaseFactor;

	public enum PersonalityType{
		
		CHOLERIC	(0.5,	0.9,	0.9,	0.9),
		SANGUINE	(0.9,	0.05,	0.9,	0.1),
		MELANCHOLIC	(0.05,	0.8,	0.1,	0.8),
		PHLEGMATIC	(0.3,	0.5,	0.2,	0.2);
		
		private final double trustIncreaseFactor;
		private final double trustDecreaseFactor;
		
		private final double likeabilityIncreaseFactor;
		private final double likeabilityDecreaseFactor;

		private PersonalityType(
				double trustIncreaseFactor,
				double trustDecreaseFactor,
				double likeabilityIncreaseFactor,
				double likeabilityDecreaseFactor){
			this.trustIncreaseFactor = trustIncreaseFactor;
			this.trustDecreaseFactor = trustDecreaseFactor;
			this.likeabilityIncreaseFactor = likeabilityIncreaseFactor;
			this.likeabilityDecreaseFactor = likeabilityDecreaseFactor;
		}

		private double getTrustIncreaseFactor() {
			return trustIncreaseFactor;
		}

		private double getTrustDecreaseFactor() {
			return trustDecreaseFactor;
		}
		
		private double getLikeabilityIncreaseFactor() {
			return likeabilityIncreaseFactor;
		}

		private double getLikeabilityDecreaseFactor() {
			return likeabilityDecreaseFactor;
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
		this.likeabilityIncreaseFactor = personalityType.getLikeabilityIncreaseFactor();
		this.likeabilityDecreaseFactor = personalityType.getLikeabilityDecreaseFactor();
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

	public boolean hasTrustIssues(Power power) {
		if (this.trustDict.get(power.getName()) > this.trustThreshold){
			return true;
		} else {
			return false;
		}
	}
	
//	public double getTrustVal(String powerName){
//		return this.trustDict.get(powerName);
//	}
	
//	public double getLikeabilityVal(String powerName){
//		return this.likeabilityDict.get(powerName);
//	}

//	public double getTrustThreshold() {
//		return trustThreshold;
//	}
	
}
