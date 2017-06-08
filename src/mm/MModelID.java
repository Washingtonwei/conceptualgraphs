
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;

/**
 *  Information about an individual model, but not the model
 * itself. Note that the model name is generally the filename without the
 * extension .cgx
 *
 * @see MModel
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class MModelID {
    
    public static final String observerModelUserPrefix = "observer";
   // public static final String observerModelUserPrefix = "observer";
    public static final String combinedModelUser = "combined";

    /**
     * Should not contain the extension
     */
    private String filename = null;
    private String name = null;
    private String projectName = null;
    private String group = null;
    private String team = null;
    private MPhase phase = null;
    private String user = null;

    public MModelID() {
    }

    /**
     *
     * @param name intended to be the full filename w/o extension, e.g.,
     * mm_py102-faces_team1_b_user1 Should be broken into project = mm, course =
     * py102-faces, team = team1, phase = b, user = user1
     */
    public MModelID( String name ) throws MModelNameException {
        filename = name;
        splitFilename( filename );
    }

    public boolean equals( MModelID mid ) {
        if ( !mid.sameTeamPhase( this ) ) {
            return false;
        }
        if ( mid.filename != filename ) {
            return false;
        }
        if ( mid.getUser() != user ) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether this model has the same project, group, team and
     * phase as another model.
     *
     * @param mid The model for comparison
     * @return true if project, group, team and phase all match; false
     * otherwise.
     */
    public boolean sameTeamPhase( MModelID mid ) {
        if ( ! mid.getProjectName().equals( projectName ) ) {
            return false;
        }
        if ( ! mid.getGroup().equals( group ) ) {
            return false;
        }
        if ( ! mid.getTeam().equals( team ) ) {
            return false;
        }
        if ( ! mid.getPhase().equals( phase ) ) {
            return false;
        }
        return true;
    }

    /**
     * Divides the filename into the constituent parts needed for identifying
     * the model. If a file extension is present, then it is removed.
     *
     * @param name A string of the form proj_group_team_phase_user, possibly
     * with an extension
     * @throws MModelNameException
     */
    public void splitFilename( String name ) throws MModelNameException {
        if ( name.lastIndexOf( "." ) > -1 ) {
            name = name.substring( 0, name.lastIndexOf( "." ) );
        }
        name = name.toLowerCase();
        String[] parts = name.split( "_" );
        if ( parts.length != 5 ) {
            throw new MModelNameException(
                    "MModelID: splitFilename: name \"" + name + "\" does not have five parts as created by the MMAT." );
        }

        setProjectName( parts[0] );
        setGroup( parts[1] );
        setTeam( parts[2] );
        setPhase( parts[3] );
        setUser( parts[4] );
    }

    /**
     * The full filename, without extension.
     * @return the filename associated with this model
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename( String filename ) {
        this.filename = filename;
        try {
            splitFilename( filename );
        } catch ( MModelNameException e ) {
            Global.error( e.getMessage() );
        }
    }

    public String getName() {
        return ( projectName + "_" + group + "_" + team + "_" + phase.abbr() + "_" + user );

    }

    /**
     * @return name without file extension
     */
    /*public String getName() {
     return name;
     }
     * */
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName( String projectName ) {
        this.projectName = projectName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup( String g ) {
        this.group = g;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam( String team ) {
        this.team = team;
    }

    public MPhase getPhase() {
        return phase;
    }

    public void setPhase( String abbr ) {
        switch ( abbr.charAt( 0 ) ) {
            case 'b':
                phase = MPhase.Beginning;
                break;
            case 'm':
                phase = MPhase.Middle;
                break;
            case 'e':
                phase = MPhase.End;
                break;
        }
    }

    public void setPhase( MPhase p ) {
        phase = p;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String toString() {
        return ( "Proj: " + projectName + "; Group: " + group + "; Team: " + team + "; Phase: " + phase.toString() + "; User: " + user );
    }
}
