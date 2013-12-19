package riskarena;
/*
 * The Game class is the game engine for RiskArena. It maintains information
 * about the game, and provides the procedure involved in running a player's turn
 * 
 * Evan Radkoff
 */

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

import riskarena.graphics.GameBoard;

public class Game {
	private boolean watch;	// Becomes true if the game is being watched (otherwise it is simulated without graphics)
	private GameBoard board; // The Graphics object that draws everything
	private GameData data;
	private Deck deck; // Deck of cards
	
	private int player_id_that_goes_first; // The index of PLAYER_NAMES that goes first. Randomly set in game initialization
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
		//rand = new Random(12);

		// initialize deck
		deck = new Deck(rand);

		setPlayerThatGoesFirst();
	}

	public void init() {
		if(save_game_log) {		// If logs are being kept of this game
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

		while(!data.over()) {	// over returns true when the game is done
			pause();
			sayOutput("=======================================");
			sayOutput("Beginning " + data.getPlayerName() + "'s turn.");
			data.incrementTurn();
			data.notifyPlayerOfTurn();	// Informs bot players that their turn has started, allowing for optional initialization
			fortifyArmies();  		// Step 1 of a player's turn
			attackCountries();		// Step 2 of a player's turn
			if(!data.over()) {
				fortifyPosition();		// Step 3 of a player's turn
				advanceTurn();
				data.notifyPlayerOfTurnEnd();
			}
		}
		int winner = data.getWinner(); // get the winner from the game engine
		if(data.getPlayer(winner).getType() == Player.BOT)
			((Bot)data.getPlayer(winner)).endGame(1);		// First place!
		sayOutput("Congratulations " + data.getPlayer(winner).getName() + ", you win " + Risk.PROJECT_NAME + "!");
		game_results.add(new Integer(winner));
		elapsed_time = System.nanoTime() - start_time;
	}
	
	private void pause() {
		while(data.getPause()) {
		
		}
	}

	/*
	 * This method creates and initializes the GameBoard object
	 */
	private void initializeGraphics() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
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
			@Override
			public void run() {
				board.refresh();
			}
		});
	}

	/* Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
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
				@Override
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
			if(data.getPlayer(i).getName().equals("letmecheat"))	// Cheat
				pile[i] += 30;
		}
		// claiming:
		try {
			for(int i=0; i < data.NUM_COUNTRIES; i++) {
				boolean human = data.currentPlayerHuman();
				Player curr_player = data.getCurrentPlayer();
				int claimed;
				if(human) {
					sayOutput(data.getPlayerName() + ": Enter the number of the territory you would like to claim.", OutputFormat.QUESTION);
					claimed = curr_player.askInt(1,data.NUM_COUNTRIES);
				} else {
					((Bot)curr_player).claimTerritory();	// Signal to the bot that they need to make a decision
					claimed = curr_player.askInt(0,data.NUM_COUNTRIES-1);
				}
				if(!human) claimed++;
				while(!claimCountry(claimed)) {		// If claiming a country fails, it's because it's already occupied
					if(!human)
						throw new Bot.RiskBotException("Tried to claim a territory that was already claimed.");

					Risk.sayError("Territory already taken. Choose another.");
					claimed = curr_player.askInt(1,data.NUM_COUNTRIES);
				}
				if(!human)
					sayOutput(data.getPlayerName() + " has claimed " + data.getCountry(claimed-1).getName() + ".");
				refreshGraphics();
				pile[data.getCurrentPlayerID()]--;
				advanceTurn();
			}
			// fortifying:
			while(true) {
				boolean human = data.currentPlayerHuman();
				Player curr_player = data.getCurrentPlayer();
				int to_fortify;
				if(human) {
					sayOutput(data.getPlayerName() + ": Enter the number of the territory you would like to fortify.", OutputFormat.QUESTION);
					to_fortify = curr_player.askInt(1, data.NUM_COUNTRIES);
				} else {
					((Bot)curr_player).fortifyTerritory(pile[data.getCurrentPlayerID()]);
					to_fortify = curr_player.askInt(0, data.NUM_COUNTRIES-1);
				}
				if(human)
					to_fortify--;
				while(data.getCountry(to_fortify).getPlayer() != data.getCurrentPlayerID()) {
					if(!human)
						throw new Bot.RiskBotException("Tried to fortify a territory that wasn't his.");
					Risk.sayError("Not your territory to fortify. Enter another.");
					to_fortify = curr_player.askInt(1, data.NUM_COUNTRIES);
					to_fortify--;
				}
				if(human)
					sayOutput("How many armies would you like to add to " + data.getCountry(to_fortify).getName() + "? " + pile[data.getCurrentPlayerID()] + " armies left in your pile.", OutputFormat.QUESTION);
				int armies_added = curr_player.askInt(1, pile[data.getCurrentPlayerID()]);

				fortifyCountry(to_fortify, armies_added);
				pile[data.getCurrentPlayerID()] -= armies_added;
				refreshGraphics();

				boolean done_fortifying = false;
				for(int i=0;;i++) {
					if(i > data.NUM_PLAYERS) {
						done_fortifying = true;
						break;
					}
					advanceTurn();
					if( pile[data.getCurrentPlayerID()] > 0) break; 
				}
				if(done_fortifying) break;
			}
			refreshGraphics();
			data.setCurrentPlayerID(player_id_that_goes_first); // after initial fortifying, the player to go first
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
				boolean human = data.currentPlayerHuman();
				Player curr_player = data.getCurrentPlayer();
				int country_improving;
				if(human) {
					sayOutput("Territory number to fortify?", OutputFormat.QUESTION);
					country_improving = curr_player.askInt(1, data.NUM_COUNTRIES);
				} else {
					((Bot)curr_player).fortifyTerritory(armies_to_place);
					country_improving = curr_player.askInt(0, data.NUM_COUNTRIES-1);
				}
				if(human)
					country_improving--;
				while(data.getCountry(country_improving).getPlayer() != curr_player.getId()) {
					if(!human)
						throw new Bot.RiskBotException("Tried to place armies on a territory that wasn't his.");
					Risk.sayError("Not your territory, enter another.");
					country_improving = curr_player.askInt(1, data.NUM_COUNTRIES);
					country_improving--;
				}
				int num_to_add;
				if(armies_to_place == 1 && human)
					num_to_add = 1;
				else {
					if(human)
						sayOutput("How many armies?", OutputFormat.QUESTION);
					num_to_add = curr_player.askInt(1,armies_to_place);
				}
				fortifyCountry(country_improving, num_to_add);
				armies_to_place -= num_to_add;
				refreshGraphics();

				if(human) {
					String to_out = data.getCountry(country_improving).getName() + " fortified with " + num_to_add + " armies.";
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
		boolean human = data.currentPlayerHuman();
		Player curr_player = data.getCurrentPlayer();
		try {
			while(true) {	// main attack loop
				if(human)
					sayOutput(data.getPlayerName() + ", which of your territories would you like to attack with? When finished attacking, enter 0.", OutputFormat.QUESTION);
				else
					((Bot)curr_player).launchAttack();
				int attacking_from; // the country id of the attacking country
				int attacking_to; // the id of the country being attacked
				Country from;

				boolean done_attacking = false;
				while(true) {	// Loop asking for the country number that they'd like to attack from
					if(human) {
						attacking_from = curr_player.askInt(0, data.NUM_COUNTRIES);
						if(attacking_from == 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						attacking_from--; // The number entered is 1-data.NUM_COUNTRIES, but we want 0-(data.NUM_COUNTRIES-1)
					} else {
						attacking_from = curr_player.askInt();
						if(attacking_from < 0) {
							done_attacking = true; // zero was entered, leave the attack loop
							break;
						}
						if(attacking_from >= data.NUM_COUNTRIES)
							throw new Bot.RiskBotException("Tried to attack from a country that doesn't exist.");
					}
					from = data.getCountry(attacking_from);
					if(from.getPlayer() != data.getCurrentPlayerID()) {
						if(human)
							Risk.sayError("Not your territory, enter another.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + from.getName() + ", but does not own it.");
						continue;
					}
					if(from.getArmies() <= 1) {
						if(human)
							Risk.sayError("At least 2 armies are required to attack.");
						else
							throw new Bot.RiskBotException("Attempted to attack from " + from.getName() + ", but there are not enough armies in it to do so.");
						continue;
					}
					break;
				}
				if(done_attacking) break;
				from = data.getCountry(attacking_from);
				int adj[] = data.getAdjacencies(attacking_from);	// Get territory adjacency list from World class
				if(adj.length == 0) {
					Risk.sayError("According to the map file, " + from + " doesn't have any adjacencies.");
					continue;
				}
				ArrayList<Integer> foreign_adjacencies = new ArrayList<Integer>();
				// We are only interested in those surrounding territories that are of foreign ownership
				for(int i=0 ; i<adj.length; i++) {
					if(data.getCountry(adj[i]).getPlayer() != data.getCurrentPlayerID())
						foreign_adjacencies.add(new Integer(adj[i]));
				}
				if(foreign_adjacencies.size() == 0) {
					if(human)
						Risk.sayError("No foreign adjacencies found for " + from.getName() + ".");
					else
						throw new Bot.RiskBotException("Tried to attack from " + from.getName() + ", which has no foreign adjacencies.");
					continue;
				}
				if(human) {
					if(foreign_adjacencies.size() == 1) {
						sayOutput(data.getCountry(foreign_adjacencies.get(0)).getName() + " is the only foreign territory adjacent to " + from.getName() + ". Launching attack.");
						attacking_to = foreign_adjacencies.get(0);
					} else {
						sayOutput("Which territory would you like to attack from " + from.getName() + "?", OutputFormat.QUESTION);
						for(int i=1; i<=foreign_adjacencies.size(); i++) {
							sayOutput(i+": " + data.getCountry(foreign_adjacencies.get(i-1)).getName(), OutputFormat.TABBED);
						}
						int choice = curr_player.askInt(1,foreign_adjacencies.size());
						attacking_to = foreign_adjacencies.get(choice-1);
						sayOutput("Launching attack on " + data.getCountry(attacking_to).getName() + ".");
					}
				} else {
					attacking_to = curr_player.askInt(0, data.NUM_COUNTRIES-1);
					if(!foreign_adjacencies.contains(new Integer(attacking_to)))
						throw new Bot.RiskBotException("Tried to attack from " + from.getName() + " to " + data.getCountry(attacking_to).getName() + ", which is not a valid target.");
					sayOutput(data.getPlayerName() + " is launching an attack from " + from.getName() + " to " + data.getCountry(attacking_to).getName() + ".");
				}
				if(attack(attacking_from, attacking_to)) {		// attack() plays out the attack
					gained_territory = true;
					if(data.over()) return;
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
				curr_player.incrementCardType(drawn); // give card to player for winning territory
				if(human) {
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
					sayOutput("As " + data.getPlayerName() + " has gained territory this turn, they get to draw a card. They are now holding " + curr_player.getNumCards() + ".");
				}
			}
		}
	}

	/*
	 * Simulates an attack from COUNTRIES index 'attacker' to COUNTRIES index 'defender'
	 * @return boolean, true if the defender's territory was captured
	 */
	private boolean attack(int attacker, int defender) {
		boolean human = data.currentPlayerHuman();
		Country from = data.getCountry(attacker), to = data.getCountry(defender);
		Player curr_player = data.getCurrentPlayer(), being_attacked = data.getPlayer(to.getPlayer());
		try {
			while(true) {
				refreshGraphics();
				int armies_attacking, armies_defending;
				if(human) {
					if(from.getArmies() == 1) {
						sayOutput("No more armies to attack with.", OutputFormat.TABBED);
						return false;
					}
					if(from.getArmies() == 2) {
						sayOutput("Only 2 armies are left in " + from.getName() + ", continue the attack with one army? (Y)es or (n)o", OutputFormat.TABBED_QUESTION);
						while(true) {
							String answer = curr_player.askLine();
							if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) return false;
							else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
							else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
						}
						armies_attacking = 1;
					} else if(from.getArmies() == 3) {
						sayOutput("How many armies do you send to battle - 1 or 2? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = curr_player.askInt(0, 2);
					} else {
						sayOutput("How many armies do you send to battle - 1, 2, or 3? 0 to cancel the attack.", OutputFormat.TABBED_QUESTION);
						armies_attacking = curr_player.askInt(0, 3);
					}
					if(armies_attacking == 0) {
						return false;
					}
				} else { // Bot
					armies_attacking = curr_player.askInt(1, 3);
				}
				if(!human) {
					sayOutput(from.getName() + " is sending " + armies_attacking + " armies to battle...", OutputFormat.TABBED);
				}
				if(to.getArmies() == 1)
					armies_defending = 1;
				else
					armies_defending = 2;

				if(human)
					sayOutput("Rolling dice...", OutputFormat.TABBED);
				Dice dice = new Dice(rand, armies_attacking, armies_defending);
				from.setArmies(from.getArmies() + dice.attackerArmyChange);
				to.setArmies(to.getArmies() + dice.defenderArmyChange);
				refreshGraphics();

				switch(dice.attackerArmyChange) {
				case 0:
					if(dice.defenderArmyChange == -1)
						sayOutput(to.getName() + " (" + being_attacked.getName() + ") loses 1 army.", OutputFormat.TABBED);
					else sayOutput(to.getName() + " (" + being_attacked.getName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				case -1:
					if(dice.defenderArmyChange == -1)
						sayOutput("Each player loses 1 army.", OutputFormat.TABBED);
					else sayOutput(from.getName() + " (" + data.getPlayerName() + ") loses 1 army.", OutputFormat.TABBED);
					break;
				case -2:
					sayOutput(from.getName() + " (" + data.getPlayerName() + ") loses 2 armies.", OutputFormat.TABBED);
					break;
				}

				// Territory captured
				if(to.getArmies() == 0) {
					if(human)
						sayOutput("Congratulations " + from.getName() + ", you captured " + to.getName() + "!", OutputFormat.TABBED);
					else
						sayOutput(from.getName() + " has captured " + to.getName() + " (" + being_attacked.getName() + ")", OutputFormat.TABBED);
					refreshGraphics();
					to.setPlayer(data.getCurrentPlayerID());	// transfer ownership to the attacker
					if(playerEliminated(being_attacked.getId())) {
						sayOutput("*** " + from.getName() + " has eliminated " + being_attacked.getName() + " ***", OutputFormat.TABBED);
						if(data.over()) return true;	// if the game is over
						if(being_attacked.getNumCards() > 0) { // turn_player_id gets some free cards from defender
							sayOutput("* " + from.getName() + " gets " + being_attacked.getNumCards() + " free cards. *", OutputFormat.TABBED);
							curr_player.increaseCardType(0, being_attacked.getNumCardType(0));
							curr_player.increaseCardType(1, being_attacked.getNumCardType(1));
							curr_player.increaseCardType(2, being_attacked.getNumCardType(2));
							curr_player.increaseCardType(3, being_attacked.getNumCardType(3));
							being_attacked.clearCards();
							if(curr_player.getNumCards() >= 6) {
								int additional_armies = 0;
								if(human)
									sayOutput("Since you have more than 5 cards, you must immediately turn in sets for armies.");
								else
									sayOutput("Since he has more than 5, he is required to turn in sets for armies.");
								additional_armies += turnInSet(false);
								while(curr_player.getNumCards() > 4) {
									if(human)
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
					if(from.getArmies() - armies_attacking > 1) {
						if(human)
							sayOutput("How many armies would you like to move in for occupation? Min " + armies_attacking + ", Max " + (from.getArmies()-1), OutputFormat.TABBED_QUESTION);
						else
							((Bot)curr_player).fortifyAfterVictory(attacker, defender, armies_attacking, (from.getArmies()-1));
						armies_to_move = curr_player.askInt(armies_attacking, (from.getArmies()-1));
					} else armies_to_move = armies_attacking;
					if(!human)
						sayOutput(curr_player.getName() + " moves " + armies_to_move + " armies into " + to.getName() + " for occupation.");
					from.setArmies( from.getArmies() - armies_to_move );
					to.setArmies( to.getArmies() + armies_to_move );

					refreshGraphics();
					return true;
				}
				if(!human)
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
		int possible_triples[][] = deck.possibleCardTriples(data.getCurrentPlayer().getCards());
		boolean human = data.currentPlayerHuman();
		Player curr_player = data.getCurrentPlayer();

		try {
			if(optional) {
				if(human) {
					sayOutput("You have enough cards for a set. Would you like to turn it in for " + data.getArmiesFromNextSet() + " additional armies? (Y)es or (n)o.", OutputFormat.QUESTION);
					while(true) {
						String answer = curr_player.askLine();
						if(answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) { 
							sayOutput(cardReport());
							return armies;
						}
						else if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) break;
						else Risk.sayError("Invalid input. Enter (y)es or (n)o.");
					}
				} else {	// Bot
					((Bot)curr_player).chooseToTurnInSet();
					int choice = curr_player.askInt();
					if(choice != 1)
						return armies;
					sayOutput(data.getPlayerName() + " is turning in a set of cards for armies.");
				}
			}

			if(possible_triples.length == 1) {
				curr_player.decrementCardType(possible_triples[0][0]);
				curr_player.decrementCardType(possible_triples[0][1]);
				curr_player.decrementCardType(possible_triples[0][2]);
				deck.addCards(possible_triples[0]);
				armies += data.getArmiesFromNextSet();
			} else {		// If there are more than one possible sets to turn in
				int choice;
				if(human) {
					sayOutput("Which combination would you like to turn in?", OutputFormat.QUESTION);
					for(int i=0;i<possible_triples.length;i++)
						sayOutput((i+1) + ": " + deck.getCardType(possible_triples[i][0]) + " " + deck.getCardType(possible_triples[i][1]) + " " + deck.getCardType(possible_triples[i][2]), OutputFormat.TABBED);
					choice = curr_player.askInt(1,possible_triples.length);
					choice--;
				} else {
					((Bot)curr_player).chooseCardSet(possible_triples);
					choice = curr_player.askInt(0, possible_triples.length-1);
				}
				curr_player.decrementCardType(possible_triples[choice][0]);
				curr_player.decrementCardType(possible_triples[choice][1]);
				curr_player.decrementCardType(possible_triples[choice][2]);
				deck.addCards(possible_triples[choice]);
				armies += data.getArmiesFromNextSet();
			}
			if(human)
				sayOutput("You get to place an additional " + data.getArmiesFromNextSet() + " armies.");
			else
				sayOutput(data.getPlayerName() + " gets to place an additional " + data.getArmiesFromNextSet() + " armies.");
			data.advanceCardArmies();
		} catch(Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "While turning in a set of cards:", e);
		}
		return armies;
	}

	/*
	 * Fortifies the country at COUNTRIES index country_to_foritfy, and adds num_armies_added
	 * @return A boolean indicating success (different player ownership than turn_player_id will yield false)
	 */
	private boolean fortifyCountry(int country_to_fortify, int num_armies_added) {
		if(data.getCountry(country_to_fortify).getPlayer() != data.getCurrentPlayerID())
			return false;
		if(!data.currentPlayerHuman())
			sayOutput(data.getPlayerName() + " has placed " + num_armies_added + " armies on " + data.getCountry(country_to_fortify).getName() + ".");
		data.getCountry(country_to_fortify).setArmies(data.getCountry(country_to_fortify).getArmies() + num_armies_added);
		return true;
	}

	/*
	 * Checks if player_id has been eliminated from the game.
	 * If so, sets still_in[player_id] as false to indicate they are out.
	 */
	private boolean playerEliminated(int player_id) {
		for(int i=0;i<data.NUM_COUNTRIES;i++) {
			if(data.getCountry(i).getPlayer() == player_id) {
				return false;
			}
		}
		data.getPlayer(player_id).setStillIn(false);
		if(data.getPlayer(player_id).getType() == Player.BOT)
			((Bot)data.getPlayer(player_id)).endGame(data.NUM_PLAYERS - game_results.size());
		game_results.add(new Integer(player_id));
		return true;
	}

	/*
	 * If a RiskBot gives an incorrect input or exceeds the time limit, this method is called to print an error message and quit.
	 * Player_id is the bot that messed up. Scope is some message about what part of the game/turn it occured in.
	 */
	public void BadRobot(int player_id, String scope, Exception e) {
		sayError("The RiskBot " + data.getPlayer(player_id).getName() + " messed up big time, and the game could not go on.", true);
		sayOutput(scope, true);
		sayOutput(e.getMessage(), true);
		while(true) {
			
		}
		//System.exit(0);
	}

	/*
	 * This method calculates how many armies the player who's turn
	 * it is gets to place that come from having a certain number of territories
	 * @return int representing the num of armies
	 */
	private int armiesFromTerritories() {
		int territories_held = 0;
		for(int i=0; i < data.NUM_COUNTRIES; i++) {
			if(data.getCountry(i).getPlayer() == data.getCurrentPlayerID())
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
		for(int country_id=0; country_id < data.NUM_COUNTRIES; country_id++) {
			if(data.getCountry(country_id).getPlayer() != data.getCurrentPlayerID())
				continents_won[data.getCountry(country_id).getCont()] = false;
		}
		int bonus_armies = 0;
		for(int i=0;i<continents_won.length;i++) {
			if(continents_won[i]) {
				bonus_armies += data.getContinentBonus(i);
				sayOutput("+ " + data.getContinentBonus(i) + " armies for owning all of " + data.getContinentName(i) + ".", OutputFormat.TABBED);
			}
		}
		return bonus_armies;
	}

	/*
	 * Checks if turn_player_id can turn in sets of cards for armies
	 * @return int, the number of armies gained from cards (default 0)
	 */
	private int armiesFromCards() {
		boolean human = data.currentPlayerHuman();
		Player curr_player = data.getCurrentPlayer();
		int num_cards = curr_player.getNumCards();
		int armies = 0;
		if(num_cards >= 5) {
			if(human)
				sayOutput("Since you have " + num_cards + " cards, you must turn in a set for armies.");
			else
				sayOutput("Since " + data.getPlayerName() + " has " + num_cards + " cards, he must turn in a set for armies.");
			armies += turnInSet(false);
		}

		int possible_triples[][] = deck.possibleCardTriples(curr_player.getCards());
		if(possible_triples.length > 0) {
			armies += turnInSet(true);
		}
		if(human)
			sayOutput(cardReport());
		return armies;
	}

	/*
	 * Generates a string describing the current player's hand.
	 * Example: "Your hand consists of 2 infantry, 1 cannon, and 1 wildcard."
	 */
	private String cardReport() {
		Player curr_player = data.getCurrentPlayer();
		String card_report = "";
		int card_variety = 0;
		int last_type = -1;
		for(int i=0;i<4;i++)
			if(curr_player.getNumCardType(i) > 0) { card_variety++; last_type = i; }
		if(curr_player.getNumCards() == 0)
			card_report += "You do not have any cards in your hand.";
		else if(card_variety == 1) {
			card_report += "Your hand consists of: " + curr_player.getNumCardType(last_type) + " " + deck.getCardType(last_type) + ".";
		} else {
			card_report += "Your hand consists of: ";
			for(int i=0;i<4;i++) {
				if(curr_player.getNumCardType(i) > 0) {
					if(i != last_type) {
						card_report += curr_player.getNumCardType(i) + " " + deck.getCardType(i) + ", ";
					} else card_report += "and " + curr_player.getNumCardType(i) + " " + deck.getCardType(i) + ".";
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
		boolean human = data.currentPlayerHuman();
		Player curr_player = data.getCurrentPlayer();
		int move_from = 0, move_to = 0, army_change = 0;
		try {

			if(human) {
				sayOutput("You may now fortify your position.");
				sayOutput("From which territory would you like to move armies? To skip fortification, enter 0.", OutputFormat.QUESTION);
				while(true) {	// Loop asking for the country number that they'd like to move armies from
					move_from = curr_player.askInt(0, data.NUM_COUNTRIES);
					if(move_from == 0) return;
					move_from--; // The number entered is 1-data.NUM_COUNTRIES, but we want 0-(data.NUM_COUNTRIES-1)
					Country from = data.getCountry(move_from);
					if(from.getPlayer() != data.getCurrentPlayerID()) {
						Risk.sayError("Not your territory, enter another.");
						continue;
					}
					if(from.getArmies() <= 1) {
						Risk.sayError("A minimum of 1 army must be present in each territory. Enter another.");
						continue;
					}
					break;
				}
			} else { // BOT
				((Bot)curr_player).fortifyPosition();
				move_from = curr_player.askInt();
				if(move_from < 0) return;
				Country from = data.getCountry(move_from);
				if(move_from >= data.NUM_COUNTRIES)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't exist.");
				if(from.getPlayer() != data.getCurrentPlayerID())
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't belong to them.");
				if(from.getArmies() <= 1)
					throw new Bot.RiskBotException("Tried to move armies from a territory that doesn't have more than 1.");
				move_to = curr_player.askInt(0, data.NUM_COUNTRIES-1);
				if(from.getPlayer() != data.getCurrentPlayerID())
					throw new Bot.RiskBotException("Tried to move armies to a territory that doesn't belong to them.");
				army_change = curr_player.askInt(1, from.getArmies()-1);
			}

			Country from = data.getCountry(move_from);
			int adj[] = data.getAdjacencies(move_from);	// Get territory adjacency list from World class
			if(adj.length == 0) {
				if(!human)
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no adjacencies.");
				Risk.sayError("According to the map file, " + from.getName() + " doesn't have any adjacencies.");
				fortifyPosition();
			}
			ArrayList<Integer> domestic_adjacencies = new ArrayList<Integer>();
			// We are only interested in those surrounding territories that belong to the player
			for(int i=0 ; i<adj.length; i++) {
				if(data.getCountry(adj[i]).getPlayer() == data.getCurrentPlayerID())
					domestic_adjacencies.add(new Integer(adj[i]));
			}
			if(domestic_adjacencies.size() == 0) {
				if(!human)
					throw new Bot.RiskBotException("Tried to move armies from a territory that has no friendly adjacencies.");
				Risk.sayError("No friendly adjacencies found for " + from.getName() + ".");
				fortifyPosition();
			}
			if(human) {
				if(domestic_adjacencies.size() == 1) {
					sayOutput(data.getCountry(domestic_adjacencies.get(0)).getName() + " is the only friendly territory adjacent to " + from.getName() + ".");
					move_to = domestic_adjacencies.get(0);
				} else {
					sayOutput("Which territory would you like to foritfy?", OutputFormat.QUESTION);
					for(int i=1; i<=domestic_adjacencies.size(); i++) {
						sayOutput(i+": " + data.getCountry(domestic_adjacencies.get(i-1)).getName(), OutputFormat.TABBED);
					}
					int choice = ((Human)curr_player).askInt(1,domestic_adjacencies.size());
					move_to = domestic_adjacencies.get(choice-1);
				}
			} else {
				if(!domestic_adjacencies.contains(move_to))
					throw new Bot.RiskBotException("Tried to move armies from " + from.getName() + " to " + data.getCountry(move_to).getName() + ", but they don't connect.");
			}
		} catch( Bot.RiskBotException e) {
			BadRobot(data.getCurrentPlayerID(), "During the fortification phase:", e);
		}
		Country from = data.getCountry(move_from);
		if(human) {
			sayOutput("How many armies would you like to move into " + data.getCountry(move_to).getName() + "? Max " + (from.getArmies()-1), OutputFormat.QUESTION);
			army_change = ((Human)curr_player).askInt(1, from.getArmies()-1);
		} else {
			sayOutput(data.getPlayerName() + " is fortifying " + data.getCountry(move_to).getName() + " with " + army_change + " armies from " + from.getName() + ".");
		}
		from.setArmies(from.getArmies() - army_change);
		data.getCountry(move_to).setArmies(data.getCountry(move_to).getArmies() + army_change);
		refreshGraphics();
	}

	/*
	 * claimCountry is used in game setup, when players are claiming territories.
	 * It attempts to claim a territory at COUNTRIES index to_claim-1 for player_id.
	 * @return A boolean indicating success or failure
	 */
	private boolean claimCountry(int to_claim) {
		if(data.getCountry(to_claim-1).getArmies() != 0)	// If the territory has already been claimed by someone
			return false;
		else {
			data.getCountry(to_claim-1).setArmies(1);
			data.getCountry(to_claim-1).setPlayer(data.getCurrentPlayerID());
		}
		return true;
	}

	/* Advances the turns by one player. Edits the turn_player_id variable
	 * Called to advance turn_player_id to the next int in a looped sequence of PLAYER_NAMES indices
	 */
	private void advanceTurn() {
		if(data.getCurrentPlayerID() > data.NUM_PLAYERS - 1) {
			data.setCurrentPlayerID(0);
			sayOutput("First player not randomly chosen. " + data.getPlayerName() + " goes first.");
		}
		if(data.getCurrentPlayerID() == data.NUM_PLAYERS - 1)
			data.setCurrentPlayerID(0);
		else data.setCurrentPlayerID(data.getCurrentPlayerID() + 1);
		if(!data.getCurrentPlayer().getStillIn()) advanceTurn();	// if the player just advanced to is not in, do it again
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
	
	// When a Game object is used for more than one game, this method can be called to "clear" old game data
	public void clearGame() {
		data.resetPlayers();
		
	}
}
