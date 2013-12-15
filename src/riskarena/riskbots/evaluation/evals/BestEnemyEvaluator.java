package riskarena.riskbots.evaluation.evals;
/*
 * Uses a heuristic function to score each enemy player, and returns the largest such score
 * Makes it negative, since a higher enemy score is bad
 */

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.PlayerInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;

public class BestEnemyEvaluator extends AbstractEvaluator {
	private double score;
	private PlayerInfo[] players;
	
	public BestEnemyEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		refresh();
	}
	
	public double getScore() {
		return score;
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		return recalculate();
	}
	
	public void refresh() {
		players = stats.getPlayers();
		score = recalculate();
	}
	
	private double recalculate() {
		double highestScore = Double.MIN_VALUE;
		for(int i=0; i<players.length; i++) {
			if(players[i].getId() == game.me())
				continue;
			double enemyScore = rateEnemy(players[i].getId());
			if(enemyScore > highestScore)
				highestScore = enemyScore;
		}
		return -1 * highestScore;
	}
	
	private double rateEnemy(int player_id) {
		double armyRatio = 0.0, territoryRatio = 0.0;
		if(stats.getTotalArmies() != 0)
			armyRatio = stats.getArmiesPerPlayer()[player_id] / (double)stats.getTotalArmies();
		if(game.getNumContinents() != 0)
			territoryRatio = stats.getOccupationCounts()[player_id] / (double)game.getNumCountries();
		double enemyScore = (armyRatio + territoryRatio) / 2.0;
		return enemyScore;
	}

}
