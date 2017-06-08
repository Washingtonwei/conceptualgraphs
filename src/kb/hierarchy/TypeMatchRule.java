/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb.hierarchy;

import kb.hierarchy.TypeMatchingRuleSet.CharacterRule;

/**
 * Encapsulates a single rule for type matching.
 * The basic rules consist of a simple rule using one of the Rule enumeration values.
 * Subclasses should be used to create more interesting rules.
 * Matching should apply the rules one by one.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class TypeMatchRule {

    TypeMatchingRuleSet.CharacterRule rule = TypeMatchingRuleSet.CharacterRule.NoCharacterRule;
    
    public TypeMatchRule( TypeMatchingRuleSet.CharacterRule rule ) {
        this.rule = rule;
    }

    public CharacterRule getCharacterRule() {
        return rule;
    }

    public void setRule( CharacterRule rule ) {
        this.rule = rule;
    }
    
    public String toString() {
        return rule.toString();
    }
    
    /**
     * Apply an arbitrary rule on the value. This method can be used and/or
     * overridden for subclasses to provide their own method for the rule.
     *
     * @param t The hierarchy that needs the rule applied. Subclasses may need
     * to access the hierarchy.
     * @param value The value to be transformed in applying the rule. Note this
     * value may have already been altered by previous rules.
     * @return the altered string
     */
    public String transformByRule( TypeHierarchy t, String value ) {
        if ( rule == CharacterRule.IgnoreCase ) {
            return value.toLowerCase();
        }
        if ( rule == CharacterRule.IgnoreSpaces ) {
            return value.replaceAll( "\\s", "" );
        }
        if ( rule == CharacterRule.IgnoreSpecial ) {
            return value.replaceAll( "[^a-zA-Z0-9]", "" );
        }
        return value;
    }
}
