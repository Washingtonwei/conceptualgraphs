/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.matching;

import charger.obj.Concept;
import charger.util.Tag;

/**
 *
 * @author Harry Delugach
 */
public class BasicConceptMatcher extends AbstractConceptMatcher {

    public BasicConceptMatcher() {
    }
    
    protected boolean matchReferents = true;

    /**
     * Tells the matcher whether to bother matching referents or not.
     *
     * @param matchReferents
     */
    public void setMatchReferents( boolean matchReferents ) {
        this.matchReferents = matchReferents;
    }

    public boolean isMatchReferents() {
        return matchReferents;
    }

    @Override
    public boolean isIgnoreCase() {
        return super.isIgnoreCase();
    }

    @Override
    public void setIgnoreCase( boolean ignoreCase ) {
        super.setIgnoreCase( ignoreCase );
    }

    /**
     * The routine used for comparing strings. For this matcher, it's simply
     * string equality with possibly ignoring case. Can be overridden by
     * subclasses.
     *
     * @param s1
     * @param s2
     * @return true if they are equal (possibly ignoring case); false otherwise.
     */
    public boolean stringsEqual( String s1, String s2 ) {
        if ( isIgnoreCase() ) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        if ( s1.equals( s2 ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The routine used for comparing referents. For this matcher, same as
     * comparing type labels. Can be overridden by subclasses.
     *
      * @param masterConcept First concept. In some operations, it may be considered
      * a master concept for non-symmetric matching.
      * @param conceptToMatch Second concept. 
     * @return An agreed-upon value -- usually 0 if no match and 1 if there is.
     */
    public boolean referentsMatch( Concept masterConcept, Concept conceptToMatch ) {
        String s1 = masterConcept.getReferent();
        String s2 = conceptToMatch.getReferent();

        return stringsEqual( s1, s2 );
    }

    /**
     * The routine used for comparing types. For this matcher, just compare
     * strings Can be overridden by subclasses.
     *
      * @param masterConcept First concept. In some operations, it may be considered
      * a master concept for non-symmetric matching.
      * @param conceptToMatch Second concept. 
     * @return An agreed-upon value -- usually 0 if no match and 1 if there is.
     */
    public boolean typesMatch( Concept masterConcept, Concept conceptToMatch ) {
        String s1 = masterConcept.getTypeLabel();
        String s2 = conceptToMatch.getTypeLabel();
        return stringsEqual( s1, s2 );
    }

    /**
     * Give a description of the rules used, in HTML. This should probably be
     * more structured. Recommended that the explanation use the UL tag to list
     * the rules.
     */
    public String explainYourself() {
        String r = "";
        r += Tag.p( Tag.italic( "BasicConceptMatcher:" + Tag.br )
                + "Match referents: " + Tag.bold( isMatchReferents() + Tag.br )
                + "Ignore case: " + Tag.bold( isIgnoreCase() + "" ) );
        return r;
    }
    
    
}
