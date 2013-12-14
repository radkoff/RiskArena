package riskarena.riskbots.evaluation.evals;

import riskarena.GameInfo;
/*
 * The EnemyContinentsEvaluator measures how many continent bonus armies enemy players receive (together)
 * It returns this number divided by the total number of army continent bonuses.
 * Negative because it's bad.
 */
import riskarena.riskbots.evaluation.GameStats;

public class EnemyContinentsEvaluator extends AbstractEvaluator {
	private int bonusThem = 0, bonusAll = 0;
	
	public EnemyContinentsEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return -1 * bonusThem / (double)bonusAll;
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		int contBonuses[] = game.getContinentBonuses();
		int contOwnership[] = stats.getContinentOwnership();
		bonusThem = 0;
		bonusAll = 0;
		for(int i=0; i<contBonuses.length; i++) {
			bonusAll += contBonuses[i];
			if(contOwnership[i] != game.me() && contOwnership[i] != -1)
				bonusThem += contBonuses[i];
		}
	}
	
}

