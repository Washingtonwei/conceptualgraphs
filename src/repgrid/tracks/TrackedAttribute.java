//
//  TrackedAttribute.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Jun 11 2003.
//
package repgrid.tracks;

import java.util.*;
import repgrid.*;

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
	Abstraction of a tracked attribute, with links to Wordnet.
	Realized as a "row" in a tracked repertory grid.
 */
public class TrackedAttribute extends repgrid.RGAttribute 
{	
	private String eol = System.getProperty( "line.separator" );

	/**
		Create a new tracked attribute, copying the label and cells
			from the original RGAttribute.
		@see TrackedRepertoryGrid#replaceAttribute
	 */
	public TrackedAttribute( RGAttribute attr )
	{
		super( attr.getLabel() );
		Iterator cells = attr.getCells().iterator();
		while ( cells.hasNext() )
		{
			RGCell cellToCopy = (RGCell) cells.next();
			cellToCopy.setAttribute( this );
			addCell( cellToCopy );
					//charger.Global.info( "replace attr for cell" );
		}
			//charger.Global.info( "constr for tracked attribute, old has " + attr.getCells().size() + 
			//	" cells; new has " + associatedCells.size() + " cells.");
	}
	
	/** Holds ("term", TypeDescriptor) pairs, where "term" is one or more words
			from the attribute's label. 
		Terms should not be overlapping; e.g., the phrase "herb roasted chicken" should
		not have both "herb roasted" and "roasted chicken" as terms.
	*/
	//protected Hashtable typeDescriptors = new Hashtable();
	ArrayList typeDescriptors = new ArrayList();
	
	/** Associates the given descriptor with the given term.
		@param term one or more words from the attribute's label
		@param descr a type descriptor; if null, then this method has no effect.
	 */
	public void setTypeDescriptor( String term, charger.gloss.AbstractTypeDescriptor descr )
	{
		if ( term != null && descr != null )
			//typeDescriptors.put( term, descr );
			typeDescriptors.add( descr );
				//craft.Craft.say( "added descriptor for " + term + "; there are now " + typeDescriptors.size() +" descriptors." );
	}
	
	/**
		Forget all of the type descriptors assigned to this attribute.
	 */
	public void removeAllTypeDescriptors()
	{
		typeDescriptors.clear();
	}

	/** 
		Get the type descriptor associated with the given term
		@param term one or more words from the attribute's label; <code>null</code> if there isn't one.
		@return the type descriptor corresponding to this term in the attribute phrase label. 
			If the term occurs more than once in the phrase label, then the first one's descriptor is returned.
			If the term can't be found, returns null.
	*/
	public charger.gloss.AbstractTypeDescriptor getTypeDescriptor( String term )
	{
		//return (charger.obj.TypeDescriptor)typeDescriptors.get( term );
		for ( int k = 0; k < typeDescriptors.size(); k++ )
		{
			charger.gloss.AbstractTypeDescriptor d = (charger.gloss.AbstractTypeDescriptor)typeDescriptors.get( k );
			if ( d.getLabel().equals( term ) )
				return d;
		}
		return null;
	}
	
		/** all of the cells that are associated with this attribute.
			Can be considered the cells in this attribute's "row" */
	protected ArrayList associatedCells = new ArrayList();
	
		/** Make a new attribute with the given string as a label */
	public TrackedAttribute( String s )
	{
		super( s );
	}
	
	/**
		Get all the type descriptors associated with this attribute's phrase label.
		Note that not all words or terms in a phrase have descriptors; some (or all) of the words
			may be omitted.
		@return an array containing all defined descriptors for this attribute, in the order that their 
			associated terms appear in the phrase. If there are no descriptors, a zero-length array is returned.
	 */
	public charger.gloss.AbstractTypeDescriptor[] getTypeDescriptors()
	{
		return (charger.gloss.AbstractTypeDescriptor[])(typeDescriptors.toArray( new charger.gloss.AbstractTypeDescriptor[0] ));
		//return (charger.obj.TypeDescriptor[])(typeDescriptors.values().toArray( new charger.obj.TypeDescriptor[0] ));
	}

}
