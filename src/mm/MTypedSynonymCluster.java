/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

/**
 * Adds the notion of a type to the synonyms. 
 * The type is a string, usually rendered in upper case.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MTypedSynonymCluster extends SynonymCluster  {
    public String type = null;
    
    public MTypedSynonymCluster( String type, String commaSeparated  ) {
        super( commaSeparated );
        this.type = type;
    }
    
    /**
     * Create a new list from a regular formatted text string (e.g., from a file)
     * @param fullline A string of the form "TYPE: term1, term2, term3, ...."
     */
    public MTypedSynonymCluster( String fullline  ) {
        fromString( fullline );
    }
    
    /** Makes a deep copy for purposes of saving in a state */
//    public MTypedSynonymCluster( MTypedSynonymCluster syns ) {
//        this( syns.toString() );
//    }
    
    /**
     * 
     * @param t a possible type label for this list
     * @return true if t is the type lable for this list, false otherwise
     */
    public boolean typeEquals( String t ) {
        if ( t.equalsIgnoreCase( this.type ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return false if the type is the empty string; true otherwise
     */
    public boolean hasType() {
        if ( type.equals( "" )) return false;
        else return true;
    }
    
    /**
     * Parses and stores a typed synonym string. 
     * @param s A string of the format the format is "TYPE: syn1, syn2, syn3, ... "
     * If there is no type, then the type is "" (not null)
     */
    public void fromString( String s ) {
        String parts[] = s.split( ":" );
        if ( parts.length == 1 ) {
            type = "";
            super.fromString( new String( parts[ 0 ] ) );
        }else {
            type = new String( parts[0]  /*.trim() */ );        // don't need to trim since it's next to the ":"
            super.fromString( new String( parts[ 1 ] ) );
        }  
    }
    
        /**
     * A simple data structure - just the comma-separated values.
     * May be preceded by a typelabel (if there is one) followed by a colon.
     */

    public String toString(){
        String out = "";
        if ( ! type.equals("")) {
            out += type + ": ";
        }
        out += super.toString();
        return out;
    }

    public String getType() {
        return type;
    }
    
    
    
}
