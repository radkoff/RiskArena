package riskarena.riskbots.evaluation;

import riskarena.GameInfo;
/*
 * The OwnArmiesEvaluator measures how many armies the player has on the board.
 * It returns this number divided by the total number of armies on the board.
 */


public class OwnArmiesEvaluator extends AbstractEvaluator {
	private double score;
	
	public OwnArmiesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
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
		score = stats.getArmiesPerPlayer()[game.me()] / (double)stats.getTotalArmies();
	}
	
}
