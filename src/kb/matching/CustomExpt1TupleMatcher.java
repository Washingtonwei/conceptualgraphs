package kb.matching;

/**
    For use in custom experiments. Currently just adds equality match between "process" and "activity"
         @since 3.5b2

 */
public class CustomExpt1TupleMatcher extends BasicTupleMatcher
{

    public boolean generousStringMatch( String ss1, String ss2 )
    {
        if ( super.generousStringMatch( ss1, ss2 ) ) 
        {
                        // whether either string appears to start with "no" or "not"
            boolean s1not = false;
            boolean s2not = false;
            String[] toks = ss1.split( nonAlphaNumericRegex );
            if ( toks[0].equalsIgnoreCase( "not" ) || toks[0].equalsIgnoreCase( "no" ) ) s1not = true;
            toks = ss2.split( nonAlphaNumericRegex );
            if ( toks[0].equalsIgnoreCase( "not" ) || toks[0].equalsIgnoreCase( "no" ) ) s2not = true;
            if ( s1not == s2not ) return true;
            else return false;
        }
                // strictly for our first experiment, we goofed and need to make these synonyms.
        if ( ss1.equalsIgnoreCase( "Process" ) && ss2.equalsIgnoreCase( "Activity" ) ) return true;
        if ( ss2.equalsIgnoreCase( "Process" ) && ss1.equalsIgnoreCase( "Activity" ) ) return true;
        return false;
    }
    
    /**
        Explains the matcher, by adding one additional rule to the basic matcher:
        namely, consider "process" and "activity" equilvalent.
     */
    public String explainYourself()
    {
        String s = "CustomExpt1TupleMatcher:\n<ul>\n";
        s += bullet( "Consider \"Process\" == \"Activity\".");
        s += bullet( "Consider string beginning with \"not\" or \"no\" to be negated.");
        s += "</ul>\n";

        s += super.explainYourself();

        return s;
    }
    


}   // class
