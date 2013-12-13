package riskarena.riskbots.evaluation;
/*
 * The EnemyContinentsEvaluator measures how many continents are owned by enemies
 * It returns this number divided by the total number of continents, times -1 (because higher is worse)
 * To save computation time, it uses a GameStats instance instead of GameInfo. 
 */

public class EnemyContinentsEvaluator extends AbstractEvaluator {
	private GameStats stats;
	private double score;
	
	public EnemyContinentsEvaluator(String name, double weight, GameStats _stats) {
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
		int enemyOwned = 0;
		int contOwnership[] = stats.getContinentOwnership();
		for(int i=0; i<contOwnership.length; i++) {
			if(contOwnership[i] != -1 && contOwnership[i] != stats.me())
				enemyOwned++;
		}
		score = -1 * enemyOwned / (double)stats.getNumContinents();
	}
	
}

