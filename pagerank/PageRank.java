/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;


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
    final static double EPSILON = 0.00001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
    	int noOfDocs = readDocs( filename );
    	computePagerank( noOfDocs );
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


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
	//
	//   YOUR CODE HERE
	//
        
        for (Map.Entry<String,Integer> entry : docNumber.entrySet()) {
            names.put(""+entry.getValue(),entry.getKey());
        }
        System.out.println("Number of docs: " + numberOfDocs);
        double[] pi = new double[numberOfDocs+1];  // add one for convenience, We don't use the 0th element.
        double[] new_pi = new double[numberOfDocs+1];
        pi[1] = 1;

        double[] transistion = new double[numberOfDocs+1];

        for (int i=1;i<=numberOfDocs;i++) {
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
        System.out.println(sink);
        System.out.println(no_link);
        for (int iter=1;iter<=MAX_NUMBER_OF_ITERATIONS;iter++) {
            System.out.println("Iteration: "+ iter);
            new_pi = new double[numberOfDocs+1];
            for (int i=1;i<=numberOfDocs;i++) {
                row = link.get(i);
                if (row == null) {
                    for (int j=1;j<=numberOfDocs;j++) {
                        new_pi[j] += sink*pi[i];
                    }
                    new_pi[i] -= (1-BORED)/(numberOfDocs-1)*pi[i];
                } else {
                    for (int j=1;j<=numberOfDocs;j++) {
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
            for (int i=1;i<=numberOfDocs;i++) {
                e = Math.max(e,Math.abs(new_pi[i]-pi[i]));
                converged = converged && (Math.abs(new_pi[i]-pi[i]) < EPSILON);
                pi[i] = new_pi[i];
                sum += pi[i];
            }
            double sum2=0;
            for (int i=1;i<=numberOfDocs;i++) {
                pi[i] /= sum;
                sum2 += pi[i];
            }
            System.out.println("Sum: " + sum + ",  Sum2: "+sum2);

            if  (iter%10 == 0) {
                printStatus(numberOfDocs, pi);
            }
            System.out.println("Max error: " + e);
            if (converged && iter > 40) {
                System.out.println("---");
                System.out.println("Converged!!!!!"); 

                break;
            }
        }
        printStatus(numberOfDocs, pi);
    }

    private void printStatus(int numberOfDocs, double[] pi) {
        TreeMap<Double,Integer> s = new TreeMap<Double,Integer>();
        for (int i=1;i<=numberOfDocs;i++) {
            s.put(pi[i],i);
        }

        int i=0;

        for (Map.Entry<Double,Integer> entry : s.descendingMap().entrySet()) {
            System.out.println((i+1)+": "+docName[entry.getValue()]+ " = "+ idResults[i] +" ### "+String.format("%.6f",entry.getKey() )+ " = "+String.format("%.6f",scoreResults[i]) + " ##### " +  names.get(docName[entry.getValue()]));
            i++;
            if (i==50) {
                break;
            }
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
