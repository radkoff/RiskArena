/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena;
/*
 * The Deck class represents a deck of Risk cards during a game of Risk.
 * It allows for the creation of a deck, the ability to draw a card,
 * and the ability to add turned in cards back into the deck.
 * Contains 14 infantry, horse, and cannon cards, as well as two wildcards.
 * 
 * Evan Radkoff
 */

import java.util.*;

public class Deck {
	private List<Integer> cardz; // cards in deck
	private Random gen;

	/* Deck() constructs a deck object by supplying cardz[] with ints
	 	These ints represent the card type */
	public Deck(Random rand) {
		gen = rand;
		cardz = new ArrayList<Integer>();
		for(int i=0;i<14;i++) {
			cardz.add(new Integer(0)); // infantry
			cardz.add(new Integer(1)); // horse
			cardz.add(new Integer(2)); // cannon
		}
		cardz.add(new Integer(3)); // wildcard
		cardz.add(new Integer(3)); // wildcard
	}

	/* 
	 * Draw a card from the deck. If no cards are left, return -1
	 * The returning int indicates the card type.
	 * 0 - Infantry. 1 - Horse. 2 - Cannon. 3 - Wildcard
	 */
	public int drawCard() {
		if(cardz.size() == 0)
			return -1;
		int an_index = gen.nextInt(cardz.size());
		int a_card = cardz.get(an_index);
		cardz.remove(an_index);
		return a_card;
	}

	/*
	 * When cards (3 at a time) are played, they are added
	 * back to the deck. This method accomplishes this by appending cardz
	 */
	public void addCards(int[] toAdd) {
		for(int i=0; i<toAdd.length;i++)
			cardz.add(new Integer(toAdd[i]));
	}

	/*
	 * Returns the type of card int 'type' represents
	 */
	public String getCardType(int type) {
		switch(type) {
		case 0:	return "infantry";
		case 1:	return "cavalry";
		case 2: return "artillery";
		case 3: return "wildcard";
		default:
			Risk.sayError("Invalid card type.");
			return "";
		}
	}

	// Only used in possibleCardTriples because ArrayList<int[]> is not allowed
	private class CardSet {
		public CardSet(int a_, int b_, int c_) {
			a = a_; b = b_; c = c_;
		}
		public int a, b, c;
	}

	/*
	 * Calculates all possible triples that a player with cards[] could turn in.
	 * @return int[num_possibilities][3] where the 3 is the card type (0-3).
	 * Thus turning in a set of 3 different cards might be: [0, 1, 2]. Or for three Infantry: [0, 0, 0].
	 * 
	 * This does not reference any other data members or methods within the Deck class, so it's really
	 * more of a utility method. It's placed here though because it's related to cards.
	 */
	public int[][] possibleCardTriples(int cards[]) {
		ArrayList<CardSet> trips = new ArrayList<CardSet>();
		if(cards[0]>0 && cards[1]>0 && cards[2]>0)	// Checks for a I-C-A set (one of each)
			trips.add(new CardSet(0,1,2));
		if(cards[0]>=3)	// Checks for a III set (all infantry)
			trips.add(new CardSet(0,0,0));
		if(cards[1]>=3)	// Checks for a CCC set
			trips.add(new CardSet(1,1,1));
		if(cards[2]>=3)	// Checks for a AAA set
			trips.add(new CardSet(2,2,2));

		// The rest of these involve a wildcard, which can become any type
		if(cards[3]>=1) {
			if(cards[0] >= 2)
				trips.add(new CardSet(0,0,3));
			if(cards[1] >= 2)
				trips.add(new CardSet(1,1,3));
			if(cards[2] >= 2)
				trips.add(new CardSet(2,2,3));
			if(cards[0] >= 1 && cards[1] >= 1)
				trips.add(new CardSet(0,1,3));
			if(cards[0] >= 1 && cards[2] >= 1)
				trips.add(new CardSet(0,2,3));
			if(cards[1] >= 1 && cards[2] >= 1)
				trips.add(new CardSet(1,2,3));
		}
		if(cards[3] >= 2) {
			if(cards[0] >= 1)
				trips.add(new CardSet(0,0,3));
			if(cards[1] >= 1)
				trips.add(new CardSet(1,1,3));
			if(cards[2] >= 1)
				trips.add(new CardSet(2,2,3));
		}
		if(cards[3] >= 3)
			trips.add(new CardSet(3,3,3));
		int ret[][] = new int[trips.size()][3];
		for(int i=0;i<trips.size();i++) {
			int temp[] = new int[3];
			temp[0] = trips.get(i).a; temp[1] = trips.get(i).b; temp[2] = trips.get(i).c;
			ret[i] = temp;
		}
		return ret;
	}

}
