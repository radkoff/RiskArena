package riskarena.graphics;
/*
 * BotSpeedPanel represents the small panel on the left of the GameBoard
 * that allows the user to control how quickly decisions are made by bots using a JSlider.
 * Added 1/17/13
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import riskarena.GameData;

import java.lang.Math;

public class BotSpeedPanel extends JPanel {
	private GameData data;		// Game engine object used to get/set bot playing speed
	private Color BGCOLOR, LINE_COLOR;	// Background, separation line color
	private Border border, pad_sides = BorderFactory.createEmptyBorder(0, 8, 0, 8);

	private JSlider speed_slider;
	private JLabel speed_label, minus, plus;
	private JPanel slider_panel;
	
	private int label_size = 13;
	private int plus_minus_size = 22;
	private int speed_max = 700;		// Maximum bot playing speed allowed
	private int speed_min = 1;

	// Construct an InfoPanel object. Dimension d is the size of the panel, Color c is the background color
	public BotSpeedPanel(Dimension d, Color c, Color l_color) {
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		BGCOLOR = c;
		LINE_COLOR = l_color;
		border = BorderFactory.createMatteBorder(0, 0, 1, 0, LINE_COLOR);	// Line on bottom of panel
		init();
	}

	// Initialize the BotSpeedPanel panel
	private void init() {
		setBackground(BGCOLOR);
		setBorder(border);
		if(data == null) return;	// Without a game object we don't have information to get/set

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));	// Lay vertically

		speed_label = new JLabel("Bot Speed:");
		speed_label.setForeground(Color.white);
		speed_label.setFont(FontMaker.makeCustomFont(label_size));
		
		// Slider for selecting how fast bots should play.
		speed_slider = new JSlider();
		speed_slider.setMinimum(speed_min);
		speed_slider.setMaximum(speed_max-1);
		speed_slider.setValue(gameToSlider((int)data.getBotPlayingSpeed()));	// See gameToSlider() documentation
		speed_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {	// Signaled when the slider value is changed
				data.setBotPlayingSpeed(sliderToGame(speed_slider.getValue()));	// See sliderToGame() documentation
			}
		});
		speed_slider.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyChar() == 'p') {
					data.pause();
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) { }
			@Override
			public void keyTyped(KeyEvent arg0) { }
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
	public void sendGameData(GameData _d) {
		data = _d;
		init();
	}
	
	/*
	 * The speed slider is a value from 1 (slow) to speed_max (fast), while the game
	 * uses a number to determine how long each bot waits before making a decision, ie
	 * a pause of 1 is fast and a pause of speed_max is slow. This implies that the two
	 * numbers are inverses, and can be translated by taking speed_max - x. However we
	 * also don't want a linear relationship, ie putting the slider towards either end should
	 * have an exaggerated effect. To this end an exponential function is used, where the
	 * formula for converting from the slider value to the sleep time per bot decision is:
	 * 			gameValue = e^( -(sliderValue + offset)^exponent + delta ) - dampener
	 * The four parameters can be tweaked, but it's recommended that you graph it to understand
	 * what they do. And no matter what, it's important that the two functions, gameToSlider
	 * and sliderToGame, are exact inverses of each other.
	 * Also ensure that sliderToGame is not negative for speed_max
	 */
	private final double delta = 11.5;
	private final double offset = 100.0;
	private final double exponent = .31;
	private final double dampener = 10.0;
	
	// Convert a Game.bot_playing_speed value to a slider value
	private int gameToSlider(int input) {
		double val = Math.pow( delta - Math.log(input + dampener), 1.0/exponent );
		return (int)(val - offset);
	}
	
	// Convert a value given by the speed slider to a value that the Game will understand/use
	private int sliderToGame(int input) {
		double val = -1 * Math.pow(input + offset, exponent);
		return (int)(Math.exp(val + delta)-dampener);
	}

}
