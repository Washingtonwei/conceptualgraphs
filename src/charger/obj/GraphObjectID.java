/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger.obj;

import java.rmi.server.UID;

/**
 * Represents a graph object's ID.
 * Although these are currently strings, it's helpful to have their 
 * own sub-class for type checking and for possible future enhancements.
 * Regardless of the implementation, each new ID must be unique.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public final class GraphObjectID  {
    /** the string that is the unique identifier for an object. 
     * If constructed with a null string, then the string value is "-1".
     * */
    private String ident = null;
    
    /**
     * Represents an ident that is not attached to any object; e.g., if a graph has no owner ident.
     */
    public static final GraphObjectID zero = new GraphObjectID( "0" );
    
    /**
     * Create a new unique-to-the-universe id.
     */
    public GraphObjectID() {
        ident = new UID().toString();
    }
    
    /**
     * Create an id from the given string. Note that this allows there to be
     * more than one of any given id, thus violating the basic constraint.
     * This assumes that the given string will have come from a properly-created GOID.
     * @param s 
     */
    public GraphObjectID( String s ) {
        if ( s == null ) 
            ident = "-1";
        else 
            ident = s;
    }
    
    /** The string representation of this id. 
     * @return A unique string representation of this id.
     * */
    public String toString() {
        return ident;
    }

    /**
     * Two objects are equal if their string representations are equal.
     * @param other the id to compare
     * @return true if they are represented by the same string; false otherwise.
     */
    public boolean equals( GraphObjectID other ) {
        if ( other.ident.equals( this.ident ) ) 
            return true;
        else 
            return false;
    }
    
    /**
     * For debugging and other purposes, create a short version of the unique string representation.
     * Not necessarily unique across all implementations, but still locally useful.
     * @return up to 6 characters of more-or-less distinct identifying information.
     */
    public String getShort() {
        final int shortLength = 6;
        String[] parts = ident.split(":");
        String last = parts[ parts.length - 1 ];
        if ( last.length() <= shortLength ) 
            return last;
        else
            return last.substring( last.length() - shortLength );
            
    }
}
