/*
 * The GameInfo class is used by bots to find out information regarding the state of
 * the board. This is anything that a person playing risk can see. Everything returned
 * by these getters is "safe" for an untrustworthy bot, meaning they are clones of arrays
 * 
 * Evan Radkoff
 */

public class GameInfo {
	private GameData risk_game;		// The game engine to collect info from
	private Player player;		// The player that is using this GameInfo object

	public GameInfo(GameData g, Player p) {
		risk_game = g;
		player = p;
	}

	// Returns an array of PlayerInfo objects that describe all opponents still in the game
	public PlayerInfo[] getPlayerInfo() {
		Player[] players = risk_game.getPlayersNotEliminated();
		PlayerInfo[] player_infos = new PlayerInfo[players.length];
		for(int i=0;i<players.length;i++) {
			player_infos[i] = new PlayerInfo(players[i]);
		}
		return player_infos;
	}

	public int getNumCountries() {
		return risk_game.NUM_COUNTRIES;
	}

	// Returns a copy of the game engine's Country array
	public CountryInfo[] getCountryInfo() {
		Country[] old = risk_game.getCountries();
		CountryInfo[] countries = new CountryInfo[risk_game.NUM_COUNTRIES];
		for(int i=0;i<risk_game.NUM_COUNTRIES;i++)
			countries[i] = new CountryInfo(old[i]);
		return countries;
	}

	// Get an array of all continent army bonuses
	public int[] getContinentBonuses() {
		return (int[])risk_game.getContinentInfo().clone();
	}

	// Get world info, which contains adjacencies
	public World getWorldInfo() {
		return new World(risk_game.world);
	}

	// Get this player's hand of cards
	public int[] getCardInfo() {
		return player.getCards();
	}

	// Get how many armies the next set turned in is worth
	public int getArmiesFromNextSet(){
		return risk_game.getArmiesFromNextSet();
	}

	public int me() {
		return player.getId();
	}
}