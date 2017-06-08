/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

/**
 * Represents the abstraction of the  different kinds of analysis being performed in the MM experiments.
 * Currently just two kinds are supported.
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public enum MMComponentKind {
    /** Refers to analysis focused on concepts only. */
    CONCEPT, 
    
    /** Refers to analysis focused on  relations and their associated concepts. */
    RELATION;
}
