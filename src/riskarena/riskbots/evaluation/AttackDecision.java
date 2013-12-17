package riskarena.riskbots.evaluation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

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

	private double delta_threshold = 1.0;

	public AttackDecision(GameInfo _game, Evaluation _eval) {
		game = _game;
		world = game.getWorldInfo();
		eval = _eval;
		oracle = new BattleOracle();
		attacks = new PriorityQueue< AttackPlans >();
	}

	public void initTurn() {
		// At the start of each turn, this bot probabilistically forms decisions to attack
		// enemy territories, and adds these to the "attacks" queue.
		// The launchAttack() method executes these decisions until DEATH!
		attacks.clear();
		countries = game.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			considerAttackFrom(i, false);	// Consider attacking from country i
		}
	}

	public ArrayList<Integer> decide() {
		countries = game.getCountryInfo();

		if( previousTo != null )
			considerAttackFrom(previousTo, false);
		if( previousFrom != null )
			considerAttackFrom(previousFrom, false);
		previousTo = null;
		previousFrom = null;

		if(!attacks.isEmpty()) {
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
			answer.add(Math.min(countries[attack.from()].getArmies()-1, 3)); // Attack with all you've got!
			return answer;
		} else {
			ArrayList<Integer> answer = new ArrayList<Integer>();
			answer.add(-1);
			return answer;
		}
	}

	private void considerAttackFrom(int id, boolean debug) {
		if(countries[id].getPlayer() != game.me() || countries[id].getArmies() <= 1)
			return;
		int adj[] = world.getAdjacencies(id);
		double score_before = eval.score();
		if(debug)
			Risk.sayOutput("Original score: " + Utilities.dec(score_before), OutputFormat.BLUE, true);
		for(int a=0; a<adj.length; a++) {
			if(countries[adj[a]].getPlayer() != game.me()) {
				double score;
				int numAttacking = countries[id].getArmies()-1;
				int numDefending = countries[adj[a]].getArmies();
				if(debug)
					Risk.sayOutput("Considering " + countries[id].getName() + " -> " + countries[adj[a]].getName(), OutputFormat.BLUE, true);
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
					score += bonusAggressiveness(countries[id].getArmies(), win.fst);
					if(debug)
						Risk.sayOutput(win.fst + " " + Utilities.dec(winScore) + " " + loss.fst + " " + Utilities.dec(lossScore), OutputFormat.QUESTION, true);
				}
				if(debug)
				Risk.sayOutput("Delta: "+ Utilities.dec(score - score_before), OutputFormat.BLUE, true);
				if( (score - score_before) > delta_threshold) {
					attacks.add( new AttackPlans((score - score_before), id, adj[a]) );
					if(debug)
						Risk.sayOutput(countries[id].getName() + " -> " + countries[adj[a]].getName() + " TARGET SET\n", OutputFormat.BLUE, true);
				}
			}
		}
	}

	/*
	 * To encourage the use of very large armies, some score is added depending on the size
	 */
	private double bonusAggressiveness(int armies, Double prob) {
		if(Math.abs(prob - 1.00) < 0.0000001 && armies > 40)
			return 1.0;
		else if(Math.abs(prob - 1.00) < 0.0001 && armies > 20)
			return 0.4;
		else if(Math.abs(prob - 1.00) < 0.05 && armies > 15)
			return 0.1;
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
