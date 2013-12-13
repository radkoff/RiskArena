package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.PlayerInfo;
import riskarena.World;

public class GameStats {
	private GameInfo game;
	private CountryInfo countries[];	// Stored at the class level so individual calculation
										// methods don't need to have fresh copies made.
	private PlayerInfo players[];
	private World world;				// Stored at the class level and only set once because
										// this never changes (mere adjacency info)
	
	/*
	 * Ratings that represent a score for each continent, using only
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
	
	public GameStats(GameInfo initial) {
		game = initial;
		world = game.getWorldInfo();	// Only grab this once, it never changes
		myCountries = new ArrayList<Integer>();
		frontier = new ArrayList<Integer>();
		refresh();
		rateContinents();				// Only rate these once, they never change
	}
	
	//	Alerts GameStats that the world has changed, so reload and recalculate!
	public void refresh() {
		countries = game.getCountryInfo();
		players = game.getPlayerInfo();
		calculate();
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
	
	// Returns the number of continents owned by the player with ID player_id
	public int getNumContinentsOwnedBy(int player_id) {
		int answer = 0;
		for(int i=0; i<continentOwnership.length; i++) {
			if(continentOwnership[i] == player_id)
				answer++;
		}
		return answer;
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
	
	public int[] getArmiesPerPlayer() {
		return armies;
	}
	
	public int getTotalArmies() {
		return totalArmies;
	}
	
	public int[] getOccupationCounts() {
		return occupationCounts;
	}
	
	public PlayerInfo[] getPlayers() {
		return players;
	}
	
	public CountryInfo[] getCountries() {
		return countries;
	}
	
	public ArrayList<Integer> getMyCountries() {
		return myCountries;
	}
	
	public ArrayList<Integer> getFrontier() {
		return frontier;
	}
	
}
