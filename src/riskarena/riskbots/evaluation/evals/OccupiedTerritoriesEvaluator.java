package riskarena.riskbots.evaluation.evals;

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.OccupationChange;
/*
 * The OccurpiedTerritories measures how many territories the player owns.
 * It returns this number divided by the total number of territories on the board.
 */
import riskarena.riskbots.evaluation.GameStats;


public class OccupiedTerritoriesEvaluator extends AbstractEvaluator {
	private double score;
	
	public OccupiedTerritoriesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		refresh();
	}
	
	public double getScore() {
		return score;
	}
	
	public double getScore(OccupationChange change) {
		return calculate();
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		return calculate();
	}
	
	public void refresh() {
		score = calculate();
	}
	
	private double calculate() {
		return stats.getOccupationCounts()[game.me()] / (double)stats.getCountries().length;
	}
	
}
