package riskarena.riskbots;
/*
 * An "Evaluation" player that makes decisions by evaluating a score for the game states
 * that result from each possible choice. The decision making is done in classes found in the
 * riskarena.riskbots.evaluation package, and the state scoring in Evaluation.java
 * Evaluation consists of a weighted sum of smaller scores that each evaluate an aspect
 * of the game state (see riskarena.riskbots.evaluation.evals)
 * The weights of these scores are central to the behavior of AwesomeBot.
 * If there is a weights file named "Awesome.txt" present in src/data/weights/, it will read what weights to use
 * from the last line of that file. If there is not, random weights will be chosen.
 * If "shouldLearn" is set to true, new weights are trained using TD(lambda) learning and new lines to the
 * weights file are written. For more see WeightManager.java
 * 
 * Evan Radkoff
 * 
 * Much of the strategy of this player was inspired by "An Intelligent Artificial Player for the Game of Risk"
 * by Michael Wolf http://www.ke.tu-darmstadt.de/bibtex/publications/show/1302
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import riskarena.Bot;
import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.PlayerInfo;
import riskarena.Risk;
import riskarena.RiskBot;
import riskarena.World;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.AttackDecision;
import riskarena.riskbots.evaluation.CardIndicator;
import riskarena.riskbots.evaluation.Evaluation;
import riskarena.riskbots.evaluation.FortifyAfterVictoryDecision;
import riskarena.riskbots.evaluation.FortifyArmiesDecision;
import riskarena.riskbots.evaluation.FortifyPositionDecision;

public class RiskBotAwesome implements RiskBot{
	/*	Game related data members it's always a good idea to keep */
	private Bot.RiskListener to_game;		// Send game time decisions using to_game.sendInt(int/Integer)
	private GameInfo risk_info;
	private World world = null;				// Holds adjacency information
	private PlayerInfo[] players = null;
	
	/* Data members specific to this particular RiskBot */
	private Evaluation eval;
	private CardIndicator card;
	private final boolean shouldLearn = false;
	
	/*	Decision-makers	*/
	private FortifyArmiesDecision fortifier;
	private FortifyAfterVictoryDecision afterVictory;
	private FortifyPositionDecision posFortifier;
	private AttackDecision attackDecider;

	/*
	 * Initialize the bot, locally store the given instance of GameInfo so that we can
	 * get board info any time we want, as well as a RiskListener so we can communicate our answers.
	 * @see riskarena.RiskBot#init(riskarena.GameInfo, riskarena.Bot.RiskListener)
	 */
	public void init(GameInfo gi, Bot.RiskListener rl) {
		risk_info = gi;
		to_game = rl;
		world = risk_info.getWorldInfo();
		players = risk_info.getPlayerInfo();
		card = new CardIndicator();
		eval = new Evaluation(risk_info, card, shouldLearn);
		fortifier = new FortifyArmiesDecision(risk_info, eval);
		afterVictory = new FortifyAfterVictoryDecision(eval);
		posFortifier = new FortifyPositionDecision(risk_info, eval);
		attackDecider = new AttackDecision(risk_info, eval);
	}
	
	/*
	 * Turn-based initialization
	 * @see riskarena.RiskBot#initTurn()
	 */
	public void initTurn() {
		card.setVictory(false);
		eval.refresh("initTurn() in Awesome");
	}
	
	/*
	 * Called every time this player's turn is over.
	 * @see riskarena.RiskBot#endTurn()
	 */
	public void endTurn() {
		eval.endTurn();
	}
	
	/*
	 * Called when the game is over so that new training weights can be saved
	 * @see riskarena.RiskBot#endGame(int)
	 */
	public void endGame(int place) {
		eval.endGame(place);
	}

	/*
	 * Claim an unclaimed territory. At the moment this functionality is the same as
	 * the Dumb player, and evaluation isn't used.
	 * @see riskarena.RiskBot#claimTerritory()
	 */
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

	/*
	 * Use a FortifyArmiesDecision to make this choice
	 * @see riskarena.RiskBot#fortifyTerritory(int)
	 */
	public void fortifyTerritory(int num_to_place) {
		eval.refresh("fortifyTerritory() in Awesome");
		ArrayList< ArmyChange > choices = fortifier.decideAll(num_to_place);
		attackDecider.initTurn();	// New attack targets are chosen when new territories are being placed
		for(ArmyChange choice : choices) {
			to_game.sendInt(choice.ID());
			to_game.sendInt(choice.amount());
		}
	}

	/*
	 * Called to send the decision of which territory to attack (if any).
	 * @see riskarena.RiskBot#launchAttack()
	 */
	public void launchAttack() {
		eval.refresh("launchAttack() in Awesome");	// Refresh the evaluation of the game state
		for(Integer toSend : attackDecider.decide()) {
			to_game.sendInt(toSend);
		}
	}

	/*
	 * After a victory, always choose to occupy the gained territory with as many armies as possible 
	 * @see riskarena.RiskBot#fortifyAfterVictory(int, int, int, int)
	 */
	public void fortifyAfterVictory(int attacker, int defender, int min, int max) {
		// Consider attacking again with the victorious army
		attackDecider.notifyOfVictory(attacker, defender);
		card.setVictory(true);
		to_game.sendInt( afterVictory.decide(attacker, defender, min, max) );
	}

	/*
	 * Yes, always choose to turn in a set when given a choice
	 * @see riskarena.RiskBot#chooseToTurnInSet()
	 */
	public void chooseToTurnInSet() {
		to_game.sendInt(1);
	}

	/*
	 * Choose the first possible card set given
	 * @see riskarena.RiskBot#chooseCardSet(int[][])
	 */
	public void chooseCardSet(int[][] possible_sets) {
		to_game.sendInt(0);
	}

	/*
	 * @see riskarena.RiskBot#fortifyPosition()
	 */
	public void fortifyPosition() {
		eval.refresh("fortifyPosition() in Awesome");
		for(Integer i : posFortifier.decide()) {
			to_game.sendInt(i);
		}
	}

}
