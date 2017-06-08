//
//  RGTableModel.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sat Apr 26 2003.
//
package repgrid;

import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import java.awt.Component;
import java.awt.Color;

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

public class RGTableModel extends AbstractTableModel  {

	public RepertoryGrid rg = null;

	/** 
		Used for setting up the row headers for a table in a scrolling pane.
		Contains an extra blank at the beginning of the headers, so that
			the upper left corner of the table can be blank.
	 */
	public ListModel rowListModel = new AbstractListModel() {
      //public Object headers[];
      //public int getSize() { return rg.getAttributes().size() + 1; }
      public int getSize() { return rg.getAttributes().size() ; }
      public Object getElementAt(int index) {
        return ((RGAttribute)rg.getAttributes().get( index )).getLabel();
		//if ( index == 0 ) return "";
        //else return ((RGAttribute)rg.getAttributes().get( index - 1 )).getLabel();
      }
    };

	
	public RGTableModel( RepertoryGrid ingrid )
	{
		rg = ingrid;
	}
	
	//void	addTableModelListener(TableModelListenerl) 
	/*Adds a listener to the list that is notified each time a change to the data model occurs. */
	
	/*Returns the most specific superclass for all the cell values in the column.*/
	public Class	getColumnClass( int columnIndex ) 
	{
		if ( rg != null && rg.valuetype != null )
			return rg.valuetype.getValueClass();
		else
			return String.class;
		/*
		//return RGElement.class;
		if ( rg.valuetype instanceof RGIntegerValue ) 
			return Integer.class;
		if ( rg.valuetype instanceof RGBooleanValue )
			//return String.class;
			return Boolean.class;
			//return ImageIcon.class;
		else 
			return String.class;
		*/
	}
	
	public int	getColumnCount() 
	{
		return rg.getElements().size();
	}
	/*Returns the number of columns in the model. */
	
	public String	getColumnName(int columnIndex) 
	{
		return ((RGElement)rg.getElements().get( columnIndex )).getLabel();
	}
	/*Returns the name of the column at columnIndex. */
	
	public int	getRowCount() 
	{
		return rg.getAttributes().size();
	}
	/*Returns the number of rows in the model. */
	
	public Object getValueAt( int rowIndex, int columnIndex ) 
	/*Returns the value for the cell at columnIndex and rowIndex. */
	{
		//try {
			RGAttribute a = (RGAttribute)rg.getAttributes().get( rowIndex );
					//craft.Craft.say( "rg attribute is " + a );
			RGElement e = (RGElement)rg.getElements().get( columnIndex );
					//craft.Craft.say( "rg element is " + e );
			RGCell c = rg.getCell( a, e );
					//craft.Craft.say( "Cell at (" + rowIndex + "," + columnIndex + ") is " + c );
			return c.getRGValue().getValue();
			//return c.getRGValue().toString();
			/*if ( c.hasValue() )
				return c.getRGValue().getValue();
			else
				return "(no value)";
			*/
		/*} catch ( NullPointerException ee ) 
		{
			craft.Craft.say( "null pointer for RGTableModel: row = " + rowIndex + ", col = " + columnIndex );
			return null;
		}*/
	}  
	
	public boolean	isCellEditable(int rowIndex, int columnIndex) { return true; }
	/*Returns true if the cell at rowIndex and columnIndex is editable. */
	
	//void	removeTableModelListener(TableModelListenerl) 
	/*Removes a listener from the list that is notified each time a change to the data model occurs. */
	
	public void	setValueAt( Object aValue, int rowIndex, int columnIndex ) 
	{
		RGCell c = rg.getCell(  rowIndex, columnIndex );
		try
		{
			if ( rg.valuetype instanceof RGIntegerValue )
				((RGIntegerValue)c.getRGValue()).setValue( Integer.decode( (String)aValue ) );
			else if ( rg.valuetype instanceof RGBooleanValue )
				((RGBooleanValue)c.getRGValue()).setValue( (Boolean)aValue );
			fireTableCellUpdated( rowIndex, columnIndex );
		} catch ( RGValueException e )
		{
			charger.Global.error( "Exception in setValueAt: " + e.getMessage() );
			// should never happen!! (right....)
		}
				//rg.summary();
	}

}
