package charger.obj;

//import charger.util.*;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import charger.*;
import charger.util.CGUtil;
import charger.util.Util;

//import com.traversetechnologies.fulcrum.math.LineGeometry;
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
 * Abstract superclass for arrows and co-referent links. Assume that an edge has
 * exactly two end objects. displayRect consists of a tiny rectangle at the
 * midpt for selection purposes
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
abstract public class GEdge extends GraphObject  {

    public GraphObject fromObj, toObj;
    
      /**
     * used for labeling the connected nodes; i.e., a "from" node
     */
  public enum Direction {
        FROM,
        TO,
        UNLINKED
    }
  
//    public static int FROM = 0;
    /**
     * used for indicating an output node; i.e., a "to" node
     */
//    public static int TO = 1;
    /**
     * used for indicating the node isn't connected
     */
//    public static int UNLINKED = 2;
    // how "spread out" & "long" is an arrowhead (which may appear at the MIDPOINT of the edge)
    public EdgeAttributes edgeAttributes = new EdgeAttributes( );
//    public  int arrowHeadWidth = 0;
//    public  int arrowHeadHeight = 0; 
//    public  float edgeThickness = Global.edgeThickness;
    // --- arrow calculations done once at construct time when possible
//    private static float arrowHeadLength = (float)Math.sqrt( arrowWidth * arrowWidth + arrowHeight * arrowHeight );
//    private static float arrowHeadAngle = (float)Math.atan( (double)arrowWidth / (double)arrowHeight );
    /**
     * remembers its source point as set.
     */
    public Point2D.Double fromPt = new Point2D.Double();
    /**
     * remembers its destination point as set.
     *
     */
    public Point2D.Double toPt = new Point2D.Double();
    /**
     * remembers its middle point as set.
     *
     */
    Point2D.Double midPoint = new Point2D.Double();
    /* these three are set by makeArrowPoints
     @see makeArrowPoints
     */
    /**
     * the "point" of the arrow.
     *
     * @see #makeArrowPoints
     */
    Point2D.Double arrowPoint = new Point2D.Double();
    /**
     * one back corner of the arrow.
     *
     * @see #makeArrowPoints
     */
    Point2D.Double arrowEndPoint1 = new Point2D.Double();
    /**
     * another back corner of the arrow.
     *
     * @see #makeArrowPoints
     */
    Point2D.Double arrowEndPoint2 = new Point2D.Double();
    /**
     * Arrowhead triangle to be filled in when drawing an arrow.
     *
     * @see #arrowPoint
     * @see #arrowEndPoint1
     * @see #arrowEndPoint2
     */
    GeneralPath arrowHeadShape = null;

    /**
     * Constructs a new GEdge with label "-".
     */
    public GEdge() {
        textLabel = "-";
        myKind = GraphObject.Kind.GEDGE;
        setColor();
    }

    /**
     * Constructs a new GEdge with label "-" going from one object to another.
     *
     * @param FromOne edge starts here
     * @param ToOne edge ends here
     */
    public GEdge( GraphObject FromOne, GraphObject ToOne ) {
        super();
        textLabel = "-";
        myKind = GraphObject.Kind.GEDGE;
        fromObj = FromOne;
        toObj = ToOne;
        ( (GNode)FromOne ).attachGEdge( this );
        ( (GNode)ToOne ).attachGEdge( this );
        placeEdge();
    }

    /**
     * 
     * @return the display rectangle
     */
    public Shape getShape() {
        return displayRect;
    }

    /**
     * @return the length of the diagonal.
     */
    public float getLength() {
        return (float)fromPt.distance( toPt );
    }
            
    /**
     * Sets the colors of a GEdge to black. May be overridden in a sub-class.
     */
    public void setColor() {
        Color fromColor = Color.white;
        Color toColor = Color.white;

        // Global.info( "looking up color for " + CGUtil.shortClassName( this ) );
        if ( fromObj != null ) {
            fromColor = fromObj.getColor( "fill" );
        }
        if ( toObj != null ) {
            toColor = toObj.getColor( "fill" );
        }
        //Global.info( "from fill color is " + fromColor + "; to fill color is " + toColor );
        foreColor = CGUtil.getDarkest( fromColor, toColor );
        if ( foreColor.equals( Color.white ) ) // if it's still white
        {
            foreColor = Color.black;
        }

        backColor = Color.black;
        //Global.info( "GEdge forecolor is " + foreColor + "; backcolor is " + backColor );

    }
    
        private  float getArrowHeadLength() {
             return (float)Math.sqrt( 
                     this.getArrowHeadWidth() * this.getArrowHeadWidth() + this.getArrowHeadHeight() * this.getArrowHeadHeight() );
        }
    
        private  float getArrowHeadAngle() {
            return (float)Math.atan( (double)this.getArrowHeadWidth() / (double)this.getArrowHeadHeight() );
        }

    public int getArrowHeadWidth() {
        return edgeAttributes.arrowHeadWidth;
    }

    public void setArrowHeadWidth( int arrowHeadWidth ) {
        edgeAttributes.arrowHeadWidth = arrowHeadWidth;
    }
    
    public int getArrowHeadHeight() {
        return edgeAttributes.arrowHeadHeight;
    }

    public void setArrowHeadHeight( int arrowHeadHeight ) {
        edgeAttributes.arrowHeadHeight = arrowHeadHeight;
    }

    public double getEdgeThickness() {
        return edgeAttributes.edgeThickness;
    }

    public void setEdgeThickness( double edgeThickness ) {
        edgeAttributes.edgeThickness = edgeThickness;
    }
    
    public void initializeArrowHead() {
        setArrowHeadWidth( Global.userEdgeAttributes.getArrowHeadWidth() );
        setArrowHeadHeight( Global.userEdgeAttributes.getArrowHeadHeight() );
        makeArrowPoints( Global.arrowPointLocation);
    }
        
        


    /**
     * Currently empty, may be used in the future.
     */
    public void drawBorder( Graphics2D g, Color borderColor ) {
        // right now, doesn't make sense to do anything here
    }

    /**
     * Currently empty, may be used in the future.
     */
    /*protected void drawHighlighted( Graphics2D g, Color highlightColor )
     {
     // right now, doesn't make sense to do anything here
     }*/
    public String toString() {
        return super.toString() + "|" + fromObj.objectID + "," + toObj.objectID;
    }

    /**
     * Does everything but the actual removal (de-allocation)
     */
    public void abandonObject() {
        if ( ( fromObj != null ) || ( toObj != null ) ) {
            detachFromGNodes();
        }
    }




    /**
     * Finds the endpoint nodes of the edge, and removes the edge from each of
     * their lists
     */
    public void detachFromGNodes() {
        //Global.info( "starting to detach 2 gnodes: " + fromObj.getTextLabel() + ", " + toObj.getTextLabel());
        if ( fromObj != null ) {
            ( (GNode)fromObj ).deleteGEdge( this );
        }
        if ( toObj != null ) {
            ( (GNode)toObj ).deleteGEdge( this );
        }

        //Global.info( "detaching 2 nodes succeeded.");
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

    /**
     * Determines the placement of a GEdge. Uses the display rectangles of its
     * linked components, rather than their centers. Pre-calculates the
     * coordinates of its arrow points (whether used or not) for efficiency.
     *
     * @see GraphObject#displayRect
     * @see GEdge#getObjectShapeForClipping
     */
    public void placeEdge() {
//                Global.info( "calcDimensions: arrow from " + fromObj.getTextLabel() + " to " + toObj.getTextLabel() );
        fromPt = findClippingPoint( toObj.getCenter(), fromObj.getCenter(), getObjectShapeForClipping( fromObj ) );
        toPt = findClippingPoint( fromObj.getCenter(), toObj.getCenter(), getObjectShapeForClipping( toObj ) );


        makeArrowPoints( fromPt, toPt, Global.arrowPointLocation );
        Rectangle2D.Double r = CGUtil.adjustForCartesian( fromPt.x, fromPt.y, toPt.x, toPt.y );
        //Global.info( "calcDimensions for edge from " + fromObj.getTextLabel() + " to " +
        //		toObj.getTextLabel() + "; rectangle " + r );

          // draw the selection point in the middle; may conflict if arrow head is placed there
        double handleSize = 8;
        Point2D.Double mid = Util.midPoint( fromPt, toPt );

        displayRect = new Rectangle2D.Double(
                mid.x -  handleSize / 2 ,
                mid.y -  handleSize / 2 ,
                handleSize, handleSize );

        EditFrame ef = getOwnerFrame();
        FontMetrics fm = null;
        if ( ef != null ) {
            fm = ef.currentFontMetrics;
        } else {
            fm = Global.defaultFontMetrics;
        }

        textLabelLowerLeftPt = CGUtil.getStringLowerLeftFromCenter( fm, textLabel, getCenter() );
    }

    /**
     * Determines the appropriate rectangle to use as object boundaries when
     * drawing edges
     *
     * @param go The object whose boundary rectangle is sought.
     * @return The boundary rectangle for the object.
     */
    protected Shape getObjectShapeForClipping( GraphObject go ) {
        // @bug it may be useful to use Shape instead of rectangle so that Actors (and others) could be handled differently.
        String whatClass = CGUtil.shortClassName( go );
        //Rectangle2D.Double dr = (Rectangle2D.Double)(go.displayRect.clone());
        //dr.grow( 1, 1 );		// a kludge so that arrowheads won't look funny...
        if ( whatClass.equals( "Concept" ) ) {
            return go.displayRect;
        } else if ( whatClass.equals( "TypeLabel" ) ) {
            return go.displayRect;
        } else if ( whatClass.equals( "RelationLabel" ) ) {
            //return go.displayRect;
            return go.getShape();
        } else if ( whatClass.equals( "Relation" ) ) {
            //return go.displayRect;
            return go.getShape();
        } else if ( whatClass.equals( "Actor" ) ) {
            //return go.displayRect;
            return go.getShape();
        } else if ( whatClass.equals( "Graph" ) ) {
            return go.displayRect;
        } else {
            return new Rectangle2D.Double( go.getCenter().x, go.getCenter().y, 1f, 1f );
        }
    }
    
    /**
     * Convenience method for pre-calculating the arrow points for this edge (if needed).
     * Gets the end points from the edge's known end objects
     * @param howFar How far from the "from" object to draw the arrowhead. Ranges from 0 to 1.0;
     */
    public void makeArrowPoints( double howFar ) {
        if ( fromObj == null || toObj == null ) return;
        fromPt = findClippingPoint( toObj.getCenter(), fromObj.getCenter(), getObjectShapeForClipping( fromObj ) );
        toPt = findClippingPoint( fromObj.getCenter(), toObj.getCenter(), getObjectShapeForClipping( toObj ) );
        
        makeArrowPoints( fromPt, toPt, howFar );
    }

    /**
     * Sets the variables arrowPoint, arrowEndPoint1 and arrowEndPoint2 to speed
     * up drawing. Knows nothing about clipping the edge for rectangles, shapes,
     * etc. That must be done elsewhere. We assume that an arrowhead is a triangle, one of whose
     * vertices is on the line, while the other two are equidistant from the line.
     *
     * @param fromPt the beginning point of the edge
     * @param toPt the end point of the edge
     * @param howFar where the arrow should go where start is 0.0 and end is 1.0
     * @see GEdge#makeArrowPoints
     * @see GEdge#drawArrowHead
     */
    public void makeArrowPoints( Point2D.Double fromPt, Point2D.Double toPt, double howFar ) {
        double xp1, yp1, xp2, yp2;	 // these are the other "endpoints" of the arrowhead lines

        // theta is the angle of the original arrow edge itself
        // watch for divide by zero!!!
        float theta;
        if ( toPt.x == fromPt.x ) {
            if ( fromPt.y > toPt.y ) {
                theta = 3.0f * (float)Math.PI / 2.0f;
            } else {
                theta = (float)Math.PI / 2.0f;
            }
        } else {
            theta = (float)Math.atan( -1.0f * ( toPt.y - fromPt.y ) / ( toPt.x - fromPt.x ) );
        }
        if ( toPt.x > fromPt.x ) {
            theta += Math.PI;
        }

        // actual angles of the arrowhead lines from the horizontal
        float alpha1 = theta + this.getArrowHeadAngle();
        float alpha2 = theta - this.getArrowHeadAngle();

        // calculate x and y offsets from the "point" point
        xp1 = (float)( Math.cos( alpha1 ) * this.getArrowHeadLength() );
        yp1 = (float)( Math.sin( alpha1 ) * this.getArrowHeadLength() );

        xp2 = (float)( Math.cos( alpha2 ) * this.getArrowHeadLength() );
        yp2 = (float)( Math.sin( alpha2 ) * this.getArrowHeadLength() );

        // use Pythogorean theorem to determine edge length
        double length = Math.sqrt(
                Math.pow( ( toPt.x - fromPt.x ), 2.0f ) + Math.pow( (float)( toPt.y - fromPt.y ), 2.0f ) );
        length = length * howFar;
        //if ( Math.abs( howFar - 1.00 ) <= 0.1 )	// really testing if howfar == 1.00 
        if ( howFar == 1.00d ) {
            arrowPoint.x = toPt.x;
            arrowPoint.y = toPt.y;
        } else {
            //arrowPoint.x = fromPt.x - (int)Math.round((float) (length * Math.cos(theta) ) );  // signs seemed reversed
            //arrowPoint.y = fromPt.y +(int)Math.round( (float) (length * Math.sin(theta) ) );
            arrowPoint.x = fromPt.x - ( length * (float)Math.cos( theta ) );  // signs seemed reversed
            arrowPoint.y = fromPt.y + ( length * (float)Math.sin( theta ) );
        }
        arrowEndPoint1.x = arrowPoint.x + xp1; //(int)Math.round( xp1 );
        arrowEndPoint1.y = arrowPoint.y - yp1; //(int)Math.round( yp1 );
        arrowEndPoint2.x = arrowPoint.x + xp2; // (int)Math.round( xp2 );
        arrowEndPoint2.y = arrowPoint.y - yp2; // (int)Math.round(yp2 );

        // find the corner of the arrow head and make a polygon
        int xx[] = { (int)arrowPoint.x, (int)arrowEndPoint1.x, (int)arrowEndPoint2.x };
        int yy[] = { (int)arrowPoint.y, (int)arrowEndPoint1.y, (int)arrowEndPoint2.y };
        //arrowHeadShape = new Polygon( xx, yy, 3 );
        arrowHeadShape = new GeneralPath();
        // arrowHeadShape.append( new Line2D.Double( arrowPoint, arrowEndPoint1 ), false );
        // arrowHeadShape.append( new Line2D.Double( arrowEndPoint1, arrowEndPoint2 ), false);
        // arrowHeadShape.append( new Line2D.Double( arrowEndPoint2, arrowPoint ), false );
        arrowHeadShape.moveTo( arrowPoint.x, arrowPoint.y );
        arrowHeadShape.lineTo( arrowEndPoint1.x, arrowEndPoint1.y );
        arrowHeadShape.lineTo( arrowEndPoint2.x, arrowEndPoint2.y );
        arrowHeadShape.lineTo( arrowPoint.x, arrowPoint.y );
    }

    /**
     * Uses the instance variables arrowPoint, arrowEndPoint1 and arrowEndPoint2
     * to speed up drawing
     *
     * @param g Graphics context on which to draw
     * @param filled Whether to fill in the arrow head or not
     * @param printing Whether we are drawing to print or to display on screen
     * (not used)
     * @see GEdge#makeArrowPoints
     */
    public void drawArrowHead( Graphics2D g, boolean filled, boolean printing ) {
        // Global.info( "draw arrow head
        if ( arrowHeadShape != null ) {
            g.setStroke( new BasicStroke( 1.0f ) );
            if ( filled ) {
                g.fill( arrowHeadShape );
            }
            g.draw( arrowHeadShape );
        }
    }

    /**
     * Assuming that there's a shape at the toPt, find the clipping point on its boundary
     * along a line from the fromPt to the toPt.
     */
    public static Point2D.Double findClippingPoint( Point2D.Double fromPt, Point2D.Double toPt, Shape s ) {
//        Global.info( "at find clipping point, shape is " + s.getClass().getCanonicalName() + " " + s.toString());
        if ( s instanceof Rectangle2D.Double ) {
            return findClippingPointRectangle( fromPt, (Rectangle2D.Double)s );
        }
//            Global.info( "calling findclippingpointshape");
        return findClippingPointShape( fromPt, toPt, s );
        //else if ( s instanceof GeneralPath ) return findClippingPoint( fromPt, toPt, (GeneralPath)s );
        // return toPt;
    }

    /**
     * Given a point and a rectangle, find that point on the rectangle that lies
     * on the edge between the given point and the center of the rectangle
     *
     * @param fromPt any point on the canvas; assume it is outside the rectangle
     * @param rect the rectangle we want to intersect -- assume its center point
     * is the other end of the line
     * @return a point on the rectangle that is also on the edge between fromPt
     * and r's center.
     */
    public static Point2D.Double findClippingPointRectangle( Point2D.Double fromPt, Rectangle2D.Double rect ) {
        // beta is the angle of the rectangle's diagonal
        Rectangle2D.Double r = Util.make2DDouble( rect );
        CGUtil.grow( r, 1.0f, 1.0f );  // to smooth lines       
        // to take account of the lack of a drawn border
        if ( Global.showBorders ) {
            CGUtil.grow( r, GNode.borderOutlineWidth, GNode.borderOutlineWidth );
        }
        float beta = (float)Math.atan( r.height / r.width );

        Point2D.Double center = new Point2D.Double( r.x + r.width / 2, r.y + r.height / 2 );

        float newX = 0.0f, newY = 0.0f;
        float xdiff = 0.0f, ydiff = 0.0f;		// used for calculating offsets from the x and y axes

        // theta is the angle of the original edge itself in quadrant One; i.e., 0 .. 90.
        // watch for divide by zero!!!
        float theta;
        if ( center.x != fromPt.x ) {
            theta = (float)Math.atan( ( Math.abs( (float)( fromPt.y - center.y ) ) )
                    / Math.abs( (float)( fromPt.x - center.x ) ) );
        } else {
            theta = (float)Math.PI / 2.0f;
        }

        // not pretty, but then again, who picked this coordinate system anyway??
        if ( theta > beta ) {	// top or bottom
            xdiff = (float)( ( 1.0f / Math.tan( theta ) ) * r.height ) / 2.0f;
            if ( center.x > fromPt.x ) {
                xdiff *= -1.0f;
            }
            newX = (float)( center.x + xdiff );
            if ( center.y > fromPt.y ) { // top
                newY = (float)r.y;
            } else {	// bottom
                newY = (float)( r.y + r.height );
            }
        } else {						// a side
            ydiff = (float)( Math.tan( theta ) * r.width ) / 2.0f;
            if ( center.y > fromPt.y ) {
                ydiff *= -1.0;
            }
            newY = (float)( center.y + ydiff );
            if ( center.x > fromPt.x ) { // left side
                newX = (float)r.x;
            } else {	// right side
                newX = (float)( r.x + r.width );
            }
        }
        {
            //return new Point( (int)(newX + center.x)/2, (int)(newY + center.y)/2 );
        }
        //else
        return new Point2D.Double( newX, newY );
    }
    
    

    /**
     * Finds the clipping point for an arbitrary shape.
     *
     * @param fromPt origin of the line
     * @param toPt other end of the line (assumed to be inside the shape)
     * @param shape the general shape to be analyzed. If toPt is not inside the
     * shape, or if fromPt is already inside the shape, strange things will
     * happen.
     */
    public static Point2D.Double findClippingPointShape( Point2D.Double fromPt, Point2D.Double toPt, Shape shape ) {
        // general process is to keep cutting the line in half until it's very close to the boundary.
        // 1 - propose a line from the fromPt to the toPt, considering that as "front" to "back"
        // 2 - find the midpoint
        // 3 - if midpoint is inside the shape, use front half of the line, else use back half
        // 4 - if line used is less than one unit long (1.0f), then use front end as the clipping point
        //     otherwise, go to step 2
//        Global.info( "find clipping point: fromPt " + fromPt.toString() + "; toPt " + toPt.toString() );
        
        
        Point2D.Double from = fromPt;
        Point2D.Double to = toPt;
        Point2D.Double mid = Util.midPoint( from, to );
        if ( shape.contains( mid ) ) {
            
            to = mid;
        } else {
            from = mid;
        }
        
        if ( from.equals( fromPt ) && to.equals( toPt) ) return from;

        if ( (float)from.distance( to ) < 1.5f ) {
            return from;
        } else {
//            Global.info( "find clipping point shape; distance from,to " + (float)from.distance(to));
            return findClippingPointShape( from, to, shape );
        }

    }

//    /**
//     * Considers the display rect (not shape yet!) and returns the distance between 
//     * the clipping points of a center-to-center line between the objects.
//     */
//    public static float getClippedLength( GEdge edge ) {
//        Point2D.Double clipPointTo = findClippingPoint( edge.fromPt, edge.toPt, edge.toObj.getShape() );
//        Point2D.Double clipPointFrom = findClippingPoint( clipPointTo, edge.fromPt, edge.fromObj.getShape());
//        return (float)clipPointFrom.distance( clipPointTo );
//    }
//
//    /**
//     * Considers the display rect (not shape yet!) and returns the distance between 
//     * the clipping points of a center-to-center line between the objects.
//     */
//    public static float getClippedLength( GNode node1, GNode node2 ) {
//        Point2D.Double clipPointTo = findClippingPoint( edge.fromPt, edge.toPt, edge.toObj.getShape() );
//        Point2D.Double clipPointFrom = findClippingPoint( clipPointTo, edge.fromPt, edge.fromObj.getShape());
//        return (float)clipPointFrom.distance( clipPointTo );
//    }

    /**
     * Determines whether the two charger nodes are linked together, by any
     * GEdge.
     *
     * @param go1 one node
     * @param go2 another node
     * @return true if there is any GEdge connecting the two; false otherwise.
     */
    public static boolean areLinked( GNode go1, GNode go2 ) {
        ArrayList theEdges = go1.getEdges();
        Iterator iter = theEdges.iterator();
        while ( iter.hasNext() ) {
            if ( ( (GEdge)( iter.next() ) ).linkedTo( go2 ) != null ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether the given node is linked at all with this GEdge.
     *
     * @param gn is one end of the link
     * @return the node to which gn is connected via the GEdge, null if gn is
     * not linked
     */
    public GraphObject linkedTo( GNode gn ) {
        if ( gn == fromObj ) {
            return toObj;
        }
        if ( gn == toObj ) {
            return fromObj;
        }
        return null;
    }

    /**
     * Tells which end of the current link (if either) is the given GNode.
     *
     * @param gn is a possible end of the link
     * @return GEdge.Direction.FROM if gn is the "from" node for the link, or GEdge.Direction.TO if
     * gn is its "to" node.
     * @see Direction#FROM
     * @see Direction#TO
     * @see Direction#UNLINKED
     */
    public Direction howLinked( GNode gn ) {
        if ( gn == fromObj ) {
            return Direction.FROM;
        }
        if ( gn == toObj ) {
            return Direction.TO;
        }
        return Direction.UNLINKED;
    }

    /**
     * needed for GraphLayout interface
     */
//    public GLNode fromNode() {
//        return (GNode)fromObj;
//    }
//
    /**
     * needed for GraphLayout interface
     */
//    public GLNode toNode() {
//        return (GNode)toObj;
//    }
//    
    public boolean commitToKnowledgeBase( kb.KnowledgeBase kb ) {
        return true;
    }

    public boolean unCommitFromKnowledgeBase( kb.KnowledgeBase kb ) {
        return true;
    }

    
}
