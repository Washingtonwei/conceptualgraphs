//
//  RGIntegerValueRange.java
//  CharGer 2003
//
//  Created by Harry Delugach on Thu Apr 24 2003.
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

public class RGIntegerValueRange extends RGIntegerValue {
	int min = 1;
	int max = 1;
	
	String choices[] = null;
	
	public RGIntegerValueRange()
	{
		super();
	}
	
	public RGIntegerValueRange( int n )
	{
		max = n;
		setup();
	}
	
	public RGValue copy()
	{
		RGIntegerValueRange v = (RGIntegerValueRange)super.copy();
		v.min = 1;
		v.max = max;
		v.setup();
		return v;
	}
	
	public void setup()
	{
			//charger.Global.info( "running setup" );
		choices = new String[ max ];
		for ( int k = 0; k < max; k++ )
		{
			choices[k] = "" + (k+1) + "";
		}	
	}
	
	public void setValue( Integer n ) throws RGValueException
	{
		if ( n.intValue() < min || n.intValue() > max )
			throw new RGValueException( "Value out of range " + min + " to " + max );
		else
		{
			intvalue = n.intValue();
			hasValue = true;
		}
	}
	
	public boolean queryCellValue( String s )
	{
			//charger.Global.info( "before query dialog, choices has " + choices.length + " elements." );
		String answer = (String)JOptionPane.showInputDialog( null,
			s + "\nEnter " + explainMe(),
			"Does something apply?",
			JOptionPane.QUESTION_MESSAGE,
			null,
			choices,
			choices[0]);
		if ( answer != null )
		{
			try
			{
				setValue( Integer.decode( answer ) );
			} catch ( Exception e ) {  }
			return true;
		}
		else
			return false;
	}
	
	public String explainMe()
	{
		return "value from " + min + " (doesn't apply) - - - - - " + max + " (strongly applies)";
	}
	
	public TableCellEditor getEditor()
	{
		final JComboBox cb = new JComboBox();
		for ( int k = min; k <= max; k++ )
			cb.addItem( "" + k );

		return new DefaultCellEditor( cb );/* {
			public Object getCellEditorValue() {
				return Integer.decode( (String)cb.getSelectedItem() );
			}
		};*/
			//return null;
	}

}
