/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.graphics;
/*
 * The InfoPanel class represents the lower left panel of the GUI windows,
 * containing continent and player information. Called by the Graphics class.
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.border.Border;

import riskarena.Bot;
import riskarena.GameData;
import riskarena.Human;
import riskarena.Player;
import riskarena.Risk;

public class InfoPanel extends JPanel {
	private GameData game;		// Game engine object used to grab info
	private Color BGCOLOR;	// Background color
	private Border border = BorderFactory.createEmptyBorder(3, 3, 3, 3);
	private Color player_eliminated_color = new Color(0.6f,0.6f,0.6f);	// When a player is out, their name appears gray
	private Color player_turn_color = new Color(0.5f,0.5f,0.5f);	// When it's a player's turn, their label turns this color

	private int[] cont_info;	// Bonus armies amounts for continents
	private String[] cont_names;	// Continent names

	private int NUM_PLAYERS;
	private JLabel[] player_icons; // Player human/bot icons
	private String[] player_names;	// Player names
	private Color[] player_colors;	// Player colors
	private JLabel[] player_name_labels;	// Array of JLabels of player names
	private JLabel[] player_army_amounts;	// Array of JLabels of player army amounts

	private int label_size = 13;	// "Continents:" and "Players:" label font size
	private int cont_size = 16;		// Continent name font size
	private int player_label_size = 18;	// Player name font size

	// Construct an InfoPanel object. Dimension d is the size of the panel, Color c is the background color
	public InfoPanel(Dimension d, Color c) {
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		BGCOLOR = c;
		init();
	}
	// Initialize the info panel
	private void init() {
		setBackground(BGCOLOR);
		if(game == null) return;	// Without a game object we don't have information to obtain

		GroupLayout layout = new GroupLayout(this);
		// Turn on automatically adding gaps between components
		layout.setAutoCreateGaps(true);
		// Turn on automatically creating gaps between components and edges
		layout.setAutoCreateContainerGaps(true);
		this.setLayout(layout);
		this.setBorder(border);

		/////////////// Continents ////////////////////
		JLabel continents = new JLabel("Continents:");
		continents.setFont(FontMaker.makeCustomFont(label_size));
		continents.setForeground(Color.white);

		cont_info = game.getContinentBonuses();	// Array of continent army bonuses
		cont_names = game.getContinentNames();		// Array of continent names
		JLabel conts[] = new JLabel[cont_info.length];
		for(int i=0;i<cont_info.length;i++) {		// Construct each continent label
			JLabel somecont = new JLabel(cont_names[i] + ": + " + cont_info[i]);
			somecont.setFont(FontMaker.makeCustomFont(cont_size));
			somecont.setForeground(game.getContinentColor(i));
			conts[i] = somecont;
		}
		// Construct GroupLayout groups that contain continent labels
		GroupLayout.ParallelGroup first_column_cont = layout.createParallelGroup(Alignment.CENTER);
		GroupLayout.ParallelGroup second_column_cont = layout.createParallelGroup(Alignment.CENTER);
		GroupLayout.ParallelGroup cont_rows[] = new GroupLayout.ParallelGroup[(cont_info.length+1)/2];	// How many rows of labels


		JLabel blank = new JLabel(""); // Since these are organized in two columns, if the # continents is odd we'll need a blank label
		for(int i=0;i<cont_info.length;i+=2) {
			// Each iteration of this loop constructs a row of continent labels

			GroupLayout.ParallelGroup some_row = layout.createParallelGroup(Alignment.BASELINE);
			first_column_cont.addComponent(conts[i]);
			some_row.addComponent(conts[i]);

			if(i!=cont_info.length-1) {
				second_column_cont.addComponent(conts[i+1]);
				some_row.addComponent(conts[i+1]);
			} else {
				second_column_cont.addComponent(blank);
				some_row.addComponent(blank);
			}
			// Vertical group layout
			cont_rows[i/2] = some_row;
		}

		/////////////// Players ////////////////////
		JLabel players = new JLabel("Players:");
		players.setFont(FontMaker.makeCustomFont(label_size));
		players.setForeground(Color.white);

		// Construct GroupLayout groups that contain continent labels
		GroupLayout.ParallelGroup first_column_players = layout.createParallelGroup(Alignment.TRAILING);
		GroupLayout.ParallelGroup second_column_players = layout.createParallelGroup(Alignment.CENTER);

		NUM_PLAYERS = game.NUM_PLAYERS;
		player_icons = new JLabel[NUM_PLAYERS];
		player_names = new String[NUM_PLAYERS];
		player_colors = new Color[NUM_PLAYERS];
		player_name_labels = new JLabel[NUM_PLAYERS];
		player_army_amounts = new JLabel[NUM_PLAYERS];

		for(int i=0;i<NUM_PLAYERS;i++) {
			Player player = game.getPlayer(i);
			player_names[i] = player.getName();
			player_colors[i] = game.getPlayerColor(i);
			
			// Player's icon (human or bot)
			String icon = "";
			int player_type = player.getType();
			if(player_type == Player.BOT)
				icon = Bot.ICON_URL;
			else if(player_type == Player.HUMAN)
				icon = Human.ICON_URL;
			else
				Risk.sayError("Unrecognized player type for " + player_names[i]);
			player_icons[i] = new JLabel("", new ImageIcon(icon), JLabel.CENTER);
			// Player's name
			player_name_labels[i] = new JLabel(player_names[i] + " - ");
			player_name_labels[i].setForeground(player_colors[i]);
			player_name_labels[i].setFont(FontMaker.makeCustomFont(player_label_size));
			player_name_labels[i].setOpaque(true); // so that the background color can be changed
			player_name_labels[i].setBackground(BGCOLOR);
			// Player's army amount (should be 0 at first)
			player_army_amounts[i] = new JLabel(""+game.getPlayerArmies(i));
			player_army_amounts[i].setForeground(player_colors[i]);
			player_army_amounts[i].setFont(FontMaker.makeCustomFont(player_label_size));
			
			first_column_players.addGroup(layout.createSequentialGroup().addComponent(player_icons[i])
				.addComponent(player_name_labels[i]));
			second_column_players.addComponent(player_army_amounts[i]);
		}

		// Draw layout using GroupLayout:
		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER);
		hGroup.addComponent(continents);
		hGroup.addGroup(layout.createSequentialGroup()	 // two parallel group columns
				.addGroup(first_column_cont)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(second_column_cont));
		hGroup.addComponent(players);
		hGroup.addGroup(layout.createSequentialGroup()	 // two parallel group columns
				.addGroup(first_column_players)
				.addGroup(second_column_players));
		layout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = layout
				.createSequentialGroup();
		vGroup.addComponent(continents);
		for(int i=0;i<cont_rows.length;i++) {
			vGroup.addGroup(cont_rows[i]);
		}
		vGroup.addComponent(players);
		for(int i=0;i<NUM_PLAYERS;i++) {
			vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(player_icons[i])
					.addComponent(player_name_labels[i])
					.addComponent(player_army_amounts[i]));
		}
		layout.setVerticalGroup(vGroup);
	}

	/* When the Graphics class has its refresh method called, this panels needs to update
	 * and it calls this method. All army amounts are updated, and any eliminated players
	 * are drawn in the "player_eliminated_color" Color.
	 */
	public void refresh() {
		for(int i=0;i<NUM_PLAYERS;i++) {
			if(!game.getPlayer(i).getStillIn()) {	// If the player is eliminated
				player_army_amounts[i].setText("0");
				player_army_amounts[i].setForeground(player_eliminated_color);
				player_name_labels[i].setForeground(player_eliminated_color);
				continue;
			}
			player_army_amounts[i].setText(""+game.getPlayerArmies(i));
			if(game.getCurrentPlayerID() == i) {	// If this player's turn is happening
				player_name_labels[i].setBackground(player_turn_color);	// Highlight their name differently
			} else
				player_name_labels[i].setBackground(BGCOLOR);
		}
	}

	// When the Game is constructed and ready, this method will be called
	// and the panel will be re-initialized.
	public void sendGameData(GameData _g) {
		game = _g;
		init();
	}
}
