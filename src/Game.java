/*
 * The Game class is the game engine for RiskArena. It maintains information
 * about the game, and provides the procedure involved in running a player's turn
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import javax.swing.SwingUtilities;

public class Game {
	private boolean watch;	// Becomes true if the game is being watched (otherwise it is simulated without graphics)

	private GameBoard board; // The Graphics object that draws everything

	private GameData data;
	private Deck deck; // Deck of cards
	
	private int player_id_that_goes_first; // The index of PLAYER_NAMES that goes first. Randomly set in game initialization
	private int winner = 0;	// player id of the winner
	private Random rand;

	private boolean save_game_log; // If true, write game messages to log_path
	private BufferedWriter log_writer;	// writes to log_path
	private String log_path;

	private ArrayList<Integer> game_results; // As players are eliminated, their IDs are added to this ArrayList
	private long elapsed_time = 0;			// How long the game took

	/* This is the primary Game constructor. It sets up the data
	 * structure in Game.java that hold game information.
	 * @param ArrayList<String> player_names - the names of players of the game
	 * @param String map_file - file path to the map file, sent to a MapReader object
	 * @param boolean w - whether or not this game is being watched graphically
	 */
	public Game(Player p[], String map_file, boolean w, boolean sgl) {
		data = new GameData(p, map_file);
		save_game_log = sgl;
		watch = w; // whether or not to show the game
		if(watch) {
			initializeGraphics();
			// Wait for the graphics to be initialized on its own thread.
			while(board==null) {
				try {
					Thread.sleep(10);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		// If the game is not being watched, there's no need to have Bots pause before making decisions
		if(!watch)
			data.setBotPlayingSpeed(0);

		if(save_game_log)	// If a game log is being written
			setLogFilePath();

		game_results = new ArrayList<Integer>();	// Initialize game results, keeping track of how players finish

		// Initialize the Random generator with the current time
		Date dat = new Date();
		rand = new Random(dat.getTime());

		// initialize deck
		deck = new Deck(rand);

		setPlayerThatGoesFirst();
	}

	public void init() {
		if(save_game_log) {
			// Create the BufferedWriter that will write to log path
			try {
				File file = new File(log_path);
				file.createNewFile();
				FileWriter fstream = new FileWriter(log_path,true);
				log_writer = new BufferedWriter(fstream);
				// Write the beginnings of an HTML game report
				log_writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Game at " + new Date().toString()
						+ "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"log_format.css\"></head><body>");
			} catch(Exception e) {
				Risk.sayError("Unable to create log file at " + log_path);
			}
		}

		data.sendGameDataToBots();
		if(watch) {
			sendGameDataToBoard();
			data.sendHumanListenersToBoard(board);
		}
	}

	public void play() {
		long start_time = System.nanoTime();
		placeInitialArmies();	// Game setup, involving players placing initial armies

		while(!over()) {	// over returns true when the game is done
			sayOutput("=======================================");
			sayOutput("Beginning " + data.getPlayerName() + "'s turn.");
			fortifyArmies();  		// Step 1 of a player's turn
			attackCountries();		// Step 2 of a player's turn
			if(!over()) {
				fortifyPosition();		// Step 3 of a player's turn
				advanceTurn();
			}
		}
		int winner = getWinner(); // get the winner from the game engine
		sayOutput("Congratulations " + data.data.getPlayerName(winner) + ", you win " + Risk.PROJECT_NAME + "!");
		//game_results.add(new Integer(players[winner].getId()));
		game_results.add(new Integer(winner));
		elapsed_time = System.nanoTime() - start_time;
	}

	/*
	 * This method creates and initializes the GameBoard object
	 */
	private void initializeGraphics() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				board = new GameBoard();
				board.setVisible(true);
			}
		});
	}

	private void sendGameDataToBoard() {
		board.sendGameData(data);
		refreshGraphics();
	}

	public void refreshGraphics() {
		if(!watch)
			return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				board.refresh();
			}
		});
	}

	/* Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 * tabbed is whether or not it should be prepended with a \t
	 */
	public void sayOutput(final String toSay, final int output_format_style, boolean forced) {
		if( Risk.output_to_std || forced ) {
			if(output_format_style == OutputFormat.TABBED)
				System.out.println("\t" + toSay);
			else if (output_format_style == OutputFormat.ERROR) {
				System.err.println("ERROR: " + toSay);
				System.err.flush();
			} else if(output_format_style != OutputFormat.ANSWER)
				System.out.println(toSay);
		}
		if(watch && board != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					board.sayOutput(toSay, output_format_style);
				}
			});
		}
		if(save_game_log && log_writer != null) {
			// Append to the log_path file
			try {
				String HTMLtoSay = "<p class=\"" + OutputFormat.getClassName(output_format_style) + "\">" + toSay + "</p>";
				log_writer.write(HTMLtoSay + '\n');
			} catch (IOException e) {
				Risk.sayError("Unable to write to " + log_path + ":");
				Risk.sayError(e.getMessage());
			}
		}
	}

	/*
	 * Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 */
	public void sayOutput(final String toSay, final boolean forced) {
		sayOutput(toSay, OutputFormat.NORMAL, forced);
	}

	/* Called by various methods to send something to whatever
	 * error output is being used.
	 * @param The string wishing to be outputted error message.
	 */
	public void sayError(final String toSay, final boolean forced) {
		sayOutput(toSay, OutputFormat.ERROR, forced);
	}
	
	/*
	 * Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 */
	public void sayOutput(final String toSay) {
		sayOutput(toSay, OutputFormat.NORMAL, false);
	}

	/* Called by various methods to send something to whatever
	 * error output is being used.
	 * @param The string wishing to be outputted error message.
	 */
	public void sayError(final String toSay) {
		sayOutput(toSay, OutputFormat.ERROR, false);
	}
	
	public void sayOutput(final String toSay, final int output_format_style) {
		sayOutput(toSay, output_format_style, false);
	}

	/*
	 * Chooses a random player to go first, sets the global int player_id_that_goes_first.
	 */
	private void setPlayerThatGoesFirst() {
		int some_player = rand.nextInt(data.NUM_PLAYERS);
		player_id_that_goes_first = some_player;
		data.setCurrentPlayerID(player_id_that_goes_first);
		if(data.currentPlayerHuman())
			sayOutput(data.getPlayerName() + ", you have been chosen to go first.");
		else
			sayOutput(data.getPlayerName() + " has been chosen to go first.");
	}

	/*
	 * At the beginning of the game, this method has the players claim territories and place their initial armies
	 */
	private void placeInitialArmies() {
		int armiesToPlace = 20;
		switch(data.NUM_PLAYERS) {
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
		default: Risk.sayError("Army placement is only configured for 2-6 players. Set armiesToPlace in Game.placeInitialArmies() for " + data.NUM_PLAYERS + " players.");
		break;
		}
		sayOutput("Each player has " + armiesToPlace + " armies to place.");

		/* Initial army placement */
		int pile[] = new int[data.NUM_PLAYERS]; // array representing each player's pile of initial armies to place
		for(int i=0;i<data.NUM_PLAYERS;i++) {
			pile[i] = armiesToPlace;
			if(data.data.getPlayerName(i).equals("letmecheat"))	// Cheat
				pile[i] += 30;
		}
		// claiming:
		try {
			for(int i=0; i < data.NUM_COUNTRIES; i++) {
				int claimed;
				if(data.currentPlayerHuman()) {
					sayOutput(data.getPlayerName() + ": Enter the number of the territory you would like to claim.", OutputFormat.QUESTION);
					claimed = players[data.getCurrentPlayerID()].askInt(1,data.NUM_COUNTRIES);
				} else {
					((Bot)players[data.getCurrentPlayerID()]).claimTerritory();	// Signal to the bot that they need to make a decision
					claimed = players[data.getCurrentPlayerID()].askInt(0,data.NUM_COUNTRIES-1);
				}
				if(!data.currentPlayerHuman()) claimed++;
				while(!claimCountry(claimed)) {		// If claiming a country fails, it's because it's already occupied
					if(!data.currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to claim a territory that was already claimed.");

					Risk.sayError("Territory already taken. Choose another.");
					claimed = players[data.getCurrentPlayerID()].askInt(1,data.NUM_COUNTRIES);
				}
				if(!data.currentPlayerHuman())
					sayOutput(data.getPlayerName() + " has claimed " + COUNTRIES[claimed-1].getName() + ".");
				refreshGraphics();
				pile[data.getCurrentPlayerID()]--;
				advanceTurn();
			}
			// fortifying:
			while(true) {
				int to_fortify;
				if(data.currentPlayerHuman()) {
					sayOutput(data.getPlayerName() + ": Enter the number of the territory you would like to fortify.", OutputFormat.QUESTION);
					to_fortify = players[data.getCurrentPlayerID()].askInt(1, data.NUM_COUNTRIES);
				} else {
					((Bot)players[data.getCurrentPlayerID()]).fortifyTerritory(pile[data.getCurrentPlayerID()]);
					to_fortify = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES-1);
				}
				if(data.currentPlayerHuman())
					to_fortify--;
				while(COUNTRIES[to_fortify].getPlayer() != data.getCurrentPlayerID()) {
					if(!data.currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to fortify a territory that wasn't his.");
					Risk.sayError("Not your territory to fortify. Enter another.");
					to_fortify = players[data.getCurrentPlayerID()].askInt(1, data.NUM_COUNTRIES);
					to_fortify--;
				}
				if(data.currentPlayerHuman())
					sayOutput("How many armies would you like to add to " + COUNTRIES[to_fortify].getName() + "? " + pile[data.getCurrentPlayerID()] + " armies left in your pile.", OutputFormat.QUESTION);
				int armies_added = players[data.getCurrentPlayerID()].askInt(1, pile[data.getCurrentPlayerID()]);

				fortifyCountry(to_fortify, armies_added);
				pile[data.getCurrentPlayerID()] -= armies_added;
				refreshGraphics();

				boolean done_fortifying = false;
				for(int i=0;;i++) {
					if(i>data.NUM_PLAYERS) {
						done_fortifying = true;
						break;
					}
					advanceTurn();
					if(pile[data.getCurrentPlayerID()] > 0) break; 
				}
				if(done_fortifying) break;
			}
			refreshGraphics();
			turn_player_id = player_id_that_goes_first; // after initial fortifying, the player to go first
			// should be the same player that claimed territory first.
		} catch(Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "During the initial army placement/fortification phase:", e);
		}
	}

	/*
	 * Step 1 of a player turn. Fortifies territories for the player.
	 * Called from Risk.java
	 */
	private void fortifyArmies() {
		int armies_to_place = 0;
		armies_to_place += armiesFromCards();
		armies_to_place += armiesFromContinents();
		armies_to_place += armiesFromTerritories();
		if(data.currentPlayerHuman())
			sayOutput("You have " + armies_to_place + " armies to fortify with.");
		placeArmies(armies_to_place);
	}

	// place "armies_to_place" armies on a territory of turn_player_id's choice
	private void placeArmies(int armies_to_place) {
		try {
			while(armies_to_place > 0) {
				int country_improving;
				if(data.currentPlayerHuman()) {
					sayOutput("Territory number to fortify?", OutputFormat.QUESTION);
					country_improving = players[data.getCurrentPlayerID()].askInt(1, data.NUM_COUNTRIES);
				} else {
					((Bot)players[data.getCurrentPlayerID()]).fortifyTerritory(armies_to_place);
					country_improving = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES-1);
				}
				if(data.currentPlayerHuman())
					country_improving--;
				while(COUNTRIES[country_improving].getPlayer() != data.getCurrentPlayerID()) {
					if(!data.currentPlayerHuman())
						throw new Bot.RiskBotException("Tried to place armies on a territory that wasn't his.");
					Risk.sayError("Not your territory, enter another.");
					country_improving = players[data.getCurrentPlayerID()].askInt(1, data.NUM_COUNTRIES);
					country_improving--;
				}
				int num_to_add;
				if(armies_to_place == 1 && data.currentPlayerHuman())
					num_to_add = 1;
				else {
					if(data.currentPlayerHuman())
						sayOutput("How many armies?", OutputFormat.QUESTION);
					num_to_add = players[data.getCurrentPlayerID()].askInt(1,armies_to_place);
				}
				fortifyCountry(country_improving, num_to_add);
				armies_to_place -= num_to_add;
				refreshGraphics();

				if(data.currentPlayerHuman()) {
					String to_out = COUNTRIES[country_improving].getName() + " fortified with " + num_to_add + " armies.";
					if(armies_to_place > 0)
						to_out += " " + armies_to_place + " remaining.";
					sayOutput(to_out);
				} else if(armies_to_place > 0) {
					sayOutput(data.getPlayerName() + " has " + armies_to_place + " armies remaining.");
				}
			}
		} catch(Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "While placing armies:", e);
		}
	}

	/*
	 * Runs through the main attack loop for turn_player_id.
	 * Attacking can end at any time by entering 0 for the country id
	 */
	private void attackCountries() {
		boolean gained_territory = false;	// whether or not the player gained a territory this turn

		try {
			while(true) {	// main attack loop
				if(data.currentPlayerHuman())
					sayOutput(data.getPlayerName() + ", which of your territories would you like to attack with? When finished attacking, enter 0.", OutputFormat.QUESTION);
				else
					((Bot)players[data.getCurrentPlayerID()]).launchAttack();
				int attacking_from; // the country id of the attacking country
				int attacking_to; // the id of the country being attacked

				boolean done_attacking = false;
				while(true) {	// Loop asking for the country number that they'd like to attack from
					if(data.currentPlayerHuman()) {
						attacking_from = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES);
						if(attacking_from == 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						attacking_from--; // The number entered is 1-data.NUM_COUNTRIES, but we want 0-(data.NUM_COUNTRIES-1)
					} else {
						attacking_from = players[data.getCurrentPlayerID()].askInt();
						if(attacking_from < 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						if(attacking_from >= data.NUM_COUNTRIES)
							throw new Bot.RiskBotException("Tried to attack from a country that doesn't exist.");
					}
					if(COUNTRIES[attacking_from].getPlayer() != data.getCurrentPlayerID()) {
						if(data.currentPlayerHuman())
							Risk.sayError("Not your territory, enter another.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + COUNTRIES[attacking_from].getName() + ", but does not own it.");
						continue;
					}
					if(COUNTRIES[attacking_from].getArmies() <= 1) {
						if(data.currentPlayerHuman())
							Risk.sayError("At least 2 armies are required to attack.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + COUNTRIES[attacking_from].getName() + ", but there are not enough armies in it to do so.");
						continue;
					}
					break;
				}
				if(done_attacking) break;
				int adj[] = world.getAdjacencies(attacking_from);	// Get territory adjacency list from World class
				if(adj.length == 0) {
					Risk.sayError("According to the map file, " + COUNTRIES[attacking_from] + " doesn't have any adjacencies.");
					continue;
				}
				ArrayList<Integer> foreign_adjacencies = new ArrayList<Integer>();
				// We are only interested in those surrounding territories that are of foreign ownership
				for(int i=0 ; i<adj.length; i++) {
					if(COUNTRIES[adj[i]].getPlayer() != data.getCurrentPlayerID())
						foreign_adjacencies.add(new Integer(adj[i]));
				}
				if(foreign_adjacencies.size() == 0) {
					if(data.currentPlayerHuman())
						Risk.sayError("No foreign adjacencies found for " + COUNTRIES[attacking_from].getName() + ".");
					else
						throw new Bot.RiskBotException("Tried to attack from " + COUNTRIES[attacking_from].getName() + ", which has no foreign adjacencies.");
					continue;
				}
				if(data.currentPlayerHuman()) {
					if(foreign_adjacencies.size() == 1) {
						sayOutput(COUNTRIES[foreign_adjacencies.get(0)].getName() + " is the only foreign territory adjacent to " + COUNTRIES[attacking_from].getName() + ". Launching attack.");
						attacking_to = foreign_adjacencies.get(0);
					} else {
						sayOutput("Which territory would you like to attack from " + COUNTRIES[attacking_from].getName() + "?", OutputFormat.QUESTION);
						for(int i=1; i<=foreign_adjacencies.size(); i++) {
							sayOutput(i+": " + COUNTRIES[foreign_adjacencies.get(i-1)].getName(), OutputFormat.TABBED);
						}
						int choice = players[data.getCurrentPlayerID()].askInt(1,foreign_adjacencies.size());
						attacking_to = foreign_adjacencies.get(choice-1);
						sayOutput("Launching attack on " + COUNTRIES[attacking_to].getName() + ".");
					}
				} else {
					attacking_to = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES-1);
					if(!foreign_adjacencies.contains(new Integer(attacking_to)))
						throw new Bot.RiskBotException("Tried to attack from " + COUNTRIES[attacking_from].getName() + " to " + COUNTRIES[attacking_to].getName() + ", which is not a valid target.");
					sayOutput(data.getPlayerName() + " is launching an attack from " + COUNTRIES[attacking_from].getName() + " to " + COUNTRIES[attacking_to].getName() + ".");
				}
				if(attack(attacking_from, attacking_to)) {		// attack() plays out the attack
					gained_territory = true;
					if(over()) return;
				}
			}
		} catch (Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "While attacking countries:", e);
		}
		if(gained_territory) {
			int drawn = deck.drawCard();
			if(drawn == -1) {
				Risk.sayError("No cards left in deck.");
			} else {
				players[data.getCurrentPlayerID()].incrementCardType(drawn); // give card to player for winning territory
				if(data.currentPlayerHuman()) {
					switch(drawn) {
					case 0:	sayOutput("As you have gained territory this turn, you get to draw a card. Picked up an infantry card.");
					break;
					case 1:	sayOutput("As you have gained territory this turn, you get to draw a card. Picked up a cavalry card.");
					break;
					case 2:	sayOutput("As you have gained territory this turn, you get to draw a card. Picked up an artillery card.");
					break;
					case 3:	sayOutput("As you have gained territory this turn, you get to draw a card. Picked up a wildcard.");
					break;
					}
				} else {
					sayOutput("As " + data.getPlayerName() + " has gained territory this turn, they get to draw a card. They are now holding " + players[data.getCurrentPlayerID()].getNumCards() + ".");
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
				refreshGraphics();
				int armies_attacking, armies_defending;
				if(data.currentPlayerHuman()) {
					if(data.getArmies(attacker) == 1) {
						sayOutput("No more armies to attack with.", OutputFormat.TABBED);
						return false;
					}
					if(data.getArmies(attacker) == 2) {
						sayOutput("Only 2 armies are left in " + data.getCountryName(attacker) + ", continue the attack with one army? (Y)es or (n)o", OutputFormat.TABBED_QUESTION);
						while(true) {
							String answer = players[data.getCurrentPlayerID()].askLine();
							if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) return false;
							else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
							else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
						}
						armies_attacking = 1;
					} else if(data.getArmies(attacker) == 3) {
						sayOutput("How many armies do you send to battle - 1 or 2? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = players[data.getCurrentPlayerID()].askInt(0, 2);
					} else {
						sayOutput("How many armies do you send to battle - 1, 2, or 3? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = players[data.getCurrentPlayerID()].askInt(0, 3);
					}
					if(armies_attacking == 0) {
						return false;
					}
				} else { // Bot
					armies_attacking = players[data.getCurrentPlayerID()].askInt(1, 3);
				}
				if(!data.currentPlayerHuman()) {
					sayOutput(data.getPlayerName() + " is sending " + armies_attacking + " armies to battle...", OutputFormat.TABBED);
				}
				if(data.getArmies(defender) == 1)
					armies_defending = 1;
				else
					armies_defending = 2;

				if(data.currentPlayerHuman())
					sayOutput("Rolling dice...", OutputFormat.TABBED);
				Dice dice = new Dice(rand, armies_attacking, armies_defending);
				data.setArmies(attacker,data.getArmies(attacker) + dice.attackerArmyChange);
				data.setArmies(defender,data.getArmies(defender) + dice.defenderArmyChange);
				refreshGraphics();

				switch(dice.attackerArmyChange) {
				case 0:
					if(dice.defenderArmyChange == -1)
						sayOutput(data.getCountryName(defender) + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ") loses 1 army.", OutputFormat.TABBED);
					else sayOutput(data.getCountryName(defender) + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				case -1:
					if(dice.defenderArmyChange == -1)
						sayOutput("Each player loses 1 army.", OutputFormat.TABBED);
					else sayOutput(data.getCountryName(attacker) + " (" + data.getPlayerName() + ") loses 1 army.", OutputFormat.TABBED);
					break;
				case -2:
					sayOutput(data.getCountryName(attacker) + " (" + data.getPlayerName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				}

				// Territory captured
				if(data.getArmies(defender) == 0) {
					if(data.currentPlayerHuman())
						sayOutput("Congratulations " + data.getPlayerName() + ", you captured " + data.getCountryName(defender) + "!", OutputFormat.TABBED);
					else
						sayOutput(data.getPlayerName() + " has captured " + data.getCountryName(defender) + " (" + players[COUNTRIES[defender].getPlayer()].getName() + ")", OutputFormat.TABBED);
					int losing_player = COUNTRIES[defender].getPlayer();
					COUNTRIES[defender].setPlayer(data.getCurrentPlayerID());	// transfer ownership to the attacker
					refreshGraphics();
					if(playerEliminated(losing_player)) {
						sayOutput("*** " + data.getPlayerName() + " has eliminated " + players[losing_player].getName() + " ***", OutputFormat.TABBED);
						if(over()) return true;	// if the game is over
						if(players[losing_player].getNumCards() > 0) { // turn_player_id gets some free cards from defender
							sayOutput("* " + data.getPlayerName() + " gets " + players[losing_player].getNumCards() + " free cards. *", OutputFormat.TABBED);
							players[data.getCurrentPlayerID()].increaseCardType(0, players[losing_player].getNumCardType(0));
							players[data.getCurrentPlayerID()].increaseCardType(1, players[losing_player].getNumCardType(1));
							players[data.getCurrentPlayerID()].increaseCardType(2, players[losing_player].getNumCardType(2));
							players[data.getCurrentPlayerID()].increaseCardType(3, players[losing_player].getNumCardType(3));
							players[losing_player].clearCards();
							if(players[data.getCurrentPlayerID()].getNumCards() >= 6) {
								int additional_armies = 0;
								if(data.currentPlayerHuman())
									sayOutput("Since you have more than 5 cards, you must immediately turn in sets for armies.");
								else
									sayOutput("Since he has more than 5, he is required to turn in sets for armies.");
								additional_armies += turnInSet(false);
								while(players[data.getCurrentPlayerID()].getNumCards() > 4) {
									if(data.currentPlayerHuman())
										sayOutput("Since you have at least 5 cards, you must immediately turn in another set for armies.");
									else
										sayOutput("Since he has at least 5 cards, he is required to turn in another set for armies.");
									additional_armies += turnInSet(false);
									sayOutput("The total is now up to " + additional_armies);
								}
								placeArmies(additional_armies);
							}
						}
					}
					int armies_to_move = 1;
					if(data.getArmies(attacker) - armies_attacking > 1) {
						if(data.currentPlayerHuman())
							sayOutput("How many armies would you like to move in for occupation? Min " + armies_attacking + ", Max " + (data.getArmies(attacker)-1), OutputFormat.TABBED_QUESTION);
						else
							((Bot)players[data.getCurrentPlayerID()]).fortifyAfterVictory(attacker, defender, armies_attacking, (data.getArmies(attacker)-1));
						armies_to_move = players[data.getCurrentPlayerID()].askInt(armies_attacking, (data.getArmies(attacker)-1));
					} else armies_to_move = armies_attacking;
					if(!data.currentPlayerHuman())
						sayOutput(data.getPlayerName() + " moves " + armies_to_move + " armies into " + data.getCountryName(defender) + " for occupation.");
					data.setArmies(attacker, data.getArmies(attacker) - armies_to_move );
					data.setArmies(defender, data.getArmies(defender) + armies_to_move );

					refreshGraphics();
					return true;
				}
				if(!data.currentPlayerHuman())
					return false;
			}
		} catch (Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "While launching an attack:", e);
		}
		return false;
	}

	/*
	 * Turns a set of cards in for turn_player_id, and returns the number of armies
	 * If there is more than one possibility for set combinations, it prompts for one of them.
	 * If 'optional' is true, the player can choose to skip turning in a set
	 */
	private int turnInSet(boolean optional) {
		int armies = 0;
		int possible_triples[][] = deck.possibleCardTriples(players[data.getCurrentPlayerID()].getCards());

		try {
			if(optional) {
				if(data.currentPlayerHuman()) {
					sayOutput("You have enough cards for a set. Would you like to turn it in for " + data.getArmiesFromNextSet() + " additional armies? (Y)es or (n)o.", OutputFormat.QUESTION);
					while(true) {
						String answer = players[data.getCurrentPlayerID()].askLine();
						if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) { 
							sayOutput(cardReport());
							return armies;
						}
						else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
						else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
					}
				} else {	// Bot
					((Bot)players[data.getCurrentPlayerID()]).chooseToTurnInSet();
					int choice = players[data.getCurrentPlayerID()].askInt();
					if(choice != 1)
						return armies;
					sayOutput(data.getPlayerName() + " is turning in a set of cards for armies.");
				}
			}

			if(possible_triples.length == 1) {
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[0][0]);
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[0][1]);
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[0][2]);
				deck.addCards(possible_triples[0]);
				armies += armies_from_next_set;
			} else {		// If there are more than one possible sets to turn in
				int choice;
				if(data.currentPlayerHuman()) {
					sayOutput("Which combination would you like to turn in?", OutputFormat.QUESTION);
					for(int i=0;i<possible_triples.length;i++)
						sayOutput((i+1) + ": " + deck.getCardType(possible_triples[i][0]) + " " + deck.getCardType(possible_triples[i][1]) + " " + deck.getCardType(possible_triples[i][2]), OutputFormat.TABBED);
					choice = players[data.getCurrentPlayerID()].askInt(1,possible_triples.length);
					choice--;
				} else {
					((Bot)players[data.getCurrentPlayerID()]).chooseCardSet(possible_triples);
					choice = players[data.getCurrentPlayerID()].askInt(0, possible_triples.length-1);
				}
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[choice][0]);
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[choice][1]);
				players[data.getCurrentPlayerID()].decrementCardType(possible_triples[choice][2]);
				deck.addCards(possible_triples[choice]);
				armies += armies_from_next_set;
			}
			if(data.currentPlayerHuman())
				sayOutput("You get to place an additional " + armies_from_next_set + " armies.");
			else
				sayOutput(data.getPlayerName() + " gets to place an additional " + armies_from_next_set + " armies.");
			advanceCardArmies();
		} catch(Bot.RiskBotException e) {
			BadRobot(turn_player_id, "While turning in a set of cards:", e);
		}
		return armies;
	}

	/*
	 * Fortifies the country at COUNTRIES index country_to_foritfy, and adds num_armies_added
	 * @return A boolean indicating success (different player ownership than turn_player_id will yield false)
	 */
	private boolean fortifyCountry(int country_to_fortify, int num_armies_added) {
		if(COUNTRIES[country_to_fortify].getPlayer() != data.getCurrentPlayerID())
			return false;
		if(!data.currentPlayerHuman())
			sayOutput(data.getPlayerName() + " has placed " + num_armies_added + " armies on " + COUNTRIES[country_to_fortify].getName() + ".");
		COUNTRIES[country_to_fortify].setArmies(COUNTRIES[country_to_fortify].getArmies() + num_armies_added);
		return true;
	}

	/*
	 * Checks if player_id has been eliminated from the game.
	 * If so, sets still_in[player_id] as false to indicate they are out.
	 */
	private boolean playerEliminated(int player_id) {
		for(int i=0;i<data.data.NUM_COUNTRIES;i++) {
			if(data.getOccupier(i) == player_id) return false;
		}
		data.setPlayerStillIn(player_id, false);
		//game_results.add(new Integer(players[player_id].getId()));
		game_results.add(new Integer(player_id));
		return true;
	}

	/*
	 * If a RiskBot gives an incorrect input or exceeds the time limit, this method is called to print an error message and quit.
	 * Player_id is the bot that messed up. Scope is some message about what part of the game/turn it occured in.
	 */
	public void BadRobot(int player_id, String scope, Exception e) {
		sayError("The RiskBot " + data.data.getPlayerName(player_id) + " messed up big time, and the game could not go on.", true);
		sayOutput(scope, true);
		sayOutput(e.getMessage(), true);
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
		for(int i=0; i < data.data.NUM_COUNTRIES; i++) {
			if(data.getOccupier(i) == data.getCurrentPlayerID())
				territories_held++;
		}
		return Math.max(territories_held / 3, 3);
	}

	/*
	 * This method returns how many armies the player who's turn it is gets to place
	 * that comes from having full continents. It iterates COUNTRIES looking for a
	 * territory of each continent that is NOT owned by the current player.
	 * @return int representing the num of armies
	 */
	private int armiesFromContinents() {
		boolean continents_won[] = new boolean[data.NUM_CONTINENTS];
		for(int i=0; i < continents_won.length; i++) continents_won[i] = true;
		for(int country_id=0; country_id < data.data.NUM_COUNTRIES; country_id++) {
			if(data.getOccupier(country_id) != data.data.getCurrentPlayerID())
				continents_won[COUNTRIES[i].getCont()] = false;
		}
		int bonus_armies = 0;
		for(int i=0;i<continents_won.length;i++) {
			if(continents_won[i]) {
				bonus_armies += CONTINENT_BONUSES[i];
				sayOutput("+ " + CONTINENT_BONUSES[i] + " armies for owning all of " + CONTINENT_NAMES[i] + ".", OutputFormat.TABBED);
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
		if(players[data.getCurrentPlayerID()].getNumCards() >= 5) {
			if(data.currentPlayerHuman())
				sayOutput("Since you have " + players[data.getCurrentPlayerID()].getNumCards() + " cards, you must turn in a set for armies.");
			else
				sayOutput("Since " + data.getPlayerName() + " has " + players[data.getCurrentPlayerID()].getNumCards() + " cards, he must turn in a set for armies.");
			armies += turnInSet(false);
		}

		int possible_triples[][] = deck.possibleCardTriples(players[data.getCurrentPlayerID()].getCards());
		if(possible_triples.length > 0) {
			armies += turnInSet(true);
		}
		if(data.currentPlayerHuman())
			sayOutput(cardReport());
		return armies;
	}

	/*
	 * Generates a string describing the current player's hand.
	 * Example: "Your hand consists of 2 infantry, 1 cannon, and 1 wildcard."
	 */
	private String cardReport() {
		String card_report = "";
		int card_variety = 0;
		int last_type = -1;
		for(int i=0;i<4;i++)
			if(players[data.getCurrentPlayerID()].getNumCardType(i) > 0) { card_variety++; last_type = i; }
		if(players[data.getCurrentPlayerID()].getNumCards() == 0)
			card_report += "You do not have any cards in your hand.";
		else if(card_variety == 1) {
			card_report += "Your hand consists of: " + players[data.getCurrentPlayerID()].getNumCardType(last_type) + " " + deck.getCardType(last_type) + ".";
		} else {
			card_report += "Your hand consists of: ";
			for(int i=0;i<4;i++) {
				if(players[data.getCurrentPlayerID()].getNumCardType(i) > 0) {
					if(i != last_type) {
						card_report += players[data.getCurrentPlayerID()].getNumCardType(i) + " " + deck.getCardType(i) + ", ";
					} else card_report += "and " + players[data.getCurrentPlayerID()].getNumCardType(i) + " " + deck.getCardType(i) + ".";
				}
			}
		}
		return card_report;
	}

	/*
	 * Step 3 of a player's turn. Lets the player move armies from 1 country
	 * to 1 neighboring country. This move is optional.
	 */
	private void fortifyPosition() {
		int move_from = 0, move_to = 0, army_change = 0;
		try {

			if(data.currentPlayerHuman()) {
				sayOutput("You may now fortify your position.");
				sayOutput("From which territory would you like to move armies? To skip fortification, enter 0.", OutputFormat.QUESTION);
				while(true) {	// Loop asking for the country number that they'd like to move armies from
					move_from = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES);
					if(move_from == 0) return;
					move_from--; // The number entered is 1-data.NUM_COUNTRIES, but we want 0-(data.NUM_COUNTRIES-1)
					if(COUNTRIES[move_from].getPlayer() != data.getCurrentPlayerID()) {
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
				((Bot)players[data.getCurrentPlayerID()]).fortifyPosition();
				move_from = players[data.getCurrentPlayerID()].askInt();
				if(move_from < 0) return;
				if(move_from >= data.NUM_COUNTRIES)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't exist.");
				if(COUNTRIES[move_from].getPlayer() != data.getCurrentPlayerID())
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't belong to them.");
				if(COUNTRIES[move_from].getArmies() <= 1)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't have more than 1.");
				move_to = players[data.getCurrentPlayerID()].askInt(0, data.NUM_COUNTRIES-1);
				if(COUNTRIES[move_to].getPlayer() != data.getCurrentPlayerID())
					throw new Bot.RiskBotException("Tried to move armies to a territory that doesn't belong to them.");
				army_change = players[data.getCurrentPlayerID()].askInt(1, COUNTRIES[move_from].getArmies()-1);
			}


			int adj[] = world.getAdjacencies(move_from);	// Get territory adjacency list from World class
			if(adj.length == 0) {
				if(!data.currentPlayerHuman())
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no adjacencies.");
				Risk.sayError("According to the map file, " + COUNTRIES[move_from] + " doesn't have any adjacencies.");
				fortifyPosition();
			}
			ArrayList<Integer> domestic_adjacencies = new ArrayList<Integer>();
			// We are only interested in those surrounding territories that belong to the player
			for(int i=0 ; i<adj.length; i++) {
				if(COUNTRIES[adj[i]].getPlayer() == data.getCurrentPlayerID())
					domestic_adjacencies.add(new Integer(adj[i]));
			}
			if(domestic_adjacencies.size() == 0) {
				if(!data.currentPlayerHuman())
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no friendly adjacencies.");
				Risk.sayError("No friendly adjacencies found for " + COUNTRIES[move_from].getName() + ".");
				fortifyPosition();
			}
			if(data.currentPlayerHuman()) {
				if(domestic_adjacencies.size() == 1) {
					sayOutput(COUNTRIES[domestic_adjacencies.get(0)].getName() + " is the only friendly territory adjacent to " + COUNTRIES[move_from].getName() + ".");
					move_to = domestic_adjacencies.get(0);
				} else {
					sayOutput("Which territory would you like to foritfy?", OutputFormat.QUESTION);
					for(int i=1; i<=domestic_adjacencies.size(); i++) {
						sayOutput(i+": " + COUNTRIES[domestic_adjacencies.get(i-1)].getName(), OutputFormat.TABBED);
					}
					int choice = ((Human)players[data.getCurrentPlayerID()]).askInt(1,domestic_adjacencies.size());
					move_to = domestic_adjacencies.get(choice-1);
				}
			} else {
				if(!domestic_adjacencies.contains(move_to))
					throw new Bot.RiskBotException("Tried to move armies from " + COUNTRIES[move_from].getName() + " to " + COUNTRIES[move_to].getName() + ", but they don't connect.");
			}
		} catch( Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "During the fortification phase:", e);
		}
		if(data.currentPlayerHuman()) {
			sayOutput("How many armies would you like to move into " + COUNTRIES[move_to].getName() + "? Max " + (COUNTRIES[move_from].getArmies()-1), OutputFormat.QUESTION);
			army_change = ((Human)players[data.getCurrentPlayerID()]).askInt(1, COUNTRIES[move_from].getArmies()-1);
		} else {
			sayOutput(data.getPlayerName() + " is fortifying " + COUNTRIES[move_to].getName() + " with " + army_change + " armies from " + COUNTRIES[move_from].getName() + ".");
		}
		COUNTRIES[move_from].setArmies(COUNTRIES[move_from].getArmies() - army_change);
		COUNTRIES[move_to].setArmies(COUNTRIES[move_to].getArmies() + army_change);
		refreshGraphics();
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
			COUNTRIES[to_claim-1].setPlayer(data.getCurrentPlayerID());
		}
		return true;
	}

	/* Advances the turns by one player. Edits the turn_player_id variable
	 * Called to advance turn_player_id to the next int in a looped sequence of PLAYER_NAMES indices
	 */
	private void advanceTurn() {
		if(turn_player_id > data.NUM_PLAYERS - 1) {
			turn_player_id = 0;
			sayOutput("First player not randomly chosen. " + data.getPlayerName() + " goes first.");
		}
		if(turn_player_id == data.NUM_PLAYERS - 1)
			turn_player_id = 0;
		else turn_player_id++;
		if(!players[data.getCurrentPlayerID()].getStillIn()) advanceTurn();	// if the player just advanced to is not in, do it again
	}

	// advances armies_from_next_set according to how much the army amount should go up
	// Follows 4->6->8->10->12->15->20->25 etc..
	private void advanceCardArmies() {
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
		for(int i=0;i<data.NUM_PLAYERS;i++) {
			if(data.getPlayerStillIn(i)) {
				if(possible_winner == -1) possible_winner = i;
				else return false;
			}
		}
		winner = possible_winner;
		return true;
	}

	// Retrieves the color of a given player id. Otherwise, returns a gray
	public Color getPlayerColor(int player_id) {
		if(player_id >= 0 && player_id < data.NUM_PLAYERS)
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

	// Returns the winner of the game
	public int getWinner() {
		return winner;
	}

	/*public Country[] getCountries() {
		return COUNTRIES;
	}*/
	
	public int[] getContinentInfo() {
		return CONTINENT_BONUSES;
	}

	// Generates a file name/path for writing to as a game log
	private void setLogFilePath() {
		String logp = Risk.GAME_LOG_PATH; // path of the logs directory
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		String dateStr = sdf.format(cal.getTime());
		logp += dateStr;	// Add the current date to the file name
		int unique = 1;	// To ensure it's unique, keep adding a number until it is
		while(true) {
			File f = new File(logp + "-" + unique + ".html");
			if(!f.exists())
				break;
			unique++;
		}
		logp += "-" + unique + ".html";
		log_path = logp;
	}

	// Resets the players (clears cards, makes them all "still in")
	public void resetPlayers() {
		for(int i=0;i<players.length;i++)
			players[i].reset();
	}

	// Returns an ArrayList<Interger> of player ID's from 1st place to last place
	public ArrayList<Integer> getResults() {
		// game_results adds player ID's as they are eliminated from the game.
		// This returns the top finishers in order from 1st-last, requiring a reversal of order.
		ArrayList<Integer> results_copy = new ArrayList<Integer>(game_results);
		Collections.reverse(results_copy);
		return results_copy;
	}
	
	// Once the game is complete, this returns how long it took in nanoseconds.
	// If the game is not complete, this returns 0.
	public long getElapsedTime() {
		return elapsed_time;
	}

	// Called by the Risk class upon completion of the game to close loose ends.
	// If close_board is true, the game board is closed. Otherwise it is left open.
	public void close(boolean close_board) {
		// Finish writing game log, close the BufferedWriter
		if(save_game_log) {
			try {
				log_writer.write("</body></html>");
				log_writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Close the game board
		if(watch && close_board)
			board.setVisible(false);
	}
}
