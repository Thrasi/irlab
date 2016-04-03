/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.*;
import java.util.StringTokenizer;
import java.util.HashMap;
public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    public double alpha = 1.0000001;
    public double beta = 0.750;

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
    	StringTokenizer tok = new StringTokenizer( queryString );
    	while ( tok.hasMoreTokens() ) {
    	    terms.add( tok.nextToken() );
    	    weights.add( new Double(1) );
    	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	   return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
    	Query queryCopy = new Query();
    	queryCopy.terms = (LinkedList<String>) terms.clone();
    	queryCopy.weights = (LinkedList<Double>) weights.clone();
    	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant

            HashMap<String,Double> q0 = queryAsVec();

            // System.out.println("initial q0 length of weights: " + lengthOfWeights(q0));
            // printMap(q0);
            updateWeightsWithTfIdf(q0, indexer);  // If we want to have tfIDF not just TF
            // System.out.println("updated q0 length of weights: " + lengthOfWeights(q0));
            // printMap(q0);
            normalize(q0);
            // printMap(q0);
            // System.out.println("normalized q0 length of weights: " + lengthOfWeights(q0));
            multiply( alpha, q0 );
            // printMap(q0);

            HashMap<String,Double> dSum = sumOfRelevantDocs( results, docIsRelevant, indexer );
            
            System.out.println("Size of dSum: "+dSum.keySet().size());
            dSum = join( dSum, q0 );
            System.out.println("Size of dSum after joining with q: "+dSum.keySet().size());

            // normalize( dSum );
            System.out.println("normalized dSum length of weights: " + lengthOfWeights(dSum));

            terms = new LinkedList<String>();
            weights = new LinkedList<Double>();
            for (Map.Entry<String,Double> me : dSum.entrySet()) {
                terms.add(me.getKey());
                weights.add(me.getValue());
            }
    }

    public double vectorLenght(LinkedList<Double> x){
        double sum = 0;
        for(Double d: x){
            sum += d*d;
        }
        return Math.sqrt(sum);
    }

    private double lengthOfWeights(HashMap<String,Double> q) {
        double norm = 0;
        for (Double v : q.values()) {
            norm += v*v;
        }
        return Math.sqrt(norm);
    }

    private void updateWeightsWithTfIdf(HashMap<String,Double> q, Indexer indexer) {
        for (Map.Entry<String,Double> me: q.entrySet()) {
            me.setValue( me.getValue() * indexer.index.idf( me.getKey() ) );
        }
    }

    private HashMap<String,Double> sumOfRelevantDocs( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
        int i=0;
        double Dr=0;
        HashMap<String,Double> dSum = new HashMap<String,Double>();
        for (PostingsEntry pe : results.getList()) {
            if ( docIsRelevant[i] ) {
                Dr+=1;
                HashMap<String,Double> d = indexer.index.getBag(pe.docID);
                // printMap(d);
                updateWeightsWithTfIdf(d, indexer);

                normalize(d);
                dSum = join(dSum, d);
                // System.out.println("relevant: "+i);
                // System.out.println("Second relevant: "+pe.docID);
            }
            i++;
            if (i==10) { break; }
        }
        System.out.println("Dr: "+Dr);
        multiply( (beta/Dr), dSum );
        return dSum;
    }

    private HashMap<String,Double> queryAsVec() {
        HashMap<String,Double> q = new HashMap<String,Double>();
        for (int i=0; i<terms.size();i++) {
            String term = terms.get(i);
            if (q.containsKey(term)) {
                q.put(term,q.get(term) + weights.get(i));
            } else {
                q.put(term, weights.get(i));
            }
        }
        return q;   
    }

    private HashMap<String,Double> join(HashMap<String,Double> d1, HashMap<String,Double> d2) {
        for (Map.Entry<String,Double> me: d2.entrySet()) {
            if ( d1.containsKey( me.getKey() ) ) {
                d1.put( me.getKey(), d1.get(me.getKey()) + me.getValue() );
            } else {
                d1.put( me.getKey(), me.getValue());
            }
        }
        return new HashMap<String,Double>(d1);
    }

    private void multiply(double c, HashMap<String,Double> d) {
        for (String key : d.keySet()) {
            d.put(key, d.get(key)*c);
        }
    }

    private void normalize(HashMap<String,Double> d) {
        double norm = lengthOfWeights(d);
        multiply( (1/norm), d);
    }

    public String toString() {
        String out="";
        for (int i = 0; i<weights.size();i++) {
            out += terms.get(i) + ": " + weights.get(i)+ "\n";
        }
        return out;
    }

    private void printMap(HashMap<String,Double> m) {
        for (Map.Entry<String,Double> me : m.entrySet()) {
            System.out.println("Term: "+me.getKey()+", value: "+me.getValue());
        }
    }
}

    
