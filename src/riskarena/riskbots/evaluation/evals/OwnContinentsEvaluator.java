package riskarena.riskbots.evaluation.evals;
/*
 * The OwnContinentsEvaluator measures how many continent bonus armies the player recieves
 * It returns this number divided by the total number of army continent bonuses.
 */

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;

public class OwnContinentsEvaluator extends AbstractEvaluator {
	private int bonusMe = 0, bonusAll = 0;
	
	public OwnContinentsEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return bonusMe / (double)bonusAll;
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		return getScore();
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		int contBonuses[] = game.getContinentBonuses();
		int contOwnership[] = stats.getContinentOwnership();
		bonusMe = 0;
		bonusAll = 0;
		for(int i=0; i<contBonuses.length; i++) {
			bonusAll += contBonuses[i];
			if(contOwnership[i] == game.me())
				bonusMe += contBonuses[i];
		}
	}
	
}
