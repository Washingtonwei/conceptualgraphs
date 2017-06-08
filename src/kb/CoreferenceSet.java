/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb;

import charger.Global;
import charger.exception.CGContextException;
import charger.obj.Coref;
import charger.obj.GNode;
import charger.obj.GraphObjectID;
import charger.obj.Graph;
import charger.obj.GraphObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of graph objects that form a coreference set. 
 * One may logically assume there are coreferent links (lines of identity)
 * between all members of the set, even though they may not be explicitly drawn.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class CoreferenceSet {
    
    HashMap<String, GNode> members = new HashMap<String, GNode>();

    public CoreferenceSet() {
        
    }
    
    public CoreferenceSet( ArrayList<GNode> nodes ) {
        for ( GNode node : nodes ) {
            members.put( node.objectID.toString(), node);
        }
    }
    
    public void addMember( GNode newMember ) {
        members.put( newMember.objectID.toString(), newMember );
    }
    
    /**
     * Creates coreferent links between the objects.
     * Uses an algorithm that minimizes line length and the total length of lines.
     * Removes any existing co-referent links between the objects before creating
     * new ones.
     */
    public void drawPrettyLines() {
        ArrayList<GNode> memberObjects = new ArrayList<GNode>( members.values() );
        
//                for ( GNode go : memberObjects ) {
//                    Global.info( "owner parents of " + go.getTextLabel() + ":" );
//                    GNode nextGo = go;
//                    while ( nextGo.getOwnerGraph() != null ) {
//                        Global.info( "  owner graph of " + nextGo.objectID + " is " + nextGo.getOwnerGraph().objectID );
//                        nextGo = nextGo.getOwnerGraph();
//                    }
//                }
                    
        Graph dominantContext = null;       // where to add the co-ref links
        try {
            dominantContext = GraphObject.findDominantContext( memberObjects );
        } catch ( CGContextException ex ) {
            Global.warning( "CoreferenceSet#drawPrettyLines: " + ex.getMessage() );
            return;
        }
        
        for ( int element = 0; element < memberObjects.size() -1; element++ ) {
                Coref c = new Coref( memberObjects.get( element ) , memberObjects.get( element + 1  ));
                dominantContext.insertObject( c );
                    Global.info( "insert coref into graph " + dominantContext.objectID.getShort() + 
                            ". From " + memberObjects.get( element ).getTextLabel() + "; to " +
                            memberObjects.get( element + 1 ).getTextLabel() + ".");
            }

//        for ( int element1 = 0; element1 < memberObjects.size(); element1++ ) {
//            for ( int element2 = element1 + 1; element2 < memberObjects.size(); element2++ ) {
//                Coref c = new Coref( memberObjects.get( element1) , memberObjects.get( element2  ));
//                dominantContext.insertObject( c );
//                    Global.info( "insert coref into graph " + dominantContext.objectID.getShort() + 
//                            ". From " + memberObjects.get( element1 ).getTextLabel() + "; to " +
//                            memberObjects.get( element2 ).getTextLabel() + ".");
//            }
//        }
    }
    

}
