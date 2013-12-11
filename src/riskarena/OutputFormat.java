package riskarena;
/*
 * The OutputFormat class handles the different types of output the game
 * can generate. These are given their own CSS class and styled accordingly.
 * 
 * Evan Radkoff
 */

import javax.swing.text.html.StyleSheet;

public class OutputFormat {
	public static final int NORMAL = 0;		// Normal game output
	public static final int ERROR = 1;		// Error output (appears red)
	public static final int TABBED = 2;		// Output that should be tabbed over
	public static final int QUESTION = 3;	// A question that requires human input
	public static final int ANSWER = 4;		// An answer given by a human
	public static final int TABBED_QUESTION = 5;	// A question requiring human input that is also tabbed

	public static String getClassName(int format) {
		switch(format) {
		case 0: return "out";
		case 1: return "err";
		case 2: return "tabbed";
		case 3: return "question";
		case 4: return "answer";
		case 5: return "tabbedquestion";
		default: Risk.sayError("Unidentified OutputFormat class id.");
		}
		return "";
	}

	public static void applyCSS(StyleSheet ss) {
		try {
			// Applied to all messages:
			ss.addRule("p {margin:0; padding:0; display:inline;}");

			// CSS rules specific to the output type
			ss.addRule(".err {color:red}");
			ss.addRule(".tabbed {margin-left:10px; padding-left:10px;}");
			ss.addRule(".question {color:#175C10}");
			ss.addRule(".tabbedquestion {margin-left:10px; padding-left:10px; color:#175C10;}");
			ss.addRule(".answer {color:#1C9C25;}");

		} catch(Exception e) {
			Risk.sayError(e.getMessage());
		}
	}
}
