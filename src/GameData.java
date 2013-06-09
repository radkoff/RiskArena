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
	private int turn_player_id; // Index of PLAYER_NAMES whose turn it is
	protected World world; // The world
	private Country[] COUNTRIES; // Array of Country objects (private helper class storing name, player id, and number of armies)
	protected String[] CONTINENT_NAMES; // Array of the continent names
	protected int[] CONTINENT_BONUSES; // Array of the continent army bonuses
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
	
		
		/*
		 * Once the game object is constructed, it must be sent along to each Bot player
		 */
		public void sendGameDataToBots() {
			for(int i=0; i < NUM_PLAYERS; i++) {
				if(getPlayerType(i) == Player.BOT)
					((Bot)players[i]).initializeBot(this);
			}
		}
		
		public void sendHumanListenersToBoard(GameBoard board) {
			for(int i=0; i < NUM_PLAYERS; i++) {
				if(getPlayerType(i) == Player.HUMAN)
					((Human)players[i]).sendInputToGraphics(board);
			}
		}
		
		
		// If the player who's turn it is is Human, returns true
		public boolean currentPlayerHuman() {
			return getPlayerType(getCurrentPlayerID()) == Player.HUMAN;
		}
		
		public String getPlayerName() {
			return getPlayerName(getCurrentPlayerID());
		}
		
		public int getCurrentPlayerID() {
			return turn_player_id;
		}
		
		public void setCurrentPlayerID(int id) {
			if(id < 0 || id > NUM_PLAYERS - 1) {
				Risk.sayError("Error in GameData.setCurrentPlayerID(): invalid player id of "+id);
				return;
			}
			turn_player_id = id;
		}
		
		
		
	public ArrayList<Adjacency> getWorldAdjacencyList() {
		return world.getAdjacencyList();
	}
	
		
	public int getOccupier(int id) {
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.getOccurpier(): invalid country id of "+id);
			System.exit(-1);
		}
		return COUNTRIES[id].getPlayer();
	}
	
	public String getCountryName(int id) {
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.getCountryName(): invalid country id of "+id);
			return null;
		}
		return COUNTRIES[id].getName();
	}
	
	public int getArmies(int id) {
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.getArmies(): invalid country id of "+id);
			System.exit(-1);
		}
		return COUNTRIES[id].getArmies();
	}
	
	public void setArmies(int id, int amount) {
		if(amount < 0) {
			Risk.sayError("Error in GameData.setArmies(): negative army amount not allowed");
			System.exit(-1);
		}
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.setArmies(): invalid country id of "+id);
			System.exit(-1);
		}
		COUNTRIES[id].setArmies(amount);
	}
	
	
	public void setPlayerStillIn(int id, boolean b) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.setPlayerStillIn(): invalid player id of "+id);
			return;
		}
		players[id].setStillIn(b);
	}
	
	public boolean getPlayerStillIn(int id) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.getPlayerStillIn(): invalid player id of "+id);
			System.exit(-1);
		}
		return players[id].getStillIn();
	}
		
	public String getPlayerName(int id) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.getPlayerName(): invalid player id of "+id);
			return null;
		}
		return players[id].getName();
	}
	
	public int getPlayerType(int id) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.getPlayerName(): invalid player id of "+id);
			System.exit(-1);
		}
		return players[id].getType();
	}
		
	
	public void setBotPlayingSpeed(long bps) {
		if(bps < 0) {
			Risk.sayError("Error setting bot playing speed in GameData. Must be >= 0");
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
		
		
}
