/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.graphics;
/*
 * GameBoard draws the world using javax.swing. It's comprised of four elements:
 * A Console object for input and output, a Pretty object to draw the game board,
 * an InfoPanel object to show continent/player info, and a bot playing speed panel.
 * 
 * Evan Radkoff
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import riskarena.GameData;
import riskarena.InputListener;
import riskarena.Risk;

public class GameBoard extends JFrame {
	private int WINDOW_WIDTH = 1200, WINDOW_HEIGHT = 750;	// For now, static width/height
	private int side_panel_width = 300;	// Absolute width of the left side panel (the rest is WINDOW_WIDTH-side_panel_width)
	private int info_panel_height = 300; // Absolute height of the info panel (the console takes up the rest)
	private int bot_speed_height = 50;	// Absolute height of the bot playing speed panel
	private Color BGCOLOR = Color.black;
	private Color LINE_COLOR = new Color(0.7f, 0.7f, 0.7f);	// Color of separating line
	private Border border;
	private final int border_thickness = 1;

	private Pretty pretty; // The Pretty class draws counties and adjacency lines
	private Console console; // User input/output text area
	private BotSpeedPanel bot_speed_panel; // Panel for configuring the bot playing speed
	private InfoPanel lower_left;
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
	public void sendGameData(final GameData data) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pretty.sendGameData(data);
				pretty.sendAdjacencies(data.getWorldAdjacencyList());
				bot_speed_panel.sendGameData(data);
				lower_left.sendGameData(data);
			}
		});
	}

	// Refreshes graphics by re-drawing everything
	public void refresh() {
		pretty.repaint();
		lower_left.refresh();
	}
	

}
