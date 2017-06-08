package kb.matching;

import charger.obj.Concept;

/**
    Abstraction of the operations for matching two concepts, where one is considered the "master".
     @since 3.8
 */
abstract public class AbstractConceptMatcher extends AbstractMatcher  {
 

    public AbstractConceptMatcher() {
    }
    /** Used in explainYourself methods */
        protected String bullet( String s ) { return "<li> " + s + "\n"; }

            
    /** Perform a possible comparison between two concepts. 
        Does not alter either of the tuples given as arguments.
        @param masterConcept the concept considered to be the "correct" one (if any)
        @param conceptToMatch the candidate concept that we want to match to the master.
        @return possible match score; zero otherwise.
    */
    public MatchDegree levelOfConceptMatch( Concept masterConcept, Concept conceptToMatch ) {
        boolean typesMatch = false;
        boolean referentsMatch = false;
        if ( typesMatch( masterConcept, conceptToMatch )) typesMatch = true;
        if ( referentsMatch( masterConcept, conceptToMatch )) referentsMatch = true;
        if ( typesMatch && referentsMatch ) return MatchDegree.BOTH;
        if ( typesMatch ) return MatchDegree.TYPE;
        if ( referentsMatch ) return MatchDegree.REFERENT;
        return MatchDegree.NONE;
    }
    
    /**
     * Performs whatever type matching is required by the matcher.
     * @param masterConcept
     * @param conceptToMatch
     * @return true if types match according to criteria.
     */
    abstract public boolean typesMatch( Concept masterConcept, Concept conceptToMatch );
    
    /**
     * Performs whatever referent matching is required by the matcher.
     * @param masterConcept
     * @param conceptToMatch
     * @return true if referents match according to criteria.
     */
    abstract public boolean referentsMatch( Concept masterConcept, Concept conceptToMatch );
    
    /**
     * An explanation of the matcher's particular algorithm
     * @return A string explaining itself.
     */
       abstract  public String explainYourself();

    
    
}