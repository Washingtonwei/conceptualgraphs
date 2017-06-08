//
//  TrackedRepertoryGrid.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 30 2003.
//

package repgrid.tracks;

import charger.gloss.AbstractTypeDescriptor;
import repgrid.*;

import java.util.*;
import java.io.*;

import charger.obj.*;

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
	Extensions to repertory grids for their use in CRAFT.
	Not all extensions are encapsulated here; most uses of repertory grid in CRAFT involve
		this class instead of its base class.
	Some extensions to repertory grids and where to find them are as follows:
	<table  border="2" cellspacing="1" cellpadding="1">
	<tr><td valign="top">Attributes<td valign="top">Tracked grids can use a TrackedAttribute. They are constructed, however, with
		the default RGAttribute; a TrackedAttribute only replaces it when a type descriptor (of any
			kind) is provided, either by the user or by some automated means.
	<tr><td valign="top">XML generation<td valign="top">TrackedAttributes are generated with more detail.
	<tr><td valign="top">conceptual basis&nbsp;<td valign="top">TrackedGrid's have a concept-relation-concept pattern associated with them, 	
		and are therefore connected to specific objects in specific conceptual graphs using a locator.
	<tr><td valign="top">source file<td valign="top">For convenience, the source file (if any) can be saved with a TrackedGrid
	</table>
 */
public class TrackedRepertoryGrid extends RepertoryGrid implements kb.KnowledgeSource {

		/** The source of this grid in the file system, <code>null</code> if there isn't one. */
	public File sourceAbsoluteFile = null;
	
		/** Concept corresponding to the element aspect */
	public Concept conceptOne = null;		
		/** Concept corresponding to the attribute aspect */
	public Concept conceptTwo = null;	
		/** Either a relation or an actor */
	public GNode relation = null;

		/** Locator for the concept corresponding to the element aspect
			@see charger.util.ObjectLocator */
	public charger.util.ObjectLocator conceptOneLocator = null;
		/** Locator for the concept corresponding to the attribute aspect
			@see charger.util.ObjectLocator */
	public charger.util.ObjectLocator conceptTwoLocator = null;
		/** Locator for either a relation or an actor
			@see charger.util.ObjectLocator */
	public charger.util.ObjectLocator relationLocator = null;
		
	/** 
		Creates a new tracked repertory grid, with no rows or columns.
		@param eLabel element label, telling what kind of thing the elements are
		@param rLabel relationship label, denoting relationship between elements and attributes
		@param aLabel attribute label, telling what kind of thing the attributes are
		@param type The type of value to be associated with each element/attribute pair.
			Currently only RGBooleanValue types are fully supported; use others at your own risk!
	 */
	public TrackedRepertoryGrid( String eLabel, String rLabel, String aLabel, RGValue type )
	{
		super( eLabel, rLabel, aLabel, type );
	}
	
	public TrackedRepertoryGrid( Concept c1, GNode r, Concept c2, RGValue type )
	{
		super( c1.getTypeLabel(), r.getTypeLabel(), c2.getTypeLabel(), type );
		conceptOne = c1;
		conceptTwo = c2;
		relation = r;
		conceptOneLocator = conceptOne.getObjectLocator();
		conceptTwoLocator = conceptTwo.getObjectLocator();
		relationLocator = relation.getObjectLocator();
		
	}

	/**
		Construct a new, empty grid with no labels and no cells. Cell type defaults to RGBooleanValue.
	 */
	public TrackedRepertoryGrid()
	{
		super();
		valuetype = new RGBooleanValue();	
	}
	
	public TrackedRepertoryGrid( File f )
	{
		super();
		sourceAbsoluteFile = f;
	}
	
	public File getFile()	{ return sourceAbsoluteFile; }
	
	/**
		Associate the given file with this grid.
		@param f the file to be associated; no assumption made about its existence or permissions.
	 */
	public void setFile( File f ) 
	{ 
		sourceAbsoluteFile = f;  
		if ( f != null ) setName( f.getName() );
	}
	
	
	/**
		Find the type descriptor corresponding to the term label (ignoring uppper/lower case).
		@param label the term whose descriptor is being sought.
		@return the previous descriptor with that label; <code>null</code> if none was found.
	 */
/*	public TypeDescriptor getTypeDescriptor( String label )
	{
		Iterator as = getAttributes().iterator();
		while ( as.hasNext() )
		{
			RGAttribute a = (RGAttribute)as.next();
			if ( ! (a instanceof TrackedAttribute) ) continue;
			TypeDescriptor d = ((TrackedAttribute)a).getTypeDescriptor( label );
			if ( d != null ) return d;
		}
		return null;
	}
	*/
	
	
	/**
		Assumes a vector of strings, to be made the elements of this grid.
		If there are any existing elements, they are erased
	 */
	/*public void setElements( ArrayList v )
	{
		
	}
	
	public void addElements( ArrayList v )
	{
	
	}
	
	public void addAttribute( String s )
	{
	
	}
	*/
	
	/**
		Substitutes a new RGAttribute (or a subclass) for the current attribute.
		Useful in converting one class to another.
		Works differently from delete/insert in that associated cells are preserved.
	 */
	 public void replaceAttribute( RGAttribute oldattr, RGAttribute newattr )
	 {
				//charger.Global.info( "calling replace attr; old has " + oldattr.getCells().size() + " cells." );
		int anum = 0;
		while ( anum < attributes.size() && attributes.get( anum ) != oldattr ) 
			anum++;
					//charger.Global.info( "attr num to replace is " + anum + " of " + attributes.size() + " total.");
		if ( anum < attributes.size() ) 
		{
			attributes.set( anum, newattr );
		}
	 }


	public charger.gloss.AbstractTypeDescriptor[] findTypeDescriptors( String term )
	{
		ArrayList holder = new ArrayList();
		AbstractTypeDescriptor[] ds = getAllTypeDescriptors();
					craft.Craft.say( "grid has " + ds.length + " type descriptors, looking for term " + term );
		for ( int k = 0; k < ds.length; k++ )
		{
					//craft.Craft.say( "descriptor " + k + " has label " + ds[ k ].getLabel() );
			if ( term.equalsIgnoreCase( ds[ k ].getLabel() ) )
			{
				holder.add( ds[ k ] );
						//craft.Craft.say( "found a descriptor to match the term " + term );
			}
		}
		return (AbstractTypeDescriptor[])( holder.toArray( new AbstractTypeDescriptor[0] ) );
	}
	
	/**
		Get all the type descriptors known to this knowledge source.
		@return a possibly empty list, but never null;
	 */
	/**
		Get all the type descriptors known to this knowledge source.
		@return a possibly empty list, but never null;
	 */
	public AbstractTypeDescriptor[] getAllTypeDescriptors()
	{
		ArrayList holder = new ArrayList();
		Iterator iter = getAttributes().iterator();
		while ( iter.hasNext() )
		{
			RGAttribute a = (RGAttribute)iter.next();
			if ( a instanceof TrackedAttribute )
			{
				AbstractTypeDescriptor[] ds = ((TrackedAttribute)a).getTypeDescriptors();
				holder.addAll( Arrays.asList( ds ) );
			}
		}
		return (AbstractTypeDescriptor[])(holder.toArray( new AbstractTypeDescriptor[0] ));
	}
	
	/**
		Get all the graphs known to this knowledge source.
		@return a possibly empty list of graphs, but never null;
	 */
	public charger.obj.Graph[] getAllGraphs()
	{
		return new Graph[0];		// aren't any graphs in a grid.
	}

	
}
