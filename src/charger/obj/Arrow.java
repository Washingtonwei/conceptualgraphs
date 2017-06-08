package charger.obj;

import charger.*;
import charger.util.*;

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
 * Implements the link arrow which connects a concept to a relation or actor.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class Arrow extends GEdge {

    /**
     * Instantiates a link arrow from nowhere to nowhere.
     */
    public Arrow() {
        super();
        initializeArrowHead();
    }

    /**
     * Instantiates a link arrow from one graph object to another.
     *
     * @param FromOne starting object for the link arrow
     * @param ToOne ending object for the link arrow
     */
    public Arrow( GraphObject FromOne, GraphObject ToOne ) {
        super( FromOne, ToOne );
        initializeArrowHead();

        //if ( Hub.NotioEnabledInParallel ) MakeArrowForNotio( FromOne, ToOne, getTextLabel() );
        //makeArrowPoints( fromPt, toPt, 0.55 ); 
    }

    /**
     * Draws the link arrow on its graphic context. If the link arrow has a text
     * label other than the default "-", it is also drawn.
     *
     * @param g graphics context on which to draw the link arrow
     * @param printing if true, inhibits drawing the selection tick on the link
     * arrow
     */
    public void draw( Graphics2D g, boolean printing ) {
        
        g.setColor( foreColor );
        Shape line = new Line2D.Double( fromPt, toPt );
        g.setStroke( new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ) );
        g.draw( g.getStroke().createStrokedShape( line ) );
        g.setStroke( new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness() ) );

        if ( !printing && Global.showGEdgeDisplayRect ) {
            g.fill( displayRect );
        }
        g.setColor( foreColor );
        if ( !textLabel.equals( "-" ) ) /*if ( Hub.showGEdgeDisplayRect )*/ {
//            g.drawString( textLabel, displayRect.x, displayRect.y );
            WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
            text.setEnableWrapping( getWrapLabels() );
            text.drawWrappedText( g, getCenter(), getLabelFont() );
        }

        drawArrowHead( g, true, printing );
        g.setStroke( Global.defaultStroke );
    }

    /**
     * Allocate and initialize a notio node that is consistent with the CharGer
     * node. Assumes that the CharGer node is correct.
     *
     * @param kb Notio knowledge base, to be used for consistency checking. Not
     * currently used.
     */
    /*public void updateForNotio( notio.KnowledgeBase kb ) {
        MakeArrowForNotio( this );
    }
    * */

    /**
     * Constructs the parts of a notio relation needed to represent the CharGer
     * link arrow.
     *
     * @param a a link between two objects. Must be non-null.
     */
    /*public static void MakeArrowForNotio( Arrow a ) {
        MakeArrowForNotio( a.fromObj, a.toObj, a.getTextLabel() );
    }
    * */

    /**
     * Constructs the parts of a notio relation needed to link a relation with a
     * concept/context.
     *
     * @param FromOne starting object for the link arrow
     * @param ToOne ending object for the link arrow
     * @param label the link arrow's label (e.g, "1" "2" or "-")
     */
    /*public static void MakeArrowForNotio( GraphObject FromOne, GraphObject ToOne, String label ) {
        Integer sequenceNum = null;
        try {
            sequenceNum = Integer.valueOf( label );
        } catch ( NumberFormatException e ) {
            sequenceNum = null;
        }

        if ( Hub.ShowBoringDebugInfo ) {
            if ( FromOne == null || ToOne == null ) {
                Hub.error( "one or the other ends of a link arrow was null." );
            } else {
                Global.info( "Make Notio link arrow: From is class " + CGUtil.shortClassName( FromOne )
                        + ", To is class " + CGUtil.shortClassName( ToOne ) );
            }
        }
        notio.Concept c = null;
        notio.Relation r = null;
        notio.Actor a = null;

        if ( ( FromOne instanceof Concept ) || ( FromOne instanceof Graph ) ) {
            if ( FromOne instanceof Graph ) {
                c = ( (Graph)FromOne ).nxConcept;
            } else {
                c = ( (Concept)FromOne ).nxConcept;
            }
            if ( ToOne instanceof Relation ) {
                r = ( (Relation)ToOne ).nxRelation;
                // ToOne must be a relation or an actor
                // REMOVE-NOTIO Ops.addArgument( r, c, true, sequenceNum );
                //r.setInputArgument( r.getValence(), c );
            } else if ( ToOne instanceof Actor ) {
                a = ( (Actor)ToOne ).nxActor;
                //a.setInputArgument( a.getValence(), c );	
                // REMOVE-NOTIO Ops.addArgument( a, c, true, sequenceNum );
            } else {
                Hub.error( "link arrow from concept to " + CGUtil.shortClassName( FromOne ) );
            }
        } else if ( ( ToOne instanceof Concept ) || ( ToOne instanceof Graph ) ) {
            if ( ToOne instanceof Graph ) {
                c = ( (Graph)ToOne ).nxConcept;
            } else {
                c = ( (Concept)ToOne ).nxConcept;
            }
            if ( FromOne instanceof Relation ) {
                r = ( (Relation)FromOne ).nxRelation;
                // ToOne must be a relation or an actor
                // REMOVE-NOTIO Ops.addArgument( r, c, false, sequenceNum );
                //r.setOutputArgument( r.getValence()+1, c );
            } else if ( FromOne instanceof Actor ) {
                a = ( (Actor)FromOne ).nxActor;
                // REMOVE-NOTIO Ops.addArgument( a, c, false, sequenceNum );				
                //a.setOutputArgument( a.getValence()+1, c );			
            } else {
                Hub.error( "link arrow to concept from " + CGUtil.shortClassName( FromOne ) );
            }
        } else {
            a = a;// is a context....
        }			//Hub.error( "link arrow does not connect a concept to anything.");

    }
    * */

    /**
     * Logically removes this given edge in the Notio graph by un-attaching a
     * concept or graph from the relation or actor to which it was connected.
     */
   /* public void BreakArrowForNotio() {
        GraphObject FromOne, ToOne;
        FromOne = fromObj;
        ToOne = toObj;
        notio.Concept c = null;
        notio.Relation r = null;
        notio.Actor a = null;
        if ( ( FromOne instanceof Concept ) || ( FromOne instanceof Graph ) ) {
            if ( FromOne instanceof Graph ) {
                c = ( (Graph)FromOne ).nxConcept;
            } else {
                c = ( (Concept)FromOne ).nxConcept;
            }
            if ( ToOne instanceof Relation ) {
                r = ( (Relation)ToOne ).nxRelation;
                if ( !r.relatesConcept( c ) ) {
                    Hub.error( "'From' concept " + FromOne.getTextLabel() + " in GEdge not in notio Relation " + ToOne.getTextLabel() );
                }
                // ToOne must be a relation or an actor
                // REMOVE-NOTIO else Ops.deleteArgument( r, c, true );
            } else if ( ToOne instanceof Actor ) {
                a = ( (Actor)ToOne ).nxActor;
                if ( !a.relatesConcept( c ) ) {
                    Hub.error( "'From' concept " + FromOne.getTextLabel() + " in GEdge not in notio Actor " + ToOne.getTextLabel() );
                }
                // REMOVE-NOTIO else Ops.deleteArgument( a, c, true );
            } else {
                Hub.error( "link arrow from concept to something other than a relation or actor." );
            }
        } else if ( ( ToOne instanceof Concept ) || ( ToOne instanceof Graph ) ) {
            if ( ToOne instanceof Graph ) {
                c = ( (Graph)ToOne ).nxConcept;
            } else {
                c = ( (Concept)ToOne ).nxConcept;
            }
            if ( FromOne instanceof Relation ) {
                r = ( (Relation)FromOne ).nxRelation;
                // ToOne must be a relation or an actor
                if ( !r.relatesConcept( c ) ) {
                    Hub.error( "'To' concept " + ToOne.getTextLabel() + " in GEdge not in notio Relation " + FromOne.getTextLabel() );
                }
                // REMOVE-NOTIO else Ops.deleteArgument( r, c, false );
            } else if ( FromOne instanceof Actor ) {
                a = ( (Actor)FromOne ).nxActor;
                if ( !a.relatesConcept( c ) ) {
                    Hub.error( "'To' concept " + ToOne.getTextLabel() + " in GEdge not in notio Actor " + FromOne.getTextLabel() );
                }
                // REMOVE-NOTIO else Ops.deleteArgument( a, c, false );
            } else {
                Hub.error( "link arrow to concept from something other than a relation or actor." );
            }
        }
    }
    * */
}
