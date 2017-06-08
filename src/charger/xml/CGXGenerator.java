//
//  XMLGenerator.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 04 2003.
//
package charger.xml;


import charger.Global;
import charger.obj.Concept;
import charger.obj.GEdge;
import charger.obj.GNode;
import charger.obj.GraphObjectID;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.obj.ShallowIterator;
import charger.gloss.AbstractTypeDescriptor;
import charger.util.CGUtil;
import charger.util.Util;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Iterator;

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
 * Used for generating XML representations of a conceptual graph.
 */
public class CGXGenerator extends XMLGenerator {


    /**
     * Prepares the entire graph in XML form.
     *
     * @param gr The graph to be represented
     * @return XML representation of the graph.
     */
    public static String generateXML( Graph gr ) {

        if ( gr.createdTimeStamp == null ) {
            gr.createdTimeStamp = Util.getFormattedCurrentDateTime();
        }
        
        gr.modifiedTimeStamp = Util.getFormattedCurrentDateTime();
        String parms =
                "editor=\"" + Global.EditorNameString + "\" "
                + "version=\"" + Global.CharGerVersion + "\" "
                + "created=\"" + gr.createdTimeStamp + "\" "
                + "modified=\"" + gr.modifiedTimeStamp + "\" "
                + "user=\"" + System.getProperty( "user.name" ) + "\" "
                + "wrapLabels=\"" + gr.getWrapLabels() + "\" "
                + "wrapColumns=\"" + gr.getWrapColumns() + "\"";

        return XMLHeader() + eol
                + //CGdoctypeHeader() + eol +
                startTag( "conceptualgraph", parms ) + eol
                + GraphObjectXML( gr, "" ) + eol
                + endTag( "conceptualgraph" ) + eol;
    }

    /**
     * Creates a DOCTYPE declaration for the XML file. NOT USED! SAX doesn't
     * know how to parse it.
     *
     * @return the
     * string <code>&lt;!DOCTYPE conceptualgraph PUBLIC "conceptualgraph.dtd"&gt;</code>
     */
    public static String CGdoctypeHeader() {
        return "<!DOCTYPE conceptualgraph PUBLIC \"conceptualgraph.dtd\">";
    }

    /**
     * String-ifies graph with objects in an order that has no internal forward
     * references, using XML format.
     *
     * @return string representing the graph in a safe order; i.e., every object
     * occurs before it is referenced (e.g., by a link)
     */
    public static String GraphXML( Graph graph, String indent ) {
        StringBuilder s = new StringBuilder( "" );

        /*if ( graph.getOwnerGraph() == null ) 		// a top-level graph
         {
         StringBuilder headerinfo = new StringBuilder( "" );
         if ( graph.getOwnerFrame() != null && graph.getOwnerFrame().)
         headerinfo.append( 
         s.append( startTag( "Graph", headerinfo );
         }
         else
         */
        GraphObject go = null;
        Graph g = null;

         // do graphs before edges so that all nodes will have been seen before any of their edges
        Iterator iter = new ShallowIterator( graph, GraphObject.Kind.GRAPH );
        while ( iter.hasNext() ) {
            g = (Graph)iter.next();
            s.append( GraphObjectXML( g, tab + indent ) );
        }
        iter = new ShallowIterator( graph, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            go = (GNode)iter.next();
            if ( !( go instanceof Graph ) ) // to prevent duplicate graphs from being generated
            {
                s.append( GraphObjectXML( go, tab + indent ) );
            }
        }


        iter = new ShallowIterator( graph, GraphObject.Kind.GEDGE );
        while ( iter.hasNext() ) {
            go = (GEdge)iter.next();
            s.append( GraphObjectXML( go, tab + indent ) );
        }

        return s.toString();
    }

    /**
     * Generates any graph object into its XML version.
     *
     * @param go The graph object. If it's a charger.obj.Graph, then GraphXML is
     * invoked on it, with recursion providing arbitrary nesting of graphs in
     * XML.
     * @param indent Text string to prepend before every line of the generated
     * XML. The method may provide additional indentation for readability.
     */
    public static String GraphObjectXML( GraphObject go, String indent ) {
        StringBuilder s = new StringBuilder( "" );

        if ( go == null ) {
            return "";
        }

        // Construct the parameter list for this object
        StringBuilder parms = new StringBuilder( "" );
        parms.append( "id=\"" + go.objectID + "\"" );

        GraphObjectID id = GraphObjectID.zero;
        if ( go.getOwnerGraph() != null ) {
            id = go.getOwnerGraph().objectID;
        }
        parms.append( " owner=\"" + id + "\"" );
        //Global.info( "looking at " + CGUtil.shortClassName( go ) + " with ident = " + go.objectID +
        //	"  owner = " + ident );

        //		NO longer making label a parameter; it's going to be a composite of type and referent tags
        if ( !( go instanceof GNode ) && !go.getTextLabel().equals( "" ) ) {
            parms.append( " label=\"" + quoteForXML(go.getTextLabel()) + "\"" );
        }

        if ( go instanceof Graph && ( (Graph)go ).isNegated() ) {
            parms.append( " negated=\"true\"" );
        }

        if ( go instanceof GEdge ) {
            GEdge ge = (GEdge)go;
            if ( ge.fromObj != null ) {
                parms.append( " from=\"" + ge.fromObj.objectID + "\"" );
            }
            if ( ge.toObj != null ) {
                parms.append( " to=\"" + ge.toObj.objectID + "\"" );
            }
                    // These are properly part of the layout tag, since they don't affect semantics.
//                parms.append( " edgeThickness=\"" + ge.getEdgeThickness() + "\"" );
//            if ( go instanceof Arrow || go instanceof GenSpecLink ) {
//                parms.append( " arrowHeadWidth=\"" + ge.getArrowHeadWidth() + "\"" );
//                parms.append( " arrowHeadHeight=\"" + ge.getArrowHeadHeight() + "\"" );
//            }
        }

        // Actually write the tag with its parameters
        s.append( indent + startTag( CGUtil.shortClassName( go ).toLowerCase(), parms.toString() ) + eol );

        if ( go instanceof Concept ) {
            s.append( typeRefInfoXML( (Concept)go, tab + indent ) );
        } else if ( go instanceof GNode ) {
            s.append( typeInfoXML( (GNode)go, tab + indent ) );
        }

        // Write the layout information
        s.append( layoutInfoXML( go, tab + indent ) );

        if ( go instanceof Graph ) {
            s.append( GraphXML( (Graph)go, indent ) );
        }

        s.append( indent + endTag( CGUtil.shortClassName( go ).toLowerCase() ) + eol );
        return s.toString();
    }


    private static String typeRefInfoXML( Concept c, String indent ) {
        StringBuilder s = new StringBuilder( "" );
        s.append( typeInfoXML( c, indent ) );
        s.append( refInfoXML( c, indent ) );
        return s.toString();
    }

    private static String typeInfoXML( GNode c, String indent ) {
        if ( c.getTypeLabel() == null || c.getTypeLabel().equals( "" ) ) {
            return "";
        }
        StringBuilder s = new StringBuilder( "" );
        s.append( indent + startTag( "type" ) + eol );
        s.append( tab + indent + simpleTaggedString( "label", c.getTypeLabel() ) + eol );
        if ( c.getTypeDescriptor() != null ) {
            AbstractTypeDescriptor[] ds = c.getTypeDescriptors();
            for ( int k = 0; k < ds.length; k++ ) {
                s.append( descriptorXMLTag( ds[ k], tab + indent ) + eol );
            }
        }
        s.append( indent + endTag( "type" ) + eol );
        return s.toString();
    }

    private static String descriptorXMLTag( AbstractTypeDescriptor info, String indent ) {
        return info.toXML( indent );
    }

    private static String refInfoXML( Concept c, String indent ) {
        if ( c.getReferent() == null || c.getReferent().equals( "" ) ) {
            return "";
        }
        StringBuilder s = new StringBuilder( "" );
        s.append( indent + startTag( "referent" ) + eol );
        s.append( tab + indent + simpleTaggedString( "label", c.getReferent() ) + eol );
        s.append( indent + endTag( "referent" ) + eol );
        return s.toString();
    }

    public static String layoutInfoXML( GraphObject go, String indent ) {
        StringBuilder s = new StringBuilder( "" );
        s.append( indent + startTag( "layout" ) + eol );
        s.append( tab + indent + tagWithParms( "rectangle", rectangleXMLParms( go.getDisplayRect() ) ) + eol );
        s.append( tab + indent + tagWithParms( "color", colorXMLParms( go ) ) + eol );
        s.append( tab + indent + tagWithParms( "font", fontXMLParms( go ) ) + eol );
        if ( go instanceof GEdge ) {
            GEdge edge = (GEdge)go;
            s.append(  tab + indent + tagWithParms( "edge", edgeXMLParms( edge ) ) + eol );
        }
        s.append( indent + endTag( "layout" ) + eol );
        return s.toString();
    }

    private static String rectangleXMLParms( Rectangle2D.Double r ) {
        DecimalFormat nformat = new DecimalFormat( "0.00");
        nformat.setMaximumFractionDigits( 2 );

        return "x=\"" + nformat.format( r.x ) + "\" y=\"" + nformat.format(r.y )
                + "\" width=\"" + nformat.format(r.width) + "\" height=\"" + nformat.format(r.height) + "\"";
    }

    private static String colorXMLParms( GraphObject go ) {
        return "foreground=\""
                + go.getColor( "text" ).getRed() + ","
                + go.getColor( "text" ).getGreen() + ","
                + go.getColor( "text" ).getBlue()
                + "\" background=\""
                + go.getColor( "fill" ).getRed() + ","
                + go.getColor( "fill" ).getGreen() + ","
                + go.getColor( "fill" ).getBlue()
                + "\"";
    }

    private static String fontXMLParms( GraphObject go ) {
        return "name=\"" + go.getLabelFont().getName() + "\" "
                + "style=\"" + go.getLabelFont().getStyle() + "\" "
                + "size=\"" + go.getLabelFont().getSize() + "\" ";
    }

    private static String edgeXMLParms( GEdge edge ) {
        return "arrowHeadWidth=\"" + edge.getArrowHeadWidth() + "\" "
                + "arrowHeadHeight=\"" + edge.getArrowHeadWidth() + "\" "
                + "edgeThickness=\"" + edge.getEdgeThickness() + "\" ";
    }
}
