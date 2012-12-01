import java.awt.BorderLayout;
import java.awt.CardLayout;
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

public class SetUp extends JDialog {
	private final Color BGCOLOR = new Color(0.4f, 0.4f, 0.4f);	// Background color, currently set to some ugly grey

	private CardLayout switcher;
	private JPanel main_panel;	// Primary panel that uses switcher to go from FirstPanel to WarGameSetUp
	private FirstPanel fp;	// card 1
	private WarGameSetUp wargame;	// card 2

	private Player players[];
	private String map_name;

	public SetUp() {
		initUI();
	}

	private void initUI() {
		switcher = new CardLayout();
		main_panel = new JPanel(switcher);
		fp = new FirstPanel();
		main_panel.add(fp, "first");

		add(main_panel);
		switcher.show(main_panel, "first");
		setResizable(false);
		setTitle("Welcome to " + Risk.PROJECT_NAME);

		pack();
		// Center the dialog:
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setLocation(new Point(ge.getCenterPoint().x - (getWidth() / 2),
				ge.getCenterPoint().y - (getHeight() / 2) - 100 ) );

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		fp.newPlayersChosen(Risk.MIN_PLAYERS);
		setVisible(true);
	}

	private void startClicked(Player p[], String m) {
		players = p;
		map_name = m;
		this.setVisible(false);
	}

	public Player[] getPlayers() {
		return players;
	}

	public String getMap() {
		if(players == null) {
			Risk.sayError("SetUp is returning a null map name.");
			System.exit(1);
		}
		return map_name;
	}

	private class FirstPanel extends JPanel {
		private JComboBox mapChooser;
		private PlayersSlider players_slider;
		private JLabel maps_question, players_question;
		private ArrayList<PlayerConfig> player_configs;
		private JButton start_button;
		private ArrayList<String> available_bots;
		private JLabel logo;
		private final String LOGO_URL = "src/images/RiskArena.png";

		private int question_size = 14;
		private Border error_border = BorderFactory.createLineBorder(Color.red, 3);
		private Border original_border;
		
		public FirstPanel() {
			maps_question = new JLabel("What map would like you to play?");
			maps_question.setFont(FontMaker.makeCustomFont(question_size));
			maps_question.setForeground(Color.white);
			players_question = new JLabel("How many players are there?");
			players_question.setFont(FontMaker.makeCustomFont(question_size));
			players_question.setForeground(Color.white);
			
			logo = new JLabel("",new ImageIcon(LOGO_URL), JLabel.CENTER);
			
			File maps_dir = new File(Risk.MAPS_DIR_NAME);
			String[] files = maps_dir.list();
			ArrayList<String> map_candidates = new ArrayList<String>();
			if ( files == null ) {
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
			mapChooser = new JComboBox(new Vector<String>(map_candidates));

			players_slider = new PlayersSlider(Risk.MIN_PLAYERS, Risk.MAX_PLAYERS, Risk.MIN_PLAYERS);

			start_button = new JButton("Start");
			start_button.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					Player playerz[] = new Player[player_configs.size()];
					boolean should_continue = true;
					for(int i=0;i<player_configs.size();i++) {
						player_configs.get(i).name_field.setBorder(original_border);
					}
					for(int i=0;i<player_configs.size();i++) {
						PlayerConfig.NameField this_config = player_configs.get(i).name_field;
						this_config.setBorder(BorderFactory.createEmptyBorder());
						
						if(player_configs.get(i).type == Player.HUMAN) {
							boolean problem = false;
							// Check for any blank names
							if(!this_config.ready) {
								this_config.setBorder(error_border);
								should_continue = false;
								problem = true;
							}
							// Check for duplicate names
							for(int j=0;j<player_configs.size();j++) {
								if(j==i) continue;
								PlayerConfig.NameField that_config = player_configs.get(j).name_field;
								if(player_configs.get(j).type == Player.HUMAN && that_config.getText().equals(this_config.getText())) {
									should_continue = false;
									problem = true;
									break;
								}
							}
							if(problem)
								this_config.setBorder(error_border);
							else
								this_config.setBorder(original_border);
							playerz[i] = new Human(this_config.getText(), Risk.getPlayerColor(i), i);
						} else {
							playerz[i] = new Bot((String)player_configs.get(i).bot_chooser.getSelectedItem(), Risk.getPlayerColor(i), i);
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
							playerz[i].setName(bot_name);
						}
					}
					if(!should_continue)
						return;
					startClicked(playerz, (String)mapChooser.getSelectedItem() + ".map");
				}
			});

			BotSniffer bot_sniffer = new BotSniffer(Risk.BOT_PATH);
			available_bots = bot_sniffer.getBots();

			newPlayersChosen(Risk.MAX_PLAYERS);
			pack();
		}
		
		private void initUI() {
			this.removeAll();
			GroupLayout layout = new GroupLayout(this);
			this.setBackground(BGCOLOR);
			this.setLayout(layout);
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			// Turn on automatically adding gaps between components
			layout.setAutoCreateGaps(true);

			// Draw layout using GroupLayout:
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

			hGroup.addGroup(layout
					.createSequentialGroup()
					.addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(start_button));

			layout.setHorizontalGroup(hGroup);

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
			
			//pack();
		}

		public void newPlayersChosen(int new_num) {
			if(player_configs != null && new_num == player_configs.size())
				return;
			player_configs = new ArrayList<PlayerConfig>();
			for(int i=0;i<new_num;i++) {
				player_configs.add(new PlayerConfig(i));
			}
			initUI();
		}

		// JSlider that lets the user change the number of players
		private class PlayersSlider extends JSlider implements ChangeListener {

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

		private class PlayerConfig extends JPanel implements ActionListener {
			private int max_width = 115;
			public int type;
			public NameField name_field;
			public JComboBox bot_chooser;
			
			private JPanel radioButtonPanel;
			private ButtonGroup button_group;
			public JRadioButton human, bot;
			private ImageIcon human_img, bot_img;
			
			public PlayerConfig(int player_id) {
				setLayout(new BorderLayout());
				setPreferredSize(new Dimension(max_width,this.getPreferredSize().height));
				setMaximumSize(new Dimension(max_width,this.getPreferredSize().height));
				if(player_id == 0)
					type = Player.HUMAN;
				else if(available_bots.size() != 0)
					type = Player.BOT;
				setBackground(BGCOLOR);
				button_group = new ButtonGroup();
				
				radioButtonPanel = new JPanel(new FlowLayout());
				radioButtonPanel.setBackground(BGCOLOR);
				
				human = new JRadioButton();
				human.setActionCommand("human");
				human.addActionListener(this);
				human_img = new ImageIcon(Human.ICON_URL);
				
				bot = new JRadioButton();
				bot.setActionCommand("bot");
				bot.addActionListener(this);
				bot_img = new ImageIcon(Bot.ICON_URL);
				if(available_bots.size() == 0)
					bot.setEnabled(false);
				
				if(player_id == 0 || available_bots.size() == 0)
					human.setSelected(true);
				else
					bot.setSelected(true);
				
				button_group.add(human);
				button_group.add(bot);
				radioButtonPanel.add(human);
				radioButtonPanel.add(new JLabel("",human_img,JLabel.CENTER));
				radioButtonPanel.add(bot);
				radioButtonPanel.add(new JLabel("",bot_img,JLabel.CENTER));
				
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
			public class NameField extends JTextField implements FocusListener{
				public boolean ready;
				public NameField() {
					ready = false;
					setFont(FontMaker.makeCustomFont(14));
					setText("Enter Name");
					setForeground(Color.GRAY);
					addFocusListener(this);
				}
				public void focusGained(FocusEvent arg0) {
					if(!ready) {
						this.setText("");
						this.setForeground(Color.black);
					}
				}
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

	private class WarGameSetUp extends JPanel {
		
	}
}
