/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.util.Tag;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Handles the basic output for MM reports.
 *
 * @see MMAnalysisMgr
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MMAnalysisReporter {

    public MMAnalysisFrame mmf = null;
    public MMAnalysisMgr mgr = null;
    private File htmlTeamFile = null;

    public MMAnalysisReporter( MMAnalysisMgr manager, MMAnalysisFrame frame ) {
        this.mmf = frame;
        this.mgr = manager;
    }
    
    public MProject getProject() {
        return mmf.getProject();
    }

    /**
     * Summarizes each group showing number of teams, number/pct complete and
     * number/pct of middle-team-model teams. Assumes that all team metrics are
     * already generated; if not, then it will simply report whatever results
     * have been generated and attached to team metrics objects. Also shows a
     * grand total over all groups.
     *
     * @param metrics A collection of team metrics objects, one for each team.
     * Key is the string returned by MTeamMetrics's getKey() method
     * @return an HTML formatted string
     * @see MMAnalysisMgr#generateGrandSummary
     */
    public String getOverview( HashMap<String, MTeamMetrics> metrics ) {
        final int tableWidth = 600;

        // Hashmap key is the group name, value is for that group.
        HashMap<String, Integer> numTeams = new HashMap<>();
        HashMap<String, Integer> numComplete = new HashMap<>();
        HashMap<String, Integer> numCompleteWTeamModel = new HashMap<>();
        HashMap<String, Integer> numModels = new HashMap<>();

        StringBuilder s = new StringBuilder( Tag.table( tableWidth ) );
        for ( MTeamMetrics tm : metrics.values() ) {
            if ( numTeams.get( tm.groupName ) == null ) {
                numTeams.put( tm.groupName, new Integer( 0 ) );
                numComplete.put( tm.groupName, new Integer( 0 ) );
                numCompleteWTeamModel.put( tm.groupName, new Integer( 0 ) );
                numModels.put( tm.groupName, new Integer( 0 ) );
            }
            int k = numTeams.get( tm.groupName ).intValue();
            numTeams.put( tm.groupName, new Integer( ++k ) );
            int num = numModels.get( tm.groupName ).intValue();
            for ( MPhase p : MPhase.values() ) {
                num += tm.getNumModels( p ) < 0 ? 0 : tm.getNumModels( p );
                if ( mmf.observerIsSpecial.isSelected() && tm.hasObserverModel( p ) ) {
                    num++;
                }
            }
            numModels.put( tm.groupName, new Integer( num ) );
            if ( tm.isComplete() ) {
                k = numComplete.get( tm.groupName ).intValue();
                numComplete.put( tm.groupName, new Integer( ++k ) );
                if ( tm.hasTeamModel() ) {
                    k = numCompleteWTeamModel.get( tm.groupName ).intValue();
                    numCompleteWTeamModel.put( tm.groupName, new Integer( ++k ) );
                }
            }
        }
        s.append( Tag.hr ).append( Tag.tr( "<td style=\"width:100\">Group</td>"       /*Tag.td( "Group" )*/
                + Tag.tdc( "No. teams" ) + Tag.tdc( "No. complete" ) + Tag.tdc( "Pct complete" )
                        + Tag.tdc( "Complete w/team model" ) + Tag.tdc( "Pct w/team model" ) + Tag.tdc( "Total models" ) ));
        // Initialize the grand total counters
        int numTeamsTotal = 0;
        int numCompleteTotal = 0;
        int numCompleteWTeamModelTotal = 0;
        int numModelsTotal = 0;
        for ( String group : numTeams.keySet() ) {
            s.append( Tag.tr( Tag.td( group ) + Tag.tdc( numTeams.get( group ).intValue() + "" )
                    + Tag.tdc( numComplete.get( group ).intValue() + "" )
                    + Tag.tdc( ( numComplete.get( group ).intValue() * 100 ) / numTeams.get( group ).intValue() + " %" )
                    + Tag.tdc( numCompleteWTeamModel.get( group ).intValue() + "" )
                    + Tag.tdc( ( numComplete.get( group ).intValue() == 0 ) ? "0 %" : ( numCompleteWTeamModel.get( group ).intValue() * 100 ) / numComplete.get( group ).intValue() + " %" )
                    + Tag.tdc( numModels.get( group ).intValue() + "" ) ) );
            numTeamsTotal += numTeams.get( group ).intValue();
            numCompleteTotal += numComplete.get( group ).intValue();
            numCompleteWTeamModelTotal += numCompleteWTeamModel.get( group ).intValue();
            numModelsTotal += numModels.get( group ).intValue();
        }
        s.append( Tag.tr( Tag.td( "TOTAL" ) + Tag.tdc( numTeamsTotal + "" ) + Tag.tdc( numCompleteTotal + "" )
                + Tag.tdc( ( numCompleteTotal * 100 ) / numTeamsTotal + " %" ) + Tag.tdc( numCompleteWTeamModelTotal + "" )
                + Tag.tdc( ( numCompleteTotal == 0 ) ? "0 %" : ( numCompleteWTeamModelTotal * 100 ) / numCompleteTotal + " %" )
                + Tag.tdc( numModelsTotal + "" ) ) );
        return s.toString() + Tag._table;
    }

    /**
     * Creates a grand summary of all teams' models, in all groups. Produces one
     * line per team
     *
     */
    public String getSpreadsheet( String delim ) {
        String s = MTeamMetrics.getOneLineHeaderWithDelimiter( delim );
        for ( String group : mgr.getProject().getGroupList() ) {
            if ( !mgr.includeTrash && mgr.trashGroups.contains( group ) ) {
                continue;
            }
            if ( !mgr.includeAdmin && mgr.adminGroups.contains( group ) ) {
                continue;
            }
            for ( String team : mgr.getProject().getTeamList( group ) ) {
                s += mgr.getTeamMetrics( mgr.getProject().getName(), group, team ).getOneLineStatsWithDelimiter( delim ) + "\n";
            }
            s += "\n";
        }
        return s;
    }

    /**
     * Creates a pretty tabular summary of all teams' models, in all groups.
     * Attaches some project-wide information at the beginning. Perhaps that
     * should be made its own chunk of code? TOD
     *
     * Produces one table per team
     *
     * @return an HTML formatted string
     */
    public String getTabularSummary() {
        StringBuilder s = new StringBuilder( Tag.h( 2, "Mental Model Acquisition Summary" ) );
        s.append( Tag.p( "Created by " + Global.getInfoString() + Tag.br + "Project: " + getProject().getName() ) );

        s.append( getOverview( mgr.teamMetrics ) );
        for ( String g : getProject().getGroupList() ) {
            if ( mgr.trashGroups.contains( g ) && ! mgr.includeTrash ) {
                continue;
            } else {
                s.append( Tag.colorDiv( "#AACCFF", Tag.h( 3, "Group " + g ) ) );
                if ( mmf.useGroupSynonyms.isSelected() && mmf.useGroupSynonyms.isEnabled() ) {
                    ClusterCollection syngroup = getProject().groupSynonyms.get( g );
                    boolean selected = mmf.showSynonyms.isSelected();
                    s.append( syngroup.formatAsHTML( selected ) );
                } else {
                    s.append( Tag.h( 3, "Not using group synonyms." ) );
                }
                if ( mmf.ignoreRelationSense.isSelected() ) {
                    s.append( Tag.h( 3, "Ignoring relation direction for relation matching!"));
                }
                for ( String t : mgr.getProject().getTeamList( g ) ) {
                    s.append( mgr.getTeamMetrics( mgr.getProject().getName(), g, t ).getTabularSummary() ).append( "\n");
                }
                s.append( "\n" );
            }
        }
        return s.append( "\n" ).toString();
    }


    /**
     * Shows the analysis options and display options used for this run.
     *
     * @return HTML tagged string showing the options.
     */
    public String showAnalysisOptions() {
        String returnvalue = Tag.table( 450 )
                + Tag.tr( Tag.td( "Use synonyms:" ) + Tag.td( mmf.useSynonyms.isSelected() + "  " ) + Tag.td( Tag.sp( 10 ) )
                + Tag.td( "Group synonyms:" ) + Tag.td( mmf.useGroupSynonyms.isSelected() + "  " ) )
                + Tag.tr( Tag.td( "Treat observer special:" ) + Tag.td( mmf.observerIsSpecial.isSelected() + " " ) )
                + Tag.tr( Tag.td( "  Do observer pairing:" ) + Tag.td( mmf.observerPairEnabled.isSelected() + " " ) + Tag.td( Tag.sp( 10 ) )
                + Tag.td( "Do pair-wise metrics:" ) + Tag.td( mmf.pairwiseEnabled.isSelected() + " " ) )
                + Tag.tr( Tag.td( "Show total coref lists:" ) + Tag.td( mmf.showCorefsTotal.isSelected() + " " ) + Tag.td( Tag.sp( 10 ) )
                + Tag.td( "Show corefs per level:" ) + Tag.td( mmf.showCorefsPerLevel.isSelected() + " " ) )
                + Tag._table;
        return returnvalue;
    }

    /**
     * Writes the analysis results as text/html to a file. Assumes the analysis
     * results are in the text fields for each phase, and therefore follows
     * whatever options have been specified. Uses the currentKindOfAnalysis to
     * decide the report name. Does not re-generate the results.
     *
     * @return the File to which the results are written.
     */
    public File writeTeamResults() {
        if ( getProject().currentTeam == null ) {
            return null;
        }

        MFile teamFile = new MFile();
        teamFile.setType( MFileType.TEAMREPORT );
        teamFile.setProject( mgr.getProject().getName() );
        teamFile.setGroup( getProject().currentGroup );
        teamFile.setTeam( getProject().currentTeam );
        if ( MMConst.useNewFolderStructure ) {
            htmlTeamFile = new File( teamFile.mfileAbsoluteFolder( getProject().MMSubmittedFolder ), teamFile.makeFilename() );

        } else {
            htmlTeamFile = new File( getProject().MMSubmittedFolder, teamFile.makeFilename() );
        }

        boolean restore = mmf.enableRawHTML.isSelected();
        mmf.enableRawHTML.setSelected( true );
        // not needed if we're writing a header rather than appending -- htmlTeamFile.delete();
        writeToHTMLFile( htmlTeamFile, Tag.body
                + Tag.h( 2, "Team report for project:" + Tag.bold( getProject().getName() )
                + "  Group:" + Tag.bold( getProject().currentGroup ) + "  Team:" + Tag.bold( getProject().currentTeam ) ), false );
        writeToHTMLFile( htmlTeamFile, Tag.p( "Created by " + Global.getInfoString() ), true );
//        if ( mgr.teamIsComplete( mgr.currentTeamPhase ) ) {
//            writeToHTMLFile( htmlTeamFile, Tag.p( Tag.bold( " Team models are complete!" ) ), true );
//        } else {
//            writeToHTMLFile( htmlTeamFile, Tag.p( " Team models are NOT complete!" ), true );
//        }

        writeToHTMLFile( htmlTeamFile, showAnalysisOptions(), true );

        writeToHTMLFile( htmlTeamFile, mgr.getTeamMetrics().getTabularSummary(), true );

        if ( mmf.useSynonyms.isSelected()) {
            writeToHTMLFile( htmlTeamFile, 
                    mgr.getTeamMetrics().getTeamSynonymGroup().formatAsHTML( mmf.showSynonyms.isSelected() ), true );
            if ( mmf.useGroupSynonyms.isSelected() ) 
            writeToHTMLFile( htmlTeamFile, 
                    mgr.currentGroupSynonymGroup.formatAsHTML( mmf.showSynonyms.isSelected() ), true );
        }
        
        for ( MPhase p : MPhase.values() ) {
            writeToHTMLFile( htmlTeamFile, mgr.phaseAnalysisResults.get( p ), true );
//            writeToHTMLFile( htmlTeamFile, mmf.phaseAnalysisResultsDisplay.get( p ).getText(), true );
        }

        mmf.enableRawHTML.setSelected( restore );
        return htmlTeamFile;
    }

    /**
     *
     * @param htmlFile An html file for writing
     * @param toWrite The content to write; should be HTML-tagged
     * @param append whether to append or overwrite.
     */
    public void writeToHTMLFile( File htmlFile, String toWrite, boolean append ) {
        if ( htmlFile != null ) {
            try {
                BufferedWriter out = new BufferedWriter( new FileWriter( htmlFile, append ) );
                out.write( toWrite );
                //  Global.info( toString() );
                out.close();
            } catch ( Exception e ) {
                Global.error( "writeToHTMLFile: " + e.getMessage() );
            }
        }
    }
}
