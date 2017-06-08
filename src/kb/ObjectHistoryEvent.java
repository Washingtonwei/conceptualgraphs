/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb;

import charger.obj.Graph;
import charger.xml.XMLGenerator;
import java.io.File;
import java.util.Date;

/**
 * A single event to be recorded in an object's history.
 * The information in a history event depends on the event type.
 * For a file, the filename is recorded. For a graph, the
 * top level graph is recorded.
 * @see ObjectHistoryEventType
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class ObjectHistoryEvent {
    

    protected Date date = new Date();
    
    private File file = null;
    
    private Graph topLevelGraph = null;
    
    protected String description = null;
    
    protected ObjectHistoryEventType type = null;
    
    public ObjectHistoryEvent() {
        
    }
    
    public ObjectHistoryEvent( String s ) {
        this.type =  ObjectHistoryEventType.getTypeOf( s );
    }
    
    public ObjectHistoryEvent( File f ) {
        this.file = f;
        this.type = ObjectHistoryEventType.getTypeOf( "file");
    }
    
    public ObjectHistoryEvent( Graph g ) {
        this.topLevelGraph = g;
        this.type = ObjectHistoryEventType.getTypeOf( "graph");
    }

    public Date getDate() {
        return date;
    }

    public File getFile() {
        return file;
    }

    public Graph getGraph() {
        return topLevelGraph;
    }

    public void setGraph( Graph topLevelGraph ) {
        this.topLevelGraph = topLevelGraph;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public ObjectHistoryEventType getType() {
        return type;
    }

    public void setType( ObjectHistoryEventType type ) {
        this.type = type;
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer( date.toString() );
        if ( this.type.equals( ObjectHistoryEventType.FILE ) ) {
            s.append( " " + type.phrase() + " " + file.getName() + "\"" );
        } else if ( this.type.equals( ObjectHistoryEventType.GRAPH) ) {
            s.append( " " + type.phrase() + " " +  topLevelGraph.getTextLabel() + "\"" );
        } else if ( this.type.equals( ObjectHistoryEventType.USER ) ) {
            s.append( " " + type.phrase() + " \""  + this.getDescription() + "\"" );
        } else if ( this.type.equals( ObjectHistoryEventType.DERIVATION ) ) {
            s.append( " " + type.phrase() + " "  + this.getDescription() + "\"" );
        } else {
            s.append( this.type.toString() + "\"" + this.getDescription() + "\"" );
        }
        return s.toString();
    }
    
    public String toXML( String indent ) {
        StringBuffer sb = new StringBuffer( date.toString() );

        sb.append( indent + XMLGenerator.startTag( "event" ) + XMLGenerator.eol );


        sb.append( indent + XMLGenerator.endTag( "event" ) + XMLGenerator.eol );

//        if ( this.type.equals( ObjectHistoryEventType.FILE ) ) {
//            s.append( " " + type.phrase() + " " + file.getName() + "\"" );
//        } else if ( this.type.equals( ObjectHistoryEventType.GRAPH) ) {
//            s.append( " " + type.phrase() + " " +  topLevelGraph.getTextLabel() + "\"" );
//        } else if ( this.type.equals( ObjectHistoryEventType.USER ) ) {
//            s.append( " " + type.phrase() + " \""  + this.getDescription() + "\"" );
//        } else if ( this.type.equals( ObjectHistoryEventType.DERIVATION ) ) {
//            s.append( " " + type.phrase() + " "  + this.getDescription() + "\"" );
//        } else {
//            s.append( this.type.toString() + "\"" + this.getDescription() + "\"" );
//        }
        return sb.toString();
    }
    
}
