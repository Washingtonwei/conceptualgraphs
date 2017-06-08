/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.obj.Graph;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import kb.KnowledgeBase;

/**
 * GUI, managers and structures at the project ("top") level and also all the
 * team-phase objects. Note the models themselves are stored in collections at
 * the team/phase level. The notion of a "group" in an project (e.g., $mmat$,
 * py102-faces) is maintained only by the names of the models. All group models are in
 * one project. They must be traversed to split them up into their respective
 * groups.
 * 
 * <p>
 * For each set of models in a single submitted folder, one MProject object is created.
 * It instantiates an analysis frame, which in turn instantiates the analysis manager
 * and the model manager. Appropriate methods are provided so that each sub-component
 * has access to its controlling MProject object.
 * </p>
 *
 * @see MTeamPhase
 * @author Harry Delugach
 * @since Charger 3.8.7
 *
 */
public class MProject {

    public String name = null;
    
    public MMAnalysisFrame frame = null;
    ArrayList<MTeamPhase> teamPhases = new ArrayList();
    /**
     * key = Group name; value = synonyms for that group.
     */
    HashMap<String, ClusterCollection> groupSynonyms = new HashMap<String, ClusterCollection>();
    
    /**
     * key = Group name; value = knowledge base for that group
     */
    HashMap<String, KnowledgeBase> knowledgeBases = new HashMap<String, KnowledgeBase>();
        // TODO: incorporate separate knowledge bases for each group when opening in Charger

        /**
     * The folder containing the submitted models for analysis. When running
     * Charger for the purposes of doing MMAT analysis, this is the folder that
     * should be the 2nd argument of the "-p" parameter.
     *
     */
    public  File MMSubmittedFolder = null;
        /**
     * The folder containing the saved (but not submitted) models. This
     * folder should be a peer to the submitted folder.
     *
     */
    public  File MMSavedFolder = null;
    

    /**
     * The folder in which both the "submitted" and "saved" folders reside.
     */
    public  File MMAnalysisParent = null;

    public      MTeamMetrics currentTeamMetrics = null;

    
    public  String currentGroup = null;
    public  String currentTeam = null;
    public  String currentModelName = null;
//    public static MProject currentProj = null;

    public MProject( File folder ) {
        MMSubmittedFolder = folder;
        frame = new MMAnalysisFrame( this );
                getFrame().analyzer.refreshForNewGroup( (String)frame.groupList.getSelectedValue() );
    }
    
    /** Zeroes out all team phases and starts from scratch */
    public void clearProject() {
        teamPhases.clear();
        this.groupSynonyms.clear();
        this.knowledgeBases.clear();
    }

    public MMAnalysisFrame getFrame() {
        return frame;
    }

    public void setFrame( MMAnalysisFrame frame ) {
        this.frame = frame;
    }
    
    
    

    /**
     * Gives the readable name for this project
     *
     * @return the readable name
     */
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Get the team phase (collection of models for that team and phase) for a
     * given model id
     *
     * @param mid a valid model ID, to be checked down to the team phase level
     * @return an MTeamPhase full of models for that team and phase.
     * If there isn't already a team phase, then create one.
     */
    public MTeamPhase getTeamPhaseByID( MModelID mid ) {
        for ( MTeamPhase tp : teamPhases ) {
            if ( tp.getID().sameTeamPhase( mid ) ) {
                return tp;
            }
        }
            // if a team phase wasn't found, then create an empty one.
        MTeamPhase teamphase = new MTeamPhase( mid );
        addTeamPhase( teamphase );
            
        return teamphase;
    }

    /**
     * Insert another teamphase into this project.
     *
     * @param tp the filled-in team phase to be added. There is no special
     * processing; users may make changes to the team phase object later as
     * desired.
     */
    public void addTeamPhase( MTeamPhase tp ) {
        teamPhases.add( tp );
    }

    /**
     * Inserts the model into the right team-phase object. Does NOT add the
     * Charger graph into the model instance; this will be the job of whatever
     * routine needs the model. Various exceptions may be thrown.
     *
     * @param mm A valid model. If its teamphase does not already exist, one is
     * created and added to the MProject instance.
     * @param includeGraph whether to include the graphs when inserting the model object.
     */
    public void insertModelInTeamPhase( MModel mm, boolean includeGraph ) {
        // Global.info( "about to insert model in proj \"" + getName() + "\" -- "+ mm.getID().toString() );
        MTeamPhase tp = getTeamPhaseByID( mm.getID() );
        if ( tp == null ) {
            // create new TeamPhase
            tp = new MTeamPhase( mm );
            addTeamPhase( tp );
        }
        try {
            tp.addModel( mm );
            if ( includeGraph ) {
                Graph g = mm.getGraph();
            }

        } catch ( MModelNameException e ) {
            Global.error( e.getMessage() );
        }
//            Global.info( "Model " + mm.getID().getName() + " added to project.");
    }

    /**
     * Scans the entire set of team phases to get the set of unique group names.
     *
     * @return the set of groups in no particular order.
     */
    public ArrayList<String> getGroupList() {
        ArrayList<String> groups = new ArrayList();
        for ( MTeamPhase tp : teamPhases ) {
            String gname = tp.getID().getGroup();
            if ( !groups.contains( gname ) ) {
                groups.add( gname );
            }
        }
        return groups;
    }

    /**
     * Scans the entire set of team phases to get the set of unique team names
     * in the given group.
     *
     * @param group team phase must be in this group
     * @return the set of teams in no particular order. If there are no teams,
     * the list has size zero.
     */
    public ArrayList<String> getTeamList( String group ) {
        ArrayList<String> teams = new ArrayList();
        for ( MTeamPhase tp : teamPhases ) {
            String gname = tp.getID().getGroup();
            String tname = tp.getID().getTeam();
            if ( gname.equals( group ) && !teams.contains( tname ) ) {
                teams.add( tname );
            }
        }
        return teams;
    }

    /**
     * Gets the entire set of models in an entire group, regardless of team or
     * phase
     *
     * @param group
     * @return all models that are in the group; can be identified through their
     * model id's.
     */
    public ArrayList<String> getGroupModelList( String group ) {
        ArrayList<String> teams = new ArrayList();
        for ( MTeamPhase tp : teamPhases ) {
            String gname = tp.getID().getGroup();
            String tname = tp.getID().getTeam();
            if ( gname.equals( group ) && !teams.contains( tname ) ) {
                teams.add( tname );
            }
        }
        return teams;
    }
    
    /**
     * Finds the team phase object matching the parameters given by name, not model id.
     * @param group
     * @param team
     * @param p
     * @return a team phase with the appropriate model id. If one didn't exist already, one is
     * created for it.
     */
    public MTeamPhase getTeamPhaseByNames( String group, String team, MPhase p ) {
        MModelID mid = new MModelID();
        mid.setProjectName( this.getName() );
        mid.setGroup( group );
        mid.setTeam( team );
        mid.setPhase( p );
        return getTeamPhaseByID( mid );
    }

    /**
     * If there are any group synonyms (in a hard-wired file name based on the
     * group name), load them from the file. This is performed whether or not
     * group synonyms are enabled. If the file is empty or non-existent, then
     * nothing is created or added to lists.
     *
     * @param group
     */
    public void addGroupSynonyms( String group ) {
        File groupFile = getFrame().getAnalyzer().groupSynonymFile( this, group );
        ClusterCollection ss = new ClusterCollection( groupFile );
        ss.setOriginDescription(  "Group synonyms for group \"" + group + "\"" );
        if ( ss.getSynonymFile() != null ) {
            groupSynonyms.put( group, ss );
           
                    Global.info( "Adding group synonyms for group " + group );
        }
    }
}
