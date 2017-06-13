package infomgag.personality;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.fabregues.dip.board.Power;

public class Personality {
	
	private boolean aggressiveness;
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
		
		CHOLERIC	(0.5,	0.9,	0.9,	0.9, 2, 2, true, false),
		SANGUINE	(0.9,	0.05,	0.9,	0.1, 2, 2, false, true),
		MELANCHOLIC	(0.05,	0.8,	0.1,	0.8, 0.3, 0.3, false, false),
		PHLEGMATIC	(0.3,	0.5,	0.2,	0.2, 1, 1, false, true);
		
		private final double trustIncreaseFactor;
		private final double trustDecreaseFactor;
		
		private final double likeabilityIncreaseFactor;
		private final double likeabilityDecreaseFactor;
		double trustInitValue;
		double likeInitValue;
		private boolean aggressiveness;
		private boolean trustworthiness;
		

		private PersonalityType(
				double trustIncreaseFactor,
				double trustDecreaseFactor,
				double likeabilityIncreaseFactor,
				double likeabilityDecreaseFactor,
				double trustInitValue,
				double likeInitValue,
				boolean aggressiveness,
				boolean trustworthiness){
			this.trustIncreaseFactor = trustIncreaseFactor;
			this.trustDecreaseFactor = trustDecreaseFactor;
			this.likeabilityIncreaseFactor = likeabilityIncreaseFactor;
			this.likeabilityDecreaseFactor = likeabilityDecreaseFactor;
			this.trustInitValue = trustInitValue;
			this.likeInitValue = likeInitValue;
			this.aggressiveness = aggressiveness;
			this.trustworthiness = trustworthiness;
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
		private boolean getAggressiveness(){
			return aggressiveness;
		}
		private boolean getTrustworthiness(){
			return trustworthiness;
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
		this.aggressiveness = personalityType.getAggressiveness();
		this.trustworthiness = personalityType.getTrustworthiness();
		
	}
	
	

	public int updateTrust(String powerName, Effect effect){
		double newVal = 0;
		double oldVal = this.trustDict.get(powerName);
		
		switch(effect){
		case NEUTRAL:
			newVal = oldVal;
			break;
		case POSITIVE:
			newVal = oldVal + 0.8 * (this.trustIncreaseFactor * (2 - oldVal));
			break;
		case NEGATIVE:
			newVal = oldVal + 0.8 * (this.trustDecreaseFactor * (0 - oldVal));
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
			newVal = oldVal + 0.8 * (this.likeabilityIncreaseFactor * (2 - oldVal));
			break;
		case NEGATIVE:
			newVal = oldVal + 0.8 * (this.likeabilityDecreaseFactor * (0 - oldVal));
			break;
		default:
			break;
		}
		
		this.likeabilityDict.put(powerName, newVal);
		
		return 0;
	}

	public boolean hasTrustIssuesWith(Power power) {
//		try{
		if ((!(power.equals(this.myPower))) && (this.trustDict.get(power.getName()) < this.trustThreshold)){
			return true;
		} else {
			return false;
		}
//		}}catch(NullPointerException e){
//			for (String key : trustDict.keySet()) {
//			    System.out.println(key + " : " + trustDict.get(key));
//			}
//			System.out.println("SOMETHING WENT WRONG 1 " + trustDict.get(power.getName()));
//			System.out.println("SOMETHING WENT WRONG 2 " + power.getName());
//			System.out.println("SOMETHING WENT WRONG 3 " + this.trustThreshold);
//		}
	}
	
	public boolean hasLikeIssues(Power power) {
		if (!(power.equals(this.myPower)) && this.likeabilityDict.get(power.getName()) > this.likeThreshold){
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



	public String getPersonalityValuesString() {
		
		String retString = "\n ---------------------------------------------";
		retString += "\n" + myPower.getName() + "\n";
		for(Power power : this.allPowers){
			if (!(power.equals(this.myPower))){
					retString += power.getName() + " trust: " + trustDict.get(power.getName());
					retString += "\n";
					retString += power.getName() + " like: " + likeabilityDict.get(power.getName());
					retString += "\n";
			}
		}
		return retString;
	}

	
	
	public double getTrustVal(String powerName){
		return this.trustDict.get(powerName);
	}
	
	public double getLikeabilityVal(String powerName){
		return this.likeabilityDict.get(powerName);
	}
	
	public boolean getTrustworthiness(){
		return this.trustworthiness;
	}
	
	public boolean getAggressiveness(){
		return this.aggressiveness;
	}

//	public double getTrustThreshold() {
//		return trustThreshold;
//	}
	
}
