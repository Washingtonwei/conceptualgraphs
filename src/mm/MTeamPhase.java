/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;


import charger.obj.DeepIterator;
import charger.obj.Graph;
import charger.obj.GraphObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the models for a given team at a given phase. This is where the models themselves reside.
 * The hierarchical structure for the models is therefore
 * <pre>
 * MProject -
 * 
 *     MTeamPhase team 1 (beginning) .... contains models
 *     MTeamPhase team 1 (middle).... contains models
 *     MTeamPhase team 1 (end) .... contains models
 * 
 *     MTeamPhase team 2 (beginning) .... contains models
 *     MTeamPhase team 2 (middle) .... contains models
 *     MTeamPhase team 2 (end) .... contains models
 * ....
 * </pre>
 * Note that MPhase is merely a datatype, whereas this is a structure. MTeamMetrics holds metrics (but no models)
 * for an entire team, all phases.
 * @see MTeamMetrics
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class MTeamPhase {
 
        /** Used to keep the team information; easier to match team to its models, etc. The user name is irrelevant */
    public MModelID id = null;
    public ArrayList<MModel> models = new ArrayList<MModel>();
    
    public EnumMap<MMComponentKind, MCongruenceMetrics> congruenceMetrics = new EnumMap<>(MMComponentKind.class);
    
    

    public MTeamPhase( MModelID mid ) {
        id = mid;
        for ( MMComponentKind k : MMComponentKind.values() ) congruenceMetrics.put( k, null );
//        loadSynonymGroup();
    }
    
    /** Only sets the model's id -- it's up to the caller to explicitly add the model itself */
    public MTeamPhase( MModel model ) {
        this( model.getID() );
        //        loadSynonymGroup();
    }

    public MModelID getID() {
        return id;
    }

    public void setID(MModelID id) {
        this.id = id;
    }
    
       
    /**
     * Returns the model in this teamphase that matches the model id, returns null if not found
     * @param mid a valid model id.
     */
    public MModel findModelByID( MModelID mid ) {
        for ( MModel m : models ) {
            if ( m.getID().sameTeamPhase( mid )) return m ; 
        }
        return null;
    }
    
    public MModel findModel( MModel model ) {
        return findModelByID( model.getID() );
    }
    
    /**
     * Looks to see if the model id would belong to this team phase, whether it exists or not.
     * @param mid
     * @return true if model id would belong; false otherwise
     */
    public boolean belongsToMe( MModelID mid ) {
        if ( mid.sameTeamPhase( id ) ) 
            return true;
        else 
            return false;
    }
    
    public void addModel( MModel model ) throws MModelNameException {
        if ( ! belongsToMe( model.getID()) ) 
            throw new MModelNameException( "Model " + model.getID().toString() + "can't be added to teamphase " + id.toString() );
      //  if ( findModelByID( model.getID() ) // model is already there?
       //     throw new MMAnalysisException( "")
        models.add( model );
    }
    
    /**
     * Get the entire set of models for this team phase. 
     * @param includeSpecial If true, then also include an observer, or any other special model.
     * @return the set of models requested
     */
    public ArrayList<MModel> getModels( boolean includeSpecial ) {
        ArrayList<MModel> returnList = new ArrayList<MModel>();
        for (MModel m : models ) {
            if ( ! includeSpecial && ( m.isObserverModel() || m.isCombinedModel() ) ) {
                // only ignore special if we're not supposed to include it.
            } else {
                returnList.add( m );
            }
        }
        return returnList;
    }
    
    
    /**
     * Looks to see if any model thinks it's an observer model.
     * @return the model if there's an observer model; null otherwise.
     */
    public MModel getObserverModel() {
        for (MModel m : models ) {
            if ( m.isObserverModel() ) return m;
        }
        return null;
    }
    
    /**
     * Looks to see if any model thinks it's a team model.
     * @return the model if there's a team model; null otherwise.
     */
    public MModel getTeamModel() {
        for (MModel m : models ) {
            if ( m.isTeamModel() ) return m;
        }
        return null;
    }
    
    /**
     * Returns a combined (i.e., joined) model of all the non-special models.
        * This has no effect on whether there's a combined model in a file or not.
  * @return A combined model if one already existed (i.e., from the original set of models);
     * otherwise creates a new combined model and adds it to the teamphase. 
     */
    public MModel getCombinedModel( ) {
        if ( models.isEmpty() ) {
            return null;
        }
            for ( MModel m : models ) {
                if ( m.isCombinedModel() ) {
                    return m;
                }
            }
        // Here if there was no combined model, or if we have to create a flat one for analysis.
                // BUG: CONCEPT congruencemetrics wasn't initialized yet, but relations were!
        ModelCombiner combiner =
                new ModelCombiner( getModels( false ), congruenceMetrics.get( MMComponentKind.CONCEPT ).cDistinct );
        Graph graph = combiner.getCombinedGraph( ModelCombiner.Mode.FLAT);
        // construct a modelid for the combined model.
        MModelID combinedModelID = null;
        try {
            combinedModelID = new MModelID( getID().getFilename() );
        } catch ( MModelNameException ex ) {
            Logger.getLogger( MTeamPhase.class.getName() ).log( Level.SEVERE, null, ex );
        }
        combinedModelID.setUser( "combined" );
        MModel combinedModel = new MModel();
        combinedModel.setID( combinedModelID );
        combinedModel.setGraph( graph );
        try {
            addModel( combinedModel );
        } catch ( MModelNameException ex ) {
            Logger.getLogger( MTeamPhase.class.getName() ).log( Level.SEVERE, null, ex );
        }

        DeepIterator iter = new DeepIterator( graph );
        while ( iter.hasNext() ) {
            MMObjectHistoryEvent he = new MMObjectHistoryEvent( combinedModel );
            ( (GraphObject)iter.next() ).addHistoryEvent( he );
        }

        return combinedModel;
    }

    /**
     * Returns the "average" date of all the models in this team phase
     */
    public long getDate() {
        long sum = 0;
        int count = 0;
        for ( MModel m : models ) {
            long moddate = m.getModifiedDate();
            if ( moddate != 0 ) {       // ignore dates that are zero.
                count++;
                sum += moddate;
            }
        }
        if ( count == 0 ) return 0L;
        else return sum / count;
    }
    
}
