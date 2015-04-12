/* 
 * University of North Carolina at Chapel Hill
 * Spring 2013 Artificial Intelligence
 * Homework 2 - 3/18/2013
 * Renato Pereyra
 */

import java.io.*;
import java.util.*;
import java.lang.Math.*;

class Naive_Bayes{

	static HashMap< Integer, String > vocab;
	static HashMap< Integer, HashMap< Integer,Integer > > trainData;
	static ArrayList< ArrayList< Integer > > testData;

	static HashMap< Integer, HashMap< Integer, Double > > thetas;
	
	static ArrayList<Integer> predictedLabels;
	static ArrayList<Integer> actualLabels;

	static int numSpamInTraining;
	static int numHamInTraining;

	static boolean laplacian;

	static final int NUM_WORDS_TO_PRINT = 0;
	static final int NUM_FOLDS = 5;
	static final int HAM = 0;
	static final int SPAM = 1;

	public static void main( String[] args ){

		if( args.length == 0 ){

			laplacian = false;
			loadVocab();
			String[] trainFiles = {"train.txt"};
			String testFile = "test.txt";
			_init( testFile, trainFiles );

		}else if( args.length == 1 && args[0].equals( "-lapl" ) ){

			laplacian = true;
			loadVocab();
			String[] trainFiles = {"train.txt"};
			String testFile = "test.txt";
			_init( testFile, trainFiles );

		}else if( args.length == 1 && args[0].equals( "-crossV" ) ){

			laplacian = false;
			loadVocab();
			prepCrossValidation();
			_initCrossValidation();

		}else if( args.length == 2 && ( args[0].equals( "-crossV" ) && args[1].equals( "-lapl" ) 
			|| args[1].equals( "-crossV" ) && args[0].equals( "-lapl" ) ) ){

			laplacian = true;
			loadVocab();
			prepCrossValidation();
			_initCrossValidation();

		}else{
			System.err.println( "Unrecognized command-line arguments. Please try again." );
			System.exit( 1 );
		}

	}

	public static void _initCrossValidation(){

		double avg = 0;

		for( int i = 1; i <= NUM_FOLDS; i++ ){

			String testFile = null;
			String[] trainFiles = new String[ NUM_FOLDS - 1 ];
			int k = 0;

			for( int j = 1; j <= NUM_FOLDS; j++ ){

				if( i == j ){
					testFile = "fold" + j + ".txt";
				}else{
					trainFiles[k] = "fold" + j + ".txt";
					k++;
				}

			}
			
			avg += _init( testFile, trainFiles );

		}

		avg = avg/(double)NUM_FOLDS;

		System.out.println( "---------------------------------------------------------------------" );
		System.out.println( "------------------ CROSS VALIDATION SUMMARY -------------------------" );
		System.out.println( "---------------------------------------------------------------------" );
		System.out.println( "On average, guessed " + avg + "% of labels correctly.");
		System.out.println( "---------------------------------------------------------------------" );
		System.out.println( "---------------- END CROSS VALIDATION SUMMARY -----------------------" );
		System.out.println( "---------------------------------------------------------------------" );

	}

	public static double _init( String testFile, String[] trainFiles ){

		loadTrainingData( trainFiles );
		trainModel();
		loadTestData( testFile );
		predictLabels();
		return checkLabels();

	}

	public static void prepCrossValidation(){

		try{

			LineNumberReader testLReader = new LineNumberReader(new FileReader(new File("test.txt")));
			LineNumberReader trainLReader = new LineNumberReader(new FileReader(new File("train.txt")));

			testLReader.skip(Long.MAX_VALUE);
			trainLReader.skip(Long.MAX_VALUE);

			int numlines = testLReader.getLineNumber() + trainLReader.getLineNumber();

			int size = (int)Math.floor((double)numlines/(double)NUM_FOLDS);

			BufferedReader testReader = new BufferedReader( new InputStreamReader( new FileInputStream( "test.txt" ) ));
			BufferedReader trainReader = new BufferedReader( new InputStreamReader( new FileInputStream( "train.txt" ) ));

			String input = testReader.readLine();

			int i = 1;
			int lineCount = 0;

			BufferedWriter fwriter = new BufferedWriter( new FileWriter( "fold" + i + ".txt" ));

			while( input != null ){

				fwriter.write( input + "\r\n" );
				fwriter.flush();

				lineCount++;

				if( lineCount == size ){

					lineCount = 0;
					i++;

					fwriter.close();
					fwriter = new BufferedWriter( new FileWriter( "fold" + i + ".txt" ));

				}

				input = testReader.readLine();

			}

			input = trainReader.readLine();

			while( input != null ){

				fwriter.write( input + "\r\n" );
				fwriter.flush();

				lineCount++;

				if( lineCount == size && i != NUM_FOLDS ){

					lineCount = 0;
					i++;

					fwriter.close();
					fwriter = new BufferedWriter( new FileWriter( "fold" + i + ".txt" ));

				}

				input = trainReader.readLine();

			}

			fwriter.close();

		}catch( Exception e ){
			System.err.println( "CrossValidationPrep failed with message: " + e.getMessage() );
			System.exit( 1 );
		}

	}

	public static double checkLabels(){

		int rightCount = 0;
		int tooStrict = 0;
		int tooLoose = 0;
		int countHAM = 0;
		int countSPAM = 0;
		int totalCount = predictedLabels.size();

		for( int i = 0; i < totalCount; i++ ){
			if( predictedLabels.get(i) == HAM )
				countHAM++;
			else
				countSPAM++;
			if( predictedLabels.get(i) == actualLabels.get(i) )
				rightCount++;
			else if( predictedLabels.get(i) == HAM )
				tooStrict++;
			else
				tooLoose++;

		}

		double toReturn = (double)rightCount/(double)totalCount * (double)100;

		System.out.println( "------------------------- RUN STATISTICS ------------------------------" );
		System.out.println( "Guessed " + toReturn + "% of labels correctly.");
		System.out.println( tooStrict + " of the labels where mislabeled HAM" );
		System.out.println( tooLoose + " of the labels where mislabeled SPAM" );
		System.out.println( countHAM + " total where predicted as HAM" );
		System.out.println( countSPAM + " total where predicted as SPAM" );
		System.out.println( "----------------------- END RUN STATISTICS ----------------------------" );

		return toReturn;

	}

	public static void predictLabels(){
	
		predictedLabels = new ArrayList<Integer>();

		double p_spam = (double)numSpamInTraining / ( (double)numSpamInTraining + (double)numHamInTraining );
		double p_ham = (double)numHamInTraining / ( (double)numSpamInTraining + (double)numHamInTraining );

		for( ArrayList<Integer> list: testData ){

			double maxHam = p_ham;
			double maxSpam = p_spam;
			double tempHam, tempSpam;
			for( Integer it: list ){
				tempHam = thetas.get( it ).get( HAM );
				tempSpam = thetas.get( it ).get( SPAM );
				if( tempHam > maxHam )
					maxHam = tempHam;
				if( tempSpam > maxSpam )
					maxSpam = tempSpam;
			}

			double spamTerm = 0;
			double hamTerm = 0;

			for( Integer it: list ){
				hamTerm += Math.exp(thetas.get( it ).get( HAM ) - maxHam);
				spamTerm += Math.exp(thetas.get( it ).get( SPAM ) - maxSpam);
			}

			hamTerm = Math.log( hamTerm ) + maxHam;
			spamTerm = Math.log( spamTerm ) + maxSpam;

			double hamRes = Math.log( p_ham );
			double spamRes = Math.log( p_spam );

			for( Integer it: list ){
				hamRes += thetas.get( it ).get( HAM ) - hamTerm;
				spamRes += thetas.get( it ).get( SPAM ) - spamTerm;
			}

			double result = spamRes - hamRes;

			if( result > 0 )
				predictedLabels.add( SPAM );
			else
				predictedLabels.add( HAM );

		}

	}

	public static double theta_ML( Integer label, Integer word ){
		return Math.log( n( label, word) );
	}

	public static double n( Integer label, Integer word ){

		double toReturn;
		
		try{
			toReturn = (double)trainData.get( label ).get( word );
		}catch( NullPointerException e ){
			toReturn = (double)Math.pow( 10, -12 );
		}

		if( laplacian )
			toReturn = toReturn + (double)1;
		
		return toReturn;

	}

	public static void trainModel(){

		TreeMap< Double, String > thetaDiffs = new TreeMap< Double, String >();
		HashMap< String, String > thetaMappings = new HashMap< String, String >();

		thetas = new HashMap< Integer, HashMap< Integer, Double > >();

		int i = 1;

		while( vocab.containsKey(i) ){

			HashMap< Integer, Double > temp = new HashMap< Integer, Double >();

			String wordSpelling = vocab.get(i);

			double spamTheta = theta_ML( SPAM, i );
			double hamTheta = theta_ML( HAM, i );
			double thetaDiff = Math.abs( spamTheta - hamTheta );

			thetaDiffs.put( thetaDiff, wordSpelling );

			String thetasToPrint = spamTheta + "\t\t\t" + hamTheta;

			thetaMappings.put( wordSpelling, thetasToPrint );

			temp.put( HAM, hamTheta );
			temp.put( SPAM, spamTheta );

			thetas.put( i, temp );

			i++;

		}

		Object[] sortedWords = thetaDiffs.descendingMap().values().toArray();

		if( NUM_WORDS_TO_PRINT > 0 )
			System.out.println( "<word>\t\t\t<theta_ML|spam>\t\t\t<theta_ML|ham>" );

		i = 0;

		while( i < NUM_WORDS_TO_PRINT ){
			System.out.println( sortedWords[i] + "\t\t\t" + thetaMappings.get( sortedWords[i] ) );
			i++;
		}

	}

	public static void loadTrainingData( String[] trainFiles ){

		try{

			numHamInTraining = 0;
			numSpamInTraining = 0;

			trainData = new HashMap< Integer, HashMap< Integer,Integer > >();
			HashMap< Integer,Integer > spamHash = new HashMap< Integer,Integer >();
			HashMap< Integer,Integer > hamHash = new HashMap< Integer,Integer >();

			for( int j = 0; j < trainFiles.length; j++ ){
	
				BufferedReader trainFile = new BufferedReader( new InputStreamReader( new FileInputStream( trainFiles[j] ) ));
				String input = trainFile.readLine();
		
				while( input != null ){
		
					String[] temp = input.split( ",|:" );
					
					if( temp.length < 2 ){
						input = trainFile.readLine();
						continue;
					}
					
					int label = Integer.parseInt( temp[0] );
		
					for( int i = 1; i < temp.length; i = i + 2 ){
		
						if( label == HAM ){
							numHamInTraining++;
							if( !hamHash.containsKey( Integer.parseInt(temp[i]) ) )
								hamHash.put( Integer.parseInt(temp[i]), Integer.parseInt(temp[i+1]) );
							else{
								Integer prev = hamHash.get( Integer.parseInt(temp[i]) );
								hamHash.put( Integer.parseInt(temp[i]), Integer.parseInt(temp[i+1]) + prev );
							}
						}
						else{
							numSpamInTraining++;
							if( !spamHash.containsKey( Integer.parseInt(temp[i]) ) )
								spamHash.put( Integer.parseInt(temp[i]), Integer.parseInt(temp[i+1]) );
							else{
								Integer prev = spamHash.get( Integer.parseInt(temp[i]) );
								spamHash.put( Integer.parseInt(temp[i]), Integer.parseInt(temp[i+1]) + prev );
							}
						}
		
					}

					input = trainFile.readLine();
		
				}

			}

			trainData.put( HAM, hamHash );
			trainData.put( SPAM, spamHash );
			
		}catch( Exception e ){		
			System.err.println( "An error occurred while reading train data. Message: " + e.getMessage() );	
			System.exit( 1 );
		}

	}

	public static void loadTestData( String filename ){

		try{
		
			actualLabels = new ArrayList<Integer>();

			BufferedReader testFile = new BufferedReader( new InputStreamReader( new FileInputStream( filename ) ));
			testData = new ArrayList< ArrayList< Integer > >();

			String input = testFile.readLine();

			while( input != null ){

				String[] temp = input.split( ",|:" );
				
				if( temp.length < 2 ){
					input = testFile.readLine();
					continue;
				}
				
				int label = Integer.parseInt( temp[0] );

				actualLabels.add( label );

				ArrayList< Integer > tempList = new ArrayList< Integer >();

				for( int i = 1; i < temp.length; i = i + 2 ){
					tempList.add( Integer.parseInt(temp[i]) );
				}

				testData.add( tempList );
				
				input = testFile.readLine();

			}
			
		}catch( Exception e ){		
			System.err.println( "An error occurred while reading test data. Message: " + e.getMessage() );	
			System.exit( 1 );		
		}

	}

	public static void loadVocab(){

		try{

			vocab = new HashMap< Integer, String >();

			BufferedReader vocabFile = new BufferedReader( new InputStreamReader( new FileInputStream( "vocab.txt" ) ));
			String input = vocabFile.readLine();

			while( input != null ){

				String[] temp = input.split( ":", 2 );
				
				if( temp.length < 2 ){
					input = vocabFile.readLine();
					continue;
				}
				
				vocab.put( Integer.parseInt(temp[0]), temp[1] );
				input = vocabFile.readLine();

			}

		}catch( Exception e ){
			System.err.println( "An error occurred while reading vocab data. Message: " + e.getMessage() );	
			System.exit( 1 );	
		}

	}

}