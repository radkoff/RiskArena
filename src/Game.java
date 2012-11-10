/*
 * The Game class is the game engine for RiskArena. It maintains information
 * about the game, and provides the procedure involved in running a player's turn
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Game {
	public final int NUM_PLAYERS; // number of players, set in constructor
	public final int NUM_COUNTRIES; // number of territories

	private Player players[];
	private Country[] COUNTRIES; // Array of Country objects (private helper class storing name, player id, and number of armies)
	protected String[] CONTINENT_NAMES; // Array of the continent names
	protected int[] CONTINENT_BONUSES; // Array of the continent army bonuses
	private Color[] CONTINENT_COLORS;

	private Deck deck; // Deck of cards
	protected MapReader mapreader;
	private int armies_from_next_set = 4; // number of armies the next player to turn in cards will get. 4->6->8->10->12->15->+5..
	protected World world; // The world
	private int turn_player_id; // Index of PLAYER_NAMES whose turn it is
	private int player_id_that_goes_first; // The index of PLAYER_NAMES that goes first. Randomly set in game initialization
	private int winner = 0;	// player id of the winner
	private Random rand;

	/* This is the primary Game constructor. It sets up the data
	 * structure in Game.java that hold game information.
	 * @param ArrayList<String> player_names, the names of players of the game
	 * @param String map_file file path to the map file, sent to a MapReader object
	 */
	public Game(Player p[], String map_file) {
		NUM_PLAYERS = p.length;
		// make map reader, read map info
		try {
			mapreader = new MapReader(map_file);
		} catch(Exception e) {
			Risk.sayError("Something is wrong with " + map_file + ": " + e.getMessage());
			exit();
		}

		Date dat = new Date();
		rand = new Random(dat.getTime());

		// Get country info from the map reader
		COUNTRIES = mapreader.getCountries();
		NUM_COUNTRIES = COUNTRIES.length;

		// give adjacency info to world (get from map reader, supply to world constructor)
		boolean[][] adj = mapreader.getAdjacencyInfo();
		world = new World(NUM_COUNTRIES, adj);

		// get continent info from mapreader
		CONTINENT_NAMES = mapreader.getContinentNames();
		CONTINENT_BONUSES = mapreader.getContinentBonuses();
		CONTINENT_COLORS = mapreader.getContinentColors();

		players = p;

		// initialize deck
		deck = new Deck(rand);

		setPlayerThatGoesFirst();
	}

	/*
	 * Chooses a random player to go first, sets the global int player_id_that_goes_first.
	 */
	private void setPlayerThatGoesFirst() {
		Date dat = new Date();
		int some_player = rand.nextInt(NUM_PLAYERS);
		player_id_that_goes_first = some_player;
		turn_player_id = player_id_that_goes_first;
		if(currentPlayerHuman())
			Risk.sayOutput(getPlayerName() + ", you have been chosen to go first.");
		else
			Risk.sayOutput(getPlayerName() + " has been chosen to go first.");
	}

	/*
	 * At the beginning of the game, this method has the players claim territories and place their initial armies
	 */
	public void placeInitialArmies() {
		int armiesToPlace = 20;
		switch(NUM_PLAYERS) {
		case 2: armiesToPlace = 45;
		break;
		case 3: armiesToPlace = 35;
		break;
		case 4: armiesToPlace = 30;
		break;
		case 5: armiesToPlace = 25;
		break;
		case 6: armiesToPlace = 20;
		break;
		default: Risk.sayError("Army placement is only configured for 2-6 players. Set armiesToPlace in Game.placeInitialArmies() for " + NUM_PLAYERS + " players.");
		break;
		}
		Risk.sayOutput("Each player has " + armiesToPlace + " armies to place.");

		/* Initial army placement */
		int pile[] = new int[NUM_PLAYERS]; // array representing each player's pile of initial armies to place
		for(int i=0;i<NUM_PLAYERS;i++) pile[i] = armiesToPlace;
		// claiming:
		try {
			for(int i=0;i<COUNTRIES.length;i++) {
				int claimed;
				if(currentPlayerHuman()) {
					Risk.sayOutput(getPlayerName() + ": Enter the number of the territory you would like to claim.", OutputFormat.QUESTION);
					claimed = players[turn_player_id].askInt(1,NUM_COUNTRIES);
				} else {
					((Bot)players[turn_player_id]).claimTerritory();	// Signal to the bot that they need to make a decision
					claimed = players[turn_player_id].askInt(0,NUM_COUNTRIES-1);
				}
				if(!currentPlayerHuman()) claimed++;
				while(!claimCountry(claimed)) {		// If claiming a country fails, it's because it's already occupied
					if(!currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to claim a territory that was already claimed.");

					Risk.sayError("Territory already taken. Choose another.");
					claimed = players[turn_player_id].askInt(1,NUM_COUNTRIES);
				}
				if(!currentPlayerHuman())
					Risk.sayOutput(getPlayerName() + " has claimed " + COUNTRIES[claimed-1].getName() + ".");
				Risk.refreshGraphics();
				pile[turn_player_id]--;
				advanceTurn();
			}
			// fortifying:
			while(true) {
				int to_fortify;
				if(currentPlayerHuman()) {
					Risk.sayOutput(getPlayerName() + ": Enter the number of the territory you would like to fortify.", OutputFormat.QUESTION);
					to_fortify = players[turn_player_id].askInt(1, NUM_COUNTRIES);
				} else {
					((Bot)players[turn_player_id]).fortifyTerritory(pile[turn_player_id]);
					to_fortify = players[turn_player_id].askInt(0, NUM_COUNTRIES-1);
				}
				if(currentPlayerHuman())
					to_fortify--;
				while(COUNTRIES[to_fortify].getPlayer() != turn_player_id) {
					if(!currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to fortify a territory that wasn't his.");
					Risk.sayError("Not your territory to fortify. Enter another.");
					to_fortify = players[turn_player_id].askInt(1, NUM_COUNTRIES);
					to_fortify--;
				}
				if(currentPlayerHuman())
					Risk.sayOutput("How many armies would you like to add to " + COUNTRIES[to_fortify].getName() + "? " + pile[turn_player_id] + " armies left in your pile.", OutputFormat.QUESTION);
				int armies_added = players[turn_player_id].askInt(1, pile[turn_player_id]);

				fortifyCountry(to_fortify, armies_added);
				pile[turn_player_id] -= armies_added;
				Risk.refreshGraphics();

				boolean done_fortifying = false;
				for(int i=0;;i++) {
					if(i>NUM_PLAYERS) {
						done_fortifying = true;
						break;
					}
					advanceTurn();
					if(pile[turn_player_id] > 0) break; 
				}
				if(done_fortifying) break;
			}
			Risk.refreshGraphics();
			turn_player_id = player_id_that_goes_first; // after initial fortifying, the player to go first
			// should be the same player that claimed territory first.
		} catch(Bot.RiskBotException e) {
			BadRobot(turn_player_id, "During the initial army placement/fortification phase:", e);
		}
	}

	/*
	 * Step 1 of a player turn. Fortifies territories for the player.
	 * Called from Risk.java
	 */
	public void fortifyArmies() {
		int armies_to_place = 0;
		armies_to_place += armiesFromCards();
		armies_to_place += armiesFromContinents();
		armies_to_place += armiesFromTerritories();
		if(currentPlayerHuman())
			Risk.sayOutput("You have " + armies_to_place + " armies to fortify with.");
		placeArmies(armies_to_place);
	}

	// place "armies_to_place" armies on a territory of turn_player_id's choice
	private void placeArmies(int armies_to_place) {
		try {
			while(armies_to_place > 0) {
				int country_improving;
				if(currentPlayerHuman()) {
					Risk.sayOutput("Territory number to fortify?", OutputFormat.QUESTION);
					country_improving = players[turn_player_id].askInt(1, NUM_COUNTRIES);
				} else {
					((Bot)players[turn_player_id]).fortifyTerritory(armies_to_place);
					country_improving = players[turn_player_id].askInt(0, NUM_COUNTRIES-1);
				}
				if(currentPlayerHuman())
					country_improving--;
				while(COUNTRIES[country_improving].getPlayer() != turn_player_id) {
					if(!currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to place armies on a territory that wasn't his.");
					Risk.sayError("Not your territory, enter another.");
					country_improving = players[turn_player_id].askInt(1, NUM_COUNTRIES);
					country_improving--;
				}
				int num_to_add;
				if(armies_to_place == 1 && currentPlayerHuman())
					num_to_add = 1;
				else {
					if(currentPlayerHuman())
						Risk.sayOutput("How many armies?", OutputFormat.QUESTION);
					num_to_add = players[turn_player_id].askInt(1,armies_to_place);
				}
				fortifyCountry(country_improving, num_to_add);
				armies_to_place -= num_to_add;
				Risk.refreshGraphics();

				if(currentPlayerHuman()) {
					String to_out = COUNTRIES[country_improving].getName() + " fortified with " + num_to_add + " armies.";
					if(armies_to_place > 0)
						to_out += " " + armies_to_place + " remaining.";
					Risk.sayOutput(to_out);
				} else if(armies_to_place > 0) {
					Risk.sayOutput(getPlayerName() + " has " + armies_to_place + " armies remaining.");
				}
			}
		} catch(Bot.RiskBotException e) {
			BadRobot(turn_player_id, "While placing armies:", e);
		}
	}

	/*
	 * Runs through the main attack loop for turn_player_id.
	 * Attacking can end at any time by entering 0 for the country id
	 */
	public void attackCountries() {
		boolean gained_territory = false;	// whether or not the player gained a territory this turn

		try {
			while(true) {	// main attack loop
				if(currentPlayerHuman())
					Risk.sayOutput(getPlayerName() + ", which of your territories would you like to attack with? When finished attacking, enter 0.", OutputFormat.QUESTION);
				else
					((Bot)players[turn_player_id]).launchAttack();
				int attacking_from; // the country id of the attacking country
				int attacking_to; // the id of the country being attacked

				boolean done_attacking = false;
				while(true) {	// Loop asking for the country number that they'd like to attack from
					if(currentPlayerHuman()) {
						attacking_from = players[turn_player_id].askInt(0, NUM_COUNTRIES);
						if(attacking_from == 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						attacking_from--; // The number entered is 1-NUM_COUNTRIES, but we want 0-(NUM_COUNTRIES-1)
					} else {
						attacking_from = players[turn_player_id].askInt();
						if(attacking_from < 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						if(attacking_from >= NUM_COUNTRIES)
							throw new Bot.RiskBotException("Tried to attack from a country that doesn't exist.");
					}
					if(COUNTRIES[attacking_from].getPlayer() != turn_player_id) {
						if(currentPlayerHuman())
							Risk.sayError("Not your territory, enter another.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + COUNTRIES[attacking_from].getName() + ", but does not own it.");
						continue;
					}
					if(COUNTRIES[attacking_from].getArmies() <= 1) {
						if(currentPlayerHuman())
							Risk.sayError("At least 2 armies are required to attack.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + COUNTRIES[attacking_from].getName() + ", but there are not enough armies in it to do so.");
						continue;
					}
					break;
				}
				if(done_attacking) break;
				int adj[] = world.getAdjacencies(attacking_from);	// Get territory adjacency list from World class
				if(adj.length == 0 || adj[0] == 0) {
					Risk.sayError("According to the map file, " + COUNTRIES[attacking_from] + " doesn't have any adjacencies.");
					continue;
				}
				ArrayList<Integer> foreign_adjacencies = new ArrayList<Integer>();
				// We are only interested in those surrounding territories that are of foreign ownership
				for(int i=1 ; i<adj.length; i++) {
					if(COUNTRIES[adj[i]].getPlayer() != turn_player_id)
						foreign_adjacencies.add(new Integer(adj[i]));
				}
				if(foreign_adjacencies.size() == 0) {
					if(currentPlayerHuman())
						Risk.sayError("No foreign adjacencies found for " + COUNTRIES[attacking_from].getName() + ".");
					else
						throw new Bot.RiskBotException("Tried to attack from " + COUNTRIES[attacking_from].getName() + ", which has no foreign adjacencies.");
					continue;
				}
				if(currentPlayerHuman()) {
					if(foreign_adjacencies.size() == 1) {
						Risk.sayOutput(COUNTRIES[foreign_adjacencies.get(0)].getName() + " is the only foreign territory adjacent to " + COUNTRIES[attacking_from].getName() + ". Launching attack.");
						attacking_to = foreign_adjacencies.get(0);
					} else {
						Risk.sayOutput("Which territory would you like to attack from " + COUNTRIES[attacking_from].getName() + "?", OutputFormat.QUESTION);
						for(int i=1; i<=foreign_adjacencies.size(); i++) {
							Risk.sayOutput(i+": " + COUNTRIES[foreign_adjacencies.get(i-1)].getName(), OutputFormat.TABBED);
						}
						int choice = players[turn_player_id].askInt(1,foreign_adjacencies.size());
						attacking_to = foreign_adjacencies.get(choice-1);
						Risk.sayOutput("Launching attack on " + COUNTRIES[attacking_to].getName() + ".");
					}
				} else {
					attacking_to = players[turn_player_id].askInt(0, NUM_COUNTRIES-1);
					if(!foreign_adjacencies.contains(new Integer(attacking_to)))
						throw new Bot.RiskBotException("Tried to attack from " + COUNTRIES[attacking_from].getName() + " to " + COUNTRIES[attacking_to].getName() + ", which is not a valid target.");
					Risk.sayOutput(getPlayerName() + " is launching an attack from " + COUNTRIES[attacking_from].getName() + " to " + COUNTRIES[attacking_to].getName() + ".");
				}
				if(attack(attacking_from, attacking_to)) {		// attack() plays out the attack
					if(over()) return;
					gained_territory = true;
				}
			}
		} catch (Bot.RiskBotException e) {
			BadRobot(turn_player_id, "While attacking countries:", e);
		}
		if(gained_territory) {
			int drawn = deck.drawCard();
			if(drawn == -1) {
				Risk.sayError("No cards left in deck.");
			} else {
				players[turn_player_id].incrementCardType(drawn); // give card to player for winning territory
				if(currentPlayerHuman()) {
					switch(drawn) {
					case 0:	Risk.sayOutput("As you have gained territory this turn, you get to draw a card. Picked up an infantry card.");
					break;
					case 1:	Risk.sayOutput("As you have gained territory this turn, you get to draw a card. Picked up a cavalry card.");
					break;
					case 2:	Risk.sayOutput("As you have gained territory this turn, you get to draw a card. Picked up an artillery card.");
					break;
					case 3:	Risk.sayOutput("As you have gained territory this turn, you get to draw a card. Picked up a wildcard.");
					break;
					}
				} else {
					Risk.sayOutput("As " + getPlayerName() + " has gained territory this turn, they get to draw a card. They are now holding " + players[turn_player_id].getNumCards() + ".");
				}
			}
		}
	}

	/*
	 * Simulates an attack from COUNTRIES index 'attacker' to COUNTRIES index 'defender'
	 * @return boolean, true if the defender's territory was captured
	 */
	private boolean attack(int attacker, int defender) {
		try {
			while(true) {
				Risk.refreshGraphics();
				int armies_attacking, armies_defending;
				if(currentPlayerHuman()) {
					if(COUNTRIES[attacker].getArmies() == 1) {
						Risk.sayOutput("No more armies to attack with.", OutputFormat.TABBED);
						return false;
					}
					if(COUNTRIES[attacker].getArmies() == 2) {
						Risk.sayOutput("Only 2 armies are left in " + COUNTRIES[attacker].getName() + ", continue the attack with one army? (Y)es or (n)o", OutputFormat.TABBED_QUESTION);
						while(true) {
							String answer = players[turn_player_id].askLine();
							if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) return false;
							else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
							else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
						}
						armies_attacking = 1;
					} else if(COUNTRIES[attacker].getArmies() == 3) {
						Risk.sayOutput("How many armies do you send to battle - 1 or 2? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = players[turn_player_id].askInt(0, 2);
					} else {
						Risk.sayOutput("How many armies do you send to battle - 1, 2, or 3? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = players[turn_player_id].askInt(0, 3);
					}
					if(armies_attacking == 0) {
						return false;
					}
				} else { // Bot
					armies_attacking = players[turn_player_id].askInt(1, 3);
				}
				if(!currentPlayerHuman()) {
					Risk.sayOutput(getPlayerName() + " is sending " + armies_attacking + " armies to battle...", OutputFormat.TABBED);
				}
				if(COUNTRIES[defender].getArmies() == 1)
					armies_defending = 1;
				else
					armies_defending = 2;

				if(currentPlayerHuman())
					Risk.sayOutput("Rolling dice...", OutputFormat.TABBED);
				Dice dice = new Dice(rand, armies_attacking, armies_defending);
				COUNTRIES[attacker].setArmies(COUNTRIES[attacker].getArmies() + dice.attackerArmyChange);
				COUNTRIES[defender].setArmies(COUNTRIES[defender].getArmies() + dice.defenderArmyChange);
				Risk.refreshGraphics();

				switch(dice.attackerArmyChange) {
				case 0:
					if(dice.defenderArmyChange == -1)
						Risk.sayOutput(COUNTRIES[defender].getName() + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ") loses 1 army.", OutputFormat.TABBED);
					else Risk.sayOutput(COUNTRIES[defender].getName() + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				case -1:
					if(dice.defenderArmyChange == -1)
						Risk.sayOutput("Each player loses 1 army.", OutputFormat.TABBED);
					else Risk.sayOutput(COUNTRIES[attacker].getName() + " (" + getPlayerName() + ") loses 1 army.", OutputFormat.TABBED);
					break;
				case -2:
					Risk.sayOutput(COUNTRIES[attacker].getName() + " (" + getPlayerName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				}

				// Territory captured
				if(COUNTRIES[defender].getArmies() == 0) {
					if(currentPlayerHuman())
						Risk.sayOutput("Congratulations " + getPlayerName() + ", you captured " + COUNTRIES[defender].getName() + "!", OutputFormat.TABBED);
					else
						Risk.sayOutput(getPlayerName() + " has captured " + COUNTRIES[defender].getName() + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ")", OutputFormat.TABBED);
					int losing_player = COUNTRIES[defender].getPlayer();
					COUNTRIES[defender].setPlayer(turn_player_id);	// transfer ownership to the attacker
					Risk.refreshGraphics();
					if(playerEliminated(losing_player)) {
						Risk.sayOutput("*** " + getPlayerName() + " has eliminated " + players[losing_player].getName() + " ***", OutputFormat.TABBED);
						if(over()) return true;	// if the game is over
						if(players[losing_player].getNumCards() > 0) { // turn_player_id gets some free cards from defender
							Risk.sayOutput("* " + getPlayerName() + " gets " + players[losing_player].getNumCards() + " free cards. *", OutputFormat.TABBED);
							players[turn_player_id].increaseCardType(0, players[losing_player].getNumCardType(0));
							players[turn_player_id].increaseCardType(1, players[losing_player].getNumCardType(1));
							players[turn_player_id].increaseCardType(2, players[losing_player].getNumCardType(2));
							players[turn_player_id].increaseCardType(3, players[losing_player].getNumCardType(3));
							players[losing_player].clearCards();
							if(players[turn_player_id].getNumCards() >= 6) {
								int additional_armies = 0;
								if(currentPlayerHuman())
									Risk.sayOutput("Since you have more than 5 cards, you must immediately turn in sets for armies.");
								else
									Risk.sayOutput("Since he has more than 5, he is required to turn in sets for armies.");
								additional_armies += turnInSet(false);
								while(players[turn_player_id].getNumCards() > 4) {
									if(currentPlayerHuman())
										Risk.sayOutput("Since you have at least 5 cards, you must immediately turn in another set for armies.");
									else
										Risk.sayOutput("Since he has at least 5 cards, he is required to turn in another set for armies.");
									additional_armies += turnInSet(false);
									Risk.sayOutput("The total is now up to " + additional_armies);
								}
								placeArmies(additional_armies);
							}
						}
					}
					int armies_to_move = 1;
					if(COUNTRIES[attacker].getArmies() - armies_attacking > 1) {
						if(currentPlayerHuman())
							Risk.sayOutput("How many armies would you like to move in for occupation? Min " + armies_attacking + ", Max " + (COUNTRIES[attacker].getArmies()-1), OutputFormat.TABBED_QUESTION);
						else
							((Bot)players[turn_player_id]).fortifyAfterVictory(attacker, defender, armies_attacking, (COUNTRIES[attacker].getArmies()-1));
						armies_to_move = players[turn_player_id].askInt(armies_attacking, (COUNTRIES[attacker].getArmies()-1));
					} else armies_to_move = armies_attacking;
					if(!currentPlayerHuman())
						Risk.sayOutput(getPlayerName() + " moves " + armies_to_move + " armies into " + COUNTRIES[defender].getName() + " for occupation.");
					COUNTRIES[attacker].setArmies( COUNTRIES[attacker].getArmies() - armies_to_move );
					COUNTRIES[defender].setArmies( COUNTRIES[defender].getArmies() + armies_to_move );

					Risk.refreshGraphics();
					return true;
				}
				if(!currentPlayerHuman())
					return false;
			}
		} catch (Bot.RiskBotException e) {
			BadRobot(turn_player_id, "While launching an attack:", e);
		}
		return false;
	}

	/*
	 * Turns a set of cards in for turn_player_id, and returns the number of armies
	 * If there is more than one possibility for set combinations, it prompts for one of them.
	 * NOTE: This method, as opposed to armiesFromCards, forces the player to play a set instead of offering.
	 */
	private int turnInSet(boolean optional) {
		int armies = 0;
		int possible_triples[][] = deck.possibleCardTriples(players[turn_player_id].getCards());

		try {

			if(optional) {
				if(currentPlayerHuman()) {
					Risk.sayOutput("You have enough cards for a set. Would you like to turn it in for " + armies_from_next_set + " additional armies? (Y)es or (n)o.", OutputFormat.QUESTION);
					while(true) {
						String answer = players[turn_player_id].askLine();
						if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) { 
							Risk.sayOutput(cardReport());
							return armies;
						}
						else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
						else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
					}
				} else {
					((Bot)players[turn_player_id]).chooseToTurnInSet();
					int choice = players[turn_player_id].askInt();
					if(choice != 1)
						return armies;
					Risk.sayOutput(getPlayerName() + " is turning in a set of cards for armies.");
				}
			}


			if(possible_triples.length == 1) {
				players[turn_player_id].decrementCardType(possible_triples[0][0]);
				players[turn_player_id].decrementCardType(possible_triples[0][1]);
				players[turn_player_id].decrementCardType(possible_triples[0][2]);
				deck.addCards(possible_triples[0]);
				armies += armies_from_next_set;
				advanceCardArmies();
			} else {
				int choice;
				if(currentPlayerHuman()) {
					Risk.sayOutput("Which combination would you like to turn in?", OutputFormat.QUESTION);
					for(int i=0;i<possible_triples.length;i++)
						Risk.sayOutput((i+1) + ": " + deck.getCardType(possible_triples[i][0]) + " " + deck.getCardType(possible_triples[i][1]) + " " + deck.getCardType(possible_triples[i][2]), OutputFormat.TABBED);
					choice = players[turn_player_id].askInt(1,possible_triples.length);
					choice--;
				} else {
					((Bot)players[turn_player_id]).chooseCardSet(possible_triples);
					choice = players[turn_player_id].askInt(0, possible_triples.length-1);
				}
				players[turn_player_id].decrementCardType(possible_triples[choice][0]);
				players[turn_player_id].decrementCardType(possible_triples[choice][1]);
				players[turn_player_id].decrementCardType(possible_triples[choice][2]);
				deck.addCards(possible_triples[choice]);
				armies += armies_from_next_set;
			}
			if(currentPlayerHuman())
				Risk.sayOutput("You get to place an additional " + armies_from_next_set + " armies.");
			else
				Risk.sayOutput(getPlayerName() + " gets to place an additional " + armies_from_next_set + " armies.");
			advanceCardArmies();
		} catch(Bot.RiskBotException e) {
			BadRobot(turn_player_id, "While turning in a set of cards:", e);
		}
		return armies;
	}

	/*
	 * Fortifies the country at COUNTRIES index country_to_foritfy-1, and adds num_armies_added
	 * @return A boolean indicating success (different player ownership than turn_player_id yields false)
	 */
	private boolean fortifyCountry(int country_to_fortify, int num_armies_added) {
		if(COUNTRIES[country_to_fortify].getPlayer() != turn_player_id)
			return false;
		if(!currentPlayerHuman())
			Risk.sayOutput(getPlayerName() + " has placed " + num_armies_added + " armies on " + COUNTRIES[country_to_fortify].getName() + ".");
		COUNTRIES[country_to_fortify].setArmies(COUNTRIES[country_to_fortify].getArmies() + num_armies_added);
		return true;
	}

	/*
	 * Checks if player_id has been eliminated from the game.
	 * If so, sets still_in[player_id] as false to indicate they are out.
	 */
	private boolean playerEliminated(int player_id) {		
		for(int i=0;i<NUM_COUNTRIES;i++) {
			if(COUNTRIES[i].getPlayer() == player_id) return false;
		}
		players[player_id].setStillIn(false);
		return true;
	}

	/*
	 * If a RiskBot gives an incorrect input or exceeds the time limit, this method is called to print an error message and quit.
	 * Player_id is the bot that messed up. Scope is some message about what part of the game/turn it occured in.
	 */
	public void BadRobot(int player_id, String scope, Exception e) {
		Risk.sayError("The RiskBot " + players[player_id].getName() + " messed up big time, and the game could not go on.");
		Risk.sayOutput(scope);
		Risk.sayOutput(e.getMessage());
		exit();
	}

	private void exit() {
		System.exit(0);
	}

	/*
	 * This method calculates how many armies the player who's turn
	 * it is gets to place that come from having a certain number of territories
	 * @return int representing the num of armies
	 */
	private int armiesFromTerritories() {
		int territories_held = 0;
		for(int i=0;i<NUM_COUNTRIES;i++) {
			if(COUNTRIES[i].getPlayer() == turn_player_id)
				territories_held++;
		}
		return Math.max(territories_held / 3, 3);
	}

	/*
	 * This method returns how many armies the play who's turn it is gets to place
	 * that comes from having full continents. It iterates COUNTRIES looking for a
	 * territory of each continent that is NOT owned by the current player.
	 * @return int representing the num of armies
	 */
	private int armiesFromContinents() {
		boolean continents_won[] = new boolean[CONTINENT_NAMES.length];
		for(int i=0;i<continents_won.length;i++) continents_won[i] = true;
		for(int i=0;i<COUNTRIES.length;i++) {
			if(COUNTRIES[i].getPlayer() != turn_player_id)
				continents_won[COUNTRIES[i].getCont()] = false;
		}
		int bonus_armies = 0;
		for(int i=0;i<continents_won.length;i++) {
			if(continents_won[i]) {
				bonus_armies += CONTINENT_BONUSES[i];
				Risk.sayOutput("+ " + CONTINENT_BONUSES[i] + " armies for owning all of " + CONTINENT_NAMES[i] + ".", OutputFormat.TABBED);
			}
		}
		return bonus_armies;
	}

	/*
	 * Checks if turn_player_id can turn in sets of cards for armies
	 * @return int, the number of armies gained from cards (default 0)
	 */
	private int armiesFromCards() {
		int armies = 0;
		if(players[turn_player_id].getNumCards() >= 5) {
			if(currentPlayerHuman())
				Risk.sayOutput("Since you have " + players[turn_player_id].getNumCards() + " cards, you must turn in a set for armies.");
			else
				Risk.sayOutput("Since " + getPlayerName() + " has " + players[turn_player_id].getNumCards() + " cards, he must turn in a set for armies.");
			armies += turnInSet(false);
		}

		int possible_triples[][] = deck.possibleCardTriples(players[turn_player_id].getCards());
		if(possible_triples.length > 0) {
			armies += turnInSet(true);
		}
		if(currentPlayerHuman())
			Risk.sayOutput(cardReport());
		return armies;
	}

	/*
	 * Generates a string describing the current player's hand.
	 * Examples: Your hand consists of 2 infantry, 1 cannon, and 1 wildcard.
	 */
	private String cardReport() {
		String card_report = "";
		int card_variety = 0;
		int last_type = -1;
		for(int i=0;i<4;i++)
			if(players[turn_player_id].getNumCardType(i) > 0) { card_variety++; last_type = i; }
		if(players[turn_player_id].getNumCards() == 0)
			card_report += "You do not have any cards in your hand.";
		else if(card_variety == 1) {
			card_report += "Your hand consists of: " + players[turn_player_id].getNumCardType(last_type) + " " + deck.getCardType(last_type) + ".";
		} else {
			card_report += "Your hand consists of: ";
			for(int i=0;i<4;i++) {
				if(players[turn_player_id].getNumCardType(i) > 0) {
					if(i != last_type) {
						card_report += players[turn_player_id].getNumCardType(i) + " " + deck.getCardType(i) + ", ";
					} else card_report += "and " + players[turn_player_id].getNumCardType(i) + " " + deck.getCardType(i) + ".";
				}
			}
		}
		return card_report;
	}

	/*
	 * Step 3 of a player's turn. Lets the player move armies from 1 country
	 * to 1 neighboring country. This move is optional.
	 */
	public void fortifyPosition() {
		int move_from = 0, move_to = 0, army_change = 0;
		try {

			if(currentPlayerHuman()) {
				Risk.sayOutput("You may now fortify your position.");
				Risk.sayOutput("From which territory would you like to move armies? To skip fortification, enter 0.", OutputFormat.QUESTION);
				while(true) {	// Loop asking for the country number that they'd like to move armies from
					move_from = players[turn_player_id].askInt(0, NUM_COUNTRIES);
					if(move_from == 0) return;
					move_from--; // The number entered is 1-NUM_COUNTRIES, but we want 0-(NUM_COUNTRIES-1)
					if(COUNTRIES[move_from].getPlayer() != turn_player_id) {
						Risk.sayError("Not your territory, enter another.");
						continue;
					}
					if(COUNTRIES[move_from].getArmies() <= 1) {
						Risk.sayError("A minimum of 1 army must be present in each territory. Enter another.");
						continue;
					}
					break;
				}
			} else { // BOT
				((Bot)players[turn_player_id]).fortifyPosition();
				move_from = players[turn_player_id].askInt();
				if(move_from < 0) return;
				if(move_from >= NUM_COUNTRIES)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't exist.");
				if(COUNTRIES[move_from].getPlayer() != turn_player_id)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't belong to them.");
				if(COUNTRIES[move_from].getArmies() <= 1)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't have more than 1.");
				move_to = players[turn_player_id].askInt(0, NUM_COUNTRIES-1);
				if(COUNTRIES[move_to].getPlayer() != turn_player_id)
					throw new Bot.RiskBotException("Tried to move armies to a territory that doesn't belong to them.");
				army_change = players[turn_player_id].askInt(1, COUNTRIES[move_from].getArmies()-1);
			}


			int adj[] = world.getAdjacencies(move_from);	// Get territory adjacency list from World class
			if(adj.length == 0 || adj[0] == 0) {
				if(!currentPlayerHuman())
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no adjacencies.");
				Risk.sayError("According to the map file, " + COUNTRIES[move_from] + " doesn't have any adjacencies.");
				fortifyPosition();
			}
			ArrayList<Integer> domestic_adjacencies = new ArrayList<Integer>();
			// We are only interested in those surrounding territories that belong to the player
			for(int i=1 ; i<adj.length; i++) {
				if(COUNTRIES[adj[i]].getPlayer() == turn_player_id)
					domestic_adjacencies.add(new Integer(adj[i]));
			}
			if(domestic_adjacencies.size() == 0) {
				if(!currentPlayerHuman())
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no friendly adjacencies.");
				Risk.sayError("No friendly adjacencies found for " + COUNTRIES[move_from].getName() + ".");
				fortifyPosition();
			}
			if(currentPlayerHuman()) {
				if(domestic_adjacencies.size() == 1) {
					Risk.sayOutput(COUNTRIES[domestic_adjacencies.get(0)].getName() + " is the only friendly territory adjacent to " + COUNTRIES[move_from].getName() + ".");
					move_to = domestic_adjacencies.get(0);
				} else {
					Risk.sayOutput("Which territory would you like to foritfy?", OutputFormat.QUESTION);
					for(int i=1; i<=domestic_adjacencies.size(); i++) {
						Risk.sayOutput(i+": " + COUNTRIES[domestic_adjacencies.get(i-1)].getName(), OutputFormat.TABBED);
					}
					int choice = ((Human)players[turn_player_id]).askInt(1,domestic_adjacencies.size());
					move_to = domestic_adjacencies.get(choice-1);
				}
			} else {
				if(!domestic_adjacencies.contains(move_to))
					throw new Bot.RiskBotException("Tried to move armies from " + COUNTRIES[move_from].getName() + " to " + COUNTRIES[move_to].getName() + ", but they don't connect.");
			}
		} catch( Bot.RiskBotException e) {
			BadRobot(turn_player_id, "During the fortification phase:", e);
		}
		if(currentPlayerHuman()) {
			Risk.sayOutput("How many armies would you like to move into " + COUNTRIES[move_to].getName() + "? Max " + (COUNTRIES[move_from].getArmies()-1), OutputFormat.QUESTION);
			army_change = ((Human)players[turn_player_id]).askInt(1, COUNTRIES[move_from].getArmies()-1);
		} else {
			Risk.sayOutput(getPlayerName() + " is fortifying " + COUNTRIES[move_to].getName() + " with " + army_change + " armies from " + COUNTRIES[move_from].getName() + ".");
		}
		COUNTRIES[move_from].setArmies(COUNTRIES[move_from].getArmies() - army_change);
		COUNTRIES[move_to].setArmies(COUNTRIES[move_to].getArmies() + army_change);
		Risk.refreshGraphics();
	}

	/*
	 * claimCountry is used in game setup, when players are claiming territories.
	 * It attempts to claim a territory at COUNTRIES index to_claim-1 for player_id.
	 * @return A boolean indicating success or failure
	 */
	private boolean claimCountry(int to_claim) {
		if(COUNTRIES[to_claim-1].getArmies() != 0)	// If the territory has already been claimed by someone
			return false;
		else {
			COUNTRIES[to_claim-1].setArmies(1);
			COUNTRIES[to_claim-1].setPlayer(turn_player_id);
		}
		return true;
	}

	/* Advances the turns by one player. Edits the turn_player_id variable
	 * Called to advance turn_player_id to the next int in a looped sequence of PLAYER_NAMES indices
	 */
	public void advanceTurn() {
		if(turn_player_id > NUM_PLAYERS - 1) {
			turn_player_id = 0;
			Risk.sayOutput("First player not randomly chosen. " + getPlayerName() + " goes first.");
		}
		if(turn_player_id == NUM_PLAYERS - 1)
			turn_player_id = 0;
		else turn_player_id++;
		if(!players[turn_player_id].getStillIn()) advanceTurn();	// if the player just advanced to is not in, do it again
	}

	// advances armies_from_next_set according to how much the army amount should go up
	// Follows 4->6->8->10->12->15->20->25 etc..
	public void advanceCardArmies() {
		if(armies_from_next_set < 12) armies_from_next_set += 2;
		else if(armies_from_next_set == 12) armies_from_next_set += 3;
		else armies_from_next_set += 5;
	}

	/*
	 * Returns true if game is over (still_in only has one true value)
	 * If this is the case it also sets "winner" to the player id who won
	 */
	public boolean over() {
		int possible_winner = -1;
		for(int i=0;i<NUM_PLAYERS;i++) {
			if(players[i].getStillIn()) {
				if(possible_winner == -1) possible_winner = i;
				else return false;
			}
		}
		winner = possible_winner;
		return true;
	}

	/*
	 * Gets the name of the player whose turn it is.
	 */
	public String getPlayerName() {
		return players[turn_player_id].getName();
	}

	public int getCurrentPlayerID() {
		return turn_player_id;
	}

	// If the player who's turn it is is Human, returns true
	public boolean currentPlayerHuman() {
		if(players[turn_player_id].getType() == Player.HUMAN)
			return true;
		else return false;
	}

	// Retrieves the color of a given player id. Used by the Graphics class
	public Color getPlayerColor(int player_id) {
		if(player_id >= 0 && player_id < NUM_PLAYERS)
			return players[player_id].getColor();
		else return Color.gray;
	}

	// Returns the Color at some index of CONTINENT_COLORS
	public Color getContinentColor(int cont) {
		if(cont < 0 || cont > CONTINENT_NAMES.length-1) {
			Risk.sayError("Could not get continent color of continent id " + cont);
			exit();
		}
		return CONTINENT_COLORS[cont];
	}

	public int getWinner() {
		return winner;
	}

	public Country[] getCountries() {
		return COUNTRIES;
	}
	public int[] getContinentInfo() {
		return CONTINENT_BONUSES;
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
	// Returns how many armies the next person to turn in a set of cards will get.
	public int getArmiesFromNextSet() {
		return armies_from_next_set;
	}
}
