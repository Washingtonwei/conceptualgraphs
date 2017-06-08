//
//  TypeDescriptor.java
//  CharGer 2003
//
//  Created by Harry Delugach on Thu Jun 12 2003.
//
package charger.gloss;

import charger.*;

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
 * Abstract interface for a type descriptor. A descriptor is CharGer's way of
 * storing a dictionary-style definition. The type descriptor contains the term
 * being described (called its label), a definition string, and a part of speech
 * string. Subclasses may add additional features (such as Wordnet's dictionary
 * pointers). Any GNode in CharGer may have one ore more descriptors associated
 * with it. In order to be parsed, any concrete class's tag name needs to be
 * added to the CGXMLParser. In order to be queried, any concrete class must be
 * handled by the SenseQueryDialog class.
 *
 * @see craft.SenseQueryDialog
 * @see charger.xml.CGXParser
 * @see charger.wn.WordnetTypeDescriptor
 */
abstract public class AbstractTypeDescriptor {

    protected static  String tagName;;
    /**
     * Convenience for the toXML methods
     */
    protected String eol = System.getProperty( "line.separator" );
    /**
     * The actual term to which this descriptor applies
     */
    protected String label = null;
    /**
     * A text definition, possibly including examples, variants, etc.
     */
    protected String definition = null;
    /**
     * The part of speech.
     *
     * @see TypeDescriptor#legalPartsOfSpeech
     */
    protected String partofspeech = null;
    /**
     * the list of available parts of speech. Not really relevant for Wordnet,
     * which has its own reserved four types: "noun", "verb", "adjective", and
     * "adverb". The legal labels are: "noun", "verb", "adjective", "adverb",
     * "preposition", "other";
     */
    public static String[] legalPartsOfSpeech = { "noun", "verb", "adjective", "adverb", "preposition", "other" };
    /**
     * Whether this type descriptor has been tested or analyzed with respect to
     * some dictionary, etc.
     */
    protected boolean validated = false;

    /**
     * Sets the definition of this type descriptor, without validation.
     */
    public void setDefinition( String def ) {
        definition = def;
        //setValidated( false );
    }

    /**
     * Gets the definition string that is being represented by this descriptor.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Gets the part of speech string that is being represented by this
     * descriptor.
     */
    public String getPOS() {
        return partofspeech;
    }

    /**
     * Whether this type descriptor has been validated or not.
     */
    public boolean isValidated() {
        return validated;
    }

    /**
     * Sets the type descriptor's term label.
     *
     * @param lab the term label to be assigned to this descriptor.
     */
    public void setLabel( String lab ) {
        label = lab;
    }

    /**
     * Gets the type descriptor's term label.
     *
     * @return the term label that was assigned to this descriptor.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets whether this type descriptor is validated or not. Sub-classes can
     * override and provide a real validation procedure if the argument is
     * <code>true</code>. Default is to return the value passed in by the caller
     *
     * @param b if <code>true</code>, then perform whatever validation procedure
     * will be required; otherwise just invalidate the descriptor.
     */
    public boolean setValidated( boolean b ) {
        validated = b;
        return isValidated();
    }

    /**
     * Gives the tag name that will be used by XML to represent this descriptor
     */
    static public String getTagName() {
        return tagName;
    }

    /**
     * Translates the descriptor into its self-contained XML version.
     *
     * @param indent For convenience, what is the current indentation prefix?
     * @return a self-contained XML tag, including nested parts if necessary.
     */
    abstract public String toXML( String indent );

    /**
     * Parses the XML parms that go along with the tag in getTagName(). Does not
     * handle the tag itself; it assumes we've already determined that this is
     * the right class to invoke.
     *
     * @param attrs the Attributes passed to org.xml.sax.startElement
     * @return an instance of the descriptor, or null if it can't be formed.
     * @see charger.xml.CGXParser
     */
    public static AbstractTypeDescriptor getInstanceFromXML( org.xml.sax.Attributes attrs ) {
        return null;
    }

    /**
     * Shorten the definition so it won't overrun windows and dialogs. If
     * there's a ";" or ":" symbol in the string, it and everything after it is
     * deleted. Then if the string is still longer than 80 characters, it is
     * truncated.
     *
     * @return a possibly shortened form of the definition.
     */
    public String getTrimmedString() {
        int cutOff = definition.length();
        if ( definition.indexOf( ";" ) != -1 ) {
            cutOff = definition.indexOf( ";" );
        } else if ( definition.indexOf( ":" ) != -1 ) {
            cutOff = definition.indexOf( ":" );
        }
        cutOff = Math.min( cutOff, 80 );
        return partofspeech + " - " + definition.substring( 0, cutOff );
    }

    /**
     * Compares two type descriptors for equality. They are considered equal if
     * the part of speech is equal and the labels agree (ignored upper/lower
     * case).
     *
     * @param d Descriptor to be compared.
     * @return true if the POS and label are equal.
     */
    public boolean equals( AbstractTypeDescriptor d ) {
        charger.Global.info( "comparing descriptors: LHS: " + label + " " + partofspeech + " RHS: "
                + d.getLabel() + " " + d.getPOS() );
        if ( !partofspeech.equals( d.getPOS() ) ) {
            return false;
        }
        if ( !label.equalsIgnoreCase( d.getLabel() ) ) {
            return false;
        }
        if ( !definition.equalsIgnoreCase( d.getDefinition() ) ) {
            return false;
        }
        return true;
    }
    //public String toString() { return this.toString(); }
}
