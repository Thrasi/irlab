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
		return index.get( token );
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
			// System.err.println("--------------------term: "+termIdx+"---------------");

			while ( i < currentList.size() && j < nextList.size() ) {
				boolean tt = true;
				if ( currentList.get(i).docID == nextList.get(j).docID ) {
					/*CHECK POSITIONS*/
					LinkedList<Integer> pp1 = currentList.get(i).positions();
					LinkedList<Integer> pp2 = nextList.get(j).positions();
					int iMax = pp1.size();
					int jMax = pp2.size();
					int ii=0, jj=0;
					/* As soon as we find a match we add the document */
					PostingsEntry pe = new PostingsEntry(currentList.get(i).docID);
					

					while ( ii < iMax && jj < jMax ) {
						if ( pp2.get(jj) - pp1.get(ii) == 1 ) {
							// if (tt) {
							// 	System.err.println("FOUND A MATCHING DOC: " + docIDs.get(""+currentList.get(i).docID));
							// 	tt=false;
							// }
							pe.addPosition( pp2.get(jj) );
							// System.err.println("FOUND A MATCHING OFFSET");
							// System.err.println(ii +" and "+ jj);
							// System.err.println("Offsets: "+pp1.get(ii) +" and "+ pp2.get(jj));
							ii++;
							jj++;
						} else {
							if ( pp1.get(ii) < pp2.get(jj) ) {
								ii++;
							} else {
								jj++;
							}
						}
					}

					if ( pe.positions().size() > 0 ) {
						answerList.add( pe );
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
		return currentList;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
