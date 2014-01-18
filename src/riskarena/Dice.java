/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * The Dice class simulates the rolling of attack/defender dice.
 * The constructor chooses a random float from 0-1, and uses the
 * probabilities found on wikipedia to determine how many armies are
 * lost. These results are set by change attackerArmyChange and defenderArmyChange.
 * 
 * Evan Radkoff
 */

import java.util.Random;

public class Dice {
	public int attackerArmyChange;
	public int defenderArmyChange;

	/*
	 * @param attack_dice The number of dice the attacker is rolling
	 * @param def_dice The number of dice the defender is rolling
	 */
	public Dice(Random gen, int attack_dice, int def_dice) {
		attackerArmyChange = 0;
		defenderArmyChange = 0;

		float dice_roll = gen.nextFloat();

		switch(attack_dice) {
		case 1:
			if(def_dice == 1) {
				if(dice_roll < .4167) {
					defenderArmyChange = -1;
				} else attackerArmyChange = -1;
			} else {
				if(dice_roll < .2546) {
					defenderArmyChange = -1;
				} else attackerArmyChange = -1;
			}
			break;
		case 2:
			if(def_dice == 1) {
				if(dice_roll < .5787) {
					defenderArmyChange = -1;
				} else attackerArmyChange = -1;
			} else {
				if(dice_roll < .2276) {
					defenderArmyChange = -2;
				} else if(dice_roll < .6759) {
					attackerArmyChange = -2;
				} else {
					defenderArmyChange = -1;
					attackerArmyChange = -1;
				}
			}
			break;
		case 3:
			if(def_dice == 1) {
				if(dice_roll < .6597) {
					defenderArmyChange = -1;
				} else attackerArmyChange = -1;
			} else {
				if(dice_roll < .3717) {
					defenderArmyChange = -2;
				} else if(dice_roll < .6643) {
					attackerArmyChange = -2;
				} else {
					defenderArmyChange = -1;
					attackerArmyChange = -1;
				}
			}
			break;
		default:
			Risk.sayError("\t" + attack_dice + " is not a valid number of attacking dice.");
		}
	}
}
