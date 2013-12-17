package riskarena;
/*
 * GameData hold the actual game data structures of Countries, Players, Continents, and more,
 * providing adequate getters and setters. It also maintains general game data like
 * whose turn it is. The instance of GameData being used for the game can be passed to
 * any class that needs to interact with the game information.
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.util.ArrayList;

import riskarena.graphics.GameBoard;

public class GameData {
	/***** Numbers public to everyone *****/
	public final int NUM_PLAYERS; // number of players, set in constructor
	public final int NUM_COUNTRIES; // number of territories
	public final int NUM_CONTINENTS; // number of continents

	/***** Game data objects *****/
	private MapReader mapreader;	// MapReader object that allows the map file to be read easily
	private World world; // The world object that holds adjacency information

	/***** Important arrays of game data *****/
	private Player[] players; // Array of Player objects (name, cards, ID, etc)
	private Country[] countries; // Array of Country objects (private helper class storing name, player id, and number of armies)
	private String[] continent_names; // Array of the continent names
	private int[] continent_bonuses; // Array of the continent army bonuses
	private Color[] continent_colors;

	/***** Game numbers to keep track of *****/
	private int armies_from_next_set = 4; // number of armies the next player to turn in cards will get. 4->6->8->10->12->15->+5..
	private long bot_playing_speed = 120; // milliseconds bots wait before sending game decisions
	private int winner = 0;	// player id of the winner
	private int turn_player_id; // Index of PLAYER_NAMES whose turn it is
	private int turn_number = 0;	// Incremented each time all players make their turn
	private volatile boolean pause = false;

	// A GameData object is created by the game engine (Game.java) using an array of Players and
	// the string path to a valid map file.
	public GameData(Player p[], String map_file) {
		NUM_PLAYERS = p.length;

		// make map reader, read map info
		try {
			mapreader = new MapReader(map_file);
		} catch(Exception e) {
			Risk.sayError("Something is wrong with " + map_file + ": " + e.getMessage());
			System.exit(-1);
		}

		// Get country info from the map reader
		countries = mapreader.getCountries();
		NUM_COUNTRIES = countries.length;

		// Construct the World object that keeps track of adjacencies (getting them from the map reader)
		world = new World(NUM_COUNTRIES, mapreader.getAdjacencyInfo());

		// get continent info from mapreader
		continent_names = mapreader.getContinentNames();
		continent_bonuses = mapreader.getContinentBonuses();
		continent_colors = mapreader.getContinentColors();
		NUM_CONTINENTS = continent_names.length;

		players = p;
	}
	
	/********************* General game play helper methods **********************/

	// advances armies_from_next_set according to how much the army amount should go up
	// Follows 4->6->8->10->12->15->20->25 etc..
	public void advanceCardArmies() {
		if(armies_from_next_set < 12) armies_from_next_set += 2;
		else if(armies_from_next_set == 12) armies_from_next_set += 3;
		else armies_from_next_set += 5;
	}

	// Resets the players (clears cards, makes them all "still in")
	public void resetPlayers() {
		for(int i=0; i < players.length; i++)
			players[i].reset();
	}
	
	/*
	 * When a Bot player's turn has begun, call their initTurn() method to allow optional initialization
	 */
	public void notifyPlayerOfTurn() {
		if(!currentPlayerHuman())
			((Bot)getCurrentPlayer()).initTurn();
	}
	
	public void notifyPlayerOfTurnEnd() {
		if(!currentPlayerHuman())
			((Bot)getCurrentPlayer()).endTurn();
	}
	
	// Once the GameData object is constructed, it must be sent along to each Bot player
	public void sendGameDataToBots() {
		for(int i=0; i < NUM_PLAYERS; i++) {
			if(getPlayer(i).getType() == Player.BOT)
				((Bot)players[i]).initializeBot(this);
		}
	}

	// Once the GameData object is constructed, each Human player must send its input listener to the graphics
	public void sendHumanListenersToBoard(GameBoard board) {
		for(int i=0; i < NUM_PLAYERS; i++) {
			if(getPlayer(i).getType() == Player.HUMAN)
				((Human)players[i]).sendInputToGraphics(board);
		}
	}

	// If the player who's turn it is is Human, returns true
	public boolean currentPlayerHuman() {
		return getCurrentPlayer().getType() == Player.HUMAN;
	}
	
	
	/********************* Getters **********************/
	
	// Gets an array of all players currently still in the game
	public Player[] getPlayersNotEliminated() {
		ArrayList<Player> still_in_players = new ArrayList<Player>();
		for(int i=0;i<players.length;i++)
			if(players[i].getStillIn())
				still_in_players.add(players[i]);
		Player[] player_arr = new Player[still_in_players.size()];
		// We need an array, not an ArrayList
		for(int i=0; i < still_in_players.size(); i++) player_arr[i] = still_in_players.get(i);
		return player_arr;
	}

	// Retrieves the color of a given player id. Otherwise, returns a gray
	public Color getPlayerColor(int player_id) {
		if(player_id >= 0 && player_id < NUM_PLAYERS)
			return players[player_id].getColor();
		else return Color.gray;
	}

	// Retrieves a copy of the World object (holds country adjacencies)
	public World getWorldCopy() {
		return new World(world);
	}

	// Gets the Player with the given id
	public Player getPlayer(int id) {
		if(id < 0 || id > NUM_PLAYERS-1) {
			Risk.sayError("Error in GameData.getPlayer(id): invalid player id "+id);
			Thread.dumpStack();
			System.exit(-1);
		}
		return players[id];
	}

	// Gets the Country with the given id
	public Country getCountry(int id) {
		if(id < 0 || id > NUM_COUNTRIES-1) {
			Risk.sayError("Error in GameData.getCountry(id): invalid country id "+id);
			Thread.dumpStack();
			System.exit(-1);
		}
		return countries[id];
	}

	// Gets an array of CountryInfo objects. A CountryInfo object has only getters, so it
	// is safe to give to "untrusted" code (bots)
	public CountryInfo[] getCountryInfo() {
		CountryInfo[] countries2 = new CountryInfo[NUM_COUNTRIES];
		for(int i=0; i < NUM_COUNTRIES; i++)
			countries2[i] = new CountryInfo((CountryInterface) countries[i]);
		return countries2;
	}

	public int[] getContinentBonuses() {
		return continent_bonuses.clone();
	}

	public String[] getContinentNames() {
		return continent_names.clone();
	}

	// The name of the player who's turn it is
	public String getPlayerName() {
		return getCurrentPlayer().getName();
	}

	// Returns the player ID of the player who's turn it is
	public int getCurrentPlayerID() {
		return turn_player_id;
	}

	// Returns the player who's turn it is
	public Player getCurrentPlayer() {
		return players[getCurrentPlayerID()];
	}

	// Sets who's turn it is
	public void setCurrentPlayerID(int id) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.setCurrentPlayerID(): invalid player id of "+id);
			Thread.dumpStack();
			return;
		}
		turn_player_id = id;
	}

	// Calculates and returns the total number of armies a player has on the board
	public int getPlayerArmies(int id) {
		if(id < 0 || id > NUM_PLAYERS - 1) {
			Risk.sayError("Error in GameData.getPlayerArmies(id): invalid player id of "+id);
			Thread.dumpStack();
			System.exit(-1);
		}
		int total = 0;
		for(Country c : countries) {
			if (c.getPlayer() == id)
				total += c.getArmies();
		}
		return total;
	}

	// Returns how many armies one gets as a bonus for completely owning the continent with ID 'id'
	public int getContinentBonus(int id) {
		if(id < 0 || id > NUM_CONTINENTS - 1) {
			Risk.sayError("Error in GameData.getContinentBonus(id): invalid continent id of "+id);
			Thread.dumpStack();
		}
		return continent_bonuses[id];
	}

	// Returns the Color of the continent with ID 'id'
	public Color getContinentColor(int id) {
		if(id < 0 || id > NUM_CONTINENTS - 1) {
			Risk.sayError("Error in GameData.getContinentColor(id): invalid continent id of "+id);
			Thread.dumpStack();
		}
		return continent_colors[id];
	}

	// Returns the Name of the continent with ID 'id'
	public String getContinentName(int id) {
		if(id < 0 || id > NUM_CONTINENTS - 1) {
			Risk.sayError("Error in GameData.getContinentName(id): invalid continent id of "+id);
			Thread.dumpStack();
		}
		return continent_names[id];
	}

	// Returns the ArrayList of adjacencies from the World object
	public ArrayList<Adjacency> getWorldAdjacencyList() {
		return world.getAdjacencyList();
	}

	// Returns the adjacencies for a particular Country with ID 'id' using the World object
	public int[] getAdjacencies(int id) {
		if(id < 0 || id > NUM_COUNTRIES - 1) {
			Risk.sayError("Error in GameData.getAdjacencies(id): invalid country id of "+id);
			Thread.dumpStack();
			System.exit(-1);
		}
		return world.getAdjacencies(id);
	}

	// The bot playing speed is the number of milliseconds a bot pauses before making a decision
	public void setBotPlayingSpeed(long bps) {
		if(bps < 0) {
			Risk.sayError("Error setting bot playing speed in GameData. Must be >= 0");
			return;
		}
		bot_playing_speed = bps;
	}
	
	public void pause() {
		pause = !pause;
	}
	
	public boolean getPause() {
		return pause;
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
	
	public void incrementTurn() {
		turn_number++;
	}
	
	public int getTurnNumber() {
		return turn_number;
	}

	// Returns true if game is over (still_in only has one true value)
	// If this is the case it also sets "winner" to the player id who won
	public boolean over() {
		int possible_winner = -1;
		for(int i=0; i < NUM_PLAYERS; i++) {
			if(getPlayer(i).getStillIn()) {
				if(possible_winner == -1) possible_winner = i;
				else return false;
			}
		}
		winner = possible_winner;
		if(players[winner].getType() == Player.BOT)
			((Bot)players[winner]).endGame(1);		// First place!
		return true;
	}
}
