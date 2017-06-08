//
//  KnowledgeSource.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun Jul 13 2003.
//

package kb;

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
	Encapsulates the interfaces to various knowledge sources (e.g., conceptual graphs or repertory grids).
	
 */
public interface KnowledgeSource {

	/**
		Look for the given type descriptor in the knowledge source, however it may be represented.
		Calls to findTypeDescriptors may return more than one descriptor, since some compound terms
			may require two or more descriptors.
		@param term a phrase that may have type descriptors.
		@return zero or more type descriptors, but never null;
	 */
	public charger.gloss.AbstractTypeDescriptor[] findTypeDescriptors(String term);
	
	/**
		Get all the type descriptors known to this knowledge source.
		@return a possibly empty list, but never null;
	 */
	public charger.gloss.AbstractTypeDescriptor[] getAllTypeDescriptors();
	
	/**
		Get all the graphs known to this knowledge source.
		@return a possibly empty list of graphs, but never null;
	 */
	public charger.obj.Graph[] getAllGraphs();
}
