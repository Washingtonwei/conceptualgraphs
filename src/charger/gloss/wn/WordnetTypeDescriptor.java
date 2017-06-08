//
//  WordnetTypeDescriptor.java
//  CharGer 2003
//
//  Created by Harry Delugach on Thu Jun 12 2003.
//
package charger.gloss.wn;

import charger.gloss.GenericTypeDescriptor;
import charger.gloss.AbstractTypeDescriptor;
import net.didion.jwnl.data.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

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
 * Encapsulates a wordnet synonym set (synset) for the purposes of keeping track
 * in CharGer. Really just a wrapper for net.didion.jwnl.data.Synset
 */
public class WordnetTypeDescriptor extends charger.gloss.AbstractTypeDescriptor {

    protected static String tagName = "wordnet-descriptor";

    Synset synset = null;

    private long _offset = 0;

    private double version = 0.0;       //  something like 1.7 or 2.0

    /**
     * Create a wordnet type descriptor from the given synset. Attempts to see
     * if the synset's contents represent an actual synset in Wordnet; if they
     * do not, then set <code>validate = false</code>.
     *
     * @param s a filled-in synset.
     */
    public WordnetTypeDescriptor( Synset s ) {
        synset = s;
        partofspeech = s.getPOS().getLabel();
        _offset = s.getOffset();
        setValidated( true );
        if ( isValidated() ) {
            definition = synset.getGloss();
        }
    }

    /**
     * @param posString one of "verb" "noun" "adjective" "adverb"
     * @param offset a possible offset into the corresponding POS file Returns a
     * descriptor containing the given sense. If the pos and offset pair do not
     * result in a valid wordnet descriptor, either because they are not found
     * or because wordnet itself is not available, then the string is used and
     * the descriptor is marked as invalid.
     */
    public WordnetTypeDescriptor( String posString, long offset ) {
        //charger.Global.info( "entering wordnet descriptor with pos " + posString + " and offset " + offset );
        partofspeech = posString;
        _offset = offset;
        POS pos = WordnetManager.getPOSForLabel( partofspeech );
        try {
            synset = WordnetManager.getSynset( pos, _offset );
        } catch ( Exception e ) {
            setValidated( false );
        }
        if ( synset == null ) {
            setValidated( false );
        } else {
            //setValidated( true );
            validated = true;		// 
            definition = synset.getGloss();
        }
        //charger.Global.info( "leaving wordnet descriptor with validated = " + validated );
    }

    /**
     * Sets the synset for this descriptor, without performing any validation
     */
    public void setSynset( Synset s ) {
        synset = s;
    }

    /**
     * Gets the synset for this descriptor, without performing any validation
     */
    public Synset getSynset() {
        return synset;
    }

    /**
     * Lets clients know that this descriptor will have the tag name
     * <code>"wordnet"</code> if it's needed.
     *
     * @return string <code>"wordnet"</code>
     */
    public static String getTagName() {
        return tagName;
    }

    /**
     * Lets clients know that this descriptor will have the tag name
     * <code>"wordnet"</code> if it's needed.
     *
     * @return string <code>"wordnet"</code>
     */
    public double getVersion() {
        return version;
    }

    /**
     * Compares two wordnet descriptors for equality. Two wordnet descriptors
     * are considered equal if their labels are equal and they have the same pos
     * and offset.
     *
     * @param d The descriptor to be compared.
     * @return true if the labels, pos and offsets are all equal.
     * @see charger.obj.TypeDescriptor#equals
     */
    public boolean equals( WordnetTypeDescriptor d ) {
        craft.Craft.say( "using wordnet type descriptor's equal" );
        if ( !super.equals( d ) ) {
            return false;
        } else if ( _offset == d._offset ) {
            return true;
        }
        return false;
    }

    /**
     * Returns a Wordnet descriptor as a tag in XML form, including the start
     * and end tags.
     *
     * @return parameter list, e.g.,
     * <code>&lt;wordnet&gt; version="2.0" pos="noun" offset="1789046"</code>,
     * etc.
     */
    public String toXML( String indent ) {
        String tab = "";
        if ( indent.length() > 0 ) {
            tab = indent.substring( 0, 1 );
        }
        //if ( isValidated() )
        if ( synset != null ) {
            return indent + "<" + getTagName() + " version=\"" + WordnetManager.getActiveVersion() + "\" pos=\"" + synset.getPOS().getLabel() + "\" "
                    + "offset=\"" + synset.getOffset() + "\">" + eol
                    + indent + tab + "<label>" + label + "</label>" + eol
                    + indent + tab + "<definition>" + definition + "</definition>" + eol
                    + indent + "</" + getTagName() + ">";
        } else {
            return indent + "<" + getTagName() + " pos=\"" + partofspeech + "\" "
                    + "offset=\"" + 0L + "\">" + eol
                    + indent + tab + "<label>" + label + "</label>" + eol
                    + indent + tab + "<definition>" + definition + "</definition>" + eol
                    + indent + "</" + getTagName() + ">";
        }
        /*try
         {
         if ( synset != null ) return "";
         return "pos=\"" + 
         synset.getPOS().getLabel() + 
         "\" offset=\"" + synset.getOffset() + "\"";
         } catch ( net.didion.jwnl.JWNLException je )
         { JOptionPane.showMessageDialog( null, "JWNL Exception: " + je.getMessage() ); }
         return null;*/
    }

    /**
     * @param b If <code>false</code> just invalidate and return; otherwise try
     * to see if the current pos and offset actually correspond to a Wordnet
     * synset. If they do, mark it as valid, otherwise, mark it as invalid.
     * @return <code>true</code> if we were able to validate it, otherwise
     * false.
     */
    public boolean setValidated( boolean b ) {
        if ( !b ) {
            validated = false;
            return false;
        } else {
            if ( !WordnetManager.isWordnetAvailable() ) // disabling check for availability of wordnet for now
            {
                validated = false;
                return false;
            } else {
							// HERE is a problem!!!!  -- 
                // should use local pos and offset??
                Synset s = WordnetManager.getSynset(
                        WordnetManager.getPOSForLabel( partofspeech ), _offset );
                if ( s != null ) {
                    validated = true;
                    return true;
                } else {
                    validated = false;
                    return false;
                }
            }
        }
    }

    /**
     * Parses the XML parms corresponding to a <code>&lt;wordnet&gt;</code> tag.
     * Does not handle the tag itself; it assumes we've already determined that
     * this is the right class to invoke.
     *
     * @param attrs the Attributes passed to org.xml.sax.startElement
     * @see charger.xml.CGXParser
     */
    public static charger.gloss.AbstractTypeDescriptor getInstanceFromXML( org.xml.sax.Attributes attrs ) {
        //charger.Global.info( "wordnet's getinstance, attrs are " + attrs );
        if ( ( attrs.getValue( "pos" ) == null ) ) {
            return null;
        }
        String partofspeech = attrs.getValue( "pos" );
        //POS pos = WordnetManager.getPOSForLabel( partofspeech );
        long offset = 0;
        if ( attrs.getValue( "offset" ) == null ) {
            offset = 0;
        } else {
            offset = Long.parseLong( attrs.getValue( "offset" ) );
        }
        WordnetTypeDescriptor d = new WordnetTypeDescriptor( partofspeech, offset );
        if ( !( offset > 0 ) ) {
            d.setValidated( false );
        }
        if ( ( attrs.getValue( "version" ) == null ) ) {
            d.setValidated( false );
        } else if ( Double.parseDouble( attrs.getValue( "version" ) ) != WordnetManager.getActiveVersion() ) {
            d.setValidated( false );
        }
        return (charger.gloss.AbstractTypeDescriptor)d;
    }
    
        public static AbstractTypeDescriptor getInstanceFromXMLDOM( Element elem ) {
                    // Handle any attributes first
        NamedNodeMap map = elem.getAttributes();
        String pos = null;
        if ( map.getNamedItem( "pos" ) != null ) {
             pos = map.getNamedItem( "pos" ).getNodeValue();
        }

        String version = null;
        if ( map.getNamedItem( "version" ) != null ) {
             version = map.getNamedItem( "version" ).getNodeValue();
        }
        String offset = null;
        if ( map.getNamedItem( "offset" ) != null ) {
             offset = map.getNamedItem( "offset" ).getNodeValue();
        }
        
        WordnetTypeDescriptor d = new WordnetTypeDescriptor( pos, Long.parseLong(offset) );
        d.version = Double.parseDouble( version );
        
                // Handle nested elements next
        NodeList elems = elem.getElementsByTagName( "label" );
        if ( elems != null && elems.getLength() > 0 ) {
            String label = elems.item( 0 ).getTextContent();
            d.label = label;
        }
         elems = elem.getElementsByTagName( "definition" );
        if ( elems != null && elems.getLength() > 0 ) {
            String definition = elems.item( 0 ).getTextContent();
            d.definition = definition;
        }

        //craft.Craft.say( " getting instance from XML; descr is " + descr.toString() );
        //descr.definition = attrs.getValue( "definition" );
        return d;
    }


    public String toString() {
        if ( WordnetManager.isWordnetAvailable() && this.isValidated() ) //return synset.getGloss();
        {
            return WordnetManager.getInstance().getTrimmedGloss( synset, 90, true, false );
        } else {
            return getTrimmedString();
        }
    }
}
