package riskarena;
/*
 * This class is the interface that all RiskBots must follow.
 * It defines the signatures of methods that a particular RiskBot uses
 * to determine its playing strategy.
 * 
 * Evan Radkoff
 */

public interface RiskBot {

	// Initialize the bot, sending an instance of GameInfo so that it can see board info,
	// as well as a RiskListener so it can communicate answers. See HOWTO for more on these objects.
	public void init(GameInfo gi, Bot.RiskListener rl);

	/*
	 * A method called at the start of each of the RiskBot's turns that can be used for initialization purposes
	 */
	public void initTurn();

	/*
	 * At the beginning of the game during the territory claiming phase, the game engine
	 * will call this when it needs you to give the "number" of an unclaimed territory.
	 * Testing if a country has been claimed can be achieved through the isTaken()
	 * method ie countries[i].isTaken() will return true if the ith country has been
	 * claimed. By "number" we mean some index of the Countries array.
	 * Basically, some time in this method you will call to_game.sendInt(herp)
	 * where herp is an int from 0 to the number of countries - 1.
	 */
	public void claimTerritory();


	/*
	 * Throughout the game there will be times when you need to place a certain number
	 * of armies on the board, in which case this will be called. num_to_place is the
	 * number of armies you need to place, but it need not be all in one territory.
	 * If you want to spread them out, simply place some amount under num_to_place on a territory.
	 * The game engine will then re-call fortifyTerritory with what's left, and continue to do so
	 * until the number left to place gets to zero.
	 * It must call sendInt twice, first with the country id (index) and then with the number of armies.
	 */
	public void fortifyTerritory(int num_to_place);


	/*
	 * To launch an attack, call sendInt three times: with the "from" country, the "to" country,
	 * and the number of armies (from 1 to 3). Make sure the "from" country has more than one army
	 * on it, and that "to" is a connecting enemy territory.
	 */
	public void launchAttack();


	/*
	 * Following a victory (attack resulting in you taking over the territory) you have to
	 * move in some armies to occupy what you conquered.
	 * attacker - the country you just attacked from
	 * defender - the country you are about to occupy
	 * min - minimum number of soldiers to move in (how ever many you attacked with)
	 * max - max number of soldiers to move in (attacker's army amount - 1)
	 * Call sendInt() with some number from min to max
	 */
	public void fortifyAfterVictory(int attacker, int defender, int min, int max);


	/*
	 * Sometimes in the game a player has a choice of whether or not to turn
	 * in a set of cards for armies, and sometimes not. When the former is the case,
	 * this method is called for a yes/no answer. Send 1 if you would like to turn
	 * in a set, or 0 (or anything else) if not.
	 */
	public void chooseToTurnInSet();


	/*
	 * When a set of cards is being turned in, sometimes there are different combinations
	 * of one's hand that can be chosen. For example with {wildcard, infantry, infantry, artillery}
	 * you could turn in three infantries or {infantry, cavalry, artillery}. The size of possible_sets
	 * is the number of possible sets (will be at least 1). Each possible_sets[i] is a size-3 array
	 * of ints 0-4 (signifying infantry, cav., artil., wildcard). Send the index of possible_sets you choose to turn in.
	 */
	public void chooseCardSet(int[][] possible_sets);

	/*
	 * Step 3 of a person's turn is the optional fortification of some group of armies from one
	 * territory to one adjacent territory. If you want to skip fortification, send a -1 (or some negative number).
	 * If you want to fortify something, send three ints: the "from" country, the "to" country,
	 * and the number of armies (min is 1, max is "from".getArmies()-1)
	 */
	public void fortifyPosition();
}
