package charger.obj;

import charger.*;
import charger.util.*;
import kb.KnowledgeBase;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import kb.hierarchy.TypeHierarchyNode;

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
 * Implements the concept construct.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class Concept extends GNode {

    public static int TEXT_WIDTH = 200;
    public static int TEXT_HEIGHT = 50;
    /**
     * The concept's type label.
     */
    //String typeLabel = null;
    /**
     * The concepts' referent (possibly null)
     */
    Referent referent = new Referent();
    /**
     * Whether the type and referent are current
     */
    boolean refDone = false;

////    JTextArea nodeLabel = new JTextArea();
//    JTextArea nodeLabel = new JTextArea();
    /**
     * Instantiates a default concept with label "T".
     */
    public Concept() {
        setTextLabel( "T" );
        setColor();
//        nodeLabel.setWrapStyleWord( true );
//        nodeLabel.setLineWrap( true );
//        nodeLabel.setPreferredSize( new Dimension( TEXT_WIDTH, TEXT_HEIGHT));
//        nodeLabel.setVisible( true );
    }

    // TODO: Need to remove concept from any coreference sets to which it belongs.
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
     * @see GNode#getShape
     */
    public Shape getShape() {
        return Util.make2DDouble( displayRect );
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

    /*protected void drawHighlighted( Graphics2D g, Color highlightColor )
     {
     Color save = g.getColor();
     g.setColor( highlightColor );
     g.drawString( textLabel, textLabelLowerLeftPt.x, textLabelLowerLeftPt.y );
     g.setColor( save );  
     }*/
    /**
     * Breaks up text label into its constituents, removes leading and trailing
     * spaces, and replaces all embedded spaces with underscore '_' characters.
     * Bears no relationship to the notio referent.
     */
    public void parseTextLabel() {
        if ( textLabel == null || textLabel.equals( "" ) ) {
            textLabel = "";
            typeLabel = "";
//            referent = "";
            referent.setReferentString( "");
            return;
        }
        StringTokenizer s = new StringTokenizer( textLabel, ":" );
        typeLabel = Util.makeLegalChars( s.nextToken() );

        // collect the rest; if there's a colon ":" in the label, be sure to insert it.
        StringBuilder r = new StringBuilder( "" );
        while ( s.hasMoreTokens() ) {
            r = r.append( s.nextToken().trim() );
            if ( s.countTokens() > 0 ) {
                r.append( ":" );
            }
        }
//        referent = r.toString();
        referent.setReferentString( r.toString());
        refDone = true;
    }

    /**
     * Get just the type label portion of the concept's text label.
     *
     * @return a type label
     */
    public String getTypeLabel() {
        parseTextLabel();
        return typeLabel;
    }

    /**
     * Define a complete text label for the concept; type and referent are
     * extracted.
     *
     * @param label The new text label for the concept
     */
    public void setTextLabel( String label ) {
        if ( label == null ) {
            label = "";
        }
        super.setTextLabel( label );

        typeLabel = textLabel;		// some sub-classes may want to override this
        parseTextLabel();
    }

    public void setTypeLabel( String newlabel ) {
        boolean resize = false;
        if ( getOwnerGraph() != null && getOwnerGraph().getOwnerFrame() != null ) {
            resize = true;
        }
        setTypeLabel( newlabel, resize );
    }

    /**
     * Define the type label for the concept. Referent is unaffected.
     *
     * @param newlabel type label only.
     * @param resize whether to bother with adjusting the concept's size or not.
     */
    public void setTypeLabel( String newlabel, boolean resize ) {
        super.setTypeLabel( newlabel, resize );
        //	parseTextLabel();
        //typeLabel = new String( Util.makeLegalChars( newlabel ) );
        if ( referent.getReferentString() == null || referent.getReferentString().equals( "" ) ) {
            textLabel = typeLabel;
        } else {
            textLabel = typeLabel + ": " + referent.getReferentString();
        }
        //setTextLabel( textLabel );
        if ( resize ) {
            resizeIfNecessary();
        }
    }
    
    public void setReferent( String newref ) {
        setReferent( newref, false );
    }

    /**
     * Sets this concept's referent designator as a string.
     *
     * @param newref the string for the new referent
     * @param changing whether to look for changes in a referent;      * if <code>true</code> then try resizing concept, etc. Need to
     * indicate whether the referent is changing; otherwise every setting of a
     * referent (including initial value setting) will force resizing, which may
     * not make sense if the concept doesn't even have a size yet.
     */
    public void setReferent( String newref, boolean changing ) {
        //	parseTextLabel();
        String oldLabel = textLabel;
//        referent = newref;
        referent.setReferentString( newref );
        if ( !referent.getReferentString().equals( "" ) ) {
            textLabel = typeLabel + ": " + referent.getReferentString();
        } else {
            textLabel = typeLabel;
        }
        if ( changing && ( !oldLabel.equals( textLabel ) ) ) {
            resizeIfNecessary();
            //if ( getOwnerFrame() != null ) 
            //	getOwnerFrame().emgr.setChangedContent( true );
        }

        //if ( ! ( this instanceof Graph ) ) 
        //	super.setTextLabel( textLabel );
        // don't like this solution; problem is that on input or pasting sometimes referent is set before real 
        // position is known, but if existing position is some bad default, the ownergraph 
        // is stuck with it.
        //  better solution would be to adjust display rectangles somewhere else..
    }

    /**
     * Finds the referent part of this concept's text label. "parses" the label,
     * setting both the type and referent part.
     *
     * @return Concept's referent designator as a string. If there's no
     * referent, returns empty string (""), not null.
     * @see #parseTextLabel
     */
    public String getReferent() {
        parseTextLabel();
        return referent.getReferentString();
    }

    /**
     * Perform whatever activities are required for this concept to be committed
     * to a knowledge base. Adds the type label to the type hierarchy if it is
     * not already there.
     *
     * @param kb
     */
    public boolean commitToKnowledgeBase( KnowledgeBase kb ) {
        TypeHierarchyNode node = kb.getConceptTypeHierarchy().addTypeLabel( this.getTypeLabel() );
//            Global.info( "Concept \"" + this.getTypeLabel() + "\" added = " + b + " " + kb.showConceptTypeHierarchy() );
        if ( node == null ) {
            return false;
        } else {
            kb.getConceptTypeHierarchy().addToTopAndBottom( node );
            return true;
        }
    }

    /**
     * Perform whatever activities are required for this concept to be committed
     * to a knowledge base. Adds the type label to the type hierarchy.
     *
     * @param kb
     */
    public boolean UnCommitFromKnowledgeBase( KnowledgeBase kb ) {
        // TODO: needs to check if this label is used by any other concepts and if so, don't do anything more to the hierarchy.
        boolean b = kb.getConceptTypeHierarchy().removeTypeLabel( this.getTypeLabel() );
//            Global.info( "Concept \"" + this.getTypeLabel() + "\" removed = " + b + " " + kb.showConceptTypeHierarchy() );
        return b;

    }
}
