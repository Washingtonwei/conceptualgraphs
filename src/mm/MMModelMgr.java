/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

import charger.EditFrame;
import charger.EditingChangeState;
import charger.Global;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MMModelMgr {

    private MMAnalysisFrame frame = null;
    public static MMFileManager submittedFolderFileMgr = null;

    public MMModelMgr( MMAnalysisFrame frame ) {
        this.frame = frame;
        submittedFolderFileMgr = new MMFileManager( frame.getProject().MMSubmittedFolder );
    }
    
    public MProject getProject() {
        return getFrame().getProject();
    }

    public MMAnalysisFrame getFrame() {
        return frame;
    }
    
    public void tearDown() {
        
    }
    
        /**
     * Initializes the list of predefined groups to include all known groups,
     * as well as -admin- and -trash-.
     */
    public ArrayList<String> getPredefinedGroups() {
        ArrayList<String> list = new ArrayList<>();
        Object[] slist = frame.groupListModel.toArray();
        for ( Object s : slist ) {
            list.add( (String)s );
        }
        if ( ! list.contains(  "-admin-") )
            list.add( "-admin-");
        if ( ! list.contains(  "-trash-") )
            list.add( "-trash-");
        return list;
    }


    


    
        /**
     * Open all the models named in the text elements of the combinedModel
     * given.
     */
    public void openModelsInCharger( DefaultListModel listmodel ) {
        String[] models = new String[ listmodel.getSize() ];
        for ( int element = 0; element < listmodel.getSize(); element++ ) {
            String filename = (String)listmodel.elementAt( element ) + Global.ChargerFileExtension;
            if ( MMConst.useNewFolderStructure ) {
                MFile mfile = new MFile( filename );
                File modelFile = new File( mfile.mfileAbsoluteFolder( getProject().MMSubmittedFolder), filename );
                Global.openGraphInNewFrame( modelFile.getPath() );
            } else {
                models[ element] = filename;
            }
//            openInCharger( (String)listmodel.elementAt( element ) );
        }
         if ( ! MMConst.useNewFolderStructure )
             Global.CharGerMasterFrame.openNamedFiles( models );
   }

    /**
     * Combine the given models in a single edit frame, as an unsaved (but titled) graph.
     * Check if any model is an observer model that should be excluded.
     */
    public void combineModelsInCharger( MTeamPhase tp ) {
        if ( tp == null ) {
            return;
        }
        ModelCombiner combiner =
                new ModelCombiner( tp.getModels( false ), tp.congruenceMetrics.get( MMComponentKind.CONCEPT ).cDistinct );
                    // BUG: the combined file is being named for the folder in which Charger sits, not where the models are!
        MFile combinedFile = new MFile( tp.getID().getFilename() );
        combinedFile.setUser( MModelID.combinedModelUser.toUpperCase() );
        combinedFile.setType( MFileType.MODEL );
        if ( MMConst.useNewFolderStructure ) {
            combinedFile.setFolder( combinedFile.mfileAbsoluteFolder( getProject().MMSubmittedFolder ) );
        } else {
            combinedFile.setFolder( getProject().MMSubmittedFolder );
        }
        EditFrame ef = new EditFrame(
                new File( combinedFile.getFolder(), combinedFile.makeFilename() ),
//                combiner.getCombinedGraph( ModelCombiner.Mode.NESTED), true );
                combiner.getCombinedGraph( ModelCombiner.Mode.FLAT), true );
        ef.emgr.setChangedContent( EditingChangeState.EditChange.SEMANTICS  );
        if ( Global.enableEditFrameThreads ) {
            new Thread( Global.EditFrameThreadGroup, ef ).start();
        }

    }

        /**
     * Convenience action to take control when user selects the general combinedModel
     * renaming option. Sets up the combinedModel renamer frame, where control is
     * transferred. The renaming therefore takes place in two parts, first by
     * this method and next by the combinedModel renamer.
     *
     * @see ModelRenamer
     */
    public void startupRenamer() {
        String newName = "";
        ModelRenamer renamer = null;
        if ( getProject().currentModelName == null ) {
            JOptionPane.showMessageDialog( frame, "Sorry, there is no current model selected." );
            return;
        } else {
            try {
                renamer = new ModelRenamer( this, getProject().currentModelName );
            } catch ( MModelNameException ex ) {
                JOptionPane.showMessageDialog( frame, "Error in current model name: " + ex.getMessage() );
                return;
            }

            // Hand over to the renamer
            renamer.setVisible( true );
        }
    }

    /**
     * Convenience action to move an entire team from both the save directory
     * and submittedFile directory. While it has no parameters, it relies on
     * MMConst.currentGroup and currentTeam to be accurate. Displays a summary dialog
     * when done.
     */
    public void moveCurrentTeamAction() {
        String newGroup = "";
        if ( getProject().currentTeam == null ) {
            JOptionPane.showMessageDialog( frame, "Sorry, there is no current team." );
        } else {
            newGroup = JOptionPane.showInputDialog( frame, "Group: " + getProject().currentGroup 
                    + "\nTeam: " + getProject().currentTeam.toUpperCase()
                    + "\n\nEnter the new group name:", getProject().currentGroup );
            Global.info( "new group name is \"" + newGroup + "\"." );

            if ( newGroup != null ) {
                if ( newGroup.contains( "_" ) || newGroup.contains( "." ) || newGroup.contains( " " ) ) {
                    JOptionPane.showMessageDialog( frame, "Group: " + newGroup
                            + "'s name cannot contain underscores, spaces or periods. Please try again." );
                    return;
                }

                // Check to see if the new group is the same as the old group
                if ( newGroup.equalsIgnoreCase( getProject().currentGroup ) ) {
                    JOptionPane.showMessageDialog( frame, "Team " + getProject().currentTeam.toUpperCase()
                            + " is already in group \"" + newGroup.toUpperCase() + "\". No files changed." );
                    return;
                }

//                File submittedFolder = new File( MMConst.MMSubmittedFolder.getAbsolutePath() );
                // First find out whether any models for this team already exists in the new group
                boolean teamExistsInNewGroup = false;
                for ( String t : getProject().getTeamList( newGroup ) ) {
                    if ( t.equalsIgnoreCase( getProject().currentTeam ) ) {
                        teamExistsInNewGroup = true;
                    }
                }
                int result = JOptionPane.YES_OPTION;
                if ( teamExistsInNewGroup ) {
                    result = JOptionPane.showConfirmDialog( frame, "Group: \"" + newGroup
                            + "already has models for a team named \"" + getProject().currentTeam.toUpperCase() + "\"\n"
                            + "Do you want to add to and/or replace its models?" );
                }
                if ( result == JOptionPane.YES_OPTION ) {
                    int numberSubmitted = submittedFolderFileMgr.moveTeam( getProject().currentGroup, newGroup, getProject().currentTeam );
                    JOptionPane.showMessageDialog( frame, numberSubmitted + " file(s) renamed." );
                    if ( numberSubmitted > 0 ) {
                        String teamToRestore = getProject().currentTeam;
                        frame.analyzer.refreshAnalysisWindow( false );
                        frame.analyzer.initializeTopPanels();

                        frame.analyzer.refreshForNewGroup( newGroup );  // needs to reload the group  
                        frame.teamList.setSelectedValue( teamToRestore, true );

                    }
                }
            }
        }
    }

    /**
     * Convenience action to move an entire team from both the save directory
     * and submittedFile directory. While it has no parameters, it relies on
     * currentGroup and MMConst.currentTeam to be accurate. Displays a summary dialog
     * when done.
     *
     * @see MProject#currentTeam
     * @see MProject#currentGroup
     */
    public void renameCurrentTeamAction() {
        String newTeam = "";
        if ( getProject().currentTeam == null ) {
            JOptionPane.showMessageDialog( frame, "Sorry, there is no current team." );
        } else {
            newTeam = JOptionPane.showInputDialog( frame, "Group: " + getProject().currentGroup + "\nTeam: " + getProject().currentTeam.toUpperCase()
                    + "\n\nEnter the new team name:", getProject().currentTeam );
            newTeam = newTeam.toLowerCase();
            Global.info( "new team name is \"" + newTeam + "\"." );

            if ( newTeam != null ) {
                // Check to make sure it doesn'team contain underscores or periods.
                if ( newTeam.contains( "_" ) || newTeam.contains( "." ) || newTeam.contains( " " ) ) {
                    JOptionPane.showMessageDialog( frame, "Team: " + newTeam
                            + "'s name cannot contain underscores, spaces or periods. Please try again." );
                    return;
                }
                // Check to see if it's the same as the current team
                if ( newTeam.equalsIgnoreCase( getProject().currentTeam ) ) {
                    JOptionPane.showMessageDialog( frame, newTeam.toUpperCase() + "'s name is already \""
                            + getProject().currentTeam.toUpperCase() + "\". No files changed." );
                    return;
                }
//                File submittedFolder = new File( MMConst.MMSubmittedFolder.getAbsolutePath() );

                // First find out whether any models for this team already exists in the new group
                boolean teamAlreadyExists = false;
                for ( String t : getProject().getTeamList( getProject().currentGroup ) ) {
                    if ( t.equalsIgnoreCase( newTeam ) ) {
                        teamAlreadyExists = true;
                    }
                }
                int result = JOptionPane.YES_OPTION;
                if ( teamAlreadyExists ) {
                    result = JOptionPane.showConfirmDialog( frame, "Group: \"" + getProject().currentGroup
                            + "\" already has models for a team named \"" + newTeam.toUpperCase() + "\"\n"
                            + "Do you want to add to and/or replace its models?" );
                }
                if ( result == JOptionPane.YES_OPTION ) {
                    int numberSubmitted = submittedFolderFileMgr.renameTeam( getProject().currentGroup, getProject().currentTeam, newTeam );
                    JOptionPane.showMessageDialog( frame, numberSubmitted + " file(s) renamed." );
                    if ( numberSubmitted > 0 ) {
                        String groupToRestore = getProject().currentGroup;
                        frame.analyzer.refreshAnalysisWindow( true );
                        frame.analyzer.initializeTopPanels();

                        frame.analyzer.refreshForNewGroup( groupToRestore );  // needs to reload the group  
                        frame.teamList.setSelectedValue( newTeam, true );

                    }
                }
            }
        }
    }

        /**
     * Compares the set of saved models with the set of submittedFile models.
     * Since the usual procedure is for all models to be stored in the top-level
     * folder, with only submittedFile models in the nested folder, a simple
     * scan determines whether each saved combinedModel has a same-named combinedModel in the
     * submittedFile folder. This routine doesn't look to see if the files
     * are the same size, date, etc.
     *
     * @return a newline-separated list of the simple file names with modification dates.
     */
    public String checkForUnSubmittedModels() {

        String[] submittedNames = MMFileManager.getMMATFileList( getProject().MMSubmittedFolder, MFileType.MODEL );
            if ( submittedNames.length == 1 && submittedNames[0] == null ) submittedNames = new String[0];

        String[] savedNames = null;
        if ( MMConst.useNewFolderStructure ) {
            savedNames = MMFileManager.getMMATSavedOnlyFileList( getProject().MMSubmittedFolder ).toArray( new String[1] );
            if ( savedNames.length == 1 && savedNames[0] == null ) savedNames = new String[0];
        } else {
            savedNames = MMFileManager.getMMATFileList( getProject().MMSavedFolder, MFileType.MODEL );
        }

        String unsubmittedText = "";


        if ( savedNames == null ) {
            unsubmittedText = "Can't find folder " + getProject().MMSavedFolder.getAbsolutePath() + "\n";
        } else if ( savedNames.length == 0 ) {
            unsubmittedText = "-- No unsubmitted (Saved only) models found --\n";
        } else {
            for ( String name : savedNames ) {
                File unsubmittedFile;
                if ( MMConst.useNewFolderStructure ) {
                    MFile mfile = new MFile( name );
                    File submittedFolder = mfile.mfileAbsoluteFolder( getProject().MMSubmittedFolder )  ;
                    File savedFolder = new File(
                            submittedFolder.getAbsolutePath() + File.separator + "Saved");
                    unsubmittedFile =  new File( savedFolder , name );
                } else {
                    unsubmittedFile = new File( getProject().MMSavedFolder, name );
                }
                Date d = new Date( unsubmittedFile.lastModified() );
                unsubmittedText += d.toString() + " \t " + name;
                // Look for any saved files that are also in the submittedFile folder -- this would be an error
                // since it's our intention that the saved file be deleted once submittedFile.
                if ( submittedNames.length != 0 ) 
                if ( Arrays.binarySearch( submittedNames, name ) >= 0 ) {
                    File duplicatedFile = new File( getProject().MMSubmittedFolder, name );
                    d = new Date( duplicatedFile.lastModified() );
                    unsubmittedText += " (duplicated in submitted folder " + d.toString() + ")";
                }
                unsubmittedText += "\n";
            }
        }
        return unsubmittedText;
    }

}
