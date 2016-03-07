/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;
import java.util.stream.*;


public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    int[] idResults = {121,21,245,1531,1367,31,80,1040,254,452,157,392,169,100,561,3870,997,884,202,8,72,145,27,645,490,2883,81,942,125,247,337,179,708,1403,484,26,152,321,242,15492,1964,1043,857,1755,1200,281,154,16,1365,15686};
    double[] scoreResults = {0.007980901794924564,0.007731247234492249,0.007359891426395264,0.00509405660492995,0.0028366893437372915,0.0025369975190081186,0.002216407671615069,0.0021825129548366097,0.0020234985434726717,0.0019454428874005798,0.0016263649985622695,0.0016195381427957791,0.0016098408219562277,0.001563095428378879,0.0014602141893513304,0.0014439897797260072,0.0013543509333874295,0.001277762824007422,0.001266144765920104,0.0012575035891859777,0.001230526929286469,0.0011901429022755287,0.0010921473662683632,0.001083156870669533,0.001062683563202588,0.0010501250673160228,0.0010264697730446022,0.0010101607543902813,9.522527024813006*0.0001,9.402995763037513*0.0001,8.777449765518368*0.0001,8.776636143175264*0.0001,8.766749797539502*0.0001,8.697030112833079*0.0001,8.57637060167815*0.0001,8.540721077509235*0.0001,8.526138239010308*0.0001,8.277735810492957*0.0001,8.118822019024427*0.0001,7.968234400580981*0.0001,7.869667098749676*0.0001,7.853311765722786*0.0001,7.714146325915579*0.0001,7.504804360825602*0.0001,7.226875154669279*0.0001,7.119962573160739*0.0001,7.091595631764484*0.0001,7.031042059743013*0.0001,7.023635516744133*0.0001,7.00519329506775*0.0001};
    

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    Hashtable<String,String> names = new Hashtable<String,String>();

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */
    /** Stores the mapping between ids and names */
    HashMap<Integer,String> id2name;

    public PageRank( String filename ) {
    	int noOfDocs = readDocs( filename );
        id2name = readId2Name();
        double[] exactRank  = readRankFromFile("exactRank");
        int N = 20;
        boolean dense = true;
        computePagerank4or5( noOfDocs, exactRank, N, 5, dense );
        // computePagerank4or5( noOfDocs, exactRank, N, 4, dense );
        // computePagerank3( noOfDocs, exactRank, N, dense );
        // computePagerank1or2( noOfDocs, exactRank, N, 2, dense );
        // computePagerank1or2( noOfDocs, exactRank, N, 1, dense );
    	//computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
    	int fileIndex = 0;
    	try {
    	    System.err.print( "Reading file... " );
    	    BufferedReader in = new BufferedReader( new FileReader( filename ));
    	    String line;
    	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
        		int index = line.indexOf( ";" );
        		String title = line.substring( 0, index );
        		Integer fromdoc = docNumber.get( title );
        		//  Have we seen this document before?
        		if ( fromdoc == null ) {	
        		    // This is a previously unseen doc, so add it to the table.
        		    fromdoc = fileIndex++;
        		    docNumber.put( title, fromdoc );
        		    docName[fromdoc] = title;
        		}
        		// Check all outlinks.
        		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
        		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
        		    String otherTitle = tok.nextToken();
        		    Integer otherDoc = docNumber.get( otherTitle );
        		    if ( otherDoc == null ) {
            			// This is a previousy unseen doc, so add it to the table.
            			otherDoc = fileIndex++;
            			docNumber.put( otherTitle, otherDoc );
            			docName[otherDoc] = otherTitle;
        		    }
        		    // Set the probability to 0 for now, to indicate that there is
        		    // a link from fromdoc to otherDoc.
        		    if ( link.get(fromdoc) == null ) {
        			     link.put(fromdoc, new Hashtable<Integer,Boolean>());
        		    }
        		    if ( link.get(fromdoc).get(otherDoc) == null ) {
            			link.get(fromdoc).put( otherDoc, true );
            			out[fromdoc]++;
        		    }
        		}
    	    }
    	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
    		  System.err.print( "stopped reading since documents table is full. " );
    	    }
    	    else {
    		  System.err.print( "done. " );
    	    }
    	    // Compute the number of sinks.
    	    for ( int i=0; i<fileIndex; i++ ) {
                if ( out[i] == 0 )
    		        {numberOfSinks++;}
    	    }
    	}
    	catch ( FileNotFoundException e ) {
    	    System.err.println( "File " + filename + " not found!" );
    	}
    	catch ( IOException e ) {
    	    System.err.println( "Error reading file " + filename );
    	}
    	System.err.println( "Read " + fileIndex + " number of documents" );
    	return fileIndex;
    }


    /* --------------------------------------------- */


    void computePagerank1or2( int numberOfDocs, double[] exactRank, int N, int task, boolean dense ) {
        int printEvery = (dense) ? 100 : numberOfDocs;
        Random rand = new Random();
        double pi[] = new double[numberOfDocs];
        int counter = 0;
        int currentState = -1;
        int endState = -1;
        compareToExact(pi,exactRank);
        for (int j=0;j<N;j++) {
            for (int startState=0;startState<numberOfDocs;startState++) {
                if (task==2) {
                    currentState = startState; 
                } else if (task==1){
                    currentState = rand.nextInt(numberOfDocs);
                }
                endState = doRandomWalk(currentState, numberOfDocs, rand);
                pi[endState] += 1;
                counter++;  
                if (counter%printEvery==0) {
                    // System.err.println("iteration: " + counter);
                    double[] normPi = normalize(pi);
                    compareToExact(normPi,exactRank);
                }
            }
        }
        pi = normalize(pi);
        printStatus(numberOfDocs, pi);
    }

    

    void computePagerank3( int numberOfDocs, double[] exactRank, int N, boolean dense ) {
        int printEvery = (dense) ? 100 : numberOfDocs;
        
        Random rand = new Random();
        double pi[] = new double[numberOfDocs];
        int counter = 0;
        compareToExact(pi,exactRank);
        for (int j=0;j<N;j++) {
            for (int startState=0;startState<numberOfDocs;startState++) {
                HashMap<Integer,Integer> history = doRandomWalkHistory(startState, numberOfDocs, rand);
                // int sum=0;
                // for (Map.Entry<Integer,Integer> me : history.entrySet()) {
                //     sum += me.getValue();
                // }
                // double n = N*numberOfDocs*sum;
                for (Map.Entry<Integer,Integer> me : history.entrySet()) {
                    pi[me.getKey()] += (me.getValue() );/// n);
                }
                counter++;  
                if (counter%printEvery==0) {
                    // System.err.println("iteration: " + counter);
                    double[] normPi = normalize(pi);
                    compareToExact(normPi,exactRank);
                }
            }
        }
        pi = normalize(pi);
        printStatus(numberOfDocs, pi);   
    }

    void computePagerank4or5( int numberOfDocs, double[] exactRank, int N, int task, boolean dense ) {
        int printEvery = (dense) ? 100 : numberOfDocs;
        Random rand = new Random();
        double pi[] = new double[numberOfDocs];
        int counter = 0;
        Hashtable<Integer,Boolean> outlinks;
        int newState;
        int currentState=-1;
        compareToExact(pi,exactRank);
        for (int j=0;j<N;j++) {
            for (int startState=0;startState<numberOfDocs;startState++) {
                if (task==4) {
                    currentState = startState;
                } else if (task==5) {
                    currentState = rand.nextInt(numberOfDocs);
                } else {
                    System.err.println("task has to be 4 or 5, not: " + task);
                }
                
                HashMap<Integer,Integer> history = new HashMap<Integer,Integer>();
                history.put(currentState, 1); 
                boolean not_sink = true;
                double c = 1;
                while (not_sink && c > BORED) {
                    outlinks = link.get(currentState);
                    if (outlinks == null) {
                        not_sink = false;
                    } else {
                        newState = (int)(outlinks.keySet().toArray()[rand.nextInt(outlinks.size())]);
                        if (history.keySet().contains(newState)) {
                        history.put(newState, history.get(newState) + 1 ); 
                        } else {
                            history.put(newState,1);
                        }
                        currentState = newState;
                    }
                    c = rand.nextDouble();
                    
                }
                for (Map.Entry<Integer,Integer> me : history.entrySet()) {
                    pi[me.getKey()] += me.getValue();
                }
                counter++;  
                if (counter%printEvery==0) {
                    double[] normPi = normalize(pi);
                    compareToExact(normPi,exactRank);
                }
            }
        }
        pi = normalize(pi);
        printStatus(numberOfDocs, pi);   
    }

    void compareToExact(double[] pi, double[] exactRank) {
        ArrayList<Map.Entry<Integer,Double>> aprox = createSortedList(pi, pi.length);
        ArrayList<Map.Entry<Integer,Double>> exact = createSortedList(exactRank, exactRank.length);
        double topSum = 0;
        double lowSum = 0;
        int size = pi.length;
        for (int i=0;i<50;i++) {
            //System.out.println(pi[i]);
            topSum += Math.abs(aprox.get(i).getValue()-exact.get(i).getValue());
            lowSum += Math.abs(aprox.get(size - 1 - i).getValue()-exact.get(size - i - 1).getValue());
        }
        System.out.println(topSum + " " + lowSum);
    }

    ArrayList<Map.Entry<Integer,Double>> createSortedList(double[] pi, int numberOfDocs) {
        HashMap<Integer,Double> ss = new HashMap<Integer,Double>();
        ArrayList<Map.Entry<Integer,Double>> list = new ArrayList<Map.Entry<Integer,Double>>();
        for (int i=0;i<numberOfDocs;i++) {
            ss.put(i,pi[i]);
        }
        for (Map.Entry<Integer,Double> me : ss.entrySet()){
            list.add(me);
        }
        Collections.sort(list, new ValueComparator());
        return list;
    }

    double[] normalize(double[] pi) {
        double[] normPi = new double[pi.length];
        double sum = DoubleStream.of(pi).sum();
        int size = pi.length;
        for (int i=0;i<size;i++) {
            normPi[i] = pi[i] / sum;
        }
        return normPi;
    }

    int doRandomWalk(int startState, int numberOfDocs, Random rand) {
        double c = 1;
        int currentState = startState;
        Hashtable<Integer,Boolean> outlinks; 
        int newState;
        while (c > BORED) {
            outlinks = link.get(currentState);
            if (outlinks == null) {
                newState = currentState;
                while (newState == currentState) {
                    newState = rand.nextInt(numberOfDocs);
                }
            } else {
                int j = rand.nextInt(outlinks.size());
                newState = (int)(outlinks.keySet().toArray()[j]);
            }
            currentState = newState;
            c = rand.nextDouble();
        }
        return currentState;
    }

    HashMap<Integer,Integer> doRandomWalkHistory(int startState, int numberOfDocs, Random rand) {
        double c = 1;
        int currentState = startState;
        Hashtable<Integer,Boolean> outlinks; 
        int newState;
        HashMap<Integer,Integer> history = new HashMap<Integer,Integer>();
        history.put(currentState, 1); 
        while (c > BORED) {
            outlinks = link.get(currentState);
            if (outlinks == null) {
                newState = currentState;
                while (newState == currentState) {
                    newState = rand.nextInt(numberOfDocs);
                }
            } else {
                int j = rand.nextInt(outlinks.size());
                newState = (int)(outlinks.keySet().toArray()[j]);
            }
            if (history.keySet().contains(newState)) {
                history.put(newState, history.get(newState) + 1 ); 
            } else {
                history.put(newState,1);
            }
            currentState = newState;
            c = rand.nextDouble();
        }
        return history;
    }



    /*
     *   Computes the pagerank of each document. Exact calculation.
     */
    void computePagerank( int numberOfDocs ) {
        
        for (Map.Entry<String,Integer> entry : docNumber.entrySet()) {
            names.put(""+entry.getValue(),entry.getKey());
        }
        System.out.println("Number of docs: " + numberOfDocs);
        double[] pi = new double[numberOfDocs]; 
        double[] new_pi = new double[numberOfDocs];
        pi[0] = 1;

        double[] transistion = new double[numberOfDocs];

        for (int i=0;i<numberOfDocs;i++) {
            Hashtable<Integer,Boolean> row = link.get(i);
            if (row != null) {
                int N = row.size();
                transistion[i] = (1 - BORED)/N + BORED/numberOfDocs;
            } else {
                transistion[i] = (1-BORED)/(numberOfDocs-1) + BORED/numberOfDocs;
            }
        }

        Hashtable<Integer,Boolean> row;
        double sink = (1-BORED)/(numberOfDocs-1) + BORED/numberOfDocs;
        double no_link = BORED/numberOfDocs;

        for (int iter=1;iter<=MAX_NUMBER_OF_ITERATIONS;iter++) {
            System.out.println("Iteration: "+ iter);
            new_pi = new double[numberOfDocs];
            for (int i=0;i<numberOfDocs;i++) {
                row = link.get(i);
                if (row == null) {
                    for (int j=0;j<numberOfDocs;j++) {
                        new_pi[j] += sink*pi[i];
                    }
                    new_pi[i] -= (1-BORED)/(numberOfDocs-1)*pi[i];
                } else {
                    for (int j=0;j<numberOfDocs;j++) {
                        if (row.keySet().contains(j)) {
                            new_pi[j] += transistion[i] * pi[i];  
                        } else {
                            new_pi[j] += no_link * pi[i];
                        }
                    }
                    // no need to take care of the self jump since we already take cate of states with no links to i.e. no_link
                }
            }

            boolean converged = true;
            double e = 0;
            double sum = 0;
            double err = 0;
            for (int i=0;i<numberOfDocs;i++) {
                e = Math.sqrt(Math.max(e,Math.abs(new_pi[i]-pi[i])));
                err += Math.pow(new_pi[i]-pi[i],2);
                converged = converged && (Math.abs(new_pi[i]-pi[i]) < EPSILON);
                pi[i] = new_pi[i];
                sum += pi[i];
            }
            converged = err < EPSILON;
            double sum2=0;
            for (int i=0;i<numberOfDocs;i++) {
                pi[i] /= sum;
                sum2 += pi[i];
            }
            System.out.println("Sum: " + sum + ",  Sum2: "+sum2);

            if  (iter%10 == 0) {
                printStatus(numberOfDocs, pi);
            }
            System.out.println("Max error: " + e);
            System.out.println("sum of square errors: " + err);
            if (converged && iter >= 18) {
                System.out.println("---");
                System.out.println("Converged!!!!!");
                writeRankToFile("exactRank",pi);
                break;
            }
        }
        printStatus(numberOfDocs, pi);
    }

    private HashMap<Integer,String> readId2Name() {
        HashMap<Integer,String> results = new HashMap<Integer,String>();
        String filename = "articleTitles.txt";
        try {
            BufferedReader in = new BufferedReader( new FileReader( filename ));
            String line; String[] parts;
            while ((line = in.readLine()) != null) {
                parts = line.split(";");
                results.put(Integer.parseInt(parts[0]), parts[1]);
                // System.out.println(parts[0] + " : " + parts[1]);
            }
        }
        catch ( FileNotFoundException e ) {
            System.err.println( "File " + filename + " not found!" );
        }
        catch ( IOException e ) {
            System.err.println( "Error reading file " + filename );
        }
        return results;
    }

    private void printStatus(int numberOfDocs, double[] pi) {
        ArrayList<Map.Entry<Integer,Double>> list = createSortedList(pi,pi.length);
        int i=0;
        for (Map.Entry<Integer,Double> entry : list) {
            System.out.println((i+1)+": "+docName[entry.getKey()]+ " = "+ idResults[i] +" ### "+String.format("%.6f",entry.getValue() )+ " = "+String.format("%.6f",scoreResults[i]) + " ##### " +  id2name.get(Integer.parseInt(docName[entry.getKey()]) ));
            i++;
            if (i==50) {
                break;
            }
        }
    }




    void writeRankToFile(String fileName, double[] rank) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rank);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    double[] readRankFromFile(String fileName) {
         double[] rank = null;
         try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            rank = (double[])ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rank;
    }

    static class ValueComparator implements Comparator<Map.Entry<Integer,Double>> {
        public int compare(Map.Entry<Integer,Double> c1, Map.Entry<Integer,Double> c2) {
            return -c1.getValue().compareTo(c2.getValue());
        }
    }

    /* --------------------------------------------- */


    public static void main( String[] args ) {
    	if ( args.length != 1) {
    	    System.err.println( "Please give the name of the link file" );
    	}
    	else {
    	    new PageRank( args[0] );
    	}
    }
}
