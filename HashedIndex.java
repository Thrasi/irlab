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
		PostingsEntry pe = new PostingsEntry(docID, offset);
	
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
			pl = phraseQuery( query );    
		} 
		else if ( queryType == Index.RANKED_QUERY ) {

		}
		
		return pl;
    }

    private PostingsList phraseQuery( Query query ) {
    	PostingsList answerList = null;
		PostingsList currentList = index.get( query.terms.get(0) );
	
		for (int termIdx=1; termIdx < query.terms.size(); termIdx++ ) {
			answerList = new PostingsList();
			PostingsList nextList = index.get( query.terms.get(termIdx) );
			int i=0, j=0;

			while ( i < currentList.size() && j < nextList.size() ) {
				if ( currentList.get(i).docID == nextList.get(j).docID ) {
					/*CHECK POSITIONS*/
					LinkedList<Integer> pp1 = currentList.get(i).positions();
					LinkedList<Integer> pp2 = nextList.get(j).positions();
					int ii=0, jj=0;
					/* As soon as we find a match we add the document */
					while ( ii < pp1.size() && jj < pp2.size() ) {
						if ( pp1.get(ii) + termIdx == pp2.get(jj) ) {
							answerList.add( currentList.get(i) );
							break;
						} else {
							if ( pp1.get(ii)+termIdx < pp2.get(jj) ) {
								ii++;
							} else {
								jj++;
							}
						}
					}
					i++;
					j++;
				} else {
					if ( currentList.get(i).docID < nextList.get(j).docID ) {
						currentList.remove(i);
					} else {
						j++;
					}
				}
			}
			currentList = answerList;
		}
		return currentList;
    }

    private PostingsList intersectionQuery( Query query ) {
		PostingsList answerList = null;
		PostingsList currentList = index.get( query.terms.get(0) );
	
		for (int termIdx=1; termIdx < query.terms.size(); termIdx++ ) {
			answerList = new PostingsList();
			PostingsList nextList = index.get( query.terms.get(termIdx) );
			int i=0, j=0;

			while ( i < currentList.size() && j < nextList.size() ) {
				if ( currentList.get(i).docID == nextList.get(j).docID ) {
					answerList.add( currentList.get(i) );
					i++;
					j++;
				} else {
					if ( currentList.get(i).docID < nextList.get(j).docID ) {
						currentList.remove(i);
					} else {
						j++;
					}
				}
			}
			currentList = answerList;
		}
		// LinkedList<String> ll = new LinkedList<String>();
		// for (int h=0;h<currentList.size();h++) {
		// 	ll.add(docIDs.get(""+currentList.get(h).docID));
		// }
		// Collections.sort(ll, String.CASE_INSENSITIVE_ORDER);
		// for (int h=0;h<ll.size();h++) {
		// 	System.out.println(ll.get(h));
		// }
		return currentList;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
