package kb.matching;

import charger.Global;
import java.util.*;
import java.text.*;
import charger.util.*;


/**
    Calculates. collects and reports the metrics of a group of BinaryRelationMatch objects.
    In operation, forms a collection of BinaryRelationMatch objects, getting their summary information.
    A match can be a member of more than one match group.
    @see BinaryRelationMatch
 */
public class BinaryRelationMatchGroupMetrics extends AbstractMatchGroupMetrics
{
    private ArrayList<BinaryRelationMatch> _matches = new ArrayList<BinaryRelationMatch>();
    private boolean _upToDate = false;       // whether current overall statistics are up to date.
    private String _explanation = null;      // used to determine whether all matches have same explanation (i.e., same rules)

    private FloatStatistics _matchedScore = new FloatStatistics( "Matched Scores" );
        /** In effect, this score is what
        would be the maximum possible if the graph to match were completely and exactly correct.  */
    private FloatStatistics _matchedMaxPossibleScore = new FloatStatistics( "Matched Max Possible Scores" );
    private FloatStatistics _precision = new FloatStatistics( "Precision" );
    private FloatStatistics _recall = new FloatStatistics( "Recall" );
    private FloatStatistics _normScore = new FloatStatistics( "Normalized sqrt(R*P)" );
    private FloatStatistics _maxPossible = new FloatStatistics( "Max Possible" );
    
    
            /** Used for displaying integer values. */
    protected NumberFormat intf = NumberFormat.getNumberInstance();
            /** Used for displaying floating point scores */
    protected NumberFormat ff = NumberFormat.getNumberInstance();
   
        /** @param s The name of this group (can be null) */
    public BinaryRelationMatchGroupMetrics( String s ) 
    {
        super( s ); 
        intf.setMaximumFractionDigits( 0 );
        intf.setMinimumFractionDigits( 0 );

        ff.setMaximumFractionDigits( 3 );
        ff.setMinimumFractionDigits( 1 );

    }
        
     
       /**
        Adds a member to the match group. Makes no assumption about whether the match has actually been performed or not.
        If match is already a member, does nothing.
        @param m The match object to be added.
     */
    public void addMatch( BinaryRelationMatch m )
    {
        if ( ! _matches.contains( m ) )
        {
            _matches.add( m );
            _upToDate = false;
        }
    }
    
    /**
        Return a one-line HTML TR summary, suitable for inclusion in an HTML table.
        Intended to be used within a wrapper that creates the actual table.
        @see #getSummaryHeaderHTML
     */ 
    public String getSummaryHTML( BinaryRelationMatch m )
    {
        String html = "";
        
        html += "<TR>\n";
        html += " <TD>" + m.getMatchedName() + "\n"; 
            // precision
        html += " <TD align=\"right\">" + ff.format( m.getMatchedScore() / m.getMatchedMaxPossibleScore()) + "\n";
            // recall
        html += " <TD align=\"right\">" + ff.format( m.getMatchedScore() / m.getMasterScore()) + "\n";
            // precision-recall composite
        html += " <TD align=\"right\">" + ff.format( m.getNormScore() ) + "\n";
        html += " <TD align=\"right\">" + ff.format( m.getMatchedScore() ) + "\n";
        html += " <TD align=\"right\">" + ff.format( m.getMatchedMaxPossibleScore() ) + "\n";
        html += " <TD>" + m.getMasterName() + "\n";
        
        return html;
    }
    
    /**
        Return the header labels for a one-line summary, suitable for inclusion in an HTML table.
        Intended to be used within a wrapper that creates the actual table.
        @see #getSummaryHTML
    */
    public String getSummaryHeaderHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>Graph Name\n";
        html += "<TD  align=\"right\">Precision\n";
        html += "<TD  align=\"right\">Recall\n";
        html += "<TD  align=\"right\">P-R Comp\n";
        html += "<TD  align=\"right\">Score\n";
        html += "<TD align=\"right\">Max possible\n";
        html += "<TD>Master Graph\n";
        
        return html;
    }
    
    /**
        Formats for HTML display the totals of all the matches; e.g., totals, averages, etc.
        @return a set of TR labeled strings suitable for inclusion in a table.
     */
    public String getSummaryStatisticsHTML()
    {
        return 
                //getTotalsHTML() +
            getAveragesHTML() + getStdDeviationsHTML() + getMaxesHTML() + getMinsHTML();
    }
    
    /** Returns the totals of each of the summary items in getSummaryHeaderHTML */
    public String getTotalsHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>TOTAL\n";
        html += "<TD  align=\"right\">" + ff.format( _precision.sum ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _recall.sum ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _normScore.sum ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _matchedScore.sum ) + "\n";
        html += "<TD align=\"right\">" + ff.format( _matchedMaxPossibleScore.sum ) + "\n";
        html += "<TD>TOTAL\n";
        
        return html;
    }

    /** Returns the totals of each of the summary items in getSummaryHeaderHTML */
    public String getAveragesHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>MEAN\n";
        html += "<TD  align=\"right\">" + ff.format( _precision.getAverage() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _recall.getAverage() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _normScore.getAverage() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _matchedScore.getAverage() ) + "\n";
        html += "<TD align=\"right\">" + ff.format( _matchedMaxPossibleScore.getAverage() ) + "\n";
        html += "<TD>MEAN\n";
        
        return html;
    }
    
    /** Returns the totals of each of the summary items in getSummaryHeaderHTML */
    public String getStdDeviationsHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>STD DEV\n";
        html += "<TD  align=\"right\">" + ff.format( _precision.getStdDeviation() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _recall.getStdDeviation() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _normScore.getStdDeviation() ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _matchedScore.getStdDeviation() ) + "\n";
        html += "<TD align=\"right\">" + ff.format( _matchedMaxPossibleScore.getStdDeviation() ) + "\n";
        html += "<TD>STD DEV\n";
        
        return html;
    }
    
        /** Returns the totals of each of the summary items in getSummaryHeaderHTML */
    public String getMaxesHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>MAX\n";
        html += "<TD  align=\"right\">" + ff.format( _precision.max ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _recall.max ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _normScore.max ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _matchedScore.max ) + "\n";
        html += "<TD align=\"right\">" + ff.format( _matchedMaxPossibleScore.max ) + "\n";
        html += "<TD>MAX\n";
        
        return html;
    }


        /** Returns the totals of each of the summary items in getSummaryHeaderHTML */
    public String getMinsHTML()
    {
        String html = "";
        html += "<TR>\n";
        html += "<TD>MIN\n";
        html += "<TD  align=\"right\">" + ff.format( _precision.min ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _recall.min ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _normScore.min ) + "\n";
        html += "<TD  align=\"right\">" + ff.format( _matchedScore.min ) + "\n";
        html += "<TD align=\"right\">" + ff.format( _matchedMaxPossibleScore.min ) + "\n";
        html += "<TD>MIN\n";
        
        return html;
    }


        /** Displays (in HTML) the complete summary of this group.
            Displays the following:
            <ul>
                <li>The matching strategy (in English) or a disclaimer if the matches used different strategies.
                <li>The recall, precision and scores for each match.
                <li>The mean, min, max and std dev for the group.
            </ul>
        
         */
    public String getCompleteSummaryHTML()
    {
        scanAllMatches();
        
        String html = "<font face=\"sans-serif\" size=\"-1\">\n";
        
        html += "<TABLE WIDTH=600>\n<TR><TD>\n";
        html += _explanation;
        html += "</TABLE>\n";
        
        html += "<TABLE border=\"1\" cellspacing=\"1\" bordercolor=\"green\">\n";

        html += getSummaryHeaderHTML( );
        
        Iterator<BinaryRelationMatch> iter = _matches.iterator();
        while ( iter.hasNext() )
        {
            BinaryRelationMatch m = iter.next();
            html += getSummaryHTML( m );
        }
        
        html += "<TR>"; 
        for ( int k = 0; k < 7; k++ ) html += "<TD>&nbsp;";
        html += "\n";
        
        html += getSummaryStatisticsHTML();
        
        html += "</TABLE>\n";
        
        return html;
    }
    
    
    /**
        Returns the set of matches that are all members of this group.
        Does a safe copy (i.e., clone) but does not ensure that all the matches exist.
     */
    public ArrayList getMatches()
    {
        ArrayList v =(ArrayList) _matches.clone();
        return v;
    }
    
    /**
        Gather all the stats, updating all the total, statistics, etc.
        Establishes a single explanation string (if there's only one).
        @see AbstractTupleMatcher#explainYourself
     @since 3.5b2
     */
    public void scanAllMatches()
    {
        _matchedScore.reset();
        _matchedMaxPossibleScore.reset();
        _precision.reset();
        _recall.reset();
        _normScore.reset();
        
        _explanation = Global.knowledgeManager.createCurrentTupleMatcher().explainYourself();
        
        Iterator<BinaryRelationMatch> iter = _matches.iterator();
        int numMatches = 0;
        
        while ( iter.hasNext() )
        {
            BinaryRelationMatch m = iter.next();
            numMatches++;
                    // if explanation is different from previous (non-null) one, then there's no consistent explanation.
            if ( _explanation != null )
            {
                if (! m.getTupleMatcher().explainYourself().equals( _explanation ) )
                {
                    _explanation = "Not all matches used the same set of rules.";
                }
            }
            else
                _explanation = m.getTupleMatcher().explainYourself();
            
            _matchedScore.addValue( m.getMatchedScore() );
            _matchedMaxPossibleScore.addValue( m.getMatchedMaxPossibleScore() );
            _precision.addValue( m.getPrecision() );
            _recall.addValue( m.getRecall() );
            _normScore.addValue( (float)Math.sqrt( m.getPrecision() * m.getRecall() ) );
        }

        _upToDate = true;
    }
    
    /**
        A suggested way of reducing the entire match group (really precision and recall) into one number.
        Take sqrt( precision * recall ).
     */
    public float getOverallScore()
    {
        //return (float)Math.sqrt( _precision.getAverage() * _recall.getAverage() );
        return _normScore.getAverage();
    }
}
