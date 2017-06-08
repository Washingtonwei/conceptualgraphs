package kb.matching;

import kb.matching.MatchedBinaryTuple;
import charger.*;
import charger.obj.*;
import java.util.*;
import java.text.*;
import kb.BinaryTuple;

/**
    Represents the abstraction of a match between all binary relations in two  graphs.
    * The two graphs are linked from this object. It maintains a list of matched tuples for statistical purposes.
    One of the graphs is considered the "master". Since it must match itself exactly, its score is maxScore,
     its precision is 1.0 and its recall is 1.0. The score, precision and recall of a BinaryRelationMatch object
     is therefore the "goodness" of the other graph (called here the "graph to match") with respect to the master graph.
     @since 3.5b2
 */
public class BinaryRelationMatch
{
    protected float masterScore = 0;
    protected float matchedScore = 0;
        
    protected float precision = 0.0f;
    protected float recall = 0.0f;
    
            /** Used for displaying integer values. */
    protected NumberFormat intf = NumberFormat.getNumberInstance();
            /** Used for displaying floating point scores */
    protected NumberFormat ff = NumberFormat.getNumberInstance();
    
        /** Unlike ConceptManager, this class uses MatchedBinaryTuple's, not an unstructured table entry array list.
        */
    private ArrayList<MatchedBinaryTuple> _masterTuples = null;        // unscored tuples
    private ArrayList<MatchedBinaryTuple> _masterScoredTuples = null;
    private ArrayList<MatchedBinaryTuple> _toMatchTuples = null;
    
    private AbstractTupleMatcher _tupleMatcher = new BasicTupleMatcher();
    
    private Graph _masterGraph = null;
    private Graph _toMatchGraph = null;
    private String _masterName = null;
    private String _toMatchName = null;
    
    public AbstractTupleMatcher getTupleMatcher() { return _tupleMatcher; }
    
    /**
     * Tell the relation matcher which specific tuple matcher objects to use.
     * @param m 
     */
    public void setTupleMatcher( AbstractTupleMatcher m ) 
    { 
        _tupleMatcher = m; 
    } 

    public float getTotalScore( ArrayList v )
    {
        float total = 0;
        if ( v == null ) return 0;
        Iterator iter = v.iterator();
        while ( iter.hasNext() ) 
            total += ((MatchedBinaryTuple)iter.next()).getTotalScore();
        return total;
    }
    
    /** Returns the total score obtainable; i.e., the max score of the master tuples */
    public float getMasterScore()
    { 
        return getTotalScore( _masterScoredTuples );
    }

    /** Returns the raw score obtained; i.e., the "count" of the things matched. */
    public float getMatchedScore()
    { 
        return getTotalScore( _toMatchTuples );
    }

    /** Returns the max score possible derived from the graph to match; i.e., the "count" of everything
        in the graph to match. This is used to determine precision scores. In effect, this score is what
        would be the maximum possible if the graph to match were completely and exactly correct. 
        @return the result of getting each tuple in the graph-to-be-matched, and matching it to itself.
    */
    public float getMatchedMaxPossibleScore()
    { 
        if ( _toMatchTuples == null ) return 0;
        float total = 0;
        Iterator iter = _toMatchTuples.iterator();
        while ( iter.hasNext() ) 
        {
           // total += ((MatchedBinaryTuple)iter.next()).getMaxPossible();
           MatchedBinaryTuple t = (MatchedBinaryTuple)(iter.next());
           total += _tupleMatcher.compare( t, t );
        }
        return total;
    }

        /** Returns the percentage of things in the graph to match that also appear in the master. */
    public float getPrecision() 
    { 
        if ( _toMatchTuples.size() == 0 ) return 0;
        return getMatchedScore() / getMatchedMaxPossibleScore();
    }
        /** Returns the percentage of things in the master that also appear in the graph to match. */
    public float getRecall() 
    {
        if ( _masterTuples.size() == 0 ) return 0;
        return getMatchedScore() / getMasterScore();
    }
    
    /**
    
     */
    public float getNormScore()
    {
        return (float)Math.sqrt( getPrecision() * getRecall() );
    }
    
        /** Returns the name of the master graph */
    public String getMasterName() { return _masterName; }
    
        /** Returns the name of the graph to be matched */
    public String getMatchedName() { return _toMatchName; }
    
    /**
        Create a match between a master graph and a match candidate.
        @param masterG the graph to serve as the "correct" or master graph of the comparison.
        @param masterName a label for the master graph
        @param toMatchGraph the graph to be matched to the master
        @param toMatchName a label for the toMatch graph
     */
    public BinaryRelationMatch( Graph masterG, String masterName, Graph toMatchGraph, String toMatchName )
    {
        setupMaster( masterG );
        _masterName = masterName;
        _toMatchName = toMatchName;
        _toMatchGraph = toMatchGraph;
        matchTheTuples();
    }
    
    /**
        Set up a matcher to be used by matchBinaryRelations/1
        @param masterG the graph to serve as the "correct" or master graph of the comparison.
        @param masterName a label for the master graph
     */
    public BinaryRelationMatch( Graph masterG, String masterName )
    {
        setupMaster( masterG );
        _masterName = masterName;
    }
    
    public void matchAGraph( Graph toMatchGraph, String toMatchName )
    {
        _toMatchName = toMatchName;
        _toMatchGraph = toMatchGraph;
        _toMatchTuples = makeBinaryTuples( _toMatchGraph );
        matchTheTuples();
    }
    
    /**
        Load the matcher with an entry for each binary relation. Each entry looks like this (from makeTableEntry):
		<ul>
			<li>element 0 - phrase,
			<li>element 1 - concept label,
			<li>element 2 - relation label, 
			<li>element 3 - concept label, 
			<li>element 4 - actual 1st (CharGer) concept
			<li>element 5 - actual (CharGer) relation
			<li>element 6 - actual 2nd (CharGer) concept
		</ul>

        @see kb.ConceptManager#makeTableEntryArrayList(Concept, GNode, Concept, kb.ConceptManager.ConceptsToInclude)
    */
    public void setupMaster( Graph masterG )
    {
        intf.setMaximumFractionDigits( 0 );
        intf.setMinimumFractionDigits( 0 );

        ff.setMaximumFractionDigits( 3 );
        ff.setMinimumFractionDigits( 1 );

        _masterTuples = makeBinaryTuples( masterG );
        _toMatchTuples = makeBinaryTuples( masterG );
        matchTheTuples();
        _masterScoredTuples = _toMatchTuples;
       // _toMatchTuples = null;
    }
    
    
    /**
        Converts a ArrayList of BinaryTuples to an HTML work sheet (usable for manual analysis of the relations), with a summary.
        Table entries have added score for left and right concepts and the master row that we matched. These are all strings.
        @param tuples a ArrayList of BinaryTuple or one of its subclasses.
        @param summary an identifying string, used to identify the table in HTML, but not otherwise displayed.
     */
   public  String convertTuplesToHTMLtable( ArrayList tuples, String summary )
    {
        String XMLSummary = charger.xml.XMLGenerator.quoteForXML( summary.trim() );
       
        String html = "";
       // html += "<font face=\"sans-serif\">\n";
        //html += "<br>&nbsp;\n";

        html += "<br><b>" + XMLSummary + "</b>\n";
        html += "<TABLE border=\"1\" cellspacing=\"1\" bordercolor=\"blue\" summary=\"" + XMLSummary + "\">\n";
 
        for (int row = 0; row <  tuples.size(); row++)
        {
            html += "<TR>\n";
            
            html += " <TD width=30> R" + (int)(row+1) + "\n";
            
            BinaryTuple bt = (BinaryTuple)tuples.get( row );

            html += bt.toHTML();
        }
        

        html += "<TR>\n";
        html += " <TD width=120 align=\"right\" colspan=2 ><b>Precision</b> = " + 
                intf.format(getMatchedScore()) + " / " + intf.format( getMatchedMaxPossibleScore()) + "\n"; 
        html += " <TD width=40>" + ff.format(getMatchedScore() / getMatchedMaxPossibleScore()) + "\n";
        html += " <TD width=120 align=\"right\" colspan=2><b>Recall</b> = " + 
                intf.format( getMatchedScore()) + " / " + intf.format( getMasterScore()) + "\n";
        html += " <TD width=60>" + ff.format(getMatchedScore() / getMasterScore()) + "\n";
        html += " <TD align=\"right\">TOTAL= \n";
        html += " <TD width=40>" + intf.format( getMatchedScore() ) + "\n";
        html += "</TABLE>";
        
        return html;
    }
    
    /**
        Return a one-line HTML TR summary, suitable for inclusion in an HTML table
     */ 
    public String getSummaryHTML()
    {
        String html = "";
        
        html += "<TR>\n";
        html += " <TD>" + _toMatchName + "\n"; 
                // precision
        html += " <TD align=\"right\">" + ff.format( getMatchedScore() / getMatchedMaxPossibleScore() ) + "\n";
                // recall
        html += " <TD align=\"right\">" + ff.format( getMatchedScore() / getMasterScore() ) + "\n";
                // P-R composite
        html += " <TD align=\"right\">" + ff.format( getNormScore() ) + "\n";
        html += " <TD align=\"right\">" + ff.format( getMatchedScore() ) + "\n";
        html += " <TD align=\"right\">" + ff.format( getMatchedMaxPossibleScore() ) + "\n";
        html += " <TD>" + _masterName + "\n";
        
        return html;
    }
    
    public static String getSummaryHeaderHTML()
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
        Displays the details of the master graph in HTML form.
     */
    public String masterDetailToHTML()
    {
       //return convertTuplesToHTMLtable( _masterTuples, _masterName );
       return convertTuplesToHTMLtable( _masterScoredTuples, _masterName );
    }
    
    public String matchedDetailToHTML()
    {
        if ( _toMatchTuples != null )
            return convertTuplesToHTMLtable( _toMatchTuples, _toMatchName );
        else
            return "NOTHING TO MATCH: " + _masterName;
    }
    
    /**
        Mimics getBinaryRelationTuples, but constructs a vector of BinaryTuple objects.
        If a relation is above binary (e.g., ternary, etc.) then its constituents are split into their corresponding binary
            "equivalents". That is, all the concepts that are involved in the relation are paired up.
        @param masterG graph whose tuples are to be collected
        @see kb.ConceptManager#getBinaryRelationTuples
     */
    public ArrayList<MatchedBinaryTuple> makeBinaryTuples( Graph masterG )
    {
        ArrayList<MatchedBinaryTuple> tupleVector = new ArrayList<MatchedBinaryTuple>();
                                
                        // for every relation node in the graph
        Iterator nodeiter = new DeepIterator( masterG, GraphObject.Kind.GNODE);
        while ( nodeiter.hasNext() )
        {
            GNode gn = (GNode)nodeiter.next();
            if ( gn instanceof Relation || gn instanceof Actor )
            {
                               // craft.Craft.say( " found a relation/actor " + gn.getTextLabel() + " -- id " + gn.objectID );
                GNode rel = gn;     // note: might be either relation or actor
                GEdge[] edges = (GEdge[])rel.getEdges().toArray( new GEdge[0] );
                                // for every pair of nodes linked to that relation, choose one to be output and one input
                                //   Note that this works for n-ary relations even though we're supposed to be limited
                                //   to binary. If either c1 or c2 ends up null, it means:
                                //        The relation wasn't binary to begin with
                                //        The relation's arcs were out of the ordinary (zero or two or more inputs, outputs)
                                //   In either case, we ignore it! Only straight binary will be included.
                for ( int n1 = 0; n1 < edges.length; n1++ )
                    for ( int n2 = n1 + 1; n2 < edges.length; n2++ )
                    {
                                // Find which concept is the input concept and which is the output 
                        Concept c1 = null;
                        Concept c2 = null;
                        if ( edges[ n1 ].howLinked( rel ) == GEdge.Direction.FROM ) 
                                c2 = (Concept)edges[ n1 ].toObj;
                        else 
                                c1 = (Concept)edges[ n1 ].fromObj;

                        if ( edges[ n2 ].howLinked( rel ) == GEdge.Direction.FROM ) 
                                c2 = (Concept)edges[ n2 ].toObj;
                        else 
                                c1 = (Concept)edges[ n2 ].fromObj;
                        
                        if ( c1 == null || c2 == null ) continue;    // Relation is not binary or ordinary

                        tupleVector.add( new MatchedBinaryTuple( c1, rel, c2 )  );
                    }
            }
        }
        Collections.sort( tupleVector, new BinaryTuple.TupleComparator() );
        return tupleVector;
    }
    
    public void matchTheTuples()
    {
        matchTheTuplesByMatchedTuples();
    }

    /**
        Performs the actual matching algorithm on two given graphs, 
            considering each row of the to-be-matched graph in turn. 
        Uses the previously associated "master" graph and the graph to be matched.
        Recall and precision scores are obtained for how well the graph to match did match.
        Sets all the totals but does not immediately return anything.
        @see #getSummaryHTML
     */
    public void matchTheTuplesByMatchedTuples()
    {
                // mark all the tuples as available.
        Iterator<MatchedBinaryTuple> iter = _masterTuples.iterator();
        while ( iter.hasNext() ) 
            iter.next().available = true;
                        
        iter = _toMatchTuples.iterator();
        while ( iter.hasNext() ) 
            iter.next().available = true;
                        
                // Iterate through master tuples and find the best match for each one.
                // Mark to-be-matched tuple as used once we've decided to match it.
        
        int masterRow = 0;
        iter = _masterTuples.iterator();
        while ( iter.hasNext() )        // for each to-be-matched tuple, decide whether we should match 
        {
            MatchedBinaryTuple t = (MatchedBinaryTuple)iter.next();
            
            float score = 0;
            int matchedRow = 0;
            int matchedRowWithMaxScore = -1;     // -1 means we never found a good one
            float maxScore = 0;
                    // go through each to-be-matched tuple, looking for the best score possible. 
                    // first occurrence of best score gets chosen.
            while ( matchedRow < _toMatchTuples.size() )
            {
                MatchedBinaryTuple toMatch = _toMatchTuples.get( matchedRow );
                if ( toMatch.available )
                {
                    score = _tupleMatcher.compare( t, toMatch );
                    if ( score > maxScore )
                    {
                        matchedRowWithMaxScore = matchedRow;
                        maxScore = score;
                    }
                }
                matchedRow++;
            }
            if ( matchedRowWithMaxScore != -1 )
            {
                _toMatchTuples.get( matchedRowWithMaxScore ).reason = 
                        "R" + (int)(masterRow+1) + "  (" + _masterName + ")";
                _toMatchTuples.get( matchedRowWithMaxScore ).available = false;
                float newscore = _tupleMatcher.scoreTupleMatch( t, _toMatchTuples.get( matchedRowWithMaxScore ) ) ;
                if ( newscore != maxScore ) 
                    Global.error( "tuple's matched score " + newscore + " isn't what it was " + maxScore );
            }
            masterRow++;
        }
    }
    
    /**
        Performs the actual matching algorithm on two given graphs, considering each row of the master in turn. 
        Recall and precision scores are obtained for how well the graph to match did match.
        
     */
    public void matchTheTuplesByMaster()
    {
                // mark all the master tuples as not being used yet.
        Iterator<MatchedBinaryTuple> iter = _masterTuples.iterator();
        while ( iter.hasNext() ) 
            iter.next().available = true;
                        
                // Iterate through to-be-matched tuples and find the best match for each one.
                // Mark master's tuple as used once we've decided to match it.
                        // this is really not the most favorable strategy!
                        // it's more favorable to go from master to matched:
                        //    for each row of the master, find the best match in the to-be-matched list.!!!
        
        iter = _toMatchTuples.iterator();
        while ( iter.hasNext() )        // for each to-be-matched tuple, decide whether we should match 
        {
            MatchedBinaryTuple t = (MatchedBinaryTuple)iter.next();
            
            float score = 0;
            int masterRow = 0;
            int masterRowWithMaxScore = -1;     // -1 means we never found a good one
            float maxScore = 0;
                    // go through each matchable tuple, looking for the best score possible. 
                    // first occurrence of best score gets chosen.
            while ( masterRow < _masterTuples.size() )
            {
                MatchedBinaryTuple master = _masterTuples.get( masterRow );
                if ( master.available )
                {
                    score = _tupleMatcher.compare( master, t );
                    if ( score > maxScore )
                    {
                        masterRowWithMaxScore = masterRow;
                        maxScore = score;
                    }
                }
                masterRow++;
            }
            if ( masterRowWithMaxScore != -1 )
            {
                t.reason = "R" + (int)(masterRowWithMaxScore+1) + "  (" + _masterName + ")";
                _masterTuples.get( masterRowWithMaxScore ).available = false;
                float newscore = _tupleMatcher.scoreTupleMatch( _masterTuples.get( masterRowWithMaxScore ), t ) ;
                if ( newscore != maxScore ) 
                    Global.error( "tuple's matched score " + newscore + " isn't what it was " + maxScore );
            }
        }
    }
    

}