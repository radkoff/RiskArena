package riskarena.riskbots.evaluation;

import riskarena.CountryInfo;
import riskarena.GameInfo;

public class AvgArmySizeEvaluator extends AbstractEvaluator {
	private double score;
	private int totalExtraArmies, fortifiedTerritories;
	
	public AvgArmySizeEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public void refresh() {
		totalExtraArmies = 0;
		fortifiedTerritories = 0;
		CountryInfo countries[] = stats.getCountries();
		for(Integer friendly : stats.getMyCountries()) {
			int armies = countries[friendly].getArmies();
			if(armies > 1) {
				totalExtraArmies += armies - 1;
				fortifiedTerritories++;
			}
		}
		recalculate();
	}
	
	private void recalculate() {
		score = totalExtraArmies / (double) fortifiedTerritories;
	}
	
}
