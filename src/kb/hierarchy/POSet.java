/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.hierarchy;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Implements a partially ordered set. Maintains an index of the nodes so that a given object can be found quickly.
 * NO value may appear twice anywhere in the POSet.
 * Has a root node which is the "largest" value, which allows navigation to all of its member nodes.
 * @see POSetNode
 * @author Harry Delugach
 */
public class POSet  {
    
    
    protected HashMap<Object, POSetNode> allnodes = new HashMap<Object, POSetNode>();
    
    /**
     * Searches for the given value in the partially ordered set. If found, returns a node in which it appears.
     * @param posetKey the key for the node we're looking for
     * @return The node that nodeContains the value; null otherwise.
     */
    public POSetNode getNodeByKey( Object posetKey ) {
        POSetNode foundOne = allnodes.get( posetKey );
        return foundOne;
    }
    
    /**
     * Add a node to the poset without regard for position or order.
     * @return true if the node was simply added successfully (regardless of order); false otherwise.
     */
    public boolean addNode( POSetNode node )  {
            // If the node value is already in the poset, then throw an exception.
        if ( getNodeByKey( node.getKey() ) != null ) { 
            return false;
        }
        else {
            allnodes.put( node.getKey(), node );
            return true;
        }
        
    }

        /**
     * Deletes a node from the poset.
     * Detach all of its parent and child links.
     *      * @return true if the node was simply deleted successfully (regardless of order); false otherwise.

     */
    public boolean deleteNode( POSetNode node )  {
            // If the node value is already in the poset, then throw an exception.
        if ( node == null ) { 
            return false;
        }
        else {
            boolean okay = true;
            ArrayList<POSetNode> nodes = node.getChildNodes();
            for ( POSetNode n : nodes ) {
                okay = okay & n.removeParent( node );
            }

            nodes = node.getParentNodes();
            for ( POSetNode n : nodes ) {
                okay = okay & n.removeChild( node );
            }

            POSetNode n = allnodes.remove( node.getKey() );
            return okay & (n != null);
        }
        
    }

    public int getCardinality(){
        return allnodes.size();
    }
    
    public void clear() {
        allnodes = new HashMap<Object, POSetNode>();

    }
    
}
