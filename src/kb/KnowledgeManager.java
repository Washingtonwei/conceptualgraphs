//
//  KnowledgeManager.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun Jul 13 2003.
//

package kb;

import charger.gloss.AbstractTypeDescriptor;
import charger.Global;
import kb.matching.AbstractTupleMatcher;
import charger.obj.*;
import repgrid.tracks.*;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

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
	Used for organizing the various sources of knowledge used by CharGer and Craft.
	A knowledge source is either a conceptual graph or a repertory grid. 
	This class is generally reserved for high level methods dealing with the knowledge
		source management; most of the "lower level" detailed methods are found in
		ConceptManager.
	@see KnowledgeSource
	@see ConceptManager
        @see kb.matching.AbstractMatchGroupMetrics
 */
public class KnowledgeManager {

	 private ArrayList knowledgeSources = new ArrayList();
	 private int graphCount = 0;
	 private int gridCount = 0;

	/** The summarize action that can be re-used in other places */
	public   Action summarizeKnowledgeAction = new AbstractAction()
	{
		public Object getValue( String s )
		{
			if ( s.equals( Action.NAME ) ) return Global.strs( "SummarizeKnowledgeLabel" );
			return super.getValue( s );
		}
		public void actionPerformed( ActionEvent e )
		{
				// find the source
				//   appears to be in the order
				//   source is JMenuItem
				//    JMenuItem's parent is JPopupMenu
				//    JPopupMenu's invoker is JMenu
				//    JMenu's parent is a JMenuBar
				//
					//craft.Craft.say( "source tree of " + e.getActionCommand() + " is " );
			java.awt.Component o = ((java.awt.Component)e.getSource());		// a JMenuItem
			JComponent invoker = (JComponent)((JPopupMenu)o.getParent()).getInvoker();
			JFrame top = (JFrame)invoker.getTopLevelAncestor();
					//craft.Craft.say( "top level ancestor is " + top );
			performKnowledgeSummary( top );
		}
	};
	
	/**
		Invokes Reporter's "displaySummary" for all the existing knowledge.
	 */
	public  void performKnowledgeSummary( JFrame owner )
	{
		craft.Reporter.displaySummary( owner );
	}
	
	/**
		Tells the Knowledge Manager that it's supposed to manage this particular knowledge source.
		@param s A knowledge source to be managed.
		@see KnowledgeSource
	 */
	public  void addKnowledgeSource( KnowledgeSource s )
	{
		knowledgeSources.add( s );
		if ( s instanceof Graph ) graphCount++;
		if ( s instanceof TrackedRepertoryGrid ) gridCount++;
				craft.Craft.say( "added knowledge source number " + knowledgeSources.size() + " " + s.getClass() );
	}
	
	public  void forgetKnowledgeSource( KnowledgeSource s )
	{
		if ( s instanceof Graph ) graphCount--;
		if ( s instanceof TrackedRepertoryGrid ) gridCount--;
		knowledgeSources.remove( s );
		if ( Global.craftEnabled) craft.Craft.say( "removed knowledge source " + s.getClass() + 
							" leaving " + knowledgeSources.size() + " remaining " );
	}
	
	/**
		Get all the type descriptors from any known knowledge sources that correspond to a term.
		@param term The term to look for.
		@param includeDuplicates Whether to include duplicates if they are found. A duplicate is
			defined to be any type descriptor that "equals" another one.
		@see charger.obj.TypeDescriptor#equals 
	 */
	public  AbstractTypeDescriptor[] findTypeDescriptor( String term, boolean includeDuplicates )
	{
				craft.Craft.say( "looking for term " + term + " in all knowledge sources." );
		ArrayList holder = new ArrayList();
		for ( int k = 0; k < knowledgeSources.size(); k++ )
		{
			KnowledgeSource s = (KnowledgeSource)knowledgeSources.get( k );
						craft.Craft.say( "looking for term " + term + " in " + s.getClass() );
			holder.addAll( Arrays.asList( s.findTypeDescriptors( term ) ) );
		}
				craft.Craft.say( "found " + holder.size() + " descriptors for term " + term + " in all sources" );
		if ( includeDuplicates )
			return (AbstractTypeDescriptor[])(holder.toArray( new AbstractTypeDescriptor[0] ));
		else
		{
			return removeDuplicateDescriptors( (AbstractTypeDescriptor[])(holder.toArray( new AbstractTypeDescriptor[0] )) );
		}
	}
	
	public static AbstractTypeDescriptor[] removeDuplicateDescriptors( AbstractTypeDescriptor[] ts )
	{
		ArrayList v = new ArrayList();
		v.addAll( Arrays.asList( ts ) );
				//craft.Craft.say( "starting remove dups, ts has " + ts.length + " elements" );
		for ( int index = 0; index < v.size(); index++ )
		{
			for ( int k = index + 1; k < v.size(); k++ )
			{
						//craft.Craft.say( "comparing index " + k + " with index " + index );
				if ( v.get( index ) != null && v.get( k ) != null )
				{
					AbstractTypeDescriptor o1 = (AbstractTypeDescriptor)v.get( index );
					AbstractTypeDescriptor o2 = (AbstractTypeDescriptor)v.get( k );
					if ( o1.equals( o2 ) )
					{
						v.set( k, null );
								//craft.Craft.say( "deleting duplicate at index " + k );
					}
				}
			}
		}
		while ( v.contains( null ) ) v.remove( null );
		return (AbstractTypeDescriptor[]) v.toArray( new AbstractTypeDescriptor[0] );
	}
		
	public  Graph[] getAllGraphs()
	{
		Graph[] gs = new Graph[ graphCount ];
		int next = 0;
		for ( int k = 0; k < knowledgeSources.size(); k++ )
		{
			if ( knowledgeSources.get( k ) == null )
				Global.error( "Knowledge source number " + k + " was null." );
			else if ( knowledgeSources.get( k ) instanceof Graph )
				gs[ next++ ] = (Graph)knowledgeSources.get( k );
		}
		return gs;
	}

	public  TrackedRepertoryGrid[] getAllGrids()
	{
		TrackedRepertoryGrid[] gs = new TrackedRepertoryGrid[ gridCount ];
		int next = 0;
		for ( int k = 0; k < knowledgeSources.size(); k++ )
		{
			if ( knowledgeSources.get( k ) == null )
				Global.error( "Knowledge source number " + k + " was null." );
			else if ( knowledgeSources.get( k ) instanceof TrackedRepertoryGrid )
				gs[ next++ ] = (TrackedRepertoryGrid)knowledgeSources.get( k );
		}
		return gs;
	}
	
	/**
		Locate any graphs that have type labels. Ideally there should be only one hierarchy,
			but I'd rather not get into the one-size-fits-all argument with the ontology group(s).
		@return a (possibly-empty) set of available knowledge source graphs that contain at
			least one type label.
	 */
	public  Graph[] findGraphsWithTypeHierarchy()
	{
		ArrayList result = new ArrayList();
		Graph[] gs = getAllGraphs();
		for ( int k = 0; k < gs.length; k++ )
		{
			Iterator iter = new DeepIterator( gs[ k ], new TypeLabel() );
			if ( iter.hasNext() ) result.add( gs[ k ] );
		}
		return (Graph[])result.toArray( new Graph[ 0 ] );
	}

    /**
        Uses Hub.matchingStrategy to determine which class to use for an instance of a tuple matcher.
        Any valid tuple matcher will work here, but usually the name is chosen from a list in the Preferences window.
        It's assumed that the matcher will be in the "kb" package, unless the name contains a period (".")
     */
    public  AbstractTupleMatcher createCurrentTupleMatcher()
    {
        String className = null;
        AbstractTupleMatcher matcher = null;
        className = Global.matchingStrategy;
        if ( ! className.contains( "." ) ) className = "kb.matching." + className;
        try 
        {
            Class matcherClass = Class.forName( className );
            matcher = (AbstractTupleMatcher)matcherClass.newInstance();
        }
        catch ( ClassNotFoundException cnfe ) { Global.error( "Class " + className + " not found." ); }
        catch ( InstantiationException ie )   { Global.error( "Class " + className + " couldn't be instantiated." ); }
        catch ( IllegalAccessException iae )  { Global.error( "Class " + className + " couldn't be accessed." ); }
        return matcher;
    }

}
