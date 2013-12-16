package riskarena.riskbots.evaluation;

import java.util.ArrayList;

import riskarena.CountryInfo;
import riskarena.GameInfo;
import riskarena.World;

public class FortifyPositionDecision {
	private GameInfo game;
	private Evaluation eval;
	private World world;
	
	private ArrayList<Integer> answer;
	private double highest;
	
	public FortifyPositionDecision(GameInfo _game, Evaluation _eval) {
		game = _game;
		eval = _eval;
		world = game.getWorldInfo();
		answer = new ArrayList<Integer>();
	}
	
	public ArrayList<Integer> decide() {
		highest = eval.score();
		answer.clear();
		answer.add(new Integer(-1));
		
		CountryInfo countries[] = game.getCountryInfo();
		for(int i=0; i<countries.length; i++) {
			if(countries[i].getPlayer() != game.me() || countries[i].getArmies() <= 1)
				continue;
			int adj[] = world.getAdjacencies(i);
			for(int a = 0; a<adj.length; a++) {
				if(countries[adj[a]].getPlayer() != game.me())
					continue;
				// Try moving the max and half of that
				tryNum(i, adj[a], countries[i].getArmies() - 1);
				int half = (countries[i].getArmies() - 1) / 2;
				if(half > 0 && half != countries[i].getArmies() - 1)
					tryNum(i, adj[a], half);
			}
		}
		return answer;
	}
	
	private void tryNum(int from, int to, int amount) {
		ArrayList<ArmyChange> changes = new ArrayList<ArmyChange>();
		changes.add( new ArmyChange(from, amount * -1) );
		changes.add( new ArmyChange(to, amount) );
		double score = eval.score(changes);
		if(score > highest) {
			highest = score;
			answer.clear();
			answer.add(new Integer(from));
			answer.add(new Integer(to));
			answer.add(new Integer(amount));
		}
	}
	
}
