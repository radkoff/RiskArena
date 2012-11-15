/*
 * The BotSniffer class looks through the given botpath directory
 * for all java files beginning with some prefix (Risk.RISKBOT_PREFIX).
 * Whatever passes the test of the botChecker(file) method is added to an
 * ArrayList called "good_bots" that is available publicly via getBots()
 * 
 * Evan Radkoff
 */

import java.io.File;
import java.util.ArrayList;

public class BotSniffer {
	private ArrayList<String> good_bots;

	public BotSniffer(String botpath) {
		good_bots = new ArrayList<String>();
		File folder = new File(botpath);
		File[] listOfFiles = folder.listFiles(); 

		// Loop through all files found in botpath
		for (int i = 0; i < listOfFiles.length; i++) 
		{
			if (listOfFiles[i].isFile()) 	// excludes directories
			{
				String filename = listOfFiles[i].getName();
				if (botChecker(filename))
				{
					good_bots.add(filename.substring(Risk.RISKBOT_PREFIX.length(), filename.length()-5));	// trim the prefix and ".java"
				}
			}
		}
	}

	// Checks if a file passes the tests to be considered as an AI agent.
	// If this returns true it passed, else it did not.
	private boolean botChecker(String bot_file) {
		// Does the file start with Risk.RISKBOT_PREFIX?
		if(!bot_file.substring(0, Risk.RISKBOT_PREFIX.length()).equals(Risk.RISKBOT_PREFIX))
			return false;
		// Is it a .java file?
		if(!bot_file.toLowerCase().endsWith(".java"))
			return false;
		// Is the name something more than just "Risk.RISKBOT_PREFIX + .java"?
		if(bot_file.length() <= Risk.RISKBOT_PREFIX.length() + 5)
			return false;

		// TODO Check the source code of the bot. Perhaps someday compile it,
		//	and use test cases to make sure it works and doesn't cheat

		return true;
	}

	// Returns an ArrayList of the accepted bot's names
	public ArrayList<String> getBots() {
		return good_bots;
	}
}