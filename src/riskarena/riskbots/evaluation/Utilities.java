/*******************************************************************************
 * Copyright (c) 2012-2014 Evan Radkoff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package riskarena.riskbots.evaluation;

/*
 * Various static helper methods for use by the Evaluation player
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.lang.StringBuilder;

public class Utilities {
	// By default, the number of decimal places a String returned by printDouble(double) 
	public static final int DEFAULT_DECIMAL_PLACES = 3; 
	
	/*
	 * Convert a double into a string using DEFAULT_DECIMAL_PLACES
	 */
	public static String printDouble(double d) {
		return printDouble(d, DEFAULT_DECIMAL_PLACES);
	}
	
	/*
	 * Convert a double into a string with "places" digits after the decimal place
	 */
	public static String printDouble(double d, int places) {
		StringBuilder sb = new StringBuilder("#.");
		for(int i=0; i<places; i++)
			sb.append("#");
		return (new DecimalFormat(sb.toString()).format(d)).toString();
	}
	
	/*
	 * Find the norm of a Double array, defined as the sqrt of the sum of squares
	 */
	public static double norm(Double find[]) {
		double total = 0.0;
		for(int i=0; i<find.length; i++) {
			total += find[i] * find[i];
		}
		return Math.sqrt(total);
	}
	
	 /*
	 * Read the last line of a file
	 * Courtesy of http://stackoverflow.com/questions/686231/quickly-read-the-last-line-of-a-text-file
	 */
	public static String tail( File file ) {
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
	
	/* Given a thread, prints info. For now, simply its stack trace. */
	public static void printThread(Thread thread) {
		StackTraceElement z[] = thread.getStackTrace();
		for(int i=0; i<z.length; i++)
			System.out.println("\t"+z[i].toString());
	}
}
