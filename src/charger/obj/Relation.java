package charger.obj;

import charger.*;
import charger.util.*;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import kb.KnowledgeBase;
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
 * Implements the conceptual relation construct.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class Relation extends GNode {

    /* REMOVE-NOTIO public notio.Relation nxRelation = new notio.Relation();
     public notio.RelationType nxRelationType = new notio.RelationType();
     * */
    /**
     * Instantiates a default relation with label "link".
     */
    public Relation() {
        setTextLabel( "link" );
        foreColor = (Color)( Global.userForeground.get( CGUtil.shortClassName( this ) ) );
        backColor = (Color)( Global.userBackground.get( CGUtil.shortClassName( this ) ) );
        displayRect.height = (int)( displayRect.height * 0.75 );   // *= caused an incorrect cast
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
     * @see GNode#getShape
     */
    public Shape getShape() {
        /*  return new RoundRectangle2D.Double(
         (float)getPos().x, (float)getPos().y,(float) getDim().width, (float)getDim().height, 
         //  (float)((getDim().width)/2.0f), (float)(getDim().width)/2.0f);
         12.0, 8.0f );
         */
        return new Ellipse2D.Double(
                (float)getUpperLeft().x, (float)getUpperLeft().y, (float)getDim().width, (float)getDim().height );
    }

    /**
     * Draws a relation on the given graphics context. Draws the oval, label,
     * shadows (if any).
     *
     * @param g graphics context on which to draw
     * @param printing (ignored; appears for consistency only)
     */
    public void draw( Graphics2D g, boolean printing ) {
        g.setStroke( new BasicStroke( 1.5f ) );
        if ( Global.showShadows ) {
            // draw shadow
            g.setColor( Global.shadowColor );
            g.translate( Global.shadowOffset.x, Global.shadowOffset.y );
            g.fill( getShape() );
            g.translate( -1 * Global.shadowOffset.x, -1 * Global.shadowOffset.y );
        }

        g.setColor( backColor );
        //g.fillRoundRect( getPos().x, getPos().y, getDim().width, getDim().height, 
        //		getDim().width/2, getDim().width/2 );
        g.fill( getShape() );
        g.setStroke( new BasicStroke( 0.5f ) );

        // draw border - disabled due to confusion with selection
        //g.setColor( Color.black );
        //g.drawRoundRect ( getPos().x, getPos().y, getDim().width, getDim().height,
        //		getDim().width/2, getDim().width/2 );

        g.setColor( foreColor );
//        g.drawString( textLabel, textLabelLowerLeftPt.x, textLabelLowerLeftPt.y );
        WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
        text.setEnableWrapping( getWrapLabels() );
        text.drawWrappedText( g, getCenter(), getLabelFont() );

        super.draw( g, printing );

        g.setStroke( Global.defaultStroke );
    }

    public void drawBorder( Graphics2D g, Color borderColor ) {
        g.setColor( borderColor );
        g.setStroke( new BasicStroke( 2.0f ) );
        g.draw( getShape() );
        //g.draw( new Rectangle2D.Double( 
        //	(float)getPos().x, (float)getPos().y,(float) getDim().width, (float)getDim().height ) );
        g.setStroke( Global.defaultStroke );
    }

    /**
     * Returns the CGIF string corresponding to this relation, including
     * input,output arcs.
     */
    /*   public String CGIFString()
     {
     return"(" + textLabel + CGIFStringGNodeEdges() + Hub.metaWrap( toString() ) + " )";
     }
     */
    public void setTextLabel( String label ) {
        if ( label == null ) {
            label = "";
        }
        super.setTextLabel( Util.makeLegalChars( label ) );
    }

    /**
     * Allocate and initialize a notio node that is consistent with the CharGer
     * node. Assumes that the CharGer node is correct.
     *
     * @param kb Notio knowledge base, to be used for consistency checking.
     */
    /* REMOVE-NOTIO public void updateForNotio(  notio.KnowledgeBase kb )
     {
     //if ( nxRelation == null ) 
     String notioTypeLabel = textLabel;
     if ( textLabel == "null" ) notioTypeLabel = notio.RelationTypeHierarchy.ABSURD_TYPE_LABEL;
     else if ( textLabel == "T" ) notioTypeLabel = notio.RelationTypeHierarchy.UNIVERSAL_TYPE_LABEL;
     notio.RelationType rt = kb.getRelationTypeHierarchy().getTypeByLabel( notioTypeLabel );
     if ( rt == null )
     {
     rt = new notio.RelationType( textLabel );
     kb.getRelationTypeHierarchy().addTypeToHierarchy( rt );
     }
     nxRelation = new notio.Relation();
     ownerGraph.putCharGerCounterpart( nxRelation, this );
     nxRelation.setType( rt );
     //nxRelation.setType( new notio.RelationType( textLabel ) );
		
     if ( ownerGraph != null && ownerGraph.nxGraph != null )
     {
     ownerGraph.nxGraph.addRelation( nxRelation );
     }

     nxRelation.setComment( makeCGIFcomment() );
     }
     * */
    /**
     * Fill in a CharGer relation from its Notio counterpart; assumes Notio
     * counterpart is correct.
     */
    /* REMOVE-NOTIO public void updateForCharGer( )
     {
     setTextLabel( nxRelation.getType().getLabel() );
     ownerGraph.putCharGerCounterpart( nxRelation, this );
     }
     * */
    /**
    /**
     * Perform whatever activities are required for this concept to be committed to a knowledge base.
     * Adds the type label to the type hierarchy if it is not already there.
     * @param kb 
     */
    public boolean commitToKnowledgeBase( KnowledgeBase kb ) {
        TypeHierarchyNode node = kb.getRelationTypeHierarchy().addTypeLabel( this.getTypeLabel() );
//            Global.info( "Relation \"" + this.getTypeLabel() + "\" added = " + b + " " + kb.showRelationTypeHierarchy() );
        if ( node == null ) {
            return false;
        } else {
            kb.getRelationTypeHierarchy().addToTopAndBottom( node );
            return true;
        }
    }

    /**
     * Perform whatever activities are required for this concept to be committed to a knowledge base.
     * Adds the type label to the type hierarchy.
     * @param kb 
     */
    public boolean UnCommitFromKnowledgeBase( KnowledgeBase kb ) {
                // TODO: needs to check if this label is used by any other relations and if so, don't do anything more to the hierarchy.
        boolean b = kb.getRelationTypeHierarchy().removeTypeLabel( this.getTypeLabel() );
//            Global.info( "Relation \"" + this.getTypeLabel() + "\" removed = " + b + " " + kb.showRelationTypeHierarchy() );
        return b;
        
    }
} // class
