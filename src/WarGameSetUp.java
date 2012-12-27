import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.Border;

public class WarGameSetUp extends JDialog {
	private final Color BGCOLOR = new Color(0.4f, 0.4f, 0.4f);	// Background color, currently set to some ugly grey
	private SetUpPanel sup;
	
	private int num_games;
	private int mode;
	private String savefile;
	
	public final static int WATCH_NONE = 0;
	public final static int WATCH_ONE = 1;
	public final static int WATCH_ALL = 2;
	
	public WarGameSetUp() {
		initUI();
	}
	
	private void initUI() {
		sup = new SetUpPanel();	// SetUpPanel, a private class, represents the first game set up panel

		add(sup);
		setResizable(false);
		setTitle("Config War Games " + Risk.PROJECT_NAME);
		setMinimumSize(new Dimension(500,200));
		pack();
		// Center the dialog:
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setLocation(new Point(ge.getCenterPoint().x - (getWidth() / 2),
				ge.getCenterPoint().y - (getHeight() / 2) - 100 ) );

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setVisible(true);
	}
	
	private void startClicked(int ng, int m, String file) {
		num_games = ng;
		mode = m;
		savefile = file;
		this.setVisible(false);	// close the dialog
	}
	
	public int getNumGames() {
		return num_games;
	}
	
	public int getMode() {
		return mode;
	}
	
	public String getSaveFile() {
		return savefile;
	}
	
	private class SetUpPanel extends JPanel {
		private JLabel num_games_question, watch_none_label, watch_one_label, watch_all_label, save_file_label;
		private ButtonGroup button_group;
		private JRadioButton watch_none, watch_one, watch_all;
		private JTextField num_games, save_file;
		private JButton start_button;
		
		private int question_size = 14;	// Font size of "How many players?" etc
		private Border error_border = BorderFactory.createLineBorder(Color.red, 3);	// Invalid input causes a red border
		private Border original_border;	// for red border removal
		
		public SetUpPanel() {
			num_games_question = new JLabel("How many games should the bots simulate?");
			num_games_question.setForeground(Color.white);
			num_games_question.setFont(FontMaker.makeCustomFont(question_size));
			watch_none_label = new JLabel("Don't watch any simulations");
			watch_none_label.setForeground(Color.white);
			watch_none_label.setFont(FontMaker.makeCustomFont(question_size));
			watch_one_label = new JLabel("Watch the first simulation");
			watch_one_label.setForeground(Color.white);
			watch_one_label.setFont(FontMaker.makeCustomFont(question_size));
			watch_all_label = new JLabel("Watch all game simulations");
			watch_all_label.setForeground(Color.white);
			watch_all_label.setFont(FontMaker.makeCustomFont(question_size));
			save_file_label = new JLabel("History file path: ");
			save_file_label.setForeground(Color.white);
			save_file_label.setFont(FontMaker.makeCustomFont(question_size));
			
			num_games = new JTextField("1", 3);
			save_file = new JTextField("",8);
			button_group = new ButtonGroup();
			watch_none = new JRadioButton();
			watch_one = new JRadioButton();
			watch_all = new JRadioButton();
			watch_none.setSelected(true);
			button_group.add(watch_none);
			button_group.add(watch_all);
			button_group.add(watch_one);
			
			// Start button
			start_button = new JButton("Start");
			start_button.addActionListener(new StartAction());
			
			initUI();
			pack();
		}
		
		private void initUI() {
			GroupLayout layout = new GroupLayout(this);
			this.setBackground(BGCOLOR);
			this.setLayout(layout);
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			// Turn on automatically added gaps between components
			layout.setAutoCreateGaps(true);

			// Horizontal group
			GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
			hGroup.addGroup(layout.createSequentialGroup().addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
							.addComponent(num_games_question)
							.addComponent(watch_none_label)
							.addComponent(watch_one_label)
							.addComponent(watch_all_label)
							.addComponent(save_file_label))
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
							.addGroup(layout.createSequentialGroup().addComponent(num_games)
									.addPreferredGap(
											LayoutStyle.ComponentPlacement.RELATED,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addComponent(watch_none).addComponent(watch_one).addComponent(watch_all).addComponent(save_file))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
			
			hGroup.addGroup(layout
					.createSequentialGroup()
					.addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(start_button));
			layout.setHorizontalGroup(hGroup);

			// Vertical GroupLayout group
			GroupLayout.SequentialGroup vGroup = layout
					.createSequentialGroup();
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(num_games_question)
					.addComponent(num_games));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(watch_none)
					.addComponent(watch_none_label));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(watch_one)
					.addComponent(watch_one_label));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(watch_all)
							.addComponent(watch_all_label));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(save_file_label)
							.addComponent(save_file));
			vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(start_button);
			layout.setVerticalGroup(vGroup);
		}
		
		private class StartAction extends AbstractAction {
			public void actionPerformed(ActionEvent ae) {
				boolean should_continue = true;
				Integer parsed_num_games = new Integer(1);
				try {
					parsed_num_games = new Integer(num_games.getText());
				} catch(NumberFormatException e) {
					num_games.setBorder(error_border);
					should_continue = false;
				}
				int m = -1;
				if(watch_none.isSelected()) m = 0;
				if(watch_one.isSelected()) m = 1;
				if(watch_all.isSelected()) m = 2;
				if(m < 0) {
					Risk.sayError("Unidentified mode in WarGamesSetUp");
					System.exit(-1);
				}
				if(should_continue)
					startClicked(parsed_num_games.intValue(), m, save_file.getText());
			}
		}
	}
	
}
