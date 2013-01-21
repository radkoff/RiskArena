/*
 * An instance of the Adjacency class represents an adjacency between two territories.
 * 
 * Evan Radkoff
 */

public class Adjacency {
	private String from, to;	// The names of the two countries
	private int fromID, toID;	// The country ID's of the two countries
	private int edge_cross;		// Some ajacnecies need to go accross edges rather than through the middle
								// For example, the adjacency bewteen Alaska and Kamchatcka in Earth.map crosses horizontally
								// There are 5 edge crossing policies, and edge_cross takes the value of one of the following:
	
	public final static int CROSS_NONE = 0;			// Does not cross
	public final static int CROSS_HORIZONTAL = 1;	// crosses horizontally
	public final static int CROSS_VERTICAL = 2;		// crosses the map edges vertically
	public final static int CROSS_DIAG_RIGHT = 3;	// crosses by going diagonally up and to the right (and down and to the left)
	public final static int CROSS_DIAG_LEFT = 4;	// crosses by going diagonally up and to the left (and down and to the right)
	
	// Adjacencies are created with the String names of the two territories, and an edge crossing policy
	public Adjacency(String f, String t, int edge)
	{
		from = f;
		to = t;
		edge_cross = edge;
	}
	
	// Getters
	
	public int getCrossPolicy() {
		return edge_cross;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public int fromCountryID() {
		return fromID;
	}
	
	public int toCountryID() {
		return toID;
	}
	
	// Setters
	
	public void setFromID(int id) {
		fromID = id;
	}
	
	public void setToID(int id) {
		toID = id;
	}
}
