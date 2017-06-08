//
//  RGBooleanValue.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 23 2003.
//
package repgrid;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

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
	Abstraction of a repertory grid cell value that represents a true/false value.
 */
public class RGBooleanValue extends RGValue {
	public boolean booleanvalue = false;

	public RGBooleanValue( )
	{
		super();
	}
	
	public void setValue( Boolean b ) throws RGValueException
	{ 
		String s = "hello";
		if ( s.equals( "goodbye" ) ) throw new RGValueException( "Boolean value exception; should never happen." );
		else booleanvalue = b.booleanValue();
		hasValue = true;
	}
	
	public Object getValue()
	{
		//if ( ! hasValue() ) return new String( "(no value)" );
		if ( booleanvalue ) return new Boolean( true ); //"X";
		else return new Boolean( false ); //" ";
	}
	
	public boolean queryCellValue( String s )
	{
			//charger.Global.info( "before query dialog, choices has " + choices.length + " elements." );
		int option = JOptionPane.showConfirmDialog( null,
			s + "\nEnter " + explainMe(),
			"Does something apply?",
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE );
		try {
			if ( option == JOptionPane.YES_OPTION )
				setValue( new Boolean( true ) );
			else if ( option == JOptionPane.NO_OPTION ) 
				setValue( new Boolean( false ) );
		} catch ( RGValueException  e ) { }	// should never happen :-)

		if ( option == JOptionPane.CANCEL_OPTION )
			return false;
		else
			return true;
	}
	

	public String toString() 
	{ 
		if ( hasValue() )
			return "" + booleanvalue; 
		else
			return "(no value)";
	}
	
	public String explainMe()
	{
		return "YES if applies, \"no\" if doesn't apply";
	}
	
	public Class getValueClass() { return Boolean.class; }

	
	public TableCellEditor getEditor()
	{
		final JCheckBox cb = new JCheckBox();
		
		return new DefaultCellEditor( cb ) {
			public Object getCellEditorValue() {
				return new Boolean( cb.getSelectedObjects() != null );
			}
			public Component getTableCellEditorComponent( JTable table, Object value, boolean  isSelected,
						int row, int column)
			{
				RepertoryGrid rg = ((RGTableModel)table.getModel()).rg;
				Component   result   =
					super.getTableCellEditorComponent( table, value, isSelected, row, column );
					//((JTextComponent)result).setHorizontalAlignment( SwingConstants.CENTER );
					RGCell c = rg.getCell( row, column );
					if ( ! c.hasValue() ) 
					{
						result.setBackground( Color.green );
					}
				return (result);
			}
		};
			//return null;
	}
}
