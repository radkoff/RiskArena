/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation.evals;
/*
 * The OwnContinentsEvaluator measures how many continent bonus armies the player recieves
 * It returns this number divided by the total number of army continent bonuses.
 */

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public class OwnContinentsEvaluator extends AbstractEvaluator {
	private int bonusMe = 0, bonusAll = 0;
	
	public OwnContinentsEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
		int contBonuses[] = game.getContinentBonuses();
		for(int i=0; i<contBonuses.length; i++) {
			bonusAll += contBonuses[i];
		}
		recalculate();
	}
	
	public double getScore() {
		return bonusMe / (double)bonusAll;
	}
	
	public double getScore(OccupationChange change) {
		int priorMe = bonusMe;
		recalculate();
		double score = getScore();
		bonusMe = priorMe;
		return score;
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
		for(int i=0; i<contBonuses.length; i++) {
			if(contOwnership[i] == game.me())
				bonusMe += contBonuses[i];
		}
	}
	
}
