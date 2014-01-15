/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation.evals;
/*
 * Scores the negative percentage of continent army bonuses being obtained by enemy players
 */

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.OccupationChange;
/*
 * The EnemyContinentsEvaluator measures how many continent bonus armies enemy players receive (together)
 * It returns this number divided by the total number of army continent bonuses.
 * Negative because it's bad.
 */
import riskarena.riskbots.evaluation.GameStats;

public class EnemyContinentsEvaluator extends AbstractEvaluator {
	private int bonusThem = 0, bonusAll = 0;
	
	public EnemyContinentsEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
		int contBonuses[] = game.getContinentBonuses();
		for(int i=0; i<contBonuses.length; i++) {
			bonusAll += contBonuses[i];
		}
		recalculate();
	}
	
	public double getScore() {
		return -1 * bonusThem / (double)bonusAll;
	}

	public double getScore(ArrayList<ArmyChange> changes) {
		return getScore();
	}
	
	public double getScore(OccupationChange change) {
		int priorThem = bonusThem;
		recalculate();
		double score = getScore();
		bonusThem = priorThem;
		return score;
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		int contBonuses[] = game.getContinentBonuses();
		int contOwnership[] = stats.getContinentOwnership();
		bonusThem = 0;
		for(int i=0; i<contBonuses.length; i++) {
			if(contOwnership[i] != game.me() && contOwnership[i] != -1)
				bonusThem += contBonuses[i];
		}
	}
	
}

