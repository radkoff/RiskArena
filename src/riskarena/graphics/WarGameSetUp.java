/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.graphics;
/*
 * WarGameSetUp represents the window that allows the user to configure
 * War Game settings before they commence. (War games are all-AI)
 * The user can choose the number of games, the "game watching" mode, and the results file
 * 
 * Evan Radkoff
 */

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.Border;

import riskarena.Risk;

public class WarGameSetUp extends JDialog {
	private SetUpPanel sup;
	
	private int num_games, mode;
	private String savefile;	// Filename to save results in
	private boolean save_game_logs;	// Whether or not to save individual game logs
	
	public final static int WATCH_NONE = 0;		// If mode is set to this, the user does not want to watch any games
	public final static int WATCH_ONE = 1;		// If mode is set to this, the user wants to watch just the first game
	public final static int WATCH_ALL = 2;		// If mode is set to this, the user wants to watch all games
	
	public WarGameSetUp() {
		initUI();
	}
	
	private void initUI() {
		sup = new SetUpPanel();	// SetUpPanel, a private class, represents the first game set up panel

		add(sup);
		setResizable(false);
		setTitle(Risk.PROJECT_NAME + ": Configure War Games");
		setMinimumSize(new Dimension(500,200));
		
		// When this window is closed, exit the application
		addWindowListener(new WindowAdapter()
	      {
	         public void windowClosing(WindowEvent e)
	         {
	           System.exit(0);
	         }
	      });
		
		pack();
		// Center the dialog:
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setLocation(new Point(ge.getCenterPoint().x - (getWidth() / 2),
				ge.getCenterPoint().y - (getHeight() / 2) - 100 ) );

		// Do not continue the main execution until this dialog is closed
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		setVisible(true);
	}
	
	// Called when "start" is clicked and settings are collected
	private void startClicked(int ng, int m, String file, boolean sgl) {
		num_games = ng;
		mode = m;
		savefile = file;
		save_game_logs = sgl;
		this.setVisible(false);	// close the dialog
	}
	
	// Get the number of games to be played
	public int getNumGames() {
		return num_games;
	}
	
	// Get the watch mode
	public int getMode() {
		return mode;
	}
	
	// Get the results file name
	public String getSaveFile() {
		return savefile;
	}
	
	// Get the boolean of whether or not to save individual game logs
	public boolean getSaveGameLogs() {
		return save_game_logs;
	}
	
	private class SetUpPanel extends JPanel {
		private JLabel num_games_question, watch_none_label, watch_one_label, watch_all_label, save_file_label, game_logs_label, results_file_path;
		private ButtonGroup button_group;
		private JRadioButton watch_none, watch_one, watch_all;
		private JTextField num_games, save_file;
		private JButton start_button;
		private JCheckBox game_logs;
		private JLabel logo;
		
		private int question_size = 14;	// Font size of "How many players?" etc
		private Border error_border = BorderFactory.createLineBorder(Color.red, 3);	// Invalid input causes a red border
		private Border original_border;	// for red border removal
		
		public SetUpPanel() {
			num_games_question = new JLabel("How many games should the bots simulate?");
			num_games_question.setForeground(Color.white);
			num_games_question.setFont(FontMaker.makeCustomFont(question_size));
			
			watch_none_label = new JLabel("Don't watch any games");
			watch_none_label.setForeground(Color.white);
			watch_none_label.setFont(FontMaker.makeCustomFont(question_size));
			
			watch_one_label = new JLabel("Watch the first game");
			watch_one_label.setForeground(Color.white);
			watch_one_label.setFont(FontMaker.makeCustomFont(question_size));
			
			watch_all_label = new JLabel("Watch all games");
			watch_all_label.setForeground(Color.white);
			watch_all_label.setFont(FontMaker.makeCustomFont(question_size));
			
			save_file_label = new JLabel("Save results to file: ");
			save_file_label.setForeground(Color.white);
			save_file_label.setFont(FontMaker.makeCustomFont(question_size));
			
			results_file_path = new JLabel(Risk.WAR_GAME_LOG_PATH);
			results_file_path.setForeground(Color.white);
			results_file_path.setFont(FontMaker.makeCustomFont(question_size));
			
			game_logs_label = new JLabel("Keep logs of individual games: ");
			game_logs_label.setForeground(Color.white);
			game_logs_label.setFont(FontMaker.makeCustomFont(question_size));
			
			logo = new JLabel("",new ImageIcon(Risk.LOGO_URL), JLabel.CENTER);
			num_games = new JTextField("1", 3);	// Text input for the number of games
			save_file = new JTextField("",8);	// Text input for the file to save results in
			save_file.setText(guessResultsFile());
			if(original_border == null)		// In order to "clear" an error border, the original border is saved
				original_border = num_games.getBorder();
			button_group = new ButtonGroup();
			watch_none = new JRadioButton();
			watch_one = new JRadioButton();
			watch_all = new JRadioButton();
			watch_none.setSelected(true);	// By default, watch no games
			button_group.add(watch_none);
			button_group.add(watch_all);
			button_group.add(watch_one);
			game_logs = new JCheckBox();	// Whether or not to keep game logs
			
			// Start button
			start_button = new JButton("Start");
			start_button.addActionListener(new StartAction());
			
			initUI();
			pack();
		}
		
		// Draw the WarGameSetUp window using a GroupLayout
		private void initUI() {
			GroupLayout layout = new GroupLayout(this);
			this.setBackground(Risk.UGLY_GREY);
			this.setLayout(layout);
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			// Turn on automatically added gaps between components
			layout.setAutoCreateGaps(true);

			// Horizontal group
			GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
			hGroup.addComponent(logo);
			hGroup.addGroup(layout.createSequentialGroup().addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
							.addComponent(num_games_question)
							.addComponent(watch_none_label)
							.addComponent(watch_one_label)
							.addComponent(watch_all_label)
							.addComponent(save_file_label)
							.addComponent(game_logs_label))
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
							.addGroup(layout.createSequentialGroup().addComponent(num_games)
									.addPreferredGap(
											LayoutStyle.ComponentPlacement.RELATED,
											GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addComponent(watch_none).addComponent(watch_one).addComponent(watch_all)
					.addGroup(layout.createSequentialGroup().addComponent(results_file_path).addComponent(save_file)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
									GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGroup(layout.createSequentialGroup().addComponent(game_logs).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					);
			
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
			vGroup.addComponent(logo);
			vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,30, 30);
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
							.addComponent(results_file_path)
							.addComponent(save_file));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(game_logs_label)
					.addComponent(game_logs));
			vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(start_button);
			layout.setVerticalGroup(vGroup);
		}
		
		// The action used when the Start button is pressed.
		// Validates input, then calls the startClicked method
		private class StartAction extends AbstractAction {
			public void actionPerformed(ActionEvent ae) {
				// Clear borders
				num_games.setBorder(original_border);
				save_file.setBorder(original_border);
				boolean should_continue = true;		// If this gets set to false, there was a problem with the input
				Integer parsed_num_games = new Integer(1);	// Dummy value initialization
				try {
					parsed_num_games = new Integer(num_games.getText());
				} catch(NumberFormatException e) {	// If not actually a number
					num_games.setBorder(error_border);
					should_continue = false;
				}
				if(parsed_num_games != null && parsed_num_games.intValue() < 1) {	// If the number of games is < 1
					num_games.setBorder(error_border);
					should_continue = false;
				}
				File f = new File(Risk.WAR_GAME_LOG_PATH + save_file.getText());
				if(f.exists()) {		// If the given results file name already exists
					Risk.sayError("War game results file " + save_file.getText() + " already exists in " + Risk.WAR_GAME_LOG_PATH);
					should_continue = false;
					save_file.setBorder(error_border);
				}
				
				int m = -1;		// Watch mode
				if(watch_none.isSelected()) m = 0;
				if(watch_one.isSelected()) m = 1;
				if(watch_all.isSelected()) m = 2;
				if(m < 0) {
					Risk.sayError("Unidentified mode in WarGamesSetUp");
					System.exit(-1);
				}
				if(should_continue)		// If no problem was encountered with the input
					startClicked(parsed_num_games.intValue(), m, save_file.getText(), game_logs.isSelected());
			}
		}
		
		// So that the user doesn't have to supply a results file name, a possible one is pre-generated
		private String guessResultsFile() {
			String logp = Risk.WAR_GAME_LOG_PATH; // path of the war game logs directory
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = sdf.format(cal.getTime());
			logp += dateStr;	// Add the current date to the file name
			int unique = 1;	// To ensure it's unique, keep adding a number until it is
			while(true) {
				File f = new File(logp + "-" + unique);
				if(!f.exists())
					break;
				unique++;
			}
			return dateStr + "-" + unique;
		}
		
	}
	
}
