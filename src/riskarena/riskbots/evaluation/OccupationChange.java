/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation;

public class OccupationChange {
	private int from;
	private int to;
	private int casualties;
	private int enemiesKilled;
	
	public OccupationChange(int from, int to, int casualties, int enemiesKilled) {
		this.from = from;
		this.to = to;
		this.casualties = casualties;
		this.enemiesKilled = enemiesKilled;
	}
	
	public int from() {
		return from;
	}
	public int to() {
		return to;
	}
	public int casualties() {
		return casualties;
	}
	public int enemiesKilled() {
		return enemiesKilled;
	}

}
