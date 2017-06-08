//
//  RGValue.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 23 2003.
//
package repgrid;

import javax.swing.*;
import javax.swing.table.*;

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
	Abstraction of a cell value in a repertory grid.
	Regardless of the actual underlying type, 
 */
abstract public class RGValue {
	protected boolean hasValue = false;
	
	public RGValue()
	{
		super();
	}
	
	public RGValue copy()
	{
		RGValue v = null;
		try {
			v = (RGValue)(getClass().newInstance());
		} catch ( Exception e ) {
			JOptionPane.showMessageDialog( null, "Error copying rvalue " + 
					e.getMessage(), "RG Value Error", JOptionPane.ERROR_MESSAGE );
		}
		return v;
	}
	
	/**
		Ask an interactive user what value to assign.
		@param s An explanatory string for the user.
	 */
	abstract public boolean queryCellValue( String s );
	
	/**
		Whether this cell has already been assigned a value or not
		@return <code>true</code> if there's a value; <code>false</code>otherwise.
	 */
	public boolean hasValue()
	{
		return hasValue;
	}
	
	/**
		Return the value as some kind of object.
	 */
	abstract public Object getValue();
	
	/**
		Whatever kind of value this is, show it as a displayable string.
		@return the value as a string.
	 */
	abstract public String toString();
	
	/**
		An explanatory string to describe what the values mean.
		@return a fixed message for every value of this type.
	 */
	abstract public String explainMe();
	
	/**
		Returns the kind of value expected.
	 */
	abstract public Class getValueClass();
	
	/**
		Returns the editor that should be used in a JTable (or others) for this kind of value.
	 */
	public TableCellEditor getEditor()
	{
		//return new DefaultCellEditor( new JTextField() );
		return null;
	}

}
