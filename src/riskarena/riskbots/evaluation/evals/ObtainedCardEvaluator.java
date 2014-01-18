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
/*
 * Evaluates whether or not the player will pick up a card at the end of their turn.
 * Formula:		0 if no, CARD_REWARD*sqrt(game turn #)
 */
import riskarena.riskbots.evaluation.CardIndicator;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public class ObtainedCardEvaluator extends AbstractEvaluator {
	private CardIndicator card;
	private final double CARD_REWARD = 0.2;
	private double score;
	
	public ObtainedCardEvaluator(String name, GameStats stats, GameInfo game) {
		super(name, stats, game);
	}
	
	public void sendCardIndicator(CardIndicator ci) {
		card = ci;
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public double getScore(OccupationChange change) {
		if(!card.getVictory()) {
			return CARD_REWARD * Math.sqrt(game.getTurnNumber());
		} else
			return score;
	}
	
	public double getScore(ArrayList<ArmyChange> changes) {
		return getScore();
	}
	
	public void refresh() {
		recalculate();
	}
	
	private void recalculate() {
		if(card.getVictory())
			score = CARD_REWARD * Math.sqrt(game.getTurnNumber());
		else
			score = 0.0;
	}
	
}
