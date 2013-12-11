package riskarena.graphics;
/*
 * Pretty is the panel where the game board is drawn.
 * 
 * Evan Radkoff
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import riskarena.Adjacency;
import riskarena.Country;
import riskarena.GameData;
import riskarena.Risk;

public class Pretty extends JPanel {
	private int WIDTH, HEIGHT;
	protected final Color BGCOLOR = Risk.UGLY_GREY;	// Background color, currently set to some ugly grey
	protected final int country_circle_radius = 16;	// The radius of each territory node (in pixels)
	private final int shadow_distance = 2;	// To make text stand out, it is all drawn twice, with one of the
	// times shadow_distance away and behind the other.
	private int single_digit_army_label_size = 26;	// When the army amount has a single digit, use this text size
	private int double_digit_army_label_size = 20;	// When the army amount is double digit, use this text size
	private int triple_digit_army_label_size = 14; // When the army amount has three digits, use this text size

	private GameData game;		// Reference to the game engine instance being drawn
	private ArrayList<Adjacency> adjacencies;
	private Point2D.Float[] country_positions;	// The normalized coordinates of each territory

	public static int min_size = 1, max_size = 5, min_mag = 1, max_mag = 5;

	public Pretty(int w, int h) {
		WIDTH = w;
		HEIGHT = h;
		initUI();
	}
	private void initUI() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		repaint();
	}
	// Overrides the JPanel paintComponent method. First paints adjacencies, then territories
	public void paintComponent(java.awt.Graphics g) {
		g.setColor(BGCOLOR);
		g.clearRect(0, 0, WIDTH, HEIGHT);
		Graphics2D g2d = (Graphics2D) g;	// Graphics2D is used instead because it has more capabilities
		g2d.setBackground(BGCOLOR);
		g2d.setColor(BGCOLOR);
		g2d.clearRect(0, 0, WIDTH, HEIGHT);
		if(game == null) return;	// If sendGame has yet to be called, there is nothing to draw

		// Anti-aliasing:
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		drawAdjacencies(g2d);
		drawCountries(g2d);
	}

	
	public void sendGameData(GameData g) {
		game = g;
		convertCountryCoordinates();
	}
	
	public void sendAdjacencies(ArrayList<Adjacency> adjs) {
		adjacencies = adjs;
	}

	/*
	 * drawCountries() draws a node representing each country on the board.
	 * The color of the name indicates the country's continent. The color of
	 * the node indicates which player owns it. The number on the node indicates
	 * how many armies currently occupy it.
	 */
	private void drawCountries(Graphics2D g2d) {
		for(int i=0; i < game.NUM_COUNTRIES; i++) {
			Country country = game.getCountry(i);
			// *********** Draw country circle ************
			int circle_stroke_width = 2;
			Ellipse2D.Float circle = new Ellipse2D.Float();
			circle.width = country_circle_radius*2;
			circle.height = country_circle_radius*2;
			circle.x = country_positions[i].x;
			circle.y = country_positions[i].y;
			g2d.setColor(game.getPlayerColor(country.getPlayer()));
			g2d.fill(circle);
			g2d.setColor(game.getContinentColor(country.getCont()));
			Stroke old_stroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(circle_stroke_width));
			g2d.draw(circle);
			g2d.setStroke(old_stroke);

			// ************ Draw country label ************
			float label_direction = country.getCLV().getDirection();
			// Convert direction to 0.00 at 12 oclock -----clockwise------> 1.00 at 12 oclock
			if(label_direction > 0.5f)
				label_direction -= 1;
			label_direction = 0.5f - label_direction;

			// The CountryLabelVector magnitude is how far away the label text should be.
			// 1 is closest, 5 is furthest. Each computed radius is based on country_circle_radius
			int label_magnitude = country.getCLV().getMagnitude();
			int label_radius = (int)(1.5*country_circle_radius);
			switch(label_magnitude) {
			case 1: label_radius = (int)(1.5*country_circle_radius);
			break;
			case 2: label_radius = (int)(2.0*country_circle_radius);
			break;
			case 3: label_radius = (int)(2.5*country_circle_radius);
			break;
			case 4: label_radius = (int)(3.0*country_circle_radius);
			break;
			case 5: label_radius = (int)(4.0*country_circle_radius);
			break;
			default:
				Risk.sayError("Warning: invalid country label radius " + label_magnitude + ". Using 2 instead");
				break;
			}

			// How big the font size should be based on the CountryLabelVector's size element
			int country_label_size = 16;
			switch(country.getCLV().getFontSize()) {
			case 1: country_label_size = 14;
			break;
			case 2: country_label_size = 16;
			break;
			case 3: country_label_size = 18;
			break;
			case 4: country_label_size = 20;
			break;
			case 5: country_label_size = 22;
			break;
			default:
				Risk.sayError("Warning: invalid country label size " + country.getCLV().getFontSize() + ". Using 2 instead");
				break;
			}
			// Make the text begin with the country's number
			String label_text = (i+1) + ". " + country.getName();
			Font label_font = FontMaker.makeCustomFont(country_label_size);
			g2d.setFont(label_font);

			FontMetrics textMetrics = g2d.getFontMetrics(label_font);
			int label_width = textMetrics.stringWidth(label_text);
			float label_x = circle.x + 2 + country_circle_radius/2;
			float label_y = circle.y - 3 + (1.5f*country_circle_radius);
			float delta_x = (float) (label_radius * Math.sin(2 * Math.PI * label_direction));
			float delta_y = (float) (label_radius * Math.cos(2 * Math.PI * label_direction));
			// If the label is to the left, it needs to end at the node instead of beginning with it
			if(label_direction > 0.5f) {
				delta_x -= label_width - country_circle_radius;
			}
			if(label_direction < 0.55f && label_direction > 0.45) {
				delta_x -= label_width/2;
			}
			// Double shadow:
			g2d.setColor(Color.black);
			g2d.drawString(label_text, label_x + delta_x + 1, label_y + delta_y + 1);
			g2d.setColor(Color.GRAY);
			g2d.drawString(label_text, label_x + delta_x - 1, label_y + delta_y - 1);

			g2d.setColor(game.getContinentColor(country.getCont()));
			g2d.drawString(label_text, label_x + delta_x, label_y + delta_y);


			// ************ Draw army amount *************
			if(country.getArmies() > 0) {
				if(country.getArmies() < 10) {		// Single digit
					g2d.setColor(Color.DARK_GRAY);
					g2d.setFont(FontMaker.makeCustomFont(single_digit_army_label_size));
					g2d.drawString(""+(country.getArmies()), circle.x + shadow_distance + country_circle_radius/2, circle.y + shadow_distance + (1.5f*country_circle_radius));
					g2d.setColor(Color.WHITE);
					//g2d.setFont(FontMaker.makeCustomFont(single_digit_army_label_size));
					g2d.drawString(""+(country.getArmies()), circle.x + country_circle_radius/2, circle.y + (1.5f*country_circle_radius));
				} else {		// Double/triple digit
					g2d.setColor(Color.DARK_GRAY);
					if(country.getArmies() < 100)
						g2d.setFont(FontMaker.makeCustomFont(double_digit_army_label_size));
					else
						g2d.setFont(FontMaker.makeCustomFont(triple_digit_army_label_size));
					g2d.drawString(""+(country.getArmies()), circle.x - 5 + shadow_distance + country_circle_radius/2, circle.y - 3 + shadow_distance + (1.5f*country_circle_radius));
					g2d.setColor(Color.WHITE);
					//g2d.setFont(FontMaker.makeCustomFont(double_digit_army_label_size));
					g2d.drawString(""+(country.getArmies()), circle.x - 5 + country_circle_radius/2, circle.y - 3 + (1.5f*country_circle_radius));
				}
			}
		}
	}

	// Draw all adjacency lines given in the adjacencies array and given contry_positions
	private void drawAdjacencies(Graphics2D g2d) {
		int line_width = 2;

		for(int i=0;i<adjacencies.size();i++) {
			// draw a line from countries[i] to countries[j]
			float x1 = (int)country_positions[adjacencies.get(i).fromCountryID()].x + country_circle_radius;
			float y1 = (int)country_positions[adjacencies.get(i).fromCountryID()].y + country_circle_radius;
			float x2 = (int)country_positions[adjacencies.get(i).toCountryID()].x + country_circle_radius;
			float y2 = (int)country_positions[adjacencies.get(i).toCountryID()].y + country_circle_radius;

			Stroke old_stroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(line_width));
			Country from = game.getCountry(adjacencies.get(i).fromCountryID());
			// If the adjacency is between two territories of the same continent, draw in the continent's color
			if(from.getCont() == game.getCountry(adjacencies.get(i).toCountryID()).getCont()) {
				g2d.setColor(game.getContinentColor(from.getCont()));
			} else {	// otherwise, draw it white
				g2d.setColor(Color.white);
			}
			
			/*
			 * The following ifelse handles the fact that some adjacency lines need to cross the edge and not go through
			 * the center of the map, like Alaska-Kamchatka in Earth.map. The 5 policies possible, found in Adjacency.java, are:
			 * CROSS_NONE - normal adjacency, no edge crossing
			 * CROSS_HORIZONTAL - crosses horizontally
			 * CROSS_VERTICAL - crosses the map edges vertically
			 * CROSS_DIAG_RIGHT - crosses by going diagonally up and to the right (and down and to the left)
			 * CROSS_DIAG_LEFT - crosses by going diagonally up and to the left (and down and to the right)
			 */
			if(adjacencies.get(i).getCrossPolicy() == Adjacency.CROSS_NONE) {
				drawLine(g2d, x1, y1, x2, y2);	// draw normally
			} else if(adjacencies.get(i).getCrossPolicy() == Adjacency.CROSS_HORIZONTAL) {
				if(x1 < x2) {		// Determine which country's location is left-most
					drawLine(g2d, x1, y1, x2 - WIDTH, y2);	// From the first country to outside the left edge
					drawLine(g2d, x2, y2, x1 + WIDTH, y1);	// From the second country to the outside of the right edge
				} else {
					drawLine(g2d, x2, y2, x1 - WIDTH, y1);	// From the second country to outside the left edge
					drawLine(g2d, x1, y1, x2 + WIDTH, y2);	// From the first country to the outside of the right edge
				}
			} else if(adjacencies.get(i).getCrossPolicy() == Adjacency.CROSS_VERTICAL) {
				if(y1 < y2) {		// Determine which country's location is upper-most (smaller y value)
					drawLine(g2d, x1, y1, x2, y2 - HEIGHT);	// From the first country to above the top of the map
					drawLine(g2d, x2, y2, x1, y1 + HEIGHT);	// From the second country to below the bottom of the map
				} else {
					drawLine(g2d, x2, y2, x1, y1 - HEIGHT);	// From the second country to above the top of the map
					drawLine(g2d, x1, y1, x2, y2 + HEIGHT);	// From the first country to below the bottom of the map
				}
			} else if(adjacencies.get(i).getCrossPolicy() == Adjacency.CROSS_DIAG_RIGHT) {
				// If the "from" country is closer to the top right corner (using a^2 + b^2 = c^2)
				if( (WIDTH-x1)*(WIDTH-x1) + (y1)*(y1) < (WIDTH-x2)*(WIDTH-x2) + (y2)*(y2) ) {
					drawLine(g2d, x1, y1, x2 + WIDTH, y2 - HEIGHT);	// From the first country, up and to the right
					drawLine(g2d, x2, y2, x1 - WIDTH, y1 + HEIGHT);	// From the second country, down and to the left
				} else {
					drawLine(g2d, x2, y2, x1 + WIDTH, y1 - HEIGHT);	// From the second country, up and to the right
					drawLine(g2d, x1, y1, x2 - WIDTH, y2 + HEIGHT);	// From the first country, down and to the left
				}
			} else if(adjacencies.get(i).getCrossPolicy() == Adjacency.CROSS_DIAG_LEFT) {
				if( (x1)*(x1) + (y1)*(y1) < (x2)*(x2) + (y2)*(y2) ) {	// Determine which country is closer to the top left corner
					drawLine(g2d, x1, y1, x2 - WIDTH, y2 - HEIGHT);	// From the first country up and to the left
					drawLine(g2d, x2, y2, x1 + WIDTH, y1 + HEIGHT);	// From the second country down and to the right
				} else {
					drawLine(g2d, x2, y2, x1 - WIDTH, y1 - HEIGHT);	// From the second country up and to the left
					drawLine(g2d, x1, y1, x2 + WIDTH, y2 + HEIGHT);	// From the first country down and to the right
				}
			} else {
				Risk.sayError("Unrecognized edge crossing policy for adjacency.");
			}
			
			g2d.setStroke(old_stroke);	// Since a different thickness was used for adjacency lines, reset it to its old value
		}
	}
	
	// Draws a line on the Graphics2D object, casting from float coordinates to ints
	private void drawLine(Graphics2D g2d, float x1, float y1, float x2, float y2) {
		g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
	}

	/*
	 * The x and y coordinates stored within each Country object is
	 * loaded from the maps file. To have it draw correctly, this method
	 * finds the appropriate bounds and normalizes all coordinates to be (0,1)
	 */
	private void convertCountryCoordinates() {
		country_positions = new Point2D.Float[game.NUM_COUNTRIES];
		// So that there is some buffer around the edge of the map, these float values
		// represent how much of the x and y axes are taken up by stuff
		float percent_xaxis_taken_up_by_countries = .90f;
		float percent_yaxis_taken_up_by_countries = .90f;

		// Find the highest and lowest x and y values:
		int lowest_x = Integer.MAX_VALUE, lowest_y = Integer.MAX_VALUE;
		int highest_x = Integer.MIN_VALUE, highest_y = Integer.MIN_VALUE;
		for(int i=0; i < game.NUM_COUNTRIES; i++) {
			Point country_pos = game.getCountry(i).getPosition();
			if(country_pos.x < lowest_x)
				lowest_x = country_pos.x;
			if(country_pos.x > highest_x)
				highest_x = country_pos.x;
			if(country_pos.y < lowest_y)
				lowest_y = country_pos.y;
			if(country_pos.y > highest_y)
				highest_y = country_pos.y;
		}
		// Since we want the buffer to be beyond all nodes, not just the center point of all nodes:
		highest_x += 2.0f*country_circle_radius;
		highest_y += 2.0f*country_circle_radius;

		// This loops calculates what the coordinate of each territory's node should be based on the
		// numbers read in from the map file. They are put into country_positions[] and will lie between 0 and 1
		for(int i=0;i<game.NUM_COUNTRIES;i++) {
			Point country_pos = game.getCountry(i).getPosition();
			float newx = ((1.00f - percent_xaxis_taken_up_by_countries)/2) * (WIDTH)
					+ (float)(country_pos.x - lowest_x)/(highest_x - lowest_x) * percent_xaxis_taken_up_by_countries * (WIDTH);
			newx -= 12;
			float newy = ((1.00f - percent_yaxis_taken_up_by_countries)/2) * (HEIGHT)
					+ (float)(country_pos.y - lowest_y)/(highest_y - lowest_y) * percent_yaxis_taken_up_by_countries * (HEIGHT);
			newy -= 8;
			country_positions[i] = new Point2D.Float(newx, newy);
		}
	}
}