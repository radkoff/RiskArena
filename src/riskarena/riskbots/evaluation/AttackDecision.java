/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation;

/*
 * The AttackDecision class is consulted by Evaluation players to decide what answers
 * should be sent to the game in response to RiskBot.launchAttack(). These answers
 * are supplied by the decide() method.
 * At the start of each turn (and after a victory), potential attacks are scored in
 * the considerAttackFrom() method. If this score offers an increase in the default
 * game state score bigger than delta_threshold, it is accepted and added to the attacks PriorityQueue 
 */

import java.util.ArrayList;
import java.util.PriorityQueue;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;
import riskarena.World;

import com.sun.tools.javac.util.Pair;

public class AttackDecision {
	private GameInfo game;
	private World world;
	private CountryInfo[] countries;
	private Evaluation eval;
	private BattleOracle oracle;
	private PriorityQueue< AttackPlans > attacks;			// A queue of intended attacks, reset each turn
	private Integer previousTo;		// Necessary in order to consider attacking from a newly conquered territory
	private Integer previousFrom;

	// If this is higher the player is move conservative, if lower they're more aggressive
	private double delta_threshold = 0.35;

	public AttackDecision(GameInfo _game, Evaluation _eval) {
		game = _game;
		world = game.getWorldInfo();
		eval = _eval;
		oracle = new BattleOracle();
		attacks = new PriorityQueue< AttackPlans >();
	}

	/*
	 * At the start of each turn, this bot probabilistically forms decisions to attack
 	 * enemy territories, and adds these to the "attacks" queue.
 	 * The launchAttack() method executes these decisions until DEATH!
	 */
	public void initTurn() {
		attacks.clear();
		eval.refresh("initTurn() in AttackDecision");
		countries = game.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			considerAttackFrom(i, false);	// Consider attacking from country i
		}
	}

	/*
	 * Provides whatever Evaluation player is using this with answers to RiskBot.launchAttack()
	 */
	public ArrayList<Integer> decide() {
		countries = game.getCountryInfo();

		if( previousTo != null )
			considerAttackFrom(previousTo, false);
		if( previousFrom != null )
			considerAttackFrom(previousFrom, false);
		previousTo = null;
		previousFrom = null;

		if(attacks.isEmpty()) {
			ArrayList<Integer> answer = new ArrayList<Integer>();
			answer.add(-1);
			return answer;
		} else {
			AttackPlans attack = attacks.peek();		// The attack currently being executed
			// Check to see if you've conquered the territory
			if(countries[attack.to()].getPlayer() == game.me()) {
				attacks.remove();
				return decide();
			}
			// Check to see if you've run out of armies to attack with :(
			if(countries[attack.from()].getArmies() == 1) {
				attacks.remove();
				return decide();
			}
			ArrayList<Integer> answer = new ArrayList<Integer>();
			answer.add(attack.from());
			answer.add(attack.to());
			answer.add( Math.min(countries[attack.from()].getArmies()-1, 3) ); // Attack with all you've got!
			return answer;
		}
	}

	/*
	 * Considers an attack from country with ID 'id'.
	 * Comes up with a score according to the following formula:
	 * Pr(Victory) * Eval( Ex(Victory) ) + Pr(Defeat) * Eval( Ex(Defeat) )
	 * where Pr is probability, Ex is the expected game state given victory or defeat,
	 * and Eval is the evaluation function. Battle probabilities and expectations are found
	 * using the BattleOracle class. If the ratio of this potential game state score to the
	 * default game state score (without the attack) is above delta_threshold, the attack is
	 * added to the "attacks" PriorityQueue according to the score.
	 */
	private void considerAttackFrom(int id, boolean debug) {
		//debug = true;
		if(countries[id].getPlayer() != game.me() || countries[id].getArmies() <= 1)
			return;
		int adj[] = world.getAdjacencies(id);
		double score_before = eval.score();
		if(debug)
			Risk.sayOutput("Original score: " + Utilities.printDouble(score_before), OutputFormat.BLUE, true);
		for(int a=0; a<adj.length; a++) {
			if(countries[adj[a]].getPlayer() != game.me() && countries[adj[a]].getArmies() > 0) {
				double score;
				int numAttacking = countries[id].getArmies()-1;
				int numDefending = countries[adj[a]].getArmies();
				if(debug)
					Risk.sayOutput("Considering " + countries[id].getName() + " -> " + countries[adj[a]].getName() + " with " + numAttacking + " armies.", OutputFormat.BLUE, true);
				if(countries[id].getArmies() <= 1)
					return;
				if(numAttacking > oracle.maxPredictionAbility())
					score = Double.MAX_VALUE;
				else if(numDefending > oracle.maxPredictionAbility())
					return;
				else {
					Pair<Double,Integer> win = oracle.predictWin(numAttacking, numDefending);
					Pair<Double,Integer> loss = oracle.predictLoss(numAttacking, numDefending);
					OccupationChange winChange = new OccupationChange(id, adj[a], numAttacking - win.snd, numDefending);
					ArrayList<ArmyChange> lossChange = new ArrayList<ArmyChange>();
					lossChange.add( new ArmyChange(id, -1 * numAttacking) );
					lossChange.add( new ArmyChange(adj[a], -1 * (numDefending - loss.snd)) );
					double winScore = eval.score(winChange), lossScore = eval.score(lossChange);
					score = win.fst * winScore + loss.fst * lossScore;
					score += Math.abs(score) * bonusAggressiveness(countries[id].getArmies(), win.fst);
					if(debug)
						Risk.sayOutput(win.fst + " " + Utilities.printDouble(winScore) + " " + loss.fst + " " + Utilities.printDouble(lossScore), OutputFormat.QUESTION, true);
				}
				if(debug)
					Risk.sayOutput("Delta: "+ Utilities.printDouble((score - score_before) / Math.abs(score_before)), OutputFormat.BLUE, true);
				if( (score - score_before) / Math.abs(score_before) > delta_threshold) {
					attacks.add( new AttackPlans((score - score_before), id, adj[a]) );
					if(debug)
						Risk.sayOutput(countries[id].getName() + " -> " + countries[adj[a]].getName() + " TARGET SET\n", OutputFormat.BLUE, true);
				}
			}
		}
	}

	/*
	 * To encourage the use of very large armies, some score is added depending on the size and probability of victory
	 */
	private double bonusAggressiveness(int armies, Double prob) {
		if(Math.abs(prob - 1.00) < 0.0000001 && armies > 40)
			return 0.8;
		else if(Math.abs(prob - 1.00) < 0.0001 && armies > 20)
			return 0.5;
		else if(Math.abs(prob - 1.00) < 0.09 && armies > 15)
			return 0.3;
		else if(armies > 40)
			return 0.15;
		else
			return 0.0;
	}

	public void notifyOfVictory(int att, int def) {
		previousTo = new Integer(def);
		previousFrom = new Integer(att);
	}

	
	private class AttackPlans implements Comparable {
		private double delta_score;
		private int from, to;
		public AttackPlans(double dscore, int f, int t) {
			delta_score = dscore;
			from = f;
			to = t;
		}
		public double delta_score() { return delta_score; }
		public int from() { return from; }
		public int to() { return to; }
		public int compareTo(Object arg0) {
			AttackPlans ap = (AttackPlans) arg0;
			if(ap.delta_score() < delta_score)
				return -1;
			else if (ap.delta_score() == delta_score)
				return 0;
			else
				return 1;
		}
	}
}
