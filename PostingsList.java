/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;
/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
	   return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	   return list.get( i );
    }

    public void remove( int index ) {
        list.remove(index);
    }

    /* document frequency */
    public int getDF() {
        return list.size();
    }

    public LinkedList<PostingsEntry> getList() {
        return list;
    }

    //
    //  YOUR CODE HERE
    //
    /** Works if the list is sorted and the entry e is always greater or equal to all previous. */
    
    /*  If last document has lower index it is a new entry.
        If it is equal to last index it is a new position    
    */
    public void add( PostingsEntry e ) {
    	int size = list.size();
    	if ( size > 0 ) {
    	    if ( list.getLast().docID < e.docID ) {
    		  list.add( e );
    	    } else if ( list.getLast().docID == e.docID ) {
                list.getLast().addPosition( e.positions().get(0) );
            }
    	} else {
    	    list.add( e );
    	}
    }

    public void replaceList(LinkedList<PostingsEntry> l) {
        list = l;
    }

    public String toString() {
        String output="\n";
        for (int i=0;i<list.size();i++){
            output = output + list.get(i).docID + ", ";
        }
        output = output + "\n";
        return output;
    }
}
	

			   
