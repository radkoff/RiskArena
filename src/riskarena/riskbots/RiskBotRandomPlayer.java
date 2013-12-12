package riskarena.riskbots;
/*
 * A dumb RiskBot used for testing purposes. See HOWTO or RiskBot.java for more on what these methods do.
 * 
 * Evan Radkoff
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import riskarena.Bot;
import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.PlayerInfo;
import riskarena.RiskBot;
import riskarena.World;

public class RiskBotRandomPlayer implements RiskBot{
	/*	Game related data members it's always a good idea to keep */
	private Bot.RiskListener to_game;		// Send game time decisions using to_game.sendInt(int/Integer)
	private GameInfo risk_info;
	private World world = null;				// Holds adjacency information
	private PlayerInfo[] players = null;
	
	/* Data members specific to this particular RiskBot */
	private Random gen; 										// Random number generator used for some decisions
	private Queue< Pair<Integer, Integer> > attacks;			// A queue of intended attacks, reset each turn
	private final int minAttackThreshold = 3;					// If a territory has under this # of armies, don't attack from it
	private final int maxAttackThreshold = 20;					// If a territory has this many armies, always attack from it
	

	/*
	 * Initialize the bot, locally store the given instance of GameInfo so that we can
	 * get board info any time we want, as well as a RiskListener so we can communicate our answers.
	 * @see riskarena.RiskBot#init(riskarena.GameInfo, riskarena.Bot.RiskListener)
	 */
	public void init(GameInfo gi, Bot.RiskListener rl) {
		risk_info = gi;
		to_game = rl;
		world = risk_info.getWorldInfo();
		players = risk_info.getPlayerInfo();
		gen = new Random((new Date()).getTime());
		attacks = new ArrayDeque< Pair<Integer, Integer> >();
	}
	
	/*
	 * Turn-based initialization
	 * @see riskarena.RiskBot#initTurn()
	 */
	public void initTurn() {
		attacks.clear();
		chooseAttacks();
	}
	
	/*
	 * [Private helper method, not part of the RiskBot interface]
	 * At the start of each turn, this bot probabilistically forms decisions to attack
	 * enemy territories, and adds these to the "attacks" queue.
	 * The launchAttack() method executes these decisions until DEATH!
	 */
	private void chooseAttacks() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			if(shouldAttackFrom(countries[i])) {
				ArrayList<Integer> possibleTargets = new ArrayList<Integer>();
				int adj[] = world.getAdjacencies(i);
				for(int j=0; j<adj.length; j++) {
					if(countries[adj[j]].getPlayer() != risk_info.me())
						possibleTargets.add(new Integer(adj[j]));
				}
				if(possibleTargets.isEmpty()) {		// No foreign targets
					continue;
				} else {
					// Choose randomly from the possible targets
					int choice = gen.nextInt(possibleTargets.size());
					attacks.add( new Pair<Integer,Integer>(new Integer(i), possibleTargets.get(choice)) );
				}
			}
		}
	}
	
	/*
	 * [Private helper method, not part of the RiskBot interface]
	 * Given a CountryInfo object, returns a boolean indicating whether or not it should attack
	 */
	private boolean shouldAttackFrom(CountryInfo country) {
		if(country.getPlayer() != risk_info.me())
			return false;
		if(country.getArmies() <= minAttackThreshold)		// Too few armies to attack
			return false;
		else if(country.getArmies() >= maxAttackThreshold)	// Lots of armies, always attack
			return true;
		else {
			// If the territory has an army amount between the two thresholds, assign a probability of attack (linearly)
			float probability = (country.getArmies() - minAttackThreshold) / ((float) maxAttackThreshold - minAttackThreshold);
			return gen.nextFloat() <= probability;
		}
	}

	/*
	 * Claim a random unclaimed territory
	 * @see riskarena.RiskBot#claimTerritory()
	 */
	public void claimTerritory() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		ArrayList<Integer> unclaimed = new ArrayList<Integer>();
		
		for(int i=0;i<countries.length;i++) {
			if(!countries[i].isTaken()) {
				unclaimed.add(new Integer(i));
			}
		}
		int choice = gen.nextInt(unclaimed.size());
		to_game.sendInt(unclaimed.get(choice));
	}

	/*
	 * Fortify a random territory with all armies that need to be placed
	 * @see riskarena.RiskBot#fortifyTerritory(int)
	 */
	public void fortifyTerritory(int num_to_place) {
		CountryInfo[] countries = risk_info.getCountryInfo();
		ArrayList<Integer> mine = new ArrayList<Integer>();

		for(int i=0;i<countries.length;i++) {
			if(countries[i].getPlayer() == risk_info.me()) {
				mine.add(new Integer(i));
			}
		}
		int choice = gen.nextInt(mine.size());

		to_game.sendInt(mine.get(choice).intValue());
		to_game.sendInt(num_to_place);
	}

	/*
	 * //TODO write this
	 * @see riskarena.RiskBot#launchAttack()
	 */
	public void launchAttack() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		if(!attacks.isEmpty()) {
			Pair<Integer,Integer> attack = attacks.peek();		// The attack currently being executed
			// Check to see if you've conquered the territory
			if(countries[attack.snd.intValue()].getPlayer() == risk_info.me()) {
				attacks.remove();
				return;
			}
			// Check to see if you've run out of armies to attack with :(
			if(countries[attack.fst.intValue()].getArmies() == 1) {
				attacks.remove();
				return;
			}
			to_game.sendInt(attack.fst);
			to_game.sendInt(attack.snd);
			to_game.sendInt(Math.min(countries[attack.fst.intValue()].getArmies()-1, 3)); // Attack with all you've got!
		} else
			to_game.sendInt(-1);
	}

	/*
	 * After a victory, always choose to occupy the gained territory with as many armies as possible 
	 * @see riskarena.RiskBot#fortifyAfterVictory(int, int, int, int)
	 */
	public void fortifyAfterVictory(int attacker, int defender, int min, int max) {
		to_game.sendInt(max);
	}

	/*
	 * Yes, always choose to turn in a set when given a choice
	 * @see riskarena.RiskBot#chooseToTurnInSet()
	 */
	public void chooseToTurnInSet() {
		to_game.sendInt(1);
	}

	/*
	 * Choose the first possible card set given
	 * @see riskarena.RiskBot#chooseCardSet(int[][])
	 */
	public void chooseCardSet(int[][] possible_sets) {
		to_game.sendInt(0);
	}

	/*
	 * Of all possible movements of all armies from one friendly territory to another, choose one randomly
	 * @see riskarena.RiskBot#fortifyPosition()
	 */
	public void fortifyPosition() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		ArrayList< Pair<Integer, Integer> > possibleForts = new ArrayList< Pair<Integer, Integer> >();
		for(int i=0; i<countries.length; i++) {
			if(countries[i].getPlayer() == risk_info.me() && countries[i].getArmies() > 1) {
				int[] adj = world.getAdjacencies(i);
				for(int j=0;j<adj.length; j++) {
					if(countries[adj[j]].getPlayer() == risk_info.me())
						possibleForts.add(new Pair<Integer,Integer>(new Integer(i), new Integer(j)) );
				}
			}
		}
		int choice = gen.nextInt(possibleForts.size());
		to_game.sendInt(possibleForts.get(choice).fst);
		to_game.sendInt(possibleForts.get(choice).snd);
		to_game.sendInt(countries[possibleForts.get(choice).fst.intValue()].getArmies() - 1);
	}

}
