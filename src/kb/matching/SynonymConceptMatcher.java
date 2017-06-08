/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.matching;

import charger.obj.Concept;
import charger.util.Tag;
import java.util.ArrayList;
import mm.ClusterCollection;
//import mm.*;

/**
 * Encapsulates a synonym matcher, using the BasicConceptMatcher policies with the addition of synonym clusters.
 * Note that synonym clusters can be disabled, rendering this  identical to the BasicConceptMatcher.
 * Synonyms are only applied to the referent matching, NOT to the types.
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class SynonymConceptMatcher extends BasicConceptMatcher {

    private ArrayList<ClusterCollection> synonyms = new ArrayList<>();
    private ClusterCollection mergedSynonyms = null;
    private boolean synonymsEnabled = true;
    private boolean ignoreSpaces = true;
    
    private int maxSynonymsToShow = 0;
    private boolean showSynonymFilename = true;
    
    public SynonymConceptMatcher() {
    }

    /**
     * Get the synonym groups associated with this matcher.
     * @return A collection of synonym sets
     */
    public ArrayList<ClusterCollection> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms( ArrayList<ClusterCollection> syns) {
        mergedSynonyms = new ClusterCollection();
        this.synonyms = syns;
        for (  ClusterCollection g : syns ) {
            mergedSynonyms = mergedSynonyms.mergeCollections( g );
        }
    }

    /**
     * Tell the matcher to use synonym clusters for the referents of concepts being matched.
     * The synonym clusters may be typed, which restricts each typed synonym set to its respective type.
     * @param synonymsEnabled 
     * @see mm.MTypedSynonymCluster
     */
    public void setSynonymsEnabled(boolean synonymsEnabled) {
        this.synonymsEnabled = synonymsEnabled;
    }

    public boolean isSynonymsEnabled() {
        return synonymsEnabled;
    }

    public int getMaxSynonymsToShow() {
        return maxSynonymsToShow;
    }

    public void setMaxSynonymsToShow( int maxSynonymsToShow ) {
        this.maxSynonymsToShow = maxSynonymsToShow;
    }

    public boolean isShowSynonymFilename() {
        return showSynonymFilename;
    }

    public void setShowSynonymFilename( boolean showSynonymFilename ) {
        this.showSynonymFilename = showSynonymFilename;
    }

    /** Whether to ignore spaces in concept referent names or not */
    public boolean isIgnoreSpaces() {
        return ignoreSpaces;
    }

    public void setIgnoreSpaces( boolean ignoreSpaces ) {
        this.ignoreSpaces = ignoreSpaces;
    }
    
    

    /**
     * Describe in human readable form the operation of this matcher.
     * Gives its type name, whether it matches referents or not,
     * whether it uses synonym clusters or not, whether it ignores case,
     * and lists the synonym clusters it uses.
     * @return The matcher's own explanation of itself
     */
     public String explainYourself(  ) {
         String r = "";
        r +=  Tag.p( Tag.italic( "SynonymConceptMatcher:" + Tag.br ) +
                "Match referents: " + Tag.bold( isMatchReferents() + Tag.br  )  +
                "Use synonyms if present: " + Tag.bold( isSynonymsEnabled() + Tag.br ) + 
                "Ignore case: " + Tag.bold( isIgnoreCase() + ""  )  );
        if ( synonymsEnabled ) {
            if ( synonyms.isEmpty() ) {
                r = r + Tag.p( "No synonyms present." + Tag.br );
            } else {
                r += "Synonyms used.";
//                for ( ClusterCollection sgroup : synonyms ) {
//                    r = r + sgroup.formatAsHTML( false );
//                }
            }
        } else {
            r += "Synonyms NOT used.";
        }
        return r;
    }
     
     
    
     /**
      * The routine used for comparing referents; if enabled, will also  consider synonym clusters. 
      * If types don't match, then fail. If referents match outright, then succeed.
      * If synonym clusters are disabled, then we stop there. Otherwise we use 
      * the synonym clusters to drive the referent match.
      * @param masterConcept First concept. In some operations, it may be considered
      * a master concept for non-symmetric matching.
      * @param conceptToMatch Second concept. 
      * @return true if they match according to the criteria. 
      */
    @Override
    public boolean referentsMatch( Concept masterConcept, Concept conceptToMatch ) {
        // First try matching without any synonym clusters, maybe we'll get lucky
        
        // if type doesn't match, don't bother going further
        if ( !super.typesMatch( masterConcept, conceptToMatch ) ) {
            return false;
        }
        
                // if types match, then we can keep going. If identical already, congratulations!

        String s1 = masterConcept.getReferent();
        String s2 = conceptToMatch.getReferent();

        if ( isIgnoreSpaces() ) {   // strip spaces if either contains spaces
            if ( s1.contains( " " )) s1 = s1.replaceAll("\\s","");
            if ( s2.contains( " " )) s2 = s2.replaceAll("\\s","");
        }
        
        if ( isIgnoreCase() ) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        
        if ( s1.equals( s2 ) ) 
            return true;
//        if ( super.referentsMatch( masterConcept, conceptToMatch ) ) {
//            return true;
//        }
       

        if ( !isSynonymsEnabled() ) {
            return false;      // if no synonymSets, then we're done
        }

//        if ( isIgnoreCase() ) {
//            return mergedSynonyms.match( masterConcept.getTypeLabel().toLowerCase(), 
//                    s1.toLowerCase(), s2.toLowerCase() );
//        } else {
            return mergedSynonyms.match( masterConcept.getTypeLabel(), s1, s2 );
//        }

    }

}
