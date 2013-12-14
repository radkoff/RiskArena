package riskarena.riskbots.evaluation.evals;
/*
 * The FrontierDistanceEvaluator uses the distance from each friendly territory
 * to the frontier (the set of friendly territories neighboring enemies)
 * It calculates the average distance it takes for each "moveable" army (ie all except the single ones
 * that must always be stationed at a territory) to get to the frontier.
 * The formula is: (moveableArmies * maxFrontierDist - sumOverFriendlyTerritories[ #moveableArmiesinTerritory * frontierDist ])
 * 								/ TotalFriendlyArmies * maxFrontierDist
 * where moveableArmies = TotalFriendlyArmies - numFriendlyTerritories
 */

import java.util.ArrayDeque;

import com.sun.tools.javac.util.Pair;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.OutputFormat;
import riskarena.Risk;
import riskarena.riskbots.evaluation.GameStats;

public class FrontierDistanceEvaluator extends AbstractEvaluator {
	private double score;
	private int distances[];
	private int maxDist;
	
	public FrontierDistanceEvaluator(String name, double weight, GameStats stats, GameInfo game) {
		super(name, weight, stats, game);
		recalculate();
	}
	
	public double getScore() {
		return score;
	}
	
	public void refresh() {
		recalculate();
	}
	
	// Sets the score variable
	private void recalculate() {
		calculateFrontierDistances();
		int added = 0;
		for(Integer friendly : stats.getMyCountries()) {
			added += (stats.getCountries()[friendly].getArmies()-1) * distances[friendly];
		}
		int worstScore = (stats.getArmiesPerPlayer()[game.me()] - stats.getMyCountries().size()) * maxDist;
		if(worstScore == 0)
			score = 1.0;		// Everything's on the frontier
		else
			score = (worstScore - added) / (double)worstScore;
	}
	
	private void calculateFrontierDistances() {
		distances = new int[game.getNumCountries()];
		maxDist = Integer.MIN_VALUE;
		
		CountryInfo countries[] = stats.getCountries();
		java.util.Arrays.fill(distances, Integer.MAX_VALUE);
		ArrayDeque< Pair<Integer, Integer> > Q = new ArrayDeque< Pair<Integer, Integer> >();
		for(Integer frontier : stats.getFrontier()) {
			Q.push( new Pair<Integer, Integer>(frontier, 0) );
		}
		while(!Q.isEmpty()) {
			Pair<Integer, Integer> current = Q.pop();
			if(countries[current.fst].getPlayer() != game.me())
				continue;
			if(distances[current.fst] > current.snd ) {
				distances[current.fst] = current.snd;
				int adj[] = stats.getWorld().getAdjacencies(current.fst);
				for(int a = 0; a<adj.length; a++)
					Q.push(new Pair<Integer,Integer>(adj[a], current.snd + 1));
			}
		}
		for(Integer friendly : stats.getMyCountries()) {
			if(distances[friendly] > maxDist)
				maxDist = distances[friendly];
		}
		//for(Integer friendly : stats.getMyCountries())
		//	Risk.sayOutput(countries[friendly].getName() + ": " + distances[friendly], OutputFormat.BLUE);
	}
	
}
