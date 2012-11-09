/*
 * PlayerInfo is a class that provides non-private player info
 * to Bots. Think of it as anything you can see about another player during a game:
 * their name, whether they're a bot or human, and their number of cards.
 * 
 * Evan Radkoff
 */

interface PlayerInterface {
	public String getName();
	public int getType();
	public int getNumCards();
}

public class PlayerInfo implements PlayerInterface {
	private final PlayerInterface player;
	public PlayerInfo(PlayerInterface wrapped) {
		player = wrapped;
	}
	public String getName() {
		return player.getName();
	}
	public int getType() {
		return player.getType();
	}
	public int getNumCards() {
		return player.getNumCards();
	}
}