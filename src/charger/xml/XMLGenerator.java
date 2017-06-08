//
//  XMLGenerator.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 04 2003.
//
package charger.xml;

import charger.*;
import java.util.*;
import java.awt.*;

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
 * Provides some general routines for generating XML, from which sub-classes may
 * derive some benefit. Everything is assumed to be static in this generator.
 */
abstract public class XMLGenerator {


    /**
     * The result of
     * <code>System.getProperty( "line.separator" )</code>
     */
        public static String eol = System.getProperty( "line.separator" );
    /**
     * Helpful in indentation; currently set to two spaces
     */
    public static String tab = "    ";

    /**
     * @return the string <code>&lt;?xml version=\"1.0\"?&gt;</code>
     */
    public static String XMLHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    }

    /**
     * Rewrite its arguments for XML.
     *
     * @param tag
     * @param value free text to be inserted between start and end tags.
     * @param parameters a string of the form <code>... name1="value1" name2="value2"
     * ...</code>
     * @return a string something like
     * <code>&lt;tag name1="value1" name2="value2" &gt; freetext &lt;/tag&gt;</code>
     */
    public static String simpleTaggedString( String tag, String value, String parameters ) {
        String parm = " " + parameters;
        if ( parameters.equals( "" ) ) {
            parm = parameters;
        }
        return startTag( tag + parm ) + quoteForXML( value ) + endTag( tag );
        /*return startTag( tag + parm) +
         "\t" + "<string>" + value + "</string>" + eol +
         endTag( tag );*/
    }

    /**
     * Rewrite its arguments for XML.
     *
     * @param tag
     * @param value free text to be inserted between start and end tags.
     * @return a string something
     * like <code>&lt;tag&gt; freetext &lt;/tag&gt;</code>
     */
    public static String simpleTaggedString( String tag, String value ) {
        return simpleTaggedString( tag, value, "" );
    }

    /**
     * Returns a simple tag.
     *
     * @return the string <code>&lt;tag&gt;</code>
     */
    public static String startTag( String tag ) {
        return "<" + tag + ">";
    } // + eol; }

    /**
     * Returns a simple tag with parameters, without any closing tag.
     *
     * @return a string something
     * like <code>&lt;tag name1="value1" name2="value2"&gt;</code>
     */
    public static String startTag( String tag, String parameters ) {
        String parm = " " + parameters;
        if ( parameters.equals( "" ) ) {
            parm = parameters;
        }
        return startTag( tag + parm );
    }

    /**
     * Returns a tag with parameters, including a closing bracket.
     *
     * @return a string something
     * like <code>&lt;tag name1="value1" name2="value2"/&gt;</code>
     */
    public static String tagWithParms( String tag, String parameters ) {
        String s = startTag( tag, parameters );
        return s.substring( 0, s.length() - 1 ) + "/>";
    }

    /**
     * Returns a simple end tag of the form
     * <code>&lt;/tag&gt;</code>
     */
    public static String endTag( String tag ) {
        return "</" + tag + ">";
    } // + eol; }

    /**
     * Converts certain characters into their XML quoted versions; e.g.,
     * <code>&quot;&lt;&quot</code> becomes
     * <code>"&amp;lt;"</code>. Currently the following are converted: <table
     * width="31%" border="1"> <tr> <td width="18%"><i><b>Character</b></i></td>
     * <td width="82%"><i><b>Reference</b></i></td> <tr> <td width="18%"> &amp;
     * <td width="82%">&amp;amp; <tr> <td width="18%"> &lt; <td
     * width="82%">&amp;lt;</td> <tr> <td width="18%"> &gt; <td
     * width="82%">&amp;gt;</td> <tr> <td width="18%"> &quot; <td
     * width="82%">&amp;quot; <tr> <td width="18%"> &apos; <td
     * width="82%">&amp;apos; </table>
     *
     */
    public static String quoteForXML( String unquoted ) {
        String s = unquoted;
        s = s.replaceAll( "&", "&amp;" );
        s = s.replaceAll( "<", "&lt;" );
        s = s.replaceAll( ">", "&gt;" );
        s = s.replaceAll( "\"", "&quot;" );
        s = s.replaceAll( "'", "&apos;" );
        return s;
    }
}
