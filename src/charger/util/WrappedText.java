/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger.util;

import charger.Global;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Represents a  text string processed for word wrapping. Wrapping considers all characters to be of equal width (i.e., think mono-spaced font)
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class WrappedText {
    
    /**
     * The set of characters that can be used to split lines.
     * Currently has the value " _-,;:\n\t"
     */
    public static String breakCharacters = " _-,;:\n\t";
    
    /**
     * The string being represented, as a single "line"
     */
    String s = new String();
    
    /**
     * The font in which this text is to be rendered.
     */
    Font font = null;
    
    /** the set of lines already wrapped */
    ArrayList<String> textLines = new ArrayList<String>();
    /** the number of columns to wrap. Note this does not take into account font char widths, etc.
      */
    int columns = 0;
    
    boolean enableWrapping = true;
    
    /**
     * Create a string to be wrapped, but turns off the wrapping function. User may call setColumns() to 
     * actually wrap the text.
     * @param s 
     */
    public WrappedText( String s ) {
        this.s = s;
        textLines = stringBreak( s, null, -1  );
    }

    /**
     * Create a string to be wrapped at no wider than a certain number of characters.
     * if enabledWrapping is false, then has no effect.
     * @param s the string to be wrapped
     * @param cols the maximum numbers of characters per line. 
     */
    public WrappedText( String s, int cols  ) {
        this.s = s;
        this.columns = cols;
//        textLines = stringBreakOLD( s, columns  );
        textLines = stringBreak( s, breakCharacters, columns  );
    }

    public int getColumns() {
        return columns;
    }

    /**
     * Sets the number of characters wide to render the text.
     * Refreshes the lines according to the new value.
     * @param columns a number of characters wide for the text. If -1, then no wrapping is performed and exactly one line is produced.
     */
    public void setColumns( int columns ) {
        this.columns = columns;
        textLines = stringBreak( s, breakCharacters, columns );
    }

    public Font getFont() {
        return font;
    }

    public void setFont( Font font ) {
        this.font = font;
    }

    /**
     * Whether to wrap the text or not
     * @return true if wrapping is enabled; false otherwise
     */
    public boolean isEnableWrapping() {
        return enableWrapping;
    }

    /**
     * Tells the wrapper whether to actually wrap the text or consider it all on one line.
     * @param enableWrapping 
     */
    public void setEnableWrapping( boolean enableWrapping ) {
        this.enableWrapping = enableWrapping;
        if ( enableWrapping ) {
            textLines = stringBreak( s, breakCharacters, columns  );
        } else {
            textLines = stringBreak( s, breakCharacters, -1 );
        }
    }
    
    
    
    /**
     * Get the number of lines needed to render the wrapped text.
     * @return number of lines needed to render the wrapped text
     */
    public int getNumLines() {
        return textLines.size();
    }
    
    /**
     * Uses drawString to render the text on a graphics context. All current
     * font choices, colors, etc. are used. This means that all the text will be
     * in the same font, etc.
     *
     * @param g the graphics context to be used
     * @param center the center point of the ENTIRE text. The method will figure
     * out how best to render the whole thing centered.
     */
    public void drawWrappedText( Graphics2D g, Point2D.Double center ) {
        Font previous = null;
        if ( font != null ) {
            previous = g.getFont();
            g.setFont( font );
        }
        
        FontMetrics fm = g.getFontMetrics();
        int height = fm.getHeight();
//        int height = fm.getAscent() + fm.getDescent() + fm.getLeading();
        int rows = getNumLines();
        Color color = g.getColor();
        Point2D.Double ctr = new Point2D.Double( center.x, (float)( center.y - ( (float)( rows - 1 ) / 2.0 ) * height - fm.getMaxDescent() ) );
        for ( int r = 0; r < rows; r++ ) {
            CGUtil.drawCenteredString( g, textLines.get( r ), ctr.x, ctr.y, color );
            ctr.y += height;
        }
        if ( previous != null ) {
            g.setFont( previous );
        }
    }

    public void drawWrappedText( Graphics2D g, Point2D.Double center, Font font ) {
        this.font = font;
//                Global.info( "at draw wrapped text; font is " + font.toString() );
        drawWrappedText( g, center );
    }
    
    /**
     * Find the size of the box in which the entire wrapped text fits.
     * @param g the graphics context which contains the font metrics.
     * @return the size of the box that encloses the entire displayed text.
     */
    public Dimension getSize( Graphics2D g ) {
        Dimension d = null;
        FontMetrics fm = g.getFontMetrics();
        if ( font != null ) {
//            Graphics2D newg = 
        }
        d = getSize( fm );
        return d;
    }
    
        /**
     * Find the size of the box in which the entire wrapped text fits.
     * @param fm the font metrics which controls the rendering
     * @return the size of the box that encloses the entire displayed text.
     */
    public Dimension getSize( FontMetrics fm ) {
        int width = 0;
        int height = 0;
        int lineheight = fm.getHeight();
        int rows = getNumLines();
        height = rows * lineheight;
        for ( String row : textLines ) {
            int w = fm.stringWidth( row );
            if ( w > width )
                width = w;
        }
        return new Dimension( width, height );
    }
    
    public ArrayList<String> getLines() {
        return textLines;
    }
    
    /**
     * Breaks up the string   by breaking words at the occurrence of any member of set of break characters.
     * @param string the string to wrap
     * @param breakchars the characters from which to determine a break characters
     * @param maxChars maximum number of characters per line when breaking. If -1 then don't break.
     * @return the list of lines after wrapping has been applied. If no wrapping, consists of a single element.
     */
    public ArrayList<String> stringBreak( String string, String breakchars, int maxChars ) {
        ArrayList<String> subLines = new ArrayList<String>();
        subLines.clear();
        if ( maxChars == -1 ) {
            subLines.add( string );
            return subLines;
        }
        int currChar = (maxChars > string.length()) ? string.length() : maxChars ;
        int startChar = 0;
                // while not at the string end
        while ( currChar < string.length() ) {
                // if the current char is not one of the break chars and we're not back at start, keep going back
            while ( currChar > startChar && ! breakchars.contains( string.substring( currChar, currChar + 1 )) )
                currChar--;
                    // if we're back at the start, then no break char found, split it right there.
            if ( currChar == startChar ) {
                subLines.add( string.substring( startChar, startChar + maxChars) );
                startChar += maxChars;
            } else {    // if not at start, break at break char
                subLines.add( string.substring(  startChar, currChar + 1) );
                startChar = currChar + 1;
            }
            currChar = startChar + maxChars;
        }
        subLines.add( string.substring( startChar ) );
        return subLines;
    }


}
