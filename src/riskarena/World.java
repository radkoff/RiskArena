package riskarena;
/*
 *  World Class
 *  Manages information about country adjacency. Verifies requests about adjacency verification.
 *  
 *  Evan Radkoff and Spencer Hall
 */

import java.util.ArrayList;

public class World {

	private boolean[][] adjacency_truth; // Stores truth values about country adjacencies. Stored as [Country1][Country2]=true/false or [Country2][Country1] = true/false
	private ArrayList<Adjacency> adjacency_list;
	private final int NUM_COUNTRIES;
	private final int NUM_ADJACENCIES;

	public World(int countries, ArrayList<Adjacency> fileadjacency) {
		NUM_COUNTRIES = countries;
		adjacency_list = fileadjacency;
		
		adjacency_truth = new boolean[NUM_COUNTRIES][NUM_COUNTRIES];
		for(int i=0;i<adjacency_list.size();i++) {
			adjacency_truth[adjacency_list.get(i).fromCountryID()][adjacency_list.get(i).toCountryID()] = true;
			adjacency_truth[adjacency_list.get(i).toCountryID()][adjacency_list.get(i).fromCountryID()] = true;
		}

		NUM_ADJACENCIES = adjacency_list.size();
	}

	// Copy constructor
	public World(World w) {
		NUM_COUNTRIES = w.getNumCountries();
		NUM_ADJACENCIES = w.getNumAdjacencies();
		adjacency_truth = w.getRawAdjacencies();
	}

	public boolean isAdjacent(int countryid1, int countryid2)
	{
		// Returns whether a given two countries (given in the form of country ID numbers) are adjacent.
		return (adjacency_truth[countryid1][countryid2] || adjacency_truth[countryid2][countryid1]); // checks for adjacencies in both directions
	}

	public int[] getAdjacencies(int countryid)
	{
		// Returns an array containing the country ID numbers of countries who are adjacent to the input country's country ID.
		int adjchecker=0;
		for(int i=0;i<NUM_COUNTRIES;i++)
		{
			if (isAdjacent(countryid,i)) adjchecker++;
		}
		int[] adjlist = new int[adjchecker];
		adjchecker=0;
		for(int i=0;i<NUM_COUNTRIES;i++)
		{
			if (isAdjacent(countryid,i)) {
				adjlist[adjchecker] = i;
				adjchecker++;
			}
		}
		return adjlist;
	}

	/*
	 * Returns a double array that describes the adjacency information for ALL countries.
	 * Used by the Graphics class
	 * @return int[NUM_ADJACENCIES][2]. The two ints are the corresponding country id's.
	 */
	public ArrayList<Adjacency> getAdjacencyList() {
		if(adjacency_list == null) {
			Risk.sayError("Trying to get AdjacencyList before instantiation.");
		}
		return adjacency_list;
	}
	
	// For debugging purposes, prints the adjacency array with 0's and 1's
	private void printAdjacencies() {
		for(int i=0;i<adjacency_truth.length;i++) {
			for(int j=0;j<adjacency_truth[i].length;j++) {
				System.out.print(adjacency_truth[i][j] ? 1 : 0);
			}
			System.out.println();
		}
	}

	// Returns the adjacency array as-is
	public boolean[][] getRawAdjacencies() {
		return adjacency_truth.clone();
	}

	public int getNumCountries() {
		return NUM_COUNTRIES;
	}

	public int getNumAdjacencies() {
		return NUM_ADJACENCIES;
	}
}
