/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.undo;

import charger.Global;
import java.util.ArrayList;

/**
 * Implements the undo and redo logic. Relies only on there being some
 * UndoableState subclass as the state being stored.
 *
 * Operates as follows: Consider a list of sequential states numbered 0, 1, ...
 * , maxUndo-1 from past to future. There is a position currentIndex that is
 * the current state. There are three operations of interest: <ul> <li>Do
 * something that's undoable -- increment currentIndex, save that something, and
 * nullify remainder of list.</li> <li>Undo - decrement currentIndex, then
 * return content at currentIndex. This leaves the redo state still on the list
 * (at currentIndex + 1), to be re-done if required.</li> <li>Redo - increment
 * currentIndex, return content from there. </ul>
 *
 * @since Charger 3.8.3
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class UndoRedoList<T> {

    /**
     * Maximum number of states that are undo-able -- currently set to 10
     */
    public int maxUndo =
            Integer.parseInt( Global.Prefs.getProperty( "defaultMaxUndo", "10" ) );
    /**
     * Each past copy of the graph, in text form, is added to the list, up to
     * its max
     */
    protected ArrayList<T> stateList = new ArrayList<>( maxUndo );
    /**
     * the index into the state list telling us which state is the current one.
     * If -1 then there is no current state.
     */
    private int currentIndex = -1;

    /**
     * Create an empty undo/redo structure
     */
    public UndoRedoList() {
        reset();
    }

    /**
     * Allows user to decide how many undo steps are saved. Also applies to
     * redo. If the max is reached, all states are "scooted" down one, losing
     * the oldest state, and the most recent one is placed at the end.
     */
    public UndoRedoList( int max ) {
        maxUndo = max;
        stateList = new ArrayList<>( max );
        reset();
    }

    /**
     * @return whether there is a previous newState which we can restore.
     * @see #popUndo
     */
    public boolean undoAvailable() {
        return currentIndex > 0;        // if only a zero state, then we can't go back.
    }

    /**
     * @return whether there is a next newState which can be re-done.
     * @see #popRedo
     *
     */
    public boolean redoAvailable() {
        return currentIndex + 1 < maxUndo && stateList.get( currentIndex + 1 ) != null;
    }

    /**
     * Undo the last editing newState. Pushes a current state onto the redo
     * stack and pops the last copy of the state from the undoStack, to become
     * the current editing newState.
     *
     * restored via "redo". Implementer must explicitly use the returned state
     * to set the current state.
     *
     * @return The previous state to be be restored.
     */
    public T popUndo() {
        T newState = null;

        if ( !undoAvailable() ) {
            newState = null;
        } else {    // we only flow here if undo is available and therefore currentIndex is safe.
            try {
                newState = stateList.get( --currentIndex );
            } catch ( ArrayIndexOutOfBoundsException e1 ) {
                Global.error( "doUndo had a problem." );
            }
        }

        //Global.info( " === UNDO returns " + newState );
        return newState;
    }

    /**
     * Restore a previous state, moving forward with respect to the saved
     * states. Pushes the most recent state onto the undo stack and pops the
     * next "future" state from the redo stack. Implementer must explicitly use
     * the returned state to set the current state.
     *
     * @return The next ("future") editing action.
     */
    public T popRedo() {
        T newState = null;
        if ( !redoAvailable() ) {
            newState = null;
        } else {
            try {
                newState = stateList.get( ++currentIndex );
            } catch ( ArrayIndexOutOfBoundsException e1 ) {
                Global.error( "doRedo had a problem." );
            }
        }

        //Global.info( " === REDO returns " + newState );
        return newState;

    }

    /**
     * Track a regular editing action (other than undo or redo) by pushing it
     * onto the undo list. Wipes out any "redo" chain of events -- after this,
     * all the previously saved future states are gone. In science
     * fiction terms, we have thus altered the future timeline and no previous
     * future timelines can occur.
     *
     * @param currentState The most recent editing action. If null, then there's no change.
     *
     */
    public void pushCurrent( T currentState ) {
        if ( currentState == null ) {
            return;
        }
        currentIndex++;

        if ( currentIndex >= maxUndo ) {    // state list is full, remove the oldest one
            for ( int k = 1; k < maxUndo; k++ ) {
                stateList.set( k - 1, stateList.get( k ) );
            }
            currentIndex = maxUndo - 1;     // decrement back to the very end of the list
        }
        stateList.set( currentIndex, currentState );    // set the current state, then clear to the end.
        for ( int clearIndex = currentIndex + 1; clearIndex < maxUndo; clearIndex++ ) {
            stateList.set( clearIndex, null );
        }
    }

    /**
     * Initialize the undo/redo sequences, so that there are no past or future
     * states. Populates the state list with null values so it will be the right
     * size. Sets the current index to -1
     */
    public void reset() {
        stateList.clear();
        for ( int k = 0; k < maxUndo; k++ ) {
            stateList.add( null );
        }
        currentIndex = -1;
    }

    /**
     * Generally used for debugging
     */
    @Override
    public String toString() {
        String out = "";
        out += "Capacity = " + stateList.size();
        out += ". Current state = " + currentIndex + "\n";
        if ( currentIndex >= 0 ) {
            for ( int stateNum = 0; stateNum < stateList.size(); stateNum++ ) {
                out += "state " + stateNum + " = ";
                T state = stateList.get( stateNum );
                if ( state == null ) {
                    out += " null";
                } else {
                    out += state.toString();
                }
                out += "\n";
            }
        }
        return out;
    }
}
