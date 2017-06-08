/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.matching;

/**
 * Abstraction of any matcher of CG constructs. 
 * In the future will contain hooks to the support used for this match.
 * @since 3.8
 * @author Harry Delugach
 */
public abstract class AbstractMatcher {
    
    
     private boolean verboseEnabled = false;
     
     private boolean ignoreCase = true;

     /** Allows matchers to tailor their output at the caller's option */
    public void setVerboseEnabled(boolean verboseEnabled) {
        this.verboseEnabled = verboseEnabled;
    }

    /** Tells whether verbose is enabled for this matcher. */
    public boolean isVerboseEnabled() {
        return verboseEnabled;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
     
     
    
        /**
        Give a description of the rules used, in HTML. This should probably be more structured.
        Recommended that the explanation use the UL tag to list the rules.
        * Can use verbose to decide its granularity.
     */
    abstract public String explainYourself();

}
