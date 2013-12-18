package riskarena.riskbots.evaluation.evals;
/*
 * Calculates the inverse of the number of fortified friendly territories (>1 armies).
 * This encourages the consolidation of armies into larger ones. The best case of everything in
 * one army isn't usually a good strategy, so dampen that score from 1.0 to 0.5
 */
import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.CountryInterface;
import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public class ArmyConsolidationEvaluator extends AbstractEvaluator {
	private int fortifiedTerritories;
	
	public ArmyConsolidationEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
	}
	
	public double getScore() {
		return calculate(fortifiedTerritories);
	}
	
	public double getScore(OccupationChange change) {
		if(change.casualties() == stats.getCountries()[change.from()].getArmies() - 2)
			return calculate(fortifiedTerritories - 1);
		else
			return getScore();
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		CountryInterface countries[] = stats.getCountries();
		int newFortifiedTerritories = fortifiedTerritories;
		for(ArmyChange change : changes) {
			if(countries[change.ID()].getPlayer() == game.me()) {
				int oldA = countries[change.ID()].getArmies();
				int newA = oldA += change.amount();
				if(oldA > 1 && newA == 1) {
					newFortifiedTerritories--;
				}
				if(oldA == 1 && newA > 1) {
					newFortifiedTerritories++;
				}
			}
		}
		return calculate(newFortifiedTerritories);
	}
	
	public void refresh() {
		fortifiedTerritories = 0;
		CountryInterface countries[] = stats.getCountries();
		for(Integer friendly : stats.getMyCountries()) {
			if(countries[friendly].getArmies() > 1) {
				fortifiedTerritories++;
			}
		}
	}
	
	private double calculate(int numFortifiedTerritories) {
		if(numFortifiedTerritories == 0)
			return 0.0;
		else if (numFortifiedTerritories == 1)
			return 0.5;
		else
			return 1 / (double) numFortifiedTerritories;
	}
	
}
