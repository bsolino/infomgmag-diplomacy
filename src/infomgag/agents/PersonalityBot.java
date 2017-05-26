package infomgag.agents;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import es.csic.iiia.fabregues.dip.Player;
import es.csic.iiia.fabregues.dip.board.Dislodgement;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.GameState;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.comm.CommException;
import es.csic.iiia.fabregues.dip.comm.IComm;
import es.csic.iiia.fabregues.dip.orders.DSBOrder;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.Order;
import es.csic.iiia.fabregues.dip.orders.RTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;
import es.csic.iiia.fabregues.dip.orders.SUPOrder;
import infomgag.decisionMaking.DecisionMaker;
import infomgag.personality.Personality;
import infomgag.personality.Personality.PersonalityType;
import es.csic.iiia.fabregues.dip.comm.daide.DaideComm;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.dbraneTactics.Plan;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.Deal;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Logger;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import ddejonge.negoServer.NegotiationClient.STATUS;


/**
 * This agent is an example that shows how you can implement a negotiating agent
 * on top of the strategic module of D-Brane.
 * 
 * 
 * @author Dave de Jonge
 *
 */
public class PersonalityBot extends Player{

	public static final int DEFAULT_GAME_SERVER_PORT = 16713;
	public static final int DEFAULT_NEGO_SERVER_PORT = 16714;
	
	//Unless specified otherwise in the command line, this agent will always propose a draw after this year.
	public static final int DEFAULT_FINAL_YEAR = 2000; 
	
	//The time in milliseconds this agent takes to negotiate each round.
	public final int NEGOTIATION_LENGTH = 3000; 
	
	
	/**
	 * Main method to start the agent.
	 * @param args
	 */
	public static void main(String[] args){
		
		
		//set the default name, game server port, and log path for the agent. 
		String name = "D-Brane Tactics Example Agent";
		String logPath = "log/";
		int gameServerPort = DEFAULT_GAME_SERVER_PORT;
		int negoPort = DEFAULT_NEGO_SERVER_PORT;
		int finalYear = DEFAULT_FINAL_YEAR;
		PersonalityType ps = PersonalityType.CHOLERIC; 		//Need to instatiate this variable, therefore CHOLERIC is now the default personality type. 
		//Overwrite these values if specified by the arguments.
		for(int i=0; i<args.length; i++){
			
			//set the name of this agent
			if(args[i].equals("-name") && args.length > i+1){
				name = args[i+1];
			}
			
			//set the path to store the log file
			if(args[i].equals("-log") && args.length > i+1){
				logPath = args[i+1];
			}
			
			//set the path to store the log file
			if(args[i].equals("-fy") && args.length > i+1){
				try{
					finalYear = Integer.parseInt(args[i+1]);
				}catch (NumberFormatException e) {
					System.out.println("main() The final year argument is not a valid integer: " + args[i+1]);
					return;
				}
			}
			
			//set the port number of the game server
			if(args[i].equals("-gamePort") && args.length > i+1){
				
				try{
					gameServerPort = Integer.parseInt(args[i+1]);
				}catch (NumberFormatException e) {
					System.out.println("main() The port number argument is not a valid integer: " + args[i+1]);
					return;
				}
			}
			
			//set the port number of the negotiation server
			if(args[i].equals("-negoPort") && args.length > i+1){
				
				try{
					negoPort = Integer.parseInt(args[i+1]);
				}catch (NumberFormatException e) {
					System.out.println("main() The port number argument is not a valid integer: " + args[i+1]);
					return;
				}
			}if(PersonalityType.valueOf(args[i])!=null){			//BROR thinks this works. .... we'll see,,,,,
				ps = PersonalityType.valueOf(args[i]);
			}
			
		}
		
		//Create the folder to store its log files.
		File logFolder = new File(logPath);
		logFolder.mkdirs();
		
		PersonalityBot exampleAgent = new PersonalityBot(name, finalYear, logPath, gameServerPort, negoPort, ps);
		
		//Connect to the game server.
		try{
			exampleAgent.start(exampleAgent.comm);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//Make sure the log file is written to hard disk when the agent is shut down.
		final PersonalityBot exAgent = exampleAgent;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	exAgent.logger.writeToFile();
	        }
	    }));
	}
	
	//FIELDS
	DBraneTactics dbraneTactics = new DBraneTactics();
	DecisionMaker decisionMaker;

	//Random number generator.
	private Random random = new Random();
	
	/**Client to connect with the game server.*/
	private IComm comm;
	
	/**To connect with the negotiation server.*/
	private DiplomacyNegoClient negoClient;
	
	/** After This year the agent will always propose a draw.*/
	int finalYear;
	
	
	/**
	 * For logging debug information to a log file.
	 * To log something to the log file, call:  
	 * 		logger.logln("some text to log");
	 * To log something to the log file and simultaneously print it to the standard output stream, call:  
	 * 		logger.logln("some text to log", true);
	 * 
	 * Note however, that calling logln only stores the text in temporary memory. It will not be written to 
	 *   the file on hard disk until you call logger.writeTofile().
	 *   
	 * Also note that the logger is by default disabled. In order to enable it you must first call logger.enable()
	 * which in this example is done in the init() method of the DBraneExampleBot.  
	 * 
	 * Furthermore, note that the Player class also defines a log field which should not be confused with this one.
	 * That logger is the logger provided by the
	 * DipGame framework which logs all the communication between game server and the agent.
	 * 
	 * */
	private Logger logger = new Logger();
	
	/**A list to store all deals that we are committed to.*/
	ArrayList<BasicDeal> confirmedDeals = new ArrayList<BasicDeal>();
	
	PersonalityBot(String name, int finalYear, String logPath, int gameServerPort, int negoServerPort, PersonalityType ps){
		super(logPath);
		
		this.name = name;
		this.finalYear = finalYear;
		this.decisionMaker = new DecisionMaker(new Personality(ps), game);
		
		//Initialize the clients
		try {
			InetAddress gameServerIp = InetAddress.getLocalHost();
			
			this.comm = new DaideComm(gameServerIp, gameServerPort, name);
			this.negoClient = new DiplomacyNegoClient(this, negoServerPort);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
				

		
	}
	
	/**
	 * This method is called once, at the start of the game, before the 'game' field is set.
	 * 
	 * It is called when the HLO message is received from the game server.
	 * The HLO contains information about the game such as the power assigned to you, and the deadlines.
	 * 
	 * The power you are playing is stored in the field 'me'.
	 * The game field will still be null when this method is called.
	 * 
	 * It is not necessary to implement this method.
	 */
	@Override
	public void init() {
		
		//enable logging at the specified path.
		logger.enable(logPath, this.me.getName() + ".log");
		
		//write our name and the power we are playing to the log file.
		logger.logln(this.name + " playing as " + this.me.getName(), true);
		logger.writeToFile();
		
		//Connect to the negotiation server.
		this.negoClient.connect();
		
		//Wait till we the connection with the server is established and we have received a start message from the Notary.
		this.negoClient.waitTillReady();
		
		if(this.negoClient.getStatus() == STATUS.READY){
			logger.logln(this.getClass().getSimpleName() + ".init() " + this.me.getName() + " Connection with negotiator correctly established. Ready to start negotiating!");
		}else{
			logger.logln(this.getClass().getSimpleName() + ".init() " + this.me.getName() + " connection failed! " + this.negoClient.getStatus(), true);
		}
		logger.writeToFile();
	
	}
	
	
	/**
	 * This method is called once, at the start of the game, when we have obtained the Game object.
	 * It is called after the first NOW message is received.
	 * (The NOW message contains the current phase and the positions of all the units.)
	 */
	@Override
	public void start() {
	}
	
	
	@Override
	public List<Order> play() {
		
		logger.logln();
		logger.logln("PHASE: " + game.getPhase() + " " + game.getYear());
		
		//The orders to return.
		ArrayList<Order> myOrders = new ArrayList<Order>();
		
	
		//list to be filled with all our allies. This means that we will not attack any supply center owned by any the powers in this list, 
		// and will assume that none of these powers will attack any supply center owned by us.
		//In this example we just create a fixed coalition structure. When implementing a real player you should of course come up with some smarter algorithm that
		// determines the coalition structure based on agreements made and supports given in previous rounds.  
		List<Power> myAllies = getAllies(game);
		
		
		//Test whether any of the deals we are committed to have become in valid.
		// A deal is invalid if it has an order for a unit that is not at that location.
		// e.g. There is an order for France to move an army from PIC to PAR, but FRA currently does not have an
		//  army at PIC.
		// This can happen if you make deals for future rounds, but the game develops in an unexpected manner.
		// e.g. you agree to move an army from BEL to PIC in the current round, and then to move from PIC to PAR 
		// in the next round. However, the move from BEL to PIC fails.
		ArrayList<BasicDeal> invalidDeals = new ArrayList<BasicDeal>();
		for(BasicDeal confirmedDeal : confirmedDeals){
			if(Utilities.testValidity(game, confirmedDeal) != null){
				invalidDeals.add(confirmedDeal);
				logger.logln("play() Deal has become invalid: " + confirmedDeal, true);
			}
		}
		//Remove all invalid deals from the list of confirmed deals.
		confirmedDeals.removeAll(invalidDeals);
		
		
		
		if(game.getPhase() == Phase.SPR || game.getPhase() == Phase.FAL){
			

			
			//MOVE PHASE
			//*1 ANALYZE the current world state. The results of the analysis are stored in a WorldState object. 
			//This object will next be used by determineBestPlan().
			
			
			//*2 NEGOTIATE!
			long negotiationDeadline = System.currentTimeMillis() + NEGOTIATION_LENGTH; //let's give the agent 3 seconds to negotiate.
			negotiate(myAllies, negotiationDeadline);
			
			
			//*3. DETERMINE BEST PLAN
			//Let the D-Brane Tactics module determine a plan that obeys the given deals.

			
			//First check whether the deals that we have negotiated are consistent.

			logger.logln();
			logger.logln("Confirmed Deals: " + confirmedDeals);
			
			// Test whether they are consistent. If yes, this method will return null. If not, it will return a string with an explanation what's wrong.
			// Note that in general, this will not happen because the Notary only confirms deals that are consistent with previously confirmed deals.
			// This is only required in case the consistency checking mechanism of the Notary has been turned off.
			String report = Utilities.testConsistency(game, confirmedDeals);
			
			if(report != null){
				
				//The confirmed deals are inconsistent! Print out the reason why.
				logger.logln(this.getClass().getSimpleName() + ".play() I am committed to inconsistent deals: " + report, true); 
			
			}else{
			
				//Now let the D-Brane Tactics module determine a plan of action that is consistent with the agreement.
				Plan plan = dbraneTactics.determineBestPlan(game, me, confirmedDeals, myAllies);
			
	
				if(plan == null){
					
					// If the D-Brane Tactics module returns null it means that it didn't manage to find a consistent plan.
					// Normally this should not happen because we have already checked that the agreements are consistent. 
					// Nevertheless, we take into account that this may happen, just in case there is a bug.
					
					logger.logln(this.getClass().getSimpleName() + ".play() " + this.me.getName() + " *** D-BraneTactics did not manage to find a plan obeying the following deals: " + confirmedDeals, true);
					
				}else{
					
					//if everything went okay dbraneTactics returned a Plan object
					// containing an order for each of our units, which are consistent with our commitments.
					
					//Add the orders of the plan to the list of orders we are going to return.
					myOrders.addAll(plan.getMyOrders());
					
					
					
						
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

					logger.logln("Commitments to obey this turn: " + committedOrders + " " + demilitarizedZones);
					
				}
			}
			
	
			
			//For any unit that, for whatever reason, still doesn't have an order, add a hold order.
			// (Normally, this should only be necessary in case dbraneTactics didn't return a plan because the commitments were
			//  inconsistent. However, we call this method anyway, just in case something went wrong.).
			myOrders = Utilities.addHoldOrders(me, myOrders);
			
			
			logger.logln("I am submitting: " + myOrders);
			logger.writeToFile();
			
			return myOrders;
			
		}else if(game.getPhase() == Phase.SUM || game.getPhase() == Phase.AUT){
			
			//RETREAT PHASE
			logger.writeToFile();
			return generateRandomRetreats();
			
		}else{
			
			//BUILD PHASE 
			logger.writeToFile();
			return dbraneTactics.getWinterOrders(game, me, myAllies);
		}
		
		
		
		
		
	}

	
	

	/**
	 * After each power has submitted its orders, this method is called several times: 
	 * once for each order submitted by any power.
	 * 
	 * You can use this to verify whether your allies have obeyed their agreements.
	 * 
	 * @param order An order submitted by any of the other powers.
	 */
	@Override
	public void receivedOrder(Order order) {
		decisionMaker.update(order);
	}
	
	
	List<Power> getAllies(Game game){
		
		ArrayList<Power> allies = new ArrayList<>(1);
		allies.add(me);
		
		//A real agent would use some algotithm to determine its allies. 
		//Here however, we just fill the list with 'me'.
		
		return allies;
		
	}
	
	private List<Order> generateRandomRetreats() {
		
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
	
	
	
	
	public void negotiate(List<Power> myAllies, long negotiationDeadline) {
		
		//This loop repeats 2 steps. The first step is to handle any incoming messages, 
		// while the second step tries to find deals to propose to the other negotiators.
		while(System.currentTimeMillis() < negotiationDeadline){
			
			//STEP 1: Handle incoming messages.
			//See if we have received any message from any of the other negotiators.
			// e.g. a new proposal or an acceptance of a proposal made earlier.
			while(negoClient.hasMessage()){
				
				//removes the message from the message que
				Message receivedMessage = negoClient.removeMessageFromQueue();
				//Asks for a string back for logging. 
				String handledMessageString = decisionMaker.handleIncomingMessages(receivedMessage);
				
				//THIS IS A COMPLETELY STUPID WAY TO HANDLE IT, BUT IT SHOULD WORK. MAYBE FIX THIS IN A LATER UPDATE. 
				if(handledMessageString.equals("Accepting proposal:" +(DiplomacyProposal)receivedMessage.getContent())){	//This means that the deal has been accepted, and the deal should be sent to the Notary. 
					this.negoClient.acceptProposal(((DiplomacyProposal) receivedMessage.getContent()).getId());
				} //There is going to be needed a consisty check here if the notary consistancy check will be turned off. 
				this.logger.logln(handledMessageString); 	//Logs the results of the handling of the message. 
			
			}
			//STEP 2:  try to find a proposal to make, and if we do find one, propose it.
			BasicDeal newDealToPropose = null;
			if(newDealToPropose == null){ //we only make one proposal per round, so we skip this if we have already proposed something.
				newDealToPropose = decisionMaker.searchForADealToPropose();
				if(newDealToPropose != null){
					
					try {
						this.logger.logln("RandomNegotiator.negotiate() Proposing: " + newDealToPropose);
						this.negoClient.proposeDeal(newDealToPropose);
	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
				this.logger.logln("No deals to propose");	
				}
			}
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * This method is automatically called after every phase. 
	 * 
	 * It is not necessary to implement it.
	 * 
	 * @param gameState
	 */
	@Override
	public void phaseEnd(GameState gameState) {
		
		//To prevent games from taking too long, we automatically propose a draw after
		// the FAL phase of the final year.
		if((game.getYear() == finalYear && game.getPhase() == Phase.FAL) || game.getYear() > finalYear){
			proposeDraw();
		}
		
	}
	
	
	/**
	 * You can call this method if you want to propose a draw.
	 * 
	 * If all players that are not yet eliminated propose a draw in the same phase, then
	 * the server ends the game.
	 * 
	 * Copy-paste this method into your own bot if you want it to be able to propose draws.
	 */
	void proposeDraw(){
		try {
			comm.sendMessage(new String[]{"DRW"});
		} catch (CommException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * This method is automatically called when the game is over.
	 * 
	 * The message contains about the names of the players, the powers they played and the 
	 * number of supply centers owned at the end of the game.
	 * 
	 */
	@Override
	public void handleSMR(String[] message) {
		
		//write the log file.
		this.logger.writeToFile();
		
		//disconnect from the game server.
		this.comm.stop();
		
		//disconnect from the negotiation server.
		this.negoClient.closeConnection();
		
		//Call exit to stop the player.
		exit();
	}
	
	
	/**
	 * This method is automatically called if you submit an illegal order for one of your units.
	 * 
	 * It is highly recommended to copy-paste this method into your own bot because it allows you to 
	 * see what went wrong if it accidentally submitted a wrong order.
	 * 
	 * @param message
	 */
	@Override
	public void submissionError(String[] message) {
		
		
		//[THX, (, (, AUS, AMY, BUD, ), MTO, MAR, ), (, FAR, )]
		
		if(message.length < 2){
			logger.logln(this.getClass().getSimpleName() + ".submissionError() " + Arrays.toString(message), true);
			return;
		}
		
		String errorType = message[message.length - 2];
		
		String illegalOrder = "";
		for(int i=2; i<message.length-4; i++){
			illegalOrder += message[i] + " ";
		}
		
		logger.logln(this.getClass().getSimpleName() + ".submissionError() Illegal order submitted: " + illegalOrder, true);
		
		switch (errorType) {
		case "FAR":
			logger.logln("Reason: Unit is trying to move to a non-adjacent region, or is trying to support a move to a non-adjacent region.", true);
			break;
		case "NSP":
			logger.logln("Reason: No such province.", true);
			break;
		case "NSU":
			logger.logln("Reason: No such unit.", true);
			break;
		case "NAS":
			logger.logln("Reason: Not at sea (for a convoying fleet)", true);
			break;
		case "NSF":
			logger.logln("Reason: No such fleet (in VIA section of CTO or the unit performing a CVY)", true);
			break;
		case "NSA":
			logger.logln("Reason: No such army (for unit being ordered to CTO or for unit being CVYed)", true);
			break;
		case "NYU":
			logger.logln("Reason: Not your unit", true);
			break;
		case "NRN":
			logger.logln("Reason: No retreat needed for this unit", true);
			break;
		case "NVR":
			logger.logln("Reason: Not a valid retreat space", true);
			break;
		case "YSC":
			logger.logln("Reason: Not your supply centre", true);
			break;
		case "ESC":
			logger.logln("Reason: Not an empty supply centre", true);
			break;
		case "HSC":
			logger.logln("Reason: Not a home supply centre", true);
			break;
		case "NSC":
			logger.logln("Reason: Not a supply centre", true);
			break;
		case "CST":
			logger.logln("Reason: No coast specified for fleet build in StP, or an attempt to build a fleet inland, or an army at sea.", true);
			break;
		case "NMB":
			logger.logln("Reason: No more builds allowed", true);
			break;
		case "NMR":
			logger.logln("Reason: No more removals allowed", true);
			break;
		case "NRS":
			logger.logln("Reason: Not the right season", true);
			break;
		default:
			
			logger.logln("submissionError() Received error message of unknown type: " + Arrays.toString(message), true);
			
			break;
		}
		
			//MBV means: Order is OK.

		
	}
	
	/**
	 * Returns true if the given phase and year are in the past with respect to the current phase and year of the game.
	 * @param phase
	 * @param year
	 * @return
	 */
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
