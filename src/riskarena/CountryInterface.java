/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available in LICENSE.txt or at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;

public interface CountryInterface {
	public String getName();
	public int getCont();
	public int getPlayer();
	public int getArmies();
	public boolean isTaken();
}
