package riskarena.riskbots.evaluation;

import riskarena.GameInfo;
/*
 * The OccurpiedTerritories measures how many territories the player owns.
 * It returns this number divided by the total number of territories on the board.
 */


public class OccupiedTerritoriesEvaluator extends AbstractEvaluator {
	private double score;
	
	public OccupiedTerritoriesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		score = stats.getOccupationCounts()[stats.me()] / (double)stats.getCountries().length;
	}
	
}
