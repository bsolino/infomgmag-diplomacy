package infomgag.monteCarloDealEvaluator;

import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import ddejonge.bandana.exampleAgents.RandomBot;
import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import ddejonge.bandana.internalAdjudicator.InternalAdjudicator;
import ddejonge.bandana.tournament.TournamentObserver;
import es.csic.iiia.fabregues.dip.Observer;
import es.csic.iiia.fabregues.dip.Player;
import es.csic.iiia.fabregues.dip.board.Dislodgement;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.GameState;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.board.State;
import es.csic.iiia.fabregues.dip.comm.Comm;
import es.csic.iiia.fabregues.dip.comm.CommException;
import es.csic.iiia.fabregues.dip.comm.OrderParser;
import es.csic.iiia.fabregues.dip.comm.Parser;
import es.csic.iiia.fabregues.dip.comm.StringA2Order;
import es.csic.iiia.fabregues.dip.comm.daide.DaideComm;
import es.csic.iiia.fabregues.dip.orders.Order;
import javafx.util.Pair;

public class Gamestate {
	public static DaideComm comm;
	
	int year;
	String phase;
	Vector<Pair<String, String>> units = new Vector<Pair<String, String>>(),
			regions = new Vector<Pair<String, String>>();
	Vector<Pair<Pair<String,String>,Vector<String>>> dislodgements = new Vector<Pair<Pair<String,String>,Vector<String>>>();
	
	@SuppressWarnings("unchecked")
	public Gamestate(Game game)
	{
		year = game.getYear();
		phase = game.getPhase().name();
		for(Province p : game.getProvinces())
		{
			Power controller = game.getController(p);
			if(controller != null)
				regions.add(new Pair(controller.getName(), p.getName()));
		}
		
		for(Power p : game.getPowers())
		{
			String pname = p.getName();
			for(Region r : p.getControlledRegions())
			regions.add( new Pair(pname, r.getName()));
		}
		
		for(Dislodgement d : game.getDislodgedRegions().values())
		{
			Pair<String, String> dislodgement = new Pair(d.getPower().getName(),d.getRegion().getName());
			Vector<String> options = new Vector<String>();
			for(Region r : d.getRetreateTo())
				options.add(r.getName());
			dislodgements.add(new Pair(dislodgement, options));
		}
	}
	
	public Game buildGameFromGamestate()
	{
		DiplomacyGameBuilder gameBuilder = new DiplomacyGameBuilder();
		gameBuilder.setPhase(Phase.valueOf(phase), year);
		for(Pair<String, String> region : regions)
			gameBuilder.setOwner(region.getKey(), region.getValue());
		for(Pair<String, String> unit : units)
			gameBuilder.placeUnit(unit.getKey(), unit.getValue());
		Game mygame = gameBuilder.createMyGame();
		
		for(Pair<Pair<String,String>,Vector<String>> dislodgement : dislodgements)
		{
			Power p = mygame.getPower(dislodgement.getKey().getKey());
			Region r = mygame.getRegion(dislodgement.getKey().getValue());
			Dislodgement d = new Dislodgement(p,r);
			for(String s : dislodgement.getValue())
				d.addRetreateToRegion(mygame.getRegion(s));
			mygame.addDislodgedRegion(r, d);
		}
		
		return mygame;
	}
	
	public Pair<TreeSet<String>, Gamestate> randomMove()
	{
		Game g = buildGameFromGamestate();
		RandomBot bot = new RandomBot(g);
		List<Order> orders = bot.getRandomMovesForEachPower();
		
		TreeSet<String> morders = new TreeSet<String>();
		for(Order o : orders)
			morders.add(o.toString());
		
		StringA2Order orderParser = new StringA2Order(g);
		orderParser.processOrders(Parser.formatOrders(orders), g);
		
		Parser.updateControlledRegions(Parser.getNOW(g), g);
		
		if(phase.equals("FAL"))Parser.updateOwnedSCs(Parser.getSCO(g), g);
		//Parser.updateOwnedSCs(Parser.getSCO(g), g); // sco message
		//Parser.updateControlledRegions(Parser.getNOW(g), g); // some other message
		return new Pair<TreeSet<String>, Gamestate>(morders, new Gamestate(g));
	}
	
	
	public String toString(){
		return "State:" + this.hashCode() + " Year:" + this.year + 
				" Phase:" + this.phase + " Units:" + units.toString();
	}
	
	private static void KillDeadPowers(Game game)
	{
		List<Power> nonDeadPowers = game.getNonDeadPowers();
		for(int i= 0; i<nonDeadPowers.size(); i++){
			Power power = nonDeadPowers.get(i);
			if(power.getOwnedSCs().size()==0){
				game.killPower(power);
				i--;
			}
		}
	}
}
