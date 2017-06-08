//
//  RGDiadicElicitor.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 30 2003.
//
package repgrid;

import repgrid.tracks.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

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
	Manages diadic elicitation on the given grid.
 */
public class RGDiadicElicitor extends RGElicitor {

	public RGDiadicElicitor( TrackedRepertoryGrid rg, RGTableModel t ) { super( rg, t ); }
	
	public ArrayList lastElements = null;

	/**
		Perform diadic elicitation on this grid
	 */
	public void elicit()
	{
		refreshHeaderPanel();
		if ( grid.elementLabel == null || grid.relationLabel == null || grid.attributeLabel == null 
				|| grid.valuetype == null  ||
			 grid.elementLabel.equals("") || grid.relationLabel.equals("") || grid.attributeLabel.equals("") )
		{
			transcript.showMessageDialog( null, 
				"Please fill in concepts 1 and 2 with a relation\n to indicate what you want to enter." );
		}
		else
		{
			boolean okay = true;
			while ( okay )
			{
				okay = nameTwoElements();
				if ( okay ) okay = nameTwoAttributes();
				if ( okay ) okay = fillInGridByQuery();
			}
		}
	}
	
	/**
		Queries the interactive user for two elements of a grid.
		@see JOptionPane#showInputDialog
	 */
	public boolean nameTwoElements()
	{
		String t2 = null;
		String t1 = queryNewElement( "" );
		if ( t1 == null ) return false;

		// grid.add( t1 ); 
		lastElements = new ArrayList();
		lastElements.add( t1 );
		// if ( model != null ) model.fireTableStructureChanged();
		t2 = queryNewElement( "    \nthat is different from " + t1 );
		if ( t2 == null ) return false;

		//grid.add( t2 );
		lastElements.add( t2 );
		craft.Craft.say( "after nameTwoElements" );
		//if ( window != null ) window.setChanged( true );
		//if ( model != null ) model.fireTableStructureChanged();

		return true;		
	}
	
	/**
		Given 
	 */
	public boolean nameTwoAttributes()
	{
		String t2 = null;
		String t1 = transcript.showInputDialog( null, "Name " + charger.util.Util.a_or_an( grid.attributeLabel ) +
			" that " + lastElements.get( 0 ) + " and " +
			lastElements.get( 1 ) + " both " + craft.Reporter.tmap.cardinalize( grid.relationLabel, 1 ) );
						// cheating here! plural of verb is actually the singular noun rule
		if ( t1 == null ) return false;
		else
		{
			grid.addAttribute( t1 );
				// no need to notify tablemodel, since attributes are in their own separate list.
			//if ( model != null ) model.fireTableStructureChanged();
			t2 = transcript.showInputDialog( null, "Name " + charger.util.Util.a_or_an( grid.attributeLabel ) +
			" where " + lastElements.get( 0 ) + " and " + lastElements.get( 1 ) + " differ   " );

			if ( t1 == null ) return false;
			else
			{
				grid.addAttribute( t2 );
						// actually set the cells
			if ( model != null ) model.fireTableStructureChanged();
			}
		}
		return true;		
	
	}
}
