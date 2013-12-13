package riskarena.riskbots.evaluation;
/*
 * The OwnArmiesEvaluator measures how many armies the player has on the board.
 * It returns this number divided by the total number of armies on the board.
 * To save computation time, it uses a GameStats instance instead of GameInfo. 
 */


public class OwnArmiesEvaluator extends AbstractEvaluator {
	private GameStats stats;
	private double score;
	
	public OwnArmiesEvaluator(String name, double weight, GameStats _stats) {
		super(name, weight);
		stats = _stats;
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		score = stats.getArmiesPerPlayer()[stats.me()] / (double)stats.getTotalArmies();
	}
	
}
