import java.util.InputMismatchException;
import java.util.Scanner;

public class InputListener {
		private Scanner ask;
		private boolean active;
		private String answer;
		
		public InputListener() {
			active = false;
			ask = new Scanner(System.in);
		}
		
		public boolean isWaiting() {
			return active;
		}
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
				answer = null;
				return getInt(MIN, MAX);
			}
			return int_value;
		}
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

		public void sendMsg(String to_send) {
			active = false;
			answer = to_send;
		}
}