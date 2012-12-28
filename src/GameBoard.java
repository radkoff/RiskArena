/*
 * GameBoard draws the world using javax.swing. It's comprised of three elements:
 * A Console object for input and output, a Pretty object to draw the game board,
 * and a InfoPanel object to show continent/player info.
 * 
 * Evan Radkoff
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameBoard extends JFrame {
	private int WINDOW_WIDTH = 1200, WINDOW_HEIGHT = 750;
	private int lower_left_width = 300;
	private int lower_left_height = 300;
	private Color BGCOLOR = Color.black;

	private Pretty pretty; // The Pretty class draws counties and adjacency lines
	private Console console; // User input/output text area
	private InfoPanel lower_left; // Not sure what this will become yet
	private JPanel main_panel;

	public GameBoard() {
		initUI();
	}

	// Initializes graphics
	private void initUI() {
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		main_panel = new JPanel(new BorderLayout());
		JPanel side = new JPanel(new BorderLayout());
		pretty = new Pretty(WINDOW_WIDTH-lower_left_width, WINDOW_HEIGHT);

		// To get the height of the title bar, make a temporary window
		JFrame temp = new JFrame();
		temp.pack();
		Insets insets = temp.getInsets();

		console = new Console(lower_left_width, WINDOW_HEIGHT-lower_left_height - insets.top);
		lower_left = new InfoPanel(new Dimension(lower_left_width,lower_left_height), pretty.BGCOLOR);

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

	public void sendInputListener(InputListener pl) {
		console.sendInputListener(pl);
	}

	public void sayOutput(String toSay, int output_format) {
		console.addHistory(toSay, output_format);
	}

	// Send the game information to the Pretty and InfoPanel objects
	public void sendGameInfo(final Game game, final int[][] adj) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pretty.sendGame(game, adj);
				lower_left.sendGame(game);
			}
		});
	}

	// Refreshes graphics by re-drawing everything
	public void refresh() {
		pretty.repaint();
		lower_left.refresh();
	}

}
