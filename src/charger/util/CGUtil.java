package charger.util;

import charger.*;
import charger.obj.*;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.Color.*;
import javax.swing.*;

/**
 * Utility classes (all static!) that should be of fairly general use
 */
public class CGUtil {

    /**
     * Displays the text string centered about the point x,y in the given color.
     * taken from p 586, Naughton and Schildt, 1996
     *
     * @param g the current Graphics context
     * @param s the string to be displayed
     * @param x horizontal position of the centerpoint (in pixels)
     * @param y vertical position of the centerpoint (in pixels)
     * @param c the color to be displayed
     */
    static public void drawCenteredString( Graphics2D g, String s, double x, double y, Color c ) {
        FontMetrics fm = g.getFontMetrics();
        double startx = x - ( ( fm.stringWidth( s ) ) / 2 );
//        float starty = y  +  ( fm.getAscent() + fm.getDescent() + fm.getLeading() ) / 2;
        double starty = y - 1 + ( fm.getDescent() + fm.getAscent() ) / 2;
        Color oldcolor = g.getColor();
        g.setColor( c );
        g.drawString( s, (float)startx, (float)starty );
        g.setColor( oldcolor );
    }

    /**
     * Displays a centered text string in black
     *
     * @see CGUtil#drawCenteredString
     */
    static public void drawCenteredString( Graphics2D g, String s, double x, double y ) {
        drawCenteredString( g, s, x, y, Color.black );
    }

    static public void drawCenteredString( Graphics2D g, String s, Point2D.Double p, Color c ) {
        drawCenteredString( g, s, p.x, p.y, c );
    }

    /**
     * Returns the short (un-qualified) name string of the given object
     *
     * @param obj Any Java object
     * @return unqualified name of the object's class
     */
    static public String shortClassName( Object obj ) {
        String full = obj.getClass().getName();
        int index = full.lastIndexOf( '.' );
        if ( index == 0 ) {
            return full;
        } else {
            return full.substring( index + 1 );
        }
    }

    /**
     * Find the lower left point for displaying a string whose center point is
     * known. adapted from p 586, Naughton and Schildt, 1996
     *
     * @param fm any font metrics
     * @param s the string to be displayed
     * @param center position of the centerpoint (in pixels)
     */
    static public Point2D.Double getStringLowerLeftFromCenter( FontMetrics fm, String s, Point2D.Double center ) {
        if ( fm == null || s == null ) {
            return center;
        }
        double startx = center.x - ( ( fm.stringWidth( s ) ) / 2 );
        //int starty = center.y - 1 + ( fm.getDescent() + fm.getAscent() )/2;
        double starty = center.y - 1 + ( fm.getAscent() ) / 2;
        return new Point2D.Double( startx, starty );
    }
    
//        /**
//     * 
//     * @param objects
//     * @param how either "vertical" or "horizontal"
//     */
//    public static void alignObjects( ArrayList<GraphObject> objects, String how ) {
//        
//    }
    
    /**
     * Gather together all the selected nodes, edges and graphs into a list. The
     * list is sorted: graphs first, then nodes then edges. Responsible for making sure that no object
     * is included more than once (e.g., if it is enclosed in a graph that's in
     * the list as well as appearing by itself.) If an edge is in the list, but
     * either of its linked nodes is NOT in the list, then the edge is not
     * included. If any two selected nodes are linked by an edge, then that edge
     * should be included whether it was originally selected or not.
     *
     * @return collection of selected objects
     */
    public static ArrayList<GraphObject> sortObjects( ArrayList<GraphObject> list  ) {
        Iterator iter = list.iterator();
        GraphObject go = null;

        ArrayList<Graph> graphs = new ArrayList<>();
        ArrayList<GNode> nodes = new ArrayList<>();
        ArrayList<GEdge> edges = new ArrayList<>();
        while ( iter.hasNext() ) {		// looking at all selected elements in the graph, ignoring edges

            go = (GraphObject)iter.next();
                    // if the object is in a graph that's selected, then skip it
            if ( list.contains( go.getOwnerGraph() )) continue;
            //if (go.isSelected) {
            if ( go.myKind == GraphObject.Kind.GRAPH ) {
                graphs.add( (Graph)go );
            } else if ( go.myKind == GraphObject.Kind.GNODE ) {
                nodes.add( (GNode)go );
            } else if ( go.myKind == GraphObject.Kind.GEDGE ) {
                edges.add( (GEdge)go );
            }
        }
        //}
        ArrayList<GraphObject> sortedOnes = new ArrayList<>();
        sortedOnes.addAll( graphs  );
        sortedOnes.addAll( nodes );
        sortedOnes.addAll(  edges );
        
        return sortedOnes;
    }


    /**
     * returns the AWT rectangle given any two opposite corners, correcting for
     * signs. Corners may either be a combination of upper left and lower right
     * or else upper right and lower left.
     *
     * @param x1 are the x,y's for points 1 and 2 representing opposite corners
     * of a rectangle
     * @param y1
     * @param x2
     * @param y2
     */
    static public Rectangle2D.Double adjustForCartesian( double x1, double y1, double x2, double y2 ) {
        Rectangle2D.Double r = new Rectangle2D.Double( x1, y1, Math.abs( x1 - x2 ), Math.abs( y1 - y2 ) );
        if ( x1 > x2 ) {
            r.x = x2;
        }
        if ( y1 > y2 ) {
            r.y = y2;
        }
        return r;
    }

    /**
     * returns the AWT rectangle given its two opposite corners
     *
     * @param p1 and p2 are the two points representing opposite corners of a
     * rectangle
     * @param p2
     */
    static public Rectangle2D.Double adjustForCartesian( Point2D.Double p1, Point2D.Double p2 ) {
        Rectangle2D.Double r = new Rectangle2D.Double( p1.x, p1.y, Math.abs( p1.x - p2.x ), Math.abs( p1.y - p2.y ) );
        if ( p1.x > p2.x ) {
            r.x = p2.x;
        }
        if ( p1.y > p2.y ) {
            r.y = p2.y;
        }
        return r;
    }

    /**
     * Calculate height and width on a string using a given font metrics.
     *
     * @param s String whose dimensions are desired.
     * @param fm Font metrics under which the string is to be rendered.
     * @return Dimensions of the string.
     */
    static public Dimension stringDimensions( String s, FontMetrics fm ) {
        //Global.info( "stringDimensions -- \"" + s ;
        if ( s == null ) {
            s = "";
        }
        Dimension returnOne = new Dimension();
        if ( fm == null ) {
            fm = Global.defaultFontMetrics;
        }
        if ( fm != null ) {
            returnOne.height = fm.getHeight();
            returnOne.width = fm.stringWidth( s );
        }
        return returnOne;
    }

    /**
     * Loads choice list from the enumeration given. Makes no assumptions about
     * whether choice is visible, listened-to, etc.
     *
     * @param c The (already allocated) Choice to be loaded
     * @param choices The list of choice strings to be put into the list.
     * @param fm The currently applicable font metrics, used for determining
     * size of box
     */
    public static int fillChoiceList( JComboBox c, String[] choices, FontMetrics fm ) {
        
        float width = 0;
        float height = fm.getHeight();
        
        if ( c.getItemCount() > 0 ) {
            c.removeAllItems();
        }
        for ( int choicenum = 0; choicenum < choices.length; choicenum++ ) {
            String s = choices[ choicenum];
            c.addItem( s );
            Dimension dim = CGUtil.stringDimensions( s, fm );
            float w = (float)dim.getWidth();
            //Global.info( "width of string " + s + " is " + w );
            if ( w > width ) {
                width = w;
            }
        }
        return (int)width;
        //Global.info( "width of choice box is " + width );
//        c.setSize( new Dimension( (int)width * 2, (int)( height * 2 ) ) );
    }

    /**
     * Loads given text field with given string, resizing the field if
     * necessary. Makes no assumptions about whether field is visible,
     * listened-to, etc.
     *
     * @param tf The (already allocated) TextField to be loaded
     * @param textLabel The string to load into tf
     * @param fm font metrics from the context for which the combo box will be
     * sized
     */
    public static void loadSizedTextField( JTextField tf, String textLabel, FontMetrics fm ) {
        tf.setText( textLabel );
        int width = fm.stringWidth( textLabel ) * 2;
        if ( width < fm.stringWidth( "MMMM" ) ) {
            width = fm.stringWidth( "MMMM" );
        }
//        tf.setSize( width, fm.getHeight() * 4 / 3 );
//        tf.setFont( fm.getFont() );

    }

    /**
     * Loads combo box, setting given string as the default, resizing the field
     * if necessary. Assumes combo box is initialized and if popup is used,
     * popup is already filled.
     *
     * @param cb The (already allocated) JComboBox to be loaded
     * @param textLabel The string to load into tf
     * @param fm font metrics from the context for which the combo box will be
     * sized
     */
    public static void loadSizedComboBox( JComboBox cb, String textLabel, FontMetrics fm ) {
        //Global.info( "load combo box to match " + textLabel );
        cb.setFont( fm.getFont() );
        int count = cb.getItemCount();
        int k = 0;
        while ( k < count && !( (String)cb.getItemAt( k ) ).equalsIgnoreCase( textLabel ) ) {
            k++;
        }
        if ( k < count ) {
            cb.setSelectedIndex( k );
        }

        int width = 0;
        for ( k = 0; k < count; k++ ) {
            int w = fm.stringWidth( (String)cb.getItemAt( k ) );
            if ( width < w ) {
                width = w;
            }
        }
//        cb.setSize( width + 40, fm.getHeight() * 5 / 2 );
//        cb.setPreferredSize( cb.getSize() );
        
        ComboBoxEditor editor = cb.getEditor();
        Component editorComp = editor.getEditorComponent();

        //Global.info( "selected index is " + k + " out of " + count );
        cb.getEditor().getEditorComponent().setFont( fm.getFont() );
//        cb.getEditor().getEditorComponent().setSize( cb.getSize() );
//        cb.getEditor().getEditorComponent().setMinimumSize( cb.getSize() );
    }

    /**
     * Determines the union of a set of nodes' display rectangles. For a context
     * node, treats its entire extent as its rectangle.
     *
     * @param nodeList Set of Graph objects.
     * @return union of nodeList's display rectangles
     */
    public static Rectangle2D.Double unionDisplayRects( ArrayList nodeList ) {
        Iterator iter = nodeList.iterator();
        GraphObject go = null;
        Rectangle2D.Double r = null;
        while ( iter.hasNext() ) {
            go = (GraphObject)iter.next();
            if ( r == null ) {
                r = Util.make2DDouble( go.displayRect );
            } else {
                r.add(  Util.make2DDouble( go.displayRect ));
            }
        }
        return r;
    }

    /**
     * Displays a point's coordinates in text at its location.
     *
     * @param g context in which to draw
     * @param p point being displayed.
     */
    public static void showPoint( Graphics2D g, Point2D.Double p ) {
        g.drawString( "(" + Math.round(p.x) + "," + Math.round(p.y) + ")", (float)p.x, (float)p.y );
    }


//    /**
//     * Creates a color-complement version of an ImageIcon.
//     *
//     * @param normal the image icon to be reversed.
//     * @param c some component to which the image icon belongs
//     * @return inverted icon
//     */
//    public static ImageIcon invertImageIcon( ImageIcon normal, Component c ) {
//        Image src = normal.getImage();
//        ImageFilter colorfilter = new InvertFilter();
//        Image img = c.createImage( new FilteredImageSource( src.getSource(), colorfilter ) );
//        return new ImageIcon( img );
//    }

    /**
     * Returns the darker of the two colors, using a very crude algorithm.
     */
    public static Color getDarkest( Color c1, Color c2 ) {
        int sum1 = c1.getRed() + c1.getBlue() + c1.getGreen();
        int sum2 = c2.getRed() + c2.getBlue() + c2.getGreen();
        if ( sum1 < sum2 ) {
            return c1;
        } else {
            return c2;
        }
    }

//    /**
//     * Mimics behavior of jawa.awt.Rectangle.grow which was oddly omitted from
//     * Rectangle2D
//     */
//    public static void grow( Rectangle2D.Float r, float xinc, float yinc ) {
//        float newx = r.x - xinc;
//        float newy = r.y - yinc;
//        float newwidth = r.width + 2 * xinc;
//        float newheight = r.height + 2 * yinc;
//        r.setFrame( newx, newy, newwidth, newheight );
//    }

        /**
     * Mimics behavior of jawa.awt.Rectangle.grow which was oddly omitted from
     * Rectangle2D
     */
    public static void grow( Rectangle2D.Double r, double xinc, double yinc ) {
        double newx = r.x - xinc;
        double newy = r.y - yinc;
        double newwidth = r.width + 2 * xinc;
        double newheight = r.height + 2 * yinc;
        r.setFrame( newx, newy, newwidth, newheight );
    }

    /**
     * Returns the center of the rectangle as a Point2D.Double
     */
    public static Point2D.Double getCenter( Rectangle2D.Double r ) {
        return new Point2D.Double( r.getCenterX(), r.getCenterY() );
    }
    
        /** Used in determining connected components. */

    public static ArrayList< ArrayList<GNode> > getConnectedComponents( Graph g ) {
             HashMap<GNode, Integer> componentLabels = new HashMap<>();
             
             ArrayList< ArrayList<GNode> > nodeLists = new ArrayList< ArrayList<GNode>>();

        /* Initialize the hashmap so that all nodes have zero (meaning not visited yet).
         * Traverse the graph, starting at 1, and label each node in the hashmap with 
         * that integer until you can't reach any more. 
         * Increment counter, look for unvisited nodes and repeat until all nodes are accounted for.
         */
         ShallowIterator iterForInit = new ShallowIterator( g, GraphObject.Kind.GNODE );
         while ( iterForInit.hasNext() ) {
             GNode go = (GNode)iterForInit.next();
             componentLabels.put( go, Integer.valueOf( -1) );
         }
         
         int componentNum = -1;
         
                // while there are still unlabeled nodes
         while ( componentLabels.containsValue( Integer.valueOf( -1 )) ) {
             componentNum++;
                // find an unlabeled node, and label everything connected to it.
             Object[] nodes = componentLabels.keySet().toArray();
             int nodenum = 0;
             while ( ! componentLabels.get( (GNode)nodes[nodenum] ).equals( Integer.valueOf(-1)) )
                 nodenum++;
                    //
                 GNode startNode =  (GNode)nodes[nodenum] ;
                 labelConnectedNodes( componentLabels, startNode, componentNum );
             
         }
         
            // set up all the empty component label lists
         for ( int cnum = 0; cnum <= componentNum; cnum++ ) {
              ArrayList<GNode> labelsForOneComponent = new ArrayList<GNode>();
              nodeLists.add( labelsForOneComponent );
         }
                // display the results
         for ( int cnum = 0; cnum <= componentNum; cnum++ ) {
             for ( GNode node : componentLabels.keySet() ) {
                 if ( componentLabels.get( node).equals(  Integer.valueOf( cnum) ) ) {
//                     Global.info( "  node " + node.getTextLabel() );
                     nodeLists.get( cnum ).add( node );
                 }
             }
//                          Global.info( "Component number " + cnum + " has " + nodeLists.get( cnum ).size() + " nodes.");
         }
           return nodeLists;  
    }
    
    /**
     * Start by labeling the start node and then recursively find all the others
     *
     * @param startNode
     */
    private static void labelConnectedNodes( HashMap<GNode, Integer> componentLabels, GNode startNode, int componentNum ) {
        Integer label = Integer.valueOf( componentNum );
        componentLabels.put( startNode, label );
        ArrayList<GEdge> edges = startNode.getEdges();
        for ( GEdge edge : edges ) {    // for all edges, label their other nodes
            GNode oneToLabel = null;
            if ( edge.howLinked( startNode ) == GEdge.Direction.FROM ) {
                oneToLabel = (GNode)edge.toObj;
            } else { // startNode is the "to" node
                oneToLabel = (GNode)edge.fromObj;
            }
                    // Don't have to worry here about contexts in the spring algorithm at least,
                    // because we've already installed custom edges for contexts
            if (  componentLabels.get( oneToLabel ) == Integer.valueOf( -1 )  ) {
                labelConnectedNodes( componentLabels, oneToLabel, componentNum );
            }

        }
    }
    
    /**
     * Finds the center of the bounding rectangle that enclosed all the nodes
     * @param nodes
     * @return the center point of the rectangle that bounds all of the nodes in the list.
     */
    static public Point2D.Double getCenterPoint( ArrayList<GNode> nodes ) {
        if ( nodes.size() == 0 ) return null;
        Rectangle2D.Double rect = Util.make2DDouble( nodes.get( 0 ).getDisplayRect() );
        for ( GNode node : nodes ) {
            rect.add( node.getDisplayRect());
        }
        return new Point2D.Double( rect.x + rect.width/2, rect.y + rect.height / 2 );
    }
    
    /**
     * Finds the object in the list that is closest to the point given
     */
    static public GNode closestObject( ArrayList<GNode> nodes, Point2D.Double point ) {
        if ( nodes.size() == 0 ) return null;
        double shortest = Double.MAX_VALUE;
        GNode closest = null;
        for ( GNode node : nodes ) {
//            if ( node instanceof Graph ) continue;
            double distance = node.getCenter().distance( point );
            if ( distance < shortest ) {
                shortest = distance;
                closest = node;
            }
        }
        return closest;
    }
}
