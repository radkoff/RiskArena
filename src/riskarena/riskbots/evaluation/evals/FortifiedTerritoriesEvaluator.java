package riskarena.riskbots.evaluation.evals;
/*
 *	An evaluator that return the fraction of this player's territories that
 *	have more than 1 army on them (are "fortified") 
 */

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.riskbots.evaluation.GameStats;

public class FortifiedTerritoriesEvaluator extends AbstractEvaluator {
	private double score;
	
	public FortifiedTerritoriesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
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
		score = numFortifiedTerritories() / (double)stats.getOccupationCounts()[game.me()];
	}
	
	private int numFortifiedTerritories() {
		int count = 0;
		CountryInfo countries[] = stats.getCountries();
		for(int i=0; i<countries.length;i++) {
			if(countries[i].isTaken() && countries[i].getPlayer() == game.me() && countries[i].getArmies() > 1)
				count++;
		}
		return count;
	}
}
