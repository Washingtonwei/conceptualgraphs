/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import kb.ObjectHistoryEvent;
import kb.ObjectHistoryEventType;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MMObjectHistoryEvent extends ObjectHistoryEvent {
    
    MModelID modelID = null;

    MMObjectHistoryEvent( MModel m ) {
        this.modelID = m.getID();
        this.type = ObjectHistoryEventType.MODEL;
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer( date.toString() );
        s.append( " " + modelID.toString());
        return s.toString();
    }

    public MModelID getModelID() {
        return modelID;
    }
    
    
}
