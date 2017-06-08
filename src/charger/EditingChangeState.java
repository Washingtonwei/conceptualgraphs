/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger;

/**
 * Encapsulates the status of the change resulting from an editing operation.
 * There are two kinds of content changes: appearance changes, where something has moved or changed color,
 * and semantic changes, where something has altered the meaning (text label or nesting) of a graph.
 * In addition, we want to separately note whether the change is to be undoable or not.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class EditingChangeState {
    boolean appearanceChanged = false;
    boolean semanticsChanged = false;
    boolean changeUndoable = false;
    
    /** What kinds of change are possible. These are used as arguments for the variable arg length calls.
     * The unchanged/not items are here for completeness, since they are the defaults. 
     * Unless an editing change is to be "backed out" (other than with undo), they probably don't need to be used.
     * */
    public enum EditChange {
        APPEARANCE, APPEARANCE_UNCHANGED,
        SEMANTICS, SEMANTICS_UNCHANGED,
        UNDOABLE, NOT_UNDOABLE
    }
    
//    public EditingChangeState( boolean appearance, boolean semantics, boolean undoable ) {
//        this.appearanceChanged = appearance;
//        this.semanticsChanged = semantics;
//        this.changeUndoable = undoable;
//    }
    
    /** Set the appropriate change(s) in a  human-readable way */
    public EditingChangeState( EditChange... changes ) {
        for ( EditChange change : changes ) {
            switch ( change ) {
                case APPEARANCE:
                    setAppearanceChanged( true );
                    break;
                case APPEARANCE_UNCHANGED:
                    setAppearanceChanged( false );
                    break;
                case SEMANTICS:
                    setSemanticsChanged( true );
                    setAppearanceChanged( true );
                 break;
                case SEMANTICS_UNCHANGED:
                    setSemanticsChanged( false );
                    break;
                case UNDOABLE:
                    setChangeUndoable( true );
                    break;
                case NOT_UNDOABLE :
                    setChangeUndoable( false );
                    break;
            }
        }
    }
    
    /** Default is that nothing has changed. */
    public EditingChangeState() {

    }

    public boolean isAppearanceChanged() {
        return appearanceChanged;
    }

    public void setAppearanceChanged( boolean appearanceChanged ) {
        this.appearanceChanged = appearanceChanged;
    }

    public boolean isSemanticsChanged() {
        return semanticsChanged;
    }

    public void setSemanticsChanged( boolean semanticsChanged ) {
        this.semanticsChanged = semanticsChanged;
    }

    public boolean isChangeUndoable() {
        return changeUndoable;
    }

    public void setChangeUndoable( boolean changeUndoable ) {
        this.changeUndoable = changeUndoable;
    }
    
    
    
    public boolean anythingChanged() {
        return isSemanticsChanged() || isAppearanceChanged();
    }
    
}
