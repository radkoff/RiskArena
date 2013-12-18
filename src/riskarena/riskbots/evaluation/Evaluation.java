package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInterface;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;
import riskarena.riskbots.evaluation.evals.*;

public class Evaluation {
	private GameInfo game;
	private GameStats stats;
	private CardIndicator card;
	
	// Internal list of evaluators. A game stat's score is a weighted combination of these.
	private final String evals[] = {"OwnContinents", "EnemyContinents", "OwnArmies", "BestEnemy", "FortifiedTerritories",
							"OccupiedTerritories", "FrontierDistance", "ObtainedCard", "ArmyConsolidation", "TargetCont" };
	private ArrayList<AbstractEvaluator> evaluators;
	private final String EVAL_PACKAGE = "riskarena.riskbots.evaluation.evals.";
	private WeightManager weighter;
	private final String FULL_DEBUG = "ALL";
	
	private CountryInterface countries[];
	private final int num_evals = evals.length;
	
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
		evaluators.add( new OwnContinentsEvaluator("OwnContinents", stats, game) );
		evaluators.add( new EnemyContinentsEvaluator("EnemyContinents", stats, game) );
		evaluators.add( new OwnArmiesEvaluator("OwnArmies", stats, game) );
		evaluators.add( new BestEnemyEvaluator("BestEnemy", stats, game) );
		evaluators.add( new FortifiedTerritoriesEvaluator("FortifiedTerritories", stats, game) );
		evaluators.add( new OccupiedTerritoriesEvaluator("OccupiedTerritories", stats, game) );
		evaluators.add( new FrontierDistanceEvaluator("FrontierDistance", stats, game) );
		evaluators.add( new ObtainedCardEvaluator("ObtainedCard", stats, game, card) );
		evaluators.add( new ArmyConsolidationEvaluator("ArmyConsolidation", stats, game) );
		evaluators.add( new TargetContEvaluator("TargetCont", stats, game) );
	}
	
	public void endTurn() {
		refresh();
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
				Risk.sayOutput(e.getName() + " " + Utilities.dec(score), OutputFormat.BLUE, true);
		}
		stats.unapply(change);
		if(debug)
			Risk.sayOutput("\tScore: " + Utilities.dec(result), OutputFormat.BLUE, true);
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
		if(changes.isEmpty())
			return score();
		if(debug)
			Risk.sayOutput(game.getMyName() + " " + changes.get(1).amount(), OutputFormat.BLUE);
		stats.apply(changes);
		double result = 0.0;
		for(AbstractEvaluator e : evaluators) {
			double score = e.getScore(changes);
			//System.out.println(e.getName());
			//stats.touchArmies();
			result += weighter.weightOf(e.getName()) * score;
			if(debug)
				Risk.sayOutput(e.getName() + " " + Utilities.dec(score), OutputFormat.BLUE);
		}
		if(debug)
			Risk.sayOutput("");
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
					Risk.sayOutput(game.getMyName() + " " + e.getName() + ": " + Utilities.dec(evalScore), OutputFormat.BLUE, true);
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
	public void refresh() {
		System.out.println(Thread.currentThread().getName() + " refreshing");
		StackTraceElement z[] = Thread.currentThread().getStackTrace();
		for(int i=0; i<z.length; i++)
			System.out.println("\t"+z[i].toString());
		stats.refresh();
		for(AbstractEvaluator e : evaluators) {
			e.refresh();
		}
		System.out.println(Thread.currentThread().getName() + " DONE refreshing");
	}
	
}
