/*
 * The WarGameReport class is responsible for keeping track of
 * the results of war games, and represents the JDialog window that
 * presents these results as they happen. Following all simulations
 * it stores the final results in a file.
 * 
 * Evan Radkoff
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;


public class WarGameReport extends JDialog {
	private Player players[];
	private int num_games;	// Total number of games to be simulated
	private String results_file, map;	// results_file is where the war game results are stored
	private boolean displayed = false;	// Once this dialog becomes visible, this is set to true
	
	private StandingsPanel standings;	// The lower half of the WarGameReport window, showing the current standings
	private CenterPanel center_panel;	// The upper half of the WarGameReport window
	private JPanel main_panel;	// Contains standings and center_panel
	
	private int game_num = 1;	// Current game number
	private int[] points;	// points[i] is how many points player i has
	private int[][] point_values = {
			{ 1, 0, 0, 0, 0, 0 },		// 2 players (1st place player gets 1 point)
			{ 2, 1, 0, 0, 0, 0 },		// 3 players (1st place gets 2, 2nd gets 1)
			{ 3, 2, 1, 0, 0, 0 },		// 4 players (and so on)
			{ 4, 3, 2, 1, 0, 0 },		// 5 players
			{ 5, 4, 3, 2, 1, 0 }		// 6 players
	};
	
	// A list of the players, the map, the number of games being simulated, and the results file name
	public WarGameReport(Player p[], String m, int n_games, String file) {
		players = p;
		map = m;
		num_games = n_games;
		results_file = file;
		// Initialize point values to 0
		points = new int[players.length];
		for(int i=0;i<players.length;i++) {
			points[i] = 0;
		}
	}

	// Display the report window
	public void display() {
		displayed = true;
		initUI();
	}
	
	// Initialize the report window, consisting of a CenterPanel and StandingsPanel
	private void initUI() {
		main_panel = new JPanel(new BorderLayout());
		standings = new StandingsPanel();
		center_panel = new CenterPanel();
		main_panel.add(standings, BorderLayout.SOUTH);
		main_panel.add(center_panel, BorderLayout.NORTH);
		add(main_panel);
		setResizable(false);
		setTitle("War Game Results");
		
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
		setVisible(true);
	}
	
	// sendResults is called when a game concludes
	// results[i] is the player id of the person that got i'th in the game
	public void sendResults(ArrayList<Integer> results) {
		// Give each player the appropriate number of points via point_values
		for(int i=0;i<results.size();i++) {
			points[results.get(i)] += point_values[players.length - Risk.MIN_PLAYERS][i];
		}
		// Except for the last game, incrememt the current game number
		if(game_num != num_games)
			game_num += 1;

		// If the window is showing, refresh the panels to update accordingly
		if(displayed) {
			center_panel.refresh();
			standings.refresh();
		}
	}
	
	// Called when all games are complete
	public void finished() {
		writeResultsToFile();	// Write war game results to a file
		center_panel.finished();
		// Refresh both panels
		center_panel.refresh();
		standings.refresh();
	}
	
	/*
	 * Writes the results of the war games, as well as information like players,
	 * map, date, and time, to the file results_file at path Risk.WAR_GAME_LOG_PATH
	 */
	private void writeResultsToFile() {
		FileWriter fstream;
		try {
			fstream = new FileWriter(Risk.WAR_GAME_LOG_PATH + results_file,true);
			BufferedWriter results_writer = new BufferedWriter(fstream);
			ArrayList<Standing> standings = getStandings();
			results_writer.write(Risk.PROJECT_NAME + " war game results on " + (new Date().toString()) + ":\n\n");
			results_writer.write("Players - " + standings.size() + "\n");	// Write the number of players
			results_writer.write("Map - " + map + "\n");	// Write the map name
			results_writer.write("Number of games - " + num_games + "\n");	// Write the number of games
			results_writer.write("\nStandings:\n");
			for(int i=0;i<standings.size();i++) {
				results_writer.write((i+1) + ". " + standings.get(i).name + " - " + standings.get(i).points + " points\n");
			}
			results_writer.close();
		} catch (IOException e) {
			Risk.sayError("Unable to open the file " + Risk.WAR_GAME_LOG_PATH + results_file + " for writing game results.");
		}
		
	}
	
	// The StandingsPanel shows the current standings in order, including
	// which player has how many points.
	private class StandingsPanel extends JPanel {
		public StandingsPanel() {
			setBackground(Risk.UGLY_GREY);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	// ensures that StandingPanels will be added vertically
			refresh();
		}
		// Re-create StandingPanels
		public void refresh() {
			this.removeAll();
			ArrayList<Standing> standings = getStandings();
			for(int i=0;i<standings.size();i++) {
				add(new StandingPanel(i, standings.get(i).name, standings.get(i).points));
			}
		}
		// A StandingPanel is an individual standings panel that contains a player's name,
		// what place they're in, and how many points they have.
		private class StandingPanel extends JPanel {
			private int place_label_size = 24;
			private int name_label_size = 19;
			public StandingPanel(int rank, String name, int points) {
				setBackground(Risk.UGLY_GREY);
				// Border consisting of a white line on top:
				setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white));
				String place;
				switch(rank) {
				case 0: place = "1st"; break;
				case 1: place = "2nd"; break;
				case 2: place = "3rd"; break;
				default: place = (rank+1) + "th"; break;
				}
				
				// Label for the player's place (1st, 2nd, etc)
				JLabel place_label = new JLabel(place + "  ");
				if(rank == 0)	// If in first place, make it gold (yellow)
					place_label.setForeground(Color.yellow);
				else	// else, white
					place_label.setForeground(Color.white);
				place_label.setFont(FontMaker.makeCustomFont(place_label_size));
				
				// Label for the player's name
				JLabel name_label = new JLabel(name);
				if(rank == 0)	// If in first place, make it gold (yellow)
					name_label.setForeground(Color.yellow);
				else
					name_label.setForeground(Color.white);
				name_label.setFont(FontMaker.makeCustomFont(name_label_size));
				
				// Label for the player's point total
				JLabel points_label = new JLabel(" - " + points + " points");
				points_label.setForeground(Color.white);
				points_label.setFont(FontMaker.makeCustomFont(name_label_size-2));
				
				add(place_label);
				add(name_label);
				add(points_label);
			}
		}
	}
	
	// CenterPanel consists of the current game number, the point values for different
	// placements, and a button to close the window.
	private class CenterPanel extends JPanel {
		private JLabel simulating, game_num_label, of_game, complete, saved;
		private int simulating_label_size = 24;
		private int saved_label_size = 12;
		private JButton button;
		private PointsPanel pp;
		private JPanel sim_panel;
		
		public CenterPanel() {
			// When all sims are finished, display a green message saying so
			complete = new JLabel("Finished " + num_games + " Simulations");
			complete.setForeground(Color.green);
			complete.setFont(FontMaker.makeCustomFont(simulating_label_size));
			
			// Label for "Simulating game x out of y", broken into parts
			simulating = new JLabel("Simulating game ");
			simulating.setFont(FontMaker.makeCustomFont(simulating_label_size));
			simulating.setForeground(Color.white);
			game_num_label = new JLabel(game_num + "");
			game_num_label.setFont(FontMaker.makeCustomFont(simulating_label_size));
			game_num_label.setForeground(Color.white);
			of_game = new JLabel(" of " + num_games);
			of_game.setFont(FontMaker.makeCustomFont(simulating_label_size));
			of_game.setForeground(Color.white);
			sim_panel = new JPanel();
			sim_panel.setBackground(Risk.UGLY_GREY);
			sim_panel.add(simulating);
			sim_panel.add(game_num_label);
			sim_panel.add(of_game);
			
			// Upon complete of the simulations, display a message reminding the user where the results were written
			saved = new JLabel("Results saved to " + Risk.WAR_GAME_LOG_PATH + results_file);
			saved.setFont(FontMaker.makeCustomFont(saved_label_size));
			saved.setForeground(Color.LIGHT_GRAY);
			saved.setVisible(false);
			
			// Stop/Close button that exits 
			button = new JButton("Stop");
			button.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			pp = new PointsPanel();
			
			initUI();
		}
		
		// Draw the FirstPanel using a GroupLayout 
		private void initUI() {
			setPreferredSize(new Dimension(450,200));
			setBackground(Risk.UGLY_GREY);
			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			// Turn on automatically added gaps between components
			layout.setAutoCreateGaps(true);
			
			// Horizontal group
			GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
			hGroup.addGroup(layout.createSequentialGroup()
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(sim_panel)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
			hGroup.addGroup(layout.createSequentialGroup()
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(pp)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
			hGroup.addGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(saved).addComponent(button));
			layout.setHorizontalGroup(hGroup);

			// Vertical GroupLayout group
			GroupLayout.SequentialGroup vGroup = layout
					.createSequentialGroup();
			vGroup.addComponent(sim_panel);
			vGroup.addGroup(layout.createSequentialGroup()
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(pp)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
			vGroup.addGroup(layout.createParallelGroup()
									.addComponent(saved).addComponent(button));
			layout.setVerticalGroup(vGroup);
		}
		
		// Called when all game simulations are complete
		public void finished() {
			sim_panel.removeAll();
			sim_panel.add(complete);
			saved.setVisible(true);
			button.setText("Close");
			initUI();
		}
		
		// Called after each game simulation (the number of games has been updated)
		public void refresh() {
			game_num_label.setText(game_num + "");
		}
		
		// Small panel showing how many points each player gets for placing in a certain position
		private class PointsPanel extends JPanel {
			private int font_size = 12;
			public PointsPanel() {
				setBackground(Risk.UGLY_GREY);
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	// Lay them out vertically
				JLabel heading = new JLabel("Point values:");
				heading.setFont(FontMaker.makeCustomFont(font_size));
				heading.setForeground(Color.white);
				heading.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));	// White line on bottom
				add(heading);
				for(int i=0;i<players.length;i++) {
					String place;
					switch(i) {
					case 0: place = "1st"; break;
					case 1: place = "2nd"; break;
					case 2: place = "3rd"; break;
					default: place = (i+1) + "th"; break;
					}
					JLabel point_value = new JLabel(place + " - " + point_values[players.length - Risk.MIN_PLAYERS][i] + " points");
					point_value.setFont(FontMaker.makeCustomFont(font_size));
					point_value.setForeground(Color.white);
					add(point_value);
				}
			}
			
		}
	}
	
	// Returns the current order of how players are doing in terms of points.
	// This is used by both the standings panel and the method that writes results to a file.
	// The i'th standing this returns is the name and points value of the player that is in i'th place.
	private ArrayList<Standing> getStandings() {
		ArrayList<Standing> standings = new ArrayList<Standing>();
		boolean valid[] = new boolean[players.length];
		for(int i=0;i<players.length;i++)
			valid[i] = true;
		for(int i=0;i<players.length;i++) {
			int highest_points = -1;
			int winner = -1;
			for(int j=0;j<players.length;j++) {
				if(valid[j] && points[j] > highest_points) {
					highest_points = points[j];
					winner = j;
				}
			}
			standings.add(new Standing(players[winner].getName(), highest_points));
			valid[winner] = false;
		}
		return standings;
	}
	
	// Small helper class to store a name and points value for a player
	private class Standing {
		public String name;
		public int points;
		public Standing(String n, int p) {
			name = n; points = p;
		}
	}
	
}