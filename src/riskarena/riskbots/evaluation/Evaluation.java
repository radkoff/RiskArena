/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation;

/*
 * The Evaluation class provides score functions that return a double signifying how well the
 * player using it is doing at any given time. This score is calculated by taking a weighted sum of
 * individual Evaluator scores, maintained in a list. Each Evaluator (see riskarena.riskbots.evaluation.evals)
 * takes the game state given by a GameInfo instance and reports a score relating to some
 * assigned aspect of the game. Many of these scores are in [0-1], but not all. Some may be negative
 * if they evaluate the strength of enemies.
 * Weights for the weighted sum are supplied by the WeightManager class.
 * To facilitate faster state evaluation, Evaluation can also return what the game state score
 * would be if a given change were to occur (instead of constructing a new game state entirely).
 * As of now these changes include two types, ArmyChange and OccupationChange.
 */

import java.util.ArrayList;

import riskarena.CountryInterface;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;
import riskarena.RiskBot;
import riskarena.riskbots.evaluation.evals.*;

public class Evaluation {
	private GameInfo game;
	private GameStats stats;	// Provides many of the evaluators with commonly needed stats (eg: the number of armies per player)
	private CardIndicator card;

	// Internal list of evaluators. A game state's score is a weighted combination of these.
	private ArrayList<AbstractEvaluator> evaluators;
	private final String EVAL_PACKAGE = "riskarena.riskbots.evaluation.evals.";
	private final String evals[] = {"OwnContinents", "EnemyContinents", "OwnArmies", "BestEnemy", "FortifiedTerritories",
			"OccupiedTerritories", "FrontierDistance", "ObtainedCard", "ArmyConsolidation", "TargetCont" };
	private final int num_evals = evals.length;
	
	private WeightManager weighter;
	private final String FULL_DEBUG = "ALL";	// Sentinel value used in score() debugging
	private CountryInterface countries[];

	public Evaluation(GameInfo gi, CardIndicator ci, boolean should_train) {
		game = gi;
		card = ci;
		stats = new GameStats(game);
		evaluators = new ArrayList<AbstractEvaluator>();
		weighter = new WeightManager(game.getMyName(), evals, should_train);
		countries = game.getCountryInfo();
		registerEvaluators();
		weighter.initGame();
	}

	private void registerEvaluators() {
		evaluators.clear();
		for(String evalName : evals) {
			AbstractEvaluator eval;
			try {
				// Loads the class of the RiskBot file being used.
				Class dynamic_class = Class.forName(EVAL_PACKAGE + evalName);
				eval = (AbstractEvaluator)dynamic_class.newInstance();
				eval.sendName(evalName);
				eval.sendGame(game);
				eval.sendStats(stats);
				if(evalName.equals("ObtainedCardEvaluator"))
					((ObtainedCardEvaluator)eval).sendCardIndicator(card);
				evaluators.add(eval);
			} catch ( ClassNotFoundException e ) {
				Risk.sayError("Source file for evaluator " + evalName + " not found.");
				System.exit(-1);
			} catch (InstantiationException e) {
				Risk.sayError("Could not instantiate evaluator " + evalName);
				e.printStackTrace();
				System.exit(-1);
			} catch (IllegalAccessException e) {
				Risk.sayError("Could not access source file for evaluator " + evalName);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void endTurn() {
		refresh("endTurn() in Evaluation.java");
		weighter.train(scoreVector());
	}

	public void endGame(int place) {
		weighter.endGame(scoreVector(), place, stats.getNumPlayers());
	}


	/*
	 * Returns the score of the board state inherent in GameInfo
	 */
	public double score() {
		return score(false, null);
	}

	public double score(OccupationChange change) {
		return score(change, false);
	}

	public double score(OccupationChange change, boolean debug) {
		//if(debug)
		//Risk.sayOutput("Considering " + countries[change.from()].getName() + " to " + countries[change.to()].getName(), OutputFormat.BLUE, true);
		stats.apply(change);
		double result = 0.0;
		for(AbstractEvaluator e : evaluators) {
			double score = e.getScore(change);
			result += weighter.weightOf(e.getName()) * score;
			if(debug)
				Risk.sayOutput(e.getName() + " " + Utilities.printDouble(score), OutputFormat.BLUE, true);
		}
		stats.unapply(change);
		if(debug)
			Risk.sayOutput("\tScore: " + Utilities.printDouble(result), OutputFormat.BLUE, true);
		return result;
	}

	public double score(ArrayList<ArmyChange> changes) {
		return score(changes, false);
	}

	/*
	 * Returns the score of the board state that would result from applying
	 * the ArrayList of army changes.
	 */
	public double score(ArrayList<ArmyChange> changes, boolean debug) {
		//debug = true;
		if(changes.isEmpty())
			return score();
		if(debug)
			Risk.sayOutput(game.getMyName() + " " + countries[changes.get(0).ID()].getName(), OutputFormat.BLUE, true);
		stats.apply(changes);
		double result = 0.0;
		for(AbstractEvaluator e : evaluators) {
			double score = e.getScore(changes);
			result += weighter.weightOf(e.getName()) * score;
			if(debug) {
				Risk.sayOutput(e.getName() + " " + Utilities.printDouble(score) + " * " + weighter.weightOf(e.getName()), OutputFormat.BLUE, true);
			}
		}
		if(debug)
			Risk.sayOutput("", true);
		stats.unapply(changes);
		return result;
	}

	public double debugScore() {
		return score(true, FULL_DEBUG);
	}

	public double debugScore(String nameOfEvalToDebug) {
		return score(true, nameOfEvalToDebug);
	}

	/*
	 * Calculate the weighted sum of scores returned by evaluators.
	 * If debug is true, intermediate calculations are also printed using Risk's static output methods.
	 * Only the final score and the score of an evaluator with the internal name nameOfEvalToDebug
	 * is printed, unless nameOfEvalToDebug has the value FULL_DEBUG, in which case they're all printed.
	 */
	private double score(boolean debug, String nameOfEvalToDebug) {
		if(debug && nameOfEvalToDebug == FULL_DEBUG)
			Risk.sayOutput("Scoring state for " + game.getMyName(), OutputFormat.BLUE, true);
		double result = 0.0;
		for(AbstractEvaluator e : evaluators) {
			double evalScore = e.getScore();
			if(debug) {
				if(nameOfEvalToDebug == FULL_DEBUG)
					Risk.sayOutput(e.getName() + ": " + evalScore, OutputFormat.TABBED, true);
				else if(nameOfEvalToDebug == e.getName())
					Risk.sayOutput(game.getMyName() + " " + e.getName() + ": " + Utilities.printDouble(evalScore), OutputFormat.BLUE, true);
			}
			result += weighter.weightOf(e.getName()) * evalScore;
		}
		if(debug && nameOfEvalToDebug == FULL_DEBUG)
			Risk.sayOutput("Final score for " + game.getMyName() + ": " + result, OutputFormat.BLUE, true);
		return result;
	}

	private Double[] scoreVector() {
		Double vec[] = new Double[evals.length];
		for(int i=0; i<evals.length; i++) {
			vec[i] = evaluators.get(i).getScore();
		}
		return vec;
	}

	/*
	 * A signal that the game state has changed, and this should be sent on to the
	 * evaluators and GameStats.
	 */
	public void refresh(String from) {
		boolean debug = false;
		if(debug) {
			System.out.println(game.getMyName() + " " + Thread.currentThread().getName() + " refreshing from " + from);
			Utilities.printThread(Thread.currentThread());
		}
		synchronized(this) {
			stats.refresh();
			for(AbstractEvaluator e : evaluators) {
				e.refresh();
			}
		}
		if(debug)
			System.out.println(Thread.currentThread().getName() + " DONE refreshing");
	}

}
