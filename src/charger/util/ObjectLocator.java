//
//  ObjectLocator.java
//  CharGer 2003
//
//  Created by Harry Delugach on Fri Jun 27 2003.
//
package charger.util;

import charger.obj.*;
import charger.*;

import java.io.*;
import java.rmi.server.UID;
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
 * Object containing the information needed to uniquely identify and access an
 * object in the CharGer system. Contains no dynamic addresses, serialization,
 * etc. Objects currently accessible include GNodes and RepertoryGrids. <p> An
 * object locator consists of a (local) file name, a fully qualified class name
 * and a unique ident. The file name may be null. <p>In the future, this should
 * probably be handled as a fully-qualified URI, but it works as an adequate
 * substitute.
 *
 * @see charger.util.WindowManager
 */
public class ObjectLocator {

    /**
     * absolute file (if any) where object is located
     */
    private File _sourceFile = null;
    /**
     * unique ID for identification
     */
    private GraphObjectID _ID = GraphObjectID.zero;
    /**
     * type of object being located
     */
    private Class _class;

    /**
     * Creates a new object locator with the given constituents.
     */
    public ObjectLocator( File f, Class c, GraphObjectID id ) {
        initialize( f, c, id );
    }

    /**
     * Creates a locator with the given file and ident, and class
     * <code>java.lang.Object</code>
     *
     * @param f file where this object is currently stored.
     * @param id an id that is unique across all locatable objects on the
     * current host.
     */
    public ObjectLocator( File f, GraphObjectID id ) {
        initialize( f, Object.class, id );
    }

    /**
     * Creates a locator with the given file and ident, and class
     * <code>java.lang.Object</code>
     */
    public ObjectLocator( String filename, GraphObjectID id ) {
        File f = new File( filename );
        initialize( f, Object.class, id );
    }

    private void initialize( File f, Class c, GraphObjectID id ) {
        _sourceFile = f;
        _ID = id;
        _class = c;
    }

    /**
     * Gets the file portion of the locator.
     *
     * @return source file of the object sought; <code>null</code> if it's not
     * in a file.
     */
    public File getFile() {
        return _sourceFile;
    }

    /**
     * Sets the file portion of the locator
     */
    public void setFile( File f ) {
        _sourceFile = f;
    }

    /**
     * Gets the file portion of the locator.
     *
     * @return ident of the object sought; 0 (zero) if it has no ident.
     */
    public GraphObjectID getID() {
        return _ID;
    }

    public void setID( GraphObjectID id ) {
        _ID = id;
    }

    /**
     * Find the object, wherever it is, opening its window if necessary.
     */
    public static Object getObject( File f, GraphObjectID id, Class c, boolean makeAvailable ) {
        ManagedWindow mw = WindowManager.getWindowFromFile( f );
        if ( mw == null ) {
            charger.Global.info( "window for " + f.getAbsolutePath() + " not found." );
            if ( !makeAvailable ) {
                return null;		// for now
            } else {
                // Open the file here, and assign its window to mw
                if ( Util.getFileExtension( f.getAbsolutePath() ).equalsIgnoreCase( Global.ChargerFileExtension ) ) {
                    String name = Global.openGraphInNewFrame( f.getAbsolutePath() );
                    mw = WindowManager.getWindowFromFile( f );
                } else {
                    charger.Global.warning( "Don't know how to open file " + f.getAbsolutePath() );
                }
            }
        }
        charger.Global.info( "window for " + f.getAbsolutePath() + " now open." );
        if ( getRelevantClass( c ).equals( GNode.class ) ) {
            if ( mw instanceof EditFrame ) {
                return ( (EditFrame)mw ).TheGraph.findByID( id );
            }
        }
        return null;
    }

    /**
     * Find the object that this locator is intended to identify.
     *
     * @param makeAvailable Whether to open up the resource and make the object
     * available. If false, and the the object is not currently accessbiel, then
     * return null.
     * @return the object in question; otherwise null.
     */
    public Object getObject( boolean makeAvailable ) {
        // look to see if the sourcefile is available
        return getObject( _sourceFile, _ID, _class, makeAvailable );
    }

    public boolean isAccessible() {
        // look to see if the sourcefile and object ident is accessible
        return true;
    }

    private static Class getRelevantClass( Class c ) {
        if ( c.equals( Object.class ) ) {
            return Object.class;
        }
        if ( c.equals( GNode.class ) ) {
            return GNode.class;
        }
        return getRelevantClass( c.getSuperclass() );
    }

    /**
     */
    public String toXML( String indent ) {
        String eol = System.getProperty( "line.separator" );
        StringBuilder s = new StringBuilder( "" );

        s.append( indent + "<locator " );
        if ( _sourceFile != null ) {
            s.append( " file=\"" + _sourceFile.getAbsolutePath() + "\"" );
        }
        if ( ! _ID.equals( GraphObjectID.zero) ) {
            s.append( " id=\"" + _ID + "\"" );
        }
        if ( _class != null ) {
            s.append( " class=" + _class.getName() + "\"" );
        }
        s.append( "/>" );

        return s.toString();
    }

    /**
     * Creates a URI to identify this object. The URI looks like
     * <code>file:/Volumes/hsd/Research/CGs/catonmat.cgx#charger.obj.Concept-12345667</code>
     * where the URI fragment (after the "#") is the class type and the ID
     * number.
     */
    public String toURI() {
        try {
            java.net.URI uri = new java.net.URI( "file", _sourceFile.getAbsolutePath(), _class.getName() + "-" + _ID );
            return uri.toString();
        } catch ( java.net.URISyntaxException e ) {
            charger.Global.warning( "URI Syntax Exception from Object Locator: " + e.getMessage() );
            StringBuilder s = new StringBuilder( "" );
            s.append( "file:" );
            if ( _sourceFile != null ) {
                s.append( _sourceFile.getAbsolutePath() );
            }
            s.append( "#" );
            s.append( _class.getName() );
            s.append( "-" );
            s.append( _ID  );
            charger.Global.info( "URI created is " + s.toString() );
            return s.toString();
        }
    }

    /**
     * Construct an object locator from its URI string.
     *
     * @see java.net.URI
     * @see #toURI
     */
    public static ObjectLocator parseURI( String uri ) {
        try {
            java.net.URI theURI = new java.net.URI( uri );
            if ( !theURI.getScheme().equals( "file" ) ) {
                return null;
            }
            String __sourceFile = theURI.getPath();
            String _frag = theURI.getRawFragment();
            StringTokenizer toks = new StringTokenizer( _frag, "-" );
            Class __class = Class.forName( toks.nextToken() );
            GraphObjectID __ID =  new GraphObjectID( toks.nextToken() );
            return new ObjectLocator( new File( __sourceFile ), __class, __ID );
        } catch ( IllegalArgumentException e ) {
            charger.Global.warning( "Illegal argument: Object locator tried to parse \""
                    + uri + "\". " + e.getMessage() );
            return null;
        } catch ( java.net.URISyntaxException se ) {
            charger.Global.warning( "URI Syntax: Object locator tried to parse \""
                    + uri + "\". " + se.getMessage() );
            return null;
        } catch ( NullPointerException ne ) {
            charger.Global.warning( "Null pointer: Object locator tried to parse \""
                    + uri + "\". " + ne.getMessage() );
            return null;
        } catch ( ClassNotFoundException ce ) {
            charger.Global.warning( "Class not found: Object locator tried to parse \"" + uri + "\". " + ce.getMessage() );
            return null;
        }
    }
}
