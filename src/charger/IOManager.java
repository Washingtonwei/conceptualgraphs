package charger;

import charger.act.GraphUpdater;
import charger.exception.CGFileException;
import charger.exception.CGStorageError;
import charger.exception.CGSyntaxException;
import charger.obj.GEdge;
import charger.obj.GNode;
import charger.obj.Graph;
import charger.obj.GraphObjectID;
import charger.obj.GraphObject;
import charger.util.Util;
import charger.xml.CGXParser;
import de.erichseifert.vectorgraphics2d.EPSGraphics2D;
import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
import de.erichseifert.vectorgraphics2d.SVGGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import kb.ObjectHistoryEvent;
//import java.beans.XMLEncoder;
//import java.beans.XMLDecoder;

/*
 CharGer - Conceptual Graph Editor
 Copyright reserved 1998-2014 by Harry S. Delugach
        
 This package is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version. This package is 
 distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 details. You should have received a copy of the GNU Lesser General Public
 License along with this package; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 * Responsible for file input/output for the CG system. Parses graph information
 * in a string (in "CharGer" format), then inserts the new graph objects. Since
 * each new object requires a new/unique ID number, IOManager must keep track of
 * both the old ID (to determine what an object was linked to in the string's
 * graphs) and the new ID (to determine how to link the object in its new
 * incarnation here). Implemented as a class because there may be reason to be
 * loading more than one frame at a time.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class IOManager {


    /**
     * This table is used by IOManager instances to maintain an old-new mapping.
     * Key is a string representing the old ID (in String form), value is the
     * new ID (in String form)
     */
    static Hashtable OldNewMapping = new Hashtable( 10 );
    protected JFrame ownerFrame = null;
    /**
     * Holds the list of image formats supported by the current image writer
     */
    public static ArrayList<String> imageFormats = new ArrayList<String>();

    public static class TransferableImage implements Transferable {

        private BufferedImage image = null;
        private DataFlavor[] flavors = { DataFlavor.imageFlavor };      // dummy for now
        private DataFlavor customFlavor = null;     // not used

        /*public TransferableImage( String imageType )
         {
         //customFlavor = new DataFlavor( "image/" + imageType, imageType );
         customFlavor = new DataFlavor( "image/jpeg", "JPEG" );
         }
         */
        public void setImage( BufferedImage bi ) {
            image = bi;
        }

        public Object getTransferData( DataFlavor flavor ) {
            if ( flavor != DataFlavor.imageFlavor ) {
                Global.error( "Wrong flavor for Transferable Image" );
                return null;
            } else {
                return image;
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            //DataFlavor[] flavors = { customFlavor };
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            // should eventually use searching the flavors array, but for testing, just compare
            if ( flavor.equals( DataFlavor.imageFlavor ) ) {
                return true;
            }
            //if ( flavor.equals( customFlavor ) ) return true;
            return false;
        }
    }

    static class ImageTransferable implements Transferable {

        private Image image;

        public ImageTransferable( Image image ) {
            this.image = image;
        }

        public Object getTransferData( DataFlavor flavor )
                throws UnsupportedFlavorException {
            if ( isDataFlavorSupported( flavor ) ) {
                return image;
            } else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            return flavor == DataFlavor.imageFlavor;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ DataFlavor.imageFlavor };
        }
    }

    public IOManager( JFrame f ) {
        ownerFrame = f;

        initializeImageFormatList();
    }

//    /**
//     * Creates a lookup that helps parsers determine (from an XML tag) what
//     * class is required. This could probably be replaced with a simple
//     * Encode/Decode XML "XML serialized" version.
//     *
//     */
//    public static void loadCharGerKeyWordToClassTable() {
//        CharGerXMLQNameToClassName.setProperty( "Graph", "Graph" );
//        CharGerXMLQNameToClassName.setProperty( "Concept", "Concept" );
//        CharGerXMLQNameToClassName.setProperty( "Relation", "Relation" );
//        CharGerXMLQNameToClassName.setProperty( "Actor", "Actor" );
//        CharGerXMLQNameToClassName.setProperty( "TypeLabel", "TypeLabel" );
//        CharGerXMLQNameToClassName.setProperty( "CGType", "TypeLabel" );
//        CharGerXMLQNameToClassName.setProperty( "RelationLabel", "RelationLabel" );
//        CharGerXMLQNameToClassName.setProperty( "CGRelType", "RelationLabel" );
//        CharGerXMLQNameToClassName.setProperty( "Arrow", "Arrow" );
//        CharGerXMLQNameToClassName.setProperty( "GenSpecLink", "GenSpecLink" );
//        CharGerXMLQNameToClassName.setProperty( "Coref", "Coref" );
//        CharGerXMLQNameToClassName.setProperty( "Note", "Note" );
//
//        CharGerXMLQNameToClassName.setProperty( "graph", "Graph" );
//        CharGerXMLQNameToClassName.setProperty( "concept", "Concept" );
//        CharGerXMLQNameToClassName.setProperty( "relation", "Relation" );
//        CharGerXMLQNameToClassName.setProperty( "actor", "Actor" );
//        CharGerXMLQNameToClassName.setProperty( "typelabel", "TypeLabel" );
//        CharGerXMLQNameToClassName.setProperty( "relationlabel", "RelationLabel" );
//        CharGerXMLQNameToClassName.setProperty( "arrow", "Arrow" );
//        CharGerXMLQNameToClassName.setProperty( "genspeclink", "GenSpecLink" );
//        CharGerXMLQNameToClassName.setProperty( "coref", "Coref" );
//        CharGerXMLQNameToClassName.setProperty( "customedge", "CustomEdge" );
//        CharGerXMLQNameToClassName.setProperty( "note", "Note" );
//    }

    /**
     * Loads from a cgx file to a graph. Does error checking on the filename,
     * graph, etc.
     *
     * @param absFile contains the absolute non-null File from which graph is to
     * be loaded
     * @param targetGraph	The graph to which the loaded objects will be added
     * @param translateBy The offset on the canvas by which the new parsed
     * objects are translated. If null, then all layout information is ignored.
     * If (0,0), perhaps the two-argument version should be used.
     * @return file object if the load succeeded, otherwise throw
     * CGFileException
     * @see IOManager#loadGraph3
     * @see IOManager#FileToGraph
     */
    public synchronized static File FileToGraph( File absFile, Graph targetGraph, Point2D.Double translateBy )
            throws CGFileException, CGStorageError {
        if ( targetGraph == null ) {
            throw new CGStorageError( "Needs an allocated graph" );
        }

        String versionToRead = null;		// valid values are "2.x" and "3.x"

        File sourceAbsoluteFile = absFile;

        FileInputStream fis = null;
        BufferedReader in = null;

        // now have a valid file name, ready to load
        try {
            // Global.info( "Graph iomgr about to load file \"" + sourceAbsoluteFile.getPath() + "\"." );
            fis = new FileInputStream( sourceAbsoluteFile );
            in = new BufferedReader( new InputStreamReader( fis ) );
            // here is where we decide which version we're reading
            if ( in == null || sourceAbsoluteFile.length() == 0 ) {
                throw new CGFileException( "Can't open file \"" + sourceAbsoluteFile.getAbsolutePath() + "\"" );
            } else {
//                in.mark( 200 );
                String s = in.readLine();
                StringTokenizer toks = new StringTokenizer( new String( s ), "|" );
                String token1 = new String( toks.nextToken() );
                //Global.info( "first token in file is " + token1 );
                if ( token1.startsWith( "<?xml version=" ) ) //charger.xml.XMLGenerator.XMLHeader() ) )
                {
                    s = in.readLine();
                    if ( !s.startsWith( "<conceptualgraph" ) ) {
                        throw new CGFileException( "Not a conceptualgraph XML file.\nExpecting content to start with "
                                + "<conceptualgraph" + "\but instead it says: " + s );
                    } else {
                        in.close();		// let the parser do the opening, etc.
                        versionToRead = "XML";
                    }
                } else if ( !token1.equals( "CharGer" ) ) // one of the old versions; "reset" the stream
                {
                    //in.close();
                    //in  = new BufferedReader(new InputStreamReader( fis ));
                    in.reset();
                    versionToRead = "2.x";
                } else if ( token1.equals( "CharGer" ) ) {
                    token1 = new String( toks.nextToken() );
                    StringTokenizer propertyPairs = new StringTokenizer( token1, "=" );
                    String propertyName = new String( propertyPairs.nextToken() );
                    if ( propertyName.equals( "version" ) ) {
                        versionToRead = new String( propertyPairs.nextToken() );
                    }
                }
            }
            // input stream is positioned to read the first real graph data
            if ( versionToRead.equals( "XML" ) ) {
                loadGraphXML( sourceAbsoluteFile, targetGraph, translateBy );
                in.close();
            } else if ( versionToRead.equals( "2.x" ) ) {
                loadGraph2( in, targetGraph );
                in.close();
            } else if ( versionToRead.startsWith( "3.0" ) || versionToRead.startsWith( "3.1" ) ) {
                loadGraph3( in, targetGraph );
                in.close();
            }

//            Global.info( "Graph iomgr finished loading file \"" + sourceAbsoluteFile.getPath() + "\"." );
        } catch ( CGFileException e ) {
            //Hub.warning( "exception generated by graph iomgr on file \"" + path + fname + "\"." );
            //e.printStackTrace();
            throw new CGFileException( sourceAbsoluteFile + ":\n" + e.getMessage() );
        } catch ( IOException ie ) {
            throw new CGFileException( sourceAbsoluteFile + ": " + ie.getMessage() );
        }

//        OperManager.performActionValidate( targetGraph );

        if ( Global.enableActors ) {
            //GraphUpdater.updateGraph( targetGraph );
            GraphUpdater gu = new GraphUpdater( null, targetGraph );
            new Thread( gu ).start();
        }

        //Global.info( "successfully finished FileToGraph with " + sourceAbsoluteFile.getPath() );
        return sourceAbsoluteFile; //.getPath();	
        // needs to eventually become a File parameter
    }

    /**
     * Loads a graph from a file to a graph. Does error checking on the
     * filename, graph, etc.
     *
     * @param absFile contains fully-qualified file name from which graph is to
     * be loaded
     * @param targetGraph	The graph to which the loaded objects will be added
     * @return filename if the load succeeded, otherwise throw CGFileException
     * @see IOManager#loadGraph3
     */
    public synchronized static File FileToGraph( File absFile, Graph targetGraph )
            throws CGFileException, CGStorageError {
        File f = null;
        try {
            f = FileToGraph( absFile, targetGraph, new Point2D.Double( 0, 0 ) );
        } catch ( CGFileException x ) {
            throw x;
        } catch ( CGStorageError x ) {
            throw x;
        }
        return f;
    }

//    public synchronized static File CGIFFileToGraph( File absFile, charger.obj.Graph targetGraph,/*Frame owner,*/ Point2D.Double translateBy )
//            throws CGFileException, CGStorageError {
////        File cgifFile = Util.queryForInputFile( "", null, absFile, null );
////        CGIFParser parser = new CGIFParser( (Reader)null );
////        Graph g = parser.parseCGIFString( contents );
////
////        throw new CGFileException( "CGIFFileToGraph is broken, pending incorporation of real CGIF into Charger." );
//    }
    /**
     * CURRENTLY BROKEN! Loads a graph from a file to a graph. Does error
     * checking on the filename, graph, etc.
     *
     * @param absFile contains file name from which graph is to be loaded; if
     * null, then use an open dialog
     * @param targetGraph	The graph to which the loaded objects will be added
     * @param translateBy The offset by which the new parsed objects are
     * translated
     * @return filename if the load succeeded, otherwise throw CGFileException
     * @see IOManager#loadGraph3
     * @see IOManager#FileToGraph
     */
//    public File CGIFFileToGraphBROKEN( File absFile, charger.obj.Graph targetGraph,/*Frame owner,*/ Point2D.Double translateBy )
//            throws CGFileException, CGStorageError {
//        if ( targetGraph == null ) {
//            throw new CGStorageError( "needs an allocated graph" );
//        }
//
//        File sourceAbsFile = absFile;
//
//        FileInputStream fis = null;
//        BufferedReader in = null;
//
//        JFileChooser filechooser = new JFileChooser( Hub.GraphFolderFile.getAbsolutePath() );
//        filechooser.setDialogTitle( "Open existing CGIF file" );
//        filechooser.setFileFilter( new javax.swing.filechooser.FileFilter() {
//            public boolean accept( File f ) {
//                if ( f.isDirectory() ) {
//                    return true;
//                }
//                return Hub.acceptCGIFFileName( f.getName() );
//            }
//
//            public String getDescription() {
//                return "CG Interchange (*.CGF)";
//            }
//        } );
//
//        int returned = filechooser.showOpenDialog( Hub.CharGerMasterFrame );
//
//        // if approved, then continue
//        if ( returned == JFileChooser.APPROVE_OPTION ) {
//            sourceAbsFile = filechooser.getSelectedFile();
//        } else {
//            return null;
//        }
//
//        try {
//            //Global.info( "About to try to load file \"" + sourceAbsFile.getAbsolutePath() + "\"." );
//            fis = new FileInputStream( sourceAbsFile );
//            in = new BufferedReader( new InputStreamReader( fis ) );
//            if ( in != null ) {
//                try {
//                    //notio.KnowledgeBase kb = new notio.KnowledgeBase();
//                    notio.translators.CGIFParser parser = new notio.translators.CGIFParser();
//                    //  parser.initializeParser(in, Hub.KB, // NotioTrans.makeNewKnowledgeBase( targetGraph.nxGraph ), 
//                    //          new notio.TranslationContext());
//                    notio.Graph ng = parser.parseOutermostContext();
//                    // would be nice to make sure that ng is okay here
//                    // REMOVE-NOTIO Global.info("\n ---        showing parsed notio graph");
//                    // REMOVE-NOTIO Global.info(Ops.showWholeNotioGraph(ng, true));
//                    //targetGraph = NotioTrans.notioGraphToCharGer(ng, targetGraph);
//                    // REMOVE-NOTIO Global.info(targetGraph.showCounterpartTable(0));
//                } catch ( notio.ParserException pe ) {
//                    Hub.error( "parser exception: " + sourceAbsFile.getAbsolutePath() + ": " + pe.getMessage() );
//                    throw new CGFileException( "Error in processing file \n"
//                            + sourceAbsFile.getAbsolutePath() + "  \nSyntax error or not a CGIF file.\n"
//                            + pe.getMessage() );
//                }
//                in.close();
//            }
//        } catch ( IOException e ) {
//            Global.info( "exception generated by graph iomgr on file \"" + sourceAbsFile.getAbsolutePath() + "\"." );
//            throw new CGFileException( e.getMessage() );
//        }
//
//        if ( Hub.enableActors ) {
//            GraphUpdater gu = new GraphUpdater( null, targetGraph );
//            new Thread( gu, targetGraph.getTextLabel() ).start();
//        }
//        return sourceAbsFile;
//    }
    /**
     * Loads (reads) a graph from a file in CharGer version 2 format and
     * attaches it to an existing (i.e., non-null, but possibly empty) graph
     *
     * @param f An already-opened file object pointing to a CharGer formatted
     * version of the graph desired
     * @see IOManager#saveGraph2
     * @param targetGraph An already existing graph to which the new contents
     * will be attached
     * @param translateBy An x,y offset by which the loaded objects will be
     * translated.
     */
    protected synchronized static void loadGraph2( BufferedReader f, Graph targetGraph, Point translateBy )
            throws CGFileException, CGSyntaxException {
        Global.info( "loading version 2 graph ...." );
        OldNewMapping.clear();
        int line = 0;
        // ArrayList v = new ArrayList( 10 );
        GraphObject go = null;
        try {
            String s = f.readLine();
            line++;
            while ( s != null && !s.equals( "\\\\" ) ) {
                Global.info( "parsing (v2): " + s );
                go = parseOneObject2( s, targetGraph, false, translateBy, false );
                //if (go != null) v.add( go );
                s = f.readLine();
                line++;
            }
        } catch ( CGSyntaxException e ) {
            throw new CGSyntaxException( "Syntax error: " + e.getMessage() + "\n at line " + line );
        } catch ( IOException ie ) {
            throw new CGFileException( "IO Exception: " + ie.getMessage() );
        }
        // Global.info( "Finished with loadGraph ...." );
        return;
    }

    /**
     * Loads (reads) a graph from a file and attaches it to an existing (i.e.,
     * non-null, but possibly empty) graph
     *
     * @param f A text file containing a text version of the graph desired
     * @see IOManager#GraphToFile
     * @param targetGraph An already existing graph to which the new contents
     * will be attached
     */
    protected synchronized static void loadGraph2( BufferedReader f, Graph targetGraph )
            throws CGFileException {
        try {
            loadGraph2( f, targetGraph, new Point( 0, 0 ) );
        } catch ( Throwable e ) {
            throw new CGFileException( e.getMessage() );
        };
    }

    /**
     * Loads (reads) a graph from a file and attaches it to an existing (i.e.,
     * non-null, but possibly empty) graph, using the "new" version 3 format
     *
     * @param f An already-opened file object pointing to a CharGer formatted
     * version of the graph desired
     * @see IOManager#saveGraph2
     * @param targetGraph An already existing graph to which the new contents
     * will be attached
     * @param translateBy An x,y offset by which the loaded objects will be
     * translated.
     */
    protected synchronized static void loadGraph3( BufferedReader f, Graph targetGraph, Point translateBy )
            throws CGFileException {
        Global.info( "loading version3 graph..." );
        OldNewMapping.clear();
        GraphObject go = null;
        try {
            String s = f.readLine();
            while ( s != null && !s.equals( "\\\\" ) ) {
                //Global.info( "parsing (v3): " + s );
                go = parseOneObject( s, targetGraph, false, translateBy, false );
                //if (go != null) v.add( go );
                s = f.readLine();
            }
        } catch ( IOException e ) {
            throw new CGFileException( "IO Exception parsing one object: " + go + " " + e.getMessage() );
        }
        // Global.info( "Finished with loadGraph ...." );
        return;
    }

    /**
     * Loads (reads) a graph from a file and attaches it to an existing (i.e.,
     * non-null, but possibly empty) graph
     *
     * @param f A text file containing a text version of the graph desired
     * @see IOManager#GraphToFile
     */
    protected synchronized static void loadGraph3( BufferedReader f, Graph targetGraph ) throws CGFileException {
        try {
            loadGraph3( f, targetGraph, new Point( 0, 0 ) );
        } catch ( Throwable e ) {
            throw new CGFileException( e.getMessage() );
        };
    }

    /**
     * Loads (reads) a graph from a file and attaches it to an existing (i.e.,
     * non-null, but possibly empty) graph, using the XML version 3.0 format
     *
     * @param f An already-opened file object pointing to a XML formatted
     * version of the graph desired
     * @see IOManager#saveGraph38XML
     * @param targetGraph An already existing graph to which the new contents
     * will be attached
     * @param translateBy An x,y offset by which the loaded objects will be
     * translated. <b>If null, then ignore any layout information.</b>. This is
     * not the same as translating by (0,0).
     */
    protected synchronized static void loadGraphXML( File f, Graph targetGraph, Point2D.Double translateBy ) {
        //charger.obj.Graph resultGraph = null;
        OldNewMapping.clear();
        CGXParser parser = null;
        if ( Global._useDOMParser ) {
            try {
                CGXParser.parseForNewGraph( new FileInputStream( f ), targetGraph );
                ObjectHistoryEvent he = new ObjectHistoryEvent( f );
                targetGraph.addHistory( he );

            } catch ( FileNotFoundException ex ) {
                Global.warning( ex.getMessage() );
                return;
            }
        } else {
//            parser = new CGXParser();
        }
        if ( translateBy == null ) {
            parser.setIgnoreLayout( true );
        }
//        // Global.info( "loading xml version graph..." );
//        if ( Global._useDOMParser ) {
////            targetGraph = parser.getParsedGraph();      // can't change pass by value the reference to target graph
//        } else {
////            parser.parseCGXMLFile( f, targetGraph );
//        }
        OperManager.performActionValidate( targetGraph );
    }

    /**
     * Loads (reads) a graph from a file and attaches it to an existing (i.e.,
     * non-null, but possibly empty) graph
     *
     * @param f A text file containing a text version of the graph desired
     * @see IOManager#GraphToFile
     */
    protected synchronized static void loadGraphXML( File f, Graph target ) throws CGFileException {
        loadGraphXML( f, target, new Point2D.Double( 0, 0 ) );
    }

    /**
     * Loads a graph from a CharGer formatted string
     *
     * @param s String containing one or more lines of parsable text
     * @param targetGraph The graph in which to store the found objects
     * @param translateBy The x,y coordinates to use in translating the parsed
     * objects
     * @param keepIDs Whether to keep existing IDs or create new ones
     * @return list of all the new objects it parsed
     * @see IOManager#loadGraph3
     */
    public static ArrayList StringToGraph( String s, Graph targetGraph, Point translateBy, boolean keepIDs )
            throws CGStorageError {
        ArrayList v = new ArrayList( 10 );
        if ( targetGraph == null ) {
            throw new CGStorageError( "needs an allocated graph" );
        }
        StringTokenizer textedges = new StringTokenizer( s, Global.LineSeparator );
        String oneline = null;
        GraphObject go = null;
        while ( textedges.hasMoreTokens() ) {
            oneline = new String( new String( textedges.nextToken() ) );
            go = parseOneObject( oneline, targetGraph, false, translateBy, keepIDs );
            if ( go != null ) {
                v.add( go );
            }
        }
        targetGraph.resizeForContents( null );
        return v;
    }

    /**
     * Tokenizes and parses a string assuming it was written using the saveGraph
     * rules for version 2, then creates a correct CharGer graph object based on
     * what was parsed.
     *
     * @param s a CharGer object string representing some graph object (not
     * including a line.separator)
     * @param rootGraph an already-allocated (empty or non-empty) graph node
     * that will be filled in
     * @param ignoreNesting disregard any owner graph information in the string;
     * i.e., don't try to connect this object to an owning graph.
     * @param translateBy an amount to translate the parsed object's position
     * @param keepIDs whether to keep the internal IDs or create new ones
     * @return the single graph object parsed; if a new object, then we need to
     * know that!!!
     * @see #saveGraph2
     */
    public static GraphObject parseOneObject2(
            String s, Graph rootGraph, boolean ignoreNesting, Point translateBy, boolean keepIDs )
            throws CGSyntaxException {
        if ( keepIDs ) {
            Global.error( "can't keep ID's when reading a version 2 graph!" );
            return null;
        }
        Global.info( "parseOneObject (v2): " + s );
        StringTokenizer toks = new StringTokenizer( s, "|" );
        StringTokenizer nums = null;	// used for substring tokenizing
        String token1 = null;
        GraphObject go = null; // to be used for whatever object is to be loaded

        token1 = new String( toks.nextToken() );
        if ( token1.equals( "\\\\" ) ) {
            return null;
        }

        String t = CGXParser.CharGerXMLTagNameToClassName.getProperty( token1, "DUMMY" );

        Graph newOwnerGraph = null;		// the new (possibly nested) graph into which objects are placed.
        if ( t.equals( "Graph" ) ) {
            go = new Graph( null );
        } // if the root node, we'll trash this.
        // GNodes
        else if ( t.equals( "Concept" ) ) {
            go = new charger.obj.Concept();
        } else if ( t.equals( "Relation" ) ) {
            go = new charger.obj.Relation();
        } else if ( t.equals( "Actor" ) ) {
            go = new charger.obj.Actor();
        } else if ( t.equals( "TypeLabel" ) ) {
            go = new charger.obj.TypeLabel();
        } else if ( t.equals( "RelationLabel" ) ) {
            go = new charger.obj.RelationLabel();
        } // GEdges
        else if ( t.equals( "Arrow" ) ) {
            go = new charger.obj.Arrow();
        } else if ( t.equals( "GenSpecLink" ) ) {
            go = new charger.obj.GenSpecLink();
        } else if ( t.equals( "Coref" ) ) {
            go = new charger.obj.Coref();
        } else if ( t.equals( "DUMMY" ) ) {
            //Hub.error( "Syntax error: Found token \"" + token1 + "\" which isn't recognized." );
            throw new CGSyntaxException( "Unrecognized token \"" + token1 + "\"" );
        }

        // decide whether to keep old ident's (easy) or create new ones (harder)
        // handle IDs -------
        nums = new StringTokenizer( new String( toks.nextToken() ), "," );
        String oldID = new String( nums.nextToken() );	// get the old object ident as a string
        String oldOwnerID = new String( nums.nextToken() );		// get the old graph ident as a string

        if ( !ignoreNesting ) {
            if ( oldOwnerID.equals( GraphObjectID.zero ) ) {
                go = rootGraph;		// garbages the just-allocated graph object, but can't be helped
            }

            OldNewMapping.put( oldID, go.objectID );

            // handle linking to appropriate graph
            // already checked to see if it happens to be the root graph root node
            if ( !oldOwnerID.equals( GraphObjectID.zero ) ) {
                String newOwnerAsString = (String)OldNewMapping.get( oldOwnerID );
                if ( newOwnerAsString == null ) {
                    newOwnerGraph = rootGraph;
                } else {
                    newOwnerGraph = (Graph)rootGraph.findByID( new GraphObjectID( newOwnerAsString ) );
                }
                if ( newOwnerGraph == null ) {
                    Global.info( "IOManager: can't find owner for stored object." );
                } else {
                    newOwnerGraph.insertObject( go );
                }
            }
        }

        // handle textLabel -----
        String textLabel = new String( toks.nextToken() );

        // handle position and size
        nums = new StringTokenizer( new String( toks.nextToken() ), "," );
        try {
            go.displayRect.x = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.y = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.width = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.height = Integer.parseInt( new String( nums.nextToken( "," ) ) );

            nums = new StringTokenizer( new String( toks.nextToken() ), "," );
            go.displayRect.x = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.y = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.width = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.height = Integer.parseInt( new String( nums.nextToken( "," ) ) );

        } catch ( NumberFormatException e ) {
            // should probably throw its own exception
            Global.error( "load encountered a bad dimension for an object." );
        }

        go.displayRect.setRect( go.displayRect.x + translateBy.x, go.displayRect.y + translateBy.y, go.displayRect.width, go.displayRect.height );
        Point2D.Double p = go.getCenter();
        p.setLocation( p.x + translateBy.x, p.y + translateBy.y );
        go.setCenter( p );

        go.setTextLabel( textLabel );
        Global.info( "set text label as " + textLabel );

        /*	if ( go.myKind == GraphObject.GNODE ) 
         {
         //   ((GNode) go).setCenter( go.getCenter() );
         ((GNode) go).setTextLabel( go.getTextLabel() );
         }
         else if ( go.myKind == GraphObject.GRAPH ) 
         {
         ((charger.obj.Graph)go).setTextLabel( go.getTextLabel() );
         }
         */
        // unless it's a GEdge, we're done with loading this object
        // if ( go.getClass().getSuperclass().getName().equals( "GEdge" ) ) {
        if ( go.myKind == GraphObject.Kind.GEDGE ) {
            // handle relationships using the OldNewMapping
            // algorithm is:
            //   for each endpoint node
            //		look up old ident
            //		find new ident
            //		find new node by its ident
            //		store new ident to appropriate node pointer in the GEdge
            //		attachGEdge to the new node

            nums = new StringTokenizer( new String( toks.nextToken() ) );

            GraphObjectID fromID, toID;
            fromID = new GraphObjectID( new String( nums.nextToken( "," ) ) );
            toID = new GraphObjectID( new String( nums.nextToken( "," ) ) );

            // pass in the rootGraph here, because GEdges may cross context boundaries
            ( (GEdge)go ).fromObj = hookUp( (GEdge)go, fromID, rootGraph, keepIDs );
            ( (GEdge)go ).toObj = hookUp( (GEdge)go, toID, rootGraph, keepIDs );
            if ( ( (GEdge)go ).fromObj == null || ( (GEdge)go ).toObj == null ) {
                // parsing a partial graph could lead to this
                go.getOwnerGraph().forgetObject( go );
                return null;
            } else {
                //if ( go instanceof Arrow ) Arrow.MakeArrowForNotio( (Arrow)go );
                //if ( go instanceof Coref ) Coref.MakeCorefForNotio( (Coref)go );
                ( (GEdge)go ).placeEdge();
            }
        }
        return go;
    }

    /**
     * Tokenizes and parses a string assuming it was written using the
     * saveGraph2 rules, then creates a correct CharGer graph object based on
     * what was parsed. Used when reading in version 3 graphs from a file and
     * when copying/pasting internally.
     *
     * @param s a CharGer object string representing some graph object (not
     * including a line.separator)
     * @param rootGraph an already-allocated (empty or non-empty) graph node
     * that will be filled in
     * @param ignoreOwner whether to consider the object's owner in creating the
     * object
     * @param translateBy an amount to translate the parsed object's position
     * @param keepIDs whether to keep the existing IDs or create new ones
     * @return the single graph object parsed; if a new object, then we need to
     * know that!!!
     * @see #saveGraph2
     */
    public static GraphObject parseOneObject(
            String s, Graph rootGraph, boolean ignoreOwner, Point translateBy, boolean keepIDs ) {
        if ( CGXParser.CharGerXMLTagNameToClassName.isEmpty() ) {
            CGXParser.loadCharGerKeyWordToClassTable();
        }
        //Global.info( "parseOneObject (v3+): " + s );
        StringTokenizer toks = new StringTokenizer( s, "|" );
        StringTokenizer nums = null;	// used for substring tokenizing
        String token1 = null;
        GraphObject go = null; // to be used for whatever object is to be loaded

        token1 = new String( toks.nextToken() );
        if ( token1.equals( "\\\\" ) ) {
            return null;
        }

        String t = CGXParser.CharGerXMLTagNameToClassName.getProperty( token1, "DUMMY" );

        Graph newOwnerGraph = null;		// the new (possibly nested) graph into which objects are placed.
        if ( t.equals( "Graph" ) ) {
            go = new Graph( null );
        } // if the root node, we'll trash this.
        // GNodes
        else if ( t.equals( "Concept" ) ) {
            go = new charger.obj.Concept();
        } else if ( t.equals( "Relation" ) ) {
            go = new charger.obj.Relation();
        } else if ( t.equals( "Actor" ) ) {
            go = new charger.obj.Actor();
        } else if ( t.equals( "TypeLabel" ) ) {
            go = new charger.obj.TypeLabel();
        } else if ( t.equals( "RelationLabel" ) ) {
            go = new charger.obj.RelationLabel();
        } // GEdges
        else if ( t.equals( "Arrow" ) ) {
            go = new charger.obj.Arrow();
        } else if ( t.equals( "GenSpecLink" ) ) {
            go = new charger.obj.GenSpecLink();
        } else if ( t.equals( "Coref" ) ) {
            go = new charger.obj.Coref();
        } else if ( t.equals( "CustomEdge" ) ) {
            go = new charger.obj.CustomEdge();
        } else if ( t.equals( "DUMMY" ) ) {
            Global.error( "Syntax error: Found token \"" + token1 + "\" which isn't recognized." );
        }

        // if keepIDs is false, then objects are to be added to the graph with a new ID
        //  objects have an old ID that has to be converted/mapped to a new one.
        // handle IDs -------
        nums = new StringTokenizer( new String( toks.nextToken() ), "," );
        String oldIDString = new String( nums.nextToken() );	// get the old object ident as a string
        String oldOwnerIDString = new String( nums.nextToken() );		// get the old graph ident as a string

        // If this is the root graph, don't worry about ownership 
        if ( oldOwnerIDString.equals( GraphObjectID.zero.toString() ) ) {
            go = rootGraph;		// garbages the just-allocated graph object, but can't be helped
        }

        String newOwnerIDString = null;
        if ( keepIDs ) {
            newOwnerIDString = oldOwnerIDString;
            go.objectID = new GraphObjectID( oldIDString );
            // WARNING: over-writing the new ident directly here, and replacing it with the old onne
            // no ident check or wrapper !
        } else {
            OldNewMapping.put( oldIDString, go.objectID );
            if ( oldOwnerIDString.equals( GraphObjectID.zero.toString() ) ) {
                newOwnerIDString = GraphObjectID.zero.toString();
            } else {
                newOwnerIDString = (String)OldNewMapping.get( oldOwnerIDString );
            }
        }
        //Global.info( "old, new ID's: " + oldIDString + ", " + go.objectID );
        //Global.info( "old, new owner ident strings: " + oldOwnerIDString + ", " + newOwnerIDString );

        /* if we can't find an owner, make the rootgraph the owner
         really a bad move: what if we paste it into a context? shouldn't the context be the owner?
         */
        // set new owner ident and owner graph
        GraphObjectID newOwnerID = GraphObjectID.zero;

        if ( newOwnerIDString == null ) // can't find an owner, perhaps it's in another graph
        {
            newOwnerID = rootGraph.objectID;
            newOwnerGraph = rootGraph;
        } else if ( newOwnerIDString.equals( GraphObjectID.zero ) ) // this is the top level graph
        {
            newOwnerID = GraphObjectID.zero;
            newOwnerGraph = null;
        } else {
            newOwnerID = new GraphObjectID( newOwnerIDString );
            newOwnerGraph = (Graph)rootGraph.findByID( newOwnerID );
        }

        if ( newOwnerGraph == null ) {
            Global.warning( go.getClass().getName() + ": can't find owner for stored object id " + newOwnerID );
        } else {
            newOwnerGraph.insertObject( go );
        }

        //Global.info( "new owner ident is " + newOwnerID );
        // handle textLabel -----
        String textLabel = new String( toks.nextToken() );

        // handle position and size
        nums = new StringTokenizer( new String( toks.nextToken() ), "," );
        try {
            go.displayRect.x = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.y = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.width = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.displayRect.height = Integer.parseInt( new String( nums.nextToken( "," ) ) );

            int r, g, b;

            nums = new StringTokenizer( new String( toks.nextToken() ), "," );
            r = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            g = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            b = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.foreColor = new Color( r, g, b );

            nums = new StringTokenizer( new String( toks.nextToken() ), "," );
            r = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            g = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            b = Integer.parseInt( new String( nums.nextToken( "," ) ) );
            go.backColor = new Color( r, g, b );

        } catch ( NumberFormatException e ) {
            // should probably throw its own exception
            Global.error( "load encountered a bad dimension for an object." );
        }

        go.displayRect.setRect( go.displayRect.x + translateBy.x, go.displayRect.y + translateBy.y, go.displayRect.width, go.displayRect.height );
        Point2D.Double p = go.getCenter();
        p.setLocation( p.x + translateBy.x, p.y + translateBy.y );
        go.setCenter( p );

        go.setTextLabel( textLabel );

        /*	if ( go.myKind == GraphObject.GNODE ) 
         {
         //   ((GNode) go).setCenter( go.getCenter() );
         ((GNode) go).setTextLabel( go.getTextLabel() );
         }
         else if ( go.myKind == GraphObject.GRAPH ) 
         {
         ((charger.obj.Graph)go).setTextLabel( go.getTextLabel() );
         }
		
         */
        // unless it's a GEdge, we're done with loading this object
        // if ( go.getClass().getSuperclass().getName().equals( "GEdge" ) ) {
        if ( go.myKind == GraphObject.Kind.GEDGE ) {
            // handle relationships using the OldNewMapping
            // algorithm is:
            //   for each endpoint node
            //		look up old ident
            //		find new ident
            //		find new node by its ident
            //		store new ident to appropriate node pointer in the GEdge
            //		attachGEdge to the new node

            nums = new StringTokenizer( new String( toks.nextToken() ) );

            GraphObjectID fromID, toID;
            fromID = new GraphObjectID( new String( nums.nextToken( "," ) ) );
            toID = new GraphObjectID( new String( nums.nextToken( "," ) ) );

            // pass in the rootGraph here, because GEdges may cross context boundaries
            ( (GEdge)go ).fromObj = hookUp( (GEdge)go, fromID, rootGraph, keepIDs );
            ( (GEdge)go ).toObj = hookUp( (GEdge)go, toID, rootGraph, keepIDs );
            if ( ( (GEdge)go ).fromObj == null || ( (GEdge)go ).toObj == null ) {
                // parsing a partial graph could lead to this
                go.getOwnerGraph().forgetObject( go );
                return null;
            } else {
                //if ( go instanceof Arrow ) Arrow.MakeArrowForNotio( (Arrow)go );
                //if ( go instanceof Coref ) Coref.MakeCorefForNotio( (Coref)go );
                ( (GEdge)go ).placeEdge();
            }
        }
        return go;
    }

    /**
     * Tokenizes and parses a Notio comment string assuming it was written using
     * makeCGIFcomment then creates a correct CharGer graph object based on what
     * was parsed.
     *
     * @param s a Notio comment string representing some graph object (not
     * including a line.separator)
     * @param preserveIDs whether to override the method's own generated new
     * unique numbers
     * @param translateBy an amount to translate the parsed object's position
     * @return the single graph object parsed; if a new object, then we need to
     * know that!!!
     * @see GraphObject#makeCGIFcomment
     */
//    public GraphObject parseNotioComment( String s, boolean preserveIDs, Point translateBy ) {
//        StringTokenizer toks = new StringTokenizer( s, "|" );
//        StringTokenizer nums = null;	// used for substring tokenizing
//        String t = null;
//        GraphObject go = null; // to be used for whatever object is to be loaded
//
//        t = new String( toks.nextToken() );
//
//        /* This code is a kludge to handle a bug in the CGIF parser!
//         Comments from CharGer should always match the kind of thing they're attached to.
//         The only exception is for a concept comment, which is sometimes attached to its 
//         enclosing graph. So we pass in a charger.obj.Graph object when we're sure that's what
//         we want -- so this method will ignore whatever the comment thinks.
//         */
//        if ( t.equals( "Graph" ) ) {
//            go = new charger.obj.Graph( null );
//        } // if the root node, we'll trash this.
//        // GNodes
//        else if ( t.equals( "Concept" ) ) {
//            go = new charger.obj.Concept();
//        } else if ( t.equals( "Relation" ) ) {
//            go = new charger.obj.Relation();
//        } else if ( t.equals( "Actor" ) ) {
//            go = new charger.obj.Actor();
//        } else if ( t.equals( "TypeLabel" ) ) {
//            go = new charger.obj.TypeLabel();
//        } else if ( t.equals( "RelationLabel" ) ) {
//            go = new charger.obj.RelationLabel();
//        }
//
//        // handle IDs -------
//        String ID = toks.nextToken();
//        if ( preserveIDs ) {
//            go.objectID = new GOID( ID );
//        }
//
//        // handle position and size
//        nums = new StringTokenizer( toks.nextToken(), "," );
//        try {
//            go.displayRect.x = Integer.parseInt( nums.nextToken( "," ) );
//            go.displayRect.y = Integer.parseInt( nums.nextToken( "," ) );
//            go.displayRect.width = Integer.parseInt( nums.nextToken( "," ) );
//            go.displayRect.height = Integer.parseInt( nums.nextToken( "," ) );
//
//        } catch ( NumberFormatException e ) {
//            // should probably throw its own exception
//            Hub.error( "load encountered a bad dimension for an object:" + e.getMessage() );
//        }
//        go.displayRect.setRect( go.displayRect.x + translateBy.x, go.displayRect.y + translateBy.y, go.displayRect.width, go.displayRect.height );
//        Global.info( "parseNotioComment displayRect for " + go.getTextLabel() + " is " + go.displayRect.toString() );
//        Point2D.Double p = go.getCenter();
//        p.setLocation( p.x + translateBy.x, p.y + translateBy.y );
//        go.setCenter( p );
//
//        return go;
//    }
    /**
     * uses the old-to-new mapping to attach an edge to its new ends
     *
     * @param ge the edge to be attached to its appropriate (new) nodes
     * @param oldID the object's ID assigned when the line's info was written
     * @param gr the (new) graph which will own the new edge
     * @param keepIDs whether to keep the old ident or lookup a new one
     */
    public static GNode hookUp( GEdge ge, GraphObjectID oldID, Graph gr, boolean keepIDs ) {
        //Global.info( "hooking up edge " + ge.getClass().getName() + " to object ident " + oldID );
        GraphObjectID newID = GraphObjectID.zero;
        if ( keepIDs ) {
            newID = oldID;
        } else {
            String newIDString = (String)OldNewMapping.get( oldID );
            if ( newIDString != null ) {
                newID = new GraphObjectID( newIDString );
            }
        }

        if ( !newID.equals( GraphObjectID.zero ) ) {
            GNode gn = (GNode)gr.findByID( newID );
            //Global.info( "  gnode to hook up is " + gn.objectID );
            gn.attachGEdge( ge );
            return gn;
        } else {
            return null;
        }
    }

    /**
     * Establish the absolute file for a given filename and its intended format.
     * If it's relative, use the Hub.GraphFolderFile value to determine the
     * absolute path.
     *
     * @param filename original filename, may be either absolute or relative
     * @param format used for replacing the extension of the given filename
     * @return an absolutely absolute file name
     * @see Global#GraphFolderFile
     */
    public synchronized static File getAbsoluteFile( String filename, FileFormat format ) {
        File initialChoiceFile = new File( Util.stripFileExtension( filename ) + "." + format.extension() );
        File initialDirectoryFile = new File( Global.GraphFolderFile.getAbsolutePath() );

        // if we have an absolute path, set directory from it, otherwise concat with graph folder 
        if ( initialChoiceFile.isAbsolute() ) {
            initialDirectoryFile = initialChoiceFile.getAbsoluteFile().getParentFile();
        } else {
            initialChoiceFile = new File( initialDirectoryFile, initialChoiceFile.getName() );
        }
        return initialChoiceFile;
    }

    /**
     * Runs the file dialog that lets the user choose the filename and location.
     * Checks to see if file already exists; if it does, the user must confirm.
     *
     * @param owner the frame that will own the modal dialog.
     * @param initialChoiceFile the initial file display information
     * @param promptstring The string to cue the user what is expected
     * @return thefile chosen by the user; null if none was chosen.
     */
    public synchronized static File chooseViaFileSaveDialog( Frame owner, File initialChoiceFile, String promptstring )
            throws CGFileException {
        File chosenOne = null;
        JFileChooser fc = new JFileChooser( initialChoiceFile );

        // construct the options panel
//        JPanel optionPanel = new JPanel();
//        optionPanel.setLayout( new BoxLayout( optionPanel, BoxLayout.Y_AXIS ) );
//        JCheckBox checkbox = new JCheckBox( "Include layout info", Hub.IncludeCharGerInfoInCGIF );  //CGIF-DISABLED
//
//        JLabel label1 = new JLabel( "     (set default in Preferences)" );
//        label1.setHorizontalAlignment( JLabel.TRAILING );
//
//        fc.setAccessory( optionPanel );
        fc.setOpaque( true );

//        String suggestedName = initialChoiceFile.getName();
        if ( owner != null ) {  // If there's a human user in front of the screen
            boolean fileOkay = false;
            fc.setDialogTitle( promptstring );
            fc.setSelectedFile( initialChoiceFile );
            while ( !fileOkay ) {
                int userAnswer = JOptionPane.CLOSED_OPTION;
//                    Global.info( "set initially chosen file to " + fc.getSelectedFile().getAbsolutePath() );
                //File dummy = fc.getSelectedFile();
                //fc.setSelectedFile( dummy );
                int returned = fc.showSaveDialog( owner );
                if ( returned == JFileChooser.APPROVE_OPTION ) {
                    chosenOne = fc.getSelectedFile();
                    if ( !chosenOne.getName().contains( "." ) ) {
                        JOptionPane.showMessageDialog( fc,
                                "File name has no extension. File name without extension may not be recognized by CharGer." );
                        throw new CGFileException( "File name has no extension." );
                    }

                    if ( chosenOne.exists() ) {
                        JTextArea wrappedName
                                = new JTextArea( "File already exists.  Replace it?\n\"" + chosenOne.getAbsolutePath() + "\"" ); //, 0, 30 );
//                        wrappedName.setLineWrap( true );
                        userAnswer = JOptionPane.showConfirmDialog(
                                fc,
                                wrappedName,
                                promptstring,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE );

                        if ( userAnswer == JOptionPane.YES_OPTION ) {
                            fileOkay = true;
                        }
                    } else {
                        fileOkay = true;
                    }
                } else // user cancelled
                {
                    fileOkay = true;
                    chosenOne = null;
                }
            }
        }
        return chosenOne;
    }

    // File operations
    /**
     * Saves a CharGer Graph to a file. If there's a human user in front, then
     * owner is non-null and dialogs are displayed to get the input. If no human
     * user, then owner is null and the filename is used directly. If one of the
     * "content" formats -- CHARGER38, CHARGER4 or CGIF2007, then we first write
     * to a temporary file in case there's an error in writing. If an image
     * format, then the file is written to directly -- if there's an error then
     * the image file may be trashed.
     *
     * @param format What type of output format is to be written.
     * @param filename The name of the file to which the graph is to be written.
     * either relative to whever Java thinks is the current directory or else
     * absolute
     * @param g The graph to be saved
     * @param owner Frame to own the file dialogs; null if no user input is
     * needed or required
     *
     * @return the file which was chosen; null if it didn't finish normally.
     * @see ImageIO#getWriterFormatNames()
     */
    public synchronized static File GraphToFile( Graph g, FileFormat format, String filename, Frame owner ) throws CGFileException {

        File initialChoiceFile = getAbsoluteFile( filename, format );

        String promptstring = "Save graph as " + format.description();
        File chosenOne = null;
        if ( owner != null ) {
            chosenOne = chooseViaFileSaveDialog( owner, initialChoiceFile, promptstring );
        } else {    // There's no human input, make your best guess
            chosenOne = initialChoiceFile;
        }

        // if approved, then continue
        if ( chosenOne != null ) {
            if ( format.family() == FileFormat.Family.VECTOR ) {
                saveGraphAsVectorGraphic( g, format, chosenOne.getAbsolutePath() );
            } else //if ( imageFormats.contains( format.extension().toLowerCase() ) ) {
            if ( format.family() == FileFormat.Family.BITMAP ) {
                saveGraphAsBitmapImage( g, format, chosenOne.getAbsoluteFile() );
            } else if ( format.family() == FileFormat.Family.TEXT ) {
                saveGraphAsTextFormat( g, format, chosenOne );
            }
            return chosenOne;
        } else {
            return null;
        }
    }

    /**
     * The output routine for versions 3.8 and later. EditFrame makes the
     * header, but GraphObject makes each object's string all found in toString
     * methods in the GraphObject classes. Uses XML format.
     *
     * @see #saveGraph38XML
     * @see GraphObject#toString
     */
    public static void saveGraph38XML( BufferedWriter f, Graph gr ) {		// version 3's format
        OperManager.performActionValidate( gr );
        try {

            f.write( charger.xml.CGXGenerator.generateXML( gr ) );
            f.write( Global.LineSeparator );
        } catch ( IOException exc ) {
            Global.error( exc.getMessage() );
        }
        //Global.info( "test of XML:\n" + charger.xml.CGXGenerator.generateXML( gr ) );
    }

    /**
     * EditFrame makes the header, but GraphObject makes each object's string
     * all found in toString methods in the GraphObject classes. Uses XML
     * format.
     *
     * @see #saveGraph38XML
     * @see GraphObject#toString
     */
    public static void saveGraph4( OutputStream os, Graph gr ) {
        OperManager.performActionValidate( gr );
//        try {
////            OutputStream os = new FileOutputStream( outfile );
        XMLEncoder encoder = new XMLEncoder( os );
        EditFrame ef = gr.getOwnerFrame();  // save ownerframe for later restoring
        gr.setOwnerFrame( null );
        encoder.writeObject( gr );
        gr.setOwnerFrame( ef ); // restore ownerframe
        encoder.close();
//        } catch ( IOException exc ) {
//            Hub.error( exc.getMessage() );
//        }
//        Global.info( "test of Charger 4 output:\n" + charger.xml.CGXGenerator.generateXML( gr ) );
    }

    /**
     * EditFrame makes the header, but GraphObject makes each object's string
     * all found in toString methods in the GraphObject classes. Uses version
     * 2's proprietary format, where values are separated by "|".
     *
     * @see GraphObject#toString
     */
    public static void saveGraph2( BufferedWriter writer, Graph gr ) {		// version 2's format
        Date now = Calendar.getInstance().getTime();
        String today = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM ).format( now );
        try {
            writer.write( Global.EditorNameString
                    + "|" + "version=" + Global.CharGerVersion
                    + "|" + "creation=" + today + Global.LineSeparator );
            writer.write( gr.safeString() + Global.LineSeparator );
            writer.write( "\\\\" );   // end graph with a 
            writer.write( Global.LineSeparator );
        } catch ( IOException exc ) {
            Global.error( exc.getMessage() );
        }
    }

    private static void saveGraphCGIF2007( BufferedWriter f, Graph gr, boolean includeCharGerInfo ) {
        try {
            f.write( cgif.generate.CGIFWriter.graphToString( gr, includeCharGerInfo ) + Global.LineSeparator );
            //f.write( "\\\\" );   // end graph with a 
            //f.write( Hub.LineSeparator );
        } catch ( IOException exc ) {
            Global.error( exc.getMessage() );
        }
    }

    /**
     * Takes an entire graph and formats it as a bitmap image. In the future,
     * should be able to format partial graphs, but for now, only works for a
     * complete graph.
     *
     * @param g the graph to be imaged; does not have to be in a frame.
     * @return the entire graph as a BufferedImage in the given format
     * @see #GraphToFile
     */
    public static BufferedImage graphToImage( Graph g ) {
//        double scaleFactor = 1.0;
        java.awt.geom.Rectangle2D.Double bounds = g.getDisplayBounds();
        //bounds.setBounds( 0, 0, (int)(bounds.width*scaleFactor), (int)(bounds.height*scaleFactor) );
        BufferedImage bi = new BufferedImage(
                (int)bounds.width,
                (int)bounds.height,
                BufferedImage.TYPE_3BYTE_BGR );
//                BufferedImage.TYPE_INT_RGB );

        RenderingHints rh = new RenderingHints( null );
        rh.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        rh.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        rh.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
        rh.put( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );

        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints( rh );
        //g2d.scale(scaleFactor, scaleFactor );	
//        g2d.translate( -1 * bounds.x, -1 * bounds.y );	// commented to address issue #2
        if ( Global.defaultFont != null ) {
            g2d.setFont( Global.defaultFont );
        }
        g2d.setColor( Color.LIGHT_GRAY );
        g2d.fillRect( (int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height );
        g.draw( g2d, true );		// format as though we're printing

        return bi;
    }

    /**
     * Sets up the imageFormats array list with all the valid image types
     * supported by the local image writer.
     */
    public static void initializeImageFormatList() {
        String[] possible = ImageIO.getWriterFormatNames();
        Arrays.sort( possible );
        for ( String s : possible ) {
            String format = s.toLowerCase();
            if ( !imageFormats.contains( format ) ) {
                imageFormats.add( format );
            }
        }

    }

    /**
     * Save as one of the text or export formats. This overwrites the file; use
     * chooseViaFileSaveDialog if you need the user to confirm overwriting.
     *
     * @param g the graph to be saved/exported
     * @param format the format of the output file desired
     * @param chosenOne the file (already confirmed) that will be used.
     * @throws CGFileException
     */
    public synchronized static void saveGraphAsTextFormat( Graph g, FileFormat format, File chosenOne ) throws CGFileException {
        FileOutputStream fos = null;
        BufferedWriter out = null;

        // create temp file for writing and then rename to what's needed when all works as expected
        File tempfile = null;
        try {
            tempfile = File.createTempFile( chosenOne.getName(), null, chosenOne.getParentFile() );
            fos = new FileOutputStream( tempfile );
        } catch ( IOException ex ) {
            Global.error( "saveGraphAsTextFormat: creating temp file exception: " + ex.getMessage() );
        }
        out = new BufferedWriter( new OutputStreamWriter( fos ) );
        if ( out != null ) {
            if ( format == FileFormat.CHARGER3 ) {
                saveGraph38XML( out, g );
            } else if ( format == FileFormat.CHARGER2 ) {
                saveGraph2( out, g );
            } else if ( format == FileFormat.CGIF2007 ) {
                saveGraphCGIF2007( out, g, Global.includeCharGerInfoInCGIF );
            } else if ( format == FileFormat.CHARGER4 ) {
                saveGraph38XML( out, g );           // Note we're using output stream here, not writer
            }
            try {
                out.close();
                // should only reach here if writing was successful
                chosenOne.delete();
                if ( tempfile.renameTo( chosenOne ) ) {
                    Global.info( "temp file sucessfully renamed" );
                } else {
                    Global.info( "temp file was NOT successfully renamed" );
                }
            } catch ( Exception ee ) {
                throw new CGFileException( ee.getMessage() );
            }
        }
    }

    /**
     * Uses the ImageIO methods to write the graph in the given format This
     * overwrites the file; use chooseViaFileSaveDialog if you need the user to
     * confirm overwriting.
     *
     * @param g the Charger graph to be saved
     * @param format one of the strings returned by
     * ImageIO.getWriterFormatNames()
     * @param f The file in which to save it. Any extension (if any) is replaced
     * by the format string (lower case)
     */
    public synchronized static void saveGraphAsBitmapImage( Graph g, FileFormat format, File f ) {
        String abs = f.getAbsolutePath();
        abs = Util.stripFileExtension( abs );
        abs += "." + format.extension().toLowerCase();

        java.awt.geom.Rectangle2D.Double printableBounds = g.getDisplayBounds();
        Dimension dim = new Dimension( (int)printableBounds.width, (int)printableBounds.height );

        try {
            File newFile = new File( abs );
            FileOutputStream fos = new FileOutputStream( newFile );
            BufferedImage im = graphToImage( g );
//            ImageIO.write( im, format.extension(), fos );
            ImageIO.write( im, format.extension(), newFile );
            fos.close();

        } catch ( IOException ee ) {
            Global.error( "Error writing file:\n " + abs + " " + format + "\nwith ImageIO:\n" + ee.getMessage() );
        }

    }

    /**
     * Uses the ImageIO methods to write the graph in pdf, svg or eps format.
     * This overwrites the file; use chooseViaFileSaveDialog if you need the
     * user to confirm overwriting.
     *
     * @param g the Charger graph to be saved
     * @param format one of the strings returned by
     * @param filename The completely qualified path/file in which to save it.
     * Any extension (if any) is replaced by the format string (lower case)
     */
    public synchronized static void saveGraphAsVectorGraphic( Graph g, FileFormat format, String filename /* File f */ ) {
        String abs = filename;
        abs = Util.stripFileExtension( abs );
        abs += "." + format.extension();

        java.awt.geom.Rectangle2D.Double printableBounds = g.getDisplayBounds();
        Dimension dim = new Dimension( (int)printableBounds.width, (int)printableBounds.height );

        try {
            VectorGraphics2D graphics = null;

            if ( format == FileFormat.PDF ) {
                graphics = new PDFGraphics2D( 0, 0, (double)dim.width, (double)dim.height );
            } else if ( format == FileFormat.SVG ) {
                graphics = new SVGGraphics2D( 0, 0, (double)dim.width, (double)dim.height );
            } else if ( format == FileFormat.EPS ) {
                graphics = new EPSGraphics2D( 0, 0, (double)dim.width, (double)dim.height );
            } else {
                Global.error( "performActionSaveGraphAsVectorGraphic: format " + format.toString() + " not allowed." );
            }

            graphics.setFontRendering( VectorGraphics2D.FontRendering.VECTORS );
            g.draw( graphics, true );

            File newFile = new File( abs );
            FileOutputStream fos = new FileOutputStream( newFile );
            byte[] imageBytes = graphics.getBytes();
            for ( int bytenum = 0; bytenum < imageBytes.length; bytenum++ ) {
                fos.write( imageBytes[bytenum] );
            }
            fos.close();

        } catch ( IOException ee ) {
            Global.error( "Error writing file:\n" + abs + " " + format + "\nwith ImageIO:\n" + ee.getMessage() );
        }

    }
}
