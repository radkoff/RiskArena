package riskarena;
/*
 * The GameInfo class is used by bots to find out information regarding the state of
 * the board. This is anything that a person playing risk can see. Everything returned
 * by these getters is "safe" for an untrustworthy bot, meaning they are clones of arrays
 * 
 * Evan Radkoff
 */

public class GameInfo {
	private GameData game_data;		// The game engine to collect info from
	private Player player;		// The player that is using this GameInfo object

	public GameInfo(GameData g, Player p) {
		game_data = g;
		player = p;
	}

	// Returns an array of PlayerInfo objects that describe all opponents still in the game
	public PlayerInfo[] getPlayerInfo() {
		Player[] players = game_data.getPlayersNotEliminated();
		PlayerInfo[] player_infos = new PlayerInfo[players.length];
		for(int i=0;i<players.length;i++) {
			player_infos[i] = new PlayerInfo(players[i]);
		}
		return player_infos;
	}

	public int getNumCountries() {
		return game_data.NUM_COUNTRIES;
	}
	
	public int getNumContinents() {
		return game_data.NUM_CONTINENTS;
	}
	
	// A bot might use player ID's an indices of an array. Since getPlayerInfo
	// only gives the ones still in, its length can't be used to size such an array
	// because a player's ID might be larger than it. This returns the number of players
	// originally in the game.
	public int getMaxPlayerID() {
		return game_data.NUM_PLAYERS;
	}

	// Returns a copy of the game engine's Country array
	public CountryInfo[] getCountryInfo() {
		return game_data.getCountryInfo();
	}

	// Get an array of all continent army bonuses
	public int[] getContinentBonuses() {
		return game_data.getContinentBonuses();
	}

	// Get world info, which contains adjacencies
	public World getWorldInfo() {
		return game_data.getWorldCopy();
	}

	// Get this player's hand of cards
	public int[] getCardInfo() {
		return player.getCards();
	}

	// Get how many armies the next set turned in is worth
	public int getArmiesFromNextSet(){
		return game_data.getArmiesFromNextSet();
	}

	public int me() {
		return player.getId();
	}
	
	public String getMyName() {
		return player.getName();
	}
}