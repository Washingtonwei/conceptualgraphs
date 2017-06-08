/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

import charger.Global;
import charger.obj.DeepIterator;
import charger.obj.GNode;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.util.CGUtil;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import kb.CoreferenceSet;
import kb.ObjectHistoryEvent;

/**
 * Combines two or more MMAT models on one sheet of assertion,
 * using synonyms to establish co-referent links between the graphs.
 * 
 * Has two modes, one where each model is enclosed in a CG context, the other where each model is inserted "flat" onto
 * the same sheet of assertion as the others.
 * @see GraphObjectCounterpartSet
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class ModelCombiner {
    
    /** What kind of join is intended. */
    public enum Mode { 
            /** Each graph will be inserted into its own enclosing context before joining. */
        NESTED,
            /** Graphs will be inserted "flat" onto the same sheet of assertion as the others. */
        FLAT
    }
    
    private int largeGridSize = 0;
    
    private static final int borderPadding = 30;
    private static final int betweenModelPadding = 100;
    private Mode mode = Mode.FLAT;
        
    private ArrayList<MModel> models = null;
    ArrayList<GraphObjectCounterpartSet> counterparts = null;
    
    private Graph combinedGraph = null;
    /**
     * Create a combiner for a set of graphs, where the counterparts are already determined.
     * @see MMAnalysisMgr
     * @param models The models to be combined -- should already include their graphs!
     * @param counterparts A predetermined set of counterpart sets, to be turned into co-referent sets.
     */
    public ModelCombiner( ArrayList<MModel> models, ArrayList<GraphObjectCounterpartSet> counterparts ) {
        this.models = new ArrayList<>();
        this.models = models;
        this.counterparts = counterparts;
        largeGridSize = (int) Math.ceil ( Math.sqrt( models.size() ) );
            Global.info( "At model combiner with grid size " + largeGridSize + " and " + models.size() + " models, with "
                    + counterparts.size() + " counterpart sets.");
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode( Mode mode ) {
        this.mode = mode;
    }

    
    /**
     * Gets the combined graph of the graphs in this model combiner.
   Uses the
     * counterparts (coreferent set) to determine where to join the graphs.
     * @param mode Whether they're to be combined "flat" (all in same context) or nested (each in its own context).
     * @return Combined graph
     */
    public Graph getCombinedGraph( Mode mode ) {
        /* Lay out graphs in a "table" that is roughly square.
         * Go row by row. Each model is in its own context
         * Create new graph with the first model. 
         * Then insert each model, translating by the already existing bounds.
         * Finally add co-referent links for each counterpart that has more than one concept.
         */
        if ( ( this.mode == mode ) && ( combinedGraph != null ) ) {
            return combinedGraph;
        } else {
            setMode( mode );
            combinedGraph = combineGraphs( mode, models );

            addCoreferentLinks( combinedGraph, counterparts );

            return combinedGraph;
        }
    }

    /**
     * Combined the graphs in each of the models according to the given mode
     *
     * @param mode Whether to nest each graph in its own context or all on the
     * same sheet of assertion.
     * @param models The models to be joined
     * @return the combined graph
     */
    private Graph combineGraphs( Mode mode, ArrayList<MModel> models )
    {
            /**  current row */
        int row = 0;
        /** current column */
        int column = largeGridSize;
        
        /** max height of previous rows */
        int previousRowHeight = 0;
        
        /** current row height */
        int currentRowHeight = 0;
        /**  width of current row */
        int currentWidth = 0;
        
        Graph combinedGraph = new Graph();
                // Arrange the graphs roughly as a row-column layout, roughly "square"
        for ( int currentSequence = 0; currentSequence < models.size(); currentSequence++ ) {
            if ( column == largeGridSize ) {
                column = 1;
                row++;
                
                previousRowHeight += currentRowHeight + 2 * betweenModelPadding;
                currentRowHeight = 0;
                currentWidth = 0;
            } else {
                column++;
            }
            
            MModel currentModel = models.get( currentSequence );
            Graph currentGraph = currentModel.getGraph();
            Rectangle2D.Double currentBounds = currentGraph.getContentBounds();
//                    Global.info( "put model " + currentModel.getID().getFilename() + " in row " + row + "; column " + column +
//                            ".\n  Bounds are " + currentBounds);
            // Create context and insert model, naming after model id's user name
            
                    // Slide the graph to the upper left corner so its bounds are true
            Point2D.Double toUpperLeft = new Point2D.Double( -1 * currentBounds.x, -1 * currentBounds.y );
            currentGraph.moveGraph( toUpperLeft );

            Rectangle2D.Double larger = currentGraph.getContentBounds();
            CGUtil.grow(  larger, borderPadding, borderPadding);
            currentGraph.setDisplayRect( larger );
//            currentGraph.setTextLabel( "Team_Model: " + currentModel.getID().getUser() );
            
            // translate x by currentWidth and translate y by previousRowHeight
            Point2D.Double overallTranslationPt = new Point2D.Double( currentWidth, previousRowHeight );
//            currentGraph.moveGraph( overallTranslationPt );
            
            if ( mode == Mode.NESTED ) {
                        // just insert the current graph into the combined graph as a nested graph
                combinedGraph.insertObject( currentGraph );
            } else if ( mode == Mode.FLAT ) {
                        // insert all the current graph's objects 
                Iterator iter = currentGraph.graphObjects();
                while ( iter.hasNext() ) {
                    combinedGraph.insertObject( (GraphObject)iter.next() );
                }
            }
            combinedGraph.resizeForContents( null );
            // set currentRowHeight to max of row height and height of this model
            if ( currentGraph.getContentBounds().height + borderPadding > currentRowHeight ) 
                currentRowHeight = (int)currentGraph.getDisplayRect().height + borderPadding;
                    
            // add  width of this model to currentWidth
            currentWidth += currentGraph.getDisplayRect().width + betweenModelPadding;
                    
            // add co-referent links  see Graph#insertObject
        }
        
//        cGraph.adjustDisplayRect( null );
        return combinedGraph;
        
    }

    /**
     * For each of the counterpart sets, establish co-referent links between its constituents.
     * @param g The graph in which all of the constituent concepts are supposed to be stored.
     * @param counterparts 
     */
    private void addCoreferentLinks( Graph g, ArrayList<GraphObjectCounterpartSet> counterparts ) {

        Global.info( "Adding co-referent links for " + counterparts.size() + " co-referent sets." );
        if ( counterparts.size() == 0 ) {
            return;
        }

        for ( GraphObjectCounterpartSet ocs : counterparts ) {
            ArrayList<GraphObject> corefSetObjects = ocs.getMatchedGraphObjects();
            if ( corefSetObjects.size() > 1 ) {
                CoreferenceSet corefset = new CoreferenceSet();
                for ( GraphObject go : corefSetObjects ) {
                    // Find the copy, because (while the id's match) the objects themselves are in the original model graphs!
                    GraphObject copiedObject = g.findByID( go.objectID );
                    if ( copiedObject == null ) {
                        Global.warning( "addCoreferentLinks: can't find object with id " + go.objectID );
                    }
                    else {
                         Global.warning( "addCoreferentLinks: FOUND object with id " + go.objectID );
                       corefset.addMember( (GNode)copiedObject );
                    }
                }
                Global.info( "Top level graph is " + g.objectID );
                // Create the co-referent links
                corefset.drawPrettyLines();
            }
        }
    }
}
