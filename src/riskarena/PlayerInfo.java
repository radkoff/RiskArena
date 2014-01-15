/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * PlayerInfo is a class that provides non-private player info
 * to Bots. Think of it as anything you can see about another player during a game:
 * their name, whether they're a bot or human, and their number of cards.
 * 
 * Evan Radkoff
 */

interface PlayerInterface {
	public String getName();
	public int getType();
	public int getNumCards();
	public int getId();
}

public class PlayerInfo implements PlayerInterface {
	private final PlayerInterface player;
	public PlayerInfo(PlayerInterface wrapped) {
		player = wrapped;
	}
	public String getName() {
		return player.getName();
	}
	public int getType() {
		return player.getType();
	}
	public int getNumCards() {
		return player.getNumCards();
	}
	public int getId() {
		return player.getId();
	}
}
