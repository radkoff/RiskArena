import java.awt.Color;
import java.util.ArrayList;

public class GameData {
	public final int NUM_PLAYERS; // number of players, set in constructor
	public final int NUM_COUNTRIES; // number of territories
	public final int NUM_CONTINENTS;
	protected MapReader mapreader;
	private int armies_from_next_set = 4; // number of armies the next player to turn in cards will get. 4->6->8->10->12->15->+5..
	private long bot_playing_speed = 120; // milliseconds bots wait before sending game decisions
	private Player players[];
	private int winner = 0;	// player id of the winner
	private int turn_player_id; // Index of PLAYER_NAMES whose turn it is
	protected World world; // The world
	private Country[] COUNTRIES; // Array of Country objects (private helper class storing name, player id, and number of armies)
	private String[] CONTINENT_NAMES; // Array of the continent names
	private int[] CONTINENT_BONUSES; // Array of the continent army bonuses
	private Color[] CONTINENT_COLORS;
	
	public GameData(Player p[], String map_file) {
		NUM_PLAYERS = p.length;
		
		// make map reader, read map info
				try {
					mapreader = new MapReader(map_file);
				} catch(Exception e) {
					Risk.sayError("Something is wrong with " + map_file + ": " + e.getMessage());
					exit();
				}

				// Get country info from the map reader
				COUNTRIES = mapreader.getCountries();
				NUM_COUNTRIES = COUNTRIES.length;

				// give adjacency info to world (get from map reader, supply to world constructor)
				ArrayList<Adjacency> adjacencies = mapreader.getAdjacencyInfo();
				world = new World(NUM_COUNTRIES, adjacencies);

				// get continent info from mapreader
				CONTINENT_NAMES = mapreader.getContinentNames();
				CONTINENT_BONUSES = mapreader.getContinentBonuses();
				CONTINENT_COLORS = mapreader.getContinentColors();
				NUM_CONTINENTS = CONTINENT_NAMES.length;

				players = p;
				
	}
	
	// advances armies_from_next_set according to how much the army amount should go up
	// Follows 4->6->8->10->12->15->20->25 etc..
	public void advanceCardArmies() {
		if(armies_from_next_set < 12) armies_from_next_set += 2;
		else if(armies_from_next_set == 12) armies_from_next_set += 3;
		else armies_from_next_set += 5;
	}
	
	// Gets an array of all players currently still in the game
		public Player[] getPlayersNotEliminated() {
			ArrayList<Player> still_in_players = new ArrayList<Player>();
			for(int i=0;i<players.length;i++)
				if(players[i].getStillIn())
					still_in_players.add(players[i]);
			Player[] player_arr = new Player[still_in_players.size()];
			for(int i=0;i<still_in_players.size();i++) player_arr[i] = still_in_players.get(i);
			return player_arr;
		}
		
		// Retrieves the color of a given player id. Otherwise, returns a gray
		public Color getPlayerColor(int player_id) {
			if(player_id >= 0 && player_id < NUM_PLAYERS)
				return players[player_id].getColor();
			else return Color.gray;
		}
	
		
		// Resets the players (clears cards, makes them all "still in")
		public void resetPlayers() {
			for(int i=0;i<players.length;i++)
				players[i].reset();
		}
		
		/*
		 * Once the game object is constructed, it must be sent along to each Bot player
		 */
		public void sendGameDataToBots() {
			for(int i=0; i < NUM_PLAYERS; i++) {
				if(getPlayer(i).getType() == Player.BOT)
					((Bot)players[i]).initializeBot(this);
			}
		}
		
		public void sendHumanListenersToBoard(GameBoard board) {
			for(int i=0; i < NUM_PLAYERS; i++) {
				if(getPlayer(i).getType() == Player.HUMAN)
					((Human)players[i]).sendInputToGraphics(board);
			}
		}
		
		
		public Player getPlayer(int id) {
			if(id < 0 || id > NUM_PLAYERS-1) {
				Risk.sayError("Error in GameData.getPlayer(id): invalid player id "+id);
				Thread.dumpStack();
				System.exit(-1);
			}
			return players[id];
		}
		
		public Country getCountry(int id) {
			if(id < 0 || id > NUM_COUNTRIES-1) {
				Risk.sayError("Error in GameData.getCountry(id): invalid country id "+id);
				Thread.dumpStack();
				System.exit(-1);
			}
			return COUNTRIES[id];
		}
		
		public CountryInfo[] getCountryInfo() {
			CountryInfo[] countries = new CountryInfo[NUM_COUNTRIES];
			for(int i=0; i < NUM_COUNTRIES; i++)
				countries[i] = new CountryInfo(COUNTRIES[i]);
			return countries;
		}
		
		public int[] getContinentBonuses() {
			return (int[])CONTINENT_BONUSES.clone();
		}
		
		public String[] getContinentNames() {
			return (String[])CONTINENT_NAMES.clone();
		}
		
		
		// If the player who's turn it is is Human, returns true
		public boolean currentPlayerHuman() {
			return getCurrentPlayer().getType() == Player.HUMAN;
		}
		
		public String getPlayerName() {
			return getCurrentPlayer().getName();
		}
		
		public int getCurrentPlayerID() {
			return turn_player_id;
		}
		
		public Player getCurrentPlayer() {
			return players[getCurrentPlayerID()];
		}
		
		public void setCurrentPlayerID(int id) {
			if(id < 0 || id > NUM_PLAYERS - 1) {
				Risk.sayError("Error in GameData.setCurrentPlayerID(): invalid player id of "+id);
				Thread.dumpStack();
				return;
			}
			turn_player_id = id;
		}
		
		public int getPlayerArmies(int id) {
			if(id < 0 || id > NUM_PLAYERS - 1) {
				Risk.sayError("Error in GameData.getPlayerArmies(id): invalid player id of "+id);
				Thread.dumpStack();
				System.exit(-1);
			}
			int total = 0;
			for(Country c : COUNTRIES) {
				if (c.getPlayer() == id)
					total += c.getArmies();
			}
			return total;
		}
		
		
		public int getContinentBonus(int id) {
			if(id < 0 || id > NUM_CONTINENTS - 1) {
				Risk.sayError("Error in GameData.getContinentBonus(id): invalid continent id of "+id);
				Thread.dumpStack();
			}
			return CONTINENT_BONUSES[id];
		}
		
		public Color getContinentColor(int id) {
			if(id < 0 || id > NUM_CONTINENTS - 1) {
				Risk.sayError("Error in GameData.getContinentColor(id): invalid continent id of "+id);
				Thread.dumpStack();
			}
			return CONTINENT_COLORS[id];
		}
		
		public String getContinentName(int id) {
			if(id < 0 || id > NUM_CONTINENTS - 1) {
				Risk.sayError("Error in GameData.getContinentName(id): invalid continent id of "+id);
				Thread.dumpStack();
			}
			return CONTINENT_NAMES[id];
		}
		
		
		
	public ArrayList<Adjacency> getWorldAdjacencyList() {
		return world.getAdjacencyList();
	}
	
	public int[] getAdjacencies(int id) {
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.getAdjacencies(id): invalid country id of "+id);
			Thread.dumpStack();
			System.exit(-1);
		}
		return world.getAdjacencies(id);
	}
	
	
	
	
	public void setBotPlayingSpeed(long bps) {
		if(bps < 0) {
			Risk.sayError("Error setting bot playing speed in GameData. Must be >= 0");
			return;
		}
		bot_playing_speed = bps;
	}
	
	public long getBotPlayingSpeed() {
		return bot_playing_speed;
	}
	
	// Returns how many armies the next person to turn in a set of cards will get.
		public int getArmiesFromNextSet() {
			return armies_from_next_set;
		}
		
		// Returns the winner of the game
		public int getWinner() {
			return winner;
		}
		
		/*
		 * Returns true if game is over (still_in only has one true value)
		 * If this is the case it also sets "winner" to the player id who won
		 */
		public boolean over() {
			int possible_winner = -1;
			for(int i=0; i < NUM_PLAYERS; i++) {
				if(getPlayer(i).getStillIn()) {
					if(possible_winner == -1) possible_winner = i;
					else return false;
				}
			}
			winner = possible_winner;
			return true;
		}
}
