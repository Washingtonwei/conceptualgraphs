/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb;

/**
 * Identifies the type of object history event.
 * For each type of event, there is an abbreviation and 
 * a longer phrase describing it.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public enum ObjectHistoryEventType {
    
    // a short string and an output string 
    GRAPH( "graph","from graph"),
    FILE ("file", "from file"),
    USER ("user", "created by user"),
    MODEL ("model", "from model"),
    DERIVATION("derivation", "derived by/from"),
    CGIF( "cgif", "imported from CGIF"),
    UNKNOWN ("unknown", "unknown" );

    
    private String type;
    private String phrase;

    private ObjectHistoryEventType( String abbrev, String phrase ) {
        type = abbrev;
        this.phrase = phrase;
    }

    /**
     * Returns the type as a string.
     * @return the type as a string.
     */
    public String type() {
        return type;
    }
    
    /**
     * Gets a descriptive phrase for the type.
     * @return a descriptive phrase for the type
     */
    public String phrase() {
        return phrase;
    }
    
    /**
     * Find the right event type from the abbr string
     * @param s one of the abbreviations
     * @return the event type that corresponds to the string
     */
    public static ObjectHistoryEventType getTypeOf( String s ) {
        for ( ObjectHistoryEventType t : ObjectHistoryEventType.values() ) {
            if ( t.type().equals( s) )
                return t;
            if ( t.phrase().equals( s) )
                return t;
        }
        return UNKNOWN;
    }


    /**
     * Determines if the source object is equal to the target object.
     * @param t
     * @return whether two types are equal
     */
    public boolean equals( ObjectHistoryEventType t ) {
        if ( t.type.equals( this.type ) ) {
            return true;
        } else {
            return false;
        }
    }
}
