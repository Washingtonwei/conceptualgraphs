package repgrid;


import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

//
//  RepertoryGrid.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun Apr 20 2003.
//
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
	Represents the abstraction of a single repertory grid, encapsulating a name, a set of attributes
		(constructs) and elements, and cells corresponding to each attribute/construct pair.
		Allows for tracked repertory grids such that this grid's attributes or elements can be used
		in another grid.
		<p>
		Conceptually the grid is a list of attributes, a list of elements and a list of cells.
 */
public class RepertoryGrid { // extends JTable  {
			/** The type of the attributes (rows) of this grid. */
	public String attributeLabel = null;
			/** The type of the elements (columns) of this grid. */
	public String elementLabel = null;
			/** The relation type that relates to attributes and elements */
	public String relationLabel = null;
			/** What type is every cell in this grid? */
	public RGValue valuetype = null;
	
	protected ArrayList attributes = new ArrayList();
	
	private ArrayList elements = new ArrayList();
	
	private ArrayList cells = new ArrayList();
	
	/** An identifying string for this grid. If association with a file is needed, use a TrackedRepertoryGrid.
	@see repgrid.tracks.TrackedRepertoryGrid
	*/
	public String name = "Untitled" + craft.Craft.getNextNumber();
	
	/**
		Create a new, empty repertory grid, with the particular labels and value type.
		@param eLabel the term that includes all of the elements in this grid (e.g., "furniture").
		@param rLabel the term that denotes the relationship from the element to the attributes (e.g., "has").
		@param aLabel the term that includes all of the attributes/constructs in this grid (e.g., "attribute" ).
		@param type the type of value for each cell of this grid. Any actual values in this instance are
			ignored.
		@see RGValue
	 */
	public RepertoryGrid( String eLabel, String rLabel, String aLabel, RGValue type )
	{
		valuetype = type.copy();
		attributeLabel = aLabel;
		elementLabel = eLabel;
		relationLabel = rLabel;
		//addAttribute( "blank" );	// these are placeholders for the upper left hand empty cell of a table
		//add( "blank" );
	}
	
	/**
		Create a new, empty and completely blank repertory grid, ready to have everything set.
	 */
	public RepertoryGrid()
	{
	}
	
	/** Get the string (question) used to label this grid; e.g., "What are attributes of furniture?" */
	public String getQuestionLabel() 
		{ return /*"Display: " +*/ elementLabel + " "  + relationLabel + " " + attributeLabel; }
	
	/** Sets the term that includes all of the attributes/constructs in this grid (e.g., "attribute" ).
	*/
	public void setAttributeLabel( String s ) { attributeLabel = s; }

	/** Sets the term that includes all of the elements in this grid (e.g., "furniture" ).
	*/
	public void setElementLabel( String s ) { elementLabel = s; 
		craft.Craft.say( "setting element label to " + s ); }

	/** Sets the term that denotes the relationship from the element to the attributes (e.g., "has").
	*/
	public void setRelationLabel( String s ) { relationLabel = s; }

	/** Sets the value type that will be used to acquire and represent values for this grad.
	*/
	public void setValueType( RGValue v ) { valuetype = v.copy(); }

	/**
		Adds the given string as an element (column) label.
		Creates a set of associated cells for every existing attribute (row) in the grid. This 
		means that the grid is never "sparse" in the sense of having any missing cells.
		@param s the element(column) label; non-null
		@throws IllegalArgumentException if s is either null or the empty string
	 */
	public void add( String s ) throws IllegalArgumentException { add( s, -1 ); }

	/** Add an element (column) with the given description to this grid. 
		@param s The label for the element (column) to add
		@param pos The position in the list of columns. -1 if to be added automatically.
		@throws IllegalArgumentException if s is either null or the empty string
	*/
	public void add( String s, int pos ) throws IllegalArgumentException
	{
		if ( s == null || s.equals( "" ) ) 
			throw new IllegalArgumentException( "add: label string cannot be null or empty" );
		RGElement e = new RGElement( s );
		if ( pos < 0 )
			elements.add( e );
		else
			elements.add( pos, e );
		Iterator iter = attributes.iterator();
		while ( iter.hasNext() )
		{
			RGCell c = new RGCell( (RGAttribute)iter.next(), e, valuetype.copy() );
			cells.add( c );
		}
	}
	
	/**
		Adds the given string as an attribute (row) label.
		Creates a set of associated cells for every existing element (column) in the grid. This 
		means that the grid is never "sparse" in the sense of having any missing cells.
		@param s the attribute (row) label; must be non-null;
		@throws IllegalArgumentException if s is either null or the empty string
	 */
	public void addAttribute( String s ) throws IllegalArgumentException { addAttribute( s, -1 ); }
	
	/** Add an attribute (construct) "row" with the given description to this grid. 
		@param s The label for the attribute (row) to add
		@param pos The position in the list of rows. -1 if label is to be added automatically.
		@throws IllegalArgumentException if s is either null or the empty string
	*/
	public void addAttribute( String s, int pos ) throws IllegalArgumentException
	{
		if ( s == null || s.equals( "" ) ) 
			throw new IllegalArgumentException( "addAttribute: label string cannot be null or empty" );
		RGAttribute a = new RGAttribute( s );
		if ( pos < 0 ) 
			attributes.add( a );
		else
			attributes.add( pos, a );
		Iterator iter = elements.iterator();
		while ( iter.hasNext() )
		{
			RGCell c = new RGCell( a, (RGElement)iter.next(), valuetype.copy() );
			cells.add( c );
		}
	}
	
	/**
		Deletes an attribute (row) label and all of its associated cells from a repertory grid.
	 */
	public void deleteAttribute( String label )
	{
		RGAttribute attr = getAttribute( label );
		if ( attr != null ) deleteAttribute( attr );
	}
	
	/**
		Deletes an element (col) label and all of its associated cells from a repertory grid.
	 */
	public void deleteElement( String label )
	{
		RGElement elem = getElement( label );
		if ( elem != null ) deleteElement( elem );
	}
	
	/**
		Finds an attribute object based on its label.
		@param label identifier for this attribute
		@return attribute object if found; <code>null</code> otherwise.
	 */
	public RGAttribute getAttribute( String label )
	{
		Iterator iter = attributes.iterator();
		while ( iter.hasNext()  )
		{
			RGAttribute attr = (RGAttribute)iter.next();
			if ( attr.getLabel().equals( label ) ) return attr;
		}
		return null;
	}


	/**
		Finds an element object based on its label.
		@param label identifier for this element
		@return element object if found; <code>null</code> otherwise.
	 */
	public RGElement getElement( String label )
	{
		Iterator iter = elements.iterator();
		while ( iter.hasNext()  )
		{
			RGElement elem = (RGElement)iter.next();
			if ( elem.getLabel().equals( label ) ) return elem;
		}
		return null;
	}

	
	/**
		Deletes an attribute (row) label and all of its associated cells froma repertory grid.
	 */
	public void deleteAttribute( RGAttribute attr )
	{
		Iterator cells = ((ArrayList)attr.getCells().clone()).iterator();
		while ( cells.hasNext() ) 
		{
			RGCell c = (RGCell)cells.next();
			deleteCell( c );
		}
		getAttributes().remove( attr );
	}
	
	/**
		Deletes an element (col) label and all of its associated cells froma repertory grid.
	 */
	public void deleteElement( RGElement elem )
	{
		Iterator cells = ((ArrayList)elem.getCells().clone()).iterator();
		while ( cells.hasNext() ) 
		{
			RGCell c = (RGCell)cells.next();
			deleteCell( c );
		}
		getElements().remove( elem );
	}
	
	
	/**
		Accesses the attributes (rows) of this repertory grid.
		@return a vector of RGAttribute objects
	 */
	public ArrayList getAttributes()
	{
		return attributes;
	}
	
	/**
		Accesses the elements (columns) of this repertory grid.
		@return a vector of RGElement objects
	 */	
	public ArrayList getElements()
	{
		return elements;
	}
	
	/**
		Returns the list of cells in this grid.
		@see RGCell
	 */
	public ArrayList getCells()
	{
		return cells;
	}
	
	/**
		Find the cell associated with a given attribute (row) and element (column) pair.
		@return the associated cell, <code>null</code> if there isn't one.
		@see #addAttribute
		@see #add
	 */
	public RGCell getCell( RGAttribute a, RGElement e )
	{
				//charger.Global.info( "getCell: attribute a is " + a.getLabel() + "; has " + a.getCells().size() + " cells");
		Iterator iter = a.getCells().iterator();
		while ( iter.hasNext() )
		{
			RGCell c = (RGCell)iter.next();
					//charger.Global.info( "getCell: cell element is " + c.getElement() );
			if ( c.getElement() == e ) return c;
		}
		return null;
	}
	
	/**
		Find the cell at the given row,col index, considering the grid as a simple 2-dimensional array.
		@param attnum index into the attribute vector
		@param elemnum index into the element vector
		@return cell in question; <code>null</code> if there isn't one.
	 */
	public RGCell getCell( int attnum, int elemnum )
	{
		RGAttribute a = (RGAttribute)getAttributes().get( attnum );
		RGElement e = (RGElement)getElements().get( elemnum );
		return getCell( a, e );
	}
	
	/**
		Detaches a cell from its associated element, attribute and the list of cells.
		Disposes the cell.
		@param c the cell to be deleted.
	 */
	public void deleteCell( RGCell c )
	{
		getElements().remove( c );
		getAttributes().remove( c );
		getCells().remove( c );
	}
	
	/** Prints a summary of the grid on System.out */
	public void summary() { summary( "" ); }
	
	/** Prints a summary of the grid on System.out.
		@param s prefix the summary with this string
	*/
	public void summary( String s )
	{
		StringBuilder sb = new StringBuilder( s + " Summary of grid for : \"" + getQuestionLabel() + "\"\n" );
		Iterator iter = cells.iterator();
		while ( iter.hasNext() )
		{
			RGCell c = (RGCell)iter.next();
			sb.append( "Cell: element: " + c.getElement().getLabel() + "  attribute: " + 
					c.getAttribute().getLabel() + "  value: " + c.getRGValue().toString() + "\n");
		}
		craft.Craft.say( sb.toString() );
	}
	
	/**
		Determines whether any cells in the grid still need to have values assigned.
		@return true if all cells have values; false otherwise.
	 */
	public boolean gridComplete()
	{
		Iterator iter = cells.iterator();
		while ( iter.hasNext() )
		{
			RGCell c = (RGCell)iter.next();
			if ( ! c.hasValue() ) return false;
		}
		return true;
	}


	/**
		Are there no cells at all in this grid?
		@return true if there are no cells (i.e., no attributes or elements either)
		 */
	public boolean gridEmpty()
	{
		if ( cells.size() == 0 ) return true;
		else return false;
	}
	
	/** Sets a name for the grid (usually its filename) */
	public void setName( String n ) { name = n; }
	
	public String getName() { return name; }
	
	public String toBurmeisterCXTString( String title )
	{
		String eol = System.getProperty( "line.separator" );
		StringBuilder s = new StringBuilder( "" );
		s.append( "B" + eol );
		s.append( title + eol );
		s.append( new Integer( getElements().size() ).toString() + eol );
		s.append( new Integer( getAttributes().size() ).toString()  + eol );
		Iterator iter = getElements().iterator();
		while ( iter.hasNext() )
		{
			s.append( ((RGElement)iter.next()).getLabel() + eol );
		}
		
		iter = getAttributes().iterator();
		while ( iter.hasNext() )
		{
			s.append( ((RGAttribute)iter.next()).getLabel() + eol );
		}
		
		Iterator iterelem = getElements().iterator();
		while ( iterelem.hasNext() )
		{
			RGElement elem = (RGElement)iterelem.next();
			Iterator iterattr = elem.getCells().iterator();
			while ( iterattr.hasNext() )
			{
				if ( ! ( valuetype instanceof RGBooleanValue ) )
				{
					charger.Global.error( "Can't export Burmeister on " + valuetype.getClass().getName() );
					return null;
				}
				else
				{
					RGCell c = (RGCell)iterattr.next();
					if ( c.getRGValue().getValue().equals(( new Boolean( "true" ) ) ) )
						s.append( "x" );
					else
						s.append( "." );
				}
			}
			s.append( eol );
		}
		return s.toString();
	}
	

}
