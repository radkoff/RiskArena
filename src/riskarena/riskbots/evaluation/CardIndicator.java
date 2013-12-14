package riskarena.riskbots.evaluation;
/*
 * Simple class with a flag indicating whether or not a player has won a victory during a turn
 * (indicating whether or not they will pick up a card at the end of the turn)
 */

public class CardIndicator {
	private boolean victory;
	
	public void setVictory(boolean val) {
		victory = val;
	}
	public boolean getVictory() {
		return victory;
	}
}
