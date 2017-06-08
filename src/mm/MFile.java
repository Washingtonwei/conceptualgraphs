/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import java.io.File;

/**
 * Represents the abstraction of any kind of MM file. There are several
 * different kinds of MM files. A simple model name represented by a MModelID
 * object, but there are other kinds as well. In the future, this class should
 * replace some uses of MModelID that are solely for file naming purposes.
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 * @see MFileType
 */
public final class MFile /* extends File */ {

    /**
     * The simple file name (no path)
     */
    public String filenameWOExtension = null;
    /**
     * The extension does NOT include the "." separator!
     */
    private String extension = null;
    
    /**
     * A regular File object associated with this MM file
     */
    public File folder = null;
    /**
     * The type of this file.
     */
    public MFileType type = MFileType.OTHER;
    private String project = null;
    private String group = null;
    private String team = null;
    private String phase = null;     // just the abbreviation
    private MPhase mphase = null;    // not used
    private String user = null;
    public final static String report = "REPORT";
    public final static String synonyms = "SYNONYMS";
    public final static String summary = "SUMMARY";
    public final static String spreadsheet = "SPREADSHEET";
    public final static String adminLog = "ADMIN_LOG";
    private String originalName = null;

    /**
     * Create an empty instance. Usually used for forming a new file to be saved or searched for.
     */
    
    public MFile() {
        
    }
    /**
     * Create a new MFile instance from an existing File instance.
     * @param f the existing file
     * @see File
     */
    public MFile( File f ) {
        originalName = f.getName();
        parseFilename( f.getName() );
    }

     /**
     * Create a new MFile instance from an existing File instance.
     * @param filename the existing file's name, not including an absolute path
     * @see File
     */
   public MFile( String filename ) {
       originalName = filename;
        parseFilename( filename );
    }

     /**
     * Create a new MFile instance from an existing File instance.
     * @param f the existing file
     * @see File
     */
   public MFile( File f, String filename ) {
        parseFilename( filename );
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder( File folder ) {
        this.folder = folder;
    }
   
   

   /** Tell this file what kind of type it should be */
    public void setType( MFileType t ) {
        this.type = t;
    }

    
    public MFileType getType() {
        return type;
    }

    /** Just parse the filename string and decide what type it would be */
    public static MFileType getType( String filename ) {
        return ( new MFile( filename ) ).getType();
    }
    
    /**
     * Returns a regular File object corresponding to this one.
     */
    public File getFile() {
        return new File( folder, makeFilename() );
    }

    /**
     * Generate the filename needed for this MM file object. Includes extension
     * but does not include absolute path.
     * If not one of the identified file types, then just return the original file name.
     */
    public String makeFilename() {
        switch ( type ) {
            case MODEL: {
                return project + "_" + group + "_" + team + "_" + phase + "_" + user + "." + type.extension();
            }
            case GROUPREPORT: {
                return project + "_" + group + "_" + report + "." + type.extension();
            }
            case TEAMREPORT: {
                return project + "_" + group + "_" + team + "_" + report + "." + type.extension();
            }
            case GROUPSYNONYM: {
                return project + "_" + group + "_" + synonyms + "." + type.extension();

            }
            case TEAMSYNONYM: {
                return project + "_" + group + "_" + team + "_" + synonyms + "." + type.extension();
            }
            case SUMMARY: {
                return project + "_" + summary + "." + type.extension();
            }
            case SPREADSHEET: {
                return project + "_" + spreadsheet + "." + type.extension();
            }
            case ADMIN_LOG : {
                return project + "_" + adminLog + "." + type.extension();
            }
            case OTHER: {
                return originalName;
            }
        }
        return originalName;
    }
    
        /**
     * Generate the foldername needed for this MM file object. 
     * The foldername includes the top level project folder
     * If not one of the identified file types, then just return the original file name.
     * @param top The project folder; i.e., the one in which all the other files are contained
     */
    public File mfileAbsoluteFolder( File top ) {
        String sep = File.separator;
        switch ( type ) {
            case MODEL: {
                return new File( top + sep + group + sep + team );
            }
            case GROUPREPORT: {
                return new File( top + sep + group );
            }
            case TEAMREPORT: {
                return new File( top + sep + group + sep + team );
            }
            case GROUPSYNONYM: {
                return new File( top + sep + group );

            }
            case TEAMSYNONYM: {
                return new File( top + sep + group + sep + team );
            }
            case SUMMARY: {
                return top;
            }
            case SPREADSHEET: {
                return top;
            }
           case ADMIN_LOG: {
                return top;
            }
            case OTHER: {
                return new File( originalName );
            }
        }
                return new File( originalName );
    }


    /**
     * Establish each of the parts of an MM file name and assign them, including the extension.
     * If the extension does not match the identified file type, then set the type to MFileType.OTHER.
     * @param name The name of any file that might mean something in the MM. It may have an extension or not.
     * Underscores are the separators between the meaningful parts of the name.
     */
    protected void parseFilename( String name ) {
        String[] tops = name.split( "\\." );
        filenameWOExtension = tops[ 0];
        if ( tops.length > 1 ) {
            extension = tops[ 1];
        }

        String[] parts = filenameWOExtension.split( "_" );

        project = parts[ 0];

        if ( parts.length > 1 ) {
            if ( parts[ 1].equalsIgnoreCase( summary ) ) {
                type = MFileType.SUMMARY;
            } else if ( parts[ 1].equalsIgnoreCase( spreadsheet ) ) {
                type = MFileType.SPREADSHEET;
            } else if ( parts[ 1].equalsIgnoreCase( adminLog ) ) {
                type = MFileType.ADMIN_LOG;
            } else {
                group = parts[ 1];
            }
        }

        if ( parts.length > 2 ) {
            if ( parts[ 2].equalsIgnoreCase( synonyms ) ) {
                type = MFileType.GROUPSYNONYM;
            } else if ( parts[ 2 ].equalsIgnoreCase( report ) ) {
                type = MFileType.GROUPREPORT;
            } else {
                team = parts[ 2];
            }
        }

        if ( parts.length > 3 ) {
            if ( parts[3].equalsIgnoreCase( report ) ) {
                type = MFileType.TEAMREPORT;
            } else if ( parts[ 3].equalsIgnoreCase( synonyms ) ) {
                type = MFileType.TEAMSYNONYM;
            } else {
                setPhase( parts[ 3] );
            }
        }

        if ( parts.length > 4 ) {
            user = parts[ 4];
            type = MFileType.MODEL;
        }

        // If extension from filename doesn't match what it's supposed to be be, then consider this unknown
        if ( !getExtension().equalsIgnoreCase( type.extension() ) ) {
            type = MFileType.OTHER;
        }
    }

    /** @return extension, without the leading period. */
    public String getExtension() {
        if ( extension == null ) {
            return "";
        } else {
            return extension;
        }
    }

    /** @param extension the extension with the period. */
    public void setExtension( String extension ) {
        this.extension = extension;
    }

    public String getProject() {
        if ( project == null ) {
            return "";
        } else {
            return project;
        }
    }

    public void setProject( String project ) {
        this.project = project;
    }

    /** @return empty string if null, the group otherwise */
    public String getGroup() {
        if ( group == null ) {
            return "";
        } else {
            return group;
        }
    }

    public void setGroup( String group ) {
        this.group = group;
    }

    /** @return empty string if null, the team otherwise */
    public String getTeam() {
        if ( team == null ) {
            return "";
        } else {
            return team;
        }
    }

    public void setTeam( String team ) {
        this.team = team;
    }

        /** @return empty string if null, the phase abbreviation (1-char) otherwise */
    public String getPhase() {
        if ( phase == null ) {
            return "";
        } else {
            return phase;
        }
    }

    /** @param phase the phase 1-letter abbreviation */
    public void setPhase( String phase ) {
        this.phase = phase;
        this.mphase = MPhase.lookupAbbr1( phase );

    }

    public MPhase getMphase() {
        return mphase;
    }

    public void setMphase( MPhase mphase ) {
        this.mphase = mphase;
        this.phase = mphase.abbr();
    }

     /** @return empty string if null, the user otherwise */
   public String getUser() {
        if ( user == null ) {
            return "";
        } else {
            return user;
        }
    }

    public void setUser( String user ) {
        this.user = user;
    }
}
