package riskarena.riskbots.evaluation.evals;

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
/*
 * The OwnArmiesEvaluator measures how many armies the player has on the board.
 * It returns this number divided by the total number of armies on the board.
 */
import riskarena.riskbots.evaluation.GameStats;

public class OwnArmiesEvaluator extends AbstractEvaluator {
	private double score;
	
	public OwnArmiesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		refresh();
	}
	
	public double getScore() {
		return score;
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		return calculate();
	}
	
	public void refresh() {
		score = calculate();
	}
	
	private double calculate() {
		return stats.getArmiesPerPlayer()[game.me()] / (double)stats.getTotalArmies();
	}
	
}
