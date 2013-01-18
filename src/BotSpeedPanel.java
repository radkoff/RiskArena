/*
 * BotSpeedPanel represents the small panel on the left of the GameBoard
 * that allows the user to control how quickly decisions are made by bots using a JSlider.
 * Added 1/17/13
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.lang.Math;

public class BotSpeedPanel extends JPanel {
	private Game game;		// Game engine object used to get/set bot playing speed
	private Color BGCOLOR, LINE_COLOR;	// Background, separation line color
	private Border border, pad_sides = BorderFactory.createEmptyBorder(0, 8, 0, 8);

	private JSlider speed_slider;
	private JLabel speed_label, minus, plus;
	private JPanel slider_panel;
	
	private int label_size = 13;
	private int plus_minus_size = 22;
	private int speed_max = 400;

	// Construct an InfoPanel object. Dimension d is the size of the panel, Color c is the background color
	public BotSpeedPanel(Dimension d, Color c, Color l_color) {
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		BGCOLOR = c;
		LINE_COLOR = l_color;
		border = BorderFactory.createMatteBorder(0, 0, 1, 0, LINE_COLOR);
		init();
	}

	// Initialize the BotSpeedPanel panel
	private void init() {
		setBackground(BGCOLOR);
		setBorder(border);
		if(game == null) return;	// Without a game object we don't have information to get/set

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		speed_label = new JLabel("Bot Speed:");
		speed_label.setForeground(Color.white);
		speed_label.setFont(FontMaker.makeCustomFont(label_size));
		
		speed_slider = new JSlider();
		speed_slider.setMinimum(1);
		speed_slider.setMaximum(speed_max-1);
		speed_slider.setValue(gameToSlider((int)game.getPlayingSpeed()));
		speed_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				game.setPlayingSpeed(sliderToGame(speed_slider.getValue()));
			}
		});
		
		minus = new JLabel("-");
		minus.setFont(FontMaker.makePlainFont(plus_minus_size));
		minus.setForeground(Color.white);
		
		plus = new JLabel("+");
		plus.setFont(FontMaker.makePlainFont(plus_minus_size));
		plus.setForeground(Color.white);
		
		slider_panel = new JPanel();
		slider_panel.setLayout(new BoxLayout(slider_panel, BoxLayout.LINE_AXIS));
		slider_panel.setBorder(pad_sides);
		slider_panel.setBackground(BGCOLOR);
		slider_panel.add(minus);
		slider_panel.add(speed_slider);
		slider_panel.add(plus);
		
		add(speed_label);
		add(slider_panel);
	}

	// When the Game is constructed and ready, this method will be called
	// and the panel will be re-initialized.
	public void sendGame(Game _g) {
		game = _g;
		init();
	}
	
	
	private double delta = .6;
	private int gameToSlider(int input) {
		//return (int)(Math.pow( ((double)speed_max - input)/ (speed_max*delta), 2.0));
		return speed_max - (int)(Math.pow(input,2.15)/(speed_max * delta));
	}
	
	private int sliderToGame(int input) {
		return (int)Math.pow((speed_max*delta) * ((double)speed_max - input), 1.0/2.15);
	}

}
