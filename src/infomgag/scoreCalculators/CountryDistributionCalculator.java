package infomgag.scoreCalculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ddejonge.bandana.tournament.GameResult;
import ddejonge.bandana.tournament.ScoreCalculator;

public class CountryDistributionCalculator extends ScoreCalculator{

	private HashMap<String, Integer> power2ScorePosition = new HashMap<String, Integer>();
	
	public CountryDistributionCalculator() {
			super(true);
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
			
			// We are calculating the score somewhat differently.
			calculateGameScore(newResult, playerName);
			
		}
	}
	
	@Override
	public double calculateGameScore(GameResult newResult, String playerName){
		
		// Get current values.
		ArrayList<Double> playerScores = names2totalScore.get(playerName);
		
		// Catch init if required
		if (playerScores == null){
			playerScores = new ArrayList<Double>(Arrays.asList(0.0,0.0,0.0,0.0,0.0,0.0,0.0));
		}
		
		int powerPosition = getPowerPosition(newResult.getPowerPlayed(playerName));
		Double powerCount;
		
		powerCount = playerScores.get(powerPosition);
		powerCount++;
		playerScores.set(powerPosition, powerCount);
		
		names2totalScore.put(playerName, playerScores);
		
		// Return value not actually used, but required to satisfy ScoreCalculator extension.
		return 0.0;
	}
	
	public int getPowerPosition(String powerName){
		
		Integer powerPosition = power2ScorePosition.get(powerName);
		
		// If it wasn't in the table, return the next position number.
		if (powerPosition == null){
			
			int nextPos = power2ScorePosition.size();
			
			power2ScorePosition.put(powerName, nextPos);
			
			return nextPos;
		}
		
		return powerPosition;
	}

	@Override
	public double getTournamentScore(String playerName) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public String getScoreSystemName() {
		
		return "Country_Distribution";
	}

	@Override
	public String getScoreString(String playerName) {
		String s = "";
		
		if (playerName == "powers"){
			
			s += "Country_Distribution, TitleLine";
			
			for (String power : getCountryOrderArray()){
				s += ", " + power;
			}
					
			return s;
			
		} else {
			
			return s;
		}
	}
	
	public double getTotalScore(String playerName){
		
		return 0.0;
	}
	
	public double getAverageScore(String playerName){
		
		return 0.0;
		
	}
	
	public double getSDForScore(String playerName){
		
		return 0.0;
	}
	
	public Set<String> getCountryOrderArray(){
		
		return power2ScorePosition.keySet();
	}
	
}

/*
package ddejonge.bandana.tournament;


import ddejonge.negoServer.Utils;

public class SoloVictoryCalculator extends ScoreCalculator{

	public SoloVictoryCalculator() {
		super(true);
	}
	
	@Override
	public double calculateGameScore(GameResult newResult, String playerName) {
		
		if(newResult.endedInSolo() && newResult.getSoloWinner().equals(playerName) ){
			return 1.0;
		}else{
			return 0.0;
		}
		
	}

	@Override
	public double getTournamentScore(String playerName) {
		return this.getAverageScore(playerName);
	}

	@Override
	public String getScoreSystemName() {
		return "Solo Victories";
	}

	@Override
	public String getScoreString(String playerName) {
		
		long total = Math.round(this.getTotalScore(playerName));
		double average = Utils.round(this.getAverageScore(playerName), 3);
		double sd = Utils.round(this.getSDForScore(playerName), 3);
		
		return "" + total + " (av. = "+ average + " sd = " + sd + ")";
		
	}


}
*/