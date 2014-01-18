/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.graphics;
/*
 * The SetUp class is responsible for the initial game set up dialog,
 * including choosing the players and map
 * 
 * Evan Radkoff
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import riskarena.Bot;
import riskarena.BotSniffer;
import riskarena.Human;
import riskarena.Player;
import riskarena.Risk;

public class SetUp extends JDialog {
	private SetUpPanel sup;

	private Player players[];
	private String map_name;
	private boolean war_games = true; // If true, all players are AI and the set up is for War Games

	public SetUp() {
		initUI();
	}

	/* Initializes the dialog UI */
	private void initUI() {
		sup = new SetUpPanel();	// SetUpPanel, a private class, represents the first game set up panel

		add(sup);
		setResizable(false);
		setTitle("Welcome to " + Risk.PROJECT_NAME);
		
		// When this window is closed, exit the application
				addWindowListener(new WindowAdapter()
			      {
			         public void windowClosing(WindowEvent e)
			         {
			           System.exit(0);
			         }
			      });

		pack();
		// Center the dialog:
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setLocation(new Point(ge.getCenterPoint().x - (getWidth() / 2),
				ge.getCenterPoint().y - (getHeight() / 2) - 100 ) );

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		sup.newPlayersChosen(Risk.MIN_PLAYERS);
		setVisible(true);
	}

	/*
	 * When "Start" is clicked, this method is called.
	 */
	private void startClicked(Player p[], String m) {
		players = p;
		map_name = m;
		if(players != null) {
		for(int i=0;i<players.length;i++) {
			if(players[i].getType() == Player.HUMAN)
				war_games = false;
		}
		}
		this.setVisible(false);	// close the dialog
	}

	// Once the set up is complete, this provides the player info to Risk.java
	public Player[] getPlayers() {
		if(players == null) {
			Risk.sayError("SetUp is returning null players.");
			System.exit(1);
		}
		return players;
	}

	// Once the set up is complete, this provides the map name to Risk.java
	public String getMap() {
		if(map_name == null) {
			Risk.sayError("SetUp is returning a null map name.");
			System.exit(1);
		}
		return map_name;
	}
	
	public boolean warGames() {
		return war_games;
	}

	// A panel that represents the first game set up phase (choose map, player info)
	private class SetUpPanel extends JPanel {
		private JComboBox mapChooser;
		private PlayersSlider players_slider;
		private JLabel maps_question, players_question;
		private ArrayList<PlayerConfig> player_configs;
		private JButton start_button;
		private ArrayList<String> available_bots;
		private JLabel logo;

		private int question_size = 14;	// Font size of "How many players?" etc
		private Border error_border = BorderFactory.createLineBorder(Color.red, 3);	// Invalid input causes a red border
		private Border original_border;	// for red border removal
		
		public SetUpPanel() {
			// Set up the label asking "What map would like you to play?"
			maps_question = new JLabel("What map would like you to play?");
			maps_question.setFont(FontMaker.makeCustomFont(question_size));
			maps_question.setForeground(Color.white);
			
			// Set up the label asking "How many players are there?"
			players_question = new JLabel("How many players are there?");
			players_question.setFont(FontMaker.makeCustomFont(question_size));
			players_question.setForeground(Color.white);
			
			logo = new JLabel("",new ImageIcon(Risk.LOGO_URL), JLabel.CENTER);
			
			// Obtain a list of available maps
			ArrayList<String> map_candidates = getMaps();
			mapChooser = new JComboBox(new Vector<String>(map_candidates));

			// A private extension of a JSlider. The min and max numbers of players are passed as args, as well as the default
			 players_slider = new PlayersSlider(Risk.MIN_PLAYERS, Risk.MAX_PLAYERS, Risk.MIN_PLAYERS);
			//players_slider = new PlayersSlider(Risk.MIN_PLAYERS, Risk.MAX_PLAYERS, 4);
			
			// Start button
			start_button = new JButton("Start");
			StartAction start_action = new StartAction();
			start_button.addActionListener(start_action);

			// Use the BotSniffer class to get all available bots
			BotSniffer bot_sniffer = new BotSniffer(Risk.BOT_PATH);
			available_bots = bot_sniffer.getBots();
			
			
			// newPlayersChosen is called when a new number of players is chosen via the slider, calling for
			// the re-rendering of player configs. To make sure the window is initially sized big enough, it is
			// called with the max number of players.
			newPlayersChosen(Risk.MAX_PLAYERS);
			pack();

		}
		
		// Draws the layout using a GroupLayout
		private void initUI() {
			this.removeAll();
			GroupLayout layout = new GroupLayout(this);
			this.setBackground(Risk.UGLY_GREY);
			this.setLayout(layout);
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			// Turn on automatically added gaps between components
			layout.setAutoCreateGaps(true);

			// Horizontal group
			GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
			hGroup.addComponent(logo);
			hGroup.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
							.addGroup(layout.createSequentialGroup()
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
											GroupLayout.DEFAULT_SIZE, 100)
											.addComponent(maps_question))
							.addGroup(layout.createSequentialGroup()
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
											GroupLayout.DEFAULT_SIZE, 100)
											.addComponent(players_question))
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING)
									.addGroup(layout.createSequentialGroup().addComponent(mapChooser)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
													GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addGroup(layout.createSequentialGroup().addComponent(players_slider)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
													GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									)
					);
			// Add player configs to the layout
			GroupLayout.SequentialGroup player_config_group = layout.createSequentialGroup();
			player_config_group.addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			for(int i=0;i<player_configs.size();i++) {
				player_config_group.addComponent(player_configs.get(i));
			}
			player_config_group.addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			hGroup.addGroup(player_config_group);
			// Add the start button to the bottom right
			hGroup.addGroup(layout
					.createSequentialGroup()
					.addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(start_button));

			layout.setHorizontalGroup(hGroup);

			// Vertical GroupLayout group
			GroupLayout.SequentialGroup vGroup = layout
					.createSequentialGroup();
			vGroup.addComponent(logo);
			vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,30, 30);

			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(maps_question)
					.addComponent(mapChooser));
			vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(players_question)
					.addComponent(players_slider));
			GroupLayout.ParallelGroup player_config_group2 = layout.createParallelGroup(Alignment.LEADING);
			for(int i=0;i<player_configs.size();i++) {
				player_config_group2.addComponent(player_configs.get(i));
			}
			vGroup.addGroup(player_config_group2);
			vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(start_button);
			layout.setVerticalGroup(vGroup);
		}
		
		// The Action called when the "start" button is pressed.
		// The main job is checking for adequate player information and creating an array
		// of Player objects using that provided information. It finally calls the startClicked
		// method, handing off the created players and the chosen map.
		private class StartAction extends AbstractAction {
			public void actionPerformed(ActionEvent ae) {
				Player playerz[] = new Player[player_configs.size()];
				boolean should_continue = true;
				
				// Re-set the human player name field borders in case they were red
				for(int i=0;i<player_configs.size();i++) {
					player_configs.get(i).name_field.setBorder(original_border);
				}
				// For each player config
				for(int i=0;i<player_configs.size();i++) {
					PlayerConfig.NameField this_config = player_configs.get(i).name_field;
					
					// If the player is Human, the name field is checked
					if(player_configs.get(i).type == Player.HUMAN) {
						boolean problem = false;
						// Check for any blank names
						if(!this_config.ready) {	// the boolean 'ready' will be true when a name was entered
							should_continue = false;	// flags for the cancelation of the game creation
							problem = true;	// flags for a red border
						}
						// Check for duplicate names
						for(int j=0;j<player_configs.size();j++) {
							if(j==i) continue;
							PlayerConfig.NameField that_config = player_configs.get(j).name_field;
							if(player_configs.get(j).type == Player.HUMAN && that_config.getText().equals(this_config.getText())) {
								should_continue = false;	// flags for the cancelation of the game creation
								problem = true;	// flags for a red border
								break;
							}
						}
						if(problem)
							this_config.setBorder(error_border);
						else
							this_config.setBorder(original_border);
						// Create a Human object for placement in the playerz array
						playerz[i] = new Human(this_config.getText(), Risk.getPlayerColor(i), i);
					} else {
						// If the player config is set to "BOT", create a new Bot object for the playerz array
						playerz[i] = new Bot((String)player_configs.get(i).bot_chooser.getSelectedItem(), Risk.getPlayerColor(i), i);
						// Now a unique name must be found by incrementing numbers (ie "Skynet", if taken "Skynet 2", if taken "Skynet 3", etc
						String bot_name = playerz[i].getName();
						int botID = 1;
						boolean found_name = false;
						while(!found_name) {
							found_name = true;
							String possible_name = new String(bot_name);
							if(botID != 1) possible_name = possible_name + " " + botID;
							for(int j=0;j<playerz.length;j++) {
								if(j==i || playerz[j] == null) continue;
								if(playerz[j].getName().equals(possible_name)) {
									found_name = false;
									botID++;
								}
							}
							if(found_name)
								bot_name = possible_name;
						}
						// Set the new chosen name
						playerz[i].setName(bot_name);
					}
				}
				
				if(!should_continue)
					return;
				String selected_map = (String)mapChooser.getSelectedItem() + ".map";
				startClicked(playerz, selected_map);
			}
		}
		
		// Obtains a list of available maps found in Risk.MAPS_DIR_NAME
		// that have the ".map" file extension.
		private ArrayList<String> getMaps() {
			File maps_dir = new File(Risk.MAPS_DIR_NAME);
			String[] files = maps_dir.list();
			ArrayList<String> map_candidates = new ArrayList<String>();
			if ( files == null ) {	// No maps directory found
				Risk.sayError("Maps directory does not exist. Please create \"" + Risk.MAPS_DIR_NAME + "\"");
				System.exit(1);
			} 
			for(int i=0; i < files.length; i++) {
				if( files[i].length() >= 5 && files[i].endsWith(".map") )
					map_candidates.add(files[i].substring(0, files[i].length()-4));
			}
			if(map_candidates.size() == 0) {
				Risk.sayError("No map file found. Please create a map file at " + Risk.MAPS_DIR_NAME + "somemap.map");
				Risk.sayError("For more on map files, see the readme.");
				System.exit(1);
			}
			return map_candidates;
		}

		// When the number of players is changed, this method is called to re-render the panel
		// with the appropriate number of player configs. 'new_num' is the new number of players.
		public void newPlayersChosen(int new_num) {
			// If the correct number is already selected, don't do anything
			if(player_configs != null && new_num == player_configs.size())
				return;
			player_configs = new ArrayList<PlayerConfig>();
			for(int i=0;i<new_num;i++) {
				player_configs.add(new PlayerConfig(i));	// Create new PlayerConfig objects with a unique id
			}
			initUI();	// re-draw the panel
		}

		// JSlider that lets the user change the number of players
		private class PlayersSlider extends JSlider implements ChangeListener {
			// min - minimum number of players allowed
			// max - maximum number of players allowed
			// def - default number of players
			public PlayersSlider(int min, int max, int def) {
				super(JSlider.HORIZONTAL);
				setMinimum(min);
				setMaximum(max);
				setValue(def);
				setMajorTickSpacing(1);
				setPaintTicks(true);
				setPaintLabels(true);
				setSnapToTicks(true);
				addChangeListener(this);
				// Make the labels white:
				Enumeration e = this.getLabelTable().keys();
				while (e.hasMoreElements()) {
			        Integer i = (Integer) e.nextElement();
			        JLabel label = (JLabel) this.getLabelTable().get(i);
			        label.setForeground(Color.white);
			    }
			}
			public void stateChanged(ChangeEvent arg0) {
				newPlayersChosen(getValue());
			}
		}

		// PlayerConfig represents the configuration panel for a player.
		// It allows for the choice of Human or Bot, where it prompts for a name
		// and bot type, respectively.
		private class PlayerConfig extends JPanel implements ActionListener {
			private int max_width = 115;
			public int type;	// set to either Player.HUMAN or Player.BOT
			public NameField name_field;
			public JComboBox bot_chooser;
			
			private JPanel radioButtonPanel;
			private ButtonGroup button_group;
			public JRadioButton human, bot;
			private ImageIcon human_img, bot_img;	// Human/Bot icons
			
			public PlayerConfig(int player_id) {
				setLayout(new BorderLayout());
				// Set the width (strictly)
				setPreferredSize(new Dimension(max_width,this.getPreferredSize().height));
				setMaximumSize(new Dimension(max_width,this.getPreferredSize().height));
				// Set the first player to Human and the others to Bot
				if(player_id == 0)
					type = Player.HUMAN;
				else if(available_bots.size() != 0)
					type = Player.BOT;
				
				setBackground(Risk.UGLY_GREY);
				button_group = new ButtonGroup();
				radioButtonPanel = new JPanel(new FlowLayout());
				radioButtonPanel.setBackground(Risk.UGLY_GREY);
				
				// Human radio button
				human = new JRadioButton();
				human.setActionCommand("human");
				human.addActionListener(this);
				human_img = new ImageIcon(Human.ICON_URL);
				
				// Bot radio button
				bot = new JRadioButton();
				bot.setActionCommand("bot");
				bot.addActionListener(this);
				bot_img = new ImageIcon(Bot.ICON_URL);
				// If there are no bots to choose from, obviously don't let the user select Bot for a player
				if(available_bots.size() == 0)
					bot.setEnabled(false);
				
				if(player_id == 0 || available_bots.size() == 0)
					human.setSelected(true);
				else
					bot.setSelected(true);
				
				// Construct radio button group (with icons)
				button_group.add(human);
				button_group.add(bot);
				radioButtonPanel.add(human);
				radioButtonPanel.add(new JLabel("",human_img,JLabel.CENTER));
				radioButtonPanel.add(bot);
				radioButtonPanel.add(new JLabel("",bot_img,JLabel.CENTER));
				
				// Bot chooser JComboBox
				bot_chooser = new JComboBox(new Vector(available_bots));
				name_field = new NameField();
				if(original_border == null)
					original_border = name_field.getBorder();
				
				add(radioButtonPanel, BorderLayout.NORTH);
				add(name_field, BorderLayout.CENTER);
				add(bot_chooser, BorderLayout.SOUTH);
				if(player_id == 0 || available_bots.size() == 0)
					bot_chooser.setVisible(false);
				else
					name_field.setVisible(false);
			}
			// When one of the buttons is selected, this is called
			public void actionPerformed(ActionEvent e) {
		        if(e.getActionCommand().equals("human")) {
		        	type = Player.HUMAN;
		        	name_field.setVisible(true);
		        	bot_chooser.setVisible(false);
		        } else if (e.getActionCommand().equals("bot")) {
		        	type = Player.BOT;
		        	name_field.setVisible(false);
		        	bot_chooser.setVisible(true);
		        } else {
		        	Risk.sayError("Unrecognized action event");
		        }
		    }
			// Human name text field
			public class NameField extends JTextField implements FocusListener {
				public boolean ready;	// True when a name has been entered
				// By default, the text "Enter Name" appears in grey
				public NameField() {
					ready = false;
					setFont(FontMaker.makeCustomFont(14));
					setText("Enter Name");
					setForeground(Color.GRAY);
					addFocusListener(this);
				}
				// When selected, clear "Enter Name"
				public void focusGained(FocusEvent arg0) {
					if(!ready) {
						this.setText("");
						this.setForeground(Color.black);
					}
				}
				
				// When focus goes away, set "ready" to true unless no name was actually entered
				public void focusLost(FocusEvent arg0) {
					if(this.getText().isEmpty()) {
						this.setText("Enter Name");
						this.setForeground(Color.GRAY);
						ready = false;
					} else
						ready = true;
				}

			}
		}
	}
}
