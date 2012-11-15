/*
 * The console class is the UI component that shows a history of game
 * messages, and allows the user to enter input. It is used by the Graphics class.
 * 
 * Evan Radkoff
 */

// ALL OF THE IMPORTS
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class Console extends JPanel{
	private Color BGCOLOR = new Color(0.8f,0.8f,0.8f);	// Background color of the history
	private CommandLine cmd;		// input
	private HistoryView history_view;	// output
	private JScrollPane history_scroller;	// SrollPane containing history_view

	// history_border is the extra space around the edge of history_view (top, left, bottom, right)
	// The right component includes extra space for the width of a possible scroll bar.
	private Border history_border = BorderFactory.createEmptyBorder(5, 5, 5, 3 + UIManager.getInt("ScrollBar.width"));
	private Border panel_border = BorderFactory.createLineBorder(Color.BLACK, 1);
	private int font_size = 14;	// Font size for all output and input
	private int max_height;	// Maximum height the console should be. Given in constructor.

	private ArrayList<InputListener> waiting_for_answers;	// InputListeners belonging to either a human player
	// or the Risk class (for game setup) are given answers typed into the console

	public Console(int width, int max_h) {	// Width and height given as arguments
		waiting_for_answers = new ArrayList<InputListener>();
		max_height = max_h;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(width,max_height));
		setMaximumSize(new Dimension(width, max_height));
		setBackground(BGCOLOR);
		setBorder(panel_border);

		// Create command line and output history
		cmd = new CommandLine(">> ");
		cmd.setFont(FontMaker.makeCustomFont(font_size));
		history_view = new HistoryView();

		// Set up the scroll area containing output history
		history_scroller = new JScrollPane(history_view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		history_scroller.setPreferredSize(new Dimension(width, max_height - getCmdHeight()));
		history_scroller.setAlignmentX(LEFT_ALIGNMENT);
		history_scroller.setAlignmentY(BOTTOM_ALIGNMENT);
		history_scroller.setBorder(BorderFactory.createEmptyBorder());
		history_scroller.getViewport().setBackground(BGCOLOR);

		// Add the command line to the bottom, output history to the top
		add(cmd, BorderLayout.SOUTH);
		add(history_scroller, BorderLayout.NORTH);

	}

	// Returns the height of the command line
	public int getCmdHeight() {
		return cmd.getPreferredSize().height;
	}

	// Add new game output to the history_view.
	// String msg is the message to be added, and output_format is an ID corresponding
	// to the type of output (normal, question, error, etc). For more on these, see OutputFormat.java
	public void addHistory(String msg, int output_format) {
		// So that the CSS being applied to history_view can pick up on the output type, add its class name
		msg = "<p class=\"" + OutputFormat.getClassName(output_format) + "\">" + msg + "</p>";
		history_view.append(msg);
		history_view.scrollPaneToBottom();
	}

	// When the game is about to be set up or a player is constructed, Console is made aware of
	// it by this method being called. It adds the InputListener to its ArrayList called "waiting_for_answers"
	public void sendInputListener(InputListener pl) {
		waiting_for_answers.add(pl);
	}

	// The history of game output formatted using HTML.
	// ScrollablePanel is a type of JPanel that allows for the lines within to wrap
	private class HistoryView extends ScrollablePanel {
		private JEditorPane tarea;		// HTML style JEditorPane
		private String log_path;		// Path of the log file the game history is saved to
		BufferedWriter log_writer;	// writes to log_path

		public HistoryView() {
			setScrollableWidth( ScrollablePanel.ScrollableSizeHint.FIT );
			setLayout(new BorderLayout());
			tarea = new JEditorPane();

			// Generate the log path
			log_path = generateLogPath();
			// Create the BufferedWriter that will write to log_path
			try {
				File file = new File(log_path);
				file.createNewFile();
				FileWriter fstream = new FileWriter(log_path,true);
				log_writer = new BufferedWriter(fstream);
			} catch(Exception e) {
				Risk.sayError("Unable to create log file at " + log_path);
			}
			tarea.setEditorKit(new StyledEditorKit());
			tarea.setBackground(BGCOLOR);
			tarea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tarea.setEditable(false);	// read only
			tarea.setBorder(history_border);
			tarea.setContentType("text/html");
			tarea.setFont(FontMaker.makeCustomFont(font_size));

			// If we give the OutputFormat class the JEditorPane's document style sheet
			// it will apply all CSS formatting rules to it.
			OutputFormat.applyCSS(((HTMLDocument) tarea.getDocument()).getStyleSheet()); 

			setBackground(BGCOLOR);
			add(tarea);
		}

		// Append new output to the history
		public void append(String to_add) {

			// Append the new message to the JEditorPane tarea
			try {
				Document this_doc = tarea.getDocument();
				((HTMLEditorKit)tarea.getEditorKit()).insertHTML( (HTMLDocument)this_doc,
						this_doc.getEndPosition().getOffset()-1,
						to_add,
						1, /*int popDepth*/
						0, /*int pushDepth*/
						null);
			} catch (Exception e) {
				Risk.sayError("Unable to append console with game message:");
				Risk.sayError(e.getMessage());
			}

			try {
				// append to the file log_path
				log_writer.write(to_add);
			} catch (IOException e) {
				Risk.sayError("Unable to write to " + log_path + ":");
				Risk.sayError(e.getMessage());
			}

		}

		// Scrolls the output history down to the bottom (in a new Thread)
		private void scrollPaneToBottom() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					tarea.setCaretPosition(tarea.getDocument().getLength());
				}
			});
		}

		// All game histories are kept in log files. This method generates the
		// path for this game's log. It is garunteed to be unique.
		private String generateLogPath() {
			if(log_path != null) {
				Risk.sayError("Trying to generate a log path twice.");
			}
			String logp = Risk.LOG_PATH; // path of the logs directory
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
			String dateStr = sdf.format(cal.getTime());
			logp += dateStr;	// Add the current date to the file name
			int unique = 1;	// To ensure it's unique, keep adding a number until it is
			while(true) {
				File f = new File(logp + "-" + unique + ".html");
				if(!f.exists())
					break;
				unique++;
			}
			logp += "-" + unique + ".html";
			return logp;
		}
	}

	// The "command line" JTextField used for human input
	private class CommandLine extends JTextField {
		// Constructs the CommandLine given the prompt text (ie ">> ")
		public CommandLine(final String prompt) {
			super(prompt);	// JTextField constructor

			// Makes it so that the prompt text can't be deleted:
			setNavigationFilter( new NavigationFilterPrefixWithBackspace(this.getText().length(), this) );

			// When something is entered, this is triggered
			Action enter_pressed = new AbstractAction() {
				public void actionPerformed(ActionEvent arg0) {
					String pressed = cmd.getText();
					pressed = pressed.substring(prompt.length());	// remove the prompt text
					// One of InputListeners should be waiting for input. Loop through all
					// the InputListeners on file until it is found.
					for(int i=0;i<waiting_for_answers.size();i++) {
						if(waiting_for_answers.get(i).isWaiting()) {
							waiting_for_answers.get(i).sendMsg(pressed);
						}
					}
					addHistory(prompt + pressed, OutputFormat.ANSWER);	// Add what's entered to the history view
					if(Risk.output_to_std)
						System.out.println(prompt + pressed);
					cmd.setText(prompt);	// Reset the text in the JTextField back to just the prompt
				}
			};

			// Allows the enter key to be used to trigger the enter_pressed action
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "herp");
			getActionMap().put("herp", enter_pressed);
		}

		/*
		 * The NavigationFilterPrefixWithBackspace class allows the command line to be prefaced with
		 * a prompt text (">> ") that cannot be deleted
		 */
		private class NavigationFilterPrefixWithBackspace extends NavigationFilter
		{
			private int prefixLength;
			private Action deletePrevious;

			public NavigationFilterPrefixWithBackspace(int prefixLength, CommandLine component)
			{
				this.prefixLength = prefixLength;
				deletePrevious = component.getActionMap().get("delete-previous");
				component.getActionMap().put("delete-previous", new BackspaceAction());
				component.setCaretPosition(prefixLength);
			}
			public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias)
			{
				fb.setDot(Math.max(dot, prefixLength), bias);
			}
			public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias)
			{
				fb.moveDot(Math.max(dot, prefixLength), bias);
			}
			class BackspaceAction extends AbstractAction
			{
				public void actionPerformed(ActionEvent e)
				{
					JTextComponent component = (JTextComponent)e.getSource();

					if (component.getCaretPosition() > prefixLength)
					{
						deletePrevious.actionPerformed( null );
					}
				}
			}
		}
	}

}
