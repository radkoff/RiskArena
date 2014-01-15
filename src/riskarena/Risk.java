/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
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

import java.awt.Color;
import java.lang.String;

import javax.swing.SwingUtilities;

import riskarena.graphics.SetUp;
import riskarena.graphics.WarGameReport;
import riskarena.graphics.WarGameSetUp;

/*
 * The Risk class, containing main()
 */
public class Risk {
	
	public final static String PROJECT_NAME = "RiskArena";
	public final static int MIN_PLAYERS = 2;	// Minimum numbers that can play
	public final static int MAX_PLAYERS = 6;	// Maximum numbers that can play
	public final static String MAPS_DIR_NAME = "src/maps/"; // name of the directory containing map files
	public final static String FONT_PATH = "src/fonts/AmericanTypewriter.ttc";
	public final static String BOT_PATH = "src/riskarena/riskbots"; // location of bots
	public final static String GAME_LOG_PATH = "logs/game_reports/";
	public final static String WAR_GAME_LOG_PATH = "logs/war_games/";
	public final static String IMAGES_PATH = "src/images/";
	public final static String LOGO_URL = Risk.IMAGES_PATH + "RiskArena.png";
	public final static String RISKBOT_PREFIX = "RiskBot";	// any file beginning with this is considered by BotSniffer as a potential AI option
	public final static Color UGLY_GREY = new Color(0.4f,0.4f,0.4f); // many dialogs throughout the application use the same ugly grey background
	public final static boolean output_to_std = false;
	public final static boolean input_from_std = false;

	private static Player players[];	// Structure to hold player information
	private static Game game;	// The instance of the game engine class, Game
	private static SetUp setup; // Game set up panel
	private static WarGameSetUp wargamesetup; // Set up panel for all-AI war games

	// Hello World!
	public static void main(String[] args) {
		
		setup = new SetUp();		// Create and display the game set up panel
		players = setup.getPlayers();	// Retrieve player information, as per the set up panel
		String map_file_path = MAPS_DIR_NAME + setup.getMap();	// Retrieve map information
		
		// War Games are when the players are all AI
		if(setup.warGames()) {
			wargamesetup = new WarGameSetUp();		// Create and display the war game set up panel
			final int num_games = wargamesetup.getNumGames();	// Retrieve the number of games to be simulated
			final int watch_mode = wargamesetup.getMode();		// Retrieve the watch mode (watch none, watch one, or watch all)
			final String results_file = wargamesetup.getSaveFile();	// Retrieve the file in which to save results
			
			// Create the battle results window, a WarGameReport object
			final WarGameReport battle_window = new WarGameReport(players, setup.getMap(), num_games, results_file);
			
			if(watch_mode == WarGameSetUp.WATCH_NONE) {	// If not watching any games, immediately show the results window (in a new thread)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					 battle_window.display();
				}
			});
			}
			// Each iteration of this loop plays out a game
			for(int i=0;i<num_games;i++) {
				if(watch_mode == WarGameSetUp.WATCH_ONE && i==1) {	// If only watching one game, after that game display the results window
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							 battle_window.display();
						}
					});
				}
				// The boolean 'watch' is true if the current game is to be watched or not
				boolean watch = (watch_mode == WarGameSetUp.WATCH_ALL || (watch_mode == WarGameSetUp.WATCH_ONE && i == 0) ) ? true : false;
				game = new Game(players, map_file_path, watch, wargamesetup.getSaveGameLogs());
				game.init();	// Initialize game
				game.play();	// Play out game
				SwingUtilities.invokeLater(new Runnable() {			// Send game results to the results window (in a new thread)
					public void run() {
						battle_window.sendResults(game.getResults(), game.getElapsedTime());
					}
				});
				game.close(true);		// close the game and board
				game.clearGame();	// Since the same Player objects are used game to game, clear their cards and stillIn status
			}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(!battle_window.isDisplayed())	// If the war games results window has yet to be displayed, display it
						 battle_window.display();
						battle_window.finished();	// Signal to the war games results window that all simulations are finished
					}
				});
		
			
		} else {	// Else there is at least one Human player, in which case only one game is played
			game = new Game(players, map_file_path, true, true);
			game.init();		// Initialize game
			game.play();		// Play game
			game.close(false);	// Close down the game but leave the board intact
		}
		
	}

	/*
	 * Returns the color of a given player id. If more than 6 players are allowed,
	 * this needs to be manually changed.
	 */
	public static Color getPlayerColor(int player_id) {
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
			return new Color(0.2f,0.2f,0.2f);
		case 5:
			return Color.yellow;
		default:
			return Color.gray;
		}
	}
	
	/* Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 * tabbed is whether or not it should be prepended with a \t
	 */
	public static void sayOutput(final String toSay, final int output_format_style, boolean forced) {
		if(game == null && output_format_style == OutputFormat.ERROR) {
			System.err.println(toSay);
		} else if (game == null) {
			System.out.println(toSay);
		} else
			game.sayOutput(toSay, output_format_style, forced);
	}

	/* Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 * tabbed is whether or not it should be prepended with a \t
	 */
	public static void sayOutput(final String toSay, final int output_format_style) {
			game.sayOutput(toSay, output_format_style, false);
	}

	/*
	 * Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 */
	public static void sayOutput(final String toSay) {
		sayOutput(toSay, OutputFormat.NORMAL);
	}
	
	/*
	 * Called by various methods to send something to whatever
	 * output is being used.
	 * toSay is the string wishing to be outputted
	 */
	public static void sayOutput(final String toSay, boolean forced) {
		sayOutput(toSay, OutputFormat.NORMAL, forced);
	}

	/* Called by various methods to send something to whatever
	 * error output is being used.
	 * @param The string wishing to be outputted error message.
	 */
	public static void sayError(final String toSay) {
		sayOutput(toSay, OutputFormat.ERROR);
	}

}
