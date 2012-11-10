/*
 * Risk.java starts execution of RiskArena. See READ_ME for more
 * 
 * In charge of output and errorout messages
 * In charge of setting the game up, as of now through the Scanner / System.in
 * Contains the main turn-by-turn execution loop
 * Uses a Game object, the RiskArena game engine
 * 
 * Evan Radkoff
 */

import java.util.ArrayList;
import java.awt.Color;
import java.io.*;
import java.lang.String;

import javax.swing.SwingUtilities;

/*
 * The Risk class, containing main()
 */
public class Risk {

	final static String PROJECT_NAME = "RiskArena";
	final static int MIN_PLAYERS = 2;	// Minimum numbers that can play
	final static int MAX_PLAYERS = 6;	// Maximum numbers that can play
	// ^ If changing MAX_PLAYERS, you may need to set the number of armies each player starts with in Game.placeArmies()
	// 		^ as well as Risk.getPlayerColor()
	final static String MAPS_DIR_NAME = "src/maps/"; // name of the directory containing map files
	final static String FONT_PATH = "src/fonts/AmericanTypewriter.ttc";
	final static String BOT_PATH = "src"; // location of bots
	final static String LOG_PATH = "src/logs/";
	final static String RISKBOT_PREFIX = "RiskBot";	// any file beginning with this is considered by BotSniffer as a potential AI option
	final static boolean output_to_std = true;
	final static boolean input_from_std = false;
	final static long bot_playing_speed = 200; // milliseconds bots wait before sending game decisions

	private static InputListener console_input;
	private static int num_players;	// The number of players in a given game. Set dynamically in main()
	private static Player players[];	// Structure to hold player names when read from input
	private static Game game;	// The instance of the game engine class, Game
	private static Graphics graphics; // The Graphics object that draws everything

	// Hello World!
	public static void main(String[] args) {

		init();
		
		game.placeInitialArmies();	// Game setup, involving players placing initial armies
		while(!game.over()) {	// over returns true when the game is done
			sayOutput("===================================================");
			sayOutput("Beginning " + game.getPlayerName() + "'s turn.");
			game.fortifyArmies();  		// Step 1 of a player's turn
			game.attackCountries();		// Step 2 of a player's turn
			if(!game.over()) {
				game.fortifyPosition();		// Step 3 of a player's turn
				game.advanceTurn();
			}
		}
		int winner = game.getWinner(); // get the winner from the game engine
		sayOutput("Congratulations " + players[winner].getName() + ", you win " + PROJECT_NAME + "!");
	}
	
	// Game initialization tasks
	private static void init() {
		initializeGraphics();
		// Wait for the graphics to be initialized on its own thread.
		while(graphics==null) {
			try {
				Thread.sleep(10);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		console_input = new InputListener();
		graphics.sendInputListener(console_input);
		
		askPlayerInfo();				// ask the player information (human vs bot, name, etc)
		String map_file = askMapFile();					// ask the map file to load from MAPS_DIR_NAME
		game = new Game(players, map_file);	// Create the game engine instance

		sendGameToBots();
		sendGameToGraphics();
		sendHumanInputToGraphics();
	}

	/* Asks the user for the names of those playing.
	 * Adds each name entered to an ArrayList<String>
	 * @return the list of names of the players
	 */
	private static void askPlayerInfo() {
		sayOutput("Welcome to " + PROJECT_NAME + "! How many players are there?", OutputFormat.QUESTION);
		num_players = askInt(MIN_PLAYERS, MAX_PLAYERS);	 // ask the number of players

		ArrayList<String> names = new ArrayList<String>();
		BotSniffer bot_sniffer = new BotSniffer(BOT_PATH);
		ArrayList<String> available_bots = bot_sniffer.getBots();
		int current_bots[];
		if(available_bots.size() == 0) {
			Risk.sayOutput("No available bots detected. This is an all-human game.");
		}
		current_bots = new int[available_bots.size()];
		for(int i=0;i<current_bots.length;i++)
			current_bots[i] = 0;

		players = new Player[num_players];
		for(int i=0; i<num_players; i++) {
			int player_type;
			if(available_bots.size() != 0) {
				sayOutput("Is player " + (i+1) + " a human(1) or bot(2)?", OutputFormat.QUESTION);
				player_type = askInt(1,2);
			} else player_type = 1;

			if(player_type == 1) {			// Human
				sayOutput("Enter the name of player " + (i+1), OutputFormat.QUESTION);
				String some_name = askLine();
				while(some_name.equals("")) {
					sayError("No blank names. Enter another.");
					some_name = askLine();
				}
				while(names.contains(some_name)) {
					sayError("Player name already taken. Enter another.");
					some_name = askLine();
				}
				players[i] = new Human(some_name, getPlayerColor(i), i);
				names.add(some_name);
			} else if(player_type == 2) {	// Bot
				// TODO handle that a human could enter their name as a bot's name (ie if a human said "Skynet 2", how would the 2nd skynet change its name)
				String bot_name;
				int botID = 0;
				if(available_bots.size() != 1) {
					sayOutput("Which RiskBot should player " + (i+1) + " use?", OutputFormat.QUESTION);
					for(int j=0; j<available_bots.size(); j++)
						sayOutput((j+1) + " - " + available_bots.get(j), OutputFormat.TABBED);
					botID = askInt(1, available_bots.size());
					botID--;
				} 
				bot_name = available_bots.get(botID);
				players[i] = new Bot(bot_name, getPlayerColor(i), i);
				if(current_bots[botID] > 0) {
					bot_name += " " + (current_bots[botID]+1);
					players[i].setName(bot_name);
				}
				names.add(bot_name);
				current_bots[botID]++;
				if(available_bots.size() == 1)
					sayOutput("Player " + (i+1) + " is " + bot_name + " (the only available bot type)");
				else
					sayOutput("Player " + (i+1) + " is " + bot_name + ".");
			}
		}
	}

	/*
	 * Once the game object is constructed, it must be sent along to each Bot player
	 */
	private static void sendGameToBots() {
		for(int i=0;i<num_players;i++) {
			if(players[i].getType() == Player.BOT)
				((Bot)players[i]).initializeBot(game);
		}
	}

	/*
	 * This method creates and initializes the Graphics object
	 */
	private static void initializeGraphics() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				graphics = new Graphics();
				graphics.setVisible(true);
			}
		});
	}

	private static void sendGameToGraphics() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				graphics.sendGameInfo(game, game.world.getAdjacencyList());
				graphics.refresh();
			}
		});
	}
	
	private static void sendHumanInputToGraphics() {
		for(int i=0;i<num_players;i++) {
			if(players[i].getType() == Player.HUMAN)
				((Human)players[i]).sendInputToGraphics(graphics);
		}
	}

	/*
	 * Returns the color of a given player id. If more than 6 players are allowed,
	 * this needs to be manually changed.
	 */
	private static Color getPlayerColor(int player_id) {
		switch(player_id) {
		case 0:
			return Color.red;
		case 1:
			return Color.blue;
		case 2:
			return Color.green;
		case 3:
			return Color.magenta;
		case 4:
			return Color.DARK_GRAY;
		case 5:
			return Color.yellow;
		default:
			return Color.gray;
		}
	}

	/* Lists the files in the maps directory that end in .map
	 * Lets the user select a map to use, and returns the path to that file
	 * @return String representing the path to the .map file to use
	 */
	private static String askMapFile() {

		File maps_dir = new File(MAPS_DIR_NAME);
		String[] files = maps_dir.list();
		ArrayList<String> map_candidates = new ArrayList<String>();
		if ( files == null ) {
			sayError("Maps directory does not exist. Please create \"" + MAPS_DIR_NAME + "\"");
			System.exit(1);
		} 
		for(int i=0; i < files.length; i++) {
			if( files[i].length() >= 5 && files[i].endsWith(".map") )
				map_candidates.add(files[i]);
		}
		if(map_candidates.size() == 0) {
			sayError("No map file found. Please create a map file at " + MAPS_DIR_NAME + "somemap.map");
			sayError("For more on map files, see the readme.");
			System.exit(1);
		}
		sayOutput("What map file would you like to use?", OutputFormat.QUESTION);
		for(int i=0; i < map_candidates.size(); i++) {
			sayOutput((i+1) + ": " + map_candidates.get(i), OutputFormat.TABBED);
		}
		return MAPS_DIR_NAME + map_candidates.get( askInt(1,map_candidates.size())-1 );
	}

	/* Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 * tabbed is whether or not it should be prepended with a \t
	 */
	public static void sayOutput(final String toSay, final int output_format_style) {
		if( output_to_std ) {
			if(output_format_style == OutputFormat.TABBED)
				System.out.println("\t" + toSay);
			else if (output_format_style == OutputFormat.ERROR) {
				System.err.println("ERROR: " + toSay);
				System.err.flush();
			} else if(output_format_style != OutputFormat.ANSWER)
				System.out.println(toSay);
		}
		if(graphics != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					graphics.sayOutput(toSay, output_format_style);
				}
			});
		}
	}

	/*
	 * Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 */
	public static void sayOutput(final String toSay) {
		sayOutput(toSay, OutputFormat.NORMAL);
	}

	/* Called by various methods to send something to whatever
	 * error output is being used.
	 * @param The string wishing to be outputted error message.
	 */
	public static void sayError(final String toSay) {
		sayOutput(toSay, OutputFormat.ERROR);
	}

	/*
	 * Ask for an integer. Forces only ints that are greater than 0
	 * @return int greater than zero
	 */
	public static int askInt() {
		console_input.activate();
		return console_input.getInt();
	}

	/* Asks for an int that is between MIN and MAX.
	 * @return int between MIN and MAX, inclusive.
	 */
	public static int askInt(int MIN, int MAX) {
		console_input.activate();
		return console_input.getInt(MIN, MAX);
	}

	// Ask for a line through the "ask" Scanner
	public static String askLine() {
		console_input.activate();
		return console_input.getString();
	}

	// In another thread, refresh the Graphics object
	public static void refreshGraphics() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				graphics.refresh();
			}
		});
	}
}
