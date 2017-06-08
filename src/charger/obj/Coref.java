package charger.obj;

import charger.*;
import charger.exception.*;


import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import charger.*;

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
 * Implements a coreferent link.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class Coref extends GEdge {

    private  float dashpattern[] = { 3.0f, 6.0f };

    /**
     * Create a coref connecting nothing
     */
    public Coref() {
        super();
        setColor();
    }

    /**
     * Create a coref edge between two objects. Order doesn't matter.
     *
     * @param FromOne one of the objects
     * @param ToOne the other object
     */
    public Coref( GraphObject FromOne, GraphObject ToOne ) {
        super( FromOne, ToOne );
        setColor();
    }

    /**
     * Over-ridden for compatibility with setting to defaults.
     *
     * @param whocares text and fill are the same for a Coref
     * @param c if not null, then we're trying to do something we shouldn't
     */
    /*	public void setColor( String whocares, Color c )
     {
     //if ( c != null ) return; 		// don't try to set the color of a Coref yet
     setColor();
     }
     */
    /**
     * Draws a coref link on the given graphics context. Draws the dashed edge
     * and selection handle.
     *
     * @param g graphics context on which to draw
     * @param printing if true, then inhibit drawing the selection handle.
     */
    public void draw( Graphics2D g, boolean printing ) {
        boolean bold = true;
        g.setColor( foreColor );
        dashpattern = new float[] { (float)Global.userEdgeAttributes.getEdgeThickness()* 3f, (float)Global.userEdgeAttributes.getEdgeThickness() * 3f };
        g.setStroke(
                new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness() * 1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 100.0f, dashpattern, 1.0f ) );
        g.draw( new Line2D.Double( (double)fromPt.x, (double)fromPt.y, (double)toPt.x, (double)toPt.y ) );
        //g.drawLine( fromPt.x, fromPt.y, toPt.x, toPt.y );
        //CGUtil.drawDashedLine( g, fromPt, toPt, 3, 4, bold );
        g.setColor( backColor );
        if ( !printing && Global.showGEdgeDisplayRect ) {
            g.fill( displayRect );
        }
        g.setStroke( Global.defaultStroke );
    }

  	
    /**
     * Create a CharGer Coref link from the given CharGer concept. To prevent
     * duplicative (and possibly scope-violating) links, a concept only gets to
     * create a link from itself to its dominant concept. Each coreference set
     * is treated separately; i.e., a member might be a dominant concept in one
     * set, but a subordinate concept in another.
     *
     * @param corefMember a potential member of a co-referent set; if not a
     * member, nothing happens
     */
    public static void updateForCharGer( charger.obj.Concept corefMember ) {
        //notio.Concept counterpart = corefMember.nxConcept;

        charger.obj.Graph outermost = corefMember.getOutermostGraph();

    }

    public static void updateAllCorefsForCharger( Graph g ) {
        Iterator iter = new DeepIterator( g, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            GNode gn = (GNode)iter.next();
            if ( gn instanceof Concept ) {
                updateForCharGer( (Concept)gn );
            }
        }
        iter = new DeepIterator( g, GraphObject.Kind.GRAPH );
        while ( iter.hasNext() ) {
            Graph cg = (Graph)iter.next();
            //if ( gn instanceof Concept ) 
            updateForCharGer( (Concept)cg );
        }
    }
}
//Some algorithms we might need sometime:
// end result is that the dom has to be dominant to sub
		/* 
 IF dom is already in a set THEN 
 IF it's dominant in that set
 just add sub to the set (sub can't already be there, we checked above)
 ELSE it's not dominant in that set
 create a new coreference set for these two (ok since dom doesn't dominate annother set)
 END IF
 ELSE dom is not in a set
 IF sub is in a set THEN add 
 */
/*	YUK! double YUK!
 Much simpler algorithm: Assume that a concept can only be a member of one coreferent set. 
 IF dom is already in a set S THEN
 IF sub is not dominant in S THEN
 add sub to S
 ELSE sub is dominant in a set
 dom now dominates all of them
 demote sub from being dominant in the set
 add 
 END IF
 */
/*
 dom's set is D
 sub's set is S
 IF S not equal D THEN 
 it's illegal, so reject it
 IF S is null THEN
 add sub to D
 ELSE IF D is null THEN
 IF sub dominates S THEN
 remove sub from S
 add dom to S
 make dom dominate S
 add sub to S
 ELSE S must equal D  -- they're both already there
 make dom dominant in S
 END IF
 END IF
 */
