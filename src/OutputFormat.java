import javax.swing.text.html.StyleSheet;

public class OutputFormat {
	public static final int NORMAL = 0;
	public static final int ERROR = 1;
	public static final int TABBED = 2;
	public static final int QUESTION = 3;
	public static final int ANSWER = 4;
	public static final int TABBED_QUESTION = 5;
	
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
			ss.addRule("p {margin:0; padding:0; display:inline;}");
			
			ss.addRule(".err {color:red}");
			ss.addRule(".tabbed {margin-left:10px; padding-left:10px;}");
			ss.addRule(".question {color:#175C10}");
			ss.addRule(".tabbedquestion {margin-left:10px; padding-left:10px; color:#175C10;}");
			ss.addRule(".answer {color:#1C9C25;}");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
