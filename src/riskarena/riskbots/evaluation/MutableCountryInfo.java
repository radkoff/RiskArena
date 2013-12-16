package riskarena.riskbots.evaluation;

import riskarena.CountryInterface;

public class MutableCountryInfo implements CountryInterface {

	private String name;
	private int cont;
	private int player;
	private int armies;
	private boolean isTaken;
	
	public MutableCountryInfo(CountryInterface country) {
		setName(country.getName());
		setCont(country.getCont());
		setPlayer(country.getPlayer());
		setArmies(country.getArmies());
		setTaken(country.isTaken());
	}
	
	public String getName() {		// get the country's name. A bit useless for bots, but here it is.
		return name;
	}
	public int getCont() {			// get the ID on the continent it belongs to
		return cont;
	}
	public int getPlayer() {		// get the ID of the player that occupies this territory
		return player;
	}
	public int getArmies() {		// get how many armies are occupying it
		return armies;
	}
	// returns true if this country has been claimed
	public boolean isTaken() {
		return isTaken;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setCont(int cont) {
		this.cont = cont;
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	public void setArmies(int armies) {
		this.armies = armies;
	}

	public void setTaken(boolean isTaken) {
		this.isTaken = isTaken;
	}
}
