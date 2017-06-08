/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.obj;

import charger.Global;
import charger.util.Util;
import charger.util.WrappedText;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * An arbitrary text node to annotate a graph.
 * Conveys no semantics, but can be useful for documentation purposes.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class Note extends GNode {

    private static final int cornerFold = 10;       // how wide is the "dog ear" in the corner
    
    public Note( ) {
        setTextLabel( "Note text");
        setColor();
    }

    /**
     * A note is a rectangle with a "dog ear" (similar to UML note) that is
     * inscribed within its displayRect.
     *
     * @see GNode#getShape
     */
    public Shape getShape() {
        //Rectangle2D.Double r = Util.make2DDouble( displayRect );
        Rectangle2D.Double r = Util.make2DDouble( displayRect );
        //r.grow( 10, 10 );	// 05-27-03 but was deprecated in JDK 1.5
        // r.add( r.x + r.width + growIncrement/2, r.y + r.height + growIncrement/2 );
        // r.add( r.x - growIncrement/2, r.y - growIncrement/2 );
        GeneralPath p2d = new GeneralPath();
        
        p2d.moveTo( r.x, r.y ); // start at top vertex
        p2d.lineTo( r.x + r.width - cornerFold, r.y );	// line to top vertex of "dog ear"
//        p2d.lineTo( r.x + r.width - cornerFold, r.y + cornerFold ); // line to corner of dog ear
        p2d.lineTo( r.x + r.width, r.y + cornerFold );	// line to right edge
//        p2d.lineTo( r.x + r.width - cornerFold, r.y );	// diagonal line to top vertex of dog ear
//        p2d.lineTo( r.x + r.width, r.y + cornerFold );	// retrace line to right edge
        p2d.lineTo( r.x + r.width, r.y + r.height ); 	// line to bottom right vertex
        p2d.lineTo( r.x, r.y + r.height );	// line to bottom left vertex
        p2d.lineTo( r.x, r.y ); // move to top vertex
        return p2d;
    }
    
        /**
     * Draws a concept on the given graphics context. Draws the box, label,
     * shadows (if any).
     *
     * @param g graphics context on which to draw
     * @param printing (ignored; appears for consistency only)
     */
    public void draw( Graphics2D g, boolean printing ) {
        Rectangle2D.Double shadowrect = new Rectangle2D.Double(
                displayRect.x + Global.shadowOffset.x, displayRect.y + Global.shadowOffset.y,
                displayRect.width, displayRect.height );

        if ( Global.showShadows ) {
            g.setColor( Global.shadowColor );
            g.translate( Global.shadowOffset.x, Global.shadowOffset.y );
            g.fill( getShape() );
            g.translate( -1 * Global.shadowOffset.x, -1 * Global.shadowOffset.y );
        }

        Color tempColor = getColor( "text" );
        if ( isChanged() ) {
            foreColor = getColor( "fill" );
            backColor = tempColor;
        } else {
            backColor = getColor( "fill" );
            foreColor = getColor( "text" );
        }


        g.setColor( backColor );
        g.fill( getShape() );

        g.setColor( foreColor );

        WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
        Font font = g.getFont();
        font = new Font( font.getFontName(), font.getStyle(), font.getSize() + 2 );
        text.setEnableWrapping( getWrapLabels() );
        text.drawWrappedText( g, getCenter(), getLabelFont() );


        if ( isChanged() ) // reverse the colors
        {
            setColor( "fill", getColor( "text" ) );
            //setColor( "text", getColor( "fill" ) );
            setColor( "text", tempColor );
        }
        super.draw( g, printing );
    }

}
