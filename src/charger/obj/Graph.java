package charger.obj;

//import charger.util.*;
//import charger.exception.*;
//
//import java.util.*;
//import java.awt.*;
//import java.awt.print.*;
//import java.awt.geom.*;
//import charger.*;
import charger.gloss.AbstractTypeDescriptor;
import charger.EditFrame;
import charger.EditManager;
import charger.EditingChangeState;
import charger.Global;
import charger.exception.CGContextException;
import charger.util.CGUtil;
import charger.util.Util;
import charger.util.WrappedText;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import kb.KBException;
import kb.ObjectHistoryEvent;


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
 * A Graph stores zero or more graph objects. Since it is a graph object itself,
 * graphs can be arbitrarily nested. It is each graph's responsibility to store
 * its own components in its objectHashStore. Extends GNode because it has a
 * name, referent and can be linked as a GNode.
 * 
 * <p>The drawing of graphs as contexts is outlined as follows:
 * <dl>
 * <dt>Display Rectangle</dt>
 * <dd>
 * The display rectangle (displayRect) is the largest rectangle occupied by the graph.
 * All other elements must be no larger than this rectangle.
 * <dd>
 * <dt>Border</dt>
 * <dd>
 * The border is inscribed inside the display rectangle. In order to draw the border,
 * consider it drawn as lines positioned in the center of the border's line, whose line thickness
 * is defined by Global.contextBorderWidth
 * </dd>
 * <dt>Inner padding
 * <dd>
 * Inside the border, there is a (narrow) band of space so that the graph's contents
 * aren't touching the border. This means that the graph's inner contents must
 * fit inside a rectangle whose width is diplayrect.width - 2* contextBorderWidth - 2 * innerpadding
 * </dd>
 * <dt>Graph name</dt>
 * <dd>The graph's text label is inscribed at the top left of the graph's contents. Its 
 * lower left corner is therefore positioned at: displayrect.
 * This means that the graph's inner contents must fit inside a rectangle whose height is
 * diplayrect.height - 2* contextBorderWidth - 2 * innerpadding - textLabelHeight
 * </dd>
 * </dl>
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class Graph extends Concept implements Printable, kb.KnowledgeSource { // GNode {   

    /**
     * Holds the list of objects owned by this graph. Should be private.
     *
     * @see Graph#findByID
     */
    public HashMap<String, GraphObject> objectHashStore = new HashMap<String, GraphObject>( 10 );

    /**
     * The actual width of the context's displayed border; included in its
     * displayRect
     */
    public static int contextBorderWidth =
            Integer.parseInt( Global.Prefs.getProperty( "contextBorderWidth", "4" ) );
    /**
     * The "breathing room" spacing inside a context's border
     */
    public static int contextInnerPadding =
            ( ( Global.showShadows ) ? ( 5 + 4 ) : ( 5 ) );
//            ( ( Global.showShadows ) ? ( contextBorderWidth + 4 ) : ( contextBorderWidth + 2 ) );
    
    public Point2D textCenterPt = null;
    //Integer.parseInt( Hub.Prefs.getProperty( "contextInnerPadding", "5" ));
    /**
     * The user interface frame that contains this graph. If not outermost, or
     * if we're internal without a frame, expect null.
     */
    protected EditFrame ownerFrame = null;
    
    public String createdTimeStamp = Util.getFormattedCurrentDateTime();
    public String modifiedTimeStamp = Util.getFormattedCurrentDateTime();
    
    public boolean wrapLabels = GraphObject.defaultWrapLabels;
    public int wrapColumns  = GraphObject.defaultWrapColumns;

    /**
     * Constructs a new Graph object, with a given parent graph within which it
     * is enclosed. Same as Graph()
     *
     * @param g existing graph to which this one is to be subordinate; null if
     * there isn't one.
     *
     */
    public Graph( Graph g ) {
        graphConstructor( g );
    }

    /**
     * Constructs a new Graph object, with a given parent graph within which it
     * is enclosed. Same as Graph( null )
     *
     */
    public Graph() {
        graphConstructor( null );
    }

    private void graphConstructor( Graph g ) {
//        setDisplayRect( new Rectangle2D.Double( 0, 0, 0, 0 ) );	// inserted to solve bug 09-14-03
        myKind = GraphObject.Kind.GRAPH;
        foreColor = (Color)( Global.userForeground.get( CGUtil.shortClassName( this ) ) );
        backColor = (Color)( Global.userBackground.get( CGUtil.shortClassName( this ) ) );
        // tell CharGer who owns me
//        ownerGraph = g;       // hsd commented 11-3-14 - prevented objectid from being inserted!
        if ( g != null ) 
            g.insertObject( this );
        else
            ownerGraph = null;
        // Global.info( "set owner of " + getTextLabel() + " to " + g.getOwnerGraph().getTextLabel() );
        // WARNING disabled, so that we can draw without having to be in a frame
        //if ( g != null && g.ownerFrame == null ) 	// if we're not the very outermost graph in this frame
        //	Hub.warning( "Graph " + getTextLabel() + " has no owner graph." );
        //setTextLabel( "Proposition" );
        if ( Global.defaultContextLabel.equals( "(none)" ) ) {
//            setTextLabel( " " );
            textLabel = " ";
        } else {
//            setTextLabel( Global.defaultContextLabel );
            textLabel = Global.defaultContextLabel;
        }
        setTextLabelPos();
        // set up Notio concept that I'm a referent descriptor for, and add it to the graph

    }

    /**
     * Find the user window (if any) associated with this graph.
     *
     * @return The user interface frame that contains this graph. If not
     * outermost, or if the graph is internal without a frame, return null.
     */
    public EditFrame getOwnerFrame() {
        return ownerFrame;
    }

    /**
     * Sets the editing window frame that contains this graph. If not outermost,
     * or if the graph is internal without a frame, pass null.
     */
    public void setOwnerFrame( EditFrame ef ) {
        // should probably check to see if this graph isn't already owned.
        ownerFrame = ef;
    }

    /**
     * Sets the position of this graph to be its center. Used to force
     * re-evaluation of its dimensions and the dimensions of its links. Same as
     * setCenter( getCenter() ).
     *
     * @see Graph#setCenter
     */
    public void setCenter() {
        this.setCenter( this.getCenter() );
    }

    /**
     * Calls the super's setCenter,  changes the textLabelLowerLeftPt
     * point to put the label in the upper left hand corner of the context.
     * Moves all of the context's contents so they maintain the same relative position
     * inside the context.
     * @param	p	the new center point, although internally the top left corner is
     * saved in the displayRect.
     * @see GNode#setCenter
     */
    public void setCenter( Point2D.Double p ) {
        Point2D.Double oldCenter = getCenter();
        super.setCenter( p );	// sets lower left point for text label too, so we alter it
        setTextLabelPos();
            // move all its contents relative to the old center
//        Point2D.Double translationVector = new Point2D.Double( p.x - oldCenter.x, p.y - oldCenter.y );
//        ShallowIterator iter = new ShallowIterator( this );
//        while (iter.hasNext()) {
//            GraphObject go = (GraphObject)iter.next();
//            go.setCenter( new Point2D.Double( go.getCenter().x + translationVector.x, go.getCenter().y + translationVector.y ) );
//        }
        //Global.info( "set center for a graph: displayRect is " + displayRect );
    }

    /**
     * Sets the position of the text label, based on the center point.
     *
     * @see CGUtil#getStringLowerLeftFromCenter
     */
    public void setTextLabelPos() {
        EditFrame ef = getOwnerFrame();
        FontMetrics fm = null;
        if ( ef != null ) {
            fm = ef.currentFontMetrics;
        } else {
            fm = Global.defaultFontMetrics;
        }

        textLabelLowerLeftPt = CGUtil.getStringLowerLeftFromCenter( fm, textLabel, getCenter() );

        textLabelLowerLeftPt.x = getUpperLeft().x + Graph.contextInnerPadding;
        textLabelLowerLeftPt.y =
                getUpperLeft().y + Graph.contextInnerPadding + 2.0f * ( textLabelLowerLeftPt.y - getCenter().y );
    }

    /**
     * Used by the PrintJob class to print the graph.
     *
     * @see EditManager#performActionPrintGraph
     */
    public int print( Graphics g, PageFormat pf, int pageIndex )
            throws PrinterException {
        Graphics2D graphics = (Graphics2D)g;
        Paper p = pf.getPaper();
        // find out where the limits of the graph are to print
        Rectangle2D.Double printableBounds = this.getDisplayBounds();

        // figure out where to translate the corner point
        Point graphImageableOffset = new Point(
                (int)p.getImageableX() + 0,
                (int)p.getImageableY() + 0 );

        double scaleFactor = 1.00;	// assume we don't need to scale the printed picture
        int widthUnitsNeeded = (int)( printableBounds.width + printableBounds.x + contextBorderWidth );
        int heightUnitsNeeded = (int)( printableBounds.height + printableBounds.y + contextBorderWidth );

        if ( pf.getOrientation() == PageFormat.LANDSCAPE ) {
                    // swap height and width
            int temp = widthUnitsNeeded;
            widthUnitsNeeded = heightUnitsNeeded;
            heightUnitsNeeded = temp;
        }
        // if height pixels won't fit, then scale to the height
        // note: scale factor is the multiplier for how the image looks on the page
        // e.g., scale factor of 2.0 means image will be twice its normal size
        if ( p.getImageableHeight() < heightUnitsNeeded ) {
            scaleFactor = p.getImageableHeight() / heightUnitsNeeded;
        }
        // if width pixels won't fit (whether scaled or not) then scale the width
        if ( p.getImageableWidth() < ( widthUnitsNeeded * scaleFactor ) ) {
            scaleFactor = p.getImageableWidth() / widthUnitsNeeded;
        }
        //Global.info( "scale factor for printing is: " + scaleFactor );
        if ( Global.showFooterOnPrint && scaleFactor < 1.0 ) {
            scaleFactor = 0.95 * scaleFactor;	// make smaller to leave room for footer
        }
        Font footerfont = graphics.getFont().deriveFont( (float)graphics.getFont().getSize() / (float)scaleFactor );

        graphics.translate(
                graphImageableOffset.x,
                graphImageableOffset.y );
        graphics.scale( scaleFactor, scaleFactor );

        graphics.setFont( ownerFrame.currentFont );
        this.draw( graphics, true );

        // prepare the footer, whether printing or not
        String footer = "";
        if ( ownerFrame != null ) {
            if ( ownerFrame.graphAbsoluteFile != null ) {
                footer = ownerFrame.graphAbsoluteFile.getAbsolutePath();
            } else {
                footer = ownerFrame.graphName;
            }
        }
        if ( footer.length() > 72 ) {
            footer = footer.substring( footer.length() - 72 );
        }
        //String footer = path + " - Page " + (pageIndex + 1);
        // scale the font size upward so that the possibly scaled result is the right size
        graphics.setFont( footerfont );
        int swidth = graphics.getFontMetrics().stringWidth( footer );
        int sheight = graphics.getFontMetrics().getHeight();
        // actually draw the footer, using the possibly expanded page size as a basis
        if ( Global.showFooterOnPrint ) {
            graphics.setColor( Color.black );
            int footerx = (int)( ( p.getImageableWidth() / scaleFactor ) / 2 - swidth / 2 );		// center
            int footery = (int)( ( p.getImageableHeight() - sheight ) / scaleFactor );	// bottom
            if ( pf.getOrientation() == PageFormat.LANDSCAPE ) {
                footerx = (int)( ( p.getImageableHeight() / scaleFactor ) / 2 - swidth / 2 );		// center
                footery = (int)( ( p.getImageableWidth() - sheight ) / scaleFactor );	// bottom
            }
            graphics.drawString( footer, footerx, footery );
            //Global.info( "footer at (" + footerx + "," + footery + "): " + footer );

            int centerx = (int)( p.getImageableWidth() / scaleFactor ) / 2;
            int centery = (int)( p.getImageableHeight() / scaleFactor ) / 2;
            if ( pf.getOrientation() == PageFormat.LANDSCAPE ) {
                int temp = centerx;
                centerx = centery;
                centery = temp;
            }
            //graphics.drawString( "DRAFT!!", centerx, centery );
        }

        if ( pageIndex == 0 ) {
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    /**
     * Find the outer boundary of the graph, taking into account the border width, 
     * but does NOT consider shadows (we let arrows and other things cover up shadows).
     * @see GNode#getShape
     */
    public Shape getShape() {
        if ( isNegated() ) {
            return new RoundRectangle2D.Double(
                    getUpperLeft().x, getUpperLeft().y, getDim().width, getDim().height, 36.0f, 36.0f );
        } else //return Util.make2DDouble( displayRect );
        {
            return displayRect;
        }
    }
    
    /**
     * Finds the center line of the border, so that drawing the border will 
     * take place entirely within the display rectangle.
     * 
     * @return The graph's rectangle, shrunk in all dimensions by
     * contextBorderWidth.
     */
    public Shape getShapeForDrawing() {
        Rectangle2D.Double rect = Util.make2DDouble( displayRect );
        CGUtil.grow( rect, -1 * contextBorderWidth/2, -1 * contextBorderWidth/2 );
        if ( isNegated() ) {
            return new RoundRectangle2D.Double(
                    rect.x, rect.y, rect.width, rect.height, 36.0f, 36.0f );
        } else //return Util.make2DDouble( displayRect );
        {
            return rect;
        }
    }
    
    /**
     * Starting with the graph's already-defined display rectangle, 
     * takes into account the context's border, the margin, and the current 
     * label's height.
     * @return the region of this graph/context that is available for content. 
     */
    public Rectangle2D.Double getRegionAvailableForContent() {
        double x, y, width, height = 0.0;
        Dimension textsize = getTextLabelSize();
        double labelHeight = textsize.height;
        x = displayRect.x + contextBorderWidth + contextInnerPadding;
        y = displayRect.y + contextBorderWidth + contextInnerPadding + labelHeight;
        double textWidthNeeded = textsize.width;
        width = displayRect.width - 2 * contextBorderWidth - 2 * contextInnerPadding;
        if ( textWidthNeeded < width ) {
            width = textWidthNeeded - 2 * contextBorderWidth - 2 * contextInnerPadding;
        }
        height = displayRect.height - 2 * contextBorderWidth - 2 * contextInnerPadding - getTextLabelSize().height;
        return new Rectangle2D.Double( x, y, width, height );
    }
    
    /**
     * Sets the display rect of the graph to contain the content as well as the border, margin and label.
     * @param rect 
     */
    public void setRegionAvailableForContent( Rectangle2D.Double rect ) {
        double labelHeight = getTextLabelSize().height;
        displayRect.x = rect.x - contextBorderWidth - contextInnerPadding;
        displayRect.y = rect.y - contextBorderWidth - contextInnerPadding - labelHeight;
        displayRect.width = rect.width  + 2 * contextBorderWidth + 2 * contextInnerPadding;
        displayRect.height = rect.height + 2 * contextBorderWidth + 2 * contextInnerPadding + getTextLabelSize().height;
    }
            

    /**
     * Draws the border of a context. Graphs are handled specially because to
     * draw a graph means not only drawing its border but also drawing its
     * contents. To ease recursive routines, the draw routine call this one.
     * (Currently, not used; the draw method does this too.)
     *
     * @see Graph#draw
     */
    public void drawBorder( Graphics2D g, Color borderColor ) {
        g.setColor( borderColor );
        g.setStroke( new BasicStroke( ( (float)contextBorderWidth ) / 2.0f ) );
        g.fill( g.getStroke().createStrokedShape( getShape() ) );
        g.setStroke( Global.defaultStroke );
    }

    /**
     * Renders the graph, recursively rendering all of it contents. If there's
     * an owner graph, draw a context rectangle INSIDE its actual border. If
     * this graph is currently selected, then draw the narrow selection
     * rectangle. Invoke draw on each of the graph's elements.
     *
     * @param g2D The 2D graphics on which to draw
     * @param printing if true, then translate to top left and be sure to set
     * font, etc.
     */
    public void draw( Graphics2D g2D, boolean printing ) {

        Rectangle2D.Double printableBounds = null;
        Dimension dim = null;
        if ( printing ) {
        }

        if ( ownerGraph == null ) {
//            if ( getOwnerFrame() != null ) {
//                g2D.setFont( getOwnerFrame().currentFont );
//            } else if ( Global.defaultFont != null ) {
//                g2D.setFont( Global.defaultFont );
//            }
            g2D.setFont( getLabelFont() );
            if ( printing ) {
                printableBounds = getDisplayBounds();
                dim = new Dimension( (int)printableBounds.width, (int)printableBounds.height );
                        // move to upper right corner of rectangle to be drawn
                g2D.translate( -1 * printableBounds.x, -1 * printableBounds.y );
                g2D.setColor( Color.white );
                //g2D.fillRect( printableBounds.x, printableBounds.y, dim.width, dim.height );
                g2D.fill( printableBounds );
            }

        } else {       // draw the border of the context after its contents

            g2D.setStroke( new BasicStroke( ( (float)contextBorderWidth ) / 2.0f ) );

            // if shadow is to be drawn, draw it first so it's covered up
            if ( Global.showShadows ) {
                // draw "shadow"
                g2D.setColor( Global.shadowColor );
                g2D.translate( Global.shadowOffset.x, Global.shadowOffset.y );
                g2D.fill( g2D.getStroke().createStrokedShape( getShapeForDrawing() ) );       // here should be small rectangle so border lies inside displayrect
                g2D.translate( -1 * Global.shadowOffset.x, -1 * Global.shadowOffset.y );
            }

            // decide how to draw negations
            if ( isNegated() && Global.showCutOrnamented ) {
                if ( this.isNegativelyNested() ) // all negatively nested elements,
                // including regular contexts, are shaded
                {           // use a lighter color for the shading, find a color 2/3 from background to white
                    g2D.setColor( new Color( backColor.getRed() * 1 / 3 + 255 * 2 / 3,
                            backColor.getGreen() * 1 / 3 + 255 * 2 / 3,
                            backColor.getBlue() * 1 / 3 + 255 * 2 / 3 ) );
                    g2D.fill( getShape() );
//                    g2D.drawString( " NOT ", getPos().x, getPos().y );    // an interesting idea
                } else {
                    g2D.setColor( Color.white );      // overdraw it in white if other colors are needed
                    g2D.fill( getShape() );
                }
            }
            // try to mimic the selection algorithm's geometry
            g2D.setColor( foreColor );
            g2D.draw( g2D.getStroke().createStrokedShape( getShapeForDrawing() ) );
            // g2D.setStroke( Hub.defaultStroke );

            g2D.setColor( foreColor );
//            g2D.drawString( textLabel, textLabelLowerLeftPt.x, textLabelLowerLeftPt.y );
            WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
            text.setEnableWrapping( getWrapLabels() );
            Dimension size = text.getSize( g2D );
            Point2D.Double textCenterPt = new Point2D.Double( 
                    getUpperLeft().x + (float)size.width / 2 + Graph.contextInnerPadding,
                    getUpperLeft().y + (float)size.height / 2 + Graph.contextInnerPadding);
            text.drawWrappedText( g2D, textCenterPt, getLabelFont() );
        }



        if ( Global.ShowBoringDebugInfo ) {
//            Font oldFont = g2D.getFont();
//            g2D.setFont( GNode.showBoringDebugInfoFont );
            g2D.drawString( "ID:" + objectID.getShort(), (float)getCenter().x - 2, (float)getUpperLeft().y + 10 );

            g2D.setColor( Color.yellow );
            //g2D.drawRect( displayRect.x, displayRect.y, displayRect.width, displayRect.height );
            g2D.draw( getShapeForDrawing() );
            Rectangle2D.Double r = getContentBounds();
            g2D.setColor( Color.red );
            //g2D.drawRect( r.x, r.y, r.width, r.height );
            g2D.draw( r );
            g2D.setColor( Color.green );
            g2D.fill( new Rectangle2D.Double( textLabelLowerLeftPt.x, textLabelLowerLeftPt.y, 2, 2 ) );
            
              g2D.setColor( Color.orange );
          g2D.draw( this.getContentBounds());
            super.drawDebuggingInfo( g2D );

//            g2D.setColor( Color.black );
//            CGUtil.showPoint( g2D, new Point2D.Double( displayRect.x, displayRect.y ) );
////            CGUtil.showPoint( g2D,
////                    new Point2D.Double( displayRect.x + displayRect.width, displayRect.y + displayRect.height ) );
//                        // drawString doesn't work with doubles :(
//            g2D.drawString( "" + Math.round(displayRect.height), (float)displayRect.x + 5, (float)displayRect.y + (float)displayRect.height / 2 );
//            g2D.drawString( "" + Math.round(displayRect.width),
//                    (float)displayRect.x + (float)displayRect.width / 2, (float)displayRect.y + (float)displayRect.height - 5 );
//            g2D.setFont( oldFont );
        }

        // Now draw all of the components inside the graph
        Iterator iter = new ShallowIterator( this, GraphObject.Kind.GRAPH );
        GraphObject go;
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            go.draw( g2D, printing );
        }

        iter = new ShallowIterator( this, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            go.draw( g2D, printing );
        }

        iter = new ShallowIterator( this, GraphObject.Kind.GEDGE );
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            go.draw( g2D, printing );
        }
        if ( !printing ) {
            if ( isSelected && this.myKind == GraphObject.Kind.GRAPH && this.getOwnerGraph() != null ) {
                g2D.setColor( GraphObject.defaultSelectColor );
                Rectangle2D.Double drect = (Rectangle2D.Double)displayRect.clone();
                g2D.draw( drect );
                CGUtil.grow( drect, -1, -1 );
                g2D.draw( new Rectangle2D.Double( drect.x + 1, drect.y + 1, drect.width - 2, drect.height - 2 ) );
            }
        }
        /*  else if ( ownerGraph == null )
         {
         g2D.translate( -1 * printableBounds.x, -1 * printableBounds.y );
         }
         */
    }


    /*============ delete operations
     charger	notio	op
     yes		no		delete arrow
     yes		no		delete concept with an arrow attached
     yes		yes	make a context
     yes		yes	delete concept no arrow
     yes		no		delete a context

     */
    /**
     * Attaches the given graph object to a CharGer graph, but doesn't do any
     * consistency check. Doesn't attach any arcs or anything else.
     */
    public void insertInCharGerGraph( GraphObject go ) {
        go.ownerGraph = this;
        objectHashStore.put( go.objectID.toString(), go );
//        Global.info( "Adding object " + go.objectID + " to graph " + this.objectID );
    }

    /**
     */
    /*	protected void insertInNotioGraph( GraphObject go )
     {
     try {
     if ( Hub.ShowBoringDebugInfo )
     Global.info( "add to Notio graph the counterpart of " + CGUtil.shortClassName( go ) );
     if ( go instanceof Concept ) nxGraph.addConcept( ((Concept)go).nxConcept );
     else if ( go instanceof Relation ) nxGraph.addRelation( ((Relation)go).nxRelation );
     else if ( go instanceof Actor ) nxGraph.addRelation( ((Actor)go).nxActor );
     else if ( go instanceof Graph ) nxGraph.addConcept( ((Graph)go).nxConcept );
     else if ( go instanceof TypeLabel ) 
     {
     try {
     Hub.KB.getConceptTypeHierarchy().addTypeToHierarchy( ((TypeLabel)go).nxConceptType );
     } catch ( notio.TypeAddError  ee ) { }
     }
     else if ( go instanceof RelationLabel ) 
     {
     try {
     Hub.KB.getRelationTypeHierarchy().addTypeToHierarchy( ((RelationLabel)go).nxRelationType );
     } catch ( notio.TypeAddError  ee ) { }
     }
     } catch ( notio.OperationError oe ) { 
     Hub.error( "Notio operation error on Object " + go.getTextLabel() + ": " +  oe.getMessage());
     oe.printStackTrace();
     }

     }
     */
    /**
     * Detaches the given graph object from a CharGer graph. Any remaining links
     * or pointers in the graph object are the responsibility of the
     * implementer.
     */
    public void removeFromGraph( GraphObject go ) {
        try {
            Global.sessionKB.unCommit( go );
        } catch ( KBException ex ) {
            Global.warning( ex.getMessage() + " on object " + ex.getSource().toString() );
            //Logger.getLogger( CanvasPanel.class.getName() ).log( Level.SEVERE, null, ex );

        }

        int old = 0;
        if ( !objectHashStore.containsKey( go.objectID.toString() ) ) {
            // Hub.error( "Graph.removeObject! Tried to remove objectID " + go.objectID + 
            // 		" from graph " + this.objectID + " but it wasn't found." );
        } else {
            objectHashStore.remove( go.objectID.toString() );
            //Global.info("removing from graph's object list: " + CGUtil.shortClassName( go ) );
            //Global.info( "Removing object " + go.objectID + " from graph " + this.objectID );
        }
    }
    
    /**
     * Forces an unverified move for the graph and all its contained objects.
     * Does not recursively call itself on its nested contents; 
     * simply grabs  all content however nested and moves them.
     * Adjusts the node edges to correspond to the new positions.
     * @param delta
     * @return whether anything actually changed
     */
    public boolean forceDeepMove( Point2D.Double delta ) {
        boolean changed = false;
        changed = forceMove( delta );
        DeepIterator iter = new DeepIterator( this );
//            Global.info( "Iterator started... with " + iter.howMany() + " objects.");
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
//                    Global.info( "iterator next object is " + go.getTextLabel() );
            go.forceMove( delta );
            if ( go instanceof GNode )
                ((GNode)go).adjustEdges();
        }
         iter = new DeepIterator( this );
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
            if ( go instanceof GEdge ) {
                ((GEdge)go).placeEdge();
            }
        }
        return changed;
    }

    /**
     * Forces an un-verified move of all objects, with identical moves for all objects.
     * Does no checking for overlap or context curruption or edge corruption.
     * To be used only when it is certain everything is to be moved.
     * @param objectsToMove
     * @param delta
     * @return whether anything actually changed
     */
    public static boolean forceMoveGraphObjects( ArrayList<GraphObject> objectsToMove, Point2D.Double delta ) {
        boolean changed = false;
        for ( GraphObject go : objectsToMove ) {
            if ( go instanceof Graph ) {
                Graph graph = (Graph)go;
                changed = changed || graph.forceMove( delta );
            } else {
                changed = changed || go.forceMove( delta );
            }
        } 
            return changed;
    }
    
    

    /**
     * Moves a set of objects on the canvas through a common x,y displacement.
     * Assumes that if a context is moved, then all of the context's contents are to be
     * moved with it. Intended for use when the objects to move are all contained
     * (directly or nested) in this graph.
     *
     * @param delta the translation displacement
     * @param objectsToMove the collection of objects to be displaced
     * @return a change state indicating whether semantics, appearance or
     * nothing has changed
     */
    public EditingChangeState moveGraphObjects( ArrayList objectsToMove, Point2D.Double delta ) {
        if ( delta.equals( new Point2D.Double( 0, 0 )))
            return new EditingChangeState();
        /* Algorithm:
         work top-down from g
         if it's a graph, call algorithm recursively on that graph
         */
        // put any un-handled nodes here for later handling
        ArrayList<GNode> nonGraphGNodes = new ArrayList<>();
//        ArrayList<GEdge> gEdges = new ArrayList<>();
        ArrayList<Graph> graphs = new ArrayList<>();
//        GraphObject go = null;
        Graph gr = null;
//        GraphObject igo = null;
        Point2D.Double newPos = null;
        EditingChangeState changeState = new EditingChangeState();
        boolean contextChanged = false;
        boolean gotChanged = false;		// did anything get changed?

        // check every element to see if it's a graph
        // separate the sheep from the goats so we can move all the nodes
        // and then tell the edges to move their ends accordingly.
        // Need to make sure that when moving a graph, all of its objects are removed from the nonGraphNodes so they don't get moved twice.
        for ( Object o : objectsToMove ) {
            GraphObject go = (GraphObject)o;
            if ( go instanceof Graph ) {
                graphs.add( (Graph)go );
            } else if ( go instanceof GNode ) {
                nonGraphGNodes.add( (GNode)go );
            } else if ( go instanceof GEdge ) {
                        // Let the edges follow their nodes...
//                gEdges.add( (GEdge)go );
            }
        }

                // For each graph, remove its non-graph nodes from the non-graph nodes list
                // They'll be picked up by movegraph and we don't want to move them twice
        for ( Graph graph : graphs ) {
            Iterator<GraphObject> iter = graph.graphObjects();
            while ( iter.hasNext()) {
                GraphObject go = (GraphObject)iter.next();
                if ( go instanceof GNode && ! (go instanceof Graph) && nonGraphGNodes.contains( go ) ) {
                    nonGraphGNodes.remove( go );
                }
            }
//            changeState = graph.moveGraph( delta );
            gotChanged = gotChanged || graph.forceDeepMove( delta );
            graph.adjustEdges();
        }

        for ( GNode node : nonGraphGNodes ) {
            gotChanged = true;
//                Global.info( "  in moveGraphObjects - non graph node " + node.getTextLabel() );
            newPos = new Point2D.Double( node.getCenter().x + delta.x, node.getCenter().y + delta.y );
            contextChanged = GraphObject.putInCorrectContext( this, node, newPos );
            node.setCenterOnly( newPos );       // BUG: this prevents moved nodes from adjusting their enclosing context!!
//            if ( node.getOwnerGraph() != null ) {
//                    Global.info( "calling resize for contents from graph " + getTextLabel() + " while moving " + node.getTextLabel());
                node.getOwnerGraph().resizeForContents( null );
//            }
            node.adjustEdges();
//                Global.info( "Moved non-graph node " + node.getTextLabel() + " by " + delta );
        }
        changeState.setAppearanceChanged( gotChanged );
        changeState.setSemanticsChanged( contextChanged );

        this.handleContextLinks();

        return changeState;
    }

    /**
     * Inserts an object into the graph, proposing the target graph as its
     * owner. In general, this is just a wrapper for insertIncharGerGraph,
     * except in one set of cases: where a GEdge crosses a context boundary. In
     * that case, this method finds the two end nodes of the edge, and inserts
     * the edge into the innermost enclosing context that also encloses both of
     * the end nodes.
     *
     * @param go Object to be added to the target graph.
     * @see Graph#insertInCharGerGraph
     * @see Graph#forgetObject
     */
    public void insertObject( GraphObject go ) {
        if ( go == this ) {
            return;       // prevent adding a graph object to itself, with overflow results
        }        // if the object is an edge, find the most dominant context for it. 
        if ( go instanceof GEdge && ( (GEdge)go ).fromObj != null && ( (GEdge)go ).toObj != null /*   && ( (GEdge)go ).toObj.getOwnerGraph() != ( (GEdge)go ).fromObj.getOwnerGraph() */ ) {
            // above commented out 9-16-2014 because we always want to make sure edges are inserted
            // at the most immediate dominant context
            ArrayList v = new ArrayList();
            v.add( ( (GEdge)go ).toObj );
            v.add( ( (GEdge)go ).fromObj );
            try {
                Graph dominant = GraphObject.findDominantContext( v );
                dominant.insertInCharGerGraph( go );
            } catch ( CGContextException e ) {
                insertInCharGerGraph( go );
            }
        } else //Global.info("inserting " + go.getTextLabel() + " into graph " + this.getTextLabel() );
        {
            insertInCharGerGraph( go );
        }
        try {
            //insertInNotioGraph( go );
            Global.sessionKB.commit( go );
        } catch ( KBException ex ) {
            Global.warning( ex.getMessage() + " on object " + ex.getSource().toString() );
            //Logger.getLogger( Graph.class.getName() ).log( Level.SEVERE, null, ex );

        }

    }

    /**
     * Removes object from this graph, removes it from the knowledge base and
     * logically garbages the object. This operation should result in a
     * correctly formed graph.
     *
     * @param go object to be erased from target graph
     */
    public void forgetObject( GraphObject go ) { //throws CGStorageError {
        go.abandonObject();     // remove from visible graph structure

        go.unCommitFromKnowledgeBase( Global.sessionKB );
        Global.deactivateID( go.objectID );
        //Global.info( "forget object " + go.toString() );
        go.selfCleanup();       // really only matters for actors
        go.getOwnerGraph().removeFromGraph( go );

    }

    /**
     * Sets the text label of the graph object. May re-size the node if the
     * label is too long.
     *
     * @param s new text label (erases the old text label)
     * @param fmetrics font metrics needed in case the label is too big or
     * requires resizing the graph
     * @param p the point around which the label is centered
     */
    public void setTextLabel( String s, FontMetrics fmetrics, Point2D.Double p ) {
        //Global.info( "in Graph: set text label " + s + " display rect is " + getDisplayRect() );
//        super.setTextLabel( s, fmetrics, p );
        textLabel = s;
        resizeForContents( p );
        // take care of the case where the new label is too big to fit
    }

    /**
     * Looks at every object in target graph and adjusts the graph's displayRect
     * to enclose them. If there's no graphics context on which to display the graph, then don't
     * bother.
     *
     * @param center Center point around which to adjust the display rect;
     * <code>null</code> means to move the center point if necessary. Leaves
     * dimensions alone unless they don't enclose the contents.
     */
    public void resizeForContents( Point2D.Double center ) {
        if ( isEmpty() ) {
            return;
        }
        // if there's no ownerFrame or it's not visible, then don't bother
        if ( ownerGraph == null ) {
            return;
        }
        if ( getOutermostGraph().getOwnerFrame() == null ) {
            return;
        }
        if ( !getOutermostGraph().getOwnerFrame().isVisible() ) {
            return;
        }
        // an empty rectangle for a graph indicates that we haven't yet sized it, so ignore its bounds
        // this is to help solve intermittent bugs where the default bound (upper left corner) was
        // interfering with properly sizing a context as it was being filled
        if ( getDisplayRect().isEmpty() ) {
//            return;       // disabled 09-20-14 because we might need to be laying out a context for the first time
        }
        // find the minimum rect needed for the contents
        Rectangle2D.Double contentBounds = ( (Graph)this ).getContentBounds();
        if ( contentBounds.x <= 0 || contentBounds.y <= 0 ) {
            Global.warning( "Graph bound is off the canvas! bound.x = " + contentBounds.x + ", bound.y = " + contentBounds.y );
        }

                Global.info( "resizing graph for content, " + this.getTextLabel() + " content rect is " + getRegionAvailableForContent() +
                "\n  content bounds are " + contentBounds );


        Rectangle2D.Double proposedRectangle = this.getRegionAvailableForContent();
        
        proposedRectangle.add( contentBounds );
        
        this.setRegionAvailableForContent( proposedRectangle );
        
        if ( center != null ) {
            setCenterOnly( center );
        }
//
        setTextLabelPos();
        adjustEdges();
        //Global.info( "after resizing graph " + this.getTextLabel() + " rect is " + displayRect );

        if ( getOwnerGraph() != null ) {
            this.getOwnerGraph().resizeForContents( null );
        }
        //Global.info( "through adjusting, this " + this.getTextLabel() + " rect is " + displayRect );
    }
    
        /**
     * Determines whether this graph is nested (at any level) within a given graph.
     * Forbids overlapping contexts; i.e., every graph has at most one enclosing
     * graph.
     *
     * @param gouter is the potential outermost graph
     * @return true if target graph is nested logically (at any level) within
     * gouter; false otherwise or if gouter is the same as this object.
     */
    public boolean nestedWithin( Graph gouter ) {
        if ( gouter == null ) {
            return false;
        }
              	// outermost graph never nested
        if ( this.getOwnerGraph() == null ) {
            return false;
        }
        if ( this.getOwnerGraph() == gouter ) {
            return true;
        }
        return this.getOwnerGraph().nestedWithin( gouter );
    }

    /**
     * Determines whether every node in the list is contained in the
     * graph. Uses shallow semantics (i.e., do not check if nested graphs
     * are entirely contained)
     *
     * @param	graphobjects	Collection of objects
     * @return	true if every element in target is contained in collection; else
     * false
     */
    public boolean containsGraphObjects( ArrayList graphobjects ) {
        Iterator iter = new ShallowIterator( this, GraphObject.Kind.GNODE );
        GraphObject go = null;
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            if ( !graphobjects.contains( go ) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Recursively descend in target graph, cutting/changing links as needed. If
     * two connected nodes are in different contexts, only allow it if: (a) the
     * connection is a coref or gen-spec link, or (b) the connection is to an
     * actor and we're allowed to link across contexts. If the connection is
     * allowed, make sure that the connection's "owner" (in the CharGer sense)
     * is in a dominant context for the two.
     *
     * @see Global#allowActorLinksAcrossContexts
     */
    public void handleContextLinks() {
        //Global.info( "handle context links for " + this.getTextLabel() );
        // handle all links in the target graph; shallow isn't complete, but deep is slow
        // call itself recursively
        Iterator graphs = new ShallowIterator( this, GraphObject.Kind.GRAPH );
        GraphObject go = null;
        while ( graphs.hasNext() ) {
            ( (Graph)graphs.next() ).handleContextLinks();
        }

        Iterator links = new ShallowIterator( this, GraphObject.Kind.GEDGE );
        GEdge ge = null;
        while ( links.hasNext() ) {
            ge = (GEdge)links.next();
            //Global.info( toString() );

            //Global.info( "begin handle context links; GEdge from " + ge.fromObj.getTextLabel() + " to " + ge.toObj.getTextLabel() );
            //if ( ge instanceof Coref || ge instanceof GenSpecLink ) continue;       // coref allowed
            // above line commented 10-07-2004 hsd because coref's may have to be moved to outer context (R0015)
            if ( ge.fromObj.getOwnerGraph() == ge.toObj.getOwnerGraph() ) {
                continue; 	// no context issue
            } else if ( Global.allowActorLinksAcrossContexts
                    && ( ( ge.fromObj instanceof Actor || ge.toObj instanceof Actor )
                    || ge instanceof Coref ) ) // added 10-07-2004 hsd R0015
            {
                Graph newOwner = null;
                // even if links are allowed, make sure the link is "safe" from
                // being processed or saved before either of its endpoints.
                // find a graph that dominates both of the ends' graphs
                ArrayList linkedObjs = new ArrayList();
                linkedObjs.add( ge.toObj.getOwnerGraph() );
                //Global.info( "adding graph " + ge.toObj.getOwnerGraph().objectID );
                linkedObjs.add( ge.fromObj.getOwnerGraph() );
                //Global.info( "adding graph " + ge.fromObj.getOwnerGraph().objectID );
                //Global.info( "trying to find dominanat context with ArrayList " + linkedObjs.toString() );
                try {
                             // TODO: Fix that it's failing to find dominant context of two graphs sideways nested
                             // newOwner comes out null!
                   newOwner = GraphObject.findDominantContext( linkedObjs );
//                            Global.info( "moving GEdge " + ge.objectID + " to graph " + newOwner.objectID );
                    ge.getOwnerGraph().removeFromGraph( ge );
                    newOwner.insertInCharGerGraph( ge );
                } catch ( CGContextException e ) {
                    Global.error( "context exception while moving GEdge: "
                            + e.getMessage() );
                }

                //Global.info( "allowing links across contexts" );
                // leave the link alone
            } else {	// remove the link across a context boundary
                //ge.getOwnerGraph().disconnectObject( ge );
                ge.getOwnerGraph().forgetObject( ge );
                //Global.info( "removing link" );
            }
            //Global.info( "handle context links: finish ge from " + ge.fromObj.getTextLabel() + " to " + ge.toObj.getTextLabel() );
        }
    }

    /**
     * Find an object by its ID at any level in the target graph. Also checks
     * the target graph itself.
     *
     * @param ID a graph ID
     * @see Global#applyForID
     * @return The object if it is found within the graph; null otherwise.
     */
    public GraphObject findByID( GraphObjectID ID ) {
        // if the graph itself has that id, return it
        if ( this.objectID.equals( ID ) ) {
            return this;
        }
        // if object is directly in this graph, return it
        GraphObject go = null;
                    // TODO: Find out why this fails even though a matching id is present...
        go = objectHashStore.get( ID.toString() );
        if ( go != null ) {
            return go;
        }
        // search through all contained objects...
        Iterator iter = new ShallowIterator( this, GraphObject.Kind.GRAPH );
        while ( iter.hasNext() ) {
            go = ( (Graph)iter.next() ).findByID( ID );
            if ( go != null ) {
                return go;
            }
        }
        return null;
    }

    /**
     * Find the CharGer object (if any) that corresponds to the Notio node
     * given. For a Notio concept that is a context, returns the graph that it
     * encloses.
     */
   /* REMOVE-NOTIO  public GraphObject getCharGerCounterpart( notio.Node node ) {
        GraphObject go = (GraphObject)NotioCharGerLookup.get( node );
        if ( go == null ) {
            Iterator nestedOnes = new ShallowIterator( this, GraphObject.GRAPH );
            while ( nestedOnes.hasNext() && ( go == null ) ) {
                Graph g = (Graph)nestedOnes.next();
                go = g.getCharGerCounterpart( node );
                //if ( go == null ) Global.info( " NULL " ); else Global.info( "found one" );
            }
        }
        return go;
    }
    * */

    /**
     * Stores a (notio.Node, GraphObject) pair for later retrieval.
     *
     * @param node Notio node to be stored (for a nested graph, store its
     * enclosing concept)
     * @param go charger object that corresponds -- for a nested graph, return
     * the graph
     */
    /* REMOVE-NOTIO public void putCharGerCounterpart( notio.Node node, GraphObject go ) {
        Object gg = NotioCharGerLookup.put( node, go );
    }*/

    /**
     * Gets a human-readable version of the counterpart table.
     *
     * @return a list of each charger graph object and the notio object that
     * corresponds to it.
     */
    /* REMOVE-NOTIO public String showCounterpartTable( int level ) {
        StringBuilder ls = new StringBuilder( " " + Hub.LineSeparator );
        for ( int k = 0; k < level; k++ ) {
            ls.append( "  " );
        }
        ls.append( level + ":" );
        StringBuilder s = new StringBuilder( "" );
        s.append( ls + "Graph " + getTextLabel() + "'s table has "
                + NotioCharGerLookup.size() + " counterpart(s)." );
        GraphObject t = null;
        Iterator iter = NotioCharGerLookup.values().iterator();
        while ( iter.hasNext() ) {
            t = (GraphObject)iter.next();
            s.append( ls + " next key is for charger node " + t.getTextLabel() );
            if ( t instanceof charger.obj.Graph ) {
                s.append( ( (Graph)t ).showCounterpartTable( level + 1 ) );
            }
        }
        return new String( s );

    }
    * */

    /**
     * Makes the object expendable from the CharGer graph. Fails when deleting a
     * context connected to actors
     */
    public void abandonObject() {
        Iterator iter = new DeepIterator( this );
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
            //this.eraseObject( go );
            //try {
            go.abandonObject();
            //}
        }
        super.abandonObject();
        //catch ( CGStorageError x ) {}
    }

    /**
     * @see Graph#forgetObject
     */
    protected void finalize() throws Throwable {
        try {
            //        Global.info( "graph finalizer" ); // graph's owner is " + (String)((ownerGraph == null)?"null":ownerGraph.objectID) );
            Iterator iter = new ShallowIterator( this );
            while ( iter.hasNext() ) {
                GraphObject go = (GraphObject)iter.next();
                go = null;      // should force garbage collection when needed 09-02-05
            }
            super.finalize();
        } catch ( Throwable t ) {
            throw t;
        } finally {
            super.finalize();
        }
    }


    /**
     * Make a duplicate of the graph, useful for making backup copies
     */
    /**
     * Create a short summary of the number of objects and their types.
     *
     * @return One-line string summary
     */
    public String getBriefSummary() {
        int concepts = 0;
        int relations = 0;
        int actors = 0;
        int contexts = 0;
        int types = 0;
        int reltypes = 0;
        int corefs = 0;
        
        Iterator iter;

        iter = new DeepIterator( this );
        while ( iter.hasNext() ) {
            Object n = iter.next();
            if ( n instanceof Concept ) {
                concepts++;
            }
            if ( n instanceof Relation ) {
                relations++;
            }
            if ( n instanceof Actor ) {
                actors++;
            }
            if ( n instanceof TypeLabel ) {
                types++;
            }
            if ( n instanceof RelationLabel ) {
                reltypes++;
            }
            if ( n instanceof Graph ) {
                contexts++;
            }
            if ( n instanceof Coref ) {
                corefs++;
            }

        }

        return "con: " + concepts + "  rel: " + relations + "  act: " + actors + "  nested: "
                + contexts + "  type: " + types + "  reltype: " + reltypes + "  corefs: " + corefs;
    }

    
    /**
     * Determines the region that a graph's contents require. Doesn't pay
     * attention to adornments such as shadows.
     * 
     *
     * @return the region needed to enclose all the graph's contents.
     * @see #getDisplayBounds
     */
    public Rectangle2D.Double getContentBounds() {
        Iterator iter = new ShallowIterator( this );
        // Iterator iter = new ShallowIterator( this );
        GraphObject go = null;
        Rectangle2D.Double biggest = null; 
        
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            
            if ( go.myKind != GraphObject.Kind.GEDGE ) {
                Rectangle2D.Double rect = ( (GraphObject)go ).getDisplayRect();
//                Global.info( "adding bounds for object " + go.getTextLabel() + " rect is " + go.getDisplayRect() );
                if ( biggest == null ) {
                    biggest = Util.make2DDouble( go.getDisplayRect() );
                } else {
                    biggest.add( go.getDisplayRect() );
                }
//                Global.info( "biggest rect is now " + biggest );
            }
        }

        // Take into account the graph's label which may need more width
        Dimension textsize = getTextLabelSize();

        double textWidthNeeded = textsize.width;
        double width = displayRect.width - 2 * contextBorderWidth - 2 * contextInnerPadding;
        if ( textWidthNeeded < width ) {
            width = textWidthNeeded - 2 * contextBorderWidth - 2 * contextInnerPadding;
        }


        if ( biggest == null ) {
            biggest = new Rectangle2D.Double( getDisplayRect().x + contextBorderWidth + contextInnerPadding,
                    getDisplayRect().y + contextBorderWidth + contextInnerPadding,
                    width, textsize.height );
            return biggest;
        } else {
            if ( biggest.width < width ) {
                biggest.width = width;
            }
            return biggest;
        }
    }

    /**
     * Determines how much space it takes to render the graph on a canvas. Takes
     * into account shadows, border widths and other adornments. Question for
     * Harry: How is this different from getDisplayRect? Answer: it is generally
     * used for printing where every pixel counts.
     *
     * @return the region needed to render the graph completely (plus one pixel
     * for margin)
     * @see #getContentBounds
     */
    public Rectangle2D.Double getDisplayBounds() {

        Rectangle2D.Double r = getContentBounds();
        Dimension textsize = getTextLabelSize();

        double textWidthNeeded = textsize.width;
        double width = displayRect.width - 2 * contextBorderWidth - 2 * contextInnerPadding;
        if ( textWidthNeeded < width ) {
            width = textWidthNeeded - 2 * contextBorderWidth - 2 * contextInnerPadding;
        }

        CGUtil.grow( r, 1, 1 );		// add 1 pixel for margin
        int xIncrement = 0;
        int yIncrement = 0;
        if ( Global.showShadows ) {
            xIncrement += Global.shadowOffset.x;
            yIncrement += Global.shadowOffset.y;
        }
        r.setFrame( r.x - 1, r.y - 1, r.width + xIncrement + 2, r.height + yIncrement + 2 );
        return r;
    }
    
    /**
     * Adjust all objects to be offset by the translation vector.
     * Needs to allow for nested contexts and needs to make sure all enclosing
     * contexts are moved (not re-sized) before redrawing.
     * @param translation 
   
     */
    public EditingChangeState moveGraph( Point2D.Double translation ) {

        ArrayList<GraphObject> currentOnes = this.getGraphObjects();
        // Save dimension because moving the objects wants to change the bounds of the graph
        Dimension oldDim = new Dimension( (int)this.getDisplayRect().width, (int)this.getDisplayRect().height );
        Point2D.Double oldctr = this.getCenter();

        EditingChangeState changed = moveGraphObjects( currentOnes, translation );
//        for ( GraphObject go : currentOnes  ) {
//            if ( go instanceof GNode ) {
//                GraphObject thisobj = go;
//                Point2D.Double oldctr = go.getCenter();
//                Point2D.Double newctr = new Point2D.Double( oldctr.x + translation.x, oldctr.y + translation.y);
//                go.setCenterOnly( newctr );
//            }
//        }
        this.setCenterOnly( new Point2D.Double( oldctr.x + translation.x, oldctr.y + translation.y ) );
        this.setDim( oldDim );

        // Commented out 9-17-2014 hsd
//        this.adjustDisplayRect( null );
                    // Seems to be working, but definitely needs to make sure edges are taken care of
        adjustEdges();
        Global.info( "Moved graph " + this.getTextLabel() + " by x = " + translation.x + "; y = " + translation.y
                + ".  Bounds: " + this.getContentBounds() );
        return changed;
    }

    /**
     * Determines whether there are any objects in this graph.
     *
     * @return <code>true</code> if there's at least one object in the
     * graph; <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        if ( objectHashStore == null ) {
            return true;
        }
        if ( objectHashStore.size() == 0 ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * String-ifies graph with objects in an order that has no internal forward
     * references, using version 3's format.
     *
     * @return string representing the graph in a safe order; i.e., every object
     * occurs before it is referenced (e.g., by a link)
     */
    public String safeString() {
        ArrayList deferredNodes = new ArrayList();		// if any 
        String returnString = toStringDeep( false );

        GraphObject go = null;
        Graph g = null;

        Iterator iter = new DeepIterator( this, GraphObject.Kind.GRAPH );
        while ( iter.hasNext() ) {
            g = (Graph)iter.next();
            returnString = returnString + Global.LineSeparator + g.toStringDeep( false );
        }

        iter = new DeepIterator( this, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            go = (GNode)iter.next();
            returnString = returnString + Global.LineSeparator + go.toString();
        }

        iter = new DeepIterator( this, GraphObject.Kind.GEDGE );
        while ( iter.hasNext() ) {
            go = (GEdge)iter.next();
            returnString = returnString + Global.LineSeparator + go.toString();
        }
        return returnString;
    }

    /**
     * String-ifies graph with objects in an order that has no internal forward
     * references.
     *
     * @return string representing the graph in a safe order; i.e., every object
     * occurs before it is referenced (e.g., by a link)
     */
    public String safeString2() {

        String returnString = toStringDeep( false );

        GraphObject go = null;
        Graph g = null;

        Iterator iter = new DeepIterator( this, GraphObject.Kind.GRAPH );
        while ( iter.hasNext() ) {
            g = (Graph)iter.next();
            returnString = returnString + Global.LineSeparator + g.toStringDeep( false );
        }

        iter = new DeepIterator( this, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            go = (GNode)iter.next();
            returnString = returnString + Global.LineSeparator + go.toString();
        }

        iter = new DeepIterator( this, GraphObject.Kind.GEDGE );
        while ( iter.hasNext() ) {
            go = (GEdge)iter.next();
            returnString = returnString + Global.LineSeparator + go.toString();
        }
        return returnString;
    }

    /**
     * Gives a CharGer-formatted version of the target graph, with objects in
     * arbitrary order.
     *
     * @return A CharGer-format string
     */
    public String toString() {
        return toStringDeep( true );
    }

    /**
     * Returns graph objects in a CharGer-formatted string
     *
     * @param deep Whether to recursively fetch graph objects or not.
     * @see GNode#toString
     * @see Graph#safeString
     * @see Graph#toStringDeep
     */
    public String toStringDeep( boolean deep ) { //, ArrayList deferredNodes ) {
        // 		@param deferredNodes if a node needs to be deferred (e.g., crosses context boundaries) add it to this arraylist
        Iterator iter = new ShallowIterator( this );
        String returnString = super.toString();

        if ( deep ) {
            Object dummy;
            Stack inOrder = new Stack();		// because enumerate returns items in reverse order...
            while ( iter.hasNext() ) {
                GraphObject go = (GraphObject)iter.next();
                dummy = inOrder.push( go );
            }
            while ( !inOrder.empty() ) {
                returnString = returnString + Global.LineSeparator + inOrder.pop().toString();
            }
        }
        return returnString; // + Hub.LineSeparator;
    }

    /**
     * Get an iterator for all the given graph's objects (including edges). A
     * convenience method to access the objectHashStore for this graph. Does not
     * use ObjectIterator or its subclasses.
     *
     * @return the immediately-contained graph objects for this graph (including
     * edges). A nested context is considered as a single graph object.
     */
    public Iterator graphObjects() {
        return objectHashStore.values().iterator();
    }

    /**
     * Return the number of objects (including edges) in this graph
     *
     * @return number of objects (including edges) in this graph. A nested graph
     * is considered a single graph object.
     */
    public int getGraphObjectCount() {
        return objectHashStore.size();
    }

    /**
     * Allocate and initialize a notio node that is consistent with the CharGer
     * node. Assumes that the CharGer node is correct. create both its outer
     * context notio.Concept and designator notio.Graph
     */
   /* public void updateForNotio( notio.KnowledgeBase kb ) {
        NotioCharGerLookup.clear();

        super.updateForNotio( kb );

        if ( ownerGraph != null ) {
            ownerGraph.putCharGerCounterpart( nxConcept, this ); // override concept's previous put
        }
        nxGraph = new notio.Graph();

        nxGraph.addComment( makeCGIFcomment() );

        nxConcept.getReferent().setDescriptor( nxGraph );

    }
    * */

    /**
     * Invokes forgetObject on all the objects (including nested ones) in this
     * graph.
     *
     * @see #forgetObject
     */
    public void dispose() {
        Iterator iter = new DeepIterator( this );
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
            forgetObject( go );
        }
    }

    public ArrayList getGraphObjects() {
        ArrayList v = new ArrayList( 10 );
        Iterator iter = this.graphObjects();
        while ( iter.hasNext() ) {
            v.add( iter.next() );
        }
        return v;
    }

    public void setSize( Dimension size ) {
        super.setDim( size );
    }
    
    /**
     * Add the history event to all objects in the graph
     * @param he 
     */
    public void addHistory( ObjectHistoryEvent he ) {
        DeepIterator iter = new DeepIterator( this );
        while ( iter.hasNext() ) {
            ((GraphObject)iter.next()).addHistoryEvent( he );
        }
    }
    

    /**
     * Look for descriptors for the given term in the knowledge source, however
     * it may be remembered. Calls to findTypeDescriptors may return more than
     * one descriptor, since some compound terms may require two or more
     * descriptors.
     *
     * @param term a phrase that may have type descriptors.
     * @return zero or more type descriptors, but never null.
     */
    public AbstractTypeDescriptor[] findTypeDescriptors( String term ) {
        //Global.info( "findTypeDescriptors: looking for term " + term );
        ArrayList holder = new ArrayList();
        Iterator iter = new DeepIterator( this, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            GNode gn = (GNode)iter.next();
            AbstractTypeDescriptor[] ds = gn.getTypeDescriptors();
            //Global.info( "findTypeDescriptors: gnode " + gn.getTextLabel() + " has " + ds.length + " descriptors." );
            if ( ds.length == 0 ) {
                continue;
            }

            for ( int k = 0; k < ds.length; k++ ) {
                charger.Global.info( "found descriptors! checking term " + ds[ k].getLabel() + " and " + term );
                if ( ds[ k].getLabel().equalsIgnoreCase( term ) ) {
                    holder.add( ds[ k] );
                }
            }
        }
        return (AbstractTypeDescriptor[])( holder.toArray( new AbstractTypeDescriptor[ 0 ] ) );
    }

    /**
     * Get all the type descriptors known to this knowledge source.
     *
     * @return a possibly empty list, but never null;
     */
    public AbstractTypeDescriptor[] getAllTypeDescriptors() {
        ArrayList holder = new ArrayList();
        Iterator iter = new DeepIterator( this, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            GNode gn = (GNode)iter.next();
            AbstractTypeDescriptor[] ds = gn.getTypeDescriptors();
            holder.addAll( Arrays.asList( ds ) );
        }
        return (AbstractTypeDescriptor[])( holder.toArray( new AbstractTypeDescriptor[ 0 ] ) );
    }

    /**
     * Get all the graphs known to this knowledge source.
     *
     * @return a one-element array consisting of this graph and no others;
     */
    public Graph[] getAllGraphs() {
        Graph[] g = { this };
        return g;
    }
    
     /**
     * Perform whatever activities are required for this concept to be committed to a knowledge base.
     * Adds the type label to the type hierarchy.
     * @param kb 
     */
    public boolean commitToKnowledgeBase( kb.KnowledgeBase kb ) {
        return true;
        //boolean b = kb.getConceptTypeHierarchy().addLabel( this.getTypeLabel() );
        //    Hub.consoleMsg( "Concept \"" + this.getTypeLabel() + "\" added = " + b + " " + kb.showConceptTypeHierarchy() );
    }

         /**
     * Perform whatever activities are required for this concept to be deleted from a knowledge base.
     * Removes the type label to the type hierarchy.
     * @param kb 
     */
    public boolean unCommitFromKnowledgeBase( kb.KnowledgeBase kb ) {
        return true;
        //boolean b = kb.getConceptTypeHierarchy().addLabel( this.getTypeLabel() );
        //    Hub.consoleMsg( "Concept \"" + this.getTypeLabel() + "\" added = " + b + " " + kb.showConceptTypeHierarchy() );
    }


}
