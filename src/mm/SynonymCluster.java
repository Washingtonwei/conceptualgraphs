/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains a cluster of strings all considered to be synonyms of each other.
 * By default, is set to ignore case in comparisons.
 *  * A synonym group consists of one or more synonym clusters, each of which
 * is akin to a synset in Wordnet -- i.e., a list of terms that are all
 * considered synonyms to each other. There can be more than two terms
 * in a cluster. While a "cluster" with one term is valid, it won''t have any effect.

 * @see SynonymGroup
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class SynonymCluster {

    public ArrayList<String> terms = new ArrayList<>();
    /**
     * key is the possibly lower case term without spaces, value is the original term
     */
    public HashMap<String, String> hashtable = new HashMap<>();
    
    protected boolean ignoreCase = true;

    public SynonymCluster() {
    }

     /**
     * Make a new synonym list
     * @param commaSeparated A string of comma-separated terms
     */
    public SynonymCluster( String commaSeparated ) {
        fromString( commaSeparated );
        copyTermsToHash();
    }

   public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase( boolean ignoreCase ) {
        this.ignoreCase = ignoreCase;
    }
    
    /**
     * For the list of terms collected, store the term as the key,
     * and either itself or its lowercase version as the value.
     * Spaces are removed from the key also.
     */
    protected void copyTermsToHash() {
        hashtable.clear();
        for ( String term : terms ) {
                        // NOTE: this means that spaces are ignored in checking for a match 
            String t = term.replaceAll("\\s","");           // remove any spaces
            if ( isIgnoreCase() ) {
                hashtable.put( new String( t.toLowerCase() ), term );
            } else {
                hashtable.put( t, term );
            }
        }
    }
    
    

    /**
     * Load synonyms using the string. Clears all synonyms first.
     * @param s a properly formatted synonym string, comma separated.
     */
    public void fromString( String s ) {
        terms.clear();
        String[] newterms = s.split( "," );
        for ( String term : newterms ) {
//            if ( term.charAt( 0) != ' ' ) {
//                Global.info( "SynonymCluster.fromString: \"" + term + "\" doesn't start with space.");
//            }
            String t = new String( term.trim() );
//            String t = new String( term );       // TODO: this is another memory leak to fix?
            if ( ! t.equals( "" )   ) {
                if ( isIgnoreCase() )
                    terms.add( new String( t.toLowerCase()) );
                else
                    terms.add( t );
            }
        }
        copyTermsToHash();
    }

    /**
     * Creates a properly formatted synonym string, comma separated.
     * @return a properly formatted synonym string, comma separated.
     */
    public String toString() {
        StringBuilder out = new StringBuilder( "" );
        for ( String t : terms ) {
            out.append( t );
            if ( terms.indexOf( t ) != ( terms.size() - 1 ) ) {
                out.append( ", " );
            }
        }
        return out.toString();
    }

    /**
     * Determines whether the given string appears anywhere in the list, ignoring case.
     * This one is really inefficient, should be avoided unless necessary.
     * @param pattern
     * @return true if the given string appears anywhere in the list, ignoring case; false otherwise.
     */
    public boolean containsIgnoreCase( String pattern ) {
        for ( String t : terms ) {
            if ( t.equalsIgnoreCase( pattern ) ) {
                return true;
            }
        }
        return false;
    }

        /**
     * Determines whether the given string appears anywhere in the list, respecting case.
     * @param pattern
     * @return true if the given string appears anywhere in the list, respecting case; false otherwise.
     */
     public boolean contains( String pattern ) {
         String s = hashtable.get( pattern );
         if ( s != null )
             return true;
         else
             return false;
     }

    /**
     * Find a match between two terms. If the terms are identical, then they trivially match.
     * If ignore case is set, then it ignores case.
     * @param s1
     * @param s2
     * @return Whether the pair matches according to its string value only.
     */
    public boolean pairMatches( String s1, String s2 ) {
//            Global.info( "test pair matches: " + s1 + ", " + s2 );
        if ( contains( s1 ) && contains( s2 ) ) {
            return true;
        } else {
            return false;
        }
    }
}