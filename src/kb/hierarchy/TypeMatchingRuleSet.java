/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb.hierarchy;

import java.util.ArrayList;

/**
 * Encapsulates a set of constraints on type matching in the hierarchy.
 * Currently allows type matching to ignore case, ignore spaces and ignore special characters.
 * These are all orthogonal to each other; i.e., separate tests are made.
 * Custom rules can also be added to the set.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class TypeMatchingRuleSet {
    
    private ArrayList<TypeMatchRule> rules = new ArrayList<>();
    
    /** Allows the character rules to say what they do. NoCharacterRule is provided so that custom rules
     * (i.e., non-character rules) may be added to the set.
     * The "consider" versions are there for completeness.
     **/
    public enum CharacterRule {
        IgnoreCase, ConsiderCase,
        IgnoreSpaces, ConsiderSpaces,
        IgnoreSpecial, ConsiderSpecial,
        NoCharacterRule
    }


    /** A convenience set that ignores case, spaces and special characters. Useful when setting up defaults. */
    public static TypeMatchingRuleSet ignoreCaseSpacesSpecial = 
            new TypeMatchingRuleSet( CharacterRule.IgnoreCase, CharacterRule.IgnoreSpaces, CharacterRule.IgnoreSpecial );
      
           
    /**
     * Create a set of rules according to the rules in the list.
     * This constructor will only create a set of character rules.
     * Custom rules must be added by themselves.
     * @param ruleList a sequence of character rule values (may be empty)
     * @see TypeMatchingRules#addRule(TypeMatchRule)
     */
    public TypeMatchingRuleSet( CharacterRule... ruleList ) {
        for ( CharacterRule r : ruleList ) {
            rules.add( new TypeMatchRule( r ));
        }
    }

    /** Get the list of rules currently in the set */
    public ArrayList<TypeMatchRule> getRules() {
        return rules;
    }
    
    /** Determine whether a particular rule is in the set. Probably only useful for the character rules. */
    public boolean contains( TypeMatchRule rule ) {
        if ( rules.contains( rule  ) )
            return true;
        else
            return false;
    }
    
        /** Remove a particular rule from the set. Probably only useful for the character rules. */
    public void removeRule( TypeMatchRule rule ) {
        if ( rules.contains( rule)) {
            rules.remove(  rule );
        }
    }
    
    /**
     * Add a rule to this set. If the rule is already present, then does nothing.
     * @param rule 
     */
    public void addRule( TypeMatchRule rule ) {
        if ( ! rules.contains(  rule ) )
            rules.add(  rule );
    }
    
    /**
     * Use the hierarchy's set of rules to transform the string. Ignore top and
     * bottom -- they aren't meant to be ever transformed.
     */
    public String transformByRules( TypeHierarchy h, String value ) {
        String r = value;
        if ( r.equals( h.getTop().getKey() ) ) {
            return value;
        }
        if ( r.equals( h.getBottom().getKey() ) ) {
            return value;
        }
        for ( TypeMatchRule rule : rules ) {
            r = rule.transformByRule( h, r );
        }
        return r;
    }
    
    /**
     * Creates a human-readable version of the rule set.
     * @return a human readable version of this set
     */
    public String toString() {
        String s = "Rules: ";
        for ( TypeMatchRule rule : rules ) {
            s += rule.toString() + "; ";
        }
           return s;
    }
}
