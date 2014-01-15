/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation.evals;
/*
 *	An evaluator that return the fraction of this player's territories that
 *	have more than 1 army on them (are "fortified") 
 */

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.CountryInterface;
import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public class FortifiedTerritoriesEvaluator extends AbstractEvaluator {
	private int fortifiedTerritories;
	
	public FortifiedTerritoriesEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
		refresh();
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
		for(int i=0; i<countries.length;i++) {
			if(countries[i].isTaken() && countries[i].getPlayer() == game.me() && countries[i].getArmies() > 1)
				fortifiedTerritories++;
		}
	}
	
	private double calculate(int forts) {
		return forts / (double)stats.getOccupationCounts()[game.me()];
	}

}
