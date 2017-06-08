/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import java.util.ArrayList;
import kb.BinaryTuple;

/**
 * A wrapper for graph tuples so that we can attach MM models to it.
 * A "match" does not necessarily mean the identical object is in more than one model.
 * Matching may consider synonyms or other criteria. 
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class TupleCounterpartSet extends GraphObjectCounterpartSet {
    
    /** The graph tuple itself */
    public BinaryTuple tuple = null;
    
    /** In addition to the relation objects, also store the tuple itself. */
    public ArrayList<BinaryTuple> matchedTuples = new ArrayList<>();



    /** Start a new tuple counterpart set, using the model and tuple as a reference. */
    public TupleCounterpartSet( MModel model, BinaryTuple tuple ) {
        super( );
        this.model = model;
        this.tuple = tuple;
    }
    

  
    /**
     * Get the relation label associated with all of the tuples in this set.
     * @return the relation label associated with all of the tuples in this set.
     */
    public String getRelationLabel() {
        if ( tuple != null ) return tuple.relation_label;
        else return "";
    }
    
        /**
     * Adds a model to this tuple's list of models to which it matches.
     * If the model is already on the list, this method does nothing.
     * Also add the tuple that caused us to add its model.
     * @param m
     */
    public void addMatchedModel(MModel m, BinaryTuple tuple ) {
        if (! matchedModels.contains(m)) {
            this.matchedModels.add(m);
            this.matchedComponents.add(  tuple.relation );
            this.matchedTuples.add( tuple );
        }
    }


    
}
