/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.hierarchy;

import charger.obj.GNode;
import java.util.*;

import charger.obj.GraphObject;

/**
 * Treats a hierarchy just like a partially ordered set, except that there is
 * one "bottom" node. Can be used for either a relation or concept type
 hierarchy. By default in Charger, every concept and relation type is added to
 its respective hierarchy, with TYPE_DEFAULT_TOP_LABEL as their supertype and
 DEFAULT_BOTTOM_LABEL as their subtype. Only using the genspeclink can real
 supertypes and subtypes be denoted.
 *
 * @see TypeHierarchyNode
 * @author Harry Delugach
 */
public class TypeHierarchy extends POSet {

    public static String TYPE_DEFAULT_TOP_LABEL = "T";
    public static String RELATION_DEFAULT_TOP_LABEL = "link";
    public static String DEFAULT_BOTTOM_LABEL = "_t_";
    public static String TYPE_DEFAULT_NAME = "Type hierarchy";
    public static String RELATION_DEFAULT_NAME = "Relation hierarchy";
    protected  TypeHierarchyNode typeRoot = new TypeHierarchyNode( TYPE_DEFAULT_TOP_LABEL, TYPE_DEFAULT_TOP_LABEL );           // The supremum of the order
    protected  TypeHierarchyNode relationRoot = new TypeHierarchyNode( RELATION_DEFAULT_TOP_LABEL, RELATION_DEFAULT_TOP_LABEL );           // The supremum of the order
    protected TypeHierarchyNode root = null;
    protected TypeHierarchyNode infinum = new TypeHierarchyNode( DEFAULT_BOTTOM_LABEL, DEFAULT_BOTTOM_LABEL );           // The infinum of the order
    protected String name = null;

    public static enum KindOfHierarchy {

        Type, Relation
    };
    protected KindOfHierarchy kind = null;
    TypeMatchingRuleSet matchRules = TypeMatchingRuleSet.ignoreCaseSpacesSpecial;

    /**
     * Creates a new hierarchy, but it is not empty. It has a root value of
 TYPE_DEFAULT_TOP_LABEL and a bottom value of DEFAULT_BOTTOM_LABEL.
     *
     * @param newname An optional name for this hierarchy
     */
    public TypeHierarchy( String newname, KindOfHierarchy kind ) {
        name = newname;
        this.kind = kind;
        if ( kind == KindOfHierarchy.Type ) {
            addNode( typeRoot );
            root = typeRoot;
        } else if ( kind == KindOfHierarchy.Relation ) {
            addNode( relationRoot );
            root = relationRoot;
        }
        addNode( infinum );
        addSubtypeToType( getBottom().getKey(), getTop().getKey() );
    }

    /**
     * Creates a new hierarchy with the same root values as the other
 constructors, but uses TYPE_DEFAULT_NAME and makes it a type hierarchy by
 default.
     *
     */
    public TypeHierarchy() {
        this(TYPE_DEFAULT_NAME, KindOfHierarchy.Type );
    }

//    /**
//     * Sets the root value for this hierarchy (e.g., "T" for CG types). Since
//     * there's always a top and bottom value, this may replace an existing top
//     * value. This may cause a number of odd changes in the hierarchy, which are
//     * not detected or corrected here.
//     *
//     * @param rootnode the node to set as the top. Note that if this is somehow invalid, we don't check it.
//     */
//    protected void setTop( TypeHierarchyNode rootnode ) {
//        typeRoot = rootnode;
//    }

    /**
     * Return the node at the top of this hierarchy.
     *
     * @return the top node of the hierarchy
     */
    public TypeHierarchyNode getTop() {
        return root;
    }

//    /**
//     * Sets the bottom node for this hierarchy .
//     * Since there's always a top and bottom node, this may replace the
//     * existing bottom node. This may cause a number of odd changes in the
//     * hierarchy, which are not detected or corrected here.
//     *
//     * @param bottom the node to set for the bottom. If this isn't valid, too bad!
//     */
//    protected void setBottom( TypeHierarchyNode bottom ) {
//        infinum = bottom;
//    }

    /**
     * Get the node at the bottom of the hierarchy. Returns the last node set by setBottom.
     * @return bottom node
     */
    public TypeHierarchyNode getBottom() {
        return infinum;
    }

    /**
     * Get the name of this hierarchy, the one given when it was created or by
     * setName.
     *
     * @return an English label for the hierarchy.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the human-readable name of this hierarchy.
     *
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Apply the rules, successively transforming the value into the key.
     *
     * @param value the original value to be stored.
     * @return The altered value, obeying the matching rules. If the value is
 strictly equal to TYPE_DEFAULT_TOP_LABEL or the TYPE_DEFAULT_TOP_LABEL, then the original value
 is returned.
     *
     */
    public String makeKey( String value ) {
        String r = value;
        if ( r.equals(TYPE_DEFAULT_TOP_LABEL ) || r.equals(DEFAULT_BOTTOM_LABEL ) ) {
            return value;
        }
        for ( TypeMatchRule rule : matchRules.getRules() ) {
            r = rule.transformByRule( this, r );
        }
        return r;
    }

    /**
     * Convenience method to tell whether there's an ignore case rule
     *
     * @return true if there's an ignore case rule in this hierarchy's rule set;
     * false otherwise.
     */
    public boolean isIgnoreCase() {
        if ( matchRules.contains( new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreCase ) ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method to set an ignore/consider case rule.
     *
     * @param ignoreCase true if we want to set IgnoreCase, false if we want to
     * set ConsiderCase. Removes a conflicting rule if present.
     */
    public void setIgnoreCase( boolean ignoreCase ) {
        TypeMatchRule ignore = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreCase );
        TypeMatchRule consider = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.ConsiderCase );
        if ( ignoreCase ) {
            matchRules.removeRule( consider );
            matchRules.addRule( ignore );
        } else {
            matchRules.removeRule( ignore );
            matchRules.addRule( consider );
        }
    }

    /**
     * Convenience method to tell whether there's an ignore spaces rule
     *
     * @return true if there's an ignore spaces rule in this hierarchy's rule
     * set; false otherwise.
     */
    public boolean isIgnoreSpaces() {
        if ( matchRules.contains( new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreSpaces ) ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method to set an ignore/consider spaces rule.
     *
     * @param ignoreSpaces true if we want to set IgnoreSpaces, false if we want
     * to set ConsiderSpaces. Removes a conflicting rule if present.
     */
    public void setIgnoreSpaces( boolean ignoreSpaces ) {
        TypeMatchRule ignore = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreSpaces );
        TypeMatchRule consider = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.ConsiderSpaces );
        if ( ignoreSpaces ) {
            matchRules.removeRule( consider );
            matchRules.addRule( ignore );
        } else {
            matchRules.removeRule( ignore );
            matchRules.addRule( consider );
        }
    }

    /**
     * Convenience method to tell whether there's an ignore special characters
     * rule
     *
     * @return true if there's an ignore special characters rule in this
     * hierarchy's rule set; false otherwise.
     */
    public boolean isIgnoreSpecial() {
        if ( matchRules.contains( new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreSpecial ) ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method to set an ignore/consider special characters rule.
     *
     * @param ignoreSpecial true if we want to set IgnoreSpecial, false if we want
     * to set ConsiderSpecial. Removes a conflicting rule if present.
     */
    public void setIgnoreSpecial( boolean ignoreSpecial ) {
        TypeMatchRule ignore = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.IgnoreSpecial );
        TypeMatchRule consider = new TypeMatchRule( TypeMatchingRuleSet.CharacterRule.ConsiderSpecial );
        if ( ignoreSpecial ) {
            matchRules.removeRule( consider );
            matchRules.addRule( ignore );
        } else {
            matchRules.removeRule( ignore );
            matchRules.addRule( consider );
        }
    }
    
    /**
     * Makes sure that the already added-node is linked to the top and bottom.
     * For a regular concept label, this is all we can know. If the label already was in the hierarchy,
     * then we wouldn't have to add it.
     * @param node A node that has already been added 
     */
    public void addToTopAndBottom( TypeHierarchyNode node ) {
        addSubtypeToType(node.getKey(), typeRoot.getKey() );
        addSubtypeToType( infinum.getKey(), node.getKey() );
    }
    

    /**
     * Adds a label in a node to the hierarchy.
     * If the label doesn't already exist, creates a TypeHierarchyNode for it,
     * and set it as subtype to top and supertype to bottom.
     * If a label forming the same key as this already exists, adds the name to that node
     * (if it's not already
     * in the node)
     * If the label is either the top or bottom, return null.
     * @param name of the label to be added. 
     * @return the node if it was added. If only a value was added to an existing node, returns null.
     */
    public TypeHierarchyNode addTypeLabel( String name ) {
        if ( name == null || name.trim().equals( "" ) ) {
            return null;
        }
        String key = makeKey( name );
        TypeHierarchyNode node = (TypeHierarchyNode)getNodeByKey( key );

        if ( node != null ) {
            if ( matchByRules(this.matchRules, node, root ) ) {
                return null;
            }
            if ( matchByRules( this.matchRules, node, infinum ) ) {
                return null;
            }

            node.addValue( name );
            return null;
        }
                // If label isn't found, then a new node is created with the new label
                // and added as subtype to root and supertype to infinum 
        node = new TypeHierarchyNode( key, name );
        if ( addNode( node ) ) {
            addSubtypeToType( name, root.getValue() );
            addSubtypeToType( infinum.getValue(), name );
            return node;
        } else {
            return null;
        }
    }

    /**
     * Removes a label from the hierarchy, without any super or sub
     * types. Before deleting, must connect all of its supertypes to its
     * subtypes.
     *
     * @param value of the label to be removed.
     * @return true if the label was removed successfully.
     */
    public boolean removeTypeLabel( String value ) {
        
       TypeHierarchyNode node = getNodeByValue( value );
       if ( node == null )
           return false;
       
       // First get all supertypes and remove them as supertypes of this node
       ArrayList<POSetNode> parents = node.getParentNodes();
       for ( POSetNode parent : parents ) {
           this.removeSuperTypeFromType( (TypeHierarchyNode) parent, node );
       }
       
       // Next get all subtypes and for each of them, remove this as a supertype
         ArrayList<POSetNode> childnodes = node.getChildNodes();
     for ( POSetNode child : childnodes ) {
           this.removeSuperTypeFromType( node, (TypeHierarchyNode) child );
       }
       // Finally remove the node
            return deleteNode( node );
    }

    /**
     * Get all the type name keys in this hierarchy.
     *
     * @return a sorted list of type keys.
     */
    public ArrayList<String> getKeys() {
        String[] names = new String[ allnodes.values().size() ];
        int num = 0;
        for ( Object s : allnodes.keySet() ) {
            names[ num++] = (String)s;
        }
        Arrays.sort( names );
        return new ArrayList( Arrays.asList( names ) );
    }

    /**
     * Get all the type labels in this hierarchy.
     *
     * @return a sorted list of type labels.
     */
    public ArrayList<String> getLabelsAsValues() {
        String[] names = new String[ allnodes.keySet().size() ];
//        allnodes
//        int num = 0;
//        for ( Object s : allnodes.keySet() ) {
//            names[ num++] = (String)s;
//        }
//        Arrays.sort( names );
        return new ArrayList( Arrays.asList( names ) );
    }

    /**
     * Adds the label of the supertype to the hierarchy if it isn't there. Adds
     * the label of the subtype to the hierarchy if it isn't there. Makes the
     * link between them. If there's already a supertype relationship (direct or
     * indirect) then nothing is changed. If either of them is null, then return
     * false.
     *
     * @param subtypeObject
     * @param supertypeObject
     * @return true if something was changed (either was added or a subsumption
     * relationship was added; false otherwise
     */
    public boolean addSubtypeToType( GNode subtypeObject, GNode supertypeObject ) {
        if ( supertypeObject == null || subtypeObject == null ) {
            return false;
        }
        return addSubtypeToType( subtypeObject.getTypeLabel(), supertypeObject.getTypeLabel( ));
    }

    /**
     * Basic construction unit for hierarchy. Create the sub- to super-type
     * relationship. If there's already a direct or indirect such relationship,
     * then return false. Create type nodes for either one if they do not
     * already exist. If the subtype is the same as the top ("T"), then return
     * false, since we can't create a supertype to the root. Likewise if the
     * supertype is the same as the bottom ("absurd") type, we also return
     * false.
     *
     * @param subtypeLabel The label being used as a subtype. will be turned
     * into a key before inserting
     * @param supertypeLabel The label being used as a supertype. will be turned
     * into a key before inserting.
     * @return true if the relationship was created, false if it failed for any
     * of the above reasons.
     */
    public boolean addSubtypeToType( String subtypeLabel, String supertypeLabel ) {
        if ( supertypeLabel == null || subtypeLabel == null ) {
            return false;
        }

        String superKey = makeKey( supertypeLabel );
        String subKey = makeKey( subtypeLabel );

        TypeHierarchyNode sup = (TypeHierarchyNode)getNodeByKey( superKey );
        TypeHierarchyNode sub = (TypeHierarchyNode)getNodeByKey( subKey );
        
        if ( sup == null || sub == null ) { // if either of them doesn't already exist, then this is a new relationship.
            if ( sup == null ) {
                sup = new TypeHierarchyNode( superKey, supertypeLabel );
                addNode( sup );
                addSubtypeToType( supertypeLabel, root.getValue() );
            }
            if ( sub == null ) {
                sub = new TypeHierarchyNode( subKey, subtypeLabel );
                addNode( sub );
                addSubtypeToType( infinum.getValue(), subtypeLabel );
            }
        } else {        // if both exist, make sure there's not already a indirect supertype relationship to sup
            if ( sub.hasIndirectParent( sup ) ) {
                return false;
            }
        }
            // Here is where we fix the super- and sub-type relationships
            // if the supertype 
        
        
        sub.addParent( sup );

        return true;
    }

    /**
     * Removes the super/sub type relationship between the nodes.
     *
     * @param supertypeObject
     * @param subtypeObject
     * @return true if relationship was successfully removed; false otherwise.
     */
    public boolean removeSuperTypeFromType( GraphObject supertypeObject, GraphObject subtypeObject ) {
        if ( supertypeObject == null || subtypeObject == null ) {
            return false;
        }
        String sup = supertypeObject.getTextLabel();
        String sub = subtypeObject.getTextLabel();
        return removeSuperTypeFromType( sup, sub );
    }

    /**
     * Removes the super/sub type relationship between the nodes.
     *
     * @param supertypeString The label being used as a supertype.
     * @param subtypeString
     * @return true if the relationship was created, false if it failed for any
     * of the above reasons.
     */
    public boolean removeSuperTypeFromType( String supertypeString, String subtypeString ) {
        if ( supertypeString == null || subtypeString == null ) {
            return false;
        }
        boolean changed = false;

        TypeHierarchyNode sup = (TypeHierarchyNode)getNodeByValue( supertypeString );
        TypeHierarchyNode sub = (TypeHierarchyNode)getNodeByValue( subtypeString );

        if ( sup == null || sub == null ) {
            return false;
        }
        
        return removeSuperTypeFromType(  sup, sub );
    }
    
    public boolean removeSuperTypeFromType( TypeHierarchyNode sup, TypeHierarchyNode sub ) {

        if ( sub.hasDirectParent( sup ) ) {   // if there's no already some supertype (recursively defined) relationship
            sub.removeParent( sup );
                        //  tie the subtype to all of supertype's direct supertypes
            ArrayList<POSetNode> parents = sup.getParentNodes();
            for ( POSetNode node : parents ) {
                sub.addParent( node );
            }
                        //  tie the supertype to all of sub's direct subtypes
            ArrayList<POSetNode> children = sub.getChildNodes();
            for ( POSetNode node : children ) {
                sup.addChild( node );
            }
          return true;
        }
        return false;
    }

    /**
     * Determine whether two nodes match values after applying the rules. In
     * theory, the keys should already reflect the values' transformations into
     * the keys, but since rules may have changed, we re-do the check.
     *
     * @param rules The rules to be applied
     * @param node1 A first node -- the order does not matter
     * @param node2 a second node
     * @return true if the node's values match after being transformed by the
     * rules.
     *
     */
    public boolean matchByRules( TypeMatchingRuleSet rules, TypeHierarchyNode node1, TypeHierarchyNode node2 ) {
        return matchByRules( rules, node1, node2.getValue() );
    }

    /**
     * Determine whether a node's value matches a given value after applying the
     * rules. In theory, the key of the node should already reflect its value's
     * transformations into the keys, but since rules may have changed, we re-do
     * the check.
     *
     * @param rules The rules to be applied
     * @param node A node whose value is to be compared.
     * @param value a string to be compared
     * @return true if the node's value matches the string after being
     * transformed by the rules.
     *
     */
    public boolean matchByRules( TypeMatchingRuleSet rules, TypeHierarchyNode node, String value ) {
        String value1 = rules.transformByRules( this, node.getValue() );
        String value2 = rules.transformByRules( this, value );
        if ( value1.equals( value2 ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds a node whose key (previously transformed from its value) matches
     * the value once it is also transformed.
     *
     * @param value The value to be transformed and matched
     * @return the node that matches; null otherwise
     */
    public TypeHierarchyNode getNodeByValue( String value ) {
        String key = matchRules.transformByRules( this, value );
        return (TypeHierarchyNode)getNodeByKey( key );
    }

    /**
     * Detects whether adding a specialization between the two terms will create
     * a redundant or cyclic hierarchy.
     *
     * @param supertypeString
     * @param subtypeString
     * @return explanation if the redundancy would exist; null otherwise
     * (meaning it's okay to add such a link)
     */
    public String isRedundantSuperSubtype( String supertypeString, String subtypeString ) {
        if ( supertypeString == null || subtypeString == null ) {
            return null;
        }

        if ( subtypeString.equalsIgnoreCase( getTop().getKey() ) ) {
            return "Can't have a supertype above the top of a hierarchy.";
        }

        if ( supertypeString.equalsIgnoreCase( getBottom().getKey() ) ) {
            return "Can't have a supertype below the bottom of a hierarchy.";
        }

        TypeHierarchyNode sup = (TypeHierarchyNode)getNodeByValue( supertypeString );
        TypeHierarchyNode sub = (TypeHierarchyNode)getNodeByValue( subtypeString );

        if ( sub == sup ) {
            return "Can't have a supertype to oneself.";
        }

//        if ( sub.hasDirectParent( sup ) && ! sup.getKey().equals( root.getKey() ) ) {
//            return "Supertype/subtype relationship already exists";
//        }
//
//        if ( sub.hasDirectParent( sup ) && ! sub.getKey().equals( infinum.getKey() ) ) {
//            return "Supertype/subtype relationship already exists";
//        }
        
        if ( !sub.hasDirectParent( sup ) && sub.hasIndirectParent( sup) ) {
            return "Subtype is already an indirect subtype of chosen supertype.";
        }

        return null;
    }

    /**
     * Displays all hierarchy nodes in very clunky text
     *
     * @param start An initial string; if null, then use the hierarchy title.
     * @return a human-readable text version of what's in the hierarchy
     * @see TypeHierarchyNode#toString(boolean)
     */
    public String showHierarchy( String start ) {
        if ( start == null ) {
            start = getName();
        }
        String s = start + ":\n";
        s += matchRules.toString() + "\n";
        for ( POSetNode n : allnodes.values() ) {
            s += kind.toString() + ": " + ( (TypeHierarchyNode)n ).toString( true );
        }
        return s;
    }

    public String showHierarchy() {
        return showHierarchy( null );
    }
}
