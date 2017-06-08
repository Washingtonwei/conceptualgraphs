/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.matching;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
/**
 * An indication of the degree to which the concepts matched.
 */
public enum MatchDegree {

    /**
     * Neither type nor referent matched according to whatever criteria are
     * specified.
     */
    NONE,
    /**
     * Just type (not referent) matched according to whatever criteria are
     * specified.
     */
    TYPE,
    /**
     * Just referent (not type) matched according to whatever criteria are
     * specified.
     */
    REFERENT,
    /**
     * Both type and referent matched according to whatever criteria are
     * specified.
     */
    BOTH
}
