package infomgag.decisionMaking;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.orders.Order;
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
import infomgag.personality.Personality.DealEffect;
public class DecisionMaker{
	
	Personality personality; 
	ArrayList<BasicDeal> confirmedDeals; 
	Game game; 
	Random random;
	
	//Constructor: Takes in a personality and a game object. 
	public DecisionMaker(Personality personality, Game game){
		random = new Random();
		this.game = game;
		this.personality = personality;
		this.confirmedDeals = new ArrayList<BasicDeal>();
	}
	
	//This is gonna get called every time recievedOrder is called. 6 players * the number of is issued by each player. 
	public void update(Order order){
	//IMPLEMENT THE FUNCTION BELOW. 
	DealEffect tempEffect = calculateDealEffect(order); 
	personality.updateTrust(order.getPower().getName(), tempEffect);		//Updates the personality dependant on if you were screwed over or not in the last round. 
	}

	//Checks if someone has screwed you over or not. 
	private DealEffect calculateDealEffect(Order order) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//Handles an incomming message, this could be a reject, accept, confirm or propose message from another player
	public String handleIncomingMessages(Message receivedMessage){
		//Check if deal is outdated
		DiplomacyProposal proposal = (DiplomacyProposal)receivedMessage.getContent();		
		//Then handle the various possible messages: PROPOSE, ACCEPT, REJECT and CONFIRM
		
		switch(receivedMessage.getPerformative()){
		
		case DiplomacyNegoClient.PROPOSE:			//A player has proposed a deal to you
			BasicDeal deal = (BasicDeal)proposal.getProposedDeal();
			if(handleProposal(deal)){		//If you choose to accept the deal. Then handleProposal should be TRUE, else FALSE
				return "Accepting proposal:" + proposal;
			}
		case DiplomacyNegoClient.ACCEPT:			//A player has accepted a deal you have proposed
			//Handle the information that another player has accepted a deal you proposed to them
			return "RandomNegotiator.negotiate() Received acceptance from " + receivedMessage.getSender() + ": " + proposal;
		case DiplomacyNegoClient.REJECT:			//A player has rejected a deal you have proposed
			//Handled the information that another player has accepted a deal you propoed to them
			return "Deal rejected: " + proposal;
		case DiplomacyNegoClient.CONFIRM:			//The negotiation client has confirmed that all players in a deal has accepted the deal. This is now binding. 
			
			BasicDeal tempDeal = (BasicDeal)proposal.getProposedDeal();
			handleConfirmation(tempDeal);
			
			
			break;
		}
		
		return "UNKOWN INCOMING MESSAGE";
		
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
			/*Order order = orderCommitment.getOrder();*/
		}
		
		//If the deal is not outdated, then check that it is consistent with the deals we are already committed to.
		String consistencyReport = null;
		if(!outDated){
		
			List<BasicDeal> commitments = new ArrayList<BasicDeal>();
			commitments.addAll(this.confirmedDeals);
			commitments.add(deal);
			consistencyReport = Utilities.testConsistency(game, commitments);
			
			
		}
		
		if(!outDated && consistencyReport == null){
			
			// This agent simply flips a coin to determine whether to accept the proposal or not.
			if(random.nextInt(2) == 0){ // accept with 50% probability.
				return true;
			}
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
		BasicDeal proposedDeal = new BasicDeal(null, null);
		
		//GET STUFF FROM MONTECARLO AND PUT THEM INTO proposedDeal
	
		return proposedDeal;
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
