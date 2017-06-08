package charger.db;

import charger.*;
import charger.exception.*;
import charger.obj.*;
import charger.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

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
	Embodies accessibility to a tab-delimited text database.
	The database is first "activated" -- i.e., the file is located and verified,
	its name stored in the ActiveDatabases table in the Hub. 
	There is a separate TextDatabase instance associated with each real database.
	Once "activated" a database file is associated with the same TextDatabase instance
	for the duration of the session, no matter how many graphs refer to it, or how many 
	concepts in a single graph refer to it.
	@author Harry S. Delugach ( delugach@uah.edu ) Copyright reserved 1998-2014 by Harry S. Delugach
 */
public class TextDatabase implements CGDatabase {
		/**
			Once the database file is opened, this class 
			leaves the file open; it's up to its user to close it with closeDB().
		*/
	String currentfilename = null;
	String[] DBValidNames = null;
	
	int primaryKey = -1;	// index in DBValidNames of the primary key
	FileInputStream fis;
	BufferedReader in = null;
	
		// indicates whether the file is ready to read the first real data record
	boolean openAndReady = false;
	boolean Attached = false;

	/*
		When creating a database, enter its identity into Hub.AttachedDatabases.
		Open the file and leave it open for the duration of this session.
	 */
	public TextDatabase( String fname ) throws CGFileException
	{
		// Determine which database is indicated by the input Database string
		// Attempt to locate a file with the name s
		// If file not found, then null everything out
		// If file is found, then read first header line of file, get field names
		try {
			open( fname );
		} catch ( CGFileException e2 ) {
			throw new CGFileException( "Could not open " + fname ); 
		}
		currentfilename = fname;
	}
	
	/**
		@return current file name of the database that's attached to me.
	 */
	public String getName()
	{
		return currentfilename;
	}
	
	
	/**
		Initializes the file "in" for reading. Only called once per file per session.
		Sets up access to the database file, loads up the valid names from 1st line of file.
		@param fname path-qualified name of file to be opened.
	 */
	public boolean open( String fname ) throws CGFileException
	{
		try {
	   		fis = new FileInputStream( fname );
			in   = new BufferedReader(new InputStreamReader( fis ));
	   		if (in == null) 
		   		throw new CGFileException( "Could not open " + fname ); 
		} catch ( IOException e ) 
		{ 
			throw new CGFileException( "Could not open " + fname );
		}
		currentfilename = fname;
		DBValidNames = getFields();
		openAndReady = true;
		return true;
	}
	
	/**
		Rewinds the database file so that the first line of read data can be read.
		Closes the actual file and re-opens it (since we can't be certain the file supports
		a reset). Checks for any errors in case the file has been modified or moved since 
		it was last opened.
	 */
	public void resetDB()
	{
		try {
			in.close();
	   		fis = new FileInputStream( currentfilename );
			in   = new BufferedReader(new InputStreamReader( fis ));
	   		if (in == null) 
		   		throw new IOException( "Could not open " + currentfilename ); 
		} catch ( IOException e ) { Global.info("reset failed."); } 
		try {
			DBValidNames = getFields();		// read first header line
		} catch ( CGFileException e ) { Global.info("reset failed to load fields."); }  ;
		openAndReady = true;
	}
	
	/**
		Close the database file.
	 */
	public void closeDB()
	{
		if ( in != null )
			try {
				in.close();
			} catch ( IOException e ) { }
	}
	
	/**
		@return true if the database file is currently open; false otherwise.
	 */
	public boolean isOpened() 
	{
		if ( in == null ) return false;
		return true;
	}

	/** @return true if the database file is open and ready to read data. */
	public boolean isReady()
	{
		return openAndReady;
	}
	
	/** @return list of strings in this file which are valid field or column names */
	public String[] getValidNames()
	{
		return DBValidNames;
	}
	
	/**
	
	 */
	public void setPrimaryKey( int keyIndex )
	{
		primaryKey = keyIndex;	
	}

	/**
	
	 */
	public void setPrimaryKey( String keyType )
	{
		primaryKey = getFieldPosition( keyType );	
	}

	/**
	
	 */
	public int getPrimaryKey()
	{
		return( primaryKey );	
	}
	
	public String getPrimaryKeyString()
	{
		if ( primaryKey < 0 ) return "";
		return ( DBValidNames[ primaryKey ] );
	}


	/** set up the database file for reading data */
	public void makeReady()
	{
		if ( openAndReady ) return;
		resetDB();
		return;
	}

	/**
		@return returns the next line of the database, from wherever we last read a line.
			null if at the end of the file
			Check end of file here; it doesn't work!!!
	 */
	public String[] getFields() throws CGFileException
	{
		String[] nameholder = null;
		try {
			if ( ! isOpened() ) { open( currentfilename ); }
	   		String theLine = null;
   			theLine = in.readLine();
   			if ( theLine == null ) return null;	// we're at end of file
   			String field = null;
   			StringTokenizer toks = new StringTokenizer( theLine, "\u0009" );
   			nameholder = new String[ toks.countTokens() ];
   			int FieldNum = 0;
			while ( toks.hasMoreTokens() ) {
				field = toks.nextToken();
						// Global.info( "field is " + field) ;			
				nameholder[ FieldNum++ ] = field;
			}
			if ( openAndReady ) openAndReady = false;
	   	} catch ( IOException e1 ) { 
	   			throw new CGFileException( "A file input error occurred on file \"" + e1.getMessage() + "\"." );
			}	
		return nameholder;
	}

	
	// find the field name that matches the key concept's referent
	// if there is no such field name, then abort
	// otherwise note the position on the header line (1st, 2nd, etc.)
	
	// 

	/**
		@param Field the text representation of a database field name
		@return array index of which column the field refers to; -1 if not found
	 */
	public int getFieldPosition( String Field ) {
		if ( Field == null ) return -1;
						// Global.info( "checking " + Field );
			// showRecordLine( DBValidNames, "DBValid names in getfieldposition" );
		for ( int FieldNum = 0; FieldNum < DBValidNames.length; FieldNum++ ) {
			if ( DBValidNames[ FieldNum ].equalsIgnoreCase( Field ) ) return FieldNum;
				// Global.info( "in loop " + DBValidNames[ FieldNum ] );
		}
		return -1;
	}
	
	/**
		Searches the database file for the appropriate value; the "guts" of a database lookup.
		@param keytype The field name used as a key; e.g., "Employee"
		@param keyvalue The key's value; e.g., "John Smith"
		@param targettype The field name (column) whose value is being sought; e.g., "Phone Number"
		@return targetvalue column's value that matches the keytype's keyvalue; e.g., "(804) 555-1212"
	 */
	public String doLookup( String keytype, String keyvalue, String targettype )
	{
				// Global.info( "doLookup: key type is " + keytype + ", targettype is " + targettype );
		int keyColumnNumber = getFieldPosition( keytype );
		if ( keyColumnNumber < 0 ) return null;
		int targetColumnNumber = getFieldPosition( targettype );
		if ( targetColumnNumber < 0 ) return null;
		int max; 
		if ( targetColumnNumber > keyColumnNumber ) max = targetColumnNumber;
		else max = keyColumnNumber;
					// Global.info( "key column is " + keyColumnNumber + ", target col is " + targetColumnNumber );
		try {
			makeReady();
			String[] holder = getFields();
			while ( holder != null /* && holder.length == DBValidNames.length */ ) {
						// the line read may not have a full set of values
						// if line is too short to include either a key or value, then skip
					//showRecordLine( holder, "doLookup: next line in file" );
				if ( holder.length >= max ) {
						// Global.info( "line has " + holder.length + " values." );
						// for ( int k = 0; k < holder.length; k++ )
						//		Global.info( "   holder[" + k + "] is " + holder[k] );
					if ( holder[ keyColumnNumber ].equals( keyvalue ) )
					{
						return holder[ targetColumnNumber ];
					}
				}
				holder = getFields();
			}
		} catch ( CGFileException fe ) { }
		return null;
	}

	private void showRecordLine( String[] holder, String s )
	{
		Global.info( "in show Record Line " + s );
		for ( int k = 0; k < holder.length; k++ )
		{
			Global.info( "column " + k + " = " + holder[k] );
		}
	}
	
	/**
		Creates a new editing window with a graph already created for accessing this database.
		@param key index into the DBValidNames array; <code>-1</code> if there is no key.
		@return reference to the new window
	 */
	public EditFrame setupGraph( int key ) throws CGException
	{
		boolean hasKey = ( key >= 0 );
		if ( currentfilename == null ) 
			throw new CGException ("Database not yet initialized." );
		int xseparation = 40;
		int yseparation = 60;
		Graph g = new Graph( null );
		Concept dbconcept, keyconcept = null, c;
		Actor a;
		
		dbconcept = new Concept();
		dbconcept.setTypeLabel( "Database" );
		dbconcept.setReferent( Util.getSimpleFilename( currentfilename ), false );
		Point2D.Double dbpoint = new Point2D.Double( dbconcept.getDim().width/2 + 15, 20 );
		dbconcept.setCenter( dbpoint );
		g.insertObject( dbconcept );
		
		if ( hasKey )
		{
			keyconcept = new Concept();
			keyconcept.setTypeLabel( DBValidNames[ primaryKey ] );
			Point2D.Double keypoint = 
				new Point2D.Double( dbconcept.getCenter().x + 
							dbconcept.getDim().width/2 + 2*xseparation + keyconcept.getDim().width/2, 20 );
			keyconcept.setCenter( keypoint );
			g.insertObject( keyconcept );
		}
		
		Point2D.Double apoint = (Point2D.Double)( dbpoint.clone() );
		apoint.setLocation( apoint.x, apoint.y + yseparation );
		Point2D.Double cpoint = (Point2D.Double)( dbpoint.clone() );
		int lastwid = 0;
		for ( int numFields = 0; numFields < DBValidNames.length; numFields++ )
		{
			if ( numFields != primaryKey )	// if no primary key, then primary key is always negative
			{
				a = new Actor();
				c = new Concept();
				c.setTextLabel( DBValidNames[numFields] );	// so we'll know its size
				apoint.setLocation( apoint.x + c.getDim().width/2, apoint.y );
				cpoint.setLocation( apoint );


				a.setCenter( apoint );
				a.setTextLabel( "lookup" );
				g.insertObject( a );
				apoint.setLocation( apoint.x + c.getDim().width/2 + xseparation, apoint.y );

				Arrow ge = new Arrow( dbconcept, a );
				g.insertObject( ge ); 
				if ( hasKey ) 
				{
					ge = new Arrow( keyconcept, a );
					g.insertObject( ge );
				}
				cpoint.setLocation( cpoint.x, cpoint.y + yseparation );
				c.setCenter( cpoint );
				g.insertObject( c );
				ge = new Arrow( a, c );
				g.insertObject( ge );
			}
		}
			    // ooops!!! need to fix this!! 
                File dummy = new File("Untitled");
		
		EditFrame ef = new EditFrame( dummy, g, false );
		if ( Global.enableEditFrameThreads ) new Thread( Global.EditFrameThreadGroup, ef ).start();
		//ef.attachGraphToFrame( currentfilename, g, true );

		return ef;
	}
}