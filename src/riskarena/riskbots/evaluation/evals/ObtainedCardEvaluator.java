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

public class ObtainedCardEvaluator extends AbstractEvaluator {
	private CardIndicator card;
	private final double CARD_REWARD = 0.2;
	private double score;
	
	public ObtainedCardEvaluator(String name, double weight, GameStats stats, GameInfo game, CardIndicator ci) {
		super(name, weight, stats, game);
		card = ci;
		recalculate();
	}
	
	public double getScore() {
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
