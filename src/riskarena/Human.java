package riskarena;
/*
 * The Human class represents a Player of human type. Its "answers" to game-time
 * decisions are taken in through a Scanner object.
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.util.InputMismatchException;
import java.util.Scanner;

import riskarena.graphics.GameBoard;

public class Human extends Player {
	public static final String ICON_URL = Risk.IMAGES_PATH + "human.png";
	//private static Scanner ask;	// Static Scanner object takes input from System.in
	private static InputListener console_input;
	//private String from_console;

	public Human(String n, Color c, int id) {
		super(0, n, c, id);	// Player constructor
		//ask = new Scanner(System.in);
		console_input = new InputListener();
	}

	public void sendInputToGraphics(GameBoard graphics) {
		graphics.sendInputListener(console_input);
	}

	/*
	 * Ask for an integer.
	 */
	public int askInt() {
		console_input.activate();
		return console_input.getInt();
	}

	/*
	 * Ask for an integer. Forces only ints that are greater than 0
	 * @return int greater than zero
	 */
	public int askInt(int MIN) {
		console_input.activate();
		return console_input.getInt(MIN);
	}


	/* Asks for an int that is between MIN and MAX.
	 * @return int between MIN and MAX, inclusive.
	 */
	public int askInt(int MIN, int MAX) {
		console_input.activate();
		return console_input.getInt(MIN, MAX);
	}


	// Ask for a line of input
	public String askLine() {
		console_input.activate();
		return console_input.getString();
	}

}
