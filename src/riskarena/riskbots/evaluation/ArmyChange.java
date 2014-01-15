/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
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
	public ArmyChange(ArmyChange other) {
		ID = other.ID();
		amount = other.amount();
	}
	
	public int ID() { return ID; }
	public int amount() { return amount; }
}
