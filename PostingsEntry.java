/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score = 0;
    private LinkedList<Integer> positions = new LinkedList<Integer>();
    private double tfidf;

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	   return Double.compare( other.score, score );
    }

    //
    //  YOUR CODE HERE
    //
    public PostingsEntry(int docID) {
        this.docID = docID;
    }

    public PostingsEntry(int docID, int offset) {
        this.docID = docID;
        this.addPosition(offset);
    }

    public LinkedList<Integer> positions() {
        return positions;
    }

    /* term frequency */
    public int getTF() {
        return positions.size();
    }

    /* As we add a new position the term frequency increases*/
    public void addPosition(int offset) {
        positions.add(offset);
    }

}

    
