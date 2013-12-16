package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.PlayerInfo;
import riskarena.Risk;
import riskarena.World;

public class GameStats {
	private GameInfo game;
	private CountryInfo countries[];	// Stored at the class level so individual calculation
										// methods don't need to have fresh copies made.
	private PlayerInfo players[];
	private World world;				// Stored at the class level and only set once because
										// this never changes (mere adjacency info)
	
	/*
	 * Static continent ratings that represent a score for each continent, using only
	 * state-independent info like #territories, #borders, and the continent army bonus 
	 */
	private double continentRatings[];
	/*
	 *  Array of size of the number of continents. Each number is
	 *	the player ID of someone that owns the entire continent.
	 *	If no one does, the int will be -1;
	 */
	private int continentOwnership[];
	/*		Number of armies each player has on the board		*/
	private int armies[];
	/*		Total number of armies on the board			*/
	private int totalArmies;
	/*		Number of territories occupied by a given player ID		*/
	private int occupationCounts[];
	/*		A list of country IDs belonging to this player		*/
	private ArrayList<Integer> myCountries;
	/*		A list of country IDs belonging to this player that are adjacent to enemy territories	*/
	private ArrayList<Integer> frontier;
	/*		The ID of a continent worth the player's focus in future attacks. -1 if no target	*/
	private int target;
	
	public GameStats(GameInfo initial) {
		game = initial;
		world = game.getWorldInfo();	// Only grab this once, it never changes
		myCountries = new ArrayList<Integer>();
		frontier = new ArrayList<Integer>();
		countries = game.getCountryInfo();
		rateContinents();				// Only rate these once, they never change
		refresh();
	}
	
	//	Alerts GameStats that the world has changed, so reload and recalculate!
	public void refresh() {
		countries = game.getCountryInfo();
		players = game.getPlayerInfo();
		calculate();
		setTargetCont();	// Select a target continent (there's an Evaluator that gives a score bump for going after this)
	}
	
	public void apply(ArrayList<ArmyChange> changes) {
		for(ArmyChange change : changes) {
			armies[countries[change.ID()].getPlayer()] += change.amount();
			totalArmies += change.amount();
		}
	}
	
	public void unapply(ArrayList<ArmyChange> changes) {
		for(ArmyChange change : changes) {
			armies[countries[change.ID()].getPlayer()] -= change.amount();
			totalArmies -= change.amount();
		}
	}
	
	// Called to re-calculate all stat variables.
	// Assumes all state variables like "countries" are up-to-date
	private void calculate() {
		calculateContentOwnership();
		calculateArmyTotals();
		calculateOccupationStats();
	}
	
	/*
	 * Responsible for filling in the continentOwnership array.
	 * Each number is the player ID of someone that owns the entire continent.
	 * If no one does, the int will be -1;
	 */
	private void calculateContentOwnership() {
		continentOwnership = new int[game.getNumContinents()];
		// Initialize each content ownership to -2, which indicates the
		// continent has yet to be examined.
		java.util.Arrays.fill(continentOwnership, -2);
		for(int i=0; i<countries.length; i++) {
			int cont = countries[i].getCont();
			if(continentOwnership[cont] == -1)	// Already know no one owns it
				continue;
			if(!countries[i].isTaken())		// If it's unclaimed, no one owns it
				continentOwnership[cont] = -1;
			else if(continentOwnership[cont] == -2)
				continentOwnership[cont] = countries[i].getPlayer();
			else if(continentOwnership[cont] != countries[i].getPlayer())
				continentOwnership[cont] = -1;
		}
	}
	
	/*
	 * Responsible for filling in the armies array with army total for each player,
	 * as well as the totalArmies int.
	 */
	private void calculateArmyTotals() {
		armies = new int[game.getMaxPlayerID()];
		totalArmies = 0;
		for(int i=0; i<countries.length; i++) {
			if(countries[i].isTaken()) {
				armies[countries[i].getPlayer()] += countries[i].getArmies();
				totalArmies += countries[i].getArmies();
			}
		}
	}
	
	/*
	 * Responsible for filling the occupationTotals array, and myCountries and frontier ArrayLists
	 * occupationTotals[i] represents the number of territories occupied by the player with ID i
	 * myCountries is a list of the country ID's belonging to this player.
	 * frontier is a list of country ID's belonging to this player and adjacent to enemy territories.
	 */
	private void calculateOccupationStats() {
		occupationCounts = new int[game.getMaxPlayerID()];
		myCountries.clear();
		frontier.clear();
		for(int i=0; i<countries.length; i++) {
			if(countries[i].isTaken()) {
				occupationCounts[countries[i].getPlayer()]++;
				if(countries[i].getPlayer() == game.me()) {
					myCountries.add(new Integer(i));
					int adj[] = world.getAdjacencies(i);
					// Check to see if countries[i] is on the player's frontier
					for(int a = 0; a<adj.length; a++) {
						if(countries[adj[a]].getPlayer() != game.me()) {
							frontier.add(new Integer(i));
							break;
						}
					}
				}
			}
		}
	}
	
	/* Responsible for filling the continentRatings array with scores for each
	 * continent they reflect how "good" they are. These use only
	 * state-independent info: #territories, #borders, and the continent army bonus 
	 */
	private void rateContinents() {
		continentRatings = new double[game.getNumContinents()];
		
		// For each continent, find its size and number of borders
		int numTerritories[] = new int[game.getNumContinents()];
		int numBorders[] = new int[game.getNumContinents()];
		for(int i=0; i<countries.length; i++) {
			numTerritories[countries[i].getCont()]++;
			int adj[] = world.getAdjacencies(i);
			for(int a = 0; a<adj.length; a++) {
				if(countries[i].getCont() != countries[adj[a]].getCont()) {
					numBorders[countries[i].getCont()]++;
					break;
				}
			}
		}
		// Use the above along with continent bonus info to calculate a score
		// Right now this one comes from Wolf's thesis
		int bonuses[] = game.getContinentBonuses();
		for(int i=0; i<continentRatings.length; i++) {
			int numerator = 15 + bonuses[i] - 4 * numBorders[i];
			continentRatings[i] = numerator/(double)numTerritories[i];
		}
	}
	
	/*
	 *  Responsible for setting "target" with the continent ID of the most
	 *  worthwhile continent for the player to focus its future attacks.
	 *  This is a formula based on:
	 *  - The % of territories occupied by the player. Weighted by alpha.
	 *  - The ratio of the number of enemy armies in the continent to the number of
	 *  	friendly armies both in the continent and in immediately surrounding territories
	 *  	that could swiftly invade. Weighted by beta.
	 *  - The static continent rating (normalized). Weighted by gamma.
	 *  Obviously doesn't choose continents with 100% ownership
	 */
	private void setTargetCont() {
		final double alpha = 8.0, beta = 1.0, gamma = 2.0;
		
		int contBonuses[] = game.getContinentBonuses();
		double contBonusRatios[] = new double[game.getNumContinents()];	// Normalized continent bonuses
		int totalBonuses = 0;
		for(int i=0; i<game.getNumContinents(); i++) {
			totalBonuses += contBonuses[i];
			contBonusRatios[i] = (double)contBonuses[i];
		}
		for(int i=0; i<game.getNumContinents(); i++)
			contBonusRatios[i] /= totalBonuses;
		
		int friendliesPerCont[] = new int[game.getNumContinents()];
		int enemiesPerCont[] = new int[game.getNumContinents()];
		int friendlyArmiesPerCont[] = new int[game.getNumContinents()];	// Only counts "extra" armies, includes neighbors
		int enemyArmiesPerCont[] = new int[game.getNumContinents()];	// Only counts "extra" armies
		for(int i=0; i<countries.length; i++) {
			int cont = countries[i].getCont();
			/*if(!countries[i].isTaken()) {		// Target is irrelevant in the beginning claiming phase
				target = -1;
				return;
			}*/
			if(countries[i].getPlayer() == game.me()) {
				friendliesPerCont[cont] += 1;
				friendlyArmiesPerCont[cont] += countries[i].getArmies() - 1;
				
				// Also include these armies in other continent counts
				int adj[] = world.getAdjacencies(i);
				boolean counts[] = new boolean[game.getNumContinents()];
				for(int a=0; a<adj.length; a++) {
					if(countries[adj[a]].getCont() != cont)
						counts[countries[adj[a]].getCont()] = true;
				}
				for(int a=0; a<counts.length; a++) {
					if(counts[a])
						friendlyArmiesPerCont[a] += countries[i].getArmies() - 1;
				}
			} else {
				enemiesPerCont[cont] += 1;
				enemyArmiesPerCont[cont] += countries[i].getArmies() - 1;
			}
		}
		double occupationRatios[] = new double[game.getNumContinents()], armyRatios[] = new double[game.getNumContinents()];
		for(int i=0; i<game.getNumContinents(); i++) {
			occupationRatios[i] = friendliesPerCont[i] / ((double)friendliesPerCont[i] + enemiesPerCont[i]);
			if(friendlyArmiesPerCont[i] + enemyArmiesPerCont[i] == 0)
				armyRatios[i] = 0.0;
			else
				armyRatios[i] = friendlyArmiesPerCont[i] / ((double)friendlyArmiesPerCont[i] + enemyArmiesPerCont[i]);
		}
		
		double highestScore = -1 * Double.MAX_VALUE;
		int winner = -1;
		for(int i=0; i<game.getNumContinents(); i++) {
			if(Math.abs(occupationRatios[i] - 1.0) < 0.0000001)		// Fully occupies, don't choose
				continue;
			double score = alpha * occupationRatios[i] + beta * armyRatios[i] + gamma * contBonusRatios[i];
			if(score > highestScore) {
				highestScore = score;
				winner = i;
			}
		}
		target = winner;
		//Risk.sayOutput("Target: " + target, OutputFormat.BLUE);
	}
	
	// Returns the number of continents
	public int getNumContinents() {
		return game.getNumContinents();
	}
	
	// Gets the number of players still in the game
	public int getNumPlayers() {
		return players.length;
	}
	
	/*********************** GETTERS ****************************/
	
	public int[] getContinentOwnership() {
		return continentOwnership;
	}
	
	public double[] getContinentRatings() {
		return continentRatings;
	}
	
	public int[] getArmiesPerPlayer() {
		return armies;
	}
	
	public int getTotalArmies() {
		return totalArmies;
	}
	
	public int[] getOccupationCounts() {
		return occupationCounts;
	}
	
	public int getTarget() {
		return target;
	}
	
	public PlayerInfo[] getPlayers() {
		return players;
	}
	
	public CountryInfo[] getCountries() {
		return countries;
	}
	
	public World getWorld() {
		return world;
	}
	
	public ArrayList<Integer> getMyCountries() {
		return myCountries;
	}
	
	public ArrayList<Integer> getFrontier() {
		return frontier;
	}
	
}
