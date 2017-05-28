package infomgag.personality;

import java.util.HashMap;
import java.util.List;
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
	private double likeThreshold = 1;
	private double likeabilityIncreaseFactor;
	private double likeabilityDecreaseFactor;
	private double trustInitValue;
	private double likeInitValue;
	private Power myPower;
	private List<Power> allPowers;

	public enum PersonalityType{
		
		CHOLERIC	(0.5,	0.9,	0.9,	0.9, 2, 2),
		SANGUINE	(0.9,	0.05,	0.9,	0.1, 2, 2),
		MELANCHOLIC	(0.05,	0.8,	0.1,	0.8, 0, 0),
		PHLEGMATIC	(0.3,	0.5,	0.2,	0.2, 1, 1);
		
		private final double trustIncreaseFactor;
		private final double trustDecreaseFactor;
		
		private final double likeabilityIncreaseFactor;
		private final double likeabilityDecreaseFactor;
		double trustInitValue;
		double likeInitValue;
		

		private PersonalityType(
				double trustIncreaseFactor,
				double trustDecreaseFactor,
				double likeabilityIncreaseFactor,
				double likeabilityDecreaseFactor,
				double trustInitValue,
				double likeInitValue){
			this.trustIncreaseFactor = trustIncreaseFactor;
			this.trustDecreaseFactor = trustDecreaseFactor;
			this.likeabilityIncreaseFactor = likeabilityIncreaseFactor;
			this.likeabilityDecreaseFactor = likeabilityDecreaseFactor;
			this.trustInitValue = trustInitValue;
			this.likeInitValue = likeInitValue;
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
		private double getTrustInitValue() {
			return trustInitValue;
		}
		private double getLikeInitValue() {
			return likeInitValue;
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
		this.likeInitValue = personalityType.getLikeInitValue();
		this.trustInitValue = personalityType.getTrustInitValue();
		
		
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
	
	public boolean hasLikeIssues(Power power) {
		if (this.likeabilityDict.get(power.getName()) > this.likeThreshold){
			return true;
		} else {
			return false;
		}
	}

	public void setMyPower(Power me) {
		this.myPower = me;
		
	}
	
	public void setPowers(List<Power> powers) {
		this.allPowers = powers;
		for(Power power : powers){
			if (!(power.equals(this.myPower))){
				this.trustDict.put(power.getName(), this.trustInitValue);
				this.likeabilityDict.put(power.getName(), this.likeInitValue);
			}
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
