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
	
	public GameStats(GameInfo initial) {
		game = initial;
		world = game.getWorldInfo();	// Only grab this once, it never changes
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
		continentOwnership = new int[game.getContinentBonuses().length];
		// Initialize each content ownership to -2, which indicates the
		// continent has yet to be examined.
		for(int i=0; i<continentOwnership.length; i++) {
			continentOwnership[i] = -2;
		}
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
		armies = new int[players.length];
		totalArmies = 0;
		for(int i=0; i<countries.length; i++) {
			if(countries[i].isTaken()) {
				armies[countries[i].getPlayer()] += countries[i].getArmies();
				totalArmies += countries[i].getArmies();
			}
		}
	}
	
	/*
	 * Responsible for filling the occupationTotals array and myCountries ArrayList
	 * occupationTotals[i] represents the number of territories occupied by the player with ID i
	 * myCountries is a list of the country ID's belonging to this player.
	 */
	private void calculateOccupationStats() {
		occupationCounts = new int[players.length];
		myCountries = new ArrayList<Integer>();
		for(int i=0; i<countries.length; i++) {
			if(countries[i].isTaken()) {
				occupationCounts[countries[i].getPlayer()]++;
				if(countries[i].getPlayer() == game.me())
					myCountries.add(new Integer(i));
			}
		}
	}
	
	/* Responsible for filling the continentRatings array with scores for each
	 * continent they reflect how "good" they are. These use only
	 * state-independent info: #territories, #borders, and the continent army bonus 
	 */
	private void rateContinents() {
		// TODO
	}
	
	/*********************** GETTERS ****************************/
	
	public int[] getContinentOwnership() {
		return continentOwnership;
	}
	
}
