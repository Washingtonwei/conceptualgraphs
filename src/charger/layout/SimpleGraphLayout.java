/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.layout;

import charger.Global;
import charger.obj.GEdge;
import charger.obj.GNode;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.obj.ShallowIterator;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

/**
 * An algorithm to lay out a graph's nodes in left-to-right top-to-bottom order,
 * more or less equally spaced. Contexts aren't considered and probably end up overlapping where they shouldn't.
 * Makes no attempt to minimize (or even pay attention to) edges or edge length.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class SimpleGraphLayout extends GraphLayout {

    /**
     * A 2-dimensional grid for laying out nodes.
     */
    GNode[][] grid = null;
    int numRows = 0;
    int numCols = 0;
    int gridCellWidth = 0;
    int gridCellHeight = 0;
    
    int gridCellPadding = 20;
    int numNodes = 0;
    Graphics2D graphics = null;

    public SimpleGraphLayout( Graph g, Graphics2D graphics ) {
        super( g );
        this.graphics = graphics;
    }
    
//    public SimpleGraphLayout( ArrayList<GraphObject> selectedNodes ) {
//        
//    }

    /**
     *
     * @return true if all went well; false if something went wrong.
     */
    public boolean performLayout() {
         ShallowIterator iter = new ShallowIterator( graph, GraphObject.Kind.GNODE );
       establishGrid( iter );
          iter = new ShallowIterator( graph, GraphObject.Kind.GNODE );
        naivePlacement( iter );
        return true;
    }

    /**
     * Places nodes in row first, right-to-left order without regard to any links.
     * @param iter An iterator of GraphObjects
     */
    public void reduceEdgeLengthPlacement( Iterator<GraphObject> iter ) {
                // first start with the naive approach
        naivePlacement( iter );
            // now reduce line lengths where you can
        for ( int r = 0; r < numRows; r++ ) {
            for ( int c = 0; c < numCols; c++ ) {
                 if ( grid[r][c] != null ) {
                     
                 }
            }
        }
    }
    
    /**
     * Places nodes in row first, right-to-left order without regard to any links.
     * @param iter An iterator of GraphObjects
     */
    public void naivePlacement( Iterator<GraphObject> iter ) {
        for ( int r = 0; r < numRows; r++ ) {
            for ( int c = 0; c < numCols; c++ ) {
                if ( iter.hasNext() ) {
                    GNode node = (GNode)iter.next();
                    placeObjectInGrid( node, r, c );
                    grid[r][c] = node;
                } else {
                    grid[r][c] = null;
                }
            }
        }
    }

    public void placeObjectInGrid( GNode node, int row, int col ) {
        int xmargin = gridCellWidth / 2;
        int ymargin = gridCellHeight / 2;
        node.setCenter( new Point2D.Double( xmargin + (row * gridCellWidth) , ymargin + (col * gridCellHeight) ) );
    }

    /**
     * Determine the rows and columns of the grid layout, and the max width and
     * height per grid cell.
     */
    public void establishGrid( Iterator<GraphObject> iter ) {

        while ( iter.hasNext() ) {
            numNodes++;
            GNode node = (GNode)iter.next();
            node.resizeIfNecessary( graphics.getFontMetrics( node.getLabelFont() ), new Point2D.Double( 0, 0 ) );

            int width = node.getSize().width;
            if ( width > gridCellWidth ) {
                gridCellWidth = width;
            }

            int height = node.getSize().height;
            if ( height > gridCellHeight ) {
                gridCellHeight = height;
            }
        }
        
        gridCellWidth *= 1.33;
        gridCellHeight *= 2;
//        gridCellWidth += gridCellPadding;
//        gridCellHeight += gridCellPadding;
        
        numRows = (int)Math.ceil( Math.sqrt( (double)numNodes ) );
        numCols = (int)Math.ceil( Math.sqrt( (double)numNodes ) );
        Global.info( "cell width, height " + gridCellWidth + ", " + gridCellHeight );
        Global.info( "Rows, columns: " + numRows + ", " + numCols );
        grid = new GNode[numRows][numCols];
    }
    
    public void copyToGraph() {
        
    }
}
