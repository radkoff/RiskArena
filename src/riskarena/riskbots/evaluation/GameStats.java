package riskarena.riskbots.evaluation;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.PlayerInfo;
import riskarena.World;

public class GameStats {
	private GameInfo game;
	private CountryInfo countries[];
	private World world;
	
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
	
	
	public GameStats(GameInfo initial) {
		game = initial;
		world = game.getWorldInfo();	// Only grab this once, it never changes
		rateContinents();
	}
	
	public void refresh() {
		countries = game.getCountryInfo();
		calculate();
	}
	
	private void calculate() {
		calculateContentOwnership();
	}
	
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
	
	private void rateContinents() {
		// TODO
	}
}
