/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.util.Tag;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * A collection of typed synonym sets. Keeps the filename associated with this collection
 of synonymClusters. Synonyms are stored as either
 <pre>
 * term1, term2, term3, ...
 * </pre> or as
 * <pre>
 * type: term1, term2, term3, ...
 * </pre>
 *
 * @see mm.MTypedSynonymCluster
 * @author Harry Delugach
 * @since Charger 3.8.2
 */
public class ClusterCollection {

    public ArrayList<MTypedSynonymCluster> synonymClusters = new ArrayList<>();
    /**
     * A file to contain the synonymClusters. Hard wired into the MMAnalysisFolder
     * folder
     */
    public File synonymFile = null;
    /**
     * A description of the origin of this synonym collection -- e.g., from a group
     * synonym file. Used in displays.
     */
    private String originDescription = "";
    /**
     * Used in the filename of a synonym file as a dummy MMAT "user" in the
     * filename syntax.
     */
    public static final String synonymUser = "SYNONYMS";
    /**
     * Filename extension for the synonym file.
     */
    public static final String synonymExtension = "txt";

//    public class 
    /**
     * Loads the synonym collection from a file.
     *
     * @param f Should be synonym file with an absolute path.
     *
     */
    public ClusterCollection( File f ) {
//                Global.info( "Trying to find synonym file named " + synonymFile.getAbsolutePath() );
        synonymFile = f;
        if ( f == null ) {
            return;
        }
        if ( f.exists() ) {
            loadFromFile( synonymFile );
//                    Global.info( "Found synonymClusters in file named " + synonymFile.getAbsolutePath() );
        }
    }

    /**
     * Make a collection instance based on the string as a formatted synonym text
     * block (e.g., from a file). This constructor seems to be creating lots of
     * garbage due to trimming of strings.
     *
     * @param s
     */
    public ClusterCollection( String s ) {
        fromString( s );
    }

    /**
     * Copy constructor creates a set of new objects identical to the parameter.
     * Like clone() except that it's called differently.
     *
     * @param syns a collection to be completely copied
     *
     */
    public ClusterCollection( ClusterCollection syns ) {
        synonymClusters.clear();
        for ( MTypedSynonymCluster cluster : syns.synonymClusters ) {
            synonymClusters.add(new MTypedSynonymCluster( cluster.toString() ) );
        }
        synonymFile = new File( syns.synonymFile.getAbsolutePath() );
    }

    /**
     * An empty synonym collection.
     */
    public ClusterCollection() {
    }

    /**
     * Combine this object's synonyms with the ones in another synonym collection. If
     * there is a filename in the others, it is lost.
     *
     * @param others
     * @return a single set of synonyms, where this object keeps its original file name.
     */
    public ClusterCollection mergeCollections( ClusterCollection others ) {
        if ( others == null ) {
            return this;
        }
        ClusterCollection news = new ClusterCollection( others.toString() );
        for ( MTypedSynonymCluster cluster : synonymClusters ) {
            news.synonymClusters.add( cluster );
        }
        news.setSynonymFile( this.getSynonymFile() );
        return news;
    }

    public void addSynonymCluster( MTypedSynonymCluster newcluster ) {
        synonymClusters.add( newcluster );
    }

    /**
     * Removes the synonym set from the given index in the collection. Indices are
     * numbered 0... size-1.
     *
     * @param position The index of a synonym set in the collection. If the position
     * is out of range, then return silently.
     */
    public void deleteSynonymCluster( int position ) {
        if ( position < 0 || position >= synonymClusters.size() ) {
            return;
        }
        Global.info( "removing synonym cluster at position " + position );
        synonymClusters.remove( position );
    }

    /**
     *
     * @return true if there are no synonymClusters (regardless of whether there's a
 type)
     */
    public boolean isEmpty() {
        if ( synonymClusters.isEmpty() ) {
            return true;
        } else {
            return false;
        }
    }


    public void setOriginDescription( String originDescription ) {
        this.originDescription = originDescription;
    }

    public String getOriginDescription() {
        return originDescription;
    }

    /**
     * Returns true if the two terms either match each other right away or
     * appear together in a synonym list. Ignores their type information.
     *
     * @param s1
     * @param s2
     * @return true if the two terms either match each other right away or
     * appear together in a synonym list.
     * @see SynonymCluster#pairMatches
     */
    public boolean match( String s1, String s2 ) {
        if ( s1.equalsIgnoreCase( s2 ) && !s1.equals( "" ) ) {
            return true;
        }
        for ( MTypedSynonymCluster cluster : synonymClusters ) {
            if ( cluster.pairMatches( s1, s2 ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * A type-restricted referent match.
     *
     * @param typelabel Only referents with this typelabel will match. If null
     * or zero-length string, then any typelabel will match (i.e., typelabel is
     * ignored).
     * @param s1
     * @param s2
     * @return true if s1 and s2 match and are both referents of the typelabel
     * given.
     *      * @see #pairMatches
     *
     */
    public boolean match( String typelabel, String s1, String s2 ) {
//                Global.info( "match type " + typelabel + ": " + s1 + " and " + s2 );
        if ( typelabel == null || typelabel.equals( "" ) ) {
            return match( s1, s2 );
        }
        String type = typelabel.trim();

        for ( MTypedSynonymCluster cluster : synonymClusters ) {        // for every list of synonymClusters
            if ( cluster.typeEquals( type ) ) {        // only look for synonymClusters if this list's type agrees
                if ( cluster.pairMatches( s1, s2 ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new synonym group from "scratch" using the text in the argument
     * string. Assumes synonymClusters are in the same form as a synonym file --
 comma separated terms on each line. Whitespace next to commas is ignored.
 Essentially the reverse of toString()
     *
     * @param s a sequence of newline-separated strings where each string is
     * either &quot;type: syn, syn&quot; ... or just syn, syn, ...
     */
    public void fromString( String s ) {
        synonymClusters.clear();
        String lines[] = s.split( "\n" );
        for ( int line = 0; line < lines.length; line++ ) {
            if ( !lines[ line].trim().equals( "" ) ) {
                MTypedSynonymCluster cluster = new MTypedSynonymCluster( lines[ line] );
                synonymClusters.add( cluster );
            }
        }
    }

    /**
     * Should show what the file shows, with all converted to lower case and
     * some pretty spacing that the input process will trim off.
     *
     * @return a comma-separated synonym line, all converted to lower case. If
     * there's a type, it appears first on the line followed by a ":".
     */
    @Override
    public String toString() {

        StringBuilder out = new StringBuilder( "" );
        for ( MTypedSynonymCluster s : synonymClusters ) {
            out.append( s.toString() ).append( "\n");
        }
        return out.toString();
    }

    /**
     * Create an HTML version of this synonym group.
     *
     * @param showAllSets whether to show all sets or not
     * @return a properly delimited HTML string.
     */
    public String formatAsHTML( boolean showAllSets ) {
        String count;
        if ( synonymClusters.isEmpty() ) {
            count = "No synonyms present.";
        } else {
            count = synonymClusters.size() + " set(s) of synonyms:";
        }
        StringBuilder s = new StringBuilder( Tag.p( Tag.bold( originDescription + ":<br>" ) + count ) );
        if ( showAllSets ) {
            for ( MTypedSynonymCluster cluster : synonymClusters ) {
                s.append( cluster.toString() ).append( "<br>" );
            }
        }
        return s.toString();
    }

    /**
     * Read the synonymClusters from a comma-separated synonym file and parse
 correctly. Clears the synonymClusters before loading.
     *
     * @param f File from which synonymClusters are read. Becomes the synonym set's
 synonymFile.
     */
    public void loadFromFile( File f ) {
        synonymFile = f;
        String lines = "";
        if ( !f.exists() ) {
            return;
        }
        try {
            int linecount = 0;
            BufferedReader in = new BufferedReader( new FileReader( f ) );
            String line = in.readLine();
            while ( line != null && line.length() > 0 ) {
                lines = lines + line + "\n";
                line = in.readLine();
                linecount++;
            }
            if ( linecount > 0 ) {
                Global.info( "Loading SYNONYMS: " + linecount + " line(s) from file " + f.getAbsolutePath() );
            }
            in.close();
        } catch ( Exception e ) {
            Global.error( "loadSynonymsFromFile: " + e.getMessage() );
        }
        fromString( lines );
    }

    /**
     * Writes the synonym text to the synonym file.
     */
    public void writeCollectionToFile() {
        if ( synonymFile != null ) {
            try {
                BufferedWriter out = new BufferedWriter( new FileWriter( synonymFile ) );
                out.write( toString() );
//                  Global.info( "writeCollectionToFile " + synonymFile.getAbsolutePath() + "\n" + toString() );
                out.close();
            } catch ( Exception e ) {
                Global.error( "writeCollectionToFile: " + e.getMessage() );
            }
        }
    }

    /**
     * Get the file that associated with this synonym collection.
     *
     * @return the file that associated with this synonym collection.
     */
    public File getSynonymFile() {
        return synonymFile;
    }

    /**
     * Set the file that associated with this synonym collection.
     */
    public void setSynonymFile( File synonymFile ) {
        this.synonymFile = synonymFile;
    }

    /**
     * Looks for any term that appears in more than one set with the same type
     * or else appears in a cluster with no type at all (i.e., would match any type)
     *
     * @return The list of terms that have duplicates. One entry per duplicate.
     * The list is empty if there are no duplicates.
     */
    public ArrayList<String> findDuplicates() {
        ArrayList<String> dups = new ArrayList<>();
        // for every set, get each of its terms and compare with all other sets.
        for ( MTypedSynonymCluster firstcluster : synonymClusters ) {
            for ( String s1 : firstcluster.terms ) {
                for ( MTypedSynonymCluster secondcluster : synonymClusters ) {
                    if ( firstcluster != secondcluster && firstcluster.getType().equalsIgnoreCase( secondcluster.getType() ) ) {
                        if ( secondcluster.containsIgnoreCase( s1 ) ) {
                            if ( !dups.contains( s1.toLowerCase() ) ) {
                                dups.add( s1.toLowerCase() );
                            }
                        }
                    }
                }
            }
        }
        return dups;
    }
}
