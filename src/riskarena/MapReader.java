/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * Given a .map filename, MapReader reads the XML file, parses it using dom4j,
 * and extracts world information like Countries and adjacencies. It makes this info
 * public to the game engine with some getter methods.
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import riskarena.graphics.Pretty;

public class MapReader {
	// The CountryLabelVector information for each territory is optional, so these are the defaults
	private final float default_clv_direction = 0.0f;
	private final int default_clv_magnitude = 2;
	private final int default_clv_fontsize = 2;

	private final int minimum_continents = 1;
	private final int minimum_countries = 2;
	private final int minimum_adjacencies = 1;

	private int NUM_COUNTRIES, NUM_CONTINENTS;

	// As new values are read in for Countries, continents, colors, etc, these
	// ArrayLists fill up with data
	private ArrayList<Country> countries = new ArrayList<Country>();
	private ArrayList<String> continent_names = new ArrayList<String>();
	private ArrayList<Integer> bonuses = new ArrayList<Integer>();
	private ArrayList<Color> colors = new ArrayList<Color>();
	private ArrayList<Adjacency> adjacencies;

	public MapReader(String filename) throws Exception {
		Document document;
		try{
			// Parse the contained XML file using dom4j
			SAXReader reader = new SAXReader();
			document = reader.read(filename);
		} catch(DocumentException de) {
			throw new Exception("Unable to parse XML file (" + de.getMessage() + ")");
		}
		Element root = document.getRootElement();

		// Each continent element
		for ( Iterator i = root.elementIterator( "continent" ); i.hasNext(); ) {
			Element cont = (Element) i.next();
			int continent_index = continent_names.size();
			float r_value = 0.0f, b_value = 0.0f, g_value = 0.0f;

			boolean found_name = false, found_bonus = false, found_territory = false, found_r = false, found_g = false, found_b = false;

			// Verifying that a name exists and extracting its value
			for ( Iterator j = cont.elementIterator( "name" ); j.hasNext(); ) {
				Element n = (Element) j.next();
				found_name = true;
				// Ensure that the continent's name isn't already taken
				for(int k = 0;k<continent_names.size();k++) {
					if(continent_names.get(k).equals(n.getText()))
						throw new Exception("The continent " + n.getText() + " is listed twice.");
				}
				continent_names.add(n.getText());
				break;
			}
			if(!found_name)
				throw new Exception("No name found for some continent.");

			// Verifying that an army bonus exists and extracting its value
			for ( Iterator j = cont.elementIterator( "bonus" ); j.hasNext(); ) {
				Element n = (Element) j.next();
				found_bonus = true;
				Integer b = new Integer(n.getText());
				if(b.intValue() < 0) {
					throw new Exception("Some continent has a negative army bonus.");
				}
				bonuses.add(b);
				break;
			}
			if(!found_bonus)
				throw new Exception("No bonus found for " + continent_names.get(continent_index) + ".");

			// Verifying that a Red value for the continent's Color exists and extracting its value
			for ( Iterator j = cont.elementIterator( "R" ); j.hasNext(); ) {
				Element n = (Element) j.next();
				found_r = true;
				Float b = new Float(n.getText());
				if(b.floatValue() < 0 || b.floatValue() > 1) {
					throw new Exception("The R value of " + continent_names.get(continent_index) + " is out of the 0.0-1.0 range.");
				}
				r_value = b;
				break;
			}
			if(!found_r)
				throw new Exception("No R value found for " + continent_names.get(continent_index) + ".");
			// Verifying that a Green value for the continent's Color exists and extracting its value
			for ( Iterator j = cont.elementIterator( "G" ); j.hasNext(); ) {
				Element n = (Element) j.next();
				found_g = true;
				Float b = new Float(n.getText());
				if(b.floatValue() < 0 || b.floatValue() > 1) {
					throw new Exception("The G value of " + continent_names.get(continent_index) + " is out of the 0.0-1.0 range.");
				}
				g_value = b;
				break;
			}
			if(!found_g)
				throw new Exception("No G value found for " + continent_names.get(continent_index) + ".");
			// Verifying that a Blue value for the continent's Color exists and extracting its value
			for ( Iterator j = cont.elementIterator( "B" ); j.hasNext(); ) {
				Element n = (Element) j.next();
				found_b = true;
				Float b = new Float(n.getText());
				if(b.floatValue() < 0 || b.floatValue() > 1) {
					throw new Exception("The B value of " + continent_names.get(continent_index) + " is out of the 0.0-1.0 range.");
				}
				b_value = b;
				break;
			}
			if(!found_b)
				throw new Exception("No B value found for " + continent_names.get(continent_index) + ".");
			colors.add(new Color(r_value, g_value, b_value));

			// Each territory listed within that continent
			for ( Iterator j = cont.elementIterator( "territory" ); j.hasNext(); ) {
				Element terr = (Element) j.next();
				found_territory = true;
				boolean name_exists = false, x_exists = false, y_exists = false;

				String name = "";
				Point pos = new Point();
				Country.CountryLabelVector clv = new Country.CountryLabelVector(default_clv_direction, default_clv_magnitude, default_clv_fontsize);

				// The territory's name
				for ( Iterator k = terr.elementIterator( "name" ); k.hasNext(); ) {
					Element n = (Element) k.next();
					name_exists = true;
					name = n.getText();
					for(int u = 0;u<countries.size();u++) {
						if(countries.get(u).getName().equals(name))
							throw new Exception("The territory " + name + " is listed twice.");
					}
					break;
				}
				if(!name_exists)
					throw new Exception("No name found for some territory in " + continent_names.get(continent_index) + ".");

				// The X position of the territory
				for ( Iterator k = terr.elementIterator( "x" ); k.hasNext(); ) {
					Element n = (Element) k.next();
					x_exists = true;
					pos.x = (new Integer(n.getText())).intValue();
					break;
				}
				if(!x_exists)
					throw new Exception("No x position given for " + name + ".");

				// The Y position of the territory
				for ( Iterator k = terr.elementIterator( "y" ); k.hasNext(); ) {
					Element n = (Element) k.next();
					y_exists = true;
					pos.y = (new Integer(n.getText())).intValue();
					break;
				}
				if(!y_exists)
					throw new Exception("No y position given for " + name + ".");

				// A CountryVectorLabel specification (optional)
				for ( Iterator k = terr.elementIterator( "label" ); k.hasNext(); ) {
					Element n = (Element) k.next();

					// The country's label's radial direction (optional)
					for ( Iterator u = n.elementIterator( "direction" ); u.hasNext(); ) {
						Element dir = (Element) u.next();
						float d = (new Float(dir.getText())).floatValue();
						if(d < 0.0 || d > 1.0) {
							throw new Exception("Some territory label direction is out of bounds.");
						}
						clv.setDirection(d);
					}
					// The country's label's distance from the node (optional)
					// For the bounds of this value see Graphics.min_mag and Graphics.max_mag
					for ( Iterator u = n.elementIterator( "distance" ); u.hasNext(); ) {
						Element mag = (Element) u.next();
						int d = (new Integer(mag.getText())).intValue();
						if(d < Pretty.min_mag || d > Pretty.max_mag) {
							throw new Exception("Some territory label distance is out of bounds.");
						}
						clv.setMagnitude(d);
					}
					// The country's label's font size
					// For the bounds of this value see Graphics.min_size and Graphics.max_size
					for ( Iterator u = n.elementIterator( "size" ); u.hasNext(); ) {
						Element font = (Element) u.next();
						int d = (new Integer(font.getText())).intValue();
						if(d < Pretty.min_size || d > Pretty.max_size) {
							throw new Exception("Some territory label size is out of bounds.");
						}
						clv.setFontSize(d);
					}
				}

				// Construct a new Country object with the gathered information
				countries.add(new Country(name, pos, continent_index, clv));
			}
			// If no territories were found, you dun goof'ed
			if(!found_territory)
				throw new Exception("Some continent has no territories.");
		}

		NUM_CONTINENTS = continent_names.size();
		NUM_COUNTRIES = countries.size();
		if(NUM_CONTINENTS < minimum_continents)
			throw new Exception("Only " + NUM_CONTINENTS + " continents present, minimum of " + minimum_continents + " needed.");
		if(NUM_COUNTRIES < minimum_countries)
			throw new Exception("Only " + NUM_COUNTRIES + " territories present, minimum of " + minimum_countries + " needed.");

		// Read all adjacencies
		int adj_count = 0;
		adjacencies = new ArrayList<Adjacency>();
		for ( Iterator i = root.elementIterator( "adjacency" ); i.hasNext(); ) {
			Element some_adj = (Element) i.next();
			adj_count++;
			boolean from_exists = false, to_exists = false;
			String from="", to="";		// When read from the map file, the "from" and "to" of the adjacency
			int crosses_edge = Adjacency.CROSS_NONE;	// The edge crossing policy of the adjacency
			
			for ( Iterator j = some_adj.elementIterator( "from" ); j.hasNext(); ) {
				Element f = (Element) j.next();
				from_exists = true;
				from = f.getText();
				break;
			}
			for ( Iterator j = some_adj.elementIterator( "to" ); j.hasNext(); ) {
				Element t = (Element) j.next();
				to_exists = true;
				to = t.getText();
				break;
			}
			// 
			for ( Iterator j = some_adj.elementIterator( "cross" ); j.hasNext(); ) {
				Element t = (Element) j.next();
				if(t.getText().equalsIgnoreCase("horizontal"))
					crosses_edge = Adjacency.CROSS_HORIZONTAL;
				else if(t.getText().equalsIgnoreCase("vertical"))
					crosses_edge = Adjacency.CROSS_VERTICAL;
				else if(t.getText().equalsIgnoreCase("diagonalright"))
					crosses_edge = Adjacency.CROSS_DIAG_RIGHT;
				else if(t.getText().equalsIgnoreCase("diagonalleft"))
					crosses_edge = Adjacency.CROSS_DIAG_LEFT;
				else
					throw new Exception("Invalid edge-crossing policy for adjacency number " + adjacencies.size() + ". Choose "
							+ "eighter Horizontal, Vertical, DiagonalRight, or DiagonalLeft.");
				break;
			}
			if(!from_exists || !to_exists)
				throw new Exception("Adjacency number " + adj_count + " is missing a \"from\" or \"to\" tag.");
			adjacencies.add(new Adjacency(from, to, crosses_edge));
		}
		if(adj_count < minimum_adjacencies)
			throw new Exception("Only " + adj_count + " adjacencies present, minimum of " + minimum_adjacencies + " needed.");

		// The following two loops use a hashmap of country names to check that all adjacency values are legit
		// In the process of converting from country names to ID's, it also sets the FromID and ToID variables of the Adjacency object
		HashMap<String,Integer> hm = new HashMap<String,Integer>();
		for(int i=0;i<countries.size();i++)
			hm.put(countries.get(i).getName(), new Integer(i));
		for(int i=0;i<adjacencies.size();i++) {
			Integer from = hm.get(adjacencies.get(i).getFrom());
			Integer to = hm.get(adjacencies.get(i).getTo());
			if(from == null || to == null)
				throw new Exception("<from> or <to> in an adjacency is a territory that doesn't exist.");
			adjacencies.get(i).setFromID(from.intValue());
			adjacencies.get(i).setToID(to.intValue());
		}

	}

	// The following getters provide arrays of game information to the Game class

	public Country[] getCountries() {
		Country country_arr[] = new Country[NUM_COUNTRIES];
		for(int i=0;i<NUM_COUNTRIES;i++) {
			country_arr[i] = countries.get(i);
		}
		return country_arr;
	}

	public String[] getContinentNames() {
		String cont_arr[] = new String[NUM_CONTINENTS];
		for(int i=0;i<NUM_CONTINENTS;i++) {
			cont_arr[i] = continent_names.get(i);
		}
		return cont_arr;
	}

	public int[] getContinentBonuses() {
		int b[] = new int[bonuses.size()];
		for(int i=0;i<bonuses.size();i++) b[i] = bonuses.get(i);
		return b;
	}

	public Color[] getContinentColors() {
		Color c[] = new Color[colors.size()];
		for(int i=0;i<colors.size();i++) c[i] = colors.get(i);
		return c;
	}

	public ArrayList<Adjacency> getAdjacencyInfo() {
		return adjacencies;
	}
}
