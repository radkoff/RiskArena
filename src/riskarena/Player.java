package riskarena;
/*
 * The abstract Player class represents a player of Risk, either human or bot.
 * The various askInt methods and askLine are implements in the Human and Bot classes.
 * Keeps track of the player's name, color, type, id, whether they're still in the game,
 * and their current hand of cards.
 * It implements the simple PlayerInterface defined in PlayerInfo.java
 * 
 * Evan Radkoff
 */

import java.awt.Color;

abstract public class Player implements PlayerInterface {
	public static final int HUMAN = 0;
	public static final int BOT = 1;

	private String name;
	private Color color;
	private int type;
	private int player_id;
	private boolean still_in;
	private int[] cards;

	public Player(int t, String n, Color c, int id) {
		name = n;
		color = c;
		type = t;
		player_id = id;
		still_in = true;
		cards = new int[4];
		for(int i=0;i<cards.length;i++)	// initialize card amounts to 0 of each
			cards[i] = 0;
	}

	// Information getters
	abstract public int askInt() throws Bot.RiskBotException;
	abstract public int askInt(int MIN) throws Bot.RiskBotException;
	abstract public int askInt(int MIN, int MAX) throws Bot.RiskBotException;
	abstract public String askLine();

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public int getType() {
		return type;
	}

	public boolean getStillIn() {
		return still_in;
	}

	public int getId() {
		return player_id;
	}

	public int[] getCards() {
		int[] cards_copy = new int[cards.length];
		for(int i=0;i<cards.length;i++)
			cards_copy[i] = cards[i];
		return cards_copy;
	}

	public int getNumCards() {
		return cards[0] + cards[1] + cards[2] + cards[3];
	}

	public int getNumCardType(int card_type) {
		return cards[card_type];
	}

	public void setName(String new_name) {
		name = new_name;
	}

	public void setStillIn(boolean true_if_alive) {
		still_in = true_if_alive;
	}

	/*
	 * These methods alter card information for the player.
	 */
	public void incrementCardType(int card_type) {
		increaseCardType(card_type, 1);
	}
	public void decrementCardType(int card_type) {
		decreaseCardType(card_type, 1);
	}
	public void increaseCardType(int card_type, int num) {
		cards[card_type] += num;
	}
	public void decreaseCardType(int card_type, int num) {
		cards[card_type] -= num;
	}
	public void setCardType(int card_type, int new_value) {
		cards[card_type] = new_value;
	}
	public void clearCards() {
		cards[0] = 0;
		cards[1] = 0;
		cards[2] = 0;
		cards[3] = 0;
	}

	// No, the player is not a goat.
	public boolean isPlayerAGoat() {
		return false;
	}
	
	public void reset() {
		for(int i=0;i<cards.length;i++)
			cards[i] = 0;
		still_in = true;
	}
}
