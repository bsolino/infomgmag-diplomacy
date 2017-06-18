package infomgag.monteCarloDealEvaluator;

import java.io.IOException;
import java.util.TreeSet;

import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.comm.GameBuilder;
import javafx.util.Pair;

public class MonteCarloTreeBuilder {
	public static void main(String[] args) throws IOException {
		DiplomacyGameBuilder gameBuilder = new DiplomacyGameBuilder();
		Game game = gameBuilder.createDefaultGame();
		Gamestate root = new Gamestate(game);
		Pair<TreeSet<String>, Gamestate> move = root.randomMove();
	}
	
	private static void LoadTreeFromDisk()
	{
	}
	
	private static void SaveTreeToDisk()
	{
	}
}
