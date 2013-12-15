package riskarena.riskbots.evaluation.evals;
/*
 * TargetContEvaluator uses the "target" continent decided in GameStats to give score
 * increases to actions that result in a focus on that continent. Its general score is always 0.0
 */

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
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
		int net = 0;
		CountryInfo countries[] = stats.getCountries();
		for(ArmyChange change : changes) {
			if(countries[change.ID()].getCont() == stats.getTarget()) {
				if(countries[change.ID()].getPlayer() == game.me()) {
					net += change.amount();
				} else {
					net -= change.amount();
				}
			}
		}
		return net / (double)armiesInTarget;
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
