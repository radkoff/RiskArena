/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * This is used for all human input.
 * The InputListener class, when activated, waits for a call to sendMsg.
 * When it receives this message it verifies that what was sent is within the
 * correct boundaries, then returns that value.
 * 
 * Evan Radkoff
 */

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputListener {
	private Scanner ask;	// When Risk.input_from_std is true, a Scanner is used for input
	private boolean active;	// When activated (waiting for input) this is true
	private String answer;	// The answer given

	public InputListener() {
		active = false;
		ask = new Scanner(System.in);
	}

	// Returns true if this InputListener is currently waiting for input
	public boolean isWaiting() {
		return active;
	}

	// Set active to true, and sleep until an answer is provided via sendMsg
	public void activate() {
		active = true;
		while(answer == null) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		active = false;
	}

	// Get a String
	public String getString() {
		if(Risk.input_from_std) return getStringStd();
		activate();
		String result = answer;
		answer = null;
		return result;
	}
	public String getStringStd() {
		return ask.nextLine();
	}

	// Get an int
	public int getInt() {
		if(Risk.input_from_std) return getIntStd();
		activate();
		String result = answer;
		answer = null;
		Integer parsed_input;
		try {
			parsed_input = new Integer(result);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			return getInt();
		}
		return parsed_input.intValue();
	}
	public int getIntStd() {
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

	// Get an int bigger than or equal to MIN
	public int getInt(int MIN) {
		if(Risk.input_from_std) return getIntStd(MIN);
		activate();
		String result = answer;
		answer = null;
		Integer parsed_input;
		try {
			parsed_input = new Integer(result);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			return getInt(MIN);
		}
		int int_value = parsed_input.intValue();
		if(int_value < MIN) {
			Risk.sayError("Invalid entry. Must be greater than or equal to " + MIN + ".");
			return getInt(MIN);
		}
		return int_value;
	}
	// Get an int bigger than or equal to MIN from the Scanner
	public int getIntStd(int MIN) {
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

	// Get an int from MIN to MAX, inclusive
	public int getInt(int MIN, int MAX) {
		if(Risk.input_from_std) return getIntStd(MIN, MAX);
		activate();
		String result = answer;
		answer = null;
		Integer parsed_input;
		try {
			parsed_input = new Integer(result);
		} catch(NumberFormatException e) {
			Risk.sayError("Integers only.");
			return getInt(MIN,MAX);
		}
		int int_value = parsed_input.intValue();
		if(int_value < MIN || int_value > MAX) {
			Risk.sayError("Invalid entry. Must be from " +
					MIN + " to " + MAX + ", inclusive.");
			return getInt(MIN, MAX);
		}
		return int_value;
	}
	// Get an int from MIN to MAX, inclusive, from the Scanner object
	public int getIntStd(int MIN, int MAX) {
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

	// Receive input from some other Thread
	public void sendMsg(String to_send) {
		active = false;
		answer = to_send;
	}
}
