//
//  Craft.java
//  CharGer 2003
//
//  Created by Harry Delugach on Fri Apr 18 2003.
//
package craft;

import charger.Global;
import repgrid.*;
import repgrid.tracks.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

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
	The main starting point for the Conceptual Requirements Acquisition and Formation Tool (CRAFT)
 */	
 
public class Craft {

	public static ArrayList windowList = new ArrayList();
	public static CraftWindow craftWindow = null;
	public static final Color craftPink = new Color( 255, 230, 230 ) ;
	public static final String rgxmlSuffix = ".rgx";
	
	public static JMenuItem backToCraftCmdItem = new JMenuItem( Global.strs( "BackToCraftCmdLabel" ) );
	
        
	
	public Craft()
	{
		craftWindow = new CraftWindow();
		//backToCraftCmdItem.addActionListener( charger.Hub.CharGerMasterFrame );
		//craftWindow.toFront();
	}
	
	/**
		Inform the CRAFT system that there's a new repertory grid window to keep track of.
		@param win the new window, minimally initiatlized.
	 */
	public static void addRGWindow( RGDisplayWindow win )
	{
		if ( ! windowList.contains( win ) ) windowList.add( win );
		Global.refreshWindowMenuList( Craft.craftWindow.windowMenu, win );
	}
	
	/**
		Inform the CRAFT system that a window needs to be forgotten.
		@param win the window to forget. If this window isn't already there, then ignore it.
	 */
	public static void removeRGWindow( RGDisplayWindow win )
	{
		if ( windowList.contains( win) ) 
		{
			windowList.remove( win );
			Global.refreshWindowMenuList( Craft.craftWindow.windowMenu, win );
		}
	}

	
	private static int number = 1;
	public static int getNextNumber() {  return number++; }
	
	public static void createNewRGWindow()
	{
		RGDisplayWindow win = new RGDisplayWindow( new TrackedRepertoryGrid(), null );
		addRGWindow( win );
	}
	
	public TrackedRepertoryGrid testgrid()
	{
		TrackedRepertoryGrid rg = 
			new TrackedRepertoryGrid( "furniture", "has", "characteristics", new RGBooleanValue() );
			//new RepertoryGrid( "furniture", "has", "characteristics", new RGIntegerValueRange( 6 ) );
		rg.addAttribute( "has legs" );
		rg.addAttribute( "used for sitting" );
		rg.addAttribute( "can be carried by one person" );
		rg.addAttribute( "holds people" );
		rg.addAttribute( "is expensive" );
		//for ( int k = 0; k < 20; k++ ) rg.addAttribute( "attr " + k );
		rg.add( "table" );
		rg.add( "chair" );
		rg.add( "desk" );
		rg.add( "bookshelf" );
		rg.add( "lamp" );
		rg.add( "endtable" );
		//for ( int k = 0; k < 10; k++ ) rg.add( "attr " + k );
		
		return rg;
	}
	
		/**
		Query the user for a new graphs folder.
		@param f parent frame for the dialog.
	 */
	public static void queryForGridFolder( JFrame f )
	{
	    File newF = Global.queryForFolder( f, Global.CRAFTGridFolderFile ,
				"Choose any file to use its parent directory for grids" );
	    if ( newF == null )
			return;
	    else 
	    {
                Global.CRAFTGridFolder = newF.getAbsolutePath();
                Global.CRAFTGridFolderFile = new File( Global.CRAFTGridFolder );
	    }
	}
	
	public static void setGridFolderFile( File folder )
	{
            Global.CRAFTGridFolder = folder.getAbsolutePath();
            Global.CRAFTGridFolderFile = new File( Global.CRAFTGridFolder );
	}

	
	public static void say( String s )
	{
            if ( Global.infoOn )
                    System.out.println( "CRAFT: " + s );
	}
	
	/*
		BUGS IDENTIFIED:
		When an editing window is closed, the Craft Window should be refreshed.
		RGDisplayWindow - thisWindowClosing - if user says "cancel" to the save, acts like NO
	 */
	
}
