package charger.obj;

import charger.*;
import charger.exception.*;
import charger.util.*;
import kb.KnowledgeBase;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import kb.ObjectHistory;
import kb.ObjectHistoryEvent;

/* 
 $Header$ 
 */
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
 * GraphObjects support common characteristics for any component of a graph.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
abstract public class GraphObject {

    /**
     * global and unique number.
     *
     * @see Global#applyForID
     */
    public GraphObjectID objectID = Global.applyForID();
    /**
     * The innermost enclosing graph in which this object logically lies; null
     * if in the outermost graph
     */
    public Graph ownerGraph = null;

    /**
     * One of GraphObject.GNODE or GEDGE or GRAPH
     */
    public enum Kind {

        ALL, GNODE, GEDGE, GRAPH, CONCEPT_OR_GRAPH
    };

    public Kind myKind = Kind.ALL;
    // these are used in the myKind variable
    /**
     * The color used for a thick rectangle around each selected node
     */
//    public final static Color defaultSelectColor = Color.black;
    public final static Color defaultSelectColor = Color.magenta;

    /**
     * One of: a referent, a link name, an actor name, a context name
     */
    public String textLabel;
    // these are the default size of the GNodes
    public static Dimension defaultDim = new Dimension(
            (int)Float.parseFloat( Global.Prefs.getProperty( "defaultGraphObjectWidth", "40" ) ),
            (int)Float.parseFloat( Global.Prefs.getProperty( "defaultGraphObjectHeight", "30" ) ) );
    public static float defaultWidth = defaultDim.width;
    public static float defaultHeight = defaultDim.height;
    // The "3-mile limit" for GNodes, inside of which no other GNode can be created
    // but one can be moved.
    public static int TerritorialLimit =
            Integer.parseInt( Global.Prefs.getProperty( "TerritorialLimit", "5" ) );
    
    public static boolean defaultWrapLabels = 
                    defaultWrapLabels = Global.Prefs.getProperty("defaultWrapLabels", "false").equals("true");

    public static int defaultWrapColumns = Integer.parseInt( Global.Prefs.getProperty( "defaultWrapColumns", "30" ) );
    
    /** The amount of white space to have around the text label */
    public static int objectMargin = 4;
    
        /**
     * The lower left corner of where the text label should be drawn. Set for
     * each object in advance to speed up drawing.
     */
    public Point2D.Double textLabelLowerLeftPt;


    /**
     * The boundary of what will be shown on the canvas.
     */
    public Rectangle2D.Double displayRect = new Rectangle2D.Double( 0, 0, 0, 0 );		// observable form
    
    /**
     * True if this object was selected on the canvas, otherwise false
     */
    public boolean isSelected = false;
    /**
     * Foreground or text color of the object
     */
    public Color foreColor = Color.black;
    /**
     * Background or fill color of the object
     */
    public Color backColor = Color.white;
    /**
     * whether this object is negated; for right now, only useful with graphs
     */
    private boolean isNegated = false;
    /**
     * whether I'm highlighted or not
     */
    //public boolean highlighted = false;
    /**
     * highlight color
     */
    public Color highlightColor = Color.red;
    
    public Font labelFont = new Font( Global.defaultFont.getName(), Global.defaultFont.getStyle(), Global.defaultFont.getSize());
    
    public ObjectHistory history = new ObjectHistory();

    /**
     * Obtain the Shape object that represents the selectable region of this
     * object.
     *
     * @return the Shape for this object, with appropriate dimensions, etc.
     */
    abstract public Shape getShape();
    
        /**
     * Finds the center line of the shape, so that drawing the border will 
     * take place entirely within the desired shape.
     * Note this is the same as getShape unless it's a context (graph) 
     * 
     * @return same as getShape, unless it's overridden in a subclass.
     * */
    public Shape getShapeForDrawing() {
        return getShape();
    }

    /**
     * Figure out whether to wrap labels. Usually gotten from the object's owner frame, but if there isn't one,
     * return the default static value.
     * @return true if labels are to be wrapped; false otherwise
     */
    public boolean getWrapLabels() {
        if ( getOutermostGraph() == null ) 
            return defaultWrapLabels;
        else
            return getOutermostGraph().wrapLabels;
    }
    
    /**
     * The number of columns to wrap a label, if wrapping is enabled
     * @return The number of columns to wrap a label, if wrapping is enabled
     */
    public int getWrapColumns() {
        if ( getOutermostGraph() == null ) 
            return defaultWrapColumns;
        else
            return getOutermostGraph().wrapColumns;
    }
    
    
    /**
     * Get the colors for this object.
     *
     * @param foreback one of <code>"fill"</code> or <code>"text"</code>
     * indicating which color to get.
     * @return requested color
     */
    public Color getColor( String foreback ) {
        if ( foreback.equals( "text" ) ) {
            return foreColor;
        } else {
            return backColor;
        }
    }

    /**
     * Sets the colors of this concept from the current defaults, whatever they
     * are.
     */
    public void setColor() {
        if ( Global.userForeground == null || Global.userBackground == null ) {
            return; // other static variables might call this method early.
        }				//Global.info( "looking up color for " + CGUtil.shortClassName( this ) );
        foreColor = (Color)( Global.userForeground.get( CGUtil.shortClassName( this ) ) );
        backColor = (Color)( Global.userBackground.get( CGUtil.shortClassName( this ) ) );
        //Global.info( "forecolor is " + foreColor + "; backcolor is " + backColor );
    }

    /**
     * Sets the colors of this object from the given color. 
     *
     * @param foreback one of <code>"text"</code> or <code>"fill"</code>
     * @param c the color to set for that property
     */
    public void setColor( String foreback, Color c ) {
        if ( foreback.equals( "text" ) ) {
            Color fg = (Color)( Global.userForeground.get( CGUtil.shortClassName( this ) ) );
            if ( c == null && fg != null ) {
                setColor( "text", fg );
            } else {
                foreColor = c;
            }
        } else {
                Color bg = (Color)( Global.userBackground.get( CGUtil.shortClassName( this ) ) );
            if ( c == null && bg != null ) {
                setColor( "fill", bg );
            } else {
                backColor = c;
            }
        }
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void setNegated( boolean isNegated ) {
        this.isNegated = isNegated;
    }
    
    /**
     * Convenience method for telling the object to tell its graph to forget it.
     */
    public void forgetObject() {
        Graph g = this.getOwnerGraph();
        g.forgetObject( this );
    }
    
    
    /**
     * Forces an un-verified move of the object.
     * @param delta
     * @return whether the new position in different from the old position
     */
    public boolean forceMove( Point2D.Double delta ) {
//            Global.info( "graph object force move of " + getTextLabel());
        double oldx = displayRect.x;
        double oldy = displayRect.y;
        
        displayRect.x += delta.x;
        displayRect.y += delta.y;
                
        return ( (oldx != displayRect.x) || (oldy != displayRect.y));
    }
    
    

    /**
     * Set the center point of the current object.
     * Adjust the display rectangle of its enclosing graph,
     * in case the new center is outside the graph's previous bounds.
     *
     * @param p new CENTER point
     */
    public void setCenter( Point2D.Double p )// , Rectangle2D.Double rect ) {
    {
        setCenterOnly( p );
        // if not done here, needs to be done somewhere...
        // warning; subclass information here
        if ( !( this instanceof GEdge ) ) {
            if ( ownerGraph != null ) {
                ownerGraph.resizeForContents( null );
            }
        }
    }
    


    /**
     * Sets the object's center point on the canvas, moving the object if
     * necessary. Moves the textlabel to be centered in this object.
     *
     * @param p The new center point
     *
     */
    public void setCenterOnly( Point2D.Double p ) {
        if ( p != null ) {
            displayRect.x = p.x - displayRect.width / 2;
            displayRect.y = p.y - displayRect.height / 2;

            //displayRect = new Rectangle2D.Double( Pos.x, Pos.y, Dim.width, Dim.height );

            EditFrame ef = getOwnerFrame();
            FontMetrics fm = null;
            if ( ef != null ) {
                fm = ef.currentFontMetrics;
            } else {
                fm = Global.defaultFontMetrics;
            }

            textLabelLowerLeftPt = CGUtil.getStringLowerLeftFromCenter( fm, textLabel, p );
        }
    }

    /**
     * Sets the upper left corner of the object. Makes no consistency or other
     * adjustments and does not change the size of the object.
     *
     * @param upperleft The point at the upper left of the graph object.
     */
    public void setUpperLeft( Point2D.Double upperleft ) {
        displayRect.x = upperleft.x;
        displayRect.y = upperleft.y;
    }

    /**
     * Sets the upper left corner of the object. Makes no consistency or other
     * adjustments, nor does it change the size of the object.
     *
     * @param upperleftx x-value of the point at the upper left of the graph
     * object.
     * @param upperlefty y-value of the point at the upper left of the graph
     * object.
     */
    public void setUpperLeft( double upperleftx, double upperlefty ) {
        setUpperLeft( new Point2D.Double( upperleftx, upperlefty ) );
    }
    

    /**
     * Find the upper left point that lies on the extreme edge of 
     * the display rectangle.
     * @return upper left corner of the displayable area of this object.
     */
    public Point2D.Double getUpperLeft() {
        return new Point2D.Double( displayRect.x, displayRect.y );
    }

    /**
     * @return center point of the object
     * @see GraphObject#getUpperLeft
     */
    public Point2D.Double getCenter() {
        return new Point2D.Double( getUpperLeft().x + displayRect.width / 2.0f, getUpperLeft().y + displayRect.height / 2.0f );
    }

    /**
     * Get the dimension of this object.
     *
     * @return its current dimension.
     */
    public Dimension getDim() {
        return new Dimension( (int)displayRect.width, (int)displayRect.height );
    }

    /**
     * Sets the dimensions of the object. Does not change the location of the
     * upper left point.
     *
     * @param d new dimensions for the object.
     */
    public void setDim( Dimension d ) {
        displayRect.setFrame( displayRect.x, displayRect.y, (float)d.width, (float)d.height );
    }

    /**
     * Sets the width and height of the object. Does not change the location of
     * the upper left point.
     *
     * @param wid new width for the object.
     * @param ht new height for the object.
     */
    public void setDim( float wid, float ht ) {
        displayRect.setFrame( displayRect.x, displayRect.y, wid, ht );
    }

    /**
     * Gets the display rectangle of this object on the canvas, without any scaling
     * information. The display rectangle includes the border but does not currently
     * include the shadow (if present). Should it?
     */
    public Rectangle2D.Double getDisplayRect() {
        return displayRect;
    }

    /**
     * Sets the size and location of the object on the unscaled canvas.
     *
     * @param rect the rectangle of the object.
     */
    public void setDisplayRect( Rectangle2D.Double rect ) {
        displayRect =  Util.make2DDouble( rect );
//        setCenterOnly( getCenter() );
    }

    /**
     * Performs whatever custom adjustments that any sub-classes want. This
     * method is called explicitly in some cases by Charger, but it does nothing
     * in the GraphObject class itself. Sub-classes can override this in order
     * to ensure their own peculiar display rect constraints (e.g., actors).
     */
    public void adjustCustomDisplayRect() {
    }

    public void setForeground( Color c ) {
        foreColor = c;
    }

    public void setBackground( Color c ) {
        backColor = c;
    }

    /**
     * @return the text label of the graph object
     */
    public String getTextLabel() {
        return textLabel;
    }

    /**
     * Sets the text label of the graph object. 
     *
     * @param newTextLabel new text label (erases the old text label)
     */
    public void setTextLabel( String newTextLabel ) {
        if ( newTextLabel == null ) return;
        setTextLabel( newTextLabel, null, null );
    }

    /**
     * Sets the text label of the graph object. Creates a dummy graphics context
     * to determine a font size, etc. May re-size the node if the label is too
     * long
     *
     * @param newTextLabel new text label (erases the old text label)
     * @param fm the font metrics that govern re-sizing of the text or object if
     * necessary
     */
    public void setTextLabel( String newTextLabel, FontMetrics fm ) {
        setTextLabel( newTextLabel, fm, null );	// force around center
    }

    /**
     * Sets the text label of the graph object. May re-size the node if the
     * label is too long.
     *
     * @param newTextLabel new text label (erases the old text label)
     * @param fmetrics the font metrics that govern re-sizing of the text or
     * object if necessary
     * @param p the point around which the label is centered
     */
    public void setTextLabel( String newTextLabel, FontMetrics fmetrics, Point2D.Double p ) {
        //Global.info( "set text label " + s + " display rect is " + getDisplayRect() );
        textLabel = newTextLabel;
        if ( p == null ) {
            p = new Point2D.Double( getCenter().x, getCenter().y );
        }

        if ( this instanceof Graph ) // warning: sub-class knowledge here!!
        {
            ((Graph)this).resizeForContents( this.getCenter() );
            //((Graph)this).adjustDisplayRect( p );		
            // take care of the case where the new label is too big to fit
        } else {
            resizeIfNecessary( fmetrics, p );
        }
    }

    public Dimension getTextLabelSize() {
//               Dimension stringdim = CGUtil.stringDimensions( getTextLabel(), fm );
        // added by hsd 7-31-14 to account for wrapping
             WrappedText text = new WrappedText( getTextLabel(), GraphObject.defaultWrapColumns );
             text.setEnableWrapping( getWrapLabels() );
//                 Global.info( "before wrapping " + getTextLabel() + " with font  " + FontChooser.getFontString( fm.getFont()));

            Dimension stringdim = text.getSize( getBestFontMetrics() );
//                Global.info( "after wrapping " + getTextLabel() + " with font  " + FontChooser.getFontString( fm.getFont()));
//            stringdim.width = d.width;
//            stringdim.height = d.height;
            return stringdim;
    }

    /**
     * Use the object's current text label and center to determine whether it
     * needs re-sizing to accommodate the (possibly new) label. If resizing is
     * necessary, then do it.
     */
    public void resizeIfNecessary() {
//        String s = getTextLabel();
        Point2D.Double center = getCenter();
        resizeIfNecessary( this.getBestFontMetrics(), center );
    }

    /**
     * Using the given string and center point, determine whether the object
     * needs resizing to accommodate the (possibly new) string label. If
     * resizing is necessary then do it, Previously resizing meant making the object
     * larger if necessary, but never smaller (at least not here). Now
     * resizing may also shrink the node if the new label is significantly
     * smaller than the box.
     */
    public void resizeIfNecessary( FontMetrics fmetrics, Point2D.Double p ) {
//        if ( this instanceof Graph ) return;
                // commented 09-21-14 because getting text size finds font metrics on its own
//        FontMetrics fm = fmetrics;
//        if ( fm == null ) {
//            fm = getBestFontMetrics();
//        }
//
//        if ( fm == null ) {
//            return;       // give up!
//        }

        if ( myKind == Kind.GRAPH ) {
            Graph g = ( (Graph)this );
            g.resizeForContents( p );
            g.adjustEdges();

        } else if ( myKind == Kind.GNODE ) // warning: uses sub-class knowledge here!!!
        {
            Dimension contentSize = getMinimumContentSize();
            double width = displayRect.width;
            double height = displayRect.height;
            boolean rectChanged = false;

            if ( displayRect.getWidth() < contentSize.getWidth() ) {
                rectChanged = true;
                width = contentSize.width;
            }

            if ( displayRect.getHeight() < contentSize.getHeight() ) {
                rectChanged = true;
                height = contentSize.height;
            }

            if ( rectChanged ) {
                displayRect.width = width;
                displayRect.height = height;
                if ( p != null ) {
                    setCenterOnly( p );
                }
            }

        }
    }
   
    /**
     * Using the size of the textlabel and including reasonable padding, etc. 
     * how much space is required
     * @return the smallest rectangle that will hold the text label including the margins.
     */
   public Dimension getMinimumContentSize() {
        Dimension textSize = getTextLabelSize();
        textSize.width += 2 * objectMargin;
        textSize.height += 2 * objectMargin;
        return textSize;
    }

    /**
     * Find the most appropriate font metrics for this object. Use the object's
     * label font. If there is no graphics context, then revert to the default
     * metrics for the session.
     *
     * @return the most applicable FontMetrics object that can be used when needed
     */
    public FontMetrics getBestFontMetrics() {
        FontMetrics metrics = null; //Global.defaultFontMetrics;
        // First, is there a graphics context nearby?
        if ( getOwnerFrame() != null ) {
            metrics = getOwnerFrame().cp.getGraphics().getFontMetrics( labelFont );
        } else {
            Canvas c = new Canvas();
            metrics = c.getFontMetrics( labelFont );

            // Need to be able to use the labelfont even if there's no frame. How do we do this?
        }
        return metrics;
    }

    /**
     * Constructs a constrained text version of the object, suitable for writing
     * to a file and then reading back into the system.
     *
     * @see IOManager
     */
    public String toString() {
        GraphObjectID ownerID;
        if ( ownerGraph == null ) {
            ownerID = GraphObjectID.zero;
        } else {
            ownerID = ownerGraph.objectID;
        }
        return CGUtil.shortClassName( this ) + "|" + objectID + "," + ownerID
                + "|" + textLabel + "|" + displayRect.x + "," + displayRect.y
                + "," + displayRect.width + "," + displayRect.height
                + "|" + foreColor.getRed() + "," + foreColor.getGreen() + "," + foreColor.getBlue()
                + "|" + backColor.getRed() + "," + backColor.getGreen() + "," + backColor.getBlue();
    }


    /**
     * Converts a list of graph objects into their i/o string form, including
     * line separators.
     *
     * @param list ArrayList of graph objects
     * @see charger.xml.CGXParser#parseCGXMLString
     * @return A string (including line separators) of the objects
     */
    public static String listToString( ArrayList list ) {
        StringBuilder s = new StringBuilder( "" );
        Iterator iter = list.iterator();
        while ( iter.hasNext() ) {
            // BUG: HERE is a problem!
            // using toString will end up Copying nested contents
            // then if contents are explicitly selected, they're repeated
            //Global.info( "vector to string next element" );
            GraphObject go = (GraphObject)iter.next();
            if ( go instanceof Graph ) {
                s.append( ( (Graph)go ).toStringDeep( false ) + Global.LineSeparator );
            } else {
                s.append( go.toString() + Global.LineSeparator );
            }
        }
        return s.toString();
    }

    /**
     * Converts an arraylist of graph objects into their XML string form,
     * including line separators.
     *
     * @param list ArrayList of graph objects
     * @return A string (including line separators, and overall
     * %&lt;conceptualgraph%gt; tags) of the objects
     * @see charger.EditManager
     */
    public static String listToStringXML( ArrayList list ) {
        StringBuilder s = new StringBuilder( "<conceptualgraph>" );
                // Here is where an ontology would be inserted
        Iterator iter = list.iterator();
        while ( iter.hasNext() ) {
            // HERE is a problem!
            // using toString will end up Copying nested contents
            // then if contents are explicitly selected, they're repeated
            //Global.info( "vector to string next element" );
            GraphObject go = (GraphObject)iter.next();
            if ( go instanceof Graph ) {
                s.append( charger.xml.CGXGenerator.GraphObjectXML( (Graph)go, "" ) ); //+ Hub.LineSeparator );
            } else {
                s.append( charger.xml.CGXGenerator.GraphObjectXML( go, "" ) ); //+ Hub.LineSeparator );
            }
        }
        return s.toString() + "</conceptualgraph>";
    }

    /**
     * Abstract method that disconnects object from any other objects. Doesn't
     * delete from its owning graph, from Notio, or destroy itself.
     */
    abstract void abandonObject();	// class-specific cleanup

    /**
     * Subclasses will implement this method in order to perform whatever
     * object-specific actions are appropriate for adding to the knowledge base.
     */
    public boolean commitToKnowledgeBase( KnowledgeBase kb ) {
        Global.warning( "commitToKnowledgeBase" + " not implemented for " + this.getTextLabel() );
        return true;
    }

    /**
     * Subclasses will implement this method in order to perform whatever
     * object-specific actions are appropriate for removing this object from the
     * knowledge base.
     */
    public boolean unCommitFromKnowledgeBase( KnowledgeBase kb ) {
           Global.warning( "unCommitFromKnowledgeBase" + " not implemented for " + this.getTextLabel() );
         return true;
    }

    /**
     * Any CharGer-specific cleanup that must be done prior to being
     * disconnect/ignored. Distinct from finalize because it can deal with
     * logical disposal rather than memory management. Does nothing at this
     * level; usually overwritten in a subclass.
     */
    public void selfCleanup() {
    }

    /**
     * Render the object with adjustments as necessary
     *
     * @param g Graphics context on which the object is to be rendered
     * @param printing Whether to pretty-print the object
     */
    abstract public void draw( Graphics2D g, boolean printing );

    /**
     * Highlights the border of the object
     *
     * @param g Graphics context on which the object is to be rendered
     * @param borderColor the color to draw
     */
    abstract void drawBorder( Graphics2D g, Color borderColor );

    /**
     * Find the dominant context for a given collection of objects, defined as
     * the one partially-included context. If more than one is found, it throws
     * an exception.
     *
     * @param nodeList list containing the objects
     * @return dominant context if a unique one is found; else null
     */
    public static Graph findDominantContext( ArrayList nodeList ) throws CGContextException {

        Graph dominantContext = null;		// start out with none

        Iterator nodes = nodeList.iterator();
        // First create a list of all the enclosing graphs for each node, with no duplicates
        // String is the GOID's string representation.
        HashMap<String, Graph> enclosingGraphs = new HashMap<String, Graph>();

        while ( nodes.hasNext() ) {
            GraphObject go = (GraphObject)nodes.next();
            if ( go.getOwnerGraph() == null ) {
//                return go.getOwnerGraph();
                if ( go instanceof Graph ) 
                    return (Graph)go;
                else
                    return null;
            }
            enclosingGraphs.put( go.getOwnerGraph().objectID.toString(), go.getOwnerGraph() );
        }
        // If only one ownergraph was found, that must be it!
        if ( enclosingGraphs.values().size() == 1 ) {
            return (Graph)enclosingGraphs.values().toArray()[0];
        }

        Iterator graphs = enclosingGraphs.values().iterator();
        while ( graphs.hasNext() ) {
            Graph g = (Graph)graphs.next();
            if ( g.getOwnerGraph() == null ) {
                return g;      // if the outermost graph is here, that must be it.
            }
            if ( dominantContext == null ) // have to start somewhere
            {
                dominantContext = g;
            } else {
                while ( !g.nestedWithin( dominantContext ) ) {
                    dominantContext = dominantContext.getOwnerGraph();
                }
            }
        }
        return dominantContext;
    }
    
        /**
     * Puts an object in its correct logical context, constrained to be nested
     * in graph g. Makes no attempt to handle links.
     *
     * @param g outermost enclosing graph out of which <b>go</b> cannot be
     * placed
     * @param go new (or repositioned) object that is to be placed
     * @param newPos the new position, usually considered to be the center point
     * for the object
     * @return true the context was changed; false otherwise Assumes that object
     * <b>go</b> is already positioned where it is to go.
     * @see Graph#handleContextLinks
     */
    public static boolean putInCorrectContext( Graph g, GraphObject go, Point2D.Double newPos ) {
        // not working when go is a graph??
        Graph newOwner = EditManager.innermostContext( g, newPos );
        //Global.info( "innermost context is " + newOwner.getTextLabel() );
        Graph oldOwner = go.getOwnerGraph();
        //Global.info( "old owner context is " + oldOwner.getTextLabel() );
        if ( newOwner != oldOwner ) {	// if the apparent owning context did change
            if ( newOwner != go
                    && ( !( go instanceof Graph ) || ( !newOwner.nestedWithin( (Graph)go ) ) ) ) {
                //Global.info( "put in correct context " );
                // move from graph ownerGraph to graph newOwner
//                displayOneLiner( "Moved " + CGUtil.shortClassName( go ) + " \"" + go.getTextLabel()
//                        + "\" from context \"" + go.getOwnerGraph().getTextLabel()
//                        + "\" to context \"" + newOwner.getTextLabel() + "\"." );
                //Global.info( "before disconnect object" );
                //Global.info( "after disconnect object; before remove from chargergraph" );
                oldOwner.removeFromGraph( go );      // NOte we don't use forgetObject because we're just moving it
                if ( newOwner != null ) {
                    newOwner.insertObject( go );
                }
            } else {
                return false;
            }
            //newOwner.adjustDisplayRect( null ); // NEED to check this; may be needed in some cases....
            //Global.info( "after connect object; before end of put in correct context" );
            return true;
        }
        return false;
    }

    public boolean putInCorrectContext( Graph g, GraphObject go ) {
        return putInCorrectContext( g, go, go.getCenter() );
    }


//    public static Graph findDominantContext1( ArrayList vnodes ) throws CGContextException {
//        // determine the dominant context, defined as the one partially-included context
//        //  where all other included objects are in contexts that are completely included.
//        //		if more than one partially-included context, then fails.
//        // algorithm:
//        // if something is in the collection then
//        // 	if its owner context is already nested in the inner context, skip it.
//        //		go recursively outward in contexts until we're in one that is not entirely selected 
//        //		if there is already a dominant context then the two must match
//        //		else fix that outermost context as the dominant context
//        // NEEDS FIXING!! Fails to catch overlapping contexts when outer graph is dominant
//        Graph g = null;
//        Graph dominantContext = null;		// start out with none
//        GraphObject go = null;
//        //ArrayList v = (ArrayList)vnodes.clone();
//        //Util.showArrayList( vnodes );
//        Iterator nodes = vnodes.iterator();
//        while ( nodes.hasNext() ) // looking at all elements in the collection
//        {
//            go = (GraphObject)nodes.next();
//            // if this is the outermost graph, then it must be dominant
//            if ( go.getOwnerGraph() == null && go instanceof Graph ) {
//                return (Graph)go;
//            }
//            // start by assuming this element's owner is the dominant context
//            g = go.getOwnerGraph();
//            // go outward until we reach a parent graph doesn't contain the entire collection; possible dominant one
//            while ( g.getOwnerGraph() != null && g.getOwnerGraph().containsGraphObjects( vnodes ) ) {
//                g = g.getOwnerGraph();
//            }
//            // if we're at the outermost context, we might as well stop here; but could still be overlapping
//            if ( g.getOwnerGraph() == null ) {
//                return g;
////                Global.info( "next possible dominant context is " + dominantContext.getTextLabel() );
//            } else {
//                if ( g != dominantContext ) // if a different potential dominant context than before,
//                // one must be nested in the other, and the nested one completely included
//                {
//                    if ( dominantContext.nestedWithin( g )
//                            && dominantContext.containsGraphObjects( vnodes )
//                            && vnodes.contains( dominantContext ) ) {
//                        dominantContext = g;
////                        Global.info( "next possible dominant context is " + dominantContext.getTextLabel() );
//                    } else {
////                        Global.info( "see if g inside dc: nestedwithin " + g.nestedWithin( dominantContext ) +
////                        	" entirely selected " + g.containsGraphObjects( vnodes ) + 
////                        	" g itself selected " + vnodes.contains( g) );
//                        if ( g.nestedWithin( dominantContext )
//                                && g.containsGraphObjects( vnodes )
//                                && vnodes.contains( g ) ) {
//                            // g is entirely nested, its contents and itself are in the selection; g is okay!
////                            Global.info( "context entirely nested: " + g.getTextLabel() );
//                        } else {
//                            Global.info( "g " + g.getTextLabel() + " dominantcontext " + dominantContext.getTextLabel() );
//                            throw new CGContextException( "No unique outer context" );
//                        }
//                    }
//                }
//            }
//        }
//        return dominantContext;
//    }

    /**
     * Find the graph that is the outermost graph of the given object.
     *
     * @return outermost graph, self if this one is the outermost
     */
    public Graph getOutermostGraph() {
        //Global.info( "begin outermost graph with " + CGUtil.shortClassName( this ) + " " + getTextLabel() );
        if ( ownerGraph == null ) {
            if ( this instanceof Graph ) {
                return (Graph)this;
            } else {
                return null;
            }
        } else {
            Graph g = this.getOwnerGraph();		// we've already checked to ensure that g isn't null
            //Global.info( " outermost graph -- finding outer graph starting with " + CGUtil.shortClassName( this ) + " " + getTextLabel() );
            while ( g.getOwnerGraph() != null ) {
                //	Global.info( "g.getOwnerGraph() != null: g is " + g.getTextLabel() );
                g = g.getOwnerGraph();
            }
            //Global.info( "end outermost graph with graph " + getTextLabel() );
            return (Graph)g;
        }
    }

    /**
     * Determine whether the given graph is negatively nested; i.e., is it
     * oddly-nested in the existential graph sense (where only cuts/negative
     * contexts matter).
     *
     * @return true if we're negatively nested (ultimately oddly-nested with
     * respect to cuts).
     */
    public boolean isNegativelyNested() {
        //Global.info( "begin outermost graph with " + CGUtil.shortClassName( this ) + " " + getTextLabel() );
        if ( ownerGraph == null ) {
            return false;       // without an owner, got to be positive
        } else {
            boolean negativeSoFar = this.isNegated;
            Graph g = this.getOwnerGraph();		// we've already checked to ensure that g isn't null
            //Global.info( " outermost graph -- finding outer graph starting with " + CGUtil.shortClassName( this ) + " " + getTextLabel() );
            while ( g != null ) {
                if ( g.isNegated() ) {
                    negativeSoFar = !negativeSoFar;
                }
                //	Global.info( "g.getOwnerGraph() != null: g is " + g.getTextLabel() );
                g = g.getOwnerGraph();
            }
            //Global.info( "end outermost graph with graph " + getTextLabel() );
            return negativeSoFar;
        }
    }

    /**
     * Determines the owner edit frame (if any) for a given graph object,
     * regardless of the level of nesting.
     *
     * @return the edit frame in which this object is located; <code>null</code>
     * if it is not in a frame.
     */
    public EditFrame getOwnerFrame() {
        //if ( go == null ) return null;
        Graph g = getOutermostGraph();
        if ( g == null ) {
            return null;
        } else {
            return g.getOwnerFrame();
        }
    }

    /**
     * Find the most immediate enclosing graph.
     *
     * @return enclosing graph; <code>null</code> if we're at the top level.
     */
    public Graph getOwnerGraph() {
        return ownerGraph;
    }

        /**
     * Set the owner of this graph.
     * @param g the owner of this graph
     *
     */
    public void setOwnerGraph( Graph g ) {
         ownerGraph = g;
    }

    /**
     * Constructs an object locator for this object.
     *
     * @return an object locator for this object.
     * @see charger.util.ObjectLocator
     */
    public ObjectLocator getObjectLocator() {
        java.io.File f = null;
        EditFrame owner = getOwnerFrame();
        if ( owner != null ) {
            f = owner.graphAbsoluteFile;
        }
        //charger.Global.info( "class used is " + this.getClass().getName() );
        return new ObjectLocator( f, this.getClass(), objectID );
    }

    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } catch ( Throwable t ) {
            throw t;
        } finally {
            super.finalize();
        }
    }
    
    public void addHistoryEvent( ObjectHistoryEvent he ) {
        history.addHistoryEvent( he );
    }
    
    public ObjectHistory getHistory() {
        return history;
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont( Font labelFont ) {
        this.labelFont = labelFont;
        
    }
    

} // class
