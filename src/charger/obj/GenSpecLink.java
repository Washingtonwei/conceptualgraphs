package charger.obj;

import charger.*;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import charger.*;
import kb.KnowledgeBase;

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
 * Implements an arrow from a subtype label to a supertype label. Note there is
 * no such construct in graphical form in the current conceptual graph format.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class GenSpecLink extends GEdge {

    public GenSpecLink() {
        super();
        initializeArrowHead();

    }

    public GenSpecLink( GraphObject FromOne, GraphObject ToOne ) {
        super( FromOne, ToOne );
                initializeArrowHead();
        commitToKnowledgeBase( Global.sessionKB );
    }

    /**
     * Draws a coref link on the given graphics context. Draws the dashed edge
     * and selection handle.
     *
     * @param g graphics context on which to draw
     * @param printing if true, then inhibit drawing the selection handle.
     */
    public void draw( Graphics2D g, boolean printing ) {

        //calcDimensions(  );
        g.setColor( foreColor );
        if ( !printing && Global.showGEdgeDisplayRect ) {
            g.fill( displayRect );
        }

                Shape line = new Line2D.Double( fromPt, toPt );
        g.setStroke( new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ) );
        g.draw( g.getStroke().createStrokedShape( line ) );
        g.setStroke( new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness() ) );

//        g.draw( new Line2D.Double( fromPt.x, fromPt.y, toPt.x, toPt.y ) );
        drawArrowHead( g, true, printing );
    }
    
    public void abandonObject() {
        super.abandonObject();
    }

    public boolean commitToKnowledgeBase( KnowledgeBase kb ) {
        // retooled to work with a Charger knowledge base
        GNode subObj = (GNode)this.fromObj;
        GNode superObj = (GNode)this.toObj;
        if ( kb == null ) {
            return false;
        }
        if ( subObj instanceof TypeLabel ) // concept type subsumption; both ends already found compatible
        {
            kb.getConceptTypeHierarchy().addSubtypeToType( subObj, superObj );
//                    Global.info( kb.getConceptTypeHierarchy().showHierarchy( ) );
        } else if ( subObj instanceof RelationLabel ) // relation type subsumption
        {
            kb.getRelationTypeHierarchy().addSubtypeToType( subObj, superObj );
//                    Global.info( kb.getRelationTypeHierarchy().showHierarchy() );

        }
            Global.info( "GenSpecLink added "  + kb.showConceptTypeHierarchy() );
        return true;

    }

    public boolean unCommitFromKnowledgeBase( KnowledgeBase kb ) {
        // retooled to work with a Charger knowledge base
        GraphObject subObj = this.fromObj;
        GraphObject superObj = this.toObj;
        if ( kb == null ) {
            return false;
        }
        if ( subObj instanceof TypeLabel ) // concept type subsumption; both ends have to be compatible
        {
            kb.getConceptTypeHierarchy().removeSuperTypeFromType( superObj, subObj );
//                    Global.info( kb.getConceptTypeHierarchy().showHierarchy( ) );
        } else if ( subObj instanceof RelationLabel ) // relation type subsumption
        {
            kb.getRelationTypeHierarchy().removeSuperTypeFromType( superObj, subObj );
//                    Global.info( kb.getRelationTypeHierarchy().showHierarchy() );

        }
            Global.info( "GenSpecLink removed "  + kb.showConceptTypeHierarchy() );
        return true;

    }



    /**
     * @param kb The knowledge base in which these objects are recorded.
     */
    public void deleteGenSpecLinkFromKB( kb.KnowledgeBase kb ) {
        // retooled to work with a Charger knowledge base
        GraphObject subObj = this.fromObj;
        GraphObject superObj = this.toObj;
        if ( kb == null ) {
            return;
        }
        if ( subObj instanceof TypeLabel ) // concept type subsumption; both ends already found compatible
        {
            kb.getConceptTypeHierarchy().removeSuperTypeFromType( superObj, subObj );
//            Global.info( kb.getConceptTypeHierarchy().showHierarchy( ) );
        } else if ( subObj instanceof RelationLabel ) // relation type subsumption
        {
            kb.getRelationTypeHierarchy().removeSuperTypeFromType( superObj, subObj );
//            Global.info( kb.getRelationTypeHierarchy().showHierarchy() );

        }
    }
}
