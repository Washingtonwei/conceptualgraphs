/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.GraphMetrics;
import charger.Global;
import charger.util.Tag;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * While the models themselves are stored in MTeamPhase objects, this class allows us 
 * to hold statistics on one team as a whole, so that we can write summary statistics when needed.
 * The main analysis process populates one of these objects per team.
 * 
 * @see MTeamPhase
 * @see MMAnalysisMgr
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class MTeamMetrics {
    
    public static int MAX_TEAM_SIZE = 15;
        
    public String projName;
    public String groupName;
    public String teamName;
    
    /** Used for purposes of retrieving from a hash map */
    public String key;
    
    public static DecimalFormat nformat = new DecimalFormat();
    
    
    /** The congruence values for concept congruence, up to a maximum team size */
    private EnumMap<MPhase, float[  ] > conceptCongruence = new EnumMap< >(MPhase.class);
    /** The congruence values for relation congruence, up to a maximum team size */
    private EnumMap<MPhase, float[  ] > relationCongruence = new EnumMap< >(MPhase.class);
    
    /** The average pairwise congruence for concept congruence, up to a maxModels team size */
    private EnumMap<MPhase, Float > avgPairConceptCongruence = new EnumMap< >(MPhase.class);
    /** The average pairwise congruence for relation congruence, up to a maxModels team size */
    private EnumMap<MPhase, Float > avgPairRelationCongruence = new EnumMap< >(MPhase.class);

    /** The number of distinct concepts (i.e., cardinality of set of distinct concepts), up to a maximum team size */
    private EnumMap<MPhase, Integer> ncDistinct = new EnumMap<>(MPhase.class);
    /** The number of distinct relations (i.e., cardinality of set of distinct tuples), up to a maximum team size */
    private EnumMap<MPhase, Integer> ntDistinct = new EnumMap<>(MPhase.class);
    
    /** The number of models for each phase. Still need to determine whether it includes special models or not.  */
    private EnumMap<MPhase, Integer> numModels = new EnumMap<>(MPhase.class);
    
    /** The maximum number of models in any phase; this helps in printing tables to know the max number of rows.  */
    private int maxModels = 0;
    
    /** The names of each field of statistics being emitted for each team. Used in a titleRow row
     * and other summaries. */
    private static ArrayList<String> fieldNames = new ArrayList<String>();
    
    /** Holds (for each phase) the distinctiveness measure for  concepts  */
    private EnumMap<MPhase, Float> conceptStdDevByCount = new EnumMap<>(MPhase.class);
    /** Holds (for each phase) the distinctiveness measure for  relations (tuples)  */
    private EnumMap<MPhase, Float> tupleStdDevByCount = new EnumMap<>(MPhase.class);
    
    /** Holds (for each phase) the distinctiveness measure for  concepts  */
    private EnumMap<MPhase, Float> conceptStdDevByPercent = new EnumMap<>(MPhase.class);
    /** Holds (for each phase) the distinctiveness measure for  relations  */
    private EnumMap<MPhase, Float> tupleStdDevByPercent = new EnumMap<>(MPhase.class);
        
    /** the average of the long values representing the creation dates of the models */
    private EnumMap<MPhase, Long> creationDates = new EnumMap<>(MPhase.class);

    /** the average of the long values representing the creation dates of the models */
    private EnumMap<MPhase, ArrayList<GraphMetrics>> graphMetrics = new EnumMap<>(MPhase.class);

    private boolean useSynonyms = false;
    
//        // Should be re-worked because we only consider team/group synonyms covering ALL phases at once 
//    private EnumMap<MPhase, Boolean> hasSynonyms = new EnumMap<>(MPhase.class);
            // should be set based on whether there's a synonym file, since analysis merges group and team synonyms
    private boolean hasTeamSynonyms = false;
    
    private ClusterCollection teamSynonyms = null;
    
    private ArrayList<ClusterCollection> synonyms = new ArrayList<>();
    
    private EnumMap<MPhase, Boolean> hasObserverModel = new EnumMap<>(MPhase.class);
    
    private EnumMap<MPhase, Boolean> hasTeamModel = new EnumMap<>(MPhase.class);
    private EnumMap<MPhase, Boolean> hasCombinedModel = new EnumMap<>(MPhase.class);
    
    private EnumMap<MPhase, String> observerAnalysis = new EnumMap<>(MPhase.class);
    
    
    private enum MetricType {
        
        numModels,
        distinctComponents,
        avgPairCongruence,
        singletonCountStdDev,
        singletonPctStdDev,
        levelCongruence,
        creationDate,
        specials    // a place to put whether there's an observer, combined, team etc.
    }
    
    public static EnumMap<MetricType, String> metricLabels = new EnumMap<>( MetricType.class );
    
    private MProject project = null;

    /**
     * Use the given parameters to initialize a metrics object that can be
     * populated with relevant statistics
     */
    public MTeamMetrics( String projName, String groupName, String teamName ) {

        this.project = MMConst.getProjectByName( projName);
        this.projName = projName;
        this.groupName = groupName;
        this.teamName = teamName;

        nformat.setMaximumFractionDigits( 3 );
        nformat.setMinimumFractionDigits( 3 );

        if ( fieldNames.isEmpty() ) { // initialize field names
            initializeFieldNames();
            initializeMetricLabels();
        }
        
        for ( MPhase p : MPhase.values() ) {
            conceptCongruence.put( p, new float[ MAX_TEAM_SIZE + 1 ] );
            Arrays.fill( conceptCongruence.get( p ), -1 );
            relationCongruence.put( p , new float[ MAX_TEAM_SIZE + 1 ] );
            Arrays.fill( relationCongruence.get( p ), -1 );
            creationDates.put( p, 0L );
            graphMetrics.put( p, new ArrayList<GraphMetrics>() );
//            hasSynonyms.put( p, new Boolean( false ));
            hasObserverModel.put( p, new Boolean( false ));
            hasCombinedModel.put( p, new Boolean( false ));
            hasTeamModel.put( p, new Boolean( false ));
        }
    }
    
    /** @return true, if the number of end models is the same as the number of beginning models; false otherwise.
     * */
    public boolean isComplete() {
        for ( MPhase p : MPhase.values() ) {
            numModels.put( p , getNumModels( p ));
        }
        if ( numModels.get( MPhase.Beginning) == numModels.get( MPhase.End ) )
            return true;
        else
            return false;
    }

//    /**
//     * Set whether a phase has any synonyms at all
//     * @param p Phase being set
//     * @param b True to indicate that there are any synonyms (whether used or not)
//     * @see #useSynonyms
//     */
//    public void setHasSynonyms( MPhase p, boolean b ) {
//        hasSynonyms.put( p, new Boolean( b ));
//    }
//    
//        /**
//     * Get whether a phase has any synonyms at all
//     * @param p Phase being requested
//     * @return true if the phase of this team has a non-empty synonym list.
//     * @see #useSynonyms
//     */
//
//    public boolean hasSynonyms( MPhase p ) {
//        Boolean b = hasSynonyms.get( p );
//        return b.booleanValue();
//    }
    
    public void addSynonymGroup( ClusterCollection synGroup ) {
        synonyms.add( synGroup );
    }
    
    public void setTeamSynonymGroup( ClusterCollection synGroup ) {
        this.teamSynonyms = synGroup;
    }
    
    public ClusterCollection getTeamSynonymGroup() {
        if ( teamSynonyms == null )
            return new ClusterCollection();
        return teamSynonyms;
    }
    
        /**
     * Set whether a phase has an observer model
     * @param p Phase being set
     * @param b True to indicate that there is an observer model
     */
    public void setHasObserverModel( MPhase p, boolean b ) {
        hasObserverModel.put( p, new Boolean( b ));
    }
        
    
    public boolean hasObserverModel( ) {
        for ( MPhase p : MPhase.values() ) {
            if ( hasObserverModel.get(  p ).booleanValue() ) return true;
        }
        return false;
    }

    public boolean hasObserverModel( MPhase p ) {
        if ( hasObserverModel.get(  p ).booleanValue() ) return true;
        return false;
    }


        /**
     * Set whether a phase has a team model
     * @param p Phase being set
     * @param b True to indicate that there is a team model
     */
    public void setHasTeamModel( MPhase p, boolean b ) {
        hasTeamModel.put( p, new Boolean( b ));
    }
    
    public boolean hasTeamModel() {
        for ( MPhase p : MPhase.values() ) {
            if ( hasTeamModel.get(  p ).booleanValue() ) return true;
        }
        return false;
    }

        /**
     * Set whether a phase has a combined (result of "Merge") model
     * @param p Phase being set
     * @param b True to indicate that there is a combined model.
     */
    public void setHasCombinedModel( MPhase p, boolean b ) {
        hasCombinedModel.put( p, new Boolean( b ));
    }
    
    public boolean hasCombinedModel() {
        for ( MPhase p : MPhase.values() ) {
            if ( hasCombinedModel.get(  p ).booleanValue() ) return true;
        }
        return false;
    }

    
    /** Uses concept lists to determine number of models in a phase. */
    public int getNumModels( MPhase phase ) {
        int num = MAX_TEAM_SIZE;
        float checkarray[] = conceptCongruence.get( phase );
        while ( num >= 0 && checkarray[ num] < 0 ) {
            num = num - 1;
        }
        return num;
    }

    /** Return a key-like string for any given parameters */
    public static String makeKey( String exptName, String groupName, String teamName) {
        return exptName + "_" + groupName + "_" + teamName;
    }

    /** Return a key-like string for this object. */    
    public String getKey() {
        return projName + "_" + groupName + "_" + teamName;
    }
    
      /** Sets the number of distinct concepts, relations, etc.  for this team and phase */
    public void setDistinct( MMComponentKind kind, MPhase phase, int value  ) {
            Global.info( "Loading team metrics object for " + getKey() + ", phase " + phase.name() + ", " + kind.name() );
        switch (kind ) {
            case CONCEPT: {
                ncDistinct.put( phase, new Integer( value ) );
                break;
            }
            case RELATION: {
                ntDistinct.put( phase, new Integer( value ) );
               break;
            }
        }
    }
    
  /** Sets the number of distinct concepts, relations, etc.  for this team and phase */
    public int getDistinct( MMComponentKind kind, MPhase phase  ) {
        switch (kind ) {
            case CONCEPT: {
                if ( ncDistinct.get( phase ) == null ) return 0;
                else return ncDistinct.get( phase ).intValue();
            }
            case RELATION: {
                if ( ntDistinct.get( phase ) == null ) return 0;
                else return ntDistinct.get( phase ).intValue();
            }
        }
        return 0;
    }
    
    /**
     * Stores the value needed for a summary report
     * @param kind One of the allowed measurement kinds
     * @param phase One of the phases of a team's operation
     * @param level The level congruence being stored
     * @param value The value (usually between 0 and 1)
     */
    public void setCongruenceValue( MMComponentKind kind, MPhase phase, int level, float value ) {
        switch (kind ) {
            case CONCEPT: {
                conceptCongruence.get( phase )[ level ] = value ;
//                    Global.info( "set congruence value level " + level + " value " + value );
                break;
            }
            case RELATION: {
                 relationCongruence.get( phase )[level] = value ;
               break;
            }
        }
    }
    
    /**
     * Stores the value needed for a summary report
     * @param kind One of the allowed measurement kinds
     * @param phase One of the phases of a team's operation
     * @param value The value (usually between 0 and 1)
     */
    public void setAvgPairCongruenceValue( MMComponentKind kind, MPhase phase, float value ) {
        switch (kind ) {
            case CONCEPT: {
                avgPairConceptCongruence.put( phase, new Float( value ) ) ;
                break;
            }
            case RELATION: {
                 avgPairRelationCongruence.put( phase, new Float( value ) ) ;
               break;
            }
        }
    }
    
    /**
     * Set this team/phase/kind singleton standard deviation for later reporting.
     * @param kind The kind of component under consideration
     * @param phase The phase under consideration
     * @param usePercentage Whether to use the percentage of singletons or the actual count
     * @param value The value being set.
     */
    public void setSingletonStdDev( MMComponentKind kind, MPhase phase, boolean usePercentage, double value ) {
        switch (kind ) {
            case CONCEPT: {
                if ( usePercentage ) 
                    conceptStdDevByPercent.put( phase, new Float(value) );
                else 
                    conceptStdDevByCount.put( phase, new Float(value) );
                break;
            }
            case RELATION: {
                if ( usePercentage ) 
                    tupleStdDevByPercent.put( phase, new Float(value) );
                else 
                    tupleStdDevByCount.put( phase, new Float(value) );
                break;
            }
        }
    }
    

        /**
     * Stores the value needed for a summary report
     * @param kind One of the allowed measurement kinds
     * @param phase One of the phases of a team's operation
     * @param level The level congruence being stored
     */
    public float getCongruenceValue( MMComponentKind kind, MPhase phase, int level  ) {
        switch (kind ) {
            case CONCEPT: {
                return conceptCongruence.get( phase )[ level ];
            }
            case RELATION: {
                return relationCongruence.get( phase )[ level ];
            }
        }
        return 0f;
    }
    
        /**
     * Retrieve the singleton atandard deviation metric for this team.
     * @param kind One of the allowed measurement kinds
     * @param byPercent if true, then calculate the standard deviation of each model's percent of singletons;
     * otherwise just calculate the standard deviation in the count of the singletons.
     * @param phase One of the phases of a team's operation
     */
    public float getSingletonStdDev( MMComponentKind kind, MPhase phase, boolean byPercent ) {
        switch (kind ) {
            case CONCEPT: {
                if ( byPercent ) {
                    if ( conceptStdDevByPercent.get( phase ) != null)
                        return conceptStdDevByPercent.get( phase ).floatValue();
                    } else {
                    if ( conceptStdDevByCount.get( phase ) != null)
                        return conceptStdDevByCount.get( phase ).floatValue();
                    }
                break;
                }
            case RELATION: {
                if ( byPercent ) {
                    if ( tupleStdDevByPercent.get( phase ) != null)
                        return tupleStdDevByPercent.get( phase ).floatValue();
                    } else {
                    if ( tupleStdDevByCount.get( phase ) != null)
                        return tupleStdDevByCount.get( phase ).floatValue();
                    }
                break;
                }
        }
        return 0f;
    }
    
            /**
     * Retrieves the average pair congruence values needed for a summary report
     * @param kind One of the allowed measurement kinds
     * @param phase One of the phases of a team's operation
     */
    public float getAvgPairCongruenceValue( MMComponentKind kind, MPhase phase  ) {
        switch (kind ) {
            case CONCEPT: {
                if ( avgPairConceptCongruence.get( phase ) != null)
                    return avgPairConceptCongruence.get( phase ).floatValue();
                break;
            }
            case RELATION: {
                if ( avgPairRelationCongruence.get( phase ) != null)
                    return avgPairRelationCongruence.get( phase ).floatValue();
                break;
            }
        }
        return 0f;
    }
    
    public void addGraphMetrics( MPhase p, GraphMetrics gmetrics ) {
        graphMetrics.get( p ).add( gmetrics );
    }
    
    /** Establish a creation time for this phase's models */
    public void setCreationDate( MPhase p, long date ) {
        creationDates.put( p, new Long(date));
    }

    /** Return the creation time for this phase's models */
    public long getCreationDate( MPhase p ) {
        return creationDates.get( p ).longValue();
    }
    
    /**
     * format the date string to be human readable.
     * @param p
     * @return an M-dd-yy date; if zero then return ="
     */
    public String getCreationDateString( MPhase p ) {
        SimpleDateFormat dateform = new SimpleDateFormat( " MM-dd-yyyy" );
        if ( getCreationDate( p ) > 0 ) 
            return dateform.format( getCreationDate( p ) );
        else
            return "-"; 
    }

    public void setUseSynonyms(boolean useSynonyms) {
        this.useSynonyms = useSynonyms;
    }

    public String getObserverAnalysis( MPhase p) {
        return observerAnalysis.get( p );
    }

    public void setObserverAnalysis( MPhase p, String s  ) {
        observerAnalysis.put( p  , s);
    }
    
    
    
    
    /**
     * Show the team as a table. Much of the formatting of the table is hardwired into
     * this method, including a number of explicit HTML tags.
     * The general algorithm is similar to the other output options, except that this
     * one has to append new values to each row in turn, and then output each row at the very end.
     * @return HTML table with stats
     */
    public String getTabularSummary() {
            // This method is really messy even after factoring -- should it have been a JTable after all? Only time will tell....
        
            /** The topmost rows of the table being produced here */
        ArrayList<String> titleRow = makeTitleRow();
            /** A list of label, value pairs. Each new phase, kind will be appended to the value string */
        EnumMap<MetricType, String> rows = new EnumMap<>(MetricType.class);
            /** Each string constitutes an entire HTML table row when completely formed */
        String[] levelRows = new String[ MAX_TEAM_SIZE + 1];       
        
        StringBuilder header = new StringBuilder("");
        
                // We need the maxModels number of models.
        for ( MPhase p : MPhase.values() ) 
            if ( getNumModels( p ) > maxModels )
                maxModels = getNumModels( p );
        
        makeFirstColumn( header, rows, levelRows );
        
                // Build one column for each phase and component kind.
                //  Append the current "column" into each row. Very clunky, but I didn't see a "slice" operator.
                // Note the commented lines that allow quick switching of kind and phase order
        for ( MPhase  phase : MPhase.values() ) {          // uncomment to have C-R-C-R-C-R tabular display
          for ( MMComponentKind kind : MMComponentKind.values() ) {     // uncomment to have C-R-C-R-C-R tabular display
       // for ( MMComponentKind kind : MMComponentKind.values() ) {     // uncomment to have C-C-C-R-R-R tabular display
       //      for ( MPhase  p : MPhase.values() ) {    // uncomment to have C-C-C-R-R-R tabular display
             appendOneKindPhaseColumn( kind, phase, header, rows );
             for ( int level = 2; level <= maxModels; level++ ) {
                 float value = getCongruenceValue( kind, phase, level );
                 if ( value < 0 )
                     levelRows[level] += Tag.tdc( " - ");
                 else 
                     levelRows[level] += Tag.tdc( nformat.format( value ) );
             }
            }
        }
                    // Arrange the final output
                    // Start the table
        String s = "<table style=\"font-family:Arial; font-size:14;\" border=\"0\">\n";
                    // add strings from header
        for ( String h : titleRow )
            s += Tag.tr( h );
        s += Tag.tr( header.toString() );
 
                // Add all one-per-phase-kind information. Here's where the order is specified.
        s += Tag.tr( rows.get( MetricType.creationDate ) );
        s += Tag.tr( rows.get( MetricType.numModels ) );
        s += Tag.tr( rows.get( MetricType.specials ) );
        s += Tag.tr( rows.get( MetricType.distinctComponents ) );
        s += Tag.tr( rows.get( MetricType.singletonCountStdDev ) );
        s += Tag.tr( rows.get( MetricType.singletonPctStdDev ) );
        s += Tag.tr( rows.get( MetricType.avgPairCongruence ) );
        
        s += Tag.tr( Tag.td( Tag.sp) );     // a blank line to separate the level congruences

               // Add the per level rows.
        for ( int level = 2; level <= maxModels; level++ ) {
            s += Tag.tr( levelRows[ level ]);
        }
                    // Finish the table/div
        s += "</table>\n";
        return s;
    }
    
    /** Creates a set of strings with some team-level information, such as the name, completeness, etc. */
    private ArrayList<String> makeTitleRow() {
                        /** The topmost rows of the table being produced here */
        ArrayList<String> titleRow = new ArrayList<String>();
        
                        // Build the "completeness" phrase
        String complete;
        if ( this.isComplete() )
            complete = Tag.colorDiv( "#AAFFAA", "Complete: YES" );
        else
            complete = Tag.colorDiv( "#FFAAAA", "Complete: NO" );
        
        String synonymInfo = "Team synonyms: ";
        File teamSynsFile = project.getFrame().getAnalyzer().teamSynonymFile( projName, groupName, teamName);
        if ( teamSynsFile.exists() && teamSynsFile.length() > 0 ) {
            if ( useSynonyms ) 
                synonymInfo += " present and used";
            else
                synonymInfo += " present (not used)";
        } else
            synonymInfo += " absent";
        
                // Build the topmost summary information rows
        titleRow.add( Tag.tdspan( 7, Tag.colorDiv( "#DDDDDD", " Team: " + Tag.bold( teamName.toUpperCase() ) + " Group: " + groupName ) ) );
        titleRow.add( Tag.td( complete )
                + Tag.tdspan( 3, synonymInfo + "." )
                + Tag.tdspan( 2, hasTeamModel() ? "HAS TEAM MODEL." : "No team model." ) );

        titleRow.add( Tag.sp );
        
        return titleRow;
    }
    
    /**
     * Initialize the one per kind-phase information rows, starting with their labels.
     * @param headerRow "Top left" cell of table, usually left blank
     * @param rows This method just provides the starting labels
     * @param levelRows This method provides labels up to the maximum number of models in this team
     */
    private void makeFirstColumn(StringBuilder headerRow, EnumMap<MetricType, String> rows, String[] levelRows) {
                // Build the label (first) column
        //header = Tag.td( "Metric" );   
        headerRow.append( Tag.td( Tag.sp ) );   
        
        rows.put( MetricType.creationDate, Tag.td( metricLabels.get(MetricType.creationDate ) ) );
        rows.put( MetricType.numModels, Tag.td( metricLabels.get(MetricType.numModels ) ) );
        rows.put( MetricType.specials, Tag.td( metricLabels.get(MetricType.specials ) ) );
        rows.put( MetricType.distinctComponents, Tag.td( metricLabels.get(MetricType.distinctComponents ) ) );
        rows.put( MetricType.avgPairCongruence, Tag.td( metricLabels.get(MetricType.avgPairCongruence ) ) );
        rows.put( MetricType.singletonCountStdDev, Tag.td( metricLabels.get(MetricType.singletonCountStdDev ) ) );
        rows.put( MetricType.singletonPctStdDev, Tag.td( metricLabels.get(MetricType.singletonPctStdDev ) ) );
        
                // Build the label (first) column for the congruence levels, whose max varies between teams
        for ( int level = 2; level <= maxModels; level++ ) {
             levelRows[level] = Tag.td( metricLabels.get( MetricType.levelCongruence) + " [" + level + "]" );
        }
        
    }
    
    /**
     *                 Handles all of the one per phase-kind 
                Build a column for each phase and kind.
               Append the current "column" text onto each row. Very clunky, but I didn't see a "slice" operator.

     * @param kind whether concept or relations
     * @param p
     * @param headerRow The header strings to which this column's header is appended
     * @param rows The row strings to which this column's numbers are appended
     */
    private void appendOneKindPhaseColumn( MMComponentKind kind, MPhase p, StringBuilder headerRow, EnumMap<MetricType, String> rows) {
        headerRow.append( Tag.td( Tag.sp + p.abbr( 3 ).toUpperCase() + " " + kind.toString().toLowerCase() + Tag.sp ) );
        rows.put( MetricType.creationDate, rows.get( MetricType.creationDate )
                + Tag.tdc(  getCreationDateString( p ) ) );
        rows.put( MetricType.numModels, rows.get( MetricType.numModels )
                + Tag.tdc( ( getNumModels( p ) > 0 ) ?  "<strong>" + getNumModels( p ) + "</strong>" : " - " ) );
        
        if ( getNumModels( p ) == 0 )
            rows.put( MetricType.specials, rows.get( MetricType.specials) + Tag.tdc( "-"));
        else
            rows.put( MetricType.specials, rows.get( MetricType.specials )
                + Tag.tdc(  
                    ((hasObserverModel.get( p ).booleanValue()) ? "obs  " : "") +
                    ((hasCombinedModel.get( p ).booleanValue()) ? "comb  " : "") +
                    ((hasTeamModel.get( p ).booleanValue()) ? "team" : "" ) ) );
        
        rows.put( MetricType.distinctComponents, rows.get( MetricType.distinctComponents )
                + Tag.tdc( ( getNumModels( p ) > 0 ) ? "" + getDistinct( kind, p ) : " - " ) );
        rows.put( MetricType.avgPairCongruence, rows.get( MetricType.avgPairCongruence )
                + Tag.tdc( ( getNumModels( p ) > 0 ) ? "" + nformat.format( getAvgPairCongruenceValue( kind, p ) ) : "-" ) );
        rows.put( MetricType.singletonCountStdDev, rows.get( MetricType.singletonCountStdDev )
                + Tag.tdc( ( getNumModels( p ) > 0 ) ? "" + nformat.format( getSingletonStdDev( kind, p, false ) ) : "-" ) );
        rows.put( MetricType.singletonPctStdDev, rows.get( MetricType.singletonPctStdDev )
                + Tag.tdc( ( getNumModels( p ) > 0 ) ? nformat.format( 100 * getSingletonStdDev( kind, p, true ) ) + " %" : "-" ) );
    }

    /**
     * Returns a string showing statistics required for running statistics external to Charger.
     * @return a tab-separated list of the metrics values
     * @see #getOneLineHeaderWithDelimiter(String)
     */
    public String getOneLineStatsWithDelimiter( String delim ) {
        String s = projName + delim + groupName + delim + teamName + delim;
        s += (isComplete() ? "yes":"no") + delim;
        s += (useSynonyms ? "yes":"no" ) + delim;
        for ( MPhase p : MPhase.values() ) {
           s += getNumModels( p ) + delim;
           s += getCreationDateString( p )  + delim;       // Added 02-16-2014 to include creation date with model
           for ( MMComponentKind kind : MMComponentKind.values() ) {
                s += getDistinct( kind, p ) + delim;
                s += nformat.format( getSingletonStdDev( kind, p, false )) + delim;
                s += nformat.format( getSingletonStdDev( kind, p, true )) + delim;
                s += nformat.format( getAvgPairCongruenceValue( kind, p )) + delim;
                for ( int level = 1; level <= MAX_TEAM_SIZE; level++) {
                    s += nformat.format( getCongruenceValue( kind, p, level)) + "\t";
                }
            }
        }
        return s;
    }

    

    /**
     * Fills the fieldNames array with the field name strings for output. This allows
     * flexibility in creating statistics either as one-liners or as text output.
     * Any changes to the desired fieldnames must be made both here and in getOneLineStats
     */
    protected void initializeFieldNames() {
        fieldNames.clear();
        fieldNames.add( "ExptName" );
        fieldNames.add( "GroupName");
        fieldNames.add( "TeamName" );
        fieldNames.add( "Complete" );
        fieldNames.add( "Use synonyms");
        for ( MPhase p : MPhase.values() ) {
            
            fieldNames.add( "models(" + p.abbr() + ")" );
            
            // Added Feb 16, 2014 to include dates in the spreadsheet
            fieldNames. add( "date(" + p.abbr() + ")" );
            
            fieldNames.add( "cDistinct(" + p.abbr() + ")" );
            fieldNames.add( "CSingletonCountStdDev(" + p.abbr() + ")" );
            fieldNames.add( "CSingletonPctStdDev(" + p.abbr() + ")" );
            fieldNames.add( "CAvgPairCongruence(" + p.abbr() + ")" );
            for ( int level = 1; level <= MAX_TEAM_SIZE; level++) {
                fieldNames.add ( "CC(" + p.abbr() + "):" + level );
            }
            
            fieldNames.add( "rDistinct(" + p.abbr() + ")" );
            fieldNames.add( "rSingletonCountStdDev(" + p.abbr() + ")" );
            fieldNames.add( "rSingletonPctStdDev(" + p.abbr() + ")" );
            fieldNames.add( "RAvgPairCongruence(" + p.abbr() + ")" );
            
          for ( int level = 1; level <= MAX_TEAM_SIZE; level++) {
                fieldNames.add ( "RC(" + p.abbr() + "):" + level );
            }
       }

    }
    
    public void initializeMetricLabels() {
        metricLabels.put( MetricType.creationDate, "Date (mm-dd-yy)" );
        metricLabels.put( MetricType.numModels, "Number of models" );
        metricLabels.put( MetricType.specials, "Special models present" );
        metricLabels.put( MetricType.distinctComponents, "Distinct components" );
        metricLabels.put( MetricType.avgPairCongruence, "Avg pair congruence" );
        metricLabels.put( MetricType.singletonCountStdDev, "Singleton count std dev" );
        metricLabels.put( MetricType.singletonPctStdDev, "Singleton pct std dev" );
        metricLabels.put( MetricType.levelCongruence, "Level congruence" );
   }
    
        /** The headers for the statistics output by getOneLineStatsWithDelimiter 
     * @param delim separator between the fields (e.g., "\t", etc.)
     * @see #getOneLineStatsWithDelimiter(String)
     * @see #initializeFieldNames() 
     * @return A delim-delimited string, newline terminated.
     * */

    public static String getOneLineHeaderWithDelimiter( String delim ) {
        nformat.setMaximumFractionDigits( 3 );
        nformat.setMinimumFractionDigits( 3 );

        String s = "";
        for ( String name : fieldNames ) {
            s += name + delim;
        }
        return s + "\n";
    }
    
}
