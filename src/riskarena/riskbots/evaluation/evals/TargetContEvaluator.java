package riskarena.riskbots.evaluation.evals;
/*
 * TargetContEvaluator uses the "target" continent decided in GameStats to give score
 * increases to actions that result in a focus on that continent. Its general score is always 0.0
 */

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;

public class TargetContEvaluator extends AbstractEvaluator {
	int armiesInTarget;
	public TargetContEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		//recalculate();
	}
	
	public double getScore() {
		return 0.0;
	}
	
	/*
	 * Finds the net favorable change of armies in the target continent, normalizes by the number
	 * of armies in the continent before.
	 * @see riskarena.riskbots.evaluation.evals.AbstractEvaluator#getScore(java.util.ArrayList)
	 */
	public double getScore(ArrayList<ArmyChange> changes) {
		double net = 0;
		CountryInfo countries[] = stats.getCountries();
		for(ArmyChange change : changes) {
			if(countries[change.ID()].getCont() == stats.getTarget()) {
				if(countries[change.ID()].getPlayer() == game.me()) {
					double bonusMultiplier = 1.0;
					if(change.amount() > 0) {
						// Give bonus if there's an enemy next door in the target continent (for attack planning)
						int adj[] = stats.getWorld().getAdjacencies(change.ID());
						for(int a = 0; a<adj.length; a++) {
							if(countries[adj[a]].getCont() == stats.getTarget() && countries[adj[a]].getPlayer() != game.me())
								bonusMultiplier = 1.5;
						}
					}
					net += bonusMultiplier * change.amount();
				} else {
					net -= change.amount();
				}
			}
		}
		double result = net / (double)armiesInTarget;
		return result;
	}
	
	public void refresh() {
		armiesInTarget = 0;
		CountryInfo countries[] = stats.getCountries();
		for(int i=0; i<game.getNumCountries(); i++) {
			if(countries[i].getCont() == stats.getTarget())
				armiesInTarget += countries[i].getArmies();
		}
	}
	
}
