package riskarena.riskbots.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import riskarena.Risk;

public class WeightManager {
	private ArrayList<Double> weights;
	private ArrayList<Double[]> previousScores;
	private HashMap<String,Integer> evalNameToID;
	private final int numWeights;
	private String weightsFileName = "src/data/weights/";
	private boolean should_train;
	private int games_trained = 0;
	private double lambda = 0.5;

	public WeightManager(String name, String evals[], boolean should_train) {
		weights = new ArrayList<Double>();
		evalNameToID = new HashMap<String,Integer>();
		previousScores = new ArrayList<Double[]>();
		weightsFileName += name + ".txt";
		this.should_train = should_train;
		numWeights = evals.length;
		for(int i=0; i<evals.length; i++)
			evalNameToID.put(evals[i], new Integer(i));
	}

	public void initGame() {
		loadWeights();
		previousScores.clear();
	}

	public void train(Double scores[]) {
		if(!should_train) return;
		boolean debug = true;
		
		int previousRounds = previousScores.size();
		if(previousRounds > 0) {
			// Perform precalculations
			Double featureSums[] = new Double[numWeights];
			for(int w = 0; w < numWeights; w++) {
				featureSums[w] = new Double(0.0);
				for(int round=0; round < previousRounds; round++) {
					featureSums[w] += Math.pow(lambda, (double)previousRounds - (round + 1)) * previousScores.get(round)[w];
				}
			}
			double norm = norm(featureSums);
			double prevNorm = norm(previousScores.get(previousRounds-1));
			
			// Perform updates
			StringBuilder updates = new StringBuilder();
			for(int w=0; w<numWeights; w++) {
				double newWeight = weights.get(w);
				double fraction = featureSums[w] / (norm * prevNorm);
				newWeight += alpha() * (applyWeights(scores) - applyWeights(previousScores.get(previousRounds-1))) * fraction;
				updates.append(Utilities.dec(newWeight) + " ");
				weights.set(w, newWeight);
			}
			if(debug)
				Risk.sayOutput(updates.toString(), true);
		}
		previousScores.add(scores);
	}

	private void loadWeights() {
		File weightsFile = new File(weightsFileName);
		String lastLine = tail(weightsFile);
		String weightStrings[] = lastLine.split(" ");
		if(weightStrings.length != numWeights) {
			System.err.println("Incorrect number of weights in " + weightsFileName + ": expected " + numWeights + ", got " + weightStrings.length);
			System.exit(-1);
		}
		for(int i=0; i<weightStrings.length; i++) {
			weights.add( new Double(weightStrings[i]) );
		}
	}

	public double weightOf(String evalName) {
		return weights.get(evalNameToID.get(evalName));
	}

	/*
	 * Read the last line of a file
	 * Courtesy of http://stackoverflow.com/questions/686231/quickly-read-the-last-line-of-a-text-file
	 */
	public String tail( File file ) {
		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile( file, "r" );
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for(long filePointer = fileLength; filePointer != -1; filePointer--){
				fileHandler.seek( filePointer );
				int readByte = fileHandler.readByte();

				if( readByte == 0xA ) {
					if( filePointer == fileLength ) {
						continue;
					} else {
						break;
					}
				} else if( readByte == 0xD ) {
					if( filePointer == fileLength - 1 ) {
						continue;
					} else {
						break;
					}
				}

				sb.append( ( char ) readByte );
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch( java.io.FileNotFoundException e ) {
			e.printStackTrace();
			return null;
		} catch( java.io.IOException e ) {
			e.printStackTrace();
			return null;
		} finally {
			if (fileHandler != null )
				try {
					fileHandler.close();
				} catch (IOException e) {
					/* ignore */
				}
		}
	}

	private double applyWeights(Double to[]) {
		double total = 0.0;
		for(int i=0; i<to.length; i++) {
			total += weights.get(i) * to[i];
		}
		return total;
	}
	
	private double norm(Double find[]) {
		double total = 0.0;
		for(int i=0; i<find.length; i++) {
			total += find[i] * find[i];
		}
		return Math.sqrt(total);
	}

	private double alpha() {
		if(games_trained == 0)
			return 1.0;
		return 1.0 / Math.ceil(games_trained / 1000.0);
	}
}
