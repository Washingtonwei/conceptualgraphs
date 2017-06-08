package charger.obj;

import charger.*;
import charger.util.*;
import charger.act.*;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
//import notio.*;

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
	Implements the actor construct.
	@author Harry S. Delugach ( delugach@uah.edu ) Copyright reserved 1998-2014 by Harry S. Delugach
 */
 public class Actor extends GNode 
 {

	    /** The Notio counterpart to this actor */
	//REMOVE-NOTIO public notio.Actor nxActor = new notio.Actor();
	    /** The Notio counterpart to the relation type label for this actor */
	//REMOVE-NOTIO public notio.RelationType nxRelationType;
	
		/** expansion percentage vertically for drawing diamond; e.g., 0.1 means expand by 10% */
	protected static final float verticalStretch = 0.1f;
		// expansion percentage horizontally for drawing diamond
	protected static final float horizontalStretch = 0.3f;
        
        protected static final float maxWidthToHeightRatio = 4.0f;
        

	/**
		Instantiates a default actor with default label "f".
	 */	
	public Actor() 
	{ 
		setTextLabel("f" );
		foreColor = (Color)(Global.userForeground.get( CGUtil.shortClassName( this ) ) );
		backColor = (Color)(Global.userBackground.get( CGUtil.shortClassName( this ) ) );
	}

	/*
	/**
		Re-sets the center point; in case there's any string adjusting to do.
	public void setCenter() {
		this.setCenter( this.getCenter() );
	}
	*/

	/**
		Set the center point of this actor to the given point.
	 */
   public void setCenter( Point2D.Double p) {
		super.setCenter( p );
	}
        
    /**
        Overrides super class's method so that we can adjust the display rect to account for odd shape
     */
    public void setDisplayRect( Rectangle2D.Double r )
    {
        super.setDisplayRect( r );
                   // Global.info( "Actor's set display rect: super is set to " + displayRect.toString() );
        //adjustActorDisplayRect();
        //            Global.info( "Actor's set display rect: actor itself is set to " + displayRect.toString() );

    }


	/**
            Actor's shape is a diamond that is inscribed within its displayRect.
		@see GNode#getShape
	 */
	public Shape getShape()
	{
		//Rectangle2D.Double r = Util.make2DDouble( displayRect );
            Rectangle2D.Double r = Util.make2DDouble( displayRect );
		//r.grow( 10, 10 );	// 05-27-03 but was deprecated in JDK 1.5
               // r.add( r.x + r.width + growIncrement/2, r.y + r.height + growIncrement/2 );
               // r.add( r.x - growIncrement/2, r.y - growIncrement/2 );
	    GeneralPath p2d = new GeneralPath();
		p2d.moveTo( r.x + r.width / 2.0f, r.y ); // move to top vertex
	    p2d.lineTo( r.x + r.width, r.y + r.height / 2.0f );	// line to right vertex
	    p2d.lineTo( r.x + r.width / 2.0f, r.y + r.height );	// line to bottom vertex
	    p2d.lineTo( r.x, r.y + r.height / 2.0f ); 	// line to left vertex
	    p2d.lineTo( r.x + r.width / 2.0f, r.y );	// line back to top vertex
		return p2d;
        }

	
	/**
			The basic rendering method for this actor.
			@param g graphics context on which to perform the rendering
			@param printing whether we're in "printing" mode. Has no effect here, but is passed to super.draw()
				*/
   public synchronized void draw( Graphics2D g, boolean printing ) {

                           // g.setColor( Color.yellow );
                           // g.draw( displayRect );
		
		if ( Global.showShadows )
		{
			g.setColor( Global.shadowColor );
			g.translate( Global.shadowOffset.x, Global.shadowOffset.y );
			g.fill( getShape() );
			g.translate( -1 * Global.shadowOffset.x, -1 * Global.shadowOffset.y );
		}

		Color temp = getColor( "text" );
		if ( isActive() )
		{		
			foreColor = getColor( "fill" );
			backColor = temp;
		}
		
		
		g.setColor( backColor );
       //p.translate( -1 * Hub.shadowOffset.x, -1 * Hub.shadowOffset.y );  	
       g.fill( getShape() );
       g.setColor( Color.black );
       g.draw( getShape() );


       g.setColor( foreColor );


//		g.drawString( textLabel, textLabelLowerLeftPt.x, textLabelLowerLeftPt.y );
       WrappedText text = new WrappedText( textLabel, GraphObject.defaultWrapColumns );
       text.setEnableWrapping( getWrapLabels() );
       text.drawWrappedText( g, getCenter(), getLabelFont() );

       if ( isActive() ) {
           setColor( "fill", getColor( "text" ) );
           setColor( "text", temp );
		}



		super.draw( g, printing );
  	}
    
    /**
	Draws the actor border, in a given color.
	@param g The graphics context on which to draw
	@param borderColor the color of the wide border.
	Uses the actor's already-set position and size to decide where to draw
    */
    public void drawBorder( Graphics2D g, Color borderColor )
    {
        Color saveColor = g.getColor();
        
        g.setColor( borderColor );
        g.setStroke( new BasicStroke( 2.0f ) );
        g.draw( getShape() );

        g.setColor( saveColor );
        g.setStroke( Global.defaultStroke );
    }
	
	/** Sets the label of this actor, editing characters if any are illegal. 
		@param label The new label for this actor. 
	 */
   public void setTextLabel( String label ) 
   {
        if ( label == null ) label = "";
        super.setTextLabel( Util.makeLegalChars( label ) );
        //adjustActorDisplayRect();
   }
   
   public void adjustCustomDisplayRect()
   {
                Global.info( "actor custom display rect - before is " + displayRect.toString() );
        double xinc = displayRect.width * horizontalStretch;
        double yinc = displayRect.height * verticalStretch;
        double x = displayRect.x - xinc / 2f;
        double y = displayRect.y - yinc / 2f;
        double width = displayRect.width + xinc;
        double height = displayRect.height + yinc;
        if ( (width / height) > maxWidthToHeightRatio ) 
            height = width / maxWidthToHeightRatio;
        displayRect = new Rectangle2D.Double( x, y, width, height );
        setCenter();        // force edges to be redrawn
                Global.info( "actor custom display rect - after is " + displayRect.toString() );
   }

  	/**
  		Returns the CGIF string corresponding to this actor, including input,output arcs.
  	 */
/*  	public String CGIFString()
	{
		return "<" + textLabel + CGIFStringGNodeEdges() + Hub.metaWrap( toString() ) + " >";
	}
*/

	/**
		Allocate and initialize a notio node that is consistent with the CharGer node.
		Assumes that the CharGer node is correct.
		@param kb Notio knowledge base, to be used for consistency checking.
	*/
	/* REMOVE-NOTIO public void updateForNotio( notio.KnowledgeBase kb )
	{
		nxActor = new notio.Actor();
		ownerGraph.putCharGerCounterpart( nxActor, this );
		nxRelationType = kb.getRelationTypeHierarchy().getTypeByLabel( textLabel ); 
		if ( nxRelationType == null )
		{
			nxRelationType = new notio.RelationType( textLabel );
			kb.getRelationTypeHierarchy().addTypeToHierarchy( nxRelationType );
		}
		nxActor.setType( nxRelationType );
		
		if ( ownerGraph != null && ownerGraph.nxGraph != null )
		{
			ownerGraph.nxGraph.addRelation( nxActor );
		}
		nxActor.setComment( makeCGIFcomment() );	
	}


	/**
		Fill in a CharGer relation from its Notio counterpart; assumes Notio counterpart is correct.
	 */
	/* REMOVE-NOTIO public void updateForCharGer( )
	{
		setTextLabel( nxActor.getType().getLabel() );
			// To be done: create all the links to this actor!!!
		ownerGraph.putCharGerCounterpart( nxActor, this );
	}
        * */
	
	/**
		Performs whatever dusting and cleaning is needed when the actor goes away.
		The most important cleanup is to invoke the "stopActor" method on this actor's plugin.
	 */
	public void selfCleanup()
	{
		if ( GraphUpdater.hasAttribute( this.getTextLabel(), "executable" ) )
		{
			ActorPlugin ap = (ActorPlugin)(GraphUpdater.pluginInstances.get( this ));
			if ( ap != null )
			{
						Global.info( "stopping actor" );
				ap.stopActor();
				GraphUpdater.pluginInstances.remove( this );
			}
		}
		super.selfCleanup();
	}

}
