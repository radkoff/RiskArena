package riskarena.riskbots.evaluation.evals;

import java.util.ArrayList;

import riskarena.GameInfo;
import riskarena.riskbots.evaluation.ArmyChange;
import riskarena.riskbots.evaluation.GameStats;
import riskarena.riskbots.evaluation.OccupationChange;

public abstract class AbstractEvaluator {
	private String name;
	protected GameStats stats;
	protected GameInfo game;
	
	public AbstractEvaluator(String _name, GameStats _stats, GameInfo _game) {
		name = _name;
		stats = _stats;
		game = _game;
	}
	
	abstract public double getScore();
	
	abstract public double getScore(ArrayList<ArmyChange> changes);
	
	abstract public double getScore(OccupationChange change);
	
	abstract public void refresh();
	
	public String getName() {
		return name;
	}
}
