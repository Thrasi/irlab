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
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.ClassNotFoundException;
import java.util.Arrays;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    public HashMap<String, String> docIDs = new HashMap<String,String>();
    private int size = 0;
    private int indexFileCount = 0;
    private File[] listOfFiles;
    private int n = 2;

    public HashedIndex() {
    	countIndexFiles();
		System.out.println( "Number of existing index files: " + indexFileCount );
    }


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  YOUR CODE HERE
	//
    	PostingsList pl;
		PostingsEntry pe = new PostingsEntry(docID, offset);
	
		if  ( index.containsKey( token ) ) {
		    pl = index.get( token );
		} else {
		    pl = new PostingsList();
		    index.put( token, pl );
		}
		pl.add( pe );
		size++;
    }

    public void countIndexFiles() {
    	File folder = new File("indexFiles/");
		listOfFiles = folder.listFiles();
		Arrays.sort( listOfFiles );
		indexFileCount = listOfFiles.length;
    }

    public int getIndexFileCount() {
    	return indexFileCount;
    }

    public int getSize() {
    	return size;
    }

    private HashMap<String,PostingsList> loadIndexFile(String fileName) throws IOException, ClassNotFoundException {

    	HashMap<String,PostingsList> tempIndex = new HashMap<String,PostingsList>();
    	try {
	    	File f = new File("indexFiles/"+fileName);
	    	FileInputStream fis = new FileInputStream(f);
			ObjectInputStream soi = new ObjectInputStream(fis);
			tempIndex = (HashMap<String,PostingsList>) soi.readObject();
			soi.close();
		} catch (FileNotFoundException e) {
			System.err.println("File: " + fileName + " does not exist yet.");
		}
		return tempIndex;
   	}

    private void saveIndexFile(String fileName, HashMap<String,PostingsList> idx) throws IOException, ClassNotFoundException {
    	File file = new File( "indexFiles/"+fileName );
    	FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(idx);
        oos.close();
    }

    private String extractLetters(String token) {
    	int m = (token.length() > 1) ? 2 : 1;
    	int tl = token.length();
    	if ( tl > 2 ) {
    		m = 3;
    	} else if ( tl > 1 ) {
    		m = 2;
    	} else {
    		m = 1;
    	}
    	return token.substring(0,m);
    }

    public void saveToFile() throws IOException, ClassNotFoundException {
    	
    	// File file;// = new File("indexFiles/indexFile_"+ ++indexFileCount );

     //    FileOutputStream fos;// = new FileOutputStream(file);
     //    ObjectOutputStream oos;// = new ObjectOutputStream(fos);
     //    // oos.writeObject(index);
     //    // oos.close();

        SortedSet<String> keys = new TreeSet<String>(index.keySet());
        String currentLetter = extractLetters( keys.first() );
        String tokenLetter;
        System.out.println(currentLetter);

        HashMap<String,PostingsList> tempIndex = new HashMap<String,PostingsList>();
        tempIndex = loadIndexFile(currentLetter);
        

        for (String token : keys) {
        	tokenLetter = extractLetters(token);
        	
        	if ( !currentLetter.equalsIgnoreCase( tokenLetter ) ) {
        		saveIndexFile(currentLetter, tempIndex);
        		currentLetter = tokenLetter;
		        tempIndex = loadIndexFile(currentLetter);
        	}
        	// System.out.println(currentLetter);

        	if ( tempIndex.containsKey(token) ) {
        		tempIndex.get(token).addEntriesOf( index.get(token) );
        	} else {
        		tempIndex.put(token, index.get(token));
        	}
        	
        }

        saveIndexFile(currentLetter, tempIndex);
    }

    public void saveDocIDs() throws IOException, ClassNotFoundException {
    	File file = new File( "docids/docids" );
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.docIDs);
        oos.close();
    }

    public void loadDocIDs() throws IOException, FileNotFoundException, ClassNotFoundException {
    	File file = new File( "docids/docids" );
    	FileInputStream fis = new FileInputStream(file);
		ObjectInputStream soi = new ObjectInputStream(fis);
		this.docIDs = (HashMap<String,String>) soi.readObject();
    }


    public void clear() {
    	size = 0;
    	// index = new HashMap<String,PostingsList>();
        index.clear();
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
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) throws IOException, FileNotFoundException, ClassNotFoundException {
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

    /*
	aggregates the PostingsList of a token from all index files.

    */

    private PostingsList getPostingsList(String token) throws IOException, FileNotFoundException, ClassNotFoundException {
    	PostingsList aggregatedList = null;
    	PostingsList tempList = null;
    	int i=0;

    	index = loadIndexFile( extractLetters(token) );
    	return index.get( token );


    	// for ( File f : listOfFiles ) {
    	// 	System.out.println(i++);
    	// 	System.out.println(f.getName());
    	// 	// FileInputStream fis = new FileInputStream(f);
    	// 	// ObjectInputStream soi = new ObjectInputStream(fis);
    	// 	// index = (HashMap<String,PostingsList>) soi.readObject();
    	// 	// soi.close();

    	// 	index = loadIndexFile( "indexFiles/"+token.substring(0,1) );
    	// 	tempList = index.get( token );
    	// 	if ( tempList != null ) {
    	// 		if ( aggregatedList != null ) {
    	// 			aggregatedList.addEntriesOf( tempList );
    	// 		} else {
    	// 			aggregatedList = tempList;
    	// 		}
    	// 	}
    	// }
    	// return aggregatedList;
    }

    private PostingsList phraseQuery( Query query ) throws IOException, FileNotFoundException, ClassNotFoundException {
    	PostingsList answerList = null;
		PostingsList currentList = getPostingsList( query.terms.get(0) );
		for (int termIdx=1; termIdx < query.terms.size(); termIdx++ ) {
			answerList = new PostingsList();
			PostingsList nextList = getPostingsList( query.terms.get(termIdx) );
			int i=0, j=0;
			// System.err.println("--------------------term: "+termIdx+"---------------");
			if (currentList == null || nextList == null) { return null; }

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


    private PostingsList intersectionQuery( Query query ) throws IOException, FileNotFoundException, ClassNotFoundException {
		PostingsList answerList = null;
		PostingsList currentList = getPostingsList( query.terms.get(0) );
		// System.out.println("intersectionQuery");
		// System.out.println(currentList);
		for (int termIdx=1; termIdx < query.terms.size(); termIdx++ ) {
			answerList = new PostingsList();
			PostingsList nextList = getPostingsList( query.terms.get(termIdx) );
			// System.out.println(nextList);
			int i=0, j=0;
			if (currentList == null || nextList == null) { return null; }

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
