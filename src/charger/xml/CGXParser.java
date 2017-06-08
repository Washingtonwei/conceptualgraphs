//
//  CGXParser.java
//  CharGer 2014
//
//  Created by Harry Delugach on Sun May 04 2003.
//  Major overhaul in Nov 2014 to accommodate DOM parsing routines.
//
package charger.xml;

import charger.gloss.GenericTypeDescriptor;
import charger.gloss.AbstractTypeDescriptor;
import charger.gloss.wn.WordnetTypeDescriptor;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

import charger.*;
import charger.obj.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import kb.KBException;
import kb.ObjectHistoryEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLReaderAdapter;

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
 * Used for parsing XML representations of a conceptual graph into its CharGer
 * internal form. Uses the DOM parsing routines.
 */
public class CGXParser extends DefaultHandler {

    Document doc = null;        // used for the experimental DOM parser

    private boolean _traceTags = false;
    private boolean _keepIDs = true;
    private boolean _ignoreLayout = false;
    /**
     * Whether to keep the top level graph intact (perhaps adding to it), but
     * retaining its created, modified, etc.
     */
    private boolean _preserveGraph = false;
    /**
     * Whether to gather all parsed objects into a arraylist (usually used for
     * selecting)
     */
    private boolean _makeList = false;
    public static Point2D.Double offsetZero = new Point2D.Double( 0.0, 0.0 );
    private Point2D.Double _offset = offsetZero;
//    private Point2D.Double _newOrigin = null;
    private ArrayList _parsedObjects = new ArrayList();
    private Graph _topLevelGraph = null;

//    //private GraphObject currentObject = null;
//    // stack of strings for each tag processed by the parser
//    Stack parserStack = new Stack();
//    // holds the sequence of outer to inner graph nesting
//    Stack graphStack = new Stack();
//    // keeps the stack of graph objects themselves
//    Stack objectStack = new Stack();
//    // everytime a label is encountered, push it here
//    Stack labelStack = new Stack();
    /**
     * Has entries of the form "nnn", "nnn" denoting the old ident (i.e. the one
     * in the file) and the new ident which has been assigned during this parse.
     * Assures that no two "new" objects have the same ident.
     */
    private Hashtable<String, String> oldNewIDs = new Hashtable<String, String>();
    String genericString = "";	// the string we just processed before an ending tag
    StringBuilder stringCollector = new StringBuilder( "" );
    WordnetTypeDescriptor wordnetDescr = null;
    GenericTypeDescriptor genericDescr = null;
    ArrayList descriptors = new ArrayList();
    String definition = null;

    /**
     * Parser works as an instance; this allows for multiple parsers to be open
     * at the same time.
     */
    public CGXParser() {
        super();
    }

    public CGXParser( String input ) {
        InputStream is = new ByteArrayInputStream( input.getBytes() );
        buildDocument( is );
    }

    public CGXParser( InputStream is ) {
        buildDocument( is );
    }

    public boolean isIgnoreLayout() {
        return _ignoreLayout;
    }

    public void setIgnoreLayout( boolean _ignoreLayout ) {
        this._ignoreLayout = _ignoreLayout;
    }

    public boolean isKeepIDs() {
        return _keepIDs;
    }

    public void setKeepIDs( boolean _keepIDs ) {
        this._keepIDs = _keepIDs;
    }

    public Point2D.Double getOffset() {
        return _offset;
    }

    public void setOffset( Point2D.Double _offset ) {
        this._offset = _offset;
    }

    public boolean isMakeList() {
        return _makeList;
    }

    public void setMakeList( boolean _makeList ) {
        this._makeList = _makeList;
    }

    public boolean isPreserveGraph() {
        return _preserveGraph;
    }

    public void setPreserveGraph( boolean _preserveGraph ) {
        this._preserveGraph = _preserveGraph;
    }

    public ArrayList getParsedObjects() {
        return _parsedObjects;
    }

    public void setParsedObjects( ArrayList _parsedObjects ) {
        this._parsedObjects = _parsedObjects;
    }

    public Graph getParsedGraph() {
        return _topLevelGraph;
    }

//    /**
//     * Parse a graph from a file, storing the results in a given graph. The
//     * highest-level structure in a CGX file is currently a graph (possibly with
//     * un-connected parts). If higher-level structures are to be placed in a CGX
//     * file, then the structure of this method will change.
//     *
//     * @param f the file in which some legal CGXML is stored.
//     * @param graph a single graph, usually blank when loading a graph from a
//     * file, but it doesn't have to be. The only restriction is that if there is
//     * more than one &lt;graph&gt; tag with <code>owner=GOID.zero</code>, the
//     * results will be unpredictable. Most likely whichever such graph appears
//     * last in the file will overwrite any previous objects that were read in.
//     */
//    public void parseCGXMLFile( File f, Graph graph ) {
//        _makeList = false;
//        _offset = new Point2D.Double( 0, 0 );
//        InputStreamReader reader = null;
//        try {
//            reader = new InputStreamReader( new FileInputStream( f ) );
//            if ( !Global._useDOMParser ) {
////                _topLevelGraph = graph;
////                parseCGXMLInputStream( reader, graph );
////                reader.close();
//            } else {
//                parseCGXMLInputStreamDOM( new FileInputStream( f ), graph );
//            }
//        } catch ( FileNotFoundException fnf ) {
//            JOptionPane.showMessageDialog( null, "File not found: " + fnf.getMessage() );
////        } catch ( IOException ie ) {
////            JOptionPane.showMessageDialog( null, "IOException: " + ie.getMessage() );
//        }
//        // Prepare history for every object
//
//        ObjectHistoryEvent he = new ObjectHistoryEvent( f );
//        graph.addHistory( he );
//    }

//    /**
//     * Parse a graph from a file, storing the results in a given graph.
//     *
//     * @param filename name of the file in which some legal CGXML is stored.
//     * @param graph a single graph, usually blank when loading a graph from a
//     * file, but it doesn't have to be. The only restriction is that if there is
//     * more than one &lt;graph&gt; tag with <code>owner=GOID.zero</code>, the
//     * results will be unpredictable. Most likely whichever such graph appears
//     * last in the file will overwrite any previous objects that were read in.
//     */
////    public void parseCGXMLFile( String filename, Graph graph ) {
////        File f = new File( filename );
////        parseCGXMLFile( f, graph );
////        OperManager.performActionValidate( graph );
////    }
//    /**
//     * Parse a graph from an XML string, storing the results in a given graph.
//     *
//     * @param xmlString the string containing all of the XML text to be parsed
//     * @param graph a single outermost graph
//     * @param offset The amount to translate each object parsed
//     * @param keepIDs whether to keep the original ID's that were parsed, or
//     * generate new ones
//     * @return a list of the graph objects that were parsed
//     */
//    public ArrayList parseCGXMLString( String xmlString, Graph graph, Point2D.Double offset, boolean keepIDs ) {
//        _keepIDs = keepIDs;
//        _offset = offset;
//        _makeList = true;
//        if ( !keepIDs ) {
//            oldNewIDs.clear();
//        }
//        Reader reader = null;
//        //try {
//        reader = new StringReader( xmlString ); //new InputStreamReader( new StringReader( xmlString) );
//        //} catch ( IOException ioe ) { 
//        //	JOptionPane.showMessageDialog( null, "IO Exception: " + ioe.getMessage() ); 
//        //}
//        parseCGXMLInputStream( reader, graph );
//        try {
//            reader.close();
//        } catch ( IOException ex ) {
//            JOptionPane.showMessageDialog( null, "IOException: " + ex.getMessage() );
//        }
//        return _parsedObjects;
//    }

//    /**
//     * Where the actual parsing gets done. The other methods are just wrappers
//     * for this one.
//     */
//    public void parseCGXMLInputStream( Reader inreader, Graph graph ) {
//        _topLevelGraph = graph;
//
//        if ( _makeList ) {
//            _parsedObjects.clear();
//        }
//        //Global.info( "ready to parse... top level graph is " + graph.toString() );
//        //XMLReader xr = XMLReaderFactory.createXMLReader();
//        // note: current parsing is tied to a particular XML reader
//        // TODO: Explore more modern options to replace the now-retired crimson
////        XMLReader xr = new org.apache.crimson.parser.XMLReaderImpl();
//        XMLReader xr;
//        try {
//            xr = new org.xml.sax.helpers.XMLFilterImpl( new ParserAdapter( new XMLReaderAdapter() ) );
//        } catch ( SAXException ex ) {
//            //Logger.getLogger( CGXParser.class.getName() ).log( Level.SEVERE, null, ex );
//            Global.error( "Error in parseGGXMLInputStream: " + ex.getMessage() );
//            graph = null;
//            return;
//        }
//
//        //xr.setContentHandler(handler);
//        //xr.setErrorHandler(handler);	
//        xr.setContentHandler( this );
//        xr.setErrorHandler( this );
//
//        try {
//            xr.parse( new InputSource( inreader ) );
//        } catch ( IOException ioe ) {
//            Global.error( "IO Exception in CGXParser: " + ioe.getMessage() );
//        } catch ( SAXParseException se ) {
//            Exception e = se.getException();
//            if ( e == null ) {
//                e = se;
//            }
//            Global.error( "Sax Parse Exception in CGXParser: Graph " + graph.getTextLabel() + " - " + e.getMessage() );
//            se.printStackTrace();
//        } catch ( SAXException see ) {
//            Exception e = see.getException();
//            if ( e == null ) {
//                e = see;
//            }
//            Global.error( "Sax Exception in CGXParser: Graph " + graph.getTextLabel() + " - " + e.getMessage() );
//            //see.printStackTrace();
//        }
//        try {
//            inreader.close();
//        } catch ( IOException ex ) {
//            JOptionPane.showMessageDialog( null, "IOException: " + ex.getMessage() );
//        }
//
//    }
//
//    public void startDocument() {
//        if ( _traceTags ) {
//            Global.info( "Start document" );
//        }
//    }
//
//    public void endDocument() {
//        if ( _traceTags ) {
//            Global.info( "End document" );
//        }
//    }

//    /**
//     * Main processor for elements. Many elements are not processed at their
//     * start, either because we need all of their contents before we process
//     * them, or because only the nested contents matter. We still test for them
//     * here, so that we won't get the "tag ignored" message.
//     */
//    public void startElement( String uri, String name, String qName, Attributes atts ) {
//        parserStack.push( qName );
//
//        if ( _traceTags ) {
//            if ( "".equals( uri ) ) {
//                Global.info( "Start tag: " + qName );
//            } else {
//                Global.info( "Start tag: {" + uri + "}" + name );
//            }
//            for ( int k = 0; k < atts.getLength(); k++ ) {
//                Global.info( "  parameter " + k + " name is " + atts.getLocalName( k )
//                        + "; value is " + atts.getValue( k ) );
//            }
//        }
//
//        // actual processing goes here
//        // if one of the graph object's tags, put that object on the stack and process it.
//        if ( qName.equals( "concept" ) || qName.equals( "relation" ) || qName.equals( "actor" ) || qName.equals( "note" )
//                || qName.equals( "arrow" ) || qName.equals( "genspeclink" ) || qName.equals( "coref" ) || qName.equals( "customedge" )
//                || qName.equals( "typelabel" ) || qName.equals( "relationlabel" ) || qName.equals( "graph" ) ) {
//            objectStack.push( processGraphObjectTag( qName, atts ) );
//            if ( atts.getValue( "negated" ) != null ) {
//                ( (GraphObject)objectStack.peek() ).setNegated( atts.getValue( "negated" ).equals( "true" ) );
//            }
//            if ( objectStack.peek() instanceof GEdge ) {
//                processGEdgeTag( (GEdge)objectStack.peek(), atts );
//            }
//            if ( atts.getValue( "owner" ).equals( GraphObjectID.zero.toString() ) ) { // check for the top level graph
//                // top level graph is already allocated for purposes of reference during this parser.
//                //Global.info( "top level graph: " + _topLevelGraph.toString() );
//            } else {
//                if ( _keepIDs ) {
//                    attachObjectToNewGraph( new GraphObjectID( atts.getValue( "owner" ) ) );
//                } else {
//                    String newOwnerID = (String)oldNewIDs.get( atts.getValue( "owner" ) );
//                    if ( newOwnerID != null ) {
//                        attachObjectToNewGraph( new GraphObjectID( newOwnerID ) );
//                    } else {
//                        attachObjectToNewGraph( _topLevelGraph.objectID );		// really an OLD graph! :-)
//                    }
//                }
//            }
//            if ( objectStack.peek() instanceof Graph ) {
//                graphStack.push( objectStack.peek() );
//                //((Graph)objectStack.peek() ).setTextLabelPos();
//            }
//        } else if ( qName.equals( "conceptualgraph" ) ) {
//            _topLevelGraph.createdTimeStamp = atts.getValue( "created" );
//            _topLevelGraph.modifiedTimeStamp = atts.getValue( "modified" ); // might be null
//            if ( _topLevelGraph.modifiedTimeStamp == null ) {
//                _topLevelGraph.modifiedTimeStamp = _topLevelGraph.createdTimeStamp;
//            }
//            if ( _topLevelGraph.createdTimeStamp == null ) {
//                _topLevelGraph.createdTimeStamp = _topLevelGraph.modifiedTimeStamp;
//            }
//            if ( atts.getValue( "wrapLabels" ) != null ) {
//                _topLevelGraph.wrapLabels = Boolean.parseBoolean( atts.getValue( "wrapLabels" ) );
//            }
//            if ( atts.getValue( "wrapColumns" ) != null ) {
//                _topLevelGraph.wrapColumns = Integer.parseInt( atts.getValue( "wrapColumns" ) );
//            }
//
//        } else if ( qName.equals( "meta-data" ) ) {
//            Global.info( "Found meta-data tag" );
//        } else if ( qName.equals( "type" ) ) {
//            descriptors.clear();
//        } else if ( qName.equals( "definition" ) ) {
//        } else if ( qName.equals( "referent" ) ) {
//        } else if ( qName.equals( "label" ) ) {
//            // these wouldn't work anyway --- "label" is at the top of the stack! :-)
//            //if ( parserStack.peek().equals( "type" ) ) { }
//            //else if ( parserStack.peek().equals( "referent" ) ) { }
//        } else if ( qName.equals( "layout" ) ) {
//            // there are no parameters to the layout tag itself; all the info is in the enclosed tags.
//        } else if ( qName.equals( "rectangle" ) ) {
//            try {
//                Rectangle2D.Double displayrect = null;
//                if ( !_ignoreLayout ) {
//                    displayrect = new Rectangle2D.Double(
//                            Double.parseDouble( atts.getValue( "x" ) ),
//                            Double.parseDouble( atts.getValue( "y" ) ),
//                            Double.parseDouble( atts.getValue( "width" ) ),
//                            Double.parseDouble( atts.getValue( "height" ) ) );
//
//                    displayrect.x += _offset.x;
//                    displayrect.y += _offset.y;
//                    if ( !objectStack.isEmpty() ) {
//                        ( (GraphObject)objectStack.peek() ).setDisplayRect( displayrect );
//                        /*if ( objectStack.peek() instanceof Graph ) 
//                         {
//                         ((Graph)objectStack.peek()).setTextLabelPos();
//                         }
//                         */
//                    }
//                }
////                Global.info( "current object is " + objectStack.peek() + "; and its display rect is " +	
////                		displayrect.toString() );
//                //Global.info( "   its owner graph is " + ((GraphObject)objectStack.peek()).getOwnerGraph() );
//            } catch ( NumberFormatException nfe ) {
//                Global.error( "number format exception on dimension" );
//            }
//        } else if ( qName.equals( "color" ) ) {
//            int r, g, b;
//            try {
//                if ( !_ignoreLayout ) {
//                    StringTokenizer nums = new StringTokenizer( atts.getValue( "foreground" ) );
//                    r = Integer.parseInt( nums.nextToken( "," ) );
//                    g = Integer.parseInt( nums.nextToken( "," ) );
//                    b = Integer.parseInt( nums.nextToken( "," ) );
//                    if ( !objectStack.isEmpty() ) {
//                        ( (GraphObject)objectStack.peek() ).setColor( "text", new Color( r, g, b ) );
//                    }
//
//                    nums = new StringTokenizer( atts.getValue( "background" ) );
//                    r = Integer.parseInt( nums.nextToken( "," ) );
//                    g = Integer.parseInt( nums.nextToken( "," ) );
//                    b = Integer.parseInt( nums.nextToken( "," ) );
//                    if ( !objectStack.isEmpty() ) {
//                        ( (GraphObject)objectStack.peek() ).setColor( "fill", new Color( r, g, b ) );
//                    }
//                }
//            } catch ( NumberFormatException e ) {
//                Global.error( "number format exception on color" );
//            }
//        } else if ( qName.equals( "font" ) ) {
//            try {
//                if ( !_ignoreLayout ) {
//                    String fontname = atts.getValue( "name" );
//                    int fontstyle = Integer.parseInt( atts.getValue( "style" ) );
//                    int fontsize = Integer.parseInt( atts.getValue( "size" ) );
//
//                    if ( !objectStack.isEmpty() ) {
//                        Font f = new Font( fontname, fontstyle, fontsize );
//                        ( (GraphObject)objectStack.peek() ).setLabelFont( f );
//                    }
//
//                }
//            } catch ( NumberFormatException e ) {
//                Global.error( "number format exception on color" );
//            }
//        } else if ( qName.equals( "edge" ) ) {
//            if ( objectStack.peek() instanceof GEdge ) {
//                GEdge edge = (GEdge)objectStack.peek();
//                String next = atts.getValue( "arrowHeadWidth" );
//                edge.setArrowHeadWidth( ( next == null ) ? Global.factoryEdgeAttributes.getArrowHeadWidth() : Integer.parseInt( atts.getValue( "arrowHeadWidth" ) ) );
//                next = atts.getValue( "arrowHeadHeight" );
//                edge.setArrowHeadHeight( ( next == null ) ? Global.factoryEdgeAttributes.getArrowHeadHeight() : Integer.parseInt( atts.getValue( "arrowHeadHeight" ) ) );
//                next = atts.getValue( "edgeThickness" );
//                edge.setEdgeThickness( ( next == null ) ? Global.factoryEdgeAttributes.getEdgeThickness() : Double.parseDouble( atts.getValue( "edgeThickness" ) ) );
//            }
//        } else if ( qName.equals( WordnetTypeDescriptor.getTagName() ) ) {
//            // this try block is because getInstanceFromXML invoked WordnetManager's getSynset
//            //   which threw an exception that (for unexplained reasons) wasn't caught locally
//            try {
//                wordnetDescr = (WordnetTypeDescriptor)WordnetTypeDescriptor.getInstanceFromXML( atts );
//                //charger.Global.info( "about to set the type descriptor for object " + objectStack.peek() );
//                //((GNode)objectStack.peek()).setTypeDescriptor( wordnetDescr );
//                //charger.Global.info( "descr validated is " + wordnetDescr.isValidated() );
//                //charger.Global.info( "after setting type desc, desc is " + wordnetDescr );
//                if ( wordnetDescr == null ) {
//                    Global.warning( "Wordnet type descriptor could not be created." );
//                } else if ( !wordnetDescr.isValidated() ) {
//                    Global.warning( "Wordnet entry not valid (pos=" + wordnetDescr.getPOS() + ",offset=" + wordnetDescr.getSynset().getOffset()
//                            + ",version=" + wordnetDescr.getVersion() + ") perhaps wrong WN version?." );
//                }
//                //else 			descriptors.add( wordnetDescr );
//
//            } catch ( Exception e ) {
//            }
//        } else if ( qName.equals( GenericTypeDescriptor.getTagName() ) ) {
//            genericDescr
//                    = (GenericTypeDescriptor)GenericTypeDescriptor.getInstanceFromXML( atts );
//            //((GNode)objectStack.peek()).setTypeDescriptor( genericDescr );
//            if ( genericDescr == null ) {
//                Global.warning( "generic type descriptor could not be created." );
//            }
//            //else descriptors.add( genericDescr );
//
//        } else {
//            Global.warning( "Ignoring XML tag " + qName );
//        }
//    }
//
//    /**
//     * Part of the SAX Parser's interface.
//     */
//    public void endElement( String uri, String name, String qName ) {
//        if ( _traceTags ) {
//            if ( "".equals( uri ) ) {
//                Global.info( "End tag: " + qName + System.getProperty( "line.separator" ) );
//            } else {
//                Global.info( "End tag:   {" + uri + "}" + name );
//            }
//        }
//
//        if ( parserStack.isEmpty() ) {
//            Global.error( "CGXML Parser Error: tag " + qName + " ended but there was no element on the parser stack." );
//        } else if ( !parserStack.peek().equals( qName ) ) {
//            Global.error( "parser problem: tag \"" + parserStack.peek()
//                    + "\" was terminated by tag \"" + qName + "\"" );
//        } else {
//            parserStack.pop();
//        }
//
//        // save whatever string has been accumulated, and reset the string collector
//        genericString = stringCollector.toString().trim();
//        stringCollector = new StringBuilder( "" );
//
//        if ( qName.equals( "concept" ) || qName.equals( "relation" ) || qName.equals( "actor" ) || qName.equals( "note" )
//                || qName.equals( "arrow" ) || qName.equals( "genspeclink" ) || qName.equals( "coref" ) || qName.equals( "customedge" )
//                || qName.equals( "typelabel" ) || qName.equals( "relationlabel" ) || qName.equals( "graph" ) ) {
//            if ( objectStack.peek() instanceof GNode ) {
////                ( (GNode)objectStack.peek() ).setCenter();
////                ( (GNode)objectStack.peek() ).setCenterOnly();
//            }
//            if ( objectStack.isEmpty() ) {
//                Global.error( "CGXML Parser Error: object tag " + qName + " ended but there was no element on the object stack." );
//            } else {
//                if ( _makeList ) {
//                    _parsedObjects.add( objectStack.peek() );
//                }
//                try {
//                    Global.sessionKB.commit( objectStack.peek() );         // 2012 - probably not a great place to put this.
//                } catch ( KBException ex ) {
//                    Global.warning( ex.getMessage() + " on object " + ex.getSource().toString() );
//                    //Logger.getLogger( CGXParser.class.getName() ).log( Level.SEVERE, null, ex );
//                }
//                objectStack.pop();
//            }
//            //Global.info( "current object is " + currentObject );
//            // //Global.info( "finished parsing graph object " + currentObject.toString() );  // @BUG
//            //Global.info( "complete parsed graph is\n" + _topLevelGraph.toString() );
//        }
//
//        if ( qName.equals( "graph" ) ) {
//            Graph finishedGraph = (Graph)graphStack.pop();
//            finishedGraph.setTextLabelPos();
//
////            finishedGraph.resizeForContents( _offset );
//            // HERE would be a good place to decide who the owner of the graph is, its display rect, etc.
//            //Global.info( "GRAPH object being popped is " + throwaway + "; and its display rect is " +	
//            //		throwaway.getDisplayRect().toString() );
//        } else if ( qName.equals( "label" ) ) {
//            if ( parserStack.peek().equals( "type" ) ) {
//                //charger.Global.info( "there are " + descriptors.size() + " descriptors saved." );
//                ( (GNode)objectStack.peek() ).setTypeLabel( genericString, false );
//                genericString = "";
//            } else if ( parserStack.peek().equals( "referent" ) ) {
//                if ( objectStack.peek() instanceof Concept ) // only concepts can have referents?
//                {
//                    ( (Concept)objectStack.peek() ).setReferent( genericString, false );
//                    genericString = "";
//                }
//            } else {
//                labelStack.push( genericString );
//                genericString = "";
//            }
//            //else if ( parserStack.peek().equals( "
//        } else if ( qName.equals( "type" ) ) // at the end of a type, put all its descriptors in the current node
//        {
//            if ( descriptors.size() > 0 ) {
//                ( (GNode)objectStack.peek() ).setTypeDescriptors(
//                        (TypeDescriptor[])descriptors.toArray( new TypeDescriptor[ 0 ] ) );
//                //charger.Global.info( "set " + descriptors.size() + 
//                //" type descriptors for object " + ((GNode)objectStack.peek()).getTypeLabel() );
//            }
//            descriptors.clear();
//        } else if ( qName.equals( "definition" ) ) {
//            definition = genericString;
//            genericString = "";
//        } else if ( qName.equals( WordnetTypeDescriptor.getTagName() ) ) {
//            //charger.Global.info( "end of wordnet descriptor tag; descr validated is " + wordnetDescr.isValidated());
//            //if ( ! wordnetDescr.isValidated() )
//            {
//                wordnetDescr.setDefinition( definition );
//                wordnetDescr.setLabel( (String)labelStack.pop() );
//                //charger.Global.info( "set wordnet descriptor, label " + wordnetDescr.getLabel() + "; def is " + definition ) ;
//            }
//            // this was previously done at startElement
//            //((GNode)objectStack.peek()).setTypeDescriptor( wordnetDescr );
//            descriptors.add( wordnetDescr );
//            //craft.Craft.say( "adding descriptor " + wordnetDescr.getLabel() + "; stack object is " +
//            //		objectStack.peek() );
//            wordnetDescr = null;
//            definition = "";
//        } else if ( qName.equals( GenericTypeDescriptor.getTagName() ) ) {
//            genericDescr.setDefinition( definition );
//            genericDescr.setLabel( (String)labelStack.pop() );
//            //((GNode)objectStack.peek()).setTypeDescriptor( genericDescr );
//            descriptors.add( genericDescr );
//            //craft.Craft.say( "adding descriptor " + genericDescr.getLabel() + "; stack object is " +
//            //		objectStack.peek() );
//            genericDescr = null;
//            definition = "";
//        } else if ( qName.equals( "string" ) ) // Not used here
//        {
//        } else {
//        }
//    }
//
//    public void characters( char[] ch, int start, int length ) throws SAXException {
//        String content = new String( ch, start, length );
//        stringCollector.append( content );
//        //Global.info( "  characters: \"" + content + "\"" + System.getProperty( "line.separator" ) );
//    }
//
//    private void processGEdgeTag( GEdge ge, Attributes atts ) {
//        GraphObjectID fromID, toID;
//        fromID = new GraphObjectID( atts.getValue( "from" ) );
//        toID = new GraphObjectID( atts.getValue( "to" ) );
//
//        // pass in the rootGraph here, because GEdges may cross context boundaries
//        ge.fromObj = hookUp( ge, fromID, _topLevelGraph );
//        ge.toObj = hookUp( ge, toID, _topLevelGraph );
//        if ( ge.fromObj == null || ge.toObj == null ) {
//            // parsing a partial graph could lead to this
//            ge.getOwnerGraph().forgetObject( ge );
//            // should somehow indicate that the edge doesn't have two legitimate ends
//        } else {
//            ge.placeEdge();
//        }
//
//    }
//
//    /**
//     * Determine which kind of graph object is being processed (from the qName),
//     * allocate the new object and fill in some of the common information.
//     *
//     * @return the allocated graph object
//     */
//    private GraphObject processGraphObjectTag( String qName, Attributes atts ) {
//        GraphObject newObject = null;
//        if ( CharGerXMLTagNameToClassName.isEmpty() ) {
//            loadCharGerKeyWordToClassTable();
//        }
//        //Global.info( "parseOneObject: " + s );
//
//        String t = CharGerXMLTagNameToClassName.getProperty( qName, "DUMMY" );
//
//        if ( t.equals( "Graph" ) ) {
//            if ( atts.getValue( "owner" ).equals( GraphObjectID.zero.toString() ) ) {
//                newObject = _topLevelGraph;
//            } else {
//                newObject = new Graph( null );
//            }
//        } // GNodes
//        else if ( t.equals( "Concept" ) ) {
//            newObject = new Concept();
//        } else if ( t.equals( "Relation" ) ) {
//            newObject = new Relation();
//        } else if ( t.equals( "Actor" ) ) {
//            newObject = new Actor();
//        } else if ( t.equals( "TypeLabel" ) ) {
//            newObject = new TypeLabel();
//        } else if ( t.equals( "RelationLabel" ) ) {
//            newObject = new RelationLabel();
//        } // GEdges
//        else if ( t.equals( "Arrow" ) ) {
//            newObject = new Arrow();
//        } else if ( t.equals( "GenSpecLink" ) ) {
//            newObject = new GenSpecLink();
//        } else if ( t.equals( "Coref" ) ) {
//            newObject = new Coref();
//        } else if ( t.equals( "CustomEdge" ) ) {
//            newObject = new CustomEdge();
//        } else if ( t.equals( "Note" ) ) {
//            newObject = new Note();
//        } else if ( t.equals( "DUMMY" ) ) {
//            Global.error( "Syntax error: Found graph object tag name \"" + t + "\" which isn't recognized." );
//        }
//        // get old ID and map it to the new one
//        GraphObjectID oldID = new GraphObjectID( atts.getValue( "id" ) );	// get the old object ident as a string
//        if ( _keepIDs ) {
//            newObject.objectID = oldID;		// using old ident's
//        } else {
//            oldNewIDs.put( oldID.toString(), newObject.objectID.toString() );
//            //Global.info( "old ident " + oldID + " maps to new ident " + currentObject.objectID );
//            //Global.info( "oldnewid hashtable is " + oldNewIDs.toString() );
//        }
//        // handle textLabel -----
//        String textLabel = atts.getValue( "label" );
//        newObject.setTextLabel( textLabel );
//        //newObject.textLabel = textLabel;
//
//        return newObject;
//    }
//
//    /**
//     * Determines who is the owner of the current graph object, and inserts the
//     * object into that graph. If the owner is 0, then don't do anything.
//     *
//     * @param oldOwnerID the owner graph id as stored with the graph. This
//     * method find the new object id for the owner graph and then the current
//     * graph object is attached to its graph.
//     */
//    private void attachObjectToNewGraph( GraphObjectID oldOwnerID ) {
//        Graph newOwnerGraph = null;
//        // all objects are to be added to the graph with a new ID
//        // all objects have an old ID that is to be mapped to the new one.
//
//        // figure out which (new) graph is the owner of this object
//        if ( !oldOwnerID.equals( GraphObjectID.zero ) ) // only true for one top-level "graph" object
//        {
////                Global.info( "old owner string is " + oldOwnerID );
//            newOwnerGraph = (Graph)_topLevelGraph.findByID( oldOwnerID );
//        }
//        if ( newOwnerGraph == null ) {
//            Global.warning( "CGXParser: can't find owner for stored object." );
//        } else {
//            newOwnerGraph.insertObject( ( (GraphObject)objectStack.peek() ) );
//        }
//
//    }
//
//    /**
//     * Uses the old-to-new mapping to attach an edge to its new ends.
//     *
//     * @param ge the edge to be attached to its appropriate (new) nodes.
//     * @param oldID the ID assigned when the line's info was written
//     * @param gr the (new) graph which will own the new edge
//     */
//    private GNode hookUp( GEdge ge, GraphObjectID oldID, Graph gr ) {
//
//        GNode gn = null;
//        if ( !_keepIDs ) {
//            String newIDstring = (String)oldNewIDs.get( oldID.toString() );
//            if ( newIDstring != null ) {
//                gn = (GNode)gr.findByID( new GraphObjectID( newIDstring ) );
//            }
//        } else {
//            gn = (GNode)gr.findByID( oldID );
//        }
//
//        gn.attachGEdge( ge );
//        return gn;
//    }
//
//    public void fatalError( SAXParseException e ) {
//        charger.Global.error( "XML Parse Error in line " + e.getLineNumber() + " col " + e.getColumnNumber() );
//    }

    /**
     *
     * used to keep track of old and new versions of CharGer class names that
     * might appear in a ".cgx" file.
     */
    public static Properties CharGerXMLTagNameToClassName = new Properties();

    /**
     * Creates a lookup that helps parsers determine (from an XML tag) what
     * class is required. This could probably be replaced with a simple
     * Encode/Decode XML "XML serialized" version.
     *
     */
    public static void loadCharGerKeyWordToClassTable() {
        CharGerXMLTagNameToClassName.setProperty( "Graph", "Graph" );
        CharGerXMLTagNameToClassName.setProperty( "Concept", "Concept" );
        CharGerXMLTagNameToClassName.setProperty( "Relation", "Relation" );
        CharGerXMLTagNameToClassName.setProperty( "Actor", "Actor" );
        CharGerXMLTagNameToClassName.setProperty( "TypeLabel", "TypeLabel" );
        CharGerXMLTagNameToClassName.setProperty( "CGType", "TypeLabel" );
        CharGerXMLTagNameToClassName.setProperty( "RelationLabel", "RelationLabel" );
        CharGerXMLTagNameToClassName.setProperty( "CGRelType", "RelationLabel" );
        CharGerXMLTagNameToClassName.setProperty( "Arrow", "Arrow" );
        CharGerXMLTagNameToClassName.setProperty( "GenSpecLink", "GenSpecLink" );
        CharGerXMLTagNameToClassName.setProperty( "Coref", "Coref" );
        CharGerXMLTagNameToClassName.setProperty( "Note", "Note" );

        CharGerXMLTagNameToClassName.setProperty( "graph", "Graph" );
        CharGerXMLTagNameToClassName.setProperty( "concept", "Concept" );
        CharGerXMLTagNameToClassName.setProperty( "relation", "Relation" );
        CharGerXMLTagNameToClassName.setProperty( "actor", "Actor" );
        CharGerXMLTagNameToClassName.setProperty( "typelabel", "TypeLabel" );
        CharGerXMLTagNameToClassName.setProperty( "relationlabel", "RelationLabel" );
        CharGerXMLTagNameToClassName.setProperty( "arrow", "Arrow" );
        CharGerXMLTagNameToClassName.setProperty( "genspeclink", "GenSpecLink" );
        CharGerXMLTagNameToClassName.setProperty( "coref", "Coref" );
        CharGerXMLTagNameToClassName.setProperty( "customedge", "CustomEdge" );
        CharGerXMLTagNameToClassName.setProperty( "note", "Note" );
    }

    // =====================================================================================
    // ========  DOM Parser routines
    //
    /**
     * Factory method for setting up a parser for a new graph from a stream.
     *
     * @param is
     * @return parser instance appropriately set up
     */
    public static void parseForNewGraph( InputStream is, Graph g ) {
        CGXParser parser = new CGXParser( is );
        parser._topLevelGraph = g;
        parser.setKeepIDs( true );
        parser.setOffset( offsetZero );
        parser.setMakeList( false );
        parser.setPreserveGraph( false );

        parser.parseCGXMLCG( g );
        
    }

    /**
     * Factory method for setting up a parser for a new graph from a string. May
     * be used either for reading in an entirely new graph or for restoring one
     * (e.g., via undo).
     *
     * @param xmlString a complete cgx graph string
     * @return parser instance appropriately set up
     */
    public static void parseForNewGraph( String xmlString, Graph g ) {
        InputStream is = new ByteArrayInputStream( xmlString.getBytes() );
        parseForNewGraph( is, g );
    }

    /**
     * Factory method for setting up a parser for a list of graph objects from a
     * string. To get the list of objects, caller needs to follow up with a call
     * to getParsedObjects
     *
     * @param cgxmlString a string of an arbitrary set of objects
     * @param g the graph to which the parsed objects will be added
     * @return parser instance appropriately set up.
     */
    public static ArrayList<GraphObject> parseForCopying( String cgxmlString, Graph g ) {

        CGXParser parser = new CGXParser( cgxmlString );
        parser.setKeepIDs( false );
        parser.setOffset( new Point2D.Double( EditFrame.xoffsetForCopy, EditFrame.yoffsetForCopy ) );
        parser.setMakeList( true );
        parser.setPreserveGraph( true );

        parser._parsedObjects.clear();

        parser.parseCGXMLCG( g );

        return parser.getParsedObjects();

//        return parser;
    }

    /**
     * Create the entire parse tree for the document.
     *
     * @param is an XML stream containing CG-XML objects.
     */
    public void buildDocument( InputStream is ) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch ( ParserConfigurationException ex ) {
            Global.warning( "Parser configuration exception: " + ex.getMessage() );
        }
        try {
            doc = dBuilder.parse( is );
        } catch ( SAXException ex ) {
            Global.warning( "SAX parser exception: " + ex.getMessage() );
        } catch ( IOException ex ) {
            Global.warning( "IO exception: " + ex.getMessage() );
        }

    }

    /**
     * Assumes that the Document has already been built. This is the very top of
     * the parse tree processing.
     *
     * @param graph initialized but possibly empty graph
     */
    public void parseCGXMLCG( Graph graph ) {
        if ( graph == null ) {
            _topLevelGraph = new Graph();       // never used
        } else {
            _topLevelGraph = graph;
        }
        Element docelem = doc.getDocumentElement();
        if ( docelem == null ) {
            return;
        }

        if ( !docelem.getTagName().equals( "conceptualgraph" ) ) {
            Global.warning( "Outermost XML tag \"" + docelem.getTagName() + "\" should be \"conceptualgraph\"." );
        }

        if ( !this.isPreserveGraph() ) {
            NamedNodeMap docmap = docelem.getAttributes();
            _topLevelGraph.createdTimeStamp = getNamedAttributeFromMap( docmap, "created" );
            _topLevelGraph.modifiedTimeStamp = getNamedAttributeFromMap( docmap, "modified" );;
            if ( _topLevelGraph.modifiedTimeStamp == null ) {
                _topLevelGraph.modifiedTimeStamp = _topLevelGraph.createdTimeStamp;
            }
            if ( _topLevelGraph.createdTimeStamp == null ) {
                _topLevelGraph.createdTimeStamp = _topLevelGraph.modifiedTimeStamp;
            }

            String s = getNamedAttributeFromMap( docmap, "wrapLabels" );
            if ( s != null ) {
                _topLevelGraph.wrapLabels = Boolean.parseBoolean( s );
            }
            s = getNamedAttributeFromMap( docmap, "wrapColumns" );
            if ( s != null ) {
                _topLevelGraph.wrapColumns = Integer.parseInt( s );
            }
        }

        NodeList nodes = docelem.getChildNodes();
        for ( int childnum = 0; childnum < nodes.getLength(); childnum++ ) {
            if ( nodes.item( childnum ).getNodeType() == Node.ELEMENT_NODE ) {
                // is either a top level graph element or a graph being pasted/duplicated
                Element elem = (Element)nodes.item( childnum );
                if ( isPreserveGraph() ) {
                    GraphObject go = instantiateGraphObject( elem.getTagName() );
                    if ( go != null ) {
                        setID( elem, go );       // make sure we set the id before adding to the graph
                        _topLevelGraph.insertObject( go );  // add this object to the graph
                        if ( isMakeList() ) {
                            _parsedObjects.add( go );   // add to the list of objects to be returned
                        }
                        parseGraphObjectElement( elem, go );    // populate the object
                    }
                } else {
                    parseCGXMLGraphTagElement( elem, _topLevelGraph );
                }
            }
        }
    }

    /**
     * Handle the "graph" tag. Assumes that the element is a "graph" tag and
     * that the graph object itself is either the top level graph or it's been
     * added to the graph it belongs to.
     *
     * @param graphelem
     * @param g the graph object, already instantiated (but probably empty).
     */
    public void parseCGXMLGraphTagElement( Element graphelem, Graph g ) {
//        Global.info( "at parseCGXMLGraphElement parsing tag " + graphelem.getTagName() );
        setID( graphelem, g );
        // Handle any attributes of this graph
        NamedNodeMap map = graphelem.getAttributes();
        Node n = map.getNamedItem( "negated" );
        if ( n != null ) {
            g.setNegated( Boolean.parseBoolean( n.getNodeValue() ) );
        }

        NodeList nodes = graphelem.getChildNodes();
        // Add all the elements of this graph to it. 
        for ( int childnum = 0; childnum < nodes.getLength(); childnum++ ) {
            if ( nodes.item( childnum ).getNodeType() == Node.ELEMENT_NODE ) {
                // should be top level graph element
                Element element = (Element)nodes.item( childnum );
                String elementName = element.getNodeName();
                if ( elementName.equals( "layout" ) ) {
                    parseLayoutInfo( element, (GraphObject)g );
                } else if ( elementName.equals( "type" ) ) {
                    parseTypeInfo( element, (GraphObject)g );
                } else {
                    GraphObject go = instantiateGraphObject( element.getTagName() );
                    if ( go != null ) {
                        setID( element, go );       // make sure we set the id before adding to the graph
                        g.insertObject( go );
                        if ( isMakeList() ) {
                            _parsedObjects.add( go );
                        }
                        parseGraphObjectElement( element, go );
                    } else {
                        Global.info( "unknown element in graph is " + element.getTagName() );
                    }
                }
            }
        }
    }

//    public void parseCGXMLInputStreamDOM( InputStream is, Graph graph ) {
//        if ( _makeList ) {
//            _parsedObjects.clear();
//        }
//        //Global.info( "ready to parse... top level graph is " + graph.toString() );
//
//        buildDocument( is );
//        parseCGXMLCG( graph );
//        try {
//            is.close();
//        } catch ( IOException ex ) {
//
//        }
//    }

    /**
     * Assume document consists of only layout information. This is primarily
     * used by the CGIF parser for CG comments.
     */
    public void parseLayoutOnly( GraphObject go ) {
        Element docelem = doc.getDocumentElement();
        if ( docelem == null ) {
            return;
        }
        parseLayoutInfo( docelem, go );
    }
    /**
     * Handles the "type" tag element for the type label and for 
     * both generic descriptors and wordnet descriptors.
     * @see WordnetTypeDescriptor#getInstanceFromXMLDOM(Element)
     * @see GenericTypeDescriptor#getInstanceFromXMLDOM(Element)
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */

    public void parseTypeInfo( Element element, GraphObject go ) {
        NodeList elems = element.getElementsByTagName( "label" );
        if ( elems != null && elems.getLength() > 0 ) {
            String label = elems.item( 0 ).getTextContent();
            ( (GNode)go ).setTypeLabel( label );
        }
        ArrayList<AbstractTypeDescriptor> descriptors = new ArrayList<>();
        elems = element.getElementsByTagName( GenericTypeDescriptor.getTagName() );
        if ( elems != null && elems.getLength() > 0 ) {
            for ( int n = 0; n < elems.getLength(); n++ ) {
                AbstractTypeDescriptor descr = GenericTypeDescriptor.getInstanceFromXMLDOM( (Element)elems.item( 0 ) );
                descriptors.add( descr );
            }
        }
        elems = element.getElementsByTagName( WordnetTypeDescriptor.getTagName() );
        if ( elems != null && elems.getLength() > 0 ) {
            for ( int n = 0; n < elems.getLength(); n++ ) {
                AbstractTypeDescriptor descr = WordnetTypeDescriptor.getInstanceFromXMLDOM( (Element)elems.item( 0 ) );
                descriptors.add( descr );
            }
        }
        ( (GNode)go ).setTypeDescriptors( descriptors.toArray( new AbstractTypeDescriptor[ descriptors.size() ] ) );
    }

        /**
     * Handles the "referent" tag element 
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */

    public void parseReferentInfo( Element element, GraphObject go ) {
        NodeList elems = element.getElementsByTagName( "label" );
        if ( elems == null || elems.getLength() == 0 ) {
            return;
        }
        String label = element.getElementsByTagName( "label" ).item( 0 ).getTextContent();
        ( (Concept)go ).setReferent( label, false );
    }

    /**
     * Handles the "layout" tag element for the rectangle, color, font and edge elements.
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */
    public void parseLayoutInfo( Element layoutElem, GraphObject go ) {
        if ( layoutElem.getNodeType() != Node.ELEMENT_NODE ) {
            return;
        }
        if ( !layoutElem.getNodeName().equals( "layout" ) ) {
            return;
        }

        // Assume we now are looking at the layout element
        NodeList nodes = layoutElem.getElementsByTagName( "rectangle" );
        if ( nodes.getLength() > 0 ) {
            Node rectnode = nodes.item( 0 );
            parseRectangleInfo( rectnode, go );
        }

        nodes = layoutElem.getElementsByTagName( "color" );
        if ( nodes.getLength() > 0 ) {
            Node colornode = nodes.item( 0 );
            parseColorInfo( colornode, go );
        }

        nodes = layoutElem.getElementsByTagName( "font" );
        if ( nodes.getLength() > 0 ) {
            Node fontnode = nodes.item( 0 );
            parseFontInfo( fontnode, go );
        }

        nodes = layoutElem.getElementsByTagName( "edge" );
        if ( nodes.getLength() > 0 ) {
            Node fontnode = nodes.item( 0 );
            parseEdgeInfo( fontnode, go );
        }

    }

    /**
     * Assumes its node is a rectangle tag from Charger. Updates the graph
     * object according to the display rect implied by the rectangle.
     * Uses the pre-set offset for the entire parser.
     * @see #parseRectangleInfo(Node, GraphObject, Point2D.Double)
     * @param node
     * @param go
     */
    public void parseRectangleInfo( Node node, GraphObject go ) {
        parseRectangleInfo( node, go, getOffset() );
    }

        /**
     * Handles the "font" tag element for arrow height, width and edge thickness.
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */

    public void parseRectangleInfo( Node node, GraphObject go, Point2D.Double offset ) {
        NamedNodeMap map = node.getAttributes();
        double x = Double.parseDouble( getNamedAttributeFromMap( map, "x" ).replaceAll( ",", "" ) ) + offset.x;
        double y = Double.parseDouble( getNamedAttributeFromMap( map, "y" ).replaceAll( ",", "" ) ) + offset.y;
        double width = Double.parseDouble( getNamedAttributeFromMap( map, "width" ).replaceAll( ",", "" ) );
        double height = Double.parseDouble( getNamedAttributeFromMap( map, "height" ).replaceAll( ",", "" ) );

        go.setDisplayRect( new Rectangle2D.Double( x, y, width, height ) );
    }

    /**
     * Assumes its node is a color tag generated by Charger. Updates the graph object
     * according to the color info parsed.
     *
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */
    public void parseColorInfo( Node node, GraphObject go ) {
        NamedNodeMap map = node.getAttributes();
        String foreground = map.getNamedItem( "foreground" ).getNodeValue();
        String background = map.getNamedItem( "background" ).getNodeValue();

        Color forecolor = parseRGB( foreground );
        go.setColor( "text", forecolor );

        Color backcolor = parseRGB( background );
        go.setColor( "fill", backcolor );

    }
    /**
     * Handles the "font" tag element for arrow height, width and edge thickness.
     * @param node The DOM element containing the tag and its subtree.
     * @param go the graph object to be modified
     */
    public void parseFontInfo( Node node, GraphObject go ) {
        NamedNodeMap map = node.getAttributes();
        String fontname = getNamedAttributeFromMap( map, "name" );
        int fontstyle = Integer.parseInt( getNamedAttributeFromMap( map, "style" ) );
        int fontsize = Integer.parseInt( getNamedAttributeFromMap( map, "size" ) );

        go.setLabelFont( new Font( fontname, fontstyle, fontsize ) );
    }

    /**
     * Handles the "edge" tag element for arrow height, width and edge thickness.
     * @param node The DOM element containing the tag and its subtree.
     * @param go the edge to be modified
     */
    public void parseEdgeInfo( Node node, GraphObject go ) {
        NamedNodeMap map = node.getAttributes();
        int arrowHeadHeight = Global.userEdgeAttributes.getArrowHeadHeight();
        int arrowHeadWidth = Global.userEdgeAttributes.getArrowHeadWidth();
        double edgeThickness = Global.userEdgeAttributes.getEdgeThickness();

        if ( getNamedAttributeFromMap( map, "arrowHeadWidth" ) != null ) {
            arrowHeadWidth = Integer.parseInt( getNamedAttributeFromMap( map, "arrowHeadWidth" ) );
        }
        if ( getNamedAttributeFromMap( map, "arrowHeadHeight" ) != null ) {
            arrowHeadHeight = Integer.parseInt( getNamedAttributeFromMap( map, "arrowHeadHeight" ) );
        }
        if ( getNamedAttributeFromMap( map, "edgeThickness" ) != null ) {
            edgeThickness = Double.parseDouble( getNamedAttributeFromMap( map, "edgeThickness" ) );
        }

        if ( !( go instanceof GEdge ) ) {
            return;
        }
        GEdge ge = (GEdge)go;
        ge.setArrowHeadWidth( arrowHeadWidth );
        ge.setArrowHeadHeight( arrowHeadHeight );
        ge.setEdgeThickness( edgeThickness );
    }

    /**
     *
     * @param rgb a string of the form "rrr,ggg,bbb"
     * @return the corresponding color, or black if null argument.
     */
    public static Color parseRGB( String rgb ) {
        if ( rgb == null ) {
            return new Color( 0, 0, 0 );
        }
        StringTokenizer nums = new StringTokenizer( rgb );
        int r = Integer.parseInt( nums.nextToken( "," ) );
        int g = Integer.parseInt( nums.nextToken( "," ) );
        int b = Integer.parseInt( nums.nextToken( "," ) );
        return new Color( r, g, b );
    }

    /** Convenience method to retrieve the value of the attribute.
     * 
     * @param map Usually obtained from a getAttributes call in the DOM parser routines.
     * @param attrName the attribute whose value is requested.
     * @return the value of the named attribute; null if not found.
     */
    private String getNamedAttributeFromMap( NamedNodeMap map, String attrName ) {
        String s = map.getNamedItem( attrName ) == null ? null : map.getNamedItem( attrName ).getNodeValue();
        return s;
    }

    /**
     * Factory to create a graph object from the given tagname.
     * The tagname is the not the actual name of the class, but can be looked up in the tagname to classname map.
     * Instantiates the object, but doesn't fill in any information.
     * @see #CharGerXMLTagNameToClassName
     * @return an instantiated GraphObject of type indicated by the tagname; null if the name isn't recognized,
     */
    private GraphObject instantiateGraphObject( String tagname ) {
//        Global.info( "Instantiating object of type " + tagname );
        if ( CharGerXMLTagNameToClassName.isEmpty() ) {
            loadCharGerKeyWordToClassTable();
        }
        String t = CharGerXMLTagNameToClassName.getProperty( tagname, "DUMMY" );
        if ( t.equals( "DUMMY" ) ) {
            return null;
        }

        GraphObject go = null;
        Class objClass = null;
        try {
            objClass = Class.forName( "charger.obj." + t );
            go = (GraphObject)objClass.newInstance();
        } catch ( ClassNotFoundException ex ) {
            Global.error( "Parsing an illegal object tag " + tagname );
        } catch ( InstantiationException ex ) {
            Global.error( "Parsing an illegal object tag " + tagname );
        } catch ( IllegalAccessException ex ) {
            Global.error( "Parsing an illegal object tag " + tagname );
        }

        return go;
    }

    /**
     * Set the object's id, either by using its already-generated id or else by
     * the id attribute in the element. If not keeping old id's then put the new
     * id in the old-new id hashtable.
     *
     * @param elem Any element that might contain an "id" attribute
     * @param go The object whose id is to be set.
     */
    public void setID( Element elem, GraphObject go ) {
        NamedNodeMap docmap = elem.getAttributes();
        String oldID = getNamedAttributeFromMap( docmap, "id" );
        if ( oldID == null ) {
            return;        // a special case when copying, since the top level element has no info
        }
        if ( !isKeepIDs() ) {
            oldNewIDs.put( oldID, go.objectID.toString() );
        } else {
            go.objectID = new GraphObjectID( oldID );
        }
    }

    /**
     * Handles any document element representing a GraphObject.
     *
     * @param goelem The graphobject tag element. Uses the tag name to determine which type argument "go" is.
     * @param go The graph object whose properties are to be set from this. Must be already instantiated.
     * element.
     */
    public void parseGraphObjectElement( Element goelem, GraphObject go ) {
        String classname = CharGerXMLTagNameToClassName.getProperty( goelem.getTagName(), "DUMMY" );
        if ( !go.getClass().getName().endsWith( classname ) ) {
            Global.error( "CGXParser: processing xml tag \"" + goelem.getTagName()
                    + "\" with object type \"" + go.getClass().getName() + "\"." );
            return;
        }

        if ( go instanceof Graph ) {
            parseCGXMLGraphTagElement( goelem, (Graph)go );
            return;
        }
        // parse the tags that are enclosed
        NodeList nodes = goelem.getChildNodes();
        for ( int childnum = 0; childnum < nodes.getLength(); childnum++ ) {
            if ( nodes.item( childnum ).getNodeType() == Node.ELEMENT_NODE ) {
                // should be top level graph element
                Element element = (Element)nodes.item( childnum );
                String elementName = element.getNodeName();
                if ( elementName.equals( "type" ) ) {
                    parseTypeInfo( element, go );
                }
                if ( elementName.equals( "referent" ) ) {
                    parseReferentInfo( element, go );
                }
                if ( elementName.equals( "layout" ) ) {
                    parseLayoutInfo( element, go );
                }
            }
        }

        // parse the attributes in the tag
        NamedNodeMap docmap = goelem.getAttributes();

        if ( go instanceof GEdge ) {
            String label = getNamedAttributeFromMap( docmap, "label" );
            go.setTextLabel( label );
            String to = getNamedAttributeFromMap( docmap, "to" );
            String from = getNamedAttributeFromMap( docmap, "from" );
            if ( !isKeepIDs() ) {
                to = oldNewIDs.get( to );
                from = oldNewIDs.get( from );
            }
            if ( to == null || from == null ) {
                Global.error( "Error in parsing graph edge!" );
            } else {
                GEdge ge = (GEdge)go;
                ge.fromObj = _topLevelGraph.findByID( new GraphObjectID( from ) );
                ge.toObj = _topLevelGraph.findByID( new GraphObjectID( to ) );
                ( (GNode)( ge.fromObj ) ).attachGEdge( ge );
                ( (GNode)( ge.toObj ) ).attachGEdge( ge );
                ge.placeEdge();
            }

        }
    }
}
