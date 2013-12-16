package riskarena.riskbots.evaluation;

public class OccupationChange {
	private int from;
	private int to;
	private int fromArmies;
	private int toArmies;
	private int enemiesKilled;
	
	public OccupationChange(int from, int to, int fromArmies, int toArmies, int enemiesKilled) {
		this.from = from;
		this.to = to;
		this.fromArmies = fromArmies;
		this.toArmies = toArmies;
		this.enemiesKilled = enemiesKilled;
	}
	
	public int from() {
		return from;
	}
	public int to() {
		return to;
	}
	public int fromArmies() {
		return fromArmies;
	}
	public int toArmies() {
		return toArmies;
	}
	public int enemiesKilled() {
		return enemiesKilled;
	}

}
