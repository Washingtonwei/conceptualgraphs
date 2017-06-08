/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

import charger.undo.UndoableState;

/**
 * Holds a complete copy of the state of the synonym editor, for purposes of undo/redo.
 * The contents of the state consist of the synonym group and the editor pane text.
 * The rest of the editor can be re-constituted from these.
 * @see charger.undo.Undoable
 * @since Charger 3.8.3
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class SynonymEditorState extends UndoableState {
    
    private ClusterCollection allSynonyms = new ClusterCollection();
    private String editorPaneText = null;
    
    
    public ClusterCollection getSynonyms() {
        return allSynonyms;
    }

    public void setSynonyms( ClusterCollection syns ) {
        this.allSynonyms = new ClusterCollection( syns );     // need to make a complete copy here
    }
    

    public String getEditorPaneText() {
        return editorPaneText;
    }

    public void setEditorPaneText( String editorPaneText ) {
        this.editorPaneText = new String( editorPaneText );
    }

    
    public String toString() {
        return allSynonyms.synonymClusters.size() + " set(s). Editor is \"" + getEditorPaneText() + "\"\n" + allSynonyms.toString();
    }

}
