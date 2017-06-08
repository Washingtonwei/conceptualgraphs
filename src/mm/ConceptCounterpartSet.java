/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;


/**
 * A wrapper for graph objects so that we can attach MM models to it.
 * A "match" does not necessarily mean the identical object is in more than one model.
 * Matching may consider synonyms or other criteria. 
 * This structure corresponds informally to the notion of a coreferent set, in the sense
 * that the coreferents have been decided by criteria in the matcher.
 * @author Harry Delugach
 * @since Charger 3.8.0
 * @see kb.matching.SynonymConceptMatcher
 */
public class ConceptCounterpartSet extends GraphObjectCounterpartSet  {

    /**
     * Start a new counterpart set. For reference purposes, use the first model and object found.
     * @param model
     * @param object 
     */
    public ConceptCounterpartSet( MModel model, charger.obj.GraphObject object ) {
        this.model = model;
        this.obj = object;
    }
    
}
