package infomgag.monteCarloDealEvaluator;

import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import ddejonge.bandana.exampleAgents.RandomBot;
import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import ddejonge.bandana.internalAdjudicator.InternalAdjudicator;
import es.csic.iiia.fabregues.dip.board.Dislodgement;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.Order;
import javafx.util.Pair;

public class Gamestate {
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
	
	public Pair<TreeSet<String>, Gamestate> randomMove(InternalAdjudicator adjudicator)
	{
		Game g = buildGameFromGamestate();
		RandomBot bot = new RandomBot(g);
		List<Order> orders = bot.getRandomMovesForEachPower();
		
		TreeSet<String> morders = new TreeSet<String>();
		for(Order o : orders) morders.add(o.toString());
		
		adjudicator.clear();
		adjudicator.resolve(g, orders);
		
		return new Pair(morders, this); // TODO: generate new gamestate from orders
	}
	
	
	public String toString(){
		return "State:" + this.hashCode() + " Year:" + this.year + 
				" Phase:" + this.phase + " Units:" + units.toString();
	}

}
