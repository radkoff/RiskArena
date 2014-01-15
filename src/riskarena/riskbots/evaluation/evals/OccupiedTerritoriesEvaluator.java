/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
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
	
	public OccupiedTerritoriesEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
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
