/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger.obj;

import charger.Global;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

/**
 * Provides a custom edge that is not semantically constrained by the types of its nodes.
 * Currently it's used to provide a harmless edge for graph layout.
 * 
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class CustomEdge extends GEdge {

    public CustomEdge() {
        
    }
    
    public CustomEdge( GraphObject FromOne, GraphObject ToOne ) {
        super( FromOne, ToOne );
    }
    
    
    public void draw( Graphics2D g, boolean printing ) {
        Color oldcolor = g.getColor();
        g.setColor( Color.red );
        Shape line = new Line2D.Double( fromPt, toPt );
//        g.draw( g.getStroke().createStrokedShape( line ) );
        g.setColor( oldcolor );
    }

}
