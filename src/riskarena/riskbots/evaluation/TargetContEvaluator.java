package riskarena.riskbots.evaluation;
/*
 * TargetContEvaluator uses the "target" continent decided in GameStats to give score
 * increases to actions that result in a focus on that continent. Its general score is always 0.0
 */

import riskarena.GameInfo;

public class TargetContEvaluator extends AbstractEvaluator {
	public TargetContEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		//recalculate();
	}
	
	public double getScore() {
		return 0.0;
	}
	
	public void refresh() {
		//recalculate();
	}
	
}
