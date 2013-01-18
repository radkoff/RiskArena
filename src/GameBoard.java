/*
 * GameBoard draws the world using javax.swing. It's comprised of three elements:
 * A Console object for input and output, a Pretty object to draw the game board,
 * and a InfoPanel object to show continent/player info.
 * 
 * Evan Radkoff
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class GameBoard extends JFrame {
	private int WINDOW_WIDTH = 1200, WINDOW_HEIGHT = 750;
	private int side_panel_width = 300;
	private int info_panel_height = 300;
	private int bot_speed_height = 50;
	private Color BGCOLOR = Color.black;
	private Color LINE_COLOR = new Color(0.7f, 0.7f, 0.7f);
	private Border border;
	private final int border_thickness = 1;

	private Pretty pretty; // The Pretty class draws counties and adjacency lines
	private Console console; // User input/output text area
	private BotSpeedPanel bot_speed_panel; // Panel for configuring the bot playing speed
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
		JPanel side = new JPanel();
		side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
		pretty = new Pretty(WINDOW_WIDTH-side_panel_width, WINDOW_HEIGHT);
		
		// To get the height of the title bar, make a temporary window
		JFrame temp = new JFrame();
		temp.pack();
		Insets insets = temp.getInsets();

		console = new Console(new Dimension(side_panel_width-border_thickness, WINDOW_HEIGHT - info_panel_height - bot_speed_height - insets.top));
		bot_speed_panel = new BotSpeedPanel(new Dimension(side_panel_width-border_thickness, bot_speed_height), pretty.BGCOLOR, LINE_COLOR);
		lower_left = new InfoPanel(new Dimension(side_panel_width-border_thickness, info_panel_height), pretty.BGCOLOR);

		setBackground(BGCOLOR);
		side.add(leftJustify(console));
		side.add(leftJustify(bot_speed_panel));
		side.add(leftJustify(lower_left));
		border = BorderFactory.createMatteBorder(0, 0, 0, border_thickness, LINE_COLOR);
		side.setBorder(border);

		main_panel.add(pretty, BorderLayout.EAST);
		main_panel.add(side, BorderLayout.WEST);
		add(main_panel);

		setTitle(Risk.PROJECT_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
	}
	
	private Component leftJustify( JPanel panel )  {
	    Box  b = Box.createHorizontalBox();
	    b.add( panel );
	    b.add( Box.createHorizontalGlue() );
	    // (Note that you could throw a lot more components
	    // and struts and glue in here.)
	    return b;
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
				bot_speed_panel.sendGame(game);
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
