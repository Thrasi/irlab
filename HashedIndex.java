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
import java.util.ArrayList;
import java.lang.Math;

import java.util.*;
import java.io.*;
import java.util.stream.*;
/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
    // private int nrOfDocs = 17500;
    private double PAGERANKPROPORTION = 0.99;
    private boolean sublinearScaling = false;
    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    private HashMap<Integer,HashMap<String,Integer>> bagsOfWords = new HashMap<Integer,HashMap<String,Integer>>();
    // private int[] lengths = new int[nrOfDocs];
    private HashMap<String,Double> pageRank = readName2RankFromFile("ir/pagerank/pageRankMap");
    private double maxPageRank = getMaxPageRank();
    // private HashMap<Integer,Double> magnitude = new HashMap<Integer,Double>();



    /**
     *  Inserts this token in the index.
     */
    // public HashedIndex() {
    //     readIndex();
    // }

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
        // docLengths.put(docID,docLengths) += 1;
        lengths[docID] += 1;
        addToBags( token, docID );
        // System.err.println(token);
    }

    private void addToBags( String token, int docID ) {
        // HashMap<String,Integer> bag = bagsOfWords.containsKey(docID) ? bagsOfWords.get(docID) : new HashMap<String,Integer>();
        // int count = bag.containsKey(token) ? bag.get(token) + 1 : 1;
        // bag.put(token, count );
        // bagsOfWords.put(docID, bag);
        if ( bagsOfWords.containsKey(docID) ) {
            HashMap<String,Integer> bag = bagsOfWords.get(docID);
            int count = bag.containsKey(token) ? bag.get(token) : 0;
            count++;
            bag.put(token, count);
        } else {
            HashMap<String,Integer> bag = new HashMap<String,Integer>();
            bag.put(token, 1);
            bagsOfWords.put(docID, bag);
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
            pl = rankedQuery( query );
            double p = getPageRankProportion(rankingType);
            // updateScores(pl,p);
            Collections.sort(pl.getList());
		}
		
		return pl;
    }

    public double idf( String term ) {
        PostingsList pl = index.get( term );
        double N = docLengths.size();//lengths.length;
        // System.out.println("N: "+N);
        // System.out.println("pl.getDF()+1: "+(pl.getDF()+1));
        double df = pl.getDF();
        return Math.log( (N+2) / (df+1) );
    }



    private PostingsList rankedQuery( Query query ) {

        PostingsList answerList = null;
        PostingsList currentList = null;
        
        scores.clear();
        magnitude.clear();
        int weightIdx = 0;
        double weightSum = 0;
        double norm = 0;

        


        for (double d : query.weights) {
            norm += d*d;
        }
        norm = Math.sqrt(norm);




        for (int i = 0; i<query.size();i++) {
            String term = query.terms.get(i);
            if ( index.containsKey( term ) == false ) {
                continue;
            }
            double termWeight = query.weights.get(weightIdx) / norm;
            double idf = idf( term );
            double tf = 0;
            currentList = index.get( term );

            weightSum += termWeight*termWeight;
            System.out.println(idf);
            for ( PostingsEntry pe : currentList.getList() ) {
                tf = pe.getTF();
                // System.out.println("tf: "+tf);
                // System.out.println("idf: "+idf);
                double score = tf * idf * termWeight;
                if (scores.containsKey(pe.docID)) {
                    scores.put(pe.docID, scores.get(pe.docID) + score);
                    magnitude.put(pe.docID, magnitude.get(pe.docID) + score*score );
                } else {
                    scores.put(pe.docID, score);
                    magnitude.put(pe.docID, score*score );
                }
            }
            weightIdx++;

        }
        
        System.out.println("sum of weights squared: "+weightSum);
        answerList = new PostingsList();


        LinkedList<PostingsEntry> tmpList = new LinkedList<PostingsEntry>();
        PostingsEntry pe = null;
        for (int doc : scores.keySet()) {
            
            double tfidf;
            if (magnitude.get(doc)==0) {
                tfidf = 0;
            } else {
                // tfidf = scores.get(doc) / Math.sqrt(magnitude.get(doc));//lengths[doc];//Math.sqrt(lengths.get(doc));
                tfidf = scores.get(doc) / lengths[doc];//Math.sqrt( lengths[doc] );//Math.sqrt(lengths.get(doc));
                tfidf = scores.get(doc) / Math.sqrt( lengths[doc] );
                // tfidf = scores.get(doc);
                // tfidf = scores.get(doc) / Math.sqrt(docLengths.get(doc));
            }
            // System.out.println("magnitude: "+magnitude.get(doc));
            
            // double tfidf = scores.get(doc) / Math.sqrt(magnitude.get(doc));//lengths[doc];//Math.sqrt(lengths.get(doc));
            // double tfidf = scores.get(doc) / lengths[doc];//Math.sqrt(lengths.get(doc));
            // double tfidf = scores.get(doc);
            pe = new PostingsEntry(doc);
            pe.score = tfidf;
            tmpList.add(pe);
        }
        
        answerList.replaceList( tmpList );
        System.out.println("answerList: "+answerList.getList().size());
        
        return answerList;
    }

    private void updateScores(PostingsList pl, double p) {
        double maxTFIDF = 0;
        maxPageRank = 0;
        for (PostingsEntry pe : pl.getList()) {
            maxTFIDF = Math.max(maxTFIDF,pe.score);
            String docName = docIDs.get(""+pe.docID).split("/")[2];
            int len = docName.length()-2;
            docName = docName.substring(0,len);
            maxPageRank = Math.max(maxPageRank,pageRank.get(docName));
        }        
        for (PostingsEntry pe : pl.getList()) {
            String docName = docIDs.get(""+pe.docID).split("/")[2];
            int len = docName.length()-2;
            docName = docName.substring(0,len);
            // pe.score = p*pageRank.get(docName)/maxPageRank + (1-p)*pe.score/maxTFIDF;
            pe.score = p*pageRank.get(docName) + (1-p)*pe.score;
        }
    }

    private double getPageRankProportion(int rankingType) {
        double p = 0;
        if (rankingType == Index.TF_IDF) {
            p = 0;
        } else if (rankingType == Index.PAGERANK) {
            p = 1;
        } else if (rankingType == Index.COMBINATION) {
            p = PAGERANKPROPORTION;
        }
        return p;
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
							pe.addPosition( pp2.get(jj) );
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

    HashMap<String,Double> readName2RankFromFile(String fileName) {
        HashMap<String,Double> result = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            result = (HashMap<String,Double>)ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    double getMaxPageRank() {
        double max = 0;
        for (Map.Entry<String,Double> me : pageRank.entrySet()) {
            max = Math.max(max, me.getValue());
        }
        return max;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        try {
            System.out.println("Start writing index");
            String fileName = "ir/indexFiles/invertedIndex";
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(index);
            System.out.println("Start writing bagsOfWords");
            fileName = "ir/indexFiles/bagsOfWords";
            fout = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(index);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<String,Double> getBag(int docID) {
        HashMap<String,Double> map = new HashMap<String,Double>();
        for (Map.Entry<String,Integer> me : bagsOfWords.get(docID).entrySet() ) {
            map.put(me.getKey(),(double)me.getValue());
        }
        return map;
    }
}
