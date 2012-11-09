/*
 * The Human class represents a Player of human type. Its "answers" to game-time
 * decisions are taken in through a Scanner object.
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Human extends Player {
	private static Scanner ask;	// Static Scanner object takes input from System.in
	private static PlayerListener player_listener;
	private String from_console;
	
	public Human(String n, Color c, int id) {
		super(0, n, c, id);	// Player constructor
		ask = new Scanner(System.in);
		player_listener = new PlayerListener();
	}
	
	public void sendInputToGraphics(Graphics graphics) {
		graphics.sendPlayerListener(player_listener);
	}
	
	/*
	 * Ask for an integer.
	 */
	public int askInt() {
		if(Risk.input_from_std) return askIntStd();
		player_listener.activate();
		while(from_console == null) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Integer parsed_input;
		try {
			parsed_input = new Integer(from_console);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			from_console = null;
			return askInt();
		}
		int answer = parsed_input.intValue();
		from_console = null;
		return answer;
	}
	public int askIntStd() {
		int result = 0;
		while(true) {
			try {
				result = ask.nextInt();
				ask.nextLine(); // eat up \n
				break;
			} catch (InputMismatchException e) {
				Risk.sayError("Integers only.");
				ask.next();
				continue;
			}
		}
		return result;
	}
	
	
	/*
	 * Ask for an integer. Forces only ints that are greater than 0
	 * @return int greater than zero
	 */
	public int askInt(int MIN) {
		if(Risk.input_from_std) return askIntStd(MIN);
		player_listener.activate();
		while(from_console == null) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Integer parsed_input;
		try {
			parsed_input = new Integer(from_console);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			from_console = null;
			return askInt(MIN);
		}
		from_console = null;
		int answer = parsed_input.intValue();
		if(answer < MIN) {
			Risk.sayError("Invalid entry. Must be greater than or equal to " + MIN + ".");
			return askInt(MIN);
		}
		return answer;
	}
	public int askIntStd(int MIN) {
		int result = 0;
		while(true) {
			try {
				result = ask.nextInt();
				ask.nextLine(); // eat up \n
				break;
			} catch (InputMismatchException e) {
				Risk.sayError("Integers only.");
				ask.next();
				continue;
			}
		}
		while (result < MIN ) {
			Risk.sayError("Invalid entry. Must be greater than or equal to " + MIN + ".");
			while(true) {
				try {
					result = ask.nextInt();
					ask.nextLine(); // eat up \n
					break;
				} catch (InputMismatchException e) {
					Risk.sayError("Integers only.");
					ask.next();
					continue;
				}
			}
		}
		return result;
	}
	
	
	/* Asks for an int that is between MIN and MAX.
	 * @return int between MIN and MAX, inclusive.
	 */
	public int askInt(int MIN, int MAX) {
		if(Risk.input_from_std) return askIntStd(MIN,MAX);
		player_listener.activate();
		while(from_console == null) {
			try {
				//Thread.sleep(1);
				Thread.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Risk.sayOutput(from_console);
		Integer parsed_input;
		try {
			parsed_input = new Integer(from_console);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			from_console = null;
			return askInt(MIN, MAX);
		}
		from_console = null;
		int answer = parsed_input.intValue();
		if(answer < MIN || answer > MAX) {
			Risk.sayError("Invalid entry. Must be from " +
					MIN + " to " + MAX + ", inclusive.");
			return askInt(MIN, MAX);
		}
		return answer;
	}
	public int askIntStd(int MIN, int MAX) {
		int result = 0;
		while(true) {
			try {
				result = ask.nextInt();
				ask.nextLine(); // eat up \n
				break;
			} catch (InputMismatchException e) {
				Risk.sayError("Integers only.");
				ask.next();
				continue;
			}
		}
		while (result < MIN || result > MAX ) {
			Risk.sayError("Invalid entry. Must be from " +
					MIN + " to " + MAX + ", inclusive.");
			while(true) {
				try {
					result = ask.nextInt();
					ask.nextLine(); // eat up \n
					break;
				} catch (InputMismatchException e) {
					Risk.sayError("Integers only.");
					ask.next();
					continue;
				}
			}
		}

		return result;
	}

	// Ask for a line of input
	public String askLine() {
		while(from_console == null) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String answer = from_console;
		from_console = null;
		return answer;
		//return ask.nextLine();
	}
	
	public class PlayerListener {
		private boolean active;
		public PlayerListener() {
			active = false;
		}
		public boolean isWaiting() {
			return active;
		}
		public void activate() {
			active = true;
		}
		public void sendMsg(String to_send) {
			active = false;
			from_console = to_send;
		}
	}
}
