package mm;

import charger.Global;
import charger.util.CProgressFrame;
import charger.util.Tag;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Main class for the analysis portion of the Mental Models package. Has its own
 * local behavior as well as providing basic functionality to the analysis
 * window. There should only one of these analysis/frame pairs instantiated at a
 * time. Instantiates the MMAnalysisFrame and the MMAnalysisReporter needed to
 * complete its mission. <br> There are several HashMap objects in this class
 * that have MPhase objects as their key. This is because each phase is analyzed
 * separately, but identically. Therefore there is need for stored values and
 * results that are associated with one particular phase. <br> The file
 * structure is as follows: <dl><dt>MMAT Parent Folde</dt>r<dd>Where the
 * runnable ChargerXXX.jar file resides as well as any utility files needed for
 * running the MMAT analysis. Also where both the Submitted folder and the Saved
 * folder reside.</dd> <dt>Submitted folder</dt><dd>Where the files to be
 * analyzed reside. Immediately nested within the parent folder.</dd>
 * <dt>Submitted folder</dt><dd>Where files saved (but not submittedFile)
 * reside. Immediately nested within the parent folder. Often is empty except in
 * the middle of an editing session.</dd> </dl>
 *
 * @see MMAnalysisFrame
 * @see MMAnalysisReporter
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public final class MMAnalysisMgr {

    /**
     * An associative array to uniformly handle initializing each phase label on
     * the analysis frame
     */
    private static EnumMap<MPhase, String> initialLabel = new EnumMap<>( MPhase.class );
    private static EnumMap<MMComponentKind, Color> colors = new EnumMap<>( MMComponentKind.class );


    public MMComponentKind currentKindOfAnalysis = MMComponentKind.CONCEPT;
    private ArrayList<String> projNames = new ArrayList();
    public boolean includeTrash = false;
    public boolean includeAdmin = true;
    public int totalTeamPhases = 0;
    /**
     * The actual html text of the results -- displayed in
     * phaseAnalysisResultsDisplay and in reports
     */
    public EnumMap<MPhase, String> phaseAnalysisResults = new EnumMap<>( MPhase.class );
    /**
     * For silencing screen updates when performing operations on many teams
     */
    public boolean updateScreenWhileAnalyzing = true;
    /**
     * An associative array to allow uniform handling of all team phases
     */
    public EnumMap<MPhase, MTeamPhase> currentTeamPhase = new EnumMap<>( MPhase.class );
    /**
     * An associative array to allow uniform handling of all team phase combinedModel
     * lists
     */
    protected EnumMap<MPhase, ArrayList<MModel>> currentTeamModels = new EnumMap<>( MPhase.class );
    /**
     * Keeps colors strings (e.group., "#EEDDEE") for each phase's output
     */
    private EnumMap<MPhase, String> phaseColorsConcepts = new EnumMap<>( MPhase.class );
    private EnumMap<MPhase, String> phaseColorsRelations = new EnumMap<>( MPhase.class );
    private EnumMap<MPhase, JLabel> phaseDateLabel = new EnumMap<>( MPhase.class );
    /**
     * Associates a team name with its team metrics object
     */
    public HashMap<String, MTeamMetrics> teamMetrics = new HashMap<>();
    //public File htmlFile = null;
    public ClusterCollection currentTeamSynonymGroup = null;
    public ClusterCollection currentGroupSynonymGroup = null;
    /**
     * A single file name for the team
     */
    private File htmlTeamFile = null;
    /**
     * A window for editing the synonymClusters
     */
    public SynonymEditor synonymEditor = null;
    /**
     * The main interface for users to organize and examine the MM analysis. The
     * intent is that there will only be one of these.
     */
    protected  MMAnalysisFrame mmf = null;
    CProgressFrame progressframe = new CProgressFrame( mmf );
    int progress = 0;
    boolean summaryFinished = false;
    /**
     * The output report generator for this analysis ohject
     */
    protected static MMAnalysisReporter reporter = null;
    private SimpleDateFormat timestampFormat = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z" );
    private SimpleDateFormat MDYdateFormat = new SimpleDateFormat( "M-dd-yy" );
    /**
     * The names (simple strings) of the groups to consider "trash"
     */
    public ArrayList<String> trashGroups = new ArrayList<>( Arrays.asList( "-trash-", "trash" ) );
    public ArrayList<String> adminGroups = new ArrayList<>( Arrays.asList( "-admin-" ) );

    public MMAnalysisMgr( MMAnalysisFrame frame ) {
        if ( mmf == null ) {
            mmf = frame; 
            reporter = new MMAnalysisReporter( this, mmf );
        }
        getProject().MMAnalysisParent = getProject().MMSubmittedFolder.getParentFile();
                // TODO: Saved models (not submitted) are now embedded in the team folder.
        getProject().MMSavedFolder = new File( frame.getProject().MMAnalysisParent, MMConst.MMSavedFolderName );
        setupAnalysisWindow();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mmf.dispose();
            mmf = null;
            reporter = null;

            super.finalize();
        } catch ( Throwable t ) {
            throw t;
        } finally {
            super.finalize();
        }
    }
    
    public void tearDown() {
        currentTeamModels.clear();
        phaseAnalysisResults.clear();
        teamMetrics.clear();
    }

    public  MMAnalysisFrame getFrame() {
        return mmf;
    }
    
    public MProject getProject() {
        
       return getFrame().getProject();
    }
    
    

    /**
     * This should be called only once per instantiated analysis window.
     * Initializes color scheme, hash maps and other bookkeeping needs. Sets
     * options to their default values.
     *
     */
    public void setupAnalysisWindow() {
        mmf.setVisible( true );
        initializeColors();
        initializeHashMaps();
        getProject().clearProject();
        refreshAnalysisWindow( false );
//        refreshAnalysisWindow( true );
        initializeTopPanels();
        MMFileManager.removeEmptyFolders( getProject().MMSubmittedFolder );
//        mmf.setVisible( true );
//        mmf.toFront();
        
    }

    /**
     * Make this window visible, and refresh the combinedModel list, activating the
     * current group and current team (if any)
     *
     * @param loadAllModels whether to load all the models (true) or simply the
     * list of names (false)
     * @see #refreshModelList
     * @see #refreshForNewGroup
     * @see #refreshForNewTeam
     */
    public void refreshAnalysisWindow( boolean loadAllModels ) {
        mmf.setVisible( true );
        refreshModelList( loadAllModels );
        refreshForNewGroup( getProject().currentGroup );
        refreshForNewTeam( getProject().currentGroup, getProject().currentTeam );
    }

    /**
     * Display a message in a modal dialog box.
     *
     * @param msg The string to display
     */
    public void message( String msg ) {
        // mmf.messageBox.setText( msg );
        JOptionPane.showMessageDialog( mmf, msg );
    }

    /**
     * Determines whether this team's information is complete. Checks to see if
     * first and last phases have the same number of models. Does not care about
     * the middle phase(s).
     *
     * @return true if the same number of models for first and last phases,
     * unless that number is zero.
     *
     */
    public boolean teamIsComplete( EnumMap<MPhase, MTeamPhase> teamphases ) {
        if ( teamphases.get( MPhase.Beginning ) == null || teamphases.get( MPhase.End ) == null ) {
            return false;
        }
        int numB = teamphases.get( MPhase.Beginning ).getModels( false ).size();
        int numE = teamphases.get( MPhase.End ).getModels( false ).size();

        if ( numB != numE ) {
            return false;   // if not equal then team is incomplete
        }
        if ( numB == 0 ) {
            return false;   // if equal but zero, doesn'team count.
        }
        return true;
    }

    /**
     * Creates hash maps (really as associative arrays) for all the frame
     * components and analysis elements that are replicated for each phase.
     */
    public void initializeHashMaps() {

        currentTeamModels.put( MPhase.Beginning, new ArrayList<MModel>() );
        currentTeamModels.put( MPhase.Middle, new ArrayList<MModel>() );
        currentTeamModels.put( MPhase.End, new ArrayList<MModel>() );

        initialLabel.put( MPhase.Beginning, "Beginning model(s):" );
        initialLabel.put( MPhase.Middle, "Middle model(s):" );
        initialLabel.put( MPhase.End, "End model(s):" );

        phaseColorsRelations.put( MPhase.Beginning, Tag.lightPink );
        phaseColorsRelations.put( MPhase.Middle, Tag.lightYellow );
        phaseColorsRelations.put( MPhase.End, Tag.lightGreen );

        phaseColorsConcepts.put( MPhase.Beginning, Tag.lighterPink );
        phaseColorsConcepts.put( MPhase.Middle, Tag.lighterYellow );
        phaseColorsConcepts.put( MPhase.End, Tag.lighterGreen );

        phaseDateLabel.put( MPhase.Beginning, mmf.DateB );
        phaseDateLabel.put( MPhase.Middle, mmf.DateM );
        phaseDateLabel.put( MPhase.End, mmf.DateE );

        phaseAnalysisResults.put( MPhase.Beginning, "" );
        phaseAnalysisResults.put( MPhase.Middle, "" );
        phaseAnalysisResults.put( MPhase.End, "" );

    }

    /**
     * Initializes the static Hashmap of colors needed for highlighting text in
     * the reports.
     *
     * @see #colors
     */
    public void initializeColors() {
        colors.put( MMComponentKind.CONCEPT, new Color( 240, 220, 240 ) );
        colors.put( MMComponentKind.RELATION, new Color( 200, 240, 240 ) );
    }

    /**
     * Look at all currently available (not necessarily open) graphs in files,
     * and make a list of their names. Assume that they are all part of a
     * project. If no project instance is found yet, create one.
     * Instantiates all the team phase collections.
     *
     * @param includeModels true if we need to load the models themselves for
     * analysis, false if we just need the names.
     */
    public void refreshModelList( boolean includeModels ) {
        // get folder name from hub frame
//        ArrayList<String> modelNames = new ArrayList();
        File projectFolder = getProject().MMSubmittedFolder;
        getProject().clearProject();
        // get all cgx files from the folder ---
        if ( projectFolder != null ) {
            String modelNames[] = MMFileManager.getMMATFileList( projectFolder, MFileType.MODEL );
            if ( modelNames.length == 1 && modelNames[0] == null )
                modelNames = new String[0];
            
            if ( (modelNames.length == 0 ) /*|| ( modelNames.length == 1 && modelNames[0] == null ) */ )  {
                        // If there are no names yet...
                String[] folders = projectFolder.getAbsolutePath().split( Pattern.quote( File.separator ));
                if ( folders[ folders.length - 1].equals( "Models") )
                    mmf.projListModel.addElement(  folders[ folders.length - 2 ]);
            return;
        }
//                    Global.info( "got the list of combinedModel files, now constructing object...");

            MModelID newmid = null;
            MModel newModel = null;
            MProject projectToUse = null;

            for ( String s : modelNames ) {     // for each combinedModel name, construct a combinedModel for it
                try {
                    boolean newProj = true;
                    // Create new combinedModel
                    // Need to add the actual graph to mm here!!!
                    newmid = new MModelID( s );
                    newModel = new MModel();
//                    newModel.setCgxFile( new MFile());
                    
                    // A poor design: Note that the graph is NOT attached here, see
                    // Mproject#insertModelInTeamPhase
                    newModel.setID( newmid );
                    //Global.info( "refreshModelList: Model found: " + newmid.toString() );
                    // Extract expt name, see if new, if so then create MExperiment
                    String pname = newmid.getProjectName();
                    projectToUse = MMConst.getProjectByName( pname );
                    if (projectToUse != null )
                        newProj = false;
//                    for ( MExperiment e : MMConst.expts ) {
//                        if ( e.getName().equals( ename ) ) {
//                            // Here is where we add it
//                            newProj = false;
//                            projectToUse = e;
//                            break;
//                        }
//                    }
                    if ( newProj ) {    // if we haven'team seen expt before, then add it as new.
                        projectToUse = getProject();
                        projectToUse.setName( pname );
                        MMConst.projs.add( projectToUse );
                        projNames.add( projectToUse.getName() );
                    }
                    // Add combinedModel id and graph to the correct TeamPhase in that expt
                    // TODO: find a way to avoid adding the graph for every file before even displaying the list.
                    projectToUse.insertModelInTeamPhase( newModel, includeModels );
                } catch ( MModelNameException mne ) {
                    message( mne.getMessage() );
//                    newModel = null;
                }

            } // end for each combinedModel name

        }
    }


//    /**
//     * With a more streamlined file structure (namely, exactly one file per
//     * combinedModel) this routine can be much simpler. Deletes any reports but renames
//     * synonym files.
//     *
//     * @param folder Folder containing MMAT named files
//     * @param field One of <ul> <li>"group" - indicates that the current team's
//     * group should be changed to the new group. <code>oldname</code> is the old
//     * group name; <code>newname</code> is the new group name. Uses
//     * currentTeamName for the team name. Also renames any synonym files. </li>
//     * <li> "team" - current team is renamed. <code>oldname</code> is the old
//     * team name; <code>newname</code> is the new team name. Uses currentGroup
//     * to determine the group for both. Also renames any synonym files. </li>
//     * <li>"filename" - the oldname and newname are complete filenames (minus
//     * any extension); * </li> </ul>
//     * @param oldname The old name for the group, team or combinedModel
//     * @param newname The new name for the group, team or combinedModel
//     * @return The number of files actually renamed
//     */
//    public int renameMMATFile( File folder, String field, String oldname, String newname ) {
//        // If team 
//        return 0;
//    }

    /**
     * Find the team metrics object that corresponds to the parameters. If there
     * isn't one yet, then create one.
     *
     * @param projName
     * @param groupName
     * @param teamName
     * @return the team metrics object corresponding to the parameters. If there
     * isn't already such an object, one is created and added to the overall
     * list of team metrics objects.
     */
    public MTeamMetrics getTeamMetrics( String projName, String groupName, String teamName ) {
        String key = MTeamMetrics.makeKey( projName, groupName, teamName );
        MTeamMetrics tm = teamMetrics.get( key );
        if ( tm == null ) {
            tm = new MTeamMetrics( projName, groupName, teamName );
            tm.setTeamSynonymGroup( new ClusterCollection( teamSynonymFile( getProject().getName(), groupName, teamName ) ) );
            tm.getTeamSynonymGroup().setOriginDescription( "Team synonyms for team \"" + teamName + "\"" );

            teamMetrics.put( tm.getKey(), tm );
        }
        return tm;
    }
    
    /**
     *
     * @return an MTeamMetrics object for the current expt, group and team.
     */
    public MTeamMetrics getTeamMetrics() {
        return getTeamMetrics( getProject().getName(), getProject().currentGroup, getProject().currentTeam );
    }

    /**
     * Generate a summary of all groups and teams in the current project.
     * Invokes analyzeOneTeamAllPhases on all teams. Most of the work is done by
     * the GrandTotalGenerator in the background, including writing the results
     * to a file and deciding what to do with it.
     *
     * @see GrandTotalGenerator
     * @param type one of SUMMARY or SPREADSHEET
     */
    public void generateGrandSummary( MFileType type ) {
        progressframe = new CProgressFrame( mmf );
        progressframe.setMinimum( 0 );

        progress = 0;
        progressframe.setValue( 0 );
        progressframe.setLabel( "Analyzing all teams..." );
        if ( type == MFileType.SUMMARY ) {
            progressframe.setTitle( "Generate HTML Summary" );
        } else if ( type == MFileType.SPREADSHEET ) {
            progressframe.setTitle( "Generate Excel Spreadsheet" );
        } else {
            return;     // the wrong file type
        }
        progressframe.setVisible( true );
        mmf.setEnabled( false );
        summaryFinished = false;
        GrandTotalGenerator reportTask = new GrandTotalGenerator( type );
        reportTask.addPropertyChangeListener( progressframe );
        reportTask.execute();


    }

    /**
     * Generates metrics for all teams in all groups. Runs in its own thread so
     * that a progress bar can be shown. Also writes to the appropriate file
     * after it's done.
     */
    public class GrandTotalGenerator extends SwingWorker<Void, Void> {

        MFileType typeOfReport = null;
        MFile mfile = new MFile();
        File outfile = null;

        /**
         * Create an instance of the generator.
         *
         * @param type one of <ul><li> "_SUMMARY.html" - to generate a
         * human-readable summary </li><li> "_SPREADSHEET.xls" - to generate one
         * line per team for reading by a spreadsheet program.</li></ul>
         */
        public GrandTotalGenerator( MFileType type ) {
            typeOfReport = type;
        }

        /**
         * Generates all of the team metrics objects needed to create the
         * reports. Controls the progress shown in the progress bar.
         */
        @Override
        public Void doInBackground() {
            int totalTeams = 0;
            for ( String group : getProject().getGroupList() ) {
                if ( !includeTrash && trashGroups.contains( group ) ) {
                    continue;
                }
                if ( !includeAdmin && adminGroups.contains( group ) ) {
                    continue;
                }
                totalTeams += getProject().getTeamList( group ).size();
                Global.info( "counted " + getProject().getTeamList( group ).size() + " teams in group " + group );
            }

            progressframe.progressBar.setMaximum( totalTeams );

//                    refreshAnalysisWindow( true );
            totalTeamPhases = getProject().teamPhases.size();
            updateScreenWhileAnalyzing = false;
            teamMetrics.clear();

            // for every group, first make sure it's not one to ignure
            for ( String group : getProject().getGroupList() ) {
                if ( !includeTrash && trashGroups.contains( group ) ) {
                    continue;
                }
                if ( !includeAdmin && adminGroups.contains( group ) ) {
                    continue;
                }
                getProject().addGroupSynonyms( group );
                currentGroupSynonymGroup = getProject().groupSynonyms.get( group );

                // for every team
                for ( String team : getProject().getTeamList( group ) ) {
                    currentTeamSynonymGroup = new ClusterCollection( teamSynonymFile( getProject().getName(), group, team ) );
                    currentTeamSynonymGroup.setOriginDescription( "Team synonyms for team \"" + team + "\"" );
                    analyzeOneTeamAllPhases( group, team );
//                            Global.info( "Collecting metrics for group " + group + " - team " + team );
                    firePropertyChange( "progress", progress++, progress ); // count each team once for progress
                    firePropertyChange( "label", null,
                            "Collecting metrics: group \"" + group + "\" - team \"" + team + "\"" );
//                    //                 + " (" + progress + " of " + totalTeams + ")" );
//
//                    progressframe.progressBar.setValue( progress );
                }
            }
            return null;
        }

        /**
         * Writes the appropriate report to its file. If it's the summary HTML
         * report, then save it, then open it. If it's the spreadsheet, just
         * save it. Refresh the analysis window when completed.
         */
        @Override
        protected void done() {
            mmf.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            mmf.setEnabled( true );
            summaryFinished = true;
            Global.info( "GrandTotalGenerator: Task done" );

            mfile.setProject( getProject().getName() );
            mfile.setType( typeOfReport );
            if ( MMConst.useNewFolderStructure) {
                 outfile = new File( mfile.mfileAbsoluteFolder( getProject().MMSubmittedFolder ), mfile.makeFilename() );

            } else {
                 outfile = new File( getProject().MMSubmittedFolder, mfile.makeFilename() );
            }
            if ( typeOfReport == MFileType.SUMMARY ) {
                charger.util.Util.writeToFile( outfile, reporter.getTabularSummary(), false );
                try {
                    if ( outfile != null ) {
                        Desktop.getDesktop().open( outfile );
                    }
                } catch ( IOException e ) {
                    message( "Problem opening tabular summary file.\n" + e.getMessage() );
                }
            } else if ( typeOfReport == MFileType.SPREADSHEET ) {
                charger.util.Util.writeToFile( outfile, reporter.getSpreadsheet( "\t" ), false );
                JOptionPane.showMessageDialog( null, "Spreadsheet summary saved to:\n " + outfile.getAbsolutePath() );

            }
            updateScreenWhileAnalyzing = true;
            refreshAnalysisWindow( false );
        }
    }

    /**
     * Save the current group/team combination, generate the summary, write to a
     * pre-determined file, and then restore the current combination.
     */
    public void makeSpreadsheetAction() {

        String group = getProject().currentGroup;
        String team = getProject().currentTeam;

        generateGrandSummary( MFileType.SPREADSHEET );
    }

    /**
     * Save the current group/team combination, generate the tabular summary,
     * write to a pre-determined file, and then restore the current combination.
     *
     */
//    public File makeTabularSummaryAction() {
    public void makeTabularSummaryAction() {

        String group = getProject().currentGroup;
        String team = getProject().currentTeam;



        generateGrandSummary( MFileType.SUMMARY );
    }

    public void makeGroupAsTeamReport( String group ) {
        progressframe = new CProgressFrame( mmf );
        progressframe.setVisible( true );
        progressframe.setTitle( "Preparing group-as-team report for group \"" + group + "\"." );
        progressframe.setValue( 0 );
        progressframe.setMaximum( 100 );
        progressframe.setLabel( "Complete Group Report on all models in group: \"" + group + "\"."
                + " Please wait..." );
        mmf.setEnabled( false );


        GroupAsTeamReportWorker task = new GroupAsTeamReportWorker( group, progressframe );
        task.addPropertyChangeListener( progressframe );

        task.execute();
    }

    /**
     * Used for generating the group-as-team report, in the background.
     */
    public class GroupAsTeamReportWorker extends SwingWorker<Void, Void> {

        String group = null;
        ArrayList<String> teamlist = null;
        int totalModelsProcessed = 0;
        MFile mfile = new MFile();
        File htmlFile = null;
        ArrayList<MModel> models = new ArrayList<>();
        ArrayList<ClusterCollection> syns = new ArrayList<>();

        public GroupAsTeamReportWorker( final String group, CProgressFrame progressframe ) {
            this.group = group;


            mfile.setType( MFileType.GROUPREPORT );
            mfile.setProject( getProject().getName() );
            mfile.setGroup( group );
            if ( MMConst.useNewFolderStructure ) {
                htmlFile = new File( mfile.mfileAbsoluteFolder( getProject().MMSubmittedFolder ), mfile.makeFilename() );

            } else {
                htmlFile = new File( getProject().MMSubmittedFolder, mfile.makeFilename() );
            }

            reporter.writeToHTMLFile( htmlFile,
                    Tag.p( "Complete Group Report on all models in group: \"" + group + "\"" ),
                    false );

            if ( mmf.useSynonyms.isSelected() ) {
                syns.add( getProject().groupSynonyms.get( group ) );
            }

            for ( ClusterCollection sgroup : syns ) {
//                reporter.writeToHTMLFile( htmlFile, Tag.p( sgroup.explainYourself( 9999, true ) ), true );
                sgroup.formatAsHTML( mmf.showSynonyms.isSelected() );
            }

        }

        @Override
        public Void doInBackground() {
            int progress = 0;
            int progressIncrement = 1 + 100 / ( MPhase.values().length * 2 );
            teamlist = getProject().getTeamList( group );
            // First, find all the models
            try {
                for ( MPhase phase : MPhase.values() ) {
                    models.clear();
                    MTeamPhase tp = null;
                    for ( String team : teamlist ) {
                        tp = getProject().getTeamPhaseByNames( group, team, phase );
                        ArrayList<MModel> teamPhaseModels = tp.getModels( false );
                        for ( MModel model : teamPhaseModels ) {
//                            if ( !combinedModel.isSpecialModel() ) {
                            if ( !( mmf.observerIsSpecial.isSelected() && model.isSpecialModel() ) ) {
                                models.add( model );
//                                        Here is a good place to edit each model so that the team name is part of each member's name
//                                      Should be done before the analysis.
                            }
                        }
                    }

                    // Next run the analysis
                    for ( MMComponentKind kind : MMComponentKind.values() ) {
                        firePropertyChange( "label", null, "Analyzing " + models.size()
                                + " models for "
                                + kind.toString() + " congruence in the " + phase.toString() + " phase." );
                        progress += progressIncrement;
                        firePropertyChange( "progress", null, progress );
                        MModelID mid = tp.getID();
                        mid.setTeam( group );
                        tp.setID( mid );
                        reporter.writeToHTMLFile( htmlFile,
                                analyzeMultipleModels( tp, kind, models, syns ),
                                true );
                    }
                    totalModelsProcessed += models.size();
                }
            } catch ( Exception e ) {
                Global.info( "Exception raised by doInBackground: " + e.getMessage() );
                e.printStackTrace();
            }
            firePropertyChange( "progress", progress, progress );
            return null;
        }

        @Override
        public void done() {
            mmf.setEnabled( true );
            JOptionPane.showMessageDialog( null, "Report on set of all "
                    + totalModelsProcessed + " models in group \"" + group + "\" saved to:\n " + htmlFile.getAbsolutePath() );
        }
    }

    /**
     * Performs every requested analysis on the models of every phase of the
     * current team.
     *
     */
    public void analyzeOneTeamAllPhases( String group, String team ) {
        if ( group == null || team == null ) {
            return;
        }
        Global.info( "Start analyzeOneTeamAllPhases: group: \"" + group + "\": team: \"" + team + "\"" );
        currentTeamSynonymGroup = new ClusterCollection( teamSynonymFile( getProject().getName(), group, team ) );
        for ( MPhase p : MPhase.values() ) {
            if ( updateScreenWhileAnalyzing ) {
                mmf.phaseAnalysisResultsDisplay.get( p ).setText( "Analyzing..." );
                mmf.adjustSynonymAppearance();
            }
            analyzeOnePhase( group, team, p );
        }
        Global.info( "Finish analyzeOneTeamAllPhases: group: \"" + group + "\": team: \"" + team + "\"" );

    }

    /**
     * Completely manages the analysis of a single phase of a single team.
     * Assumes that currentTeamModels is already set.
     *
     * @param phase Tells which phase on which to perform the analysis.
     * @see #analyzeMultipleModels
     */
    public void analyzeOnePhase( String group, String team, MPhase phase ) {

        MTeamPhase tp = getProject().getTeamPhaseByNames( group, team, phase );

        String theResults = "";

        ArrayList<ClusterCollection> clusterCollections = new ArrayList<>();

        // Set up the synonymClusters to use for this analysis
        if ( mmf.useSynonyms.isSelected() ) {
            clusterCollections.add( currentTeamSynonymGroup );
            if ( mmf.useGroupSynonyms.isSelected() ) {
                // merge the given synonymClusters with the group - not yet tested
                clusterCollections.add( currentGroupSynonymGroup );
            }
//                for ( ClusterCollection sgroup : synonymsToUse ) {
//                    theResults = theResults + sgroup.explainYourself( 0, true );
//                }
        }

        theResults += Tag.h( 3, "Team Congruence " + phase.toString() );
        
//        boolean includeSpecial = !mmf.observerIsSpecial.isSelected();

        String conceptResults = 
                analyzeMultipleModels( tp, MMComponentKind.CONCEPT, tp.getModels( true ), clusterCollections );

        String relationResults = 
                analyzeMultipleModels( tp, MMComponentKind.RELATION, tp.getModels( true ), clusterCollections );

        theResults += Tag.colorDiv( phaseColorsConcepts.get( phase ), conceptResults )
                + Tag.colorDiv( phaseColorsRelations.get( phase ), relationResults )
                + Tag.hr;

        phaseAnalysisResults.put( phase,
                Tag.comment( "Generated by Charger's MMAnalysis on "
                + timestampFormat.format( new Date() ) )
                + Tag.body + theResults );
        // Here might be a good place to do combined model processing?

        if ( updateScreenWhileAnalyzing ) {
            mmf.phaseAnalysisResultsDisplay.get( phase ).setText( phaseAnalysisResults.get( phase ) );
            mmf.phaseAnalysisResultsDisplay.get( phase ).setCaretPosition( 0 );
        }
    }

    /**
     * Performs the basic analysis of two or more models. Will eventually call
     * both comparison to an observer combinedModel (should be already coded) and
     * congruence metric. If there is an observer combinedModel (i.e., there's a user
     * name beginning with "observer" in it, this method identifies it and
     * handles it appropriately depending on the metric being used.
     *
     * @param tp The team phase being analyzed. If not for a team phase, then
     * should be null
     * @param kind What kind of analysis is this?
     * @param models A list of the models, may include an "observer" or
     * "combined" combinedModel. In most cases, this value may be identical to
     * tp.getModels() but the combinedModel list is there to allow for arbitrary combinedModel
     * sets if desired.
     * @param synonyms The synonymClusters to use for this analysis procedure.
     * @return the HTML tagged results of the analysis.
     */
    public String analyzeMultipleModels( MTeamPhase tp, MMComponentKind kind, ArrayList<MModel> models, ArrayList<ClusterCollection> synonyms ) {
        if ( models.isEmpty() ) {
            return Tag.p( "No models." );
        }
                // IDENTIFY MODELS FOR THIS OPERATION
        String group;
        String team;
        String phase;
        if ( tp != null ) {
             group = tp.getID().getGroup();
             team = tp.getID().getTeam();  
             phase = tp.getID().getPhase().toString();
        } else {
            group = tp.getModels( true ).get( 0 ).getID().getGroup();
            team = tp.getModels( true ).get( 0 ).getID().getTeam();
            phase = tp.getModels( true ).get( 0 ).getID().getPhase().name();
    }

        String returnValue = "";

        ArrayList<MModel> modelsForMetrics = new ArrayList<>();
        MModel observerModel = null;
        MModel combinedModel = null;

        // Filter out any ignored models, such as observer  or combined model
        for ( MModel m : models ) {
            if ( m.isObserverModel() ) {
                if ( mmf.observerIsSpecial.isSelected() ) {
                    observerModel = m;
                    continue;
                }
            }
            if ( m.isCombinedModel() ) {
                if ( mmf.combinedIsSpecial.isSelected() ) {
                    combinedModel = m;
                    continue;
                }
            }
            modelsForMetrics.add( m );
        }

            // IDENTIFY TEAM METRICS OBJECT THAT WILL HOLD THE RESULTS
        MTeamMetrics thisTeamMetrics = getTeamMetrics( getProject().getName(), group, team );

            // GATHER ANY PHASE-WIDE METRICS
        // Only show the combinedModel metrics once, at concept analysis time.
        if ( kind == MMComponentKind.CONCEPT && mmf.graphMetricsEnabled.isSelected() ) {
            returnValue += Tag.h( 3, "Individual Graph Metrics" );
            for ( MModel m : modelsForMetrics ) {
                if ( m.getGraph() == null ) {
                    m.insertGraphIntoModel();
                }

                charger.GraphMetrics gmetrics = new charger.GraphMetrics( m.getGraph() );
                returnValue += Tag.colorDiv( "#EEEEEE",
                        Tag.p( Tag.bold( m.getID().getFilename().toUpperCase() ) ) + gmetrics.getGraphMetrics( true ) );
                //getTeamMetrics().addGraphMetrics( MPhase.valueOf( phase ), gmetrics );
            }
        }

        //  Here is where we scan all the models (not just analyzed ones) to see if there's an observer, team combinedModel
        //  Any other uses for scanning all models can be done here.
        for ( MModel m : models ) {
            if ( m.isTeamModel() ) {
                thisTeamMetrics.setHasTeamModel( MPhase.valueOf( phase ), true);
            }
            if ( m.isObserverModel() ) {
                thisTeamMetrics.setHasObserverModel( MPhase.valueOf( phase ), true);
            }
            if ( m.isCombinedModel() ) {
                thisTeamMetrics.setHasCombinedModel( MPhase.valueOf( phase ), true);
            }
        }

            // CREATE A CONGRUENCES INSTANCE, SET IT UP, THEN GENERATE METRICS FOR THIS TEAM PHASE
        MCongruenceMetrics congruences = new MCongruenceMetrics( modelsForMetrics,
                "Team " + team.toUpperCase() + " - " + phase + " - " + kind + " congruence" );
        // TODO: store this congruence metrics objects in the current team phase object.
        if ( tp != null ) {
            tp.congruenceMetrics.put( kind, congruences );
                Global.info( "\n\nCreating congruenceMetrics for " + congruences.getCaption() );
        }
        congruences.setUseSynonyms( mmf.useSynonyms.isSelected() );

        congruences.setSynonymCollections( synonyms );
        congruences.setMatchReferents( true );
        congruences.setShowAllCorefSets( mmf.showCorefsTotal.isSelected() );
        congruences.setShowLevelCorefSets( mmf.showCorefsPerLevel.isSelected() );
        congruences.setIgnoreRelationDirection( mmf.ignoreRelationSense.isSelected());

        congruences.generateMetrics();

        if (!  mmf.combinedVsObserver.isSelected() ) {        // TODO: should figure out how to just generate the combined totals
            returnValue += congruences.getResults( kind );
        }


                // LOAD UP TEAM METRICS INSTANCE WITH SOME RESULTS
        thisTeamMetrics.setCreationDate( MPhase.valueOf( phase ), tp.getDate() );

        // BUG: without arguments, getTeamMetrics uses current team and group. Should use parameters.
        thisTeamMetrics.setUseSynonyms( mmf.useSynonyms.isSelected() );
//        thisTeamMetrics.setHasSynonyms( MPhase.valueOf( phase ), !( ( synonyms == null ) || synonyms.isEmpty() ) );

        thisTeamMetrics.setDistinct( kind, MPhase.valueOf( phase ), congruences.getNShared( kind, 1 ) );
        thisTeamMetrics.setSingletonStdDev( kind, MPhase.valueOf( phase ), false,
                congruences.getSingletonStdDev( kind, false ) );
        thisTeamMetrics.setSingletonStdDev( kind, MPhase.valueOf( phase ), true,
                congruences.getSingletonStdDev( kind, true ) );
        for ( int level = 0; level <= MTeamMetrics.MAX_TEAM_SIZE; level++ ) {
            thisTeamMetrics.setCongruenceValue(
                    kind, MPhase.valueOf( phase ), level, congruences.getCongruence( kind, level ) );
        }

        if ( modelsForMetrics.size() >= 2
                && ( mmf.pairwiseEnabled.isSelected() || mmf.observerPairEnabled.isSelected() ) ) {
            returnValue += analyzeModelsByPairs( kind, MPhase.valueOf( phase ), modelsForMetrics, synonyms );
        }

        if ( mmf.combinedVsObserver.isSelected() ) {        // TODO: should figure out how to just generate the combined totals
            returnValue += analyzeCombinedVsObserver( tp, kind, MPhase.valueOf( phase ), synonyms );
            thisTeamMetrics.setObserverAnalysis( MPhase.valueOf( phase ), returnValue );
        }

        return returnValue;
    }
    
//    /**
//     * Combine all regular user models into a combined combinedModel, and create metrics for them.
//     * Essentially creates a combined combinedModel, then performs pair-wise comparison with the observer combinedModel.
//     * @param teamMetrics Where to put the results of the analysis
//     * @param kind
//     * @param phase
//     * @param models
//     * @param synonyms 
//     */
//    public String performObserverCoverage( MTeamPhase tp, MMComponentKind kind, MPhase phase, ArrayList<MModel> models, ArrayList<SynonymGroup> synonyms ) {
////            teamMetrics.setObserverCoverage( phase, " MMAnalysisMgr::analyzeMultipleModels setting observer metrics ");
//        MModel observerModel = tp.getObserverModel();
//        if ( tp == null ) {
//            return "";
//        }
//        if ( observerModel == null ) {
//            return "";
//        }
//
//        MModel combinedModel = tp.getCombinedModel();
//        // Here do a pairwise analysis between combinedModel and the observer model
//        String returnValue = "";
//        ArrayList<MModel> newModels = new ArrayList<>();
//        newModels.add( combinedModel );
//        newModels.add( observerModel );
//            returnValue +=   analyzeModelsByPairs( kind,  tp.id.getPhase(), newModels,  synonyms );
//        
//         return returnValue;
//    }

    /**
     * Performs various congruence and comparison tests for every pair in a set
     * of models. For each pair, performs the same metrics on a set of two
     * models; e.g., will derive a congruence level 2 only for the set only. if
     * the analysis window's observerPairsEnabled is set, then only compare
     * models where one of them is the observer combinedModel.
     * @param kind Whether concepts ore relations are being analyzed.
     * @param phase Which phase to put the results in a team metrics instance.
     * @param models The models to be analyzed. Uses MMAnalysisFrame settings to decide what to do with special models.
     * @param clusterCollections A set of synonyms to be used for the comparisons. Usually will have a group synonym list and possibly a team one.
     */
    public String analyzeModelsByPairs( MMComponentKind kind, MPhase phase, ArrayList<MModel> models, ArrayList<ClusterCollection> clusterCollections ) {
        //return htmlStrings.paraStart + "Pairwise analysis starts here." + htmlStrings.paraEnd;
        ArrayList<MModel> pair = new ArrayList<>( 2 );

        // Here is where we should initialize buckets for keeping pairwise statistics.
        float sumCongruence = 0;

        String returnValue = "";
        int numPairs = 0;
        for ( int m1 = 0; m1 < models.size(); m1++ ) {
            for ( int m2 = 0; m2 < m1; m2++ ) {
                if ( m2 != m1 ) {
                    if ( mmf.observerPairEnabled.isSelected()
                            && !( models.get( m1 ).isObserverModel() || models.get( m2 ).isObserverModel() ) ) {
                        continue;
                    }
                    // perform a single pair's analysis here.
                    numPairs++;
                    pair.clear();
                    pair.add( models.get( m2 ) );
                    pair.add( models.get( m1 ) );

                    // perform congruence on the pair array
                    MCongruenceMetrics congruences = new MCongruenceMetrics( pair,
                            "Team " + pair.get( 0 ).getID().getTeam()
                            + " at " + pair.get( 0 ).getID().getPhase().toString()
                            + "\npairwise " + kind
                            + " congruence" );

                    congruences.setUseSynonyms( mmf.useSynonyms.isSelected() );
//                    ArrayList<SynonymGroup> synonymsToUse = new ArrayList<>();
//                    if ( mmf.useSynonyms.isSelected() ) {
//                        synonymsToUse.add( currentTeamSynonymGroup );
//                        if ( mmf.useGroupSynonyms.isSelected() ) {
//                            synonymsToUse.add( currentGroupSynonymGroup );
//                        }
//                    }

                    congruences.setSynonymCollections( clusterCollections );
                    congruences.setMatchReferents( true );
                    congruences.setShowAllCorefSets( mmf.showCorefsTotal.isSelected() );
                    congruences.setShowLevelCorefSets( mmf.showCorefsPerLevel.isSelected() );

                    congruences.generateMetrics();
                    sumCongruence += congruences.getCongruence( kind, 2 );  // pairwise only has level 2 congruence

                    returnValue += congruences.getResults( kind );
                }
            }
        }
        getTeamMetrics().setAvgPairCongruenceValue( kind, phase, sumCongruence / numPairs );
        returnValue += Tag.p( "Avg PAIR " + kind.toString() + " congruence = "
                + MMetrics.nformat.format( sumCongruence ) + " / " + numPairs + " = "
                + MMetrics.nformat.format( sumCongruence / numPairs ) );

        return returnValue;
    }

    /**
     * Sets up the boilerplate needed for the analysis frame. Assumes that the
     * graph folder and project are already set. Should only be called once
     * at construction time, unless there is some need to restore all checkboxes
     * to their defaults.
     *
     * @see #refreshAnalysisWindow
     */
    public void initializeTopPanels() {
        for ( String s : projNames ) {
            mmf.projListModel.addElement( s );
        }


        mmf.groupListModel.clear();
        for ( String s : getProject().getGroupList() ) {
            mmf.groupListModel.addElement( s );
        }
        
        if ( !mmf.groupListModel.isEmpty() ) {      // should only happen when there are  no models 
            //mmf.groupListLabel.setText( mmf.groupListModel.size() + " Group(s):");
            mmf.groupList.setSelectedIndex( 0 );
            getProject().currentGroup = (String)mmf.groupListModel.getElementAt( 0 );
        }

                // Set up options to a set of defaults
        mmf.observerIsSpecial.setSelected( true );
        mmf.useSynonyms.setSelected( true );
        mmf.useGroupSynonyms.setSelected( true );

        mmf.showCorefsTotal.setSelected( false );
        mmf.showCorefsPerLevel.setSelected( false );
        mmf.pairwiseEnabled.setEnabled( true );
        mmf.pairwiseEnabled.setSelected( false );
        mmf.graphMetricsEnabled.setEnabled( true );
        mmf.graphMetricsEnabled.setSelected( false );        // not sure what's a good default here
        mmf.enableRawHTML.setSelected( false );
        mmf.includeTrash.setSelected( false );
        mmf.includeAdmin.setSelected( true );

        // Add all the listeners for the various lists:
        mmf.groupList.addListSelectionListener( new GroupSelectionListener() );
        mmf.teamList.addListSelectionListener( new TeamSelectionListener() );


        for ( MPhase p : MPhase.values() ) {
            mmf.teamPhaseModelList.get( p ).addMouseListener( modelListMouseListener );
            mmf.teamPhaseModelList.get( p ).addListSelectionListener( new ModelSelectionListener() );
            mmf.phaseLabel.get( p ).setText( initialLabel.get( p ) );
            mmf.phaseAnalysisResultsDisplay.get( p ).setContentType( "text/html" );
            phaseDateLabel.get( p ).setText( "" );
        }

        mmf.menuItemSaveTeamReport.setEnabled( false );
        mmf.menuViewTeamReport.setEnabled( false );
        
        mmf.groupList.setSelectedIndex( 0 );


//        refreshForNewGroup( currentGroup );      // TODO: Avoid having this clobber the project synonym list
    }

    /**
     * When new group is determined, this re-initializes all the lists that are
     * "downstream" of the group selected. Blanks out all the team phase details
     * and then shows the team list for that group.
     *
     * @param group
     */
    public void refreshForNewGroup( String group ) {
        // blank out the lists on the interface
        if ( group == null ) {
            return;
        }

        getProject().currentGroup = group;
        mmf.teamListModel.removeAllElements();


        // clear out all the team information -- assume that no team is selected
        for ( MPhase p : MPhase.values() ) {
            mmf.phaseLabel.get( p ).setText( initialLabel.get( p ) );
            mmf.teamPhaseList_ListModel.get( p ).removeAllElements();
            currentTeamModels.get( p ).clear();
            mmf.phaseAnalysisResultsDisplay.get( p ).setText( "" );
            mmf.phaseAnalysisResultsDisplay.get( p ).setBackground( Color.white );
            phaseDateLabel.get( p ).setText( "" );
        }

        getProject().currentTeam = null;
        mmf.teamComplete.setText( "" );
        mmf.teamComplete.setBackground( Color.white );


        // will also need to remove all from phase combinedModel and synonym list
        mmf.teamListModel.removeAllElements();
        ArrayList<String> teams = getProject().getTeamList( getProject().currentGroup );
        for ( String s : teams ) {
            mmf.teamListModel.addElement( s );
        }

        getProject().addGroupSynonyms( group );
        currentGroupSynonymGroup = getProject().groupSynonyms.get( group );
        mmf.groupSynonymList.setText( currentGroupSynonymGroup.toString() );
        mmf.groupSynonymList.setCaretPosition( 0 );
        mmf.teamSynonymList.setText( "" );

        mmf.groupList.setSelectedValue( group, true );

        ( (javax.swing.border.TitledBorder)mmf.teamList.getBorder() ).setTitle( " " + mmf.teamListModel.size() + " Team(s):" );
        mmf.groupName.setText( group );
        mmf.teamName.setText( "-- none --" );

        mmf.adjustSynonymAppearance();

        mmf.menuItemSaveTeamReport.setEnabled( false );
        mmf.menuViewTeamReport.setEnabled( false );

    }

    /**
     * A wrapper to manage the logic for setting up a single team phase. There
     * are currently three team phases per screen. Putting the logic here means
     * that (a) it's all in one place and (b) adding/changing/deleting phases
     * should be fairly easy in the future.
     *
     * @param phase Which phase we should be working on -- needed in case
     * there's no MTeamPhase but we need to clear everything out anyway.
     * @param tp *
     */
    public void initializeOneTeamPhaseDisplay( MPhase phase, MTeamPhase tp ) {
        // set up all the beginning phase fields
        // blank out all fields and start them up again
        // Note this has no effect on the underlying data structure.
        currentTeamModels.get( phase ).clear();
        mmf.teamPhaseList_ListModel.get( phase ).removeAllElements();
        if ( tp != null ) {
//            MModelID currentMID = tp.getID();
            for ( MModel model : tp.getModels( true ) ) {
                mmf.teamPhaseList_ListModel.get( phase ).addElement( model.getID().getName() );
                currentTeamModels.get( phase ).add( model );
            }
            if ( tp.getDate() > 0 ) {
                phaseDateLabel.get( phase ).setText( MDYdateFormat.format( tp.getDate() ) );
            } else {
                phaseDateLabel.get( phase ).setText( "" );
            }
        }
        mmf.teamPhaseModelList.get( phase ).setCellRenderer( new ModelListCellRenderer() );
        mmf.phaseLabel.get( phase ).setText( currentTeamModels.get( phase ).size() + " " + initialLabel.get( phase ) );
    }

    /**
     * Updates all the team phase information to reflect one new team. Also sets
     * up the team HTML file for writing. Then invokes the analysis.
     *
     * @param group The group for this team
     * @param team The team to use
     * @see #analyzeOneTeamAllPhases
     */
    public void refreshForNewTeam( String group, String team ) {

        if ( team == null || team.equals( "" ) ) {
            return;
        }
                // Somehow the refresh of the model list didn't get here.
        

        for ( MPhase p : MPhase.values() ) {
            mmf.phaseAnalysisResultsDisplay.get( p ).setText( "Analyzing ..." );
            mmf.teamPhaseList_ListModel.get( p ).removeAllElements();
        }

        AnalyzeOneTeam_SwingWorker worker = new AnalyzeOneTeam_SwingWorker( group, team );
        worker.execute();
        try {
            worker.get();
        } catch ( InterruptedException ex ) {
            Global.info( "AnalyzeOneTeam(worker) interrupted: " + ex.getMessage() );
        } catch ( ExecutionException ex ) {
            Global.info( "AnalyzeOneTeam(worker) execution error: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }

    /**
     * Performs the analysis of one team (all phases) in the background
     */
    public class AnalyzeOneTeam_SwingWorker extends SwingWorker<Void, Void> {

        String team;
        String group;
        EnumMap<MPhase, MTeamPhase> thisTeamPhase;
        /**
         *
         * @param g The group in which the desired team belongs.
         * @param t The name of the team being analyzed.
         */
        public AnalyzeOneTeam_SwingWorker( String g, String t ) {
            this.team = t;
            this.group = g;
        }

        @Override
        public Void doInBackground() {
            Global.info( "Start analysis for team " + team );
            // blank out the lists on the interface

            mmf.teamName.setText( team );
            getProject().currentTeam = team;
            getProject().currentTeamMetrics = new MTeamMetrics( getProject().getName(), group, team );

            currentTeamSynonymGroup = new ClusterCollection( teamSynonymFile( getProject().getName(), group, team ) );
            currentTeamSynonymGroup.setOriginDescription( "Team synonyms for team \"" + team + "\"" );
            getProject().currentTeamMetrics.setTeamSynonymGroup( currentTeamSynonymGroup );

            mmf.teamSynonymList.setText( currentTeamSynonymGroup.toString() );
            mmf.teamSynonymList.setCaretPosition( 0 );

            thisTeamPhase = new EnumMap<>( MPhase.class );
            for ( MPhase p : MPhase.values() ) {
                MTeamPhase tp = getProject().getTeamPhaseByNames( group, team, p );
                thisTeamPhase.put( p, tp );
                initializeOneTeamPhaseDisplay( p, tp );
            }

            if ( updateScreenWhileAnalyzing ) {

                if ( teamIsComplete( thisTeamPhase ) ) {
                    mmf.teamComplete.setText( " COMPLETE!" );
                    mmf.teamComplete.setBackground( new Color( 0, 160, 0 ) );
                    mmf.teamComplete.setForeground( Color.white );
                } else {
                    mmf.teamComplete.setText( " Not complete" );
                    mmf.teamComplete.setBackground( new Color( 192, 0, 0 ) );
                    mmf.teamComplete.setForeground( Color.white );
                }
            }

            mmf.menuItemSaveTeamReport.setEnabled( true );
            mmf.menuViewTeamReport.setEnabled( true );

            analyzeOneTeamAllPhases( group, team );
//                        Global.info( "Finished analyzing team " + team );
            return null;
        }

        @Override
        protected void done() {
            currentTeamPhase = thisTeamPhase;
//            Global.info( "Done with thread analyzing team " + team );
        }
    }

    public void saveAllTeamsReports( final String group ) {

        progressframe.setMinimum( 0 );
        progressframe.setMaximum( mmf.teamList.getModel().getSize() );
        progressframe.setValue( 0 );
        progressframe.setTitle( "Save All Team Reports For Group \"" + group + "\"" );

        class SaveAllReportsWorker extends SwingWorker<Void, Void> {

            JFrame owner = null;
            int numReports = 0;

            SaveAllReportsWorker( JFrame f ) {
                owner = f;
                updateScreenWhileAnalyzing = false;
            }

            @Override
            protected Void doInBackground() {
                for ( int elem = 0; elem < mmf.teamList.getModel().getSize(); elem++ ) {
                    getProject().currentTeam = (String)mmf.teamList.getModel().getElementAt( elem );
                    firePropertyChange( "label", null, "Saving report for team: \"" + getProject().currentTeam + "\"" );
                    analyzeOneTeamAllPhases( group, getProject().currentTeam );
                    File resultsfile = reporter.writeTeamResults();
                    if ( resultsfile == null ) {
                        continue;    // sometimes there's no analysis to report (e.g., no team selected, etc.)
                    }
                    Global.info( "Report saved to: " + resultsfile.getAbsolutePath() );
                    firePropertyChange( "progress", numReports++, numReports );
                }
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog( owner,
                        "Saved " + numReports + " team reports for group \"" + group + "\"" );
            }
        }

        if ( mmf.groupList.isSelectionEmpty() ) {
            // no current group
            return;
        }

        SaveAllReportsWorker worker = new SaveAllReportsWorker( mmf );
        worker.addPropertyChangeListener( progressframe );
        worker.execute();
    }

    
    /** Compare the observer graph with the combined (joined) graphs of all the team members.
     * Gets the models from the teamphase and uses the listed
     * synonyms. It's up to the caller to keep track of any phase, kind, etc.
     * The combined graph will be created on the fly for this purpose if needed;
     * it does not have to exist already.
     * @return English description of the results. If there's not an observer model, then return empty string.
     *
     *
     */
    public String analyzeCombinedVsObserver( MTeamPhase tp, MMComponentKind kind, MPhase phase, ArrayList<ClusterCollection> synonyms ) {
        MModel observerModel = tp.getObserverModel();
        if ( tp == null ) {
            return "";
        }
        if ( observerModel == null ) {
            return "";
        }

        MModel combinedModel = tp.getCombinedModel();
        if ( combinedModel == null ) {
            return "No combined model was available.";
        }

        // Here do a pairwise analysis between combinedModel and the observer model
        String returnValue = "";
        ArrayList<MModel> models = new ArrayList<>();
        models.add( combinedModel );
        models.add( observerModel );
//        for ( MMComponentKind kind : MMComponentKind.values() ) {
        returnValue += Tag.h( 3, "OBSERVER vs. JOINED GRAPH for team " + tp.id.getTeam() + " using " + kind.name() + "S for " + tp.id.getPhase().name() );
        returnValue += analyzeModelsByPairs( kind, tp.id.getPhase(), models, synonyms );
//        }

        return returnValue;
    }


    /**
     * Makes a file object that corresponds to the appropriate team synonym
     * file. Note the file may not exist.
     *
     * @param proj Project name
     * @param group Group in which this team resides
     * @param teamname Team name whose synonymClusters are to be stored in the file.
     * @return A file object, but without any indication of whether the file
     * exists or is readable.
     */
    public File teamSynonymFile( String proj, String group, String teamname ) {
        String filename = proj + "_" + group + "_" + teamname + "_"
                + ClusterCollection.synonymUser + "." + ClusterCollection.synonymExtension;

        if ( MMConst.useNewFolderStructure ) {
            return new File( new MFile( filename ).mfileAbsoluteFolder( getProject().MMSubmittedFolder ), filename );
        } else {
            return new File( getProject().MMSubmittedFolder, filename );

        }
    }

    /**
     * Makes a file object that corresponds to the appropriate group synonym
     * file. Note the file may not exist.
     *
     * @param proj the project for this group
     * @param group the group whose synonyms are being returned.
     * @return A file object, but without any indication of whether the file
     * exists or is readable.
     */
    public File groupSynonymFile( MProject proj, String group ) {
        String filename = proj.getName() + "_" + group + "_"
                + ClusterCollection.synonymUser + "." + ClusterCollection.synonymExtension;
        if ( MMConst.useNewFolderStructure ) {
            File folder = new MFile( filename ).mfileAbsoluteFolder( getProject().MMSubmittedFolder );
            return new File( folder, filename );
        } else {
            return new File( getProject().MMSubmittedFolder, filename );

        }
    }
        

    /**
     * Load the team's synonymClusters (if any) into the synonym editor and start up
 the editor's window. Set the window listener so that when the editor is
     * closed, we can refresh what's needed here.
     *
     * @see #updateGroupSynonymList(boolean)
     */
    public void editGroupSynonyms() {
        if ( getProject().currentGroup == null || getProject().currentGroup.equals( "" ) ) {
            JOptionPane.showMessageDialog( mmf, "Sorry, there is no current group." );
            return;
        }
                Global.info( "current proj is " + getProject().getName() + ", current group is " + getProject().currentGroup);
        File groupFile = groupSynonymFile( getProject(), getProject().currentGroup );
//            Global.info( "edit group synonymClusters file " + groupFile.getName() );
        synonymEditor = new SynonymEditor( getProject().currentGroup + " group" );
        synonymEditor.loadSynonyms( groupFile );
        synonymEditor.addWindowListener( new WindowAdapter() {
            public void windowClosed( WindowEvent e ) {
                updateGroupSynonymList( false );
            }
        } );

    }

    /**
     * Load the group's synonymClusters (if any) into the synonym editor and start
 up the editor's window. Set the window listener so that when the editor
     * is closed, we can refresh what's needed here.
     *
     * @see #updateTeamSynonymList(boolean)
     */
    public void editTeamSynonyms() {
        if ( getProject().currentTeam == null || getProject().currentTeam.equals( "" ) ) {
            JOptionPane.showMessageDialog( mmf, "Sorry, there is no current team." );
            return;
        }
        synonymEditor = new SynonymEditor( "Team " + getProject().currentTeam );
        synonymEditor.loadSynonyms( teamSynonymFile( getProject().getName(), getProject().currentGroup, getProject().currentTeam ) );
        synonymEditor.addWindowListener( new WindowAdapter() {
            public void windowClosed( WindowEvent e ) {
                updateTeamSynonymList( false );
            }
        } );

    }

    /**
     * Called whenever the team synonym list loses focus -- i.e., the user may
     * have changed it. Saves whatever synonymClusters are in the (possibly changed)
 list and then re-analyzes the team.
     *
     * @param saveFirst if true, then save the list from the text pane; if
     * false, just read in from the appropriate file.
     *
     */
    public void updateTeamSynonymList( boolean saveFirst ) {
//            Global.info( "changing synonymClusters for phase " + phase );
        // commented out because it fails the first time out of the gate due to no prior synonym list
        // Probably okay because if the user is editing the field at all, it probably changed.
        if ( getProject().currentTeam != null ) {
            if ( saveFirst ) {
                currentTeamSynonymGroup.fromString( mmf.teamSynonymList.getText() );
                currentTeamSynonymGroup.writeCollectionToFile();
            }
            // It's okay to use the global "current" variables because this has to be the GUI thread
            refreshForNewTeam( getProject().currentGroup, getProject().currentTeam );
            analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
        }
    }

    /**
     * Called whenever the group synonym list loses focus -- i.e., the user may
     * have changed it. Saves whatever synonymClusters are in the (possibly changed)
 list and then re-analyzes the team.
     *
     * @param saveFirst if true, then save the list from the text pane; if
     * false, just read in from the appropriate file.
     */
    public void updateGroupSynonymList( boolean saveFirst ) {
//            Global.info( "changing synonymClusters for phase " + phase );
        // commented out because it fails the first time out of the gate due to no prior synonym list
        // Probably okay because if the user is editing the field at all, it probably changed.
        if ( getProject().currentGroup != null ) {
            if ( saveFirst ) {
                currentGroupSynonymGroup.fromString( mmf.groupSynonymList.getText() );
                currentGroupSynonymGroup.writeCollectionToFile();
            }
            refreshForNewGroup( getProject().currentGroup );
            analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
        }
    }

    /**
     * When selecting from a list, make sure the others are cleared. Currently
     * not used because it was called on a value change event and that fired
     * multiple calls to itself.
     *
     * @param selected The one that's selected, i.e., not to be cleared.
     */
    public void clearOtherSelections( JList selected ) {
        for ( MPhase p : MPhase.values() ) {
            if ( !mmf.teamPhaseModelList.get( p ).equals( selected ) ) {
                // Global.info( "List for " + p.toString() + " wasn'team selected.");
                mmf.teamPhaseModelList.get( p ).clearSelection();
            }
        }
        //Global.info( "Done with clearing selections");
    }

    
    
    // Listeners for the various lists on the main analysis frame.
    MouseListener modelListMouseListener = new MouseAdapter() {
        public void mouseClicked( MouseEvent e ) {
            JList source = (JList)e.getSource();
            if ( e.getClickCount() == 2 ) {
                String filename = getProject().currentModelName + Global.ChargerFileExtension;
                //                Global.info( "Double click event - current model is " + getProject().currentModelName );
                if ( MMConst.useNewFolderStructure ) {
                    MFile mfile = new MFile( filename );
                    File modelFile = new File( mfile.mfileAbsoluteFolder( getProject().MMSubmittedFolder ), filename );
                    Global.openGraphInNewFrame( modelFile.getPath() );
                } else {
                    Global.openGraphInNewFrame( filename );
                }
            } else {    // mouse clicked once, make this the selection
            }
        }
    };

    class GroupSelectionListener implements ListSelectionListener {
        // This method is called each time the user changes the set of selected items

        public void valueChanged( ListSelectionEvent evt ) {
            // When the user release the mouse button and completes the selection,
            // getValueIsAdjusting() becomes false
            if ( !evt.getValueIsAdjusting() ) {
                JList list = (JList)evt.getSource();
                getProject().currentGroup = (String)list.getSelectedValue();
                refreshForNewGroup( getProject().currentGroup );
            }
        }
    }

    class TeamSelectionListener implements ListSelectionListener {
        // This method is called each time the user changes the set of selected items

        public void valueChanged( ListSelectionEvent evt ) {
            String selectedTeam = null;
            // When the user release the mouse button and completes the selection,
            // getValueIsAdjusting() becomes false
            if ( !evt.getValueIsAdjusting() ) {
                JList list = (JList)evt.getSource();
                getProject().currentTeam = (String)list.getSelectedValue();
                if ( getProject().currentTeam != null ) {   // This happens when blanking out list fires a valueChanged event for TeamSelection
                    refreshForNewTeam( getProject().currentGroup, getProject().currentTeam );
                }
            }
        }
    }

    class ModelSelectionListener implements ListSelectionListener {
        // This method is called each time the user changes the set of selected items

        public void valueChanged( ListSelectionEvent evt ) {
            // When the user release the mouse button and completes the selection,
            // getValueIsAdjusting() becomes false
            if ( !evt.getValueIsAdjusting() ) {
                JList list = (JList)evt.getSource();
                // clearOtherSelections( list ); // currently disabled because clearing fires additional value changed events
                getProject().currentModelName = (String)list.getSelectedValue();
//                        Global.info( "Selected combinedModel: \"" + currentModelName + "\"" );
            }
        }
    }

    /**
     * Used for customizing the combinedModel lists (B, M and E). Currently just looks
     * to see if there's an observer or combined combinedModel and highlights in red or
     * purple
     */
    class ModelListCellRenderer extends JLabel implements ListCellRenderer {

        public ModelListCellRenderer() {
            setOpaque( true );
        }

        public Component getListCellRendererComponent( JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus ) {

            setText( value.toString() );

            Color background = Color.white;
            Color foreground = Color.black;

            if ( ( (String)value ).contains( "_" + MModelID.observerModelUserPrefix ) ) {
                if ( mmf.observerIsSpecial.isSelected() ) {
                    foreground = Color.red;
                }
            }

            if ( ( (String)value ).endsWith( "_" + MModelID.combinedModelUser ) ) {
                if ( mmf.observerIsSpecial.isSelected() ) {
                    foreground = Color.magenta;
                }
            }


            if ( isSelected ) {    // invert colors
                Color temp = background;
                background = foreground;
                foreground = temp;
            }
            setBackground( background );
            setForeground( foreground );
            setFont( new Font( "Arial", Font.BOLD, 14 ) );
            return this;
        }
    }
}
