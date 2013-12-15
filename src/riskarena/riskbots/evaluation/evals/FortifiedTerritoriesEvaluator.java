package riskarena.riskbots.evaluation.evals;
/*
 *	An evaluator that return the fraction of this player's territories that
 *	have more than 1 army on them (are "fortified") 
 */

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;

public class FortifiedTerritoriesEvaluator extends AbstractEvaluator {
	private int fortifiedTerritories;
	
	public FortifiedTerritoriesEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		refresh();
	}
	
	public double getScore() {
		return calculate(fortifiedTerritories);
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		CountryInfo countries[] = stats.getCountries();
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
		CountryInfo countries[] = stats.getCountries();
		for(int i=0; i<countries.length;i++) {
			if(countries[i].isTaken() && countries[i].getPlayer() == game.me() && countries[i].getArmies() > 1)
				fortifiedTerritories++;
		}
	}
	
	private double calculate(int forts) {
		return forts / (double)stats.getOccupationCounts()[game.me()];
	}

}
