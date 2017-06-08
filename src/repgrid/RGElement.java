//
//  RGElement.java
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
	Represents the element associated with a column in a repertory grid, known as
		an "element" in repertory grid theory.
 */
public class RGElement {

	public ArrayList associatedCells = new ArrayList();

	public RGElement( String s )
	{
		label = s;
	}
	
	private String label = null;
	/**
		Sets the label of this element; e.g., "table"
	 */
	public void setLabel( String s )
	{
		label = s;
	}
	
	/** Returns the label of this particular element */
	public String getLabel()
	{
		return label;
	}

	public void addCell( RGCell c )
	{
		associatedCells.add( c );
	}
	
	/**
		Returns all the cells for this attribute, in element order.
	 */
	public ArrayList getCells()
	{
		return associatedCells;
	}
	

}
