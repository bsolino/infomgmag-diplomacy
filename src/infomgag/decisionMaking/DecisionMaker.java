package infomgag.decisionMaking;
import es.csic.iiia.fabregues.dip.orders.Order;
import java.util.ArrayList;

import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.negoServer.Message;
import infomgag.personality.*;
import infomgag.personality.Personality.DealEffect;
public class DecisionMaker{
	
	Personality personality; 
	ArrayList<BasicDeal> currentDeals; 
	//WHAT MORE DO WE NEED
	//A list of commitments
	//
	
	public DecisionMaker(Personality personality){
		this.personality = personality;
		this.currentDeals = new ArrayList<BasicDeal>();
	}
	
	
	//This is gonna get called every time recievedOrder is called. 6 players * the number of is issued by each player. 
	public void update(Order order){
	//IMPLEMENT THE FUNCTION BELOW. 
	DealEffect tempEffect = calculateDealEffect(order); 
	personality.updateTrust(order.getPower().getName(), tempEffect);
	}

	//Checks if someone has screwed you over or not. 
	private DealEffect calculateDealEffect(Order order) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void handleIncomingMessages(Message receivedMessage){
		//Check if deal is outdated
		
		//Then handle the various possible messages: PROPOSE, ACCEPT, REJECT and CONFIRM
		
		switch(receivedMessage.getPerformative()){
		case DiplomacyNegoClient.PROPOSE:			//A player has proposed a deal to you
			break;
		case DiplomacyNegoClient.ACCEPT:			//A player has accepted a deal you have proposed
			break;
		case DiplomacyNegoClient.REJECT:			//A player has rejected a deal you have proposed
			break;
		case DiplomacyNegoClient.CONFIRM:			//The negotiation client has confirmed that all players in a deal has accepted the deal. This is now binding. 
			break;
		}
		
		
		
	}
	
	
	
	public boolean acceptDeal(BasicDeal deal){
		boolean acceptDeal = false;
		
		//DO SOME STUFF TO CALCULATE IF THE DEAL IS RECJECTED OR ACCEPTED.
		
		
		return acceptDeal;
	}
	
	public ArrayList<BasicDeal> proposeDeals(){
		ArrayList<BasicDeal> proposedDeals = new ArrayList<BasicDeal>();
		
		//GET STUFF FROM MONTECARLO AND PUT THEM INTO proposedDeals
	
		return proposedDeals;
	}
	

}
