/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.obj.GraphObject;
import java.util.ArrayList;

/**
 * Used in developing various congruence metrics. Encapsulates the abstraction of one graph object
 * that has "counterparts" in one or more models (graphs). Remembers a list of the models in which its 
 * counterparts appear. 
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class GraphObjectCounterpartSet {
    /** An English description of the matching algorithm used. Intended as a holder for the
     * explainYourself() methods of matchers.
     * */
    protected String matchExplanation = null;
    
        /** All models in which this object appears. A non-trivial instance will contain at least one model;
    otherwise, it doesn't appear in any model and we wouldn't be considering it in the first place.
     */
    protected ArrayList<MModel> matchedModels = new ArrayList();
        /** The objects themselves. Added at the same time as the model; .
     */
    protected ArrayList<GraphObject> matchedComponents = new ArrayList();
    
    /** The first model in which the graph object appears. It isn't special, but it's always there! */
    public MModel model = null;
    
    /** The number of models in which this object appears. More precisely, the number of models in which a
     * constrained match  returns true.
    A constrained match is one that may need to account for synonyms, whether to match referents, etc.)*/
    public int numMatches = 0;
    
    /** The graph object itself */
    public GraphObject obj = null;
    

    public GraphObjectCounterpartSet() {
    }

    /**
     * Adds a model to this graph object's list of models to which it matches.
     * If the model is already on the list, this method does nothing.
     * Also add the concept that caused us to add its model.
     *
     * @param go The object (either a relation or a concept) that
     * @param m The model in which the graph object appears.
     */
    
    public void addMatchedGraphObject(GraphObject go, MModel m) {
                    // BUG: if the counterpart set already contains a concept from this model, this is ignored.
                    // If we decide to allow more than one concept per model in this set, we need to make sure that
                    // congruence is determined by the number of models represented, NOT the number of concepts.
                    // always add the component, but do not add the model if we already have one of its concepts
        if (! matchedModels.contains(m)) {
            this.matchedModels.add(m);
        }
            this.matchedComponents.add(  go );
    }

    /**
     * Clear out all the graph objects and the list of matched models.
     */
    public void clearMatchedGraphObjects() {
        this.matchedModels.clear();
        this.matchedComponents.clear();
    }
    
    /**
     * Gets the id of the first model found in which this object appears.
     * @return the model ID of the first model identified -- i.e., the one that triggered this 
     * object's creation first.
     */
    public MModelID getID() {
        return model.getID();
    }

    /**
     * An English description of the matching algorithm used to legitimize this match.
     * This allows for matches to be achieved by different algorithms and yet still
     * be considered in one analysis run.
     * @see #setMatchExplanation
     * @return an English text string.
     */
    public String getMatchExplanation() {
        return matchExplanation;
    }

    /**
     * The set of models in which the objects of this counterpart set appear.
     * There should be one model per graph object (component) in this set.
     * @return A list of models in which the objects of this counterpart set appear.
     */
    public ArrayList<MModel> getMatchedModels() {
        return matchedModels;
    }
    
    public int getNumModels() {
        return matchedModels.size();
    }

    /**
     * The set of components (graph nodes) that are members of this counterpart set.
     * @return A list of components
     */
    public ArrayList<GraphObject> getMatchedGraphObjects() {
        return matchedComponents;
    }

    /**
     * One of the members of the counterpart set. It is probably the first object added 
     * to this set, but it has no special meaning or dominance over any other objects.
     * @return one of the members of this counterpart set. 
     */
    public GraphObject getObj() {
        return obj;
    }

    
    public void setObj( GraphObject obj ) {
        this.obj = obj;
    }

    
    
    public MModel getModel() {
        return model;
    }

//    public String getModelName() {
//        return getID().getName();
//    }
//
    public int getNumMatches() {
        return matchedComponents.size();
    }

    /** Determines whether a given model is in the list of matched models for this graph object. 
     * @return true if the object appears in the given model; false otherwise. */
    public boolean isMatchedModel(MModel m) {
        return matchedModels.contains(m);
    }

    /**
     * @see #getMatchExplanation()
     * @param matchExplanation an English text string explaining how a match is achieved.
     */
    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
    }

    public void setModel(MModel model) {
        this.model = model;
    }
    
}
