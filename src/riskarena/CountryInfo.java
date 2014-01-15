/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * The CountryInfo class is a way of hiding certain methods of Country from
 * bots, allowing them to only access some getters and isTaken().
 * 
 * Evan Radkoff
 */

public class CountryInfo implements CountryInterface {
	private final CountryInterface country;
	public CountryInfo(CountryInterface wrapped) {
		country = wrapped;
	}
	public String getName() {		// get the country's name. A bit useless for bots, but here it is.
		return country.getName();
	}
	public int getCont() {			// get the ID on the continent it belongs to
		return country.getCont();
	}
	public int getPlayer() {		// get the ID of the player that occupies this territory
		return country.getPlayer();
	}
	public int getArmies() {		// get how many armies are occupying it
		return country.getArmies();
	}
	// returns true if this country has been claimed
	public boolean isTaken() {
		return country.isTaken();
	}
}
