package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;

public class FortifyAfterVictoryDecision {
	private Evaluation eval;
	double highest;
	int answer = -1;
	
	public FortifyAfterVictoryDecision(Evaluation _eval) {
		eval = _eval;
	}
	
	/*
	 * Tries three options to see which is best - move the min number in,
	 * move the max number in, and move the average of the two.
	 */
	public int decide(int from, int to, int min, int max) {
		eval.refresh("decide() in FortifyAfterVictoryDecision");
		highest = -1*Double.MAX_VALUE;
		answer = -1;
		int middle = (min + max) / 2;
		
		tryNum(from, to, min);
		tryNum(from, to, max);
		if(middle != min && middle != max)
			tryNum(from, to, middle);
		
		return answer;
	}
	
	private void tryNum(int from, int to, int num) {
		ArrayList<ArmyChange> changes = new ArrayList<ArmyChange>();
		changes.add( new ArmyChange(from, -1*num) );
		changes.add( new ArmyChange(to, num) );
		double score = eval.score(changes);
		if(score > highest) {
			highest = score;
			answer = num;
		}
	}
	
}
