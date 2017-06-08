//
//  CGXMLGenerator.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 04 2003.
//

package repgrid.xml;


import repgrid.*;
import repgrid.tracks.*;
import java.util.*;
import charger.util.*;

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
	Used for parsing XML representations of a repertory grid.
 Based on code and explanation found at <a href="http://www.saxproject.org/?selected=quickstart">SAX's web site</a>.
	@see RGXMLParser
*/
public class RGXMLGenerator extends charger.xml.XMLGenerator {

	private static String eol = System.getProperty( "line.separator" );


	/**
		Prepares the entire grid in XML form.
		@param rg the grid to be represented
		@return XML representation of the repertory grid, suitable for viewing or saving.
	 */
	public static String XML( RepertoryGrid rg )
	{
		return XMLHeader() + eol +
			startTag( "repertorygrid" ) + eol +
			RGHeader( (TrackedRepertoryGrid)rg ) + eol +
			RGAttributes( rg ) + eol +
			RGElements( rg ) + eol +
			RGCells( rg ) + eol +
			endTag( "repertorygrid" ) + eol;
			
	}
	
	/*private static String XMLHeader()
	{
		return "<?xml version=\"1.0\"?>";
	}
	*/
	
	private static String RGHeader( TrackedRepertoryGrid rg )
	{
		String tab = "  ";
		StringBuilder s = new StringBuilder( "" );
		
		s.append( RGHeaderLabel( tab, "element", rg.elementLabel, rg.conceptOneLocator ) );
		s.append( RGHeaderLabel( tab, "relation", rg.relationLabel, rg.relationLocator ) );
		s.append( RGHeaderLabel( tab, "attribute", rg.attributeLabel, rg.conceptTwoLocator ) );

		String valuetypestring = "RGBooleanValue"; 	// default value type
		if ( rg.valuetype != null ) valuetypestring =  rg.valuetype.getValueClass().getName();
		s.append( tab + tagWithParms( "headerlabel", "type=\"valuetype\" class=\"" + valuetypestring + "\"" ) + eol);
		return s.toString();
	}
	
	private static String RGHeaderLabel( String indent, String labeltype, String text, ObjectLocator locator )
	{
		StringBuilder s = new StringBuilder( "" );
		StringBuilder parms = new StringBuilder( "" );
		parms.append( "type=\"" + labeltype + "\"" );
		parms.append( " text=\"" + text + "\"" );
		if ( locator != null ) 
		{
			parms.append( " uri=\"" + locator.toURI() + "\"" );
				craft.Craft.say( "URI appended is " + locator.toURI() );
		}
		s.append( indent + tagWithParms( "headerlabel", parms.toString() ) + eol );
		return s.toString();
	}

	private static String RGElements( RepertoryGrid rg )
	{
		StringBuilder s = new StringBuilder( "" );
		for ( int k = 0; k < rg.getElements().size(); k++ )
		{
			s.append( simpleTaggedString( "element", 
				((RGElement)rg.getElements().get( k )).getLabel(), 
				"id=\"" + k + "\"" ) + eol );
		}
		return s.toString();
	}

	private static String RGAttributes( RepertoryGrid rg )
	{
		StringBuilder s = new StringBuilder( "" );
		String tab = "  ";
		for ( int k = 0; k < rg.getAttributes().size(); k++ )
		{
			{
				s.append( startTag( "attribute", "id=\"" + k + "\"" ) + eol );
				s.append( tab + simpleTaggedString( "label", 
					((RGAttribute)rg.getAttributes().get( k )).getLabel() ) + eol );
				if ( rg.getAttributes().get( k ) instanceof repgrid.tracks.TrackedAttribute )
				{
					charger.gloss.AbstractTypeDescriptor[] descrs =
						((repgrid.tracks.TrackedAttribute)rg.getAttributes().get( k )).getTypeDescriptors();
					for ( int dnum = 0; dnum < descrs.length; dnum++ )
					{
						s.append( tab + startTag( "term" ) + eol );
						s.append( tab + tab + simpleTaggedString( "label", descrs[ dnum ].getLabel() ) + eol);
						s.append( descrs[ dnum ].toXML( tab + tab ) + eol );
						s.append( tab + endTag( "term" ) + eol );
					}
				}
				s.append( endTag( "attribute" ) + eol );
				/*
					form of tracked attribute is:
					<attribute>
						<label>the attribute's text label</label>
						<wordnet pos="verb" offset="12345">
						  <label>wordnet term</label>
						  <definition>a definition string goes here</definition>
						</wordnet>
						<wordnet ....
						</wordnet>
					</attribute>
				 */
			}
		}
		return s.toString();
	}

	private static String RGCells( RepertoryGrid rg )
	{
		StringBuilder s = new StringBuilder( "" );
		Iterator iter = rg.getCells().iterator();
		while ( iter.hasNext() )
		{
			s.append( RGCell( rg, (RGCell)iter.next() ) );
		}
		return s.toString();
	}
	
	private static String RGCell( RepertoryGrid rg, RGCell c )
	{
		if ( ! c.hasValue() ) return "";
		StringBuilder parms = new StringBuilder( "" );
		if ( c.getRGValue() instanceof RGBooleanValue )
			parms.append( "valuetype=\"boolean\"" + " " );
		else if ( c.getRGValue() instanceof RGIntegerValue )
			parms.append( "valuetype=\"integer\"" + " " );
			//	so far, ignoring the range of an integer value
		parms.append( "attribute=\"" + rg.getAttributes().indexOf( c.getAttribute() ) + "\"");
		parms.append( " " );
		parms.append( "element=\"" + rg.getElements().indexOf( c.getElement() ) + "\"");
		parms.append( " " );
		parms.append( "value=\"" + c.getRGValue().toString() + "\"" );
			
		return tagWithParms( "cell", parms.toString() ) + eol;
	}
	
	private static String objectLocator( ObjectLocator ol, String indent )
	{
		return ol.toXML( indent );
	}
	
	/**
		Rewrite its arguments for XML.
		@param tag
		@param value
		@param parameters

	 */
	/*private static String simpleTaggedString( String tag, String value, String parameters )
	{
		String parm = " " + parameters;
		if ( parameters.equals( "" ) ) parm = parameters;
		return startTag( tag + parm)  + value  + endTag( tag );
		/*return startTag( tag + parm) +
			"\t" + "<string>" + value + "</string>" + eol +
			endTag( tag );
	}
	
	private static String simpleTaggedString( String tag, String value )
	{
		return simpleTaggedString( tag, value, "" );
	}
	
	private static String startTag( String tag ) { return "<" + tag + ">"; } // + eol; }
	
	private static String endTag( String tag ) { return "</" + tag + ">"; } // + eol; }
*/
}
