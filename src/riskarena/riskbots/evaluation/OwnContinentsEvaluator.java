package riskarena.riskbots.evaluation;

import riskarena.GameInfo;
/*
 * The OwnContinentsEvaluator measures how many continents are owned by the player
 * It returns this number divided by the total number of continents.
 * To save computation time, it uses a GameStats instance instead of GameInfo. 
 */


public class OwnContinentsEvaluator extends AbstractEvaluator {
	private double score;
	
	public OwnContinentsEvaluator(String name, double weight, GameStats stats, GameInfo game) {
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
		score = stats.getNumContinentsOwnedBy(stats.me()) / (double)stats.getNumContinents();
	}
	
}
