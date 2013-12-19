package riskarena.riskbots.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import riskarena.Risk;

public class WeightManager {
	private ArrayList<Double> weights;
	private ArrayList<Double[]> previousScores;
	private HashMap<String,Integer> evalNameToID;
	private final int numWeights;
	private String weightsFileName = "src/data/weights/";
	private File weightsFile;
	private boolean should_train;
	private int games_trained = 0;
	private double lambda = 0.5;	// Higher = utilizer the further past more. Lower = learn only from more recent trainings
	
	private final double weightSum = 0.3;	// Weights will always add up to this!

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
		createWeightsFile();
		loadWeights();
		previousScores.clear();
	}

	public void train(Double scores[]) {
		if(!should_train) return;
		if(!previousScores.isEmpty())
			train(applyWeights(scores), applyWeights(previousScores.get(previousScores.size()-1)));
		previousScores.add(scores);
	}

	public void endGame(Double scores[], int place, int numPlayers) {
		if(should_train) {
			train(reward(place, numPlayers), applyWeights(previousScores.get(previousScores.size()-1)));
			games_trained++;
			writeUpdate();
		}
	}
	
	/*
	 * If no weights file exists, create one using randomly initialized weights (that add up to weightSum)
	 */
	private void createWeightsFile() {
		weightsFile = new File(weightsFileName);
		if(!weightsFile.exists()) {
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(weightsFileName, "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			writer.write("0");
			double newWeights[] = makeRandomWeights();
			for(int i=0; i<newWeights.length; i++)
				writer.write(" " + newWeights[i]);
			writer.close();
			weightsFile = new File(weightsFileName);
		}
	}

	public void train(double current, double past) {
		if(!should_train) return;
		boolean debug = false;

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

			double totalW = 0.0;
			// Perform updates
			StringBuilder updates = new StringBuilder();
			for(int w=0; w<numWeights; w++) {
				double newWeight = weights.get(w);
				double fraction = featureSums[w] / (norm * prevNorm);
				//double fraction = featureSums[w] / (norm);
				newWeight += alpha() * (current - past) * fraction;
				totalW += newWeight;
				updates.append(Utilities.dec(newWeight) + " ");
				weights.set(w, newWeight);
			}
			for(int w=0; w<numWeights; w++) {
				double newWeight = weights.get(w);
				weights.set(w, newWeight / (10*totalW));
			}
			if(debug)
				Risk.sayOutput(games_trained + "\t" + updates.toString(), true);
		}
	}

	private void loadWeights() {
		weights.clear();
		File weightsFile = new File(weightsFileName);
		String lastLine = tail(weightsFile);
		String weightStrings[] = lastLine.split(" ");
		if(weightStrings.length - 1 != numWeights) {
			System.err.println("Incorrect number of weights in " + weightsFileName + ": expected " + numWeights + ", got " + (weightStrings.length - 1));
			System.exit(-1);
		}
		games_trained = new Integer(weightStrings[0]);
		for(int i=1; i<weightStrings.length; i++) {
			weights.add( new Double(weightStrings[i]) );
		}
	}

	private void writeUpdate() {
		try {
			Writer output = new BufferedWriter( new FileWriter(weightsFile, true) );
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(games_trained);
			for(Double w : weights) {
				sb.append(" " + w);
			}
			output.write(sb.toString());
			output.close();
		} catch (IOException e) {
			Risk.sayError(weightsFileName + " not found!");
			e.printStackTrace();
		}

	}

	public double weightOf(String evalName) {
		return weights.get(evalNameToID.get(evalName));
	}
	
	/*
	 * Makes random weights that add up to weightSum
	 */
	private double[] makeRandomWeights() {
		Random gen = new Random((new Date()).getTime());
		double sum = 0.0;
		double rands[] = new double[numWeights];
		for(int i=0; i<numWeights;i++) {
			rands[i] = gen.nextDouble();
			sum += rands[i];
		}
		for(int i=0; i<numWeights;i++) {
			rands[i] = (rands[i] / sum) * weightSum;
		}
		return rands;
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

	private double reward(int place, int numPlayers) {
		return (1.0 / (numPlayers - 1)) * (numPlayers - place);
	}

	private double alpha() {
		if(games_trained == 0)
			return 1.0;
		return 1.0 / Math.ceil(games_trained / 1000.0);
	}
}
