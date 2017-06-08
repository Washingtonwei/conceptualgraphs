/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.hierarchy;

import java.util.*;

/**
 * A node in a type hierarchy graph. The "key" is used to facilitate matching
 * and storing. The value is the original label used for the type. There are two
 * special cases -- both top and bottom are fixed and never keyed.
 *
 * @author Harry Delugach
 */
public class TypeHierarchyNode extends POSetNode {

    /**
     * The original versions of this type label
     */
    private ArrayList<String> values = new ArrayList<>();

    /**
     * Considers the key to be the node's identity, but keeps the value too.
     *
     * @param key the identity of the node
     * @param value the first value stored with this node.
     */
    public TypeHierarchyNode( String key, String value ) {
        super( key );
        values.add( value );
    }

//    /**
//     * Considers the key to be the node's identity, but keeps the value too.
//     *
//     * @param value the  value stored with this node as well as the key.
//     */
//    public TypeHierarchyNode( String value ) {
//        super( value );
//        values.add( value );
//    }


    public String getKey() {
        return (String)super.getKey();
    }

    /**
     * Get the first value in the list, arbitrarily.
     *
     * @return the first value in the list
     */
    public String getValue() {
        return values.get( 0 );
    }

    /**
     * Returns the entire set of values for this particular node.
     */
    public ArrayList<String> getValues() {
        return values;
    }
    
    
    /**
     * If the value is not already one of the type labels represented in this
     * node, then it's added. This has no effect on the already existing key. If
     * the matching rules were not obeyed, then erroneous values are fair game,
     * since the node itself doesn't know the matching rules used to create it.
     *
     * @see TypeMatchingRules
     *
     */
    public void addValue( String value ) {
        if ( !values.contains( value ) ) {
            values.add( value );
        }
    }

    public String toString() {
        String s = "";
        if ( values.size() == 1 ) {
            s += "\"" + values.get( 0) + "\"";
        } else {
            s += "\"" + getKey() + "\" ";
            s += "(";
            for ( String value : values ) {
                if ( !value.equals( getKey() ) ) {
                    s += "\"" + value + "\" ";
                }
            }
            s += ")";
        }
        return s;
    }


    /**
     *
     * @param deep Whether to list parent and child nodes recursively or not
     * @return a human-readable text version of what's in the node
     */
    public String toString( boolean deep ) {

        if ( !deep ) {
            return toString() + ".";
        } else {
            String s = toString() + ",";
            if ( parentNodes.size() == 0 ) {
                s += " no supertype(s);";
            } else {
                s += " supertype(s):";
                for ( POSetNode p : parentNodes ) {
                    s += " \"" + p.getKey() + "\"";
                }
                s += ";";
            }
            if ( childNodes.size() == 0 ) {
                s += " no subtype(s);";
            } else {
                s += " subtype(s):";
                for ( POSetNode p : childNodes ) {
                    s += " \"" + p.getKey() + "\"";
                }
                s += ";";
            }
            return s + "\n";

        }
    }
}
