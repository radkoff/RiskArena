package riskarena.riskbots.evaluation;
/*
 * Calculates the inverse of the number of fortified friendly territories (>1 armies).
 * This encourages the consolidation of armies into larger ones. The best case of everything in
 * one army isn't usually a good strategy, so dampen that score from 1.0 to 0.5
 */
import riskarena.CountryInfo;
import riskarena.GameInfo;

public class ArmyConsolidationEvaluator extends AbstractEvaluator {
	private double score;
	private int fortifiedTerritories;
	
	public ArmyConsolidationEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public void refresh() {
		fortifiedTerritories = 0;
		CountryInfo countries[] = stats.getCountries();
		for(Integer friendly : stats.getMyCountries()) {
			if(countries[friendly].getArmies() > 1) {
				fortifiedTerritories++;
			}
		}
		recalculate();
	}
	
	private void recalculate() {
		if(fortifiedTerritories == 0)
			score = 0.0;
		else if (fortifiedTerritories == 1)
			score = 0.5;
		else
			score = 1 / (double) fortifiedTerritories;
	}
	
}
