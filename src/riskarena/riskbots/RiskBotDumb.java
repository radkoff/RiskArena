package riskarena.riskbots;
/*
 * A dumb RiskBot used for testing purposes. See HOWTO or RiskBot.java for more on what these methods do.
 * 
 * Evan Radkoff
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import riskarena.Bot;
import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.PlayerInfo;
import riskarena.RiskBot;
import riskarena.World;
import riskarena.Bot.RiskListener;
import riskarena.riskbots.evaluation.CardIndicator;
import riskarena.riskbots.evaluation.Evaluation;

public class RiskBotDumb implements RiskBot{
	private Bot.RiskListener to_game;
	private GameInfo risk_info;
	private World world = null;
	private PlayerInfo[] players = null;
	private CardIndicator card;

	Random gen;

	// Initialize the bot, locally store the given instance of GameInfo so that we can
	// get board info any time we want, as well as a RiskListener so we can communicate our answers.
	public void init(GameInfo gi, Bot.RiskListener rl) {
		risk_info = gi;
		to_game = rl;
		world = risk_info.getWorldInfo();
		players = risk_info.getPlayerInfo();
		gen = new Random((new Date()).getTime());
		//gen = new Random(5);
		card = new CardIndicator();
	}
	
	/*
	 * Turn-based initialization
	 * @see riskarena.RiskBot#initTurn()
	 */
	public void initTurn() {
		card.setVictory(false);
	}

	// Claim the country in the continent with the highest percentage of claimed friendly countries
	public void claimTerritory() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		int num_conts = risk_info.getNumContinents();
		
		// Else, calculate percentages for each continent of: taken by me, taken by someone, unclaimed
		int counts[][] = new int[num_conts][3];
		for(int i=0;i<num_conts;i++) {
			for(int j=0;j<3;j++) {
				counts[i][j] = 0;
			}
		}
		for(int i=0;i<countries.length;i++) {
			if(!countries[i].isTaken()) {
				counts[countries[i].getCont()][2] += 1;
			} else {
				if(countries[i].getPlayer() == risk_info.me())
					counts[countries[i].getCont()][0] += 1;
				else
					counts[countries[i].getCont()][1] += 1;
			}
		}
		double ratios[][] = new double[num_conts][3];
		for(int i=0;i<num_conts;i++) {
			for(int j=0;j<3;j++) {
				ratios[i][j] = ((double)counts[i][j])/(counts[i][0] + counts[i][1] + counts[i][2]);
			}
		}
		
		
		int cont = -1;
		double highest = 0.0;
		for(int i=0;i<ratios.length;i++) {
			if(ratios[i][2] < .00001)
				continue;
			if(ratios[i][1] > .2)
				continue;
			if(ratios[i][0] > highest) {
				highest = ratios[i][0];
				cont = i;
			}
		}
		if(cont != -1) {
			for(int i=0;i<countries.length;i++) {
				if(!countries[i].isTaken() && countries[i].getCont() == cont) {
					to_game.sendInt(i);
					return;
				}
			}
		}
		double lowest = 1.0;
		for(int i=0;i<ratios.length;i++) {
			if(ratios[i][2] < .00001)
				continue;
			if(ratios[i][1] < lowest) {
				lowest = ratios[i][1];
				cont = i;
			}
		}
		for(int i=0;i<countries.length;i++) {
			if(!countries[i].isTaken() && countries[i].getCont() == cont) {
				to_game.sendInt(i);
				return;
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
		card.setVictory(true);
		to_game.sendInt(max);
	}

	public void chooseToTurnInSet() {
		to_game.sendInt(1);
	}

	public void chooseCardSet(int[][] possible_sets) {
		to_game.sendInt(0);
	}

	public void fortifyPosition() {
		CountryInfo[] countries = risk_info.getCountryInfo();
		for(int i=0;i<countries.length;i++) {
			if(countries[i].getPlayer() == risk_info.me() && countries[i].getArmies() > 1) {
				int[] adj = world.getAdjacencies(i);
				/*System.out.println("Adjacencies of " + countries[i].getName() + ": ");
				for(int j=0;j<adj.length;j++)
					System.out.println("\t" + countries[adj[j]].getName());*/
				for(int j=0;j<adj.length;j++) {
					if(countries[adj[j]].getPlayer() == risk_info.me() && countries[adj[j]].getArmies() > countries[i].getArmies()) {
						to_game.sendInt(adj[j]);
						to_game.sendInt(i);
						to_game.sendInt(countries[adj[j]].getArmies()-1);
						return;
					}
				}
			}
		}
		to_game.sendInt(-1);
	}

	@Override
	public void endTurn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endGame(int place) {
		// TODO Auto-generated method stub
		
	}

}
