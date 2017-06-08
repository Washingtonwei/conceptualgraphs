/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger;

import charger.Global;
import charger.obj.Graph;
import charger.undo.UndoableState;
import kb.KnowledgeBase;

/**
 * A complete copy of the editing state, with the current graph, and
 * the current knowledge base
 * @since Charger 3.8.3
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class EditorState extends UndoableState {
        /** A CGX string that is parseable to form the current graph */
    String graph = null;
    /** Remembers whether this state represents a change from the last save, backup, etc. 
     *     @see EditFrame#somethingHasChanged
     * Note this is not the same as whether the state itself has changed */
    boolean somethingHasChanged = true;
    
    /** A copy of the ENTIRE knowledge base. */
    KnowledgeBase kb = null;
    
    
    /** Create a new editor state, converting the graph from a CGXML string */
    public EditorState( String holdgraph ) {
        setGraph( holdgraph );
        setKB( Global.sessionKB );
//            Global.info( "Editor state with graph:\n" + holdgraph );
    }
    
    /** Gets the graph stored in this state 
     * @return a CGXML String 
     * */
    public String getGraph() {
        return graph;
    }

    /** Sets the stored graph as a new CGXML string */
    public void setGraph( String graph ) {
        this.graph = new String( graph );
    }

    public KnowledgeBase getKB() {
        return kb;
    }

    public void setKB( KnowledgeBase kb ) {
        this.kb = kb;
    }
    /** Returns whether this state represents a change from the last save, backup, etc. 
     *    @see EditFrame#somethingHasChanged
     * Note this is not the same as whether the state itself has changed */
    public boolean isSomethingHasChanged() {
        return somethingHasChanged;
    }

    /** Remember for the editor state that when this state is active, the graph 
     * is considered changed from its last save, etc.
     * @see EditFrame#somethingHasChanged
     * */
    public void setSomethingHasChanged( boolean somethingHasChanged ) {
        this.somethingHasChanged = somethingHasChanged;
    }

    
}
