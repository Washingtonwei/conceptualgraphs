/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.undo;

/**
 * Represents an object's capability to have its changes undone/redone.
 * Changes represent transitions between states, which are then able
 * to be restored or re-done.
 * No edit actions <em>per se</em> should be performed by
 * the implementation -- restoreState should exactly re-construct
 * the state it represents.
 * @see charger.undo.UndoStateManager
 *  @since Charger 3.8.3
 * @author hsd
 */
public interface Undoable {
    /** Creates a new instance of the current state of the target object.
     * Note that this method must create a complete copy (either through clone() or 
     * some other method. If only a reference is saved, then all saved states will really
     * point to the same (current) state and there will appear to be no effect.
     */
    public  UndoableState currentState();
    
    /** Restores the object's current state from the given state */
    public  void restoreState(UndoableState state);
    
    /** Perform whatever setup is necessary if the undo/redo status may have changed.
     * Subclasses override to do whatever menu (or other) setup is needed to make undo/redo available */
    public  void setupMenus();

}
