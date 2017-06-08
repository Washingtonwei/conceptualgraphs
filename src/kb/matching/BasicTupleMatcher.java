package kb.matching;

import kb.matching.MatchedBinaryTuple;
import charger.obj.*;
import kb.BinaryTuple;

/**
    Basic concept/referent matcher, matches two tuples, one of which is considered the "master" 
    If relation names of tuples don't match, then give up.
    If corresponding concepts and referents match in a crude way, we get one point for matching.
    @see #scoreConcepts
     @since 3.5b2
 */
public class BasicTupleMatcher extends AbstractTupleMatcher
{
    public String nonAlphaNumericRegex = "[^a-zA-Z0-9]";
    
    
    /**
     * Instantiate a new tuple matcher with the default concept matcher.
     * @see AbstractTupleMatcher
     */
    public BasicTupleMatcher() {
        super( null );
    }
    
    /**
     * Instantiate a new tuple matcher with the given concept matcher.
     */
    public BasicTupleMatcher( AbstractConceptMatcher matcher ) {
        super( matcher );
    }
    
    
    public float compare( BinaryTuple masterTuple, BinaryTuple tupleToMatch )
    {
       // charger.Global.info( "comparing \"" + masterTuple.relation_label + "\" and \"" + tupleToMatch.relation_label + "\"." ); 
        
                // If relation label doesn't match, fail.
        if ( ! masterTuple.relation_label.equalsIgnoreCase( tupleToMatch.relation_label ) ) return 0;

        float s1 = scoreConcepts( masterTuple.concept1, tupleToMatch.concept1 );
        float s2 = scoreConcepts( masterTuple.concept2, tupleToMatch.concept2 );
        return s1 + s2;
    }

    /**
        Determines the matching score for the pair of tuples given.
        @see MatchedBinaryTuple
     */
    public float scoreTupleMatch( MatchedBinaryTuple masterTuple, MatchedBinaryTuple tupleToMatch )
    {
                //charger.Global.info( "comparing \"" + masterTuple.relation_label + "\" and \"" + tupleToMatch.relation_label + "\"." ); 
        
                // If relation label doesn't match, fail.
        if ( ! masterTuple.relation_label.equalsIgnoreCase( tupleToMatch.relation_label ) ) return 0;

        tupleToMatch.concept1_score = scoreConcepts( masterTuple.concept1, tupleToMatch.concept1 );
        tupleToMatch.concept2_score = scoreConcepts( masterTuple.concept2, tupleToMatch.concept2 );
        return tupleToMatch.getTotalScore();
    }
    
    /**
        Explains the basic matcher's rules:
          <ul>
            <li>
              Gather together all relations as binary relation tuples (n-ary 
              relations are split).
            </li>
            <li>
              Don't match if relation label doesn't match.
            </li>
            <li>
              For each concept, if type and referents match, then give 1 point 
              and stop.
            </li>
            <li>
              If types match, where master concept has no referent, but 
              matched concept does, then give 1 point and stop.
            </li>
            <li>
              Match referents using &quot;generous&quot; string matching, where either 
              can be a sub-string of the other, ignoring spaces, 
              capitalization and punctuation.
            </li>
          </ul>
     */
    public String explainYourself()
    {
        String s = "BasicTupleMatcher:\n<ul>\n";
        
        s += bullet( "Gather together all relations as binary relation tuples (n-ary relations are split)." );
        
        s += bullet( "Don't match if relation label doesn't match." );
        
        s += bullet( conceptMatcher.explainYourself() );

        s += "</ul>\n";
        
        return s;
    }
    
    
    /**
     * Calculates a "score" for matching two Charger concepts. 
     * @param c1
     * @param c2
     * @return the match score
     * @see #generousStringMatch(String, String)
     */
    protected float scoreConcepts( Concept c1, Concept c2 )
    {
        float score = 0;
        if ( generousStringMatch( c1.getTypeLabel(), c2.getTypeLabel() ) )
        {
            //score += 1;
            if ( generousStringMatch( c1.getReferent(), c2.getReferent() ) ) score += 1;
            if ( c1.getReferent().equals( "" ) && ! c1.getReferent().equals( "" ) ) score += 1;
        }
        return score;
    }
    
    /**
        Applies an optimistic algorithm to matching strings:
        <ul>
            <li>Ignore any characters other than [a-zA-Z0-9]
            <li>Ignore differences in upper or lower case
            <li>Accept if either is a substring of the other.
        </ul>
     */
    public boolean generousStringMatch( String ss1, String ss2 )
    {
        String s1 = ss1.replaceAll( nonAlphaNumericRegex, "" );
        String s2 = ss2.replaceAll( nonAlphaNumericRegex, "" );
        
                //if ( ! s1.equals( ss1 ) ) charger.Global.info( "old string: \"" + ss1 + "\"; new string \"" + s1 + "\"" );
                //if ( ! s2.equals( ss2 ) ) charger.Global.info( "old string: \"" + ss2 + "\"; new string \"" + s2 + "\"" );
        
        if ( s1.equalsIgnoreCase( s2 ) ) return true;
        
        if ( s1.contains( s2 ) || s2.contains( s1 ) ) return true;
        
        return false;
    }


}