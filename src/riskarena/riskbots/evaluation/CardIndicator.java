/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
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
