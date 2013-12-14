package riskarena.riskbots.evaluation;
/*
 * Represents a potential change in army amounts on the game board. Let Evaluators
 * re-evaluate their score without having to create a whole new game state. 
 */

public class ArmyChange {
	private int ID, amount;
	public ArmyChange(int _ID, int _amount) {
		ID = _ID;
		amount = _amount;
	}
	
	public int ID() { return ID; }
	public int amount() { return amount; }
}
