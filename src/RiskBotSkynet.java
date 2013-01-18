/*
 * A dumb RiskBot used for testing purposes. See HOWTO or RiskBot.java for more on what these methods do.
 * 
 * Evan Radkoff
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class RiskBotSkynet implements RiskBot{
	private Bot.RiskListener to_game;
	private GameInfo risk_info;
	private World world = null;
	private PlayerInfo[] players = null;

	Random gen; // Random number generator used for some decisions

	// Initialize the bot, locally store the given instance of GameInfo so that we can
	// get board info any time we want, as well as a RiskListener so we can communicate our answers.
	public void init(GameInfo gi, Bot.RiskListener rl) {
		risk_info = gi;
		to_game = rl;
		world = risk_info.getWorldInfo();
		players = risk_info.getPlayerInfo();
		gen = new Random((new Date()).getTime());
	}

	// Claim the very first country that is unclaimed
	public void claimTerritory() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		for(int i=0;i<countries.length;i++) {
			if(!countries[i].isTaken()) {
				to_game.sendInt(i);
				break;
			}
		}
	}

	// Fortify a random territory with all armies that need placement
	public void fortifyTerritory(int num_to_place) {
		CountryInfo[] countries = risk_info.getCountryInfo();
		ArrayList<Integer> mine = new ArrayList<Integer>();

		for(int i=0;i<countries.length;i++) {
			if(countries[i].getPlayer() == risk_info.me()) {
				mine.add(new Integer(i));
			}
		}
		int choice = gen.nextInt(mine.size());

		to_game.sendInt(mine.get(choice).intValue());
		to_game.sendInt(num_to_place);
	}

	public void launchAttack() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		for(int i=0;i<countries.length;i++) {
			if(countries[i].getPlayer() == risk_info.me() && countries[i].getArmies() > 4) {
				int[] adj = world.getAdjacencies(i);
				for(int j=0;j<adj.length;j++) {
					if(countries[adj[j]].getPlayer() != risk_info.me()) {
						//System.out.println("LOLOLOLOL ATTACKING FROM " + countries[i].getName() + "(" + countries[i].getArmies() + " armies) to " + countries[adj[j]].getName());
						to_game.sendInt(i);
						to_game.sendInt(adj[j]);
						to_game.sendInt(Math.min(countries[i].getArmies()-1, 3));
						return;
					}
				}
			}
		}
		to_game.sendInt(-1);
	}

	public void fortifyAfterVictory(int attacker, int defender, int min, int max) {
		to_game.sendInt(max);
	}

	public void chooseToTurnInSet() {
		to_game.sendInt(1);
	}

	public void chooseCardSet(int[][] possible_sets) {
		to_game.sendInt(0);
	}

	public void fortifyPosition() {
		to_game.sendInt(-1);
	}

}
