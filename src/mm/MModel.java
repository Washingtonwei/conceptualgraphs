/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.IOManager;
import charger.exception.CGFileException;
import charger.exception.CGStorageError;
import charger.obj.Graph;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;

/**
 *  The information and structures needed about a single team member's model. A set of
 * models are grouped together at the team/phase level.
 *
 * @see MModelID
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class MModel {

    /**
     * The file that contains the CGX version of this model
     */
    public MFile cgxFile = null;
    /**
     * the charger Graph that corresponds to this model
     */
    public Graph graph = null;
    /**
     * Contains identifying information about this model
     */
    public MModelID id = null;
    /**
     * The project with which this model is associated
     */
    public MProject proj = null;
    /**
     * The phase of some team with which this model is associated.
     */
    public MPhase phase = null;
    /**
     * Whether or not this model is considered an "observer" model (i.e., obtained 
     * from an observer, not from a subject).
     */
    public boolean observerModel = false;
    /**
     * NOt currently used, but in the future we may want to use an expert model (i.e., an oracle).
     */
    public boolean newExpertModel = false;
    
    /**
     * Whether this model is a "team" model with user name "team"
     */
    public boolean teamModel = false;
    
    /**
     * Whether this model is a "team" model with user name "team"
     */
    public boolean combinedModel = false;
    
    /**
     * Date/time this model's file was modified
     */
    public long modifiedDate = 0;

    /**
     * Create a model that isn't associated with any graph or filename
     */
    public MModel() {
    }

    /**
     * Create a model associated with a given file.
     */
    public MModel( MFile f ) throws MModelNameException {
        if ( MMConst.useNewFolderStructure) {
//            cgxFile = new File( )
        } else {
            cgxFile = f;
        }
        id = new MModelID( cgxFile.getFile().getName() );
        setIfSpecial();
    }

    /**
     * Attach an id to the model. Usually the id has already been filled in.
     */
    public void setID( MModelID mid ) {
        this.id = mid;
        setIfSpecial();
    }

    /**
     * @return the id of this model
     */
    public MModelID getID() {
        return this.id;
    }
    
    public void setIfSpecial() {
        MModelID mid = this.getID();
        if ( mid.getUser().toLowerCase().startsWith( MModelID.observerModelUserPrefix ) )
            setObserverModel( true );
        if ( mid.getUser().equalsIgnoreCase( "team" ) )
            setTeamModel( true );
        if ( mid.getUser().equalsIgnoreCase( MModelID.combinedModelUser ) )
            setCombinedModel( true );
    }

    /**
     * @param observerModel whether this model is an observer model or not. This is
     * usually indicated by the user name beginning with "observer" in the model name
     */
    public void setObserverModel( boolean observerModel ) {
        this.observerModel = observerModel;
    }

    /**
     * @return whether this is considered an observer model or not
     */
    public boolean isObserverModel() {
        return observerModel;
    }

    public void setTeamModel( boolean teamModel ) {
        this.teamModel = teamModel;
    }


    public boolean isTeamModel() {
        return teamModel;
    }

    public boolean isCombinedModel() {
        return combinedModel;
    }

    public void setCombinedModel( boolean combinedModel ) {
        this.combinedModel = combinedModel;
    }
    
    /**
     * If the model is  one of the "special" models.
     * @return Whether the model is special. Currently checks for whether it's an observer model or a combined model.
     */
    public boolean isSpecialModel() {
        if ( isObserverModel() ) return true;
        if ( isCombinedModel() ) return true;
        //if ( isTeamModel() ) return true;
        return false;
    }
    
    
    protected void finalize() {
        graph = null;
    }
    /**
     * Access the graph attached to this model. If it has not already been loaded, find it and load it.
     * @return a Charger graph associated with this model
     */
    public Graph getGraph() {
        if ( this.graph == null ) {
            this.insertGraphIntoModel();
        }
        return graph;
    }

    /**
     * @param graphToSet a Charger graph to be associated with this model.
     */
    public void setGraph( Graph graphToSet ) {
        this.graph = graphToSet;
        DateFormat df = DateFormat.getDateInstance( DateFormat.MEDIUM );
        try {
            this.setModifiedDate( df.parse( graphToSet.createdTimeStamp ).getTime() );
        } catch ( ParseException ex ) {
            Global.warning( ex.getMessage()
                    + " Parsing created date \"" + graphToSet.createdTimeStamp + "\" of " + this.getID().getName() );
        }
    }

    /**
     * @return the date for the graph in this model, usually but not necessarily
     * the last modified date of the Charger file from which the graph comes.
     */
    public long getModifiedDate() {
        if ( graph == null ) {
            insertGraphIntoModel();
        }
        return modifiedDate;
    }

    /**
     * @param modifiedDate the last modified date/time of the model, usually but
     * not necessarily the last modified date of the Charger file from which the
     * graph comes.
     */
    public void setModifiedDate( long modifiedDate ) {
        this.modifiedDate = modifiedDate;
    }
    
    /**
     * Finds the graph file for this model, converts it to CG form and attaches
     * it to the model.
     */
    public void insertGraphIntoModel() {
        Graph graphToInsert = new Graph();
        MProject proj = MMConst.getProjectByName( this.getID().getProjectName() );
        File file;
        if ( MMConst.useNewFolderStructure ) {
            MFile mfile = new MFile( getID().getFilename() );
            file = new File( mfile.mfileAbsoluteFolder( proj.MMSubmittedFolder ), getID().getFilename() );
        } else {
            file = new File( proj.MMSubmittedFolder, getID().getFilename() );
        }
        try {
            //         charger.IOManager iomgr = new charger.IOManager(null);
//            IOManager.FileToGraph( file, graphToInsert, null );   // translate was null so 
            IOManager.FileToGraph( file, graphToInsert );
        } catch ( CGFileException e ) {
            Global.error( "Couldn't load graph " + this.getID().getFilename() + ": " + e.getMessage() );
        } catch ( CGStorageError e ) {
            Global.error( "Storage exception loading graph " + this.getID().getFilename() + ": " + e.getMessage() );
        }
        this.setGraph( graphToInsert );
        graphToInsert.addHistory( new MMObjectHistoryEvent( this ) );
//            Global.info( "Graph added to model " + this.getID().getName()  );
    }

    public MFile FileStatus() {
        return cgxFile;
    }

    public void setCgxFile( MFile cgxFile ) {
        this.cgxFile = cgxFile;
    }
    
    
}
