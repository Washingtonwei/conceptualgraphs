/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

/**
 * Identifiers for the (currently) three phases of each team's involvement.
 * @author Harry Delugach
 * @since Charger 3.8
 */
public enum MPhase {
    Beginning, Middle, End;
    
    /**
     *
     * @return a one-character lower case abbreviation for the phase; equivalent to abbr( 1 )
     */
    public String abbr() {
        return abbr( 1 );
    }
    
    /** @return a lower case abbreviation with the first len characters of the phase */
    public String abbr( int len ) {
        String s = super.toString().toLowerCase();
        return new String( s.substring( 0, len ) );
    }
    
    public static MPhase lookupAbbr1( String abbr1 ) {
        for ( MPhase p : MPhase.values() )
            if ( p.abbr().equalsIgnoreCase( abbr1 ) )
                return p;
        return null;
    }
}
