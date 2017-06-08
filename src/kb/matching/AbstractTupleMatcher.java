package kb.matching;

import kb.BinaryTuple;
import kb.matching.MatchedBinaryTuple;

/**
    Abstraction of the operations for matching two tuples, where one is considered the "master".
    * A tuple matcher has a concept matcher, which is by default the BasicConceptMatcher, but 
    * can be set to any concept matcher desired.
     @since 3.5b2
 */
abstract public class AbstractTupleMatcher extends AbstractMatcher
{
    public AbstractConceptMatcher conceptMatcher = new BasicConceptMatcher();
    
    
    /**
     * Creates a new instance of a tuple matcher where the concepts will be matched using the given concept matcher.
     * @param matcher any concept matcher. If null, then use the BasicConceptMatcher.
     */
    public AbstractTupleMatcher( AbstractConceptMatcher matcher ) { 
        if ( matcher != null ) {
            this.conceptMatcher = matcher;
        }
    }
    
    public AbstractTupleMatcher() {
        this.conceptMatcher = new BasicConceptMatcher();
    }
    
    
    
    /** Perform a possible comparison between two tuples. 
        Does not alter either of the tuples given as arguments.
        @param masterTuple the relation tuple considered to be the "correct" one (if any)
        @param tupleToMatch the candidate tuple that we want to match to the master.
        @return possible match score; zero otherwise.
        @see #scoreTupleMatch
    */
    abstract public float compare( BinaryTuple masterTuple, BinaryTuple tupleToMatch );
    
    /**
        Performs the comparison between master and matchable tuple.
        May alter either of the tuples given as arguments.
        @return match score; zero otherwise.
        @see #compare
     */
    abstract public float scoreTupleMatch( MatchedBinaryTuple masterTuple, MatchedBinaryTuple tupleToMatch );

    
        protected String bullet( String s ) { return "<li> " + s + "\n"; }


    public AbstractConceptMatcher getConceptMatcher() {
        return conceptMatcher;
    }

    public void setConceptMatcher(AbstractConceptMatcher conceptMatcher) {
        this.conceptMatcher = conceptMatcher;
    }
    
    
}