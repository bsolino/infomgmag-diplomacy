package ddejonge.bandana.tournament;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ScoreCalculator {

	boolean higherIsBetter;
	
	protected HashMap<String, Integer> names2numGamesPlayed = new HashMap<String, Integer>();
	protected HashMap<String, ArrayList<Double>> names2totalScore = new HashMap<String, ArrayList<Double>>();
	
	/**
	 * 
	 * @param higherIsBetter if this parameter is true, then the player that scores the highest value is ranked highest. 
	 * If this parameter is false, then the player that scores the lowest value will be ranked highest.
	 * 
	 */
	public ScoreCalculator(boolean higherIsBetter){
		this.higherIsBetter = higherIsBetter;
	}
	
	/**
	 * This method is called after every finished game. 
	 * It will get the score for each player by calling calculateGameScore() and adds this score to that player's total.
	 * 
	 * 
	 * 
	 * @param newResult
	 */
	public void addResult(GameResult newResult){
		
		for(String playerName : newResult.getNames()){
			
			increaseNumberOfGamesPlayed(playerName);
			
			double score = calculateGameScore(newResult, playerName);
			
			addScoreToTotal(playerName, score);
			
		}
	}
	
	protected void increaseNumberOfGamesPlayed(String playerName){
		
		//get the old value from the table
		Integer numGamesPlayed = getNumberOfGamesPlayed(playerName);
		
		//increment the value.
		numGamesPlayed++;
		
		//store the new value in the table.
		names2numGamesPlayed.put(playerName, numGamesPlayed);
	}
	
	private void addScoreToTotal(String playerName, double score){
		
		//get the old value from the table
		ArrayList<Double> playerScores = names2totalScore.get(playerName);
		
		// handle init.
		if (playerScores == null){
			playerScores = new ArrayList<Double>();
		}
		
		//add the newest score
		playerScores.add(score);
		
		//store the new value in the table.
		names2totalScore.put(playerName, playerScores);
	}
	
	

	
	public int getNumberOfGamesPlayed(String playerName){
		
		Integer numGamesPlayed = names2numGamesPlayed.get(playerName);

		//if it wasn't in the table then set it to 0
		if(numGamesPlayed == null){
			return 0;
		}
		
		return numGamesPlayed;
	}
	
	public double getTotalScore(String playerName){
		
		//get the value from the table
		ArrayList<Double> playerScores = names2totalScore.get(playerName);
		
		//if it wasn't in the table then return 0.0
		if(playerScores == null){
			return 0.0;
		}
		
		// Otherwise, compute the total.
		double totalScore = 0;
		
		for(double score : playerScores)
			totalScore += score;
		
		return totalScore;
	}
	
	public double getAverageScore(String playerName){
		
		double totalScore = getTotalScore(playerName);
		int numGamesPlayed = getNumberOfGamesPlayed(playerName);
		if(numGamesPlayed == 0)
			return 0;
		
		return totalScore / (double)numGamesPlayed;
		
	}
	
	public double getSDForScore(String playerName){
		
		int numGamesPlayed = getNumberOfGamesPlayed(playerName);
		
		// Catch errored games.
		if(numGamesPlayed == 0)
			return 0;
		
		ArrayList<Double> playerScores = names2totalScore.get(playerName);
		
		// Get mean
		double mean = getAverageScore(playerName);
		
		// Calculate sum of squares
		double ssq = 0;
		
		for (double score : playerScores){
			// Substracts mean from number, square the result.
			//
			ssq += ((score - mean) * (score - mean));
		}
		
		// turn ssq into variance
		ssq = ssq / (numGamesPlayed - 1);
		
		// take square root for sd.
		return Math.sqrt(ssq);
	}
	
	public List<Double> getScoreArray(String playerName){
		
		return names2totalScore.get(playerName);
		
	}
	
	/**
	 * Calculates the score of the given player for the given game.
	 * @param newResult
	 * @param name
	 * @return
	 */
	public abstract double calculateGameScore(GameResult newResult, String playerName);
	
	/**
	 * Returns the overall score of the given player for the entire tournament. <br/>
	 * The TournamentObserver will sorts the players based on to the values returned by this method.
	 * @param name
	 * @return
	 */
	public abstract double getTournamentScore(String playerName);
	
	/**
	 * Returns the name of this score system.
	 * @return
	 */
	public abstract String getScoreSystemName();
	
	/**
	 * Returns the string to display in the table of the TournamentObserver.
	 * @param name
	 * @return
	 */
	public abstract String getScoreString(String playerName);
}
