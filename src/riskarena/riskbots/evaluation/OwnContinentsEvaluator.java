package riskarena.riskbots.evaluation;
/*
 * The OwnContinentsEvaluator measures how many continents are owned by the player
 * It returns this number divided by the total number of continents.
 * To save computation time, it uses a GameStats instance instead of GameInfo. 
 */


public class OwnContinentsEvaluator extends AbstractEvaluator {
	private GameStats stats;
	private double score;
	
	public OwnContinentsEvaluator(String name, double weight, GameStats _stats) {
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
		score = stats.getNumContinentsOwnedBy(stats.me()) / (double)stats.getNumContinents();
	}
	
}
