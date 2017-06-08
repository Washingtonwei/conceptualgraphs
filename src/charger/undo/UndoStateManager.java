/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.undo;

/**
 * Manages the saving and restoring of states. This manager relies on the
 * implementer to decide the granularity of undoing things.
 * markAfterUndoableStep should be called AFTER some atomic action that is
 * undoable. resetAndMark is a good thing to call at the beginning of a session,
 * or when contents have been saved to a file, or any other situation where it
 * is no longer necessary to maintain the history of actions for un-doing.
 * <p>What constitutes a state is entirely the responsibility of the implementer
 * of the Undoable interface. As much or as little of the object's state may be
 * saved/restored as required by the application's interaction.
 *
 * @see UndoableState
 * @see Undoable
 * @since Charger 3.8.3
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class UndoStateManager {

    /**
     * Supports the undo/redo operations
     */
    public UndoRedoList<UndoableState> urStack = null;
    /**
     * The object whose states are being managed.
     */
    public Undoable stateSource = null;
    /**
     * Whether to actually operate or not
     */
    protected boolean enabled = true;

    /**
     * Instantiates the manager, with an undo/redo maximum level
     *
     * @param max the most number of previous/future actions handled by "undo"
     * If max is reached, then old states are discarded.
     */
    public UndoStateManager( Undoable source, int max ) {
        urStack = new UndoRedoList<>( max );
        stateSource = source;
    }

    /**
     * Tells the state manager that we've just completed something undoable.
     */
    public void markAfterUndoableStep() {
        if ( !isEnabled() ) {
            return;
        }
        urStack.pushCurrent( stateSource.currentState() );
        stateSource.setupMenus();
    }

    /**
     * Restores the object's state from the last saved state. Leaves the current
     * state as a possible redo target. Disables this manager during the restore
     * state operation.
     */
    public void doUndo() {
        if ( !isEnabled() ) {
            return;
        }
        UndoableState pastState = urStack.popUndo();
        setEnabled( false );
        stateSource.restoreState( pastState );
        setEnabled( true );
        stateSource.setupMenus();
    }

    /**
     * Restores the object's state from the state just forward of the current
     * one. Undoes the "undo" process, for one state only. Disables this manager
     * during the restore state operation.
     */
    public void doRedo() {
        if ( !isEnabled() ) {
            return;
        }
        UndoableState nextState = urStack.popRedo();
        setEnabled( false );
        stateSource.restoreState( nextState );
        setEnabled( true );
        stateSource.setupMenus();

    }

    /**
     * Is there any previous state to restore?
     *
     * @return true if we can safely perform an undo, false otherwise
     */
    public boolean undoAvailable() {
        if ( !isEnabled() ) {
            return false;
        }
        return urStack.undoAvailable();
    }

    /**
     * Is there a future state to restore?
     *
     * @return true if we can safely perform an redo, false otherwise
     */
    public boolean redoAvailable() {
        if ( !isEnabled() ) {
            return false;
        }
        return urStack.redoAvailable();
    }

    /**
     * Empties the undo redo lists. Not very useful by itself because it usually
     * must be followed by a markAfterUndoableStep with a current state.
     */
    protected void reset() {
        urStack.reset();
    }

    /**
     * Forget all previous actions and mark the current state for further
     * possible undo. This is usually what the programmer wants, since resetting
     * by itself really empties everything.
     */
    public void resetAndMark() {
        if ( !isEnabled() ) {
            return;
        }
        reset();
        markAfterUndoableStep();
    }

    /**
     * Disable the marking capability. This is useful if you're performing some
     * automatic operation that might trigger normally-undoable changes. The
     * actual undo and redo operations are also enabled. In other words, it
     * protects the undo/redo stack from changes.
     */
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }

    /**
     * Whether this manager is enabled or not.
     *
     * @return true if undo/redo states are being marked, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
