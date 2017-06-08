//
//  GenericTypeDescriptor.java
//  CharGer 2003
//
//  Created by Harry Delugach on Thu Jun 12 2003.
//
package charger.gloss;

import charger.gloss.AbstractTypeDescriptor;
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
 * Encapsulation of a user-defined ("free-form") type descriptor.
 */
public class GenericTypeDescriptor extends AbstractTypeDescriptor {

    public static String tagName = "generic-descriptor";
    

    /**
     * Create an empty type descriptor, without any content.
     */
    public GenericTypeDescriptor() {
        validated = false;
    }

    /**
     * Create a new generic type descriptor, with the given contents .
     *
     * @param lab the term to which this descriptor applies.
     * @param pos the part of speech as a string
     * @param def The definition part of this descriptor, possibly including
     * exemplars, usage, etc.
     * @see TypeDescriptor#legalPartsOfSpeech
     */
    public GenericTypeDescriptor( String lab, String pos, String def ) {
        label = lab;
        validated = false;
        partofspeech = pos;
        definition = def;
    }

    /**
     * Gives the tag name that will be used by XML to represent this descriptor
     */
    public static String getTagName() {
        return tagName;
    }

    /**
     * Translate the descriptor into XML.
     */
    public String toXML( String indent ) {
        String tab = "";
        if ( indent.length() > 0 ) {
            tab = indent.substring( 0, 1 );
        }
        return indent + "<" + getTagName() + " pos=\"" + partofspeech + "\">" + eol
                + indent + tab + "<label>" + label + "</label>" + eol
                + indent + tab + "<definition>" + definition + "</definition>" + eol
                + indent + "</" + getTagName() + ">";
    }

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

        if ( attrs.getValue( "pos" ) == null ) {
            return null;
        }
        GenericTypeDescriptor descr = new GenericTypeDescriptor();
        descr.partofspeech = attrs.getValue( "pos" );
        //craft.Craft.say( " getting instance from XML; descr is " + descr.toString() );
        //descr.definition = attrs.getValue( "definition" );
        return descr;
    }

    public static AbstractTypeDescriptor getInstanceFromXMLDOM( Element elem ) {
        GenericTypeDescriptor descr = new GenericTypeDescriptor();
                    // Handle any attributes first
        NamedNodeMap map = elem.getAttributes();
        String pos = null;
        if ( map.getNamedItem( "pos" ) != null ) {
             pos = map.getNamedItem( "pos" ).getNodeValue();
        }
        descr.partofspeech = pos;
                // Handle nested elements next
        NodeList elems = elem.getElementsByTagName( "label" );
        if ( elems != null && elems.getLength() > 0 ) {
            String label = elems.item( 0 ).getTextContent();
            descr.label = label;
        }
         elems = elem.getElementsByTagName( "definition" );
        if ( elems != null && elems.getLength() > 0 ) {
            String definition = elems.item( 0 ).getTextContent();
            descr.definition = definition;
        }

        //craft.Craft.say( " getting instance from XML; descr is " + descr.toString() );
        //descr.definition = attrs.getValue( "definition" );
        return descr;
    }

    public String toString() {
        return partofspeech + " - " + definition;
    }
}
