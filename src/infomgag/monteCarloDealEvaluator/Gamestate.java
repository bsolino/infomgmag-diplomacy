package infomgag.monteCarloDealEvaluator;

import java.util.Vector;

import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.comm.GameBuilder;
import javafx.util.Pair;

public class Gamestate {
	int year;
	String phase;
	Vector<Pair<String, String>> units = new Vector<Pair<String, String>>(),
			regions = new Vector<Pair<String, String>>();
	
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
	}
	
	public Game buildGameFromGamestate()
	{
		DiplomacyGameBuilder gameBuilder = new DiplomacyGameBuilder();
		gameBuilder.setPhase(Phase.valueOf(phase), year);
		for(Pair<String, String> region : regions)
			gameBuilder.setOwner(region.getKey(), region.getValue());
		for(Pair<String, String> unit : units)
			gameBuilder.placeUnit(unit.getKey(), unit.getValue());
		return gameBuilder.createMyGame();
	}
	
	//This should return the number of possible moves in this state
	//it is used to know if nodes are fully expanded
	public int possibleMoves(){
		//TODO
		return 2;
	}
	
	public String toString(){
		return "State:" + this.hashCode() + " Year:" + this.year + 
				" Phase:" + this.phase + " Units:" + units.toString();
	}

}
