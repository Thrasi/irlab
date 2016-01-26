/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  YOUR CODE HERE
	//
		PostingsEntry pe = new PostingsEntry(docID);
	
		if  ( index.containsKey( token ) ) {
		    // add docId
		    //	    System.err.println("FOUND TOKEN IN KEYS");
		    PostingsList pl = index.get( token );
		    pl.add( pe );
		} else {
		    //	    System.err.println("TOKEN NOT IN KEYS");
		    PostingsList pl = new PostingsList();
		    pl.add ( pe );
		    index.put( token, pl );
		}
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
	return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
	return index.get( index );
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
	
	PostingsList pl = null;
	if ( queryType == Index.INTERSECTION_QUERY ) {
       	    pl = intersectionQuery( query );
	} 
	else if ( queryType == Index.PHRASE_QUERY ) {
	    
	} 
	else if ( queryType == Index.RANKED_QUERY ) {

	}
	
	return pl;
    }

    private PostingsList intersectionQuery( Query query ) {
	/**Implement intersection*/
	PostingsList answerList = null;
	PostingsList currentList = index.get( query.terms.get(0) );
	// System.out.println(currentList);
	
		for (int termIdx=1; termIdx < query.terms.size(); termIdx++ ) {
			answerList = new PostingsList();
			// System.err.println(termIdx);
			PostingsList nextList = index.get( query.terms.get(termIdx) );
			// System.out.println(nextList);
			int i=0, j=0;
			int k=0;

			while ( i < currentList.size() && j < nextList.size() ) {
				
				if ( currentList.get(i).docID == nextList.get(j).docID ) {
					// System.err.println("comparing: " + currentList.get(i).docID + " and " + nextList.get(j).docID);
					// System.err.println("were equal");
					// System.err.println("were equal, i: "+i);
					// currentList.remove(i);
					answerList.add( currentList.get(i) );
					i++;
					j++;
				} else {
					if ( currentList.get(i).docID < nextList.get(j).docID ) {
						// System.err.println("current was less");
						currentList.remove(i);
						// i++;
					} else {
						// System.err.println("next was less");
						j++;
					}
				}
			}
			// System.err.println("currlen: "+currentList.size());
			// System.err.println("currlen: "+answerList.size());

			/* Remove the rest of the unmatched elements of current. */
			// System.err.println("i: "+i);
			
			// for (;i<currentList.size();i++) {
			// 	currentList.remove(i);
			// }
			// System.err.println(currentList.size());
			currentList = answerList;
			// System.err.println(currentList.size());
		}
		// answerList = currentList;

		// System.err.println(answerList.size());
		LinkedList<String> ll = new LinkedList<String>();
		for (int h=0;h<currentList.size();h++) {
			// System.err.println(currentList.get(h).docID);
			ll.add(docIDs.get(""+currentList.get(h).docID));
		}
		Collections.sort(ll, String.CASE_INSENSITIVE_ORDER);
		for (int h=0;h<ll.size();h++) {
			System.out.println(ll.get(h));
		}
		return currentList;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
