/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation.evals;

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public abstract class AbstractEvaluator {
	private String name;			// The name of the evaluator (without "Evaluator" at the end)
	protected GameStats stats;		// Since many evaluators need similar calculations, they all share a GameStats object
	protected GameInfo game;		// Holds game state information
	
	public AbstractEvaluator(String _name, GameStats _stats, GameInfo _game) {
		name = _name;
		stats = _stats;
		game = _game;
	}
	
	/*
	 * The following three scoring functions must be implemented by all AbstractEvaluator subclasses
	 */
	
	// A double representing how good the game state is for this player (higher = better). Usually [0,1], but not always.
	abstract public double getScore();
	
	// A double representing the game state score that would result from applying every ArmyChange in this ArrayList
	abstract public double getScore(ArrayList<ArmyChange> changes);
	
	// A double representing the game state score that would result from applying an OccupationChange
	abstract public double getScore(OccupationChange change);
	
	// Signals that the game state represented by GameInfo (and soon after pushed to GameStats) has changed.
	// Usually this should prompt some kind of recalcuation.
	abstract public void refresh();
	
	public String getName() {
		return name;
	}
}
