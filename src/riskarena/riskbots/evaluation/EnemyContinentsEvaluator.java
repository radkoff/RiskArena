package riskarena.riskbots.evaluation;

import riskarena.GameInfo;
/*
 * The EnemyContinentsEvaluator measures how many continents are owned by enemies
 * It returns this number divided by the total number of continents, times -1 (because higher is worse)
 * To save computation time, it uses a GameStats instance instead of GameInfo. 
 */

public class EnemyContinentsEvaluator extends AbstractEvaluator {
	private double score;
	
	public EnemyContinentsEvaluator(String name, double weight, GameStats stats, GameInfo game) {
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
		int enemyOwned = 0;
		int contOwnership[] = stats.getContinentOwnership();
		for(int i=0; i<contOwnership.length; i++) {
			if(contOwnership[i] != -1 && contOwnership[i] != stats.me())
				enemyOwned++;
		}
		score = -1 * enemyOwned / (double)stats.getNumContinents();
	}
	
}

