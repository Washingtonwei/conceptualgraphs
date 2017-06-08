/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.hierarchy;

import java.util.*;

/**
 * Represents the abstraction of a node in a partially ordered set (i.e., a node in a hierarchy that allows multiple parent nodes).
 * A partially ordered set consists of one or more nodes, each of which may have zero or more super-nodes (whose object precedes 
 *  it in the ordering)
 * and zero or more sub-nodes ("below" it). There is exactly one top node and exactly one bottom node. 
 * A one-node lattice is such that the top and bottom node are the same.
 * @author Harry Delugach
 */
public class POSetNode {
    
    private Object posetKey = null;
        // private POSet poset = null; 
    
    ArrayList<POSetNode> parentNodes = new ArrayList<POSetNode>();
    
    ArrayList<POSetNode> childNodes = new ArrayList<POSetNode>();

    public POSetNode( Object obj ) {
        setPosetKey( obj );
    }
    
    
    
    /**
     * 
     * @return true if this node has no parent; false otherwise
     */
    public boolean hasParent() {
        if ( parentNodes.size() == 0 ) return false;
        else return true;
    }

    /**
     * 
     * @return true if this node has no child; false otherwise 
     */
    public boolean hasChild() {
        if ( childNodes.size() == 0 ) return false;
        else return true;
    }
    
    /**
     * Add a parent node to this one, unless it's already a parent to this node.
     * Make this one a child of the parent, unless it already is one.
     * @param node The parent node to add
     * @return true if the parent node was added; false if it was already there, or
     * if this node is the parent node.
     */
    public boolean addParent( POSetNode node ) { //throws NodeOrderException {
        if ( hasDirectParent( node ) ) return false;    // already there
        if ( this == node ) {
            return false;   // can't add a child to itself
        }
        parentNodes.add( node );
        node.addChild(  this );
        return true;
    }

    /**
     * Removes a parent node from this one, unless it's not a parent to this node.
     * The parent node is also changed to reflect this is no longer a child,
     * but no other changes are made to the parent node.
     * @param node The parent node to add
     * @return true if the parent node was removed; false if it wasn't there
     */
    public boolean removeParent( POSetNode node ) {
        if ( ! hasDirectParent( node ) ) 
            return false;
        else {
            parentNodes.remove( node );
            node.childNodes.remove( this );
        }
        return true;
    }

     /**
     * Add a child node to this one, unless it's already a child to this node
     * @param node The child node to add
     * @return true if the child node was added; false if it was already there
     */
   public boolean addChild( POSetNode node ) {
        if ( childNodes.contains( node ) ) {
            return false;
        }
        if ( this == node ) {
            return false;   // can't add a child to itself
        }
        childNodes.add( node );
        node.addParent( this );
        return true;
    }
     /**
     * Removes a child node from this one, as long as it's already there.
     *      * The child node is also changed to reflect this is no longer a parent,
     * but no other changes are made to the child node.

     * @param node The child node to add
     * @return true if the child node was removed; false if it was wasn't there
     */
   public boolean removeChild( POSetNode node ) {
        if ( ! childNodes.contains( node ) ) 
            return false;
        else {
            childNodes.remove( node );
            node.parentNodes.remove( this );
        }
        return true;
    }

    public ArrayList<POSetNode> getParentNodes() {
        return parentNodes;
    }
    
    public ArrayList<POSetNode> getChildNodes() {
        return childNodes;
    }

    public Object getKey() {
        return posetKey;
    }

    public void setPosetKey( Object value) {
        this.posetKey = value;
    }
    
    /**
     * Looks to see if the argument node is a direct parent to the target node
     * @param node a potential parent node being considered
     * @return true if node is a parent; false if it's not
     */
    public boolean hasDirectParent( POSetNode node ) {
        if ( parentNodes.contains( node ) ) return true;
        else return false;
    }
    
    /**
     * Looks to see if the argument node is a parent (recursively) to the target node.
     * Note that if this returns true, hasDirectParent will also return true.
     * @param node a potential parent node being considered
     * @return true if node is a recursive parent
     */
    public boolean hasIndirectParent( POSetNode node ) {
        if ( this.hasDirectParent( node )) return true;
        else {
            for ( POSetNode n : parentNodes ) {
                if ( n.hasIndirectParent( node )) return true;
            }
            return false;
        }
    }

        /**
     * Looks to see if the argument node is a direct child to the caller's node
     * @param node a potential child node being considered
     * @return true if node is a child; false if it's not
     */
    public boolean hasDirectChild( POSetNode node ) {
        if ( childNodes.contains( node ) ) return true;
        else return false;
    }
    
    /**
     * Looks to see if the argument node is a child (recursively) to the caller's node.
     * Note that if this method returns true, hasDirectChild will also return true.
     * @param node a potential child node being considered
     * @return true if node is a recursive child; false otherwise
     */
    public boolean hasIndirectChild( POSetNode node ) {
        if ( this.hasDirectChild( node )) return true;
        else {
            for ( POSetNode n : childNodes ) {
                if ( n.hasIndirectChild( node )) return true;
            }
            return false;
        }
    }
    

}
