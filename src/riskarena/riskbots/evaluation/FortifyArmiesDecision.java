package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;

public class FortifyArmiesDecision {
	private GameInfo game;
	private Evaluation eval;
	
	public FortifyArmiesDecision(GameInfo _game, Evaluation _eval) {
		game = _game;
		eval = _eval;
	}
	
	public ArrayList<ArmyChange> decideAll(int numToPlace) {
		eval.refresh();
		ArrayList<ArmyChange> winner = null;
		double highest = -1*Double.MAX_VALUE;
		CountryInfo countries[] = game.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			if(countries[i].getPlayer() != game.me())
				continue;
			ArmyChange change = new ArmyChange(i, numToPlace);
			ArrayList<ArmyChange> changes = new ArrayList<ArmyChange>();
			changes.add(change);
			double score = eval.score(changes);
			//Risk.sayOutput(countries[i].getName() + ": " + score, OutputFormat.BLUE);
			if(score > highest) {
				highest = score;
				winner = new ArrayList<ArmyChange>(changes.size());
				winner.add(new ArmyChange(changes.get(0)));
			}
			
			// Try half n half
			int firsthalf = Math.max(numToPlace/2, 1);
			int secondhalf = numToPlace - firsthalf;
			if(secondhalf == 0) continue;
			for(int j=0; j<countries.length; j++) {
				if(countries[j].getPlayer() != game.me() || i == j) {
					continue;
				}
				ArmyChange one = new ArmyChange(i, firsthalf), two = new ArmyChange(j, secondhalf);
				changes = new ArrayList<ArmyChange>();
				changes.add(one);
				changes.add(two);
				score = eval.score(changes);
				//Risk.sayOutput(countries[i].getName() + ": " + score, OutputFormat.BLUE);
				if(score > highest) {
					highest = score;
					winner = new ArrayList<ArmyChange>(changes.size());
					winner.add(new ArmyChange(changes.get(0)));
					winner.add(new ArmyChange(changes.get(1)));
				}
			}
		}
		return winner;
	}

}
