package infomgag.decisionMaking;
import es.csic.iiia.fabregues.dip.board.Dislodgement;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.DSBOrder;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.Order;
import es.csic.iiia.fabregues.dip.orders.RTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.dbraneTactics.Plan;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Logger;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import infomgag.personality.*;
import infomgag.personality.Personality.Effect;
import infomgag.personality.Personality.PersonalityType;
public class DecisionMaker{
	
	Personality personality; 
	ArrayList<BasicDeal> confirmedDeals; 
	DBraneTactics dbraneTactics = new DBraneTactics(); //Will be switched out with the montecarlo shit. 
	Game game; 
	Random random;
	Power me;
	List<String> negotiatingPowers;
	Logger logger;
	private boolean firstIncomingDeal;
	private double dealAcceptanceThreshold;
	private double dealAcceptanceModifier = 1.1;
	private double dealRejectionModifier = 0.9;
	
	//Constructor: Takes in a personality and a game object. 
	public DecisionMaker(Personality personality, Game game, Power me, ArrayList<BasicDeal> confirmedDeals, List<String> negotiatingPowers, Logger logger){
		random = new Random();
		this.game = game;
		this.personality = personality;
		this.confirmedDeals = confirmedDeals;
		this.negotiatingPowers = negotiatingPowers;
		//this.confirmedDeals = new ArrayList<BasicDeal>();
		this.me = me;
		personality.setMyPower(me);
		personality.setPowers(game.getPowers());
		this.logger = logger;
		this.firstIncomingDeal = true;
		
	}
	
	public String getPersonalityValues(){
		return personality.getPersonalityValuesString();
	}
	
	public void update(ArrayList<Order> submittedOrders){
		//However, if the order
		//is a HLDOrder, then the corresponding power is still allowed to submit a
		//SUPOrder or SUPMTOOrder for that unit instead of the HLDOrder.
		
		ArrayList<Region> regionsILeft = new ArrayList<Region>();
		
		if (submittedOrders == null){
			return;
		}
		
		if (submittedOrders != null && submittedOrders.size() > 0){
			for(Order order : submittedOrders){
				if (order.getPower().equals(me)){
					if (order instanceof MTOOrder){
						MTOOrder moveOrder = (MTOOrder) order;
						for (Region region : this.me.getControlledRegions()){
							if (moveOrder.getLocation().equals(region)){
								regionsILeft.add(region);
							}
						}
					}
				}
			}
		}
		
		if (submittedOrders != null && submittedOrders.size() > 0){
			for(Order order : submittedOrders){
				if (!(order.getPower().equals(me))){
					if (order instanceof SUPOrder || order instanceof SUPMTOOrder){
						if (order instanceof SUPOrder){
							SUPOrder supOrder = (SUPOrder) order;
							if (me.equals(supOrder.getSupportedOrder().getPower())){
								personality.updateLikeability(order.getPower().getName(), Effect.POSITIVE);
							}
						}
						if (order instanceof SUPMTOOrder){
							SUPMTOOrder supOrder = (SUPMTOOrder) order;
							if (me.equals(supOrder.getSupportedOrder().getPower())){
								personality.updateLikeability(order.getPower().getName(), Effect.POSITIVE);
							}
						}
						
					}
					
					if (order instanceof MTOOrder){
						MTOOrder moveOrder = (MTOOrder) order;
						// check whether the move order causes us to lose territory
						for (Region region : this.me.getControlledRegions()){
							if (moveOrder.getDestination().equals(region) && !(regionsILeft.contains(region))){
								personality.updateLikeability(order.getPower().getName(), Effect.NEGATIVE);
							}
						}
					}
				}
			}
		}
		
		
		if (submittedOrders.size() > 0 & this.confirmedDeals.size() > 0){
			for(BasicDeal confirmedDeal : confirmedDeals){
				if (confirmedDeal.getOrderCommitments().size() > 0){
					for(OrderCommitment orderCommitment : confirmedDeal.getOrderCommitments()){
						if(orderCommitment.getPhase().equals(game.getPhase()) && orderCommitment.getYear() == game.getYear()){
							for(Order order : submittedOrders){
								if (!(order.getPower().equals(me))){
									if (orderCommitment.getOrder().equals(order)){
										personality.updateTrust(orderCommitment.getOrder().getPower().getName(), Effect.POSITIVE);		//Updates the personality dependant on if you were screwed over or not in the last round.
										break;
									}
								}
							}
							if (!(orderCommitment.getOrder().getPower().equals(me))){
								personality.updateTrust(orderCommitment.getOrder().getPower().getName(), Effect.NEGATIVE);
							}
						}
					}
				}
				
				if (confirmedDeal.getDemilitarizedZones().size() > 0){
					for(DMZ dmz : confirmedDeal.getDemilitarizedZones()){	
						if(dmz.getPhase().equals(game.getPhase()) && dmz.getYear() == game.getYear()){
							for(Order order : submittedOrders){
								if (!(order.getPower().equals(me))){
									if(order instanceof MTOOrder){
										MTOOrder tempOrder = (MTOOrder) order;
										for(Province province : dmz.getProvinces()){
											for(Region region : province.getRegions()){
												if (tempOrder.getDestination().equals(region)){
													personality.updateTrust(order.getPower().getName(), Effect.NEGATIVE);
												}
											}
										}	
									}
								}
							}
						}
					}
				}
			} 
		}
	}

		
	//Handles an incomming message, this could be a reject, accept, confirm or propose message from another player
	public String handleIncomingMessages(Message receivedMessage, List<Power> myAllies){
		//Check if deal is outdated
		DiplomacyProposal proposal;
		//Then handle the various possible messages: PROPOSE, ACCEPT, REJECT and CONFIRM
		
		switch(receivedMessage.getPerformative()){
		
		case DiplomacyNegoClient.PROPOSE:			//A player has proposed a deal to you
			proposal  = (DiplomacyProposal)receivedMessage.getContent();	
			BasicDeal deal = (BasicDeal)proposal.getProposedDeal();
			if(handleProposal(deal, myAllies)){		//If you choose to accept the deal. Then handleProposal should be TRUE, else FALSE
				return "Accepting proposal:" + receivedMessage.getMessageId();
			}
		case DiplomacyNegoClient.ACCEPT:			//A player has accepted a deal you have proposed
			proposal  = (DiplomacyProposal)receivedMessage.getContent();	
			//Handle the information that another player has accepted a deal you proposed to them
			return "Recieved acceptence from " + receivedMessage.getSender() + ": " + proposal;
		case DiplomacyNegoClient.REJECT:
			proposal  = (DiplomacyProposal)receivedMessage.getContent();	//A player has rejected a deal you have proposed
			//Handled the information that another player has accepted a deal you propoed to them
			return "Deal rejected: " + proposal;
		case DiplomacyNegoClient.CONFIRM:			//The negotiation client has confirmed that all players in a deal has accepted the deal. This is now binding. 
			proposal  = (DiplomacyProposal)receivedMessage.getContent();	
			BasicDeal tempDeal = (BasicDeal)proposal.getProposedDeal();
			handleConfirmation(tempDeal);
			
			return "Confirmation of " + proposal;
		default:
			return "UNKOWN INCOMING MESSAGE";
		}
	}	
	
	//If a confirmation message is sent to the agent, then it is being handled here. 
	private void handleConfirmation(BasicDeal deal) {
		confirmedDeals.add(deal);
		//Incase of the notary consisty check is turned off, 
		//then we need to check consistancy with other proposed deals at the same time. 
		//There is code to do so in the exampled bots. but it is a bit cloncky. 
	}

	//Handles a proposal message that is sent to the agent. 
	private boolean handleProposal(BasicDeal deal, List<Power> myAllies) {
		boolean outDated = false;
		boolean trustIssues = false;
		Map<String, Integer> dealPowerCountDict = new HashMap<>();
		
		
			
		
		for(DMZ dmz : deal.getDemilitarizedZones()){
			
			// Sometimes we may receive messages too late, so we check if the proposal does not
			// refer to some round of the game that has already passed.
			if( isHistory(dmz.getPhase(), dmz.getYear())){
				outDated = true;
				break;
			}
			
			List<Power> tempPowers = dmz.getPowers();
			tempPowers.remove(me);
			for (Power power : tempPowers){
				if (dealPowerCountDict.get(power.getName()) == null){
					dealPowerCountDict.put(power.getName(), 1);
				}
				else{
					dealPowerCountDict.put(power.getName(), dealPowerCountDict.get(power.getName()) + 1);
				}
			}
			
			//TODO: decide whether this DMZ is acceptable or not (in combination with the rest of the proposed deal).
			/*
			List<Power> powers = dmz.getPowers();
			List<Province> provinces = dmz.getProvinces();
			*/

		}
		for(OrderCommitment orderCommitment : deal.getOrderCommitments()){
			
			
			// Sometimes we may receive messages too late, so we check if the proposal does not
			// refer to some round of the game that has already passed.
			if( isHistory(orderCommitment.getPhase(), orderCommitment.getYear())){
				outDated = true;
				break;
			}
			
			//TODO: decide whether this order commitment is acceptable or not (in combination with the rest of the proposed deal).
			Order order = orderCommitment.getOrder();
			if (!(order.getPower().equals(me))){
				if (dealPowerCountDict.get(order.getPower().getName()) == null){
					dealPowerCountDict.put(order.getPower().getName(), 1);
				}
				else{
					dealPowerCountDict.put(order.getPower().getName(), dealPowerCountDict.get(order.getPower().getName()) + 1);
				}
				if (personality.hasTrustIssuesWith(order.getPower())){
					trustIssues = true; 
				}
			}
		}
		
		
		//If the deal is not outdated, then check that it is consistent with the deals we are already committed to.
		String consistencyReport = null;
		if(!outDated){
		
			List<BasicDeal> commitments = new ArrayList<BasicDeal>();
			commitments.addAll(this.confirmedDeals);
			commitments.add(deal);
			consistencyReport = Utilities.testConsistency(game, commitments);
			
			
		}
		
		// CHECK ALL PROPOSALS`
		ArrayList<BasicDeal> commitments_temp = new ArrayList<BasicDeal>(this.confirmedDeals);
		commitments_temp.add(deal);
		Plan plan = this.dbraneTactics.determineBestPlan(game, me, commitments_temp , myAllies);
		
		double planVal = 0; // Negative? 
		
		//Check if the returned plan is better than the best plan found so far.
		if(plan != null){
			planVal = (double) plan.getValue();
		}
		
		double meanTrustVal = getMeanDealTrustVal(deal, dealPowerCountDict);
		double meanLikeVal = getMeanDealLikeVal(deal, dealPowerCountDict);
		
		double weight = 1.0; // TO BE CHANGED
		
		// GIVE more weight to proposer?   
		// Compute average throughout game and if more less 25% under, reject
		double dealVal = (planVal + (weight * meanLikeVal)) * meanTrustVal; 
		
		if (this.firstIncomingDeal){
			if (!(Double.isNaN(dealVal))){
				this.dealAcceptanceThreshold = dealVal * 1.25;
				this.firstIncomingDeal = false;
			}
			else{
				this.dealAcceptanceThreshold = 0;
			}
			return false;
		}
		
		
		if((!outDated) && (consistencyReport == null) && (dealVal > this.dealAcceptanceThreshold)){
			this.dealAcceptanceThreshold = this.dealAcceptanceThreshold * this.dealAcceptanceModifier;
			this.logger.logln("THRESHOLD: " + this.dealAcceptanceThreshold);
			return true;
			// This agent simply flips a coin to determine whether to accept the proposal or not.
			//if(random.nextInt(2) == 0){ // accept with 50% probability.
			//	return true;
			//}
		}
		this.dealAcceptanceThreshold = this.dealAcceptanceThreshold * this.dealRejectionModifier;
		this.logger.logln("THRESHOLD: " + this.dealAcceptanceThreshold);
		return false;
	}

	private double getMeanDealLikeVal(BasicDeal deal, Map<String, Integer> dealPowerCountDict) {
		double weightedAverage = 0;
		double sumOfCounts = 0;
		for (String powerName : dealPowerCountDict.keySet()){
			weightedAverage += dealPowerCountDict.get(powerName) * personality.getLikeabilityVal(powerName);
			sumOfCounts += dealPowerCountDict.get(powerName);
		}
		weightedAverage = weightedAverage / sumOfCounts;
		return weightedAverage;
	}

	private double getMeanDealTrustVal(BasicDeal deal, Map<String, Integer> dealPowerCountDict) {
		double weightedAverage = 0;
		double sumOfCounts = 0;
		for (String powerName : dealPowerCountDict.keySet()){
			weightedAverage += dealPowerCountDict.get(powerName) * personality.getTrustVal(powerName);
			sumOfCounts += dealPowerCountDict.get(powerName);
		}
		weightedAverage = weightedAverage / sumOfCounts;
		return weightedAverage;
	}
	
	public int getNumProposals(){
		
		if (this.personality.isType(PersonalityType.NEUTRAL)) return 1;
		else return random.nextInt(personality.getMaxProposals());

	}

	public ArrayList<String> checkAndRemoveInvalidDeals(){
		ArrayList<String> invalidDealsString = new ArrayList<String>();
		ArrayList<BasicDeal> invalidDeals = new ArrayList<BasicDeal>();
		for(BasicDeal confirmedDeal : confirmedDeals){
			if(Utilities.testValidity(game, confirmedDeal) != null){
				invalidDeals.add(confirmedDeal);
				invalidDealsString.add("play() Deal has become invalid: " + confirmedDeal);
			}
		}
		//Remove all invalid deals from the list of confirmed deals.
		confirmedDeals.removeAll(invalidDeals);
	return invalidDealsString;
	}
	
	
	public boolean testConsistancy(){
		String report = Utilities.testConsistency(game, confirmedDeals);
		if (report != null){
			return true;
		}
		return false;
	}
	//Should you accept a deal or not, returns TRUE if you want to accept this deal. FALSE if not. 
	public boolean acceptDeal(BasicDeal deal){
		boolean acceptDeal = false;
		
		//DO SOME STUFF TO CALCULATE IF THE DEAL IS RECJECTED OR ACCEPTED.
		
		
		return acceptDeal;
	}
	
	//Montecarlo stuff to search for a deal to propose. 
	public BasicDeal searchForADealToPropose(List<Power> myAllies){
		//THIS WILL NOT WORK. ADD THE MONTECARLO STUFF
		//BasicDeal proposedDeal = new BasicDeal(null, null);
		
		//GET STUFF FROM MONTECARLO AND PUT THEM INTO proposedDeal
	
		//return proposedDeal;
		BasicDeal bestDeal = null;
		Plan bestPlan = null;

		//Get a copy of our list of current commitments.
		ArrayList<BasicDeal> commitments = new ArrayList<BasicDeal>(this.confirmedDeals);
		
		//First, let's see what happens if we do not make any new commitments.
		bestPlan = this.dbraneTactics.determineBestPlan(game, me, commitments, myAllies);
		
		//If our current commitments are already inconsistent then we certainly
		// shouldn't make any more commitments.
		if(bestPlan == null){
			return null;
		}
		
		//let's generate 10 random deals and pick the best one.
		for(int i=0; i<10; i++){
			
			//generate a random deal.
			BasicDeal randomDeal = generateRandomDeal();
			
			//TODO
			// FRIEND is someone with > 1 attitude
			// NEGATIVE
			// Friend losing SC - Enemy gaining SC - Someone supporting enemy
			// POSTIVE
			// Friend gaining SC - Enemy losing SC - Someone supporting friend
			// Super positive 
			// We gain something
			// SUper negative
			// We lose something
			
			if(randomDeal == null){
				continue;
			}
			
			
			//add it to the list containing our existing commitments so that dBraneTactics can determine a plan.
			commitments.add(randomDeal);

			
			//Ask the D-Brane Tactical Module what it would do under these commitments.
			Plan plan = this.dbraneTactics.determineBestPlan(game, me, commitments, myAllies);
			
			//Check if the returned plan is better than the best plan found so far.
			if(plan != null && plan.getValue() > bestPlan.getValue()){
				bestPlan = plan;
				bestDeal = randomDeal;
			}
			
			
			//Remove the randomDeal from the list, for the next iteration.
			commitments.remove(commitments.size()-1);
			
			//NOTE: the value returned by plan.getValue() represents the number of Supply Centers that the D-Brane Tactical Module
			// expects to conquer in the current round under the given commitments.
			//
			// Of course, this is only a rough indication of which plan is truly the "best". After all, sometimes it is better
			// not to try to conquer as many Supply Centers as you can directly, but rather organize your armies and only attack in a later
			// stage.
			// Therefore, you may want to implement your own algorithm to determine which plan is the best.
			// You can call plan.getMyOrders() to retrieve the complete list of orders that D-Brane has chosen for you under the given commitments. 
			
		}
		
		
		return bestDeal;
		
	}
	
	//Checks to see if the propsal made is outdated or not. 
	boolean isHistory(Phase phase, int year){
		
		if(year == game.getYear()){
			return getPhaseValue(phase) < getPhaseValue(game.getPhase());
		}
		
		return year < game.getYear();
	}
	
	int getPhaseValue(Phase phase){
		
		switch (phase) {
		case SPR:
			return 0;
		case SUM:
			return 1;
		case FAL:
			return 2;
		case AUT:
			return 3;
		case WIN:
			return 4;
		default:
			return -1;
		}
	}
	
	public Plan determineBestPlan(List<Power> myAllies){
		if (!personality.getTrustworthiness()){
			if(random.nextInt(2) == 0){ // accept with 50% probability.
				ArrayList<BasicDeal> emptyConfirmedDeals = new ArrayList<BasicDeal>(); 
				return dbraneTactics.determineBestPlan(game, me, emptyConfirmedDeals, myAllies);
			}
		}
		// Once MC is implemented, we will call its engine here, passing our personality type
		return dbraneTactics.determineBestPlan(game, me, confirmedDeals, myAllies);
	}
	
	public String commitmentDebugger(){
		
		//THIS CODE BELOW IS JUST FOR DEBUGGING. 
		// Collect all OrderCommitments and Demilitarized Zones 
		// that we must obey the current turn and print them out in the log file.
		List<Order> committedOrders = new ArrayList<Order>();
		List<DMZ> demilitarizedZones = new ArrayList<DMZ>();
		for(BasicDeal deal : confirmedDeals){
			
			for(DMZ dmz : deal.getDemilitarizedZones()){
				
				if(dmz.getPhase().equals(game.getPhase()) && dmz.getYear() == game.getYear()){
					if(dmz.getPowers().contains(me)){
						demilitarizedZones.add(dmz);
					}
				}
			}
			
			for(OrderCommitment orderCommitment : deal.getOrderCommitments()){
				
				if(orderCommitment.getPhase().equals(game.getPhase()) && orderCommitment.getYear() == game.getYear()){
					if(orderCommitment.getOrder().getPower().equals(me)){
						committedOrders.add(orderCommitment.getOrder());
					}
				}
				
			}
		}
		return "Commitments to obey this turn: " + committedOrders + " " + demilitarizedZones;
	}
	
	public List<Order> getWinterOrders(List<Power> myAllies){
		return dbraneTactics.getWinterOrders(game, me, myAllies);
	}
	
	

public BasicDeal generateRandomDeal(){
	
	
	//Get the names of all the powers that are connected to the negotiation server (some players may be non-negotiating agents, so they are not connected.)
	
	//Make a copy of this list that only contains powers that are still alive.
	// (A power is dead when it has lost all its armies and fleet)
	List<Power> aliveNegotiatingPowers = new ArrayList<Power>(7);
	for(String powerName : negotiatingPowers){
		
		Power negotiatingPower = game.getPower(powerName);
		
		if( ! game.isDead(negotiatingPower)){
			aliveNegotiatingPowers.add(negotiatingPower);
		}
	}
	
	//if there are less than 2 negotiating powers left alive (only me), then it makes no sense to negotiate.
	int numAliveNegoPowers = aliveNegotiatingPowers.size();
	if(numAliveNegoPowers < 2){
		return null;
	}
	
	
	
	//Let's generate 3 random demilitarized zones.
	List<DMZ> demilitarizedZones = new ArrayList<DMZ>(3);
	for(int i=0; i<3; i++){
		
		//1. Create a list of powers
		ArrayList<Power> powers = new ArrayList<Power>(2);
		
		//1a. add myself to the list
		powers.add(me);
		
		//1b. add a random other power to the list.
		Power randomPower = me;
		while(randomPower.equals(me)){
			
			int numNegoPowers = aliveNegotiatingPowers.size();
			randomPower = aliveNegotiatingPowers.get(random.nextInt(numNegoPowers));
		}
		powers.add(randomPower);
		
		//2. Create a list containing 3 random provinces.
		ArrayList<Province> provinces = new ArrayList<Province>();
		for(int j=0; j<3; j++){
			int numProvinces = this.game.getProvinces().size();
			Province randomProvince = this.game.getProvinces().get(random.nextInt(numProvinces));
			provinces.add(randomProvince);
		}
		
		
		//This agent only generates deals for the current year and phase. 
		// However, you can pick any year and phase here, as long as they do not lie in the past.
		// (actually, you can also propose deals for rounds in the past, but it doesn't make any sense
		//  since you obviously cannot obey such deals).
		demilitarizedZones.add(new DMZ( game.getYear(), game.getPhase(), powers, provinces));

	}
	
	
	
	
	//let's generate 3 random OrderCommitments
	List<OrderCommitment> randomOrderCommitments = new ArrayList<OrderCommitment>();
	
	
	//get all units of the negotiating powers.
	List<Region> units = new ArrayList<Region>();
	for(Power power : aliveNegotiatingPowers){
		units.addAll(power.getControlledRegions());
	}
	
	
	for(int i=0; i<3; i++){
		
		//Pick a random unit and remove it from the list
		if(units.size() == 0){
			break;
		}
		Region randomUnit = units.remove(random.nextInt(units.size()));
		
		//Get the corresponding power
		Power power = game.getController(randomUnit);
		
		//Determine a list of potential destinations for the unit.
		// a Region is a potential destination for a unit if it is adjacent to that unit (or it is the current location of the unit)
		//  and the Province is not demilitarized for the Power controlling that unit.
		List<Region> potentialDestinations = new ArrayList<Region>();
		
		//Create a list of adjacent regions, including the current location of the unit.
		List<Region> adjacentRegions = new ArrayList<>(randomUnit.getAdjacentRegions());
		adjacentRegions.add(randomUnit);
		
		for(Region adjacentRegion : adjacentRegions){
			
			Province adjacentProvince = adjacentRegion.getProvince();
			
			//Check that the adjacent Region is not demilitarized for the power controlling the unit.
			boolean isDemilitarized = false;
			for(DMZ dmz : demilitarizedZones){
				if(dmz.getPowers().contains(power) && dmz.getProvinces().contains(adjacentProvince)){
					isDemilitarized = true;
					break;
				}
				
			}
			
			//If it is not demilitarized, then we can add the region to the list of potential destinations.
			if(!isDemilitarized){
				potentialDestinations.add(adjacentRegion);
			}
		}
		
		
		int numPotentialDestinations = potentialDestinations.size();
		if(numPotentialDestinations > 0){
			
			Region randomDestination = potentialDestinations.get(random.nextInt(numPotentialDestinations));
			
			Order randomOrder;
			if(randomDestination.equals(randomUnit)){
				randomOrder = new HLDOrder(power, randomUnit);
			}else{
				randomOrder = new MTOOrder(power, randomUnit, randomDestination);
			}
			// Of course we could also propose random support orders, but we don't do that here.
			
			
			//We only generate deals for the current year and phase. 
			// However, you can pick any year and phase here, as long as they do not lie in the past.
			// (actually, you can also propose deals for rounds in the past, but it doesn't make any sense
			//  since you obviously cannot obey such deals).
			randomOrderCommitments.add(new OrderCommitment(game.getYear(), game.getPhase(), randomOrder));
		}
		
	}
	
	BasicDeal deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);

	
	return deal;
	
}


public List<Order> generateRandomRetreats() {
	
	List<Order> orders = new ArrayList<Order>(game.getDislodgedRegions().size());
	int randomInt;
	
	HashMap<Region, Dislodgement> units = game.getDislodgedRegions();
	List<Region> dislodgedUnits = game.getDislodgedRegions(me);
	
	for (Region region : dislodgedUnits) {
		Dislodgement dislodgement = units.get(region);
		List<Region> dest = new ArrayList<Region>();

		dest.addAll(dislodgement.getRetreateTo());
		
		if (dest.size() == 0) {
			orders.add(new DSBOrder(region, me));
		}else{
			randomInt = random.nextInt(dest.size());
			orders.add(new RTOOrder(region, me, dest.get(randomInt)));			
		}
	}
		
		
	return orders;
}

//public ArrayList<Power> getAllies() {
//	ArrayList<Power> allies = new ArrayList<Power>();
	
	
//	return allies;
//}
	
}
