package riskarena.riskbots.evaluation;

public abstract class AbstractEvaluator {
	private double weight;
	private String name;
	
	public AbstractEvaluator(String _name, double _weight) {
		name = _name;
		weight = _weight;
	}
	
	abstract public double getScore();
	
	abstract public void refresh();
	
	final public double getWeight() {
		return weight;
	}
	
	public String getName() {
		return name;
	}
}
