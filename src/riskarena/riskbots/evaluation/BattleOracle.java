package riskarena.riskbots.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.sun.tools.javac.util.Pair;

import riskarena.Risk;

public class BattleOracle {
	private final String probFile = "src/data/victory_probabilities.txt";
	private final String winFile = "src/data/expected_attackers_remaining_on_win.txt";
	private final String lossFile = "src/data/expected_enemies_remaining_on_loss.txt";
	
	private int max;
	/*
	 * The double at probs[i][j] represents the probability that an attacking army of size
	 * i+1 will defeat a defending army of size j+1
	 */
	private double probs[][];
	/*
	 * The int at winnersLeft[i][j] represents the expected number of armies remaining in
	 * an army of (initial) size i+1 attacking an army of size j+1, assuming they were victorious
	 */
	private int winnersLeft[][];
	/*
	 * The int at losersLeft[i][j] represents the expected number of defending armies remaining in
	 * a territory in which an army of (initial) size j+1 successfully thrwarted an attack from
	 * an invading army of size i+1
	 */
	private int losersLeft[][];
	
	public BattleOracle() {
		loadProbs();
		loadWinners();
		loadLosers();
	}

	private void loadProbs() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(probFile));
			String line = br.readLine();
			max = new Integer(line);
			probs = new double[max][max];
			for(int i=0; i<max; i++) {
		        line = br.readLine();
		        double p[] = new double[max];
		        String split[] = line.split(" ");
		        if(split.length != max) {
		        	System.err.println("BattleOracle: " + probFile + " line has invalid number of elements");
		        	System.exit(-1);
		        }
		        for(int j=0; j<split.length; j++)
		        	p[j] = (new Double(split[j]))/100.0;
		        probs[i] = p;
		    }
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadWinners() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(winFile));
			String line = br.readLine();
			int max2 = new Integer(line);
			if(max2 != max) {
				System.err.println("BattleOracle: " + winFile + " has incorrect max");
				System.exit(-1);
			}
			winnersLeft = new int[max][max];
			for(int i=0; i<max; i++) {
		        line = br.readLine();
		        int w[] = new int[max];
		        String split[] = line.split(" ");
		        if(split.length != max) {
		        	System.err.println("BattleOracle: " + winFile + " line has invalid number of elements");
		        	System.exit(-1);
		        }
		        for(int j=0; j<split.length; j++)
		        	w[j] = new Integer(split[j]);
		        winnersLeft[i] = w;
		    }
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadLosers() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(lossFile));
			String line = br.readLine();
			int max2 = new Integer(line);
			if(max2 != max) {
				Risk.sayError("BattleOracle: " + lossFile + " has incorrect max");
				System.exit(-1);
			}
			losersLeft = new int[max][max];
			for(int i=0; i<max; i++) {
		        line = br.readLine();
		        int l[] = new int[max];
		        String split[] = line.split(" ");
		        if(split.length != max) {
		        	System.err.println("BattleOracle: " + lossFile + " line has invalid number of elements");
		        	System.exit(-1);
		        }
		        for(int j=0; j<split.length; j++)
		        	l[j] = new Integer(split[j]);
		        losersLeft[i] = l;
		    }
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Pair<Double,Integer> predictWin(int attackers, int defenders) {
		/*if(attackers-1 < 0 || attackers-1 >= probs.length || max != 100 || probs.length != max)
			System.err.println("Invalid # attackers: " + attackers + " " + probs.length + " " + max);
		if(defenders-1 < 0 || defenders-1 >= probs[attackers-1].length || max != 100 || probs.length != max)
			System.err.println("Invalid # defends: " + defenders + " " + probs.length + " " + max); */
		return new Pair<Double,Integer>(new Double(probs[attackers-1][defenders-1]), new Integer(winnersLeft[attackers-1][defenders-1]));
	}
	
	public Pair<Double,Integer> predictLoss(int attackers, int defenders) {
		return new Pair<Double,Integer>(new Double(1 - probs[attackers-1][defenders-1]), new Integer(losersLeft[attackers-1][defenders-1]));
	}
	
	public int maxPredictionAbility() {
		return max;
	}
	
}
