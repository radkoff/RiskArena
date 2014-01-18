/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * This class represents a country/territory in Risk. It keeps track of its
 * name, the id of the player who occupies it, the number of occupying armies,
 * the position within the map, the CountryLabelVector (defined below) and the
 * continent id.
 * 
 */

import java.awt.Point;


public class Country implements CountryInterface {
	private String name;		// Name of territory
	private int player;			// ID of the occupying player
	private int armies;			// Number of armies currently occupying
	private int continent;		// ID of the continent it belongs to

	private Point position;
	private CountryLabelVector clv;

	public Country(Country c) {
		name = c.name;
		player = c.player;
		armies = c.armies;
		position = c.getPosition();
		continent = c.continent;
		clv = c.clv;
	}
	public Country(String n, Point _position, int cont, CountryLabelVector _clv) {
		name = n;
		position = _position;
		player = -1;
		armies = 0;
		continent = cont;
		clv = _clv;
	}

	///////////////// Getters n Setters ////////////////

	public String getName() {
		return name;
	}
	public int getCont() {
		return continent;
	}
	public int getPlayer() {
		return player;
	}
	public int getArmies() {
		return armies;
	}

	// The metric for if a territory is claimed at the beginning of the game.
	public boolean isTaken() {
		if(armies <= 0)
			return false;
		else
			return true;
	}

	public CountryLabelVector getCLV() {
		return clv;
	}
	public Point getPosition() {
		return position;
	}

	public void setName(String n) {
		name = n;
	}
	public void setCont(int c) {
		continent = c;
	}
	public void setPlayer(int p) {
		player = p;
	}
	public void setArmies(int a) {
		armies = a;
	}
	public void setCLV(CountryLabelVector c) {
		clv = c;
	}
	public void setPosition(Point p) {
		position = p;
	}


	/*
	 * Keep track of three pieces of information regarding a territory's label:
	 * The radial direction according to the country's node, how far away the text
	 * should be (magnitude), and the size of the text.
	 */
	public static class CountryLabelVector {
		private float direction; // 0.00: 12 oclock -----clockwise------> 1.00
		private int magnitude; // number 1 through 5, 1 means closest to the node and 5 means farthest
		private int font_size; // font size of the label. Number 1 (smallest) through 5 (largest)
		public CountryLabelVector(float _direction, int _magnitude, int _size) {
			direction = _direction;
			magnitude = _magnitude;
			font_size = _size;
		}
		public float getDirection() {
			return direction;
		}
		public int getMagnitude() {
			return magnitude;
		}
		public int getFontSize() {
			return font_size;
		}

		public void setDirection(float d) {
			direction = d;
		}
		public void setMagnitude(int m) {
			magnitude = m;
		}
		public void setFontSize(int f) {
			font_size = f;
		}

	}
}
