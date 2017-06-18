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

	private double learningRate = 0.4;

	private Power myPower;
	private List<Power> allPowers;
	/**
	 * Max proposals are 0-inclusive, bound-exclusive. So '3' will result in (0,
	 * 1, 2), ergo 1 proposal on average.
	 */
	private int maxNumProposals;
	private final PersonalityType type;

	public enum PersonalityType {
		
		/*
		 * Enumeration of columns in personality profiles:
		 * 
		 * 	T-^			Trust Increase Factor
		 * 	T-v			Trust Decrease Factor
		 * 	L-^			Like Increase Factor
		 * 	L-v			Like Decrease Factor
		 * 	T-init		Trust Value at Initialization
		 * 	L-init		Like Value at Initialization
		 * 	Agrr		"Aggression"
		 * 	TrustW		"TrustWorthiness"
		 * 	MaxProp		Maximum number of proposals per negotiation cycle
		 */
		
		// First set. This was our initial hunch, we combined it with a learning rate of 0.2.
		//			T-^		T-v		L-^		L-v		T-init	L-init	Agrr	TrustW	MaxProp
		CHOLERIC(	0.5,	0.9, 	0.9, 	0.9, 	2, 		2, 		true, 	false, 	5), 
		SANGUINE(	0.9, 	0.05, 	0.9, 	0.1, 	2, 		2, 		false, 	true,	6), 
		MELANCHOLIC(0.05, 	0.8, 	0.1, 	0.8, 	0.3, 	0.3, 	false, 	false, 	1), 
		PHLEGMATIC(	0.3, 	0.5, 	0.2, 	0.2, 	1, 		1,		false, 	true, 	2), 
		NEUTRAL(	0, 		0, 		0, 		0, 		1, 		1, 		true, 	true, 	0);
		
		/* Second set. Two changes: Less extreme increase/decrease values, no longer initialize personalities to certain high values. Combined with a Learning Rate of 0.4.
		//			T-^		T-v		L-^		L-v		T-init	L-init	Agrr	TrustW	MaxProp
		CHOLERIC(	0.5,	0.9, 	0.9, 	0.9, 	1.7, 	1.7, 	true, 	false, 	5), 
		SANGUINE(	0.9, 	0.2, 	0.9, 	0.3, 	1.7, 	1.7, 	false, 	true,	6), 
		MELANCHOLIC(0.2, 	0.8, 	0.3, 	0.8, 	0.3, 	0.3, 	false, 	false, 	1), 
		PHLEGMATIC(	0.3, 	0.5, 	0.2, 	0.2, 	1, 		1,		false, 	true, 	2), 
		NEUTRAL(	0, 		0, 		0, 		0, 		1, 		1, 		true, 	true, 	0);
		*/

		private final double trustIncreaseFactor;
		private final double trustDecreaseFactor;

		private final double likeabilityIncreaseFactor;
		private final double likeabilityDecreaseFactor;
		double trustInitValue;
		double likeInitValue;
		private boolean aggressiveness;
		private boolean trustworthiness;
		/**
		 * Max proposals are 0-inclusive, bound-exclusive. So '3' will result in
		 * (0, 1, 2), ergo 1 proposal on average.
		 */
		private final int maxNumProposals;

		private PersonalityType(double trustIncreaseFactor, double trustDecreaseFactor,
				double likeabilityIncreaseFactor, double likeabilityDecreaseFactor, double trustInitValue,
				double likeInitValue, boolean aggressiveness, boolean trustworthiness, int maxNumProposals) {
			this.trustIncreaseFactor = trustIncreaseFactor;
			this.trustDecreaseFactor = trustDecreaseFactor;
			this.likeabilityIncreaseFactor = likeabilityIncreaseFactor;
			this.likeabilityDecreaseFactor = likeabilityDecreaseFactor;
			this.trustInitValue = trustInitValue;
			this.likeInitValue = likeInitValue;
			this.aggressiveness = aggressiveness;
			this.trustworthiness = trustworthiness;
			this.maxNumProposals = maxNumProposals;
		}

		private int getMaxNumProposals() {
			return this.maxNumProposals;
		}

		private double getTrustIncreaseFactor() {
			return this.trustIncreaseFactor;
		}

		private double getTrustDecreaseFactor() {
			return this.trustDecreaseFactor;
		}

		private double getLikeabilityIncreaseFactor() {
			return this.likeabilityIncreaseFactor;
		}

		private double getLikeabilityDecreaseFactor() {
			return this.likeabilityDecreaseFactor;
		}

		private double getTrustInitValue() {
			return this.trustInitValue;
		}

		private double getLikeInitValue() {
			return this.likeInitValue;
		}

		private boolean getAggressiveness() {
			return this.aggressiveness;
		}

		private boolean getTrustworthiness() {
			return trustworthiness;
		}
	}

	public enum Effect {
		NEUTRAL, POSITIVE, NEGATIVE
	}

	public Personality(PersonalityType personalityType) {

		this.type = personalityType;
		this.trustIncreaseFactor = personalityType.getTrustIncreaseFactor();
		this.trustDecreaseFactor = personalityType.getTrustDecreaseFactor();
		this.likeabilityIncreaseFactor = personalityType.getLikeabilityIncreaseFactor();
		this.likeabilityDecreaseFactor = personalityType.getLikeabilityDecreaseFactor();
		this.likeInitValue = personalityType.getLikeInitValue();
		this.trustInitValue = personalityType.getTrustInitValue();
		this.aggressiveness = personalityType.getAggressiveness();
		this.trustworthiness = personalityType.getTrustworthiness();
		this.maxNumProposals = personalityType.getMaxNumProposals();

	}

	public int updateTrust(String powerName, Effect effect) {
		double newVal = 0;
		double oldVal = this.trustDict.get(powerName);

		switch (effect) {
		case NEUTRAL:
			newVal = oldVal;
			break;
		case POSITIVE:
			newVal = oldVal + this.learningRate * (this.trustIncreaseFactor * (2 - oldVal));
			break;
		case NEGATIVE:
			newVal = oldVal + this.learningRate * (this.trustDecreaseFactor * (0 - oldVal));
			break;
		default:
			break;
		}

		this.trustDict.put(powerName, newVal);

		return 0;
	}

	public int updateLikeability(String powerName, Effect effect) {
		double newVal = 0;
		double oldVal = this.likeabilityDict.get(powerName);

		switch (effect) {
		case NEUTRAL:
			newVal = oldVal;
			break;
		case POSITIVE:
			newVal = oldVal + this.learningRate * (this.likeabilityIncreaseFactor * (2 - oldVal));
			break;
		case NEGATIVE:
			newVal = oldVal + this.learningRate * (this.likeabilityDecreaseFactor * (0 - oldVal));
			break;
		default:
			break;
		}

		this.likeabilityDict.put(powerName, newVal);

		return 0;
	}

	public boolean hasTrustIssuesWith(Power power) {
		// try{
		return ((!(power.equals(this.myPower))) && (this.trustDict.get(power.getName()) < this.trustThreshold));
		// }}catch(NullPointerException e){
		// for (String key : trustDict.keySet()) {
		// System.out.println(key + " : " + trustDict.get(key));
		// }
		// System.out.println("SOMETHING WENT WRONG 1 " +
		// trustDict.get(power.getName()));
		// System.out.println("SOMETHING WENT WRONG 2 " + power.getName());
		// System.out.println("SOMETHING WENT WRONG 3 " + this.trustThreshold);
		// }
	}

	public boolean hasLikeIssues(Power power) {
		return (!(power.equals(this.myPower)) && this.likeabilityDict.get(power.getName()) < this.likeThreshold);
	}

	public void setMyPower(Power me) {
		this.myPower = me;

	}

	public void setPowers(List<Power> powers) {
		this.allPowers = powers;
		for (Power power : powers) {
			if (!(power.equals(this.myPower))) {
				this.trustDict.put(power.getName(), this.trustInitValue);
				this.likeabilityDict.put(power.getName(), this.likeInitValue);
			}
		}

	}

	public String getPersonalityType(){
		return type.toString();
	}
	
	public String getPersonalityValuesString() {
		String retStringValueLike = "";
		String retStringValueTrust = "";
		int count = 0;
		for (Power power : this.allPowers) {
			retStringValueLike += likeabilityDict.get(power.getName());
			retStringValueTrust += trustDict.get(power.getName());
			count++;
			if (count != allPowers.size()) {
				retStringValueTrust += ", ";
				retStringValueLike += ", ";
			}
		}
		return retStringValueLike + ", " + retStringValueTrust;
	}

	public boolean isType(PersonalityType personalityType) {
		return this.type == personalityType;
	}

	public int getMaxProposals() {
		return this.maxNumProposals;
	}

	public double getTrustVal(String powerName) {
		return this.trustDict.get(powerName);
	}

	public double getLikeabilityVal(String powerName) {
		return this.likeabilityDict.get(powerName);
	}

	public boolean getTrustworthiness() {
		return this.trustworthiness;
	}

	public boolean getAggressiveness() {
		return this.aggressiveness;
	}

	// public double getTrustThreshold() {
	// return trustThreshold;
	// }

}
