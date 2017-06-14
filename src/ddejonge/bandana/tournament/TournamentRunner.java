package ddejonge.bandana.tournament;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ddejonge.bandana.tools.Logger;
import ddejonge.bandana.tools.ProcessRunner;
import es.csic.iiia.fabregues.dip.Player;


public class TournamentRunner {
	
	//Command lines to start the various agents provided with the Bandana framework.
	// Add your own line here to run your own bot.
	private final static String DEFAULT_LAST_YEAR = "1905";
	
	private final static String[] randomNegotiatorCommand = {"java", "-jar", "agents/RandomNegotiator.jar", "-log", "log", "-name", "RandomNegotiator", "-fy", DEFAULT_LAST_YEAR};
	private final static String[] dumbBot_1_4_Command = {"java", "-jar", "agents/DumbBot-1.4.jar", "-log", "log", "-name", "DumbBot", "-fy", DEFAULT_LAST_YEAR};
	private final static String[] dbrane_1_1_Command = {"java", "-jar", "agents/D-Brane-1.1.jar", "-log", "log", "-name", "D-Brane", "-fy", DEFAULT_LAST_YEAR};
	private final static String[] dbraneExampleBotCommand = {"java", "-jar", "agents/D-BraneExampleBot.jar", "-log", "log", "-name", "DBraneExampleBot", "-fy", DEFAULT_LAST_YEAR};

	private final static String[] anacExampleBotCommand = {"java", "-jar", "agents/AnacExampleNegotiator.jar", "-log", "log", "-name", "AnacExampleNegotiator", "-fy", DEFAULT_LAST_YEAR};

	// Modify this list to change the order in which players are loaded
	// It's not foolproof: it has to be at least 7 players, and only the 7 first players will be used
	private final static TournamentPlayer[] TOURNAMENT_PLAYERS = {
//			TournamentPlayer.DBRANE_1_1,
//			TournamentPlayer.DBRANE_1_1,
//			TournamentPlayer.DBRANE_EXAMPLE,
//			TournamentPlayer.DBRANE_EXAMPLE,
//			TournamentPlayer.RANDOM_NEGOTIATOR,
//			TournamentPlayer.RANDOM_NEGOTIATOR,
//			TournamentPlayer.DUMB_BOT_1_4
			TournamentPlayer.PERSONALITY_NEUTRAL,
			TournamentPlayer.PERSONALITY_MELANCHOLIC,
			TournamentPlayer.PERSONALITY_SANGUINE,
			TournamentPlayer.PERSONALITY_PHLEGMATIC,
			TournamentPlayer.PERSONALITY_CHOLERIC,
			TournamentPlayer.PERSONALITY_SANGUINE,
			TournamentPlayer.PERSONALITY_PHLEGMATIC
	};
	
	//Main folder where all the logs are stored. For each tournament a new folder will be created inside this folder
	// where the results of the tournament will be logged.
	final static String LOG_FOLDER = "log";
	
	private enum TournamentPlayer{
		
		RANDOM_NEGOTIATOR("RandomNegotiator", randomNegotiatorCommand),
		DUMB_BOT_1_4("DumbBot", dumbBot_1_4_Command),
		DBRANE_1_1("D-Brane", dbrane_1_1_Command),
		DBRANE_EXAMPLE("D-BraneExampleBot", dbraneExampleBotCommand),
		ANAC_EXAMPLE("AnacExampleBot", anacExampleBotCommand),
		PERSONALITY_MELANCHOLIC("Personality MELANCHOLIC", new String[] {"java", "-jar", "agents/Personality.jar", "-log", "log", "-name", "MELANCHOLICPersonalityBot", "-fy", DEFAULT_LAST_YEAR, "-ps","MELANCHOLIC" ,"-mcts", "TRUE"}),
		PERSONALITY_SANGUINE("Personality SANGUINE", new String[] {"java", "-jar", "agents/Personality.jar", "-log", "log", "-name", "SANGUINEPersonalityBot", "-fy", DEFAULT_LAST_YEAR,"-ps", "SANGUINE","-mcts", "FALSE"}),
		PERSONALITY_PHLEGMATIC("Personality PHLEGMATIC", new String[] {"java", "-jar", "agents/Personality.jar", "-log", "log", "-name", "PHLEGMATICPersonalityBot", "-fy", DEFAULT_LAST_YEAR,"-ps", "PHLEGMATIC","-mcts", "FALSE"}),
		PERSONALITY_CHOLERIC("Personality CHOLERIC", new String[] {"java", "-jar", "agents/Personality.jar", "-log", "log", "-name", "CHOLERICPersonalityBot", "-fy", DEFAULT_LAST_YEAR,"-ps", "CHOLERIC","-mcts", "FALSE"}),
		PERSONALITY_NEUTRAL("Personality NEUTRAL", new String[] {"java", "-jar", "agents/Personality.jar", "-log", "log", "-name", "NEUTRALPersonalityBot", "-fy", DEFAULT_LAST_YEAR,"-ps", "NEUTRAL","-mcts", "FALSE"});

		private String name;
		private String[] command;
		
		private TournamentPlayer(String name, String[] command){
			this.name = name;
			this.command = command;
		}
		
		private String getName(){
			return this.name;
		}
		
		private String[] getCommand(){
			return this.command;
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		
		boolean displayInterface = true;		// if we're sure stuff runs well and just want logs, this will cut down on overhead.
				
		int numberOfGames = 1;				//The number of games this tournament consists of.
		
		int deadlineForMovePhases = 60; 	//60 seconds for each SPR and FAL phases
		int deadlineForRetreatPhases = 30;  //30 seconds for each SUM and AUT phases
		int deadlineForBuildPhases = 30;  	//30 seconds for each WIN phase
		
		int finalYear = Integer.parseInt(DEFAULT_LAST_YEAR); 	//The year after which the agents in each game are supposed to propose a draw to each other. 
		// (It depends on the implementation of the players whether this will indeed happen or not, so this may not always work.) 
		
		run(numberOfGames, deadlineForMovePhases, deadlineForRetreatPhases, deadlineForBuildPhases, finalYear, displayInterface);
		
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {

			//NOTE: unfortunately, Shutdownhooks don't work on windows if the program was started in eclipse and
			// you stop it by clicking the red button (on MAC it seems to work fine).
			
			@Override
			public void run() {
				NegoServerRunner.stop();
	        	ParlanceRunner.stop();
	        }
	    });
		
		
		
	}
	
	
	static List<Process> players = new ArrayList<Process>();
	
	public static void run(int numberOfGames, int moveTimeLimit, int retreatTimeLimit, int buildTimeLimit, int finalYear, boolean displayInterface) throws IOException{
		
		//Create a folder to store all the results of the tournament. 
		// This folder will be placed inside the LOG_FOLDER and will have the current date and time as its name.
		// You can change this line if you prefer it differently.
		String tournamentLogFolderPath = LOG_FOLDER + File.separator + Logger.getDateString();
		File logFile = new File(tournamentLogFolderPath);
		logFile.mkdirs();
		
		
 		//1. Run the Parlance game server.
		ParlanceRunner.runParlanceServer(numberOfGames, moveTimeLimit, retreatTimeLimit, buildTimeLimit);
		
		//Create a list of ScoreCalculators to determine how the players should be ranked in the tournament.
		ArrayList<ScoreCalculator> scoreCalculators = new ArrayList<ScoreCalculator>();
		scoreCalculators.add(new SoloVictoryCalculator());
		scoreCalculators.add(new SupplyCenterCalculator());
		scoreCalculators.add(new PointsCalculator());
		scoreCalculators.add(new RankCalculator());
		
		//2. Create a TournamentObserver to monitor the games and accumulate the results.
		TournamentObserver tournamentObserver = new TournamentObserver(tournamentLogFolderPath, scoreCalculators, numberOfGames, 7, displayInterface);
		
		//3. Run the Negotiation Server.
		NegoServerRunner.run(tournamentObserver, tournamentLogFolderPath, numberOfGames);
		
		for(int gameNumber=1; gameNumber<=numberOfGames; gameNumber++){
			
			System.out.println();
			System.out.println("GAME " + gameNumber);
			
			NegoServerRunner.notifyNewGame(gameNumber);
			
			//4. Start the players:
			for(int i=0; i<7; i++){
				
				TournamentPlayer tournamentPlayer = TOURNAMENT_PLAYERS[i];

				//make sure that each player has a different name
				String name = tournamentPlayer.getName() + " " + i;
				String[] command = tournamentPlayer.getCommand();
				//set the log folder for this agent to be a subfolder of the tournament log folder.
				command[4] = tournamentLogFolderPath + File.separator + name + File.separator + "Game " + gameNumber + File.separator; 
				
				//set the name of the agent.
				command[6] = name; 
				
				//set the year after which the agent will propose a draw to the other agents.
				command[8] = "" + finalYear; 
				
				//start the process
				String processName = name;
				Process playerProcess = ProcessRunner.exec(command, processName);
				// We give  a name to the process so that we can see in the console where its output comes from. 
				// This name does not have to be the same as the name given to the agent, but it would be confusing
				// to do otherwise.
				
				
				//store the Process object in a list.
				players.add(playerProcess);
				
				
			}
			
			//5. Let the tournament observer (re-)connect to the game server.
			tournamentObserver.connectToServer();
			
			
			
			//NOW WAIT TILL THE GAME IS FINISHED
			while(tournamentObserver.getGameStatus() == TournamentObserver.GAME_ACTIVE || tournamentObserver.getGameStatus() == TournamentObserver.CONNECTED_WAITING_TO_START ){
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				if(tournamentObserver.playerFailed()){
					// One or more players did not send its orders in in time.
					// 
				}
				
			}
			
			//Kill the player processes.
			// (if everything is implemented okay this isn't necessary because the players should kill themselves. But just to be sure..)
			for(Process playerProces : players){
				playerProces.destroy();
			}
			
		}
		
		System.out.println("TOURNAMENT FINISHED");
		
		//Get the results of all the games played in this tournament.
		// Each GameResult object contains the results of one game.
		// The tournamentObserver already automatically prints these results to a text file,
		//  as well as the processed overall results of the tournament.
		// However, you may want to do your own processing of the results, for which
		// you can use this list.
		ArrayList<GameResult> results = tournamentObserver.getGameResults();
		for(GameResult result : results){
		System.out.println(result.toString());	
		}
		
		tournamentObserver.exit();
		ParlanceRunner.stop();
		NegoServerRunner.stop();
	}
	
}
