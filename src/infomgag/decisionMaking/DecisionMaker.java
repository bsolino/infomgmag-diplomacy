package infomgag.decisionMaking;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.Order;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import infomgag.personality.*;
import infomgag.personality.Personality.Effect;
public class DecisionMaker{
	
	Personality personality; 
	ArrayList<BasicDeal> confirmedDeals; 
	Game game; 
	Random random;
	Power me;
	
	
	//Constructor: Takes in a personality and a game object. 
	public DecisionMaker(Personality personality, Game game, Power me){
		random = new Random();
		this.game = game;
		this.personality = personality;
		this.confirmedDeals = new ArrayList<BasicDeal>();
		this.me = me;
		personality.setMyPower(me);
		personality.setPowers(game.getPowers());
		
	}
	
	public String getPersonalityValues(){
		return personality.getTrustValues();
	}
	
	public void update(ArrayList<Order> submittedOrders){
		//However, if the order
		//is a HLDOrder, then the corresponding power is still allowed to submit a
		//SUPOrder or SUPMTOOrder for that unit instead of the HLDOrder.
		
		if (submittedOrders == null){
			return;
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
						// check whether the move order causes us to lose territory
						//personality.updateLikeability(order.getPower().getName(), Effect.NEGATIVE);
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

	//Checks if someone has screwed you over or not. 
	private Effect calculateDealEffect(Order order) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//Handles an incomming message, this could be a reject, accept, confirm or propose message from another player
	public String handleIncomingMessages(Message receivedMessage){
		//Check if deal is outdated
		DiplomacyProposal proposal;
		//Then handle the various possible messages: PROPOSE, ACCEPT, REJECT and CONFIRM
		
		switch(receivedMessage.getPerformative()){
		
		case DiplomacyNegoClient.PROPOSE:			//A player has proposed a deal to you
			proposal  = (DiplomacyProposal)receivedMessage.getContent();	
			BasicDeal deal = (BasicDeal)proposal.getProposedDeal();
			if(handleProposal(deal)){		//If you choose to accept the deal. Then handleProposal should be TRUE, else FALSE
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
	private boolean handleProposal(BasicDeal deal) {
		boolean outDated = false;
		boolean trustIssues = false;
		
		for(DMZ dmz : deal.getDemilitarizedZones()){
			
			// Sometimes we may receive messages too late, so we check if the proposal does not
			// refer to some round of the game that has already passed.
			if( isHistory(dmz.getPhase(), dmz.getYear())){
				outDated = true;
				break;
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
		// Maybe check whether or not we trust the MAJORITY of powers involved in the deal
		if(!outDated && consistencyReport == null && !trustIssues){
			return true;
			// This agent simply flips a coin to determine whether to accept the proposal or not.
			//if(random.nextInt(2) == 0){ // accept with 50% probability.
			//	return true;
			//}
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
	public BasicDeal searchForADealToPropose(){
		//THIS WILL NOT WORK. ADD THE MONTECARLO STUFF
		//BasicDeal proposedDeal = new BasicDeal(null, null);
		
		//GET STUFF FROM MONTECARLO AND PUT THEM INTO proposedDeal
	
		//return proposedDeal;
		return null;
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
	
}
