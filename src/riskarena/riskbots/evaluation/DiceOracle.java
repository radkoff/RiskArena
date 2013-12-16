package riskarena.riskbots.evaluation;

import riskarena.Risk;
/*
 * Returns the probability of a certain outcome of a dice-rolling battle
 */

public class DiceOracle {
	/*
	 * A - number of dice rolled by the attacker
	 * D - number of dice rolled by the defender
	 * dloss - the number of armies lost by the defender
	 */
	public static double odds(int A, int D, int dloss) {
		if(A < 1 || A > 3)
			Risk.sayError("DiceOracle: cannot roll " + A + " attacker dice");
		if(D < 1 || D > 2)
			Risk.sayError("DiceOracle: cannot roll " + D + " attacker dice");
		if(dloss < 0 || dloss > 2)
			Risk.sayError("DiceOracle: invalid defender loss of " + dloss);
		double result = 0.0;
		switch(A) {
		case 1:
			switch(D) {
			case 1: if(dloss == 0) result = .583;
			else result = .417;
			case 2: if(dloss == 0) result = .745;
			else result = .255;
			}
		case 2:
			switch(D) {
			case 1: if(dloss == 0) result = .421;
			else result = .579;
			case 2: if(dloss == 0) result = .448;
			else if (dloss == 1) result = .324;
			else result = .228;
			}
		case 3:
			switch(D) {
			case 1: if(dloss == 0) result = .340;
			else result = .660;
			case 2: if(dloss == 0) result = .293;
			else if (dloss == 1) result = .336;
			else result = .372;
			}
		}
		return result;
	}
}
