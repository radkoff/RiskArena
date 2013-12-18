package riskarena.riskbots.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

public class WeightManager {
	private ArrayList<Double> weights;
	private HashMap<String,Integer> evalNameToID;
	private final int numWeights;
	private String weightsFileName = "src/data/weights/";
	
	public WeightManager(String name, String evals[]) {
		weights = new ArrayList<Double>();
		evalNameToID = new HashMap<String,Integer>();
		weightsFileName += name + ".txt";
		numWeights = evals.length;
		for(int i=0; i<evals.length; i++)
			evalNameToID.put(evals[i], new Integer(i));
	}
	
	public void initGame() {
		loadWeights();
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
}
