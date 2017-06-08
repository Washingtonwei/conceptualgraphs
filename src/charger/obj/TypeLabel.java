package charger.obj;

import charger.*;
import charger.util.*;
import kb.KnowledgeBase;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import kb.hierarchy.TypeHierarchy;
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
 * Implements the type construct in a hierarchy.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach
 */
public class TypeLabel extends GNode {

    /**
     * Instantiates a default type "T".
     */
    public TypeLabel() {
        constructor( "T" );
    }

    /**
     * Instantiates a type label with the given label.
     *
     * @param label the type label's string value
     */
    public TypeLabel( String label ) {
        constructor( label );
    }

    private void constructor( String label ) {
        setTextLabel( label );
        foreColor = (Color)( Global.userForeground.get( CGUtil.shortClassName( this ) ) );
        backColor = (Color)( Global.userBackground.get( CGUtil.shortClassName( this ) ) );
        displayRect.height = (int)( displayRect.height * 0.60 );   // *= caused an incorrect cast
    }

    /**
     * @see GNode#getShape
     */
    public Shape getShape() {
        return Util.make2DDouble( displayRect );
    }

    public void draw( Graphics2D g, boolean printing ) {
        if ( Global.showShadows ) {
            g.setColor( Global.shadowColor );
            g.translate( Global.shadowOffset.x, Global.shadowOffset.y );
            g.fill( getShape() );
            g.translate( -1 * Global.shadowOffset.x, -1 * Global.shadowOffset.y );
        }
        g.setColor( backColor );
        g.fill( getShape() );

        g.setColor( foreColor );
        g.draw( new Line2D.Double( getUpperLeft().x, getUpperLeft().y + getDim().height - 3,
                getUpperLeft().x + getDim().width, getUpperLeft().y + getDim().height - 3 ) );

//		g.drawString( textLabel, textLabelLowerLeftPt.x, textLabelLowerLeftPt.y );
        WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
        text.setEnableWrapping( getWrapLabels() );
        text.drawWrappedText( g, getCenter(), getLabelFont() );

        Stroke s = g.getStroke();
        g.setStroke(
                new BasicStroke( 1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dashpattern, 1.0f ) );
        g.draw( getShape() );
        g.setStroke( s );

        super.draw( g, printing );
    }

    /**
     * Sets the text of this type label. If there's a ":", then it and
     * subsequent characters are ignored.
     */
    public void setTextLabel( String label ) {
        if ( label == null ) {
            label = "";
        }
        // 09-22-2005 by hsd for R0019
        if ( label.contains( ":" ) ) {
            label = label.substring( 0, label.indexOf( ":" ) );
        }
        super.setTextLabel( Util.makeLegalChars( label ) );
    }

    /**
     * Perform whatever activities are required for this type label to be committed
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
     * Perform whatever activities are required for this concept to be removed
     * from a knowledge base. Removes the type label to the type hierarchy.
     *
     * @param kb
     */
    public boolean unCommitFromKnowledgeBase( KnowledgeBase kb ) {
        // TODO: needs to check if this label is used by any other concepts and if so, don't do anything more to the hierarchy.
        boolean b = kb.getConceptTypeHierarchy().removeTypeLabel( this.getTypeLabel() );
//            Global.info( "Concept \"" + this.getTypeLabel() + "\" removed = " + b + " " + kb.showConceptTypeHierarchy() );
        return b;

    }
    /**
     * Allocate and initialize a notio concept type that is consistent with the
     * CharGer concept type. Assumes that the CharGer node is correct.
     *
     * @param kb Notio knowledge base, to be used for storing the concept type.
     */
    /* REMOVE-NOTIO public void updateForNotio(  notio.KnowledgeBase kb )
     {
     try {
     notio.ConceptType t = null;
     if ( textLabel.equalsIgnoreCase( "T" ) )
     t = kb.getConceptTypeHierarchy().getTypeByLabel( notio.ConceptTypeHierarchy.UNIVERSAL_TYPE_LABEL );
     else if ( textLabel.equalsIgnoreCase( "null" ) )
     t = kb.getConceptTypeHierarchy().getTypeByLabel( notio.ConceptTypeHierarchy.ABSURD_TYPE_LABEL );
     else
     t = kb.getConceptTypeHierarchy().getTypeByLabel( textLabel );
     // @bug doesn't check whether type's old type label should be deleted  (see R0014)
     if ( t == null )
     {
     nxConceptType = new notio.ConceptType( textLabel );
     kb.getConceptTypeHierarchy().addTypeToHierarchy( nxConceptType );
     }
     else
     {
     nxConceptType = t;	// linking this type node to an already-existing one! dangerous!!
     }
     nxConceptType.setComment( makeCGIFcomment() );
				
     } catch (notio.TypeChangeError tce) 
     { 		}
     }*/
}
