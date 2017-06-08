//
//  RGCell.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 23 2003.
//
package repgrid;

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
	A cell inside a repertory grid, associated with exactly one element and one attribute.
 */
public class RGCell {
	private RGAttribute attr = null;
	
	private RGElement elem = null;
	
	private RGValue value = null;

	public RGCell( RGAttribute a, RGElement e, RGValue v )
	{
		attr = a;
		a.addCell( this );
		elem = e;
		e.addCell( this );
		setRGValue( v );
	}
	
	public RGAttribute getAttribute()
	{
		return attr;
	}
	
	public void setAttribute( RGAttribute a )
	{
		attr = a;
	}

	public RGElement getElement()
	{
		return elem;
	}
	
	public void setElement( RGElement e )
	{
		elem = e;
	}
	
	public RGValue getRGValue()
	{
		return value;
	}
	
	/**
		Assigns a value object to this cell, but the object may wrap a null value.
	 */
	public void setRGValue( RGValue v )
	{
		value = v;
	}
	
	public boolean hasValue()
	{
		return getRGValue().hasValue();
	}
	
	public boolean equals( RGCell c )
	{
		if ( c.getRGValue().getClass() != this.getRGValue().getClass() ) return false;
		if ( ! c.getRGValue().hasValue() && ! this.getRGValue().hasValue() ) return true;
		if ( c.getRGValue().getValue().equals( this.getRGValue().getValue() ) ) return true;
		else
			return false;
	}

}
