
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;

public class Console extends JPanel{
	private Color BGCOLOR = new Color(0.8f,0.8f,0.8f);
	private CommandLine cmd;
	private HistoryView history_view;
	private JScrollPane history_scroller;
	private ArrayList<String> history;
	
	private Border history_border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
	private Border panel_border = BorderFactory.createLineBorder(Color.BLACK, 1);
	private int font_size = 14;
	private int max_height;
	
	private ArrayList<Human.PlayerListener> players;
	
	public Console(int width, int max_h) {
		players = new ArrayList<Human.PlayerListener>();
		max_height = max_h;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(width,max_height));
		setMaximumSize(new Dimension(width, max_height));
		setBackground(BGCOLOR);
		setBorder(panel_border);
		cmd = new CommandLine(">> ");
		cmd.setFont(FontMaker.makeCustomFont(font_size));
		history_view = new HistoryView();
		history = new ArrayList<String>();
		history_scroller = new JScrollPane();
		history_scroller = new JScrollPane(history_view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//history_scroller.setBorder(BorderFactory.createLineBorder(Color.RED, 7));
		history_scroller.setPreferredSize(new Dimension(width, max_height - cmd.getPreferredSize().height));
		history_scroller.setAlignmentX(LEFT_ALIGNMENT);
		history_scroller.setAlignmentY(BOTTOM_ALIGNMENT);
		history_scroller.setBorder(BorderFactory.createEmptyBorder());
		history_scroller.getViewport().setBackground(BGCOLOR);
		add(cmd, BorderLayout.SOUTH);
		add(history_scroller, BorderLayout.NORTH);
		
	}
	
	public void addHistory(String msg, int output_format) {
		msg = "<p class=\"" + OutputFormat.getClassName(output_format) + "\">" + msg + "</p>";
		history_view.append(msg);
		history_view.refresh();
		history_view.scrollPaneToBottom();
	}
	
	public void sendPlayerListener(Human.PlayerListener pl) {
		players.add(pl);
	}

	private class HistoryView extends ScrollablePanel {
		private JEditorPane tarea;
		private String log_path;
		public HistoryView() {
			setScrollableWidth( ScrollablePanel.ScrollableSizeHint.FIT );
			setLayout(new BorderLayout());
			tarea = new JEditorPane();
			
			log_path = generateLogPath();
			try {
				File file = new File(log_path);
				file.createNewFile();
			} catch(Exception e) {
				Risk.sayError("Unable to create log file at " + log_path);
			}
			tarea.setEditorKit(new StyledEditorKit());
			tarea.setBackground(BGCOLOR);
			tarea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tarea.setEditable(false);
			tarea.setBorder(history_border);
			tarea.setContentType("text/html");
			tarea.setFont(FontMaker.makeCustomFont(font_size));
			
			//HTMLDocument doc = (HTMLDocument) tarea.getDocument();  
			//doc.getStyleSheet().addRule(".out {background-color:green}");
			
			JPanel container = new JPanel(new BorderLayout());
			container.setBackground( BGCOLOR );
			container.setBorder( tarea.getBorder() );
	        setBackground(BGCOLOR);
	        add(container, BorderLayout.NORTH);
	        add(tarea, BorderLayout.SOUTH);
		}
		public void append(String to_add) {
			// append to the file log_path
	        try {
	        		  // Create file 
	        		  FileWriter fstream = new FileWriter(log_path,true);
	        		  BufferedWriter out = new BufferedWriter(fstream);
	        		  out.write(to_add);
	        		  out.close();
			} catch (Exception e) {
				Risk.sayError("Unable to open log file at " + log_path);
			}
	        
		}
		public void refresh() {
			try {
				tarea.setPage(new File("src/logs/dummy.html").toURI().toURL());
				java.net.URL herp = new File(log_path).toURI().toURL();
				tarea.setPage(herp);
				OutputFormat.applyCSS(((HTMLDocument) tarea.getDocument()).getStyleSheet());  
			} catch(Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				Risk.sayError("Unable to open log file at " + log_path);
				System.exit(1);
			}
		}
		private void scrollPaneToBottom() {
			SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					tarea.setCaretPosition(tarea.getDocument().getLength());
			}
			});
		}
		
		private String generateLogPath() {
			if(log_path != null) {
				Risk.sayError("Trying to generate a log path twice.");
			}
			String logp = Risk.LOG_PATH;
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
			String dateStr = sdf.format(cal.getTime());
			logp += dateStr;
			int unique = 1;
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
	
	private class CommandLine extends JTextField {
	public CommandLine(final String prompt) {
		super(prompt);
		setNavigationFilter( new NavigationFilterPrefixWithBackspace(this.getText().length(), this) );
		
		Action enter_pressed = new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				String pressed = cmd.getText();
				pressed = pressed.substring(prompt.length());
				for(int i=0;i<players.size();i++) {
					if(players.get(i).isWaiting()) {
						players.get(i).sendMsg(pressed);
					}
				}
				addHistory(prompt + pressed, OutputFormat.ANSWER);
				cmd.setText(prompt);
			}
		};
		
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "herp");
		getActionMap().put("herp", enter_pressed);
	}
	
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
