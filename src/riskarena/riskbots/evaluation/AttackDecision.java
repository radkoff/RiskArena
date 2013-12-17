package riskarena.riskbots.evaluation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.Random;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.World;

import com.sun.tools.javac.util.Pair;

public class AttackDecision {
	private GameInfo game;
	private World world;
	private Evaluation eval;
	private Queue< Pair<Integer, Integer> > attacks;			// A queue of intended attacks, reset each turn
	private Integer previousTo;		// Necessary in order to consider attacking from a newly conquered territory
	private Integer previousFrom;
	
	private final int minAttackThreshold = 3;					// If a territory has under this # of armies, don't attack from it
	private final int maxAttackThreshold = 16;					// If a territory has this many armies, always attack from it

	private Random gen;
	public AttackDecision(GameInfo _game, Evaluation _eval) {
		game = _game;
		world = game.getWorldInfo();
		eval = _eval;
		attacks = new ArrayDeque< Pair<Integer, Integer> >();
		gen = new Random((new Date()).getTime());
	}

	public void initTurn() {
		// At the start of each turn, this bot probabilistically forms decisions to attack
		// enemy territories, and adds these to the "attacks" queue.
		// The launchAttack() method executes these decisions until DEATH!
		attacks.clear();
		CountryInfo[] countries = game.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			if(shouldAttackFrom(countries[i]))	// Consider attacking from country i
				attackFrom(i, countries);
		}
	}
	
	public ArrayList<Integer> decide() {
		CountryInfo[] countries = game.getCountryInfo();
		
		// TODO remove this, it's for testing
	/*	for(int i=0; i<countries.length; i++) {
			if(countries[i].getPlayer() != game.me() || countries[i].getArmies() <= 1)
				continue;
			int adj[] = world.getAdjacencies(i);
			for(int a=0; a<adj.length; a++) {
				if(countries[adj[a]].getPlayer() != game.me()) {
					OccupationChange oc = new OccupationChange(i, adj[a], countries[i].getArmies() - 1, countries[adj[a]].getArmies());
					eval.score(oc, true);
				}
			}
		}*/
		
		if( previousTo != null && shouldAttackFrom(countries[previousTo]) )
			attackFrom(previousTo, countries);
		if( previousFrom != null && shouldAttackFrom(countries[previousFrom]) )
			attackFrom(previousFrom, countries);
		previousTo = null;
		previousFrom = null;
		
		if(!attacks.isEmpty()) {
			Pair<Integer,Integer> attack = attacks.peek();		// The attack currently being executed
			// Check to see if you've conquered the territory
			if(countries[attack.snd].getPlayer() == game.me()) {
				attacks.remove();
				return decide();
			}
			// Check to see if you've run out of armies to attack with :(
			if(countries[attack.fst].getArmies() == 1) {
				attacks.remove();
				return decide();
			}
			ArrayList<Integer> answer = new ArrayList<Integer>();
			answer.add(attack.fst);
			answer.add(attack.snd);
			answer.add(Math.min(countries[attack.fst].getArmies()-1, 3)); // Attack with all you've got!
			return answer;
		} else {
			ArrayList<Integer> answer = new ArrayList<Integer>();
			answer.add(-1);
			return answer;
		}
	}
	
	/*
	 * [Private helper method, not part of the RiskBot interface]
 	 * This method takes a country given by countryID that is supposed to be the
 	 * source of an attack. If one exists, it randomly chooses a neighboring enemy
 	 * territory to invade.
 	 * countries is passed to that the info doesn't need to be reloaded every time this is called
	 */
	private void attackFrom(int countryID, CountryInfo[] countries) {
		ArrayList<Integer> possibleTargets = new ArrayList<Integer>();
		int adj[] = world.getAdjacencies(countryID);
		for(int j=0; j<adj.length; j++) {
			if(countries[adj[j]].getPlayer() != game.me())
				possibleTargets.add(new Integer(adj[j]));
		}
		if(possibleTargets.isEmpty()) {		// No foreign targets
			return;
		} else {
			// Choose randomly from the possible targets
			int choice = gen.nextInt(possibleTargets.size());
			attacks.add( new Pair<Integer,Integer>(new Integer(countryID), possibleTargets.get(choice)) );
			//Risk.sayOutput(countries[countryID].getName() + " -> " + countries[possibleTargets.get(choice).intValue()].getName() + " TARGET SET", OutputFormat.BLUE);
		}
	}
	
	/*
	 * [Private helper method, not part of the RiskBot interface]
	 * Given a CountryInfo object, returns a boolean indicating whether or not it should attack
	 */
	private boolean shouldAttackFrom(CountryInfo country) {
		boolean result = true;
		if(country.getPlayer() != game.me())
			result = false;
		else if(country.getArmies() <= minAttackThreshold)		// Too few armies to attack
			result = false;
		else if(country.getArmies() >= maxAttackThreshold)	// Lots of armies, always attack
			result = true;
		else {
			// If the territory has an army amount between the two thresholds, assign a probability of attack (linearly)
			float probability = (country.getArmies() - minAttackThreshold) / ((float) maxAttackThreshold - minAttackThreshold);
			result = gen.nextFloat() <= probability;
		}
		//if(country.getPlayer() == risk_info.me() && country.getArmies() > minAttackThreshold)
		//	Risk.sayOutput(country.getName() + ": " + result, OutputFormat.BLUE);
		return result;
	}
	
	
	public void notifyOfVictory(int att, int def) {
		previousTo = new Integer(def);
		previousFrom = new Integer(att);
	}

}
