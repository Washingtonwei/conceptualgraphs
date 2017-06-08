/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import kb.BinaryTuple;
import kb.matching.AbstractConceptMatcher;
import kb.matching.AbstractTupleMatcher;
import kb.matching.MatchedBinaryTuple;

/**
 * A tuple matcher that incorporates synonym matching
 *
 * @see kb.matching.SynonymConceptMatcher
 * @author Harry Delugach
 */
public class MMExptTupleMatcher extends AbstractTupleMatcher {

    /**
     * Creates a new instance of a tuple matcher where the concepts will be
     * matched using the given concept matcher.
     *
     * @param matcher any concept matcher. If null, then use the
     * BasicConceptMatcher.
     */
    public MMExptTupleMatcher( AbstractConceptMatcher matcher ) {
        if ( matcher != null ) {
            this.conceptMatcher = matcher;
        } else {
            this.conceptMatcher = new kb.matching.SynonymConceptMatcher();
        }
    }

    /**
     * Creates a new instance using the SynonymConceptMatcher.
     *
     * @see kb.matching.SynonymConceptMatcher
     */
    public MMExptTupleMatcher() {
        this.conceptMatcher = new kb.matching.SynonymConceptMatcher();
        // Need to set synonyms right here
    }

    public String explainYourself() {
        String s = "MMExptTupleMatcher:\n<ul>\n";
        s += bullet( "Gather together all relations as binary relation tuples (n-ary where n>2 relations are split)." );
        s += bullet( "Don't match if relation label doesn't match." );
        s += bullet( "Match related concepts using the following concept matcher:" );
        if ( conceptMatcher != null ) {
            s += bullet( conceptMatcher.explainYourself() );
        }
        s += "</ul>\n";
        return s;
    }

    public float scoreTupleMatch( MatchedBinaryTuple masterTuple, MatchedBinaryTuple tupleToMatch ) {
        return 0f;
    }

    public float compare( BinaryTuple masterTuple, BinaryTuple tupleToMatch ) {
        return 0f;
    }
}
