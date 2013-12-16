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
