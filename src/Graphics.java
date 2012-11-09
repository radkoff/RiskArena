/*
 * Graphics draws the world using javax.swing. This includes territory nodes,
 * adjacencies, and army amounts, all appropriately colorized.
 * 
 * Evan Radkoff
 */

import javax.swing.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Graphics extends JFrame {
	private int WINDOW_WIDTH = 1200, WINDOW_HEIGHT = 750;
	private int left_panel_width = 300;
	private int lower_left_height = 300;
	private Color BGCOLOR = Color.black;

	private Pretty pretty; // The Pretty class draws counties and adjacency lines
	private Console console; // User input/output text area
	private JPanel lower_left; // Not sure what this will become yet
	private JPanel main_panel;

	public Graphics() {
		initUI();
	}
	
	// Initializes graphics
	private void initUI() {
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		main_panel = new JPanel(new BorderLayout());
		JPanel side = new JPanel(new BorderLayout());
		pretty = new Pretty(WINDOW_WIDTH-left_panel_width, WINDOW_HEIGHT);
		console = new Console(left_panel_width, WINDOW_HEIGHT-lower_left_height);
		lower_left = new JPanel();
		lower_left.setMinimumSize(new Dimension(left_panel_width,lower_left_height));
		lower_left.setPreferredSize(new Dimension(left_panel_width,lower_left_height));
		lower_left.setBackground(pretty.BGCOLOR);
		//lower_left.setBorder(BorderFactory.createLineBorder(Color.white));
		
		setBackground(BGCOLOR);
		side.add(console, BorderLayout.NORTH);
		side.add(lower_left, BorderLayout.SOUTH);
		
		main_panel.add(side, BorderLayout.WEST);
		main_panel.add(pretty, BorderLayout.EAST);
		add(main_panel);

		setTitle(Risk.PROJECT_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public void sendPlayerListener(Human.PlayerListener pl) {
		console.sendPlayerListener(pl);
	}
	
	public void sayOutput(String toSay, int output_format) {
		console.addHistory(toSay, output_format);
	}
	
	public void sendGameInfo(Game game, int[][] adj) {
		pretty.sendGame(game, adj);
	}

	// Refreshes graphics by re-drawing everything
	public void refresh() {
		pretty.repaint();
	}

}
