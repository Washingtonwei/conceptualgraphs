//
//  RGAttribute.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 23 2003.
//
package repgrid;

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
	Abstraction of the notion of an attribute or construct.
	Realized as a "row" in a repertory grid.
 */
public class RGAttribute 
{
		/** the identifying label for this attribute */
	protected String label = null;
		
		/** all of the cells that are associated with this attribute.
			Can be considered the cells in this attribute's "row" */
	protected ArrayList associatedCells = new ArrayList();
	
		/** Make a new attribute with the given string as a label */
	public RGAttribute( String s )
	{
		label = s;
	}
	/**
		Sets the label of this attribute; e.g., "4 legs"
	 */
	public void setLabel( String s )
	{
		label = s;
	}
	
	/** Returns the label of this particular attribute */
	public String getLabel()
	{
		return label;
	}
	
	/** Associate the given cell with this attribute.
		@param c The (already-existing) cell to be added.
	*/
	public void addCell( RGCell c )
	{
		associatedCells.add( c );
	}
	
	public ArrayList getCells()
	{
		return associatedCells;
	}
	
}
