/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb;

import charger.xml.XMLGenerator;
import java.util.ArrayList;

/**
 * Represents the provenance and subsequent transformations of a given object.
 * @see ObjectHistoryEvent
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class ObjectHistory {
    
    /**
     * List of history events for an object. Should always add to the end, in chronologically ascending order.
     */
    private ArrayList<ObjectHistoryEvent> history = new ArrayList();
    
    public void addHistoryEvent( ObjectHistoryEvent he ) {
        history.add( he );
    }
    
    public ArrayList<ObjectHistoryEvent> getHistory() {
        return history;
    }
    
    /**
     * Get the last event in the list (which should be the most recent).
     * @return the last event in the history; null if there's no history.
     */
    public ObjectHistoryEvent getLastEvent() {
        if ( history != null )
        return history.get( history.size() - 1 );
        else
            return null;
    }
    
    /**
     * Finds a single history event of the given type.
     * If there is more than one, only one is chosen (probably the first one)
     * @param t
     * @return the first history event of the given type found in the history list for an object
     */
    public  ObjectHistoryEvent getEventByType( ObjectHistoryEventType t ) {
        for ( ObjectHistoryEvent he : history ) {
            if ( he.getType().equals( t ))
                return he;
        }
        return null;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer( "" );
        for ( ObjectHistoryEvent he : history ) {
            sb.append( he.toString() + "\n");
        }
        return sb.toString();
    }
    
    public String toXML( String indent ) {
        StringBuffer sb = new StringBuffer( "" );

        sb.append( indent + XMLGenerator.startTag( "history" ) + XMLGenerator.eol );
        for ( ObjectHistoryEvent he : history ) {
            sb.append( he.toXML( indent + XMLGenerator.tab ) + "\n");
        }

        sb.append( indent + XMLGenerator.endTag( "history" ) + XMLGenerator.eol );

        return sb.toString();

    }
}
