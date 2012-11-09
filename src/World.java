/*
 *  World Class
 *  Manages information about country adjacency. Verifies requests about adjacency verification.
 *  
 *  Evan Radkoff and Spencer Hall
 */

public class World {
	
	private boolean[][] adjacencytruth; // Stores truth values about country adjacencies. Stored as [Country1][Country2]=true/false or [Country2][Country1] = true/false
	private final int NUM_COUNTRIES;
	private final int NUM_ADJACENCIES;
	
	public World(int countries, boolean[][] fileadjacency) {
		NUM_COUNTRIES = countries;
		adjacencytruth=fileadjacency.clone();
		// calculate NUM_ADJACENCIES
		int num_adjs = 0;
		for(int i=0;i<NUM_COUNTRIES-1;i++) {
			for(int j=i+1;j<NUM_COUNTRIES;j++) {
				if(adjacencytruth[i][j]) {
					num_adjs++;
				}
			}
		}
		NUM_ADJACENCIES = num_adjs;
	}
	
	// Copy constructor
	public World(World w) {
		NUM_COUNTRIES = w.getNumCountries();
		NUM_ADJACENCIES = w.getNumAdjacencies();
		adjacencytruth = w.getRawAdjacencies();
	}
	
	public boolean isAdjacent(int countryid1, int countryid2)
	{
		// Returns whether a given two countries (given in the form of country ID numbers) are adjacent.
		return (adjacencytruth[countryid1][countryid2] || adjacencytruth[countryid2][countryid1]); // checks for adjacencies in both directions
	}
	
	public int[] getAdjacencies(int countryid)
	{
		// Returns an array containing the country ID numbers of countries who are adjacent to the input country's country ID.
		// The value of adjlist[0] is the total number n of adjacencies and the next n values are the respective country IDs of the n adjacent countries
		int adjchecker=0;
		for(int i=0;i<adjacencytruth.length;i++)
		{
			if (isAdjacent(countryid,i)) adjchecker++;
		}
		int[] adjlist = new int[adjchecker+1];
		adjlist[0]=adjchecker;
		adjchecker=0;
		for(int i=0;i<adjacencytruth.length;i++)
		{
			if (isAdjacent(countryid,i)) {
				adjchecker++;
				adjlist[adjchecker]=i;
			}
		}
		return adjlist;
	}
	
	/*
	 * Returns a double array that describes the adjacency information for ALL countries.
	 * Used by the Graphics class
	 * @return int[NUM_ADJACENCIES][2]. The two ints are the corresponding country id's.
	 */
	public int[][] getAdjacencyList() {
		int[][] adjs = new int[NUM_ADJACENCIES][2];
		int adjs_count = 0;
		for(int i=0;i<NUM_COUNTRIES-1;i++) {
			for(int j=i+1;j<NUM_COUNTRIES;j++) {
				if(adjacencytruth[i][j]) {
					adjs[adjs_count][0] = i;
					adjs[adjs_count][1] = j;
					adjs_count++;
				}
			}
		}
		return adjs;
	}
	
	// Returns the adjacency array as-is
	public boolean[][] getRawAdjacencies() {
		return adjacencytruth.clone();
	}
	
	public int getNumCountries() {
		return NUM_COUNTRIES;
	}
	
	public int getNumAdjacencies() {
		return NUM_ADJACENCIES;
	}
}
