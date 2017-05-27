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
	private int[] trust_array = new int[6];
	private Map<String, Float> trust_dict = new HashMap<String, Float>();
	private Map<String, Float> likeability_dict = new HashMap<String, Float>();
	private float trustIncreaseFactor;
	private float trustDecreaseFactor;
	public float trustThreshold = 1;
	private float likeabilityIncreaseFactor;
	private float likeabilityDecreaseFactor;

	public enum PersonalityType{
		CHOLERIC,
		SANGUINE,
		MELANCHOLIC,
		PHLEGMATIC
	}
	
	public enum Effect{
		NEUTRAL,
		POSITIVE,
		NEGATIVE
	}
	
	public Personality(PersonalityType personalityType){
		switch(personalityType){
			case CHOLERIC:
				
				this.trustIncreaseFactor = (float) 0.5;
				this.trustDecreaseFactor = (float) 0.9;
				break;
			case SANGUINE:
				this.trust_array = new int[]{2,2,2,2,2,2};
				this.trustIncreaseFactor = (float) 0.9;
				this.trustDecreaseFactor = (float) 0.05;
				break;
			case MELANCHOLIC:
				this.trust_array = new int[]{0,0,0,0,0,0};
				this.trustIncreaseFactor = (float) 0.05;
				this.trustDecreaseFactor = (float) 0.8;
				break;
			case PHLEGMATIC:
				this.trust_array = new int[]{1,1,1,1,1,1};
				this.trustIncreaseFactor = (float) 0.3;
				this.trustDecreaseFactor = (float) 0.5;
				break;
			default:
				// Captures any personality not defined in the switch yet
				break;
		}
		
		
	}
	
	public int updateTrust(String powerName, Effect effect){
		float newVal = 0;
		float oldVal = this.trust_dict.get(powerName);
		
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
		
		this.trust_dict.put(powerName, newVal);
		
		return 0;
	}
	
	public int updateLikeability(String powerName, Effect effect){
		float newVal = 0;
		float oldVal = this.likeability_dict.get(powerName);
		
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
		
		this.likeability_dict.put(powerName, newVal);
		
		return 0;
	}
	
	public float getTrustVal(String powerName){
		return this.trust_dict.get(powerName);
	}
	
	public float getLikeabilityVal(String powerName){
		return this.likeability_dict.get(powerName);
	}
	
}
