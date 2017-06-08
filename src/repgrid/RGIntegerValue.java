//
//  RGIntegerValue.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 23 2003.
//
package repgrid;

import javax.swing.*;

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

public class RGIntegerValue extends RGValue {
	public int intvalue = 0;

	public RGIntegerValue( )
	{
		super();
	}
	
	public void setValue( Integer i ) throws RGValueException
	{ 
		if ( i.intValue() > 1000000 ) throw new RGValueException( "Should never happen." );
		intvalue = i.intValue();
		hasValue = true;
	}
	
	public Object getValue()
	{
		return new Integer( intvalue );
	}
	
	public boolean queryCellValue( String s )
	{
		String answer = JOptionPane.showInputDialog( "Enter integer value:" );
		if ( answer != null ) 
		{
			/*try
			{
			
			} catch { Exception e } ( JOptionPane.showMessageDialog
			*/
		}
		return true;
	}

	public String toString() 
	{ 
		if ( hasValue() )
			return "" + intvalue; 
		else
			return "(no value)";
	}
	
	public String explainMe()
	{
		return "value from 0 (low association) to some large number (high association)";
	}

	public Class getValueClass() { return Integer.class; }
	
	
}
