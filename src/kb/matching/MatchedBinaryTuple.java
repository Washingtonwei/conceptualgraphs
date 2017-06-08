package kb.matching;

import java.util.*;
import charger.obj.*;
import kb.BinaryTuple;

/**
    A tuple that contains additional information about how it's been matched, e.g., within a matching process.
    @see BinaryRelationMatch
     @since 3.5b2
 */
public class MatchedBinaryTuple extends BinaryTuple
{
        /** whatever "score" this tuple earns in a matching operation. */
    public float score = 0;
        /** when iterating through tuples, this helps keep track of whether tuple is available for matching */
    public boolean available = true;
    
        /** Justification for the score; may just be a string identifying a row of a master graph that matched. */
    public String reason = "-";

    public float relation_score = 0;
    public float concept1_score = 0;
    public float concept2_score = 0;
    //public float totalScore = 2;

    
    public MatchedBinaryTuple()
    {
    
    }
    
   public MatchedBinaryTuple( Concept c1, GNode r, Concept c2 )
    {   super( c1, r, c2 ); }
    
    /**
        Returns the total score obtained in matching this tuple.
     */
    public float getTotalScore()  { return concept1_score + concept2_score; }
    
    //public float getMaxPossible() { return totalScore; }

    public String toHTML()
    {
        String html = "";
        
        html += super.toHTML();
            html += " <TD width=40>" + ((concept1_score == 0)?"-":concept1_score) + "\n";
            html += " <TD width=40>" + ((concept2_score == 0)?"-":concept2_score) + "\n";
            html += " <TD width=150>" + reason + "\n";
            html += " <TD width=40>" + getTotalScore() + "\n";
        
        return html;
    }
    
    
}