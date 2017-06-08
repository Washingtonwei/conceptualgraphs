//
//  SAXHandler.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 04 2003.
//
package repgrid.xml;

import charger.Global;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
import repgrid.*;
import repgrid.tracks.*;
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
 * Used for parsing XML representations of a repertory grid. Based on code and
 * explanation found at <a
 * href="http://www.saxproject.org/?selected=quickstart">SAX's web site</a>.
 * This serves the purpose of the SAXHandler class.
 *
 * @see repgrid.xml.RGXMLGenerator
 */
public class RGXMLParser extends DefaultHandler {

    private boolean traceTags = false;
    private TrackedRepertoryGrid grid = null;
    // the parser strings
    Stack parserStack = new Stack();
    // anything from a label tag
    Stack labelStack = new Stack();
    // anything from a definition tag
    Stack definitionStack = new Stack();
    // a wordnet descriptor stack
    Stack wordnetStack = new Stack();
    // a generic descriptor stack
    Stack genericStack = new Stack();
    RGAttribute currentAttribute = null;
    String attributeLabel = "";
    ArrayList elements = new ArrayList();
    ArrayList attributes = new ArrayList();
    int vectorInsertionPoint = 0;	// where the element/attribute is inserted into its vector
    String genericString = "";	// the string we just processed before an ending tag
    StringBuilder stringCollector = new StringBuilder( "" );
    // used for cell processing
    String valuetype = null;
    int elementID = 0;
    int attributeID = 0;
    // used for header label processing
    String headertype = null;

    public RGXMLParser() {
        super();
    }

    public TrackedRepertoryGrid parseXMLRepertoryGridFile( File f ) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader( new FileInputStream( f ) );
        } catch ( FileNotFoundException fnf ) {
            JOptionPane.showMessageDialog( null, "File not found: " + fnf.getMessage() );
        }
        return parseXMLRepertoryGridFile( reader, f );
    }

    public TrackedRepertoryGrid parseXMLRepertoryGridFile( String filename ) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader( new FileInputStream( filename ) );
        } catch ( FileNotFoundException fnf ) {
            JOptionPane.showMessageDialog( null, "File not found: " + fnf.getMessage() );
        }
        return parseXMLRepertoryGridFile( reader, new File( filename ) );
    }

    public TrackedRepertoryGrid parseXMLRepertoryGridFile( InputStreamReader instream, File f ) {
        grid = new TrackedRepertoryGrid( f );
        charger.Global.info( "ready to parse... grid is " + grid );
        //XMLReader xr = XMLReaderFactory.createXMLReader();
        // note: is tied to a particular XML reader
        XMLReader xr;
        try {
            xr = new XMLFilterImpl( new ParserAdapter( new XMLReaderAdapter() ) );
        } catch ( SAXException ex ) {
            //Logger.getLogger( CGXParser.class.getName() ).log( Level.SEVERE, null, ex );
            Global.error( "Error in parseGGXMLInputStream: " + ex.getMessage() );
            return grid;
        }
//        XMLReader xr = new org.apache.crimson.parser.XMLReaderImpl();

        //xr.setContentHandler(handler);
        //xr.setErrorHandler(handler);	
        xr.setContentHandler( this );
        xr.setErrorHandler( this );

        try {
            xr.parse( new InputSource( instream ) );
        } catch ( IOException ioe ) {
            charger.Global.error( "IO Exception in XMLParser: " + ioe.getMessage() );
        } catch ( SAXException se ) {
            Exception e = se.getException();
            if ( e == null ) {
                e = se;
            }
            charger.Global.error( "Sax Exception in RGXMLParser: " + e.getMessage() );
        }
        return grid;
    }

    public void startDocument() {
        if ( traceTags ) {
            craft.Craft.say( "Start document; grid is " + grid );
        }
        //processingPhase = "starting";
    }

    public void endDocument() {
        //Craft.say("End document");
        if ( !parserStack.empty() && !parserStack.peek().equals( "repertorygrid" ) ) {
            charger.Global.error( "parser problem: tag \"" + parserStack.peek()
                    + "\" was terminated by tag \"" + "repertorygrid" + "\"" );
        }
    }

    public void startElement( String uri, String name,
            String qName, Attributes atts ) {
        if ( traceTags ) {
            if ( "".equals( uri ) ) {
                charger.Global.info( "Start tag: " + qName );
            } else {
                charger.Global.info( "Start tag: {" + uri + "}" + name );
            }
            for ( int k = 0; k < atts.getLength(); k++ ) {
                charger.Global.info( "  parameter " + k + " name is " + atts.getLocalName( k )
                        + "; value is " + atts.getValue( k ) );
            }
        }
        // actual processing goes here
        int id = 0;
        if ( qName.equals( "cell" ) ) {
            try {
                RGValue v = grid.valuetype.copy();
                if ( atts.getValue( "valuetype" ).equals( "boolean" ) ) {
                    ( (RGBooleanValue)v ).setValue( Boolean.valueOf( atts.getValue( "value" ) ) );
                } else if ( atts.getValue( "valuetype" ).equals( "integer" ) ) {
                    ( (RGIntegerValue)v ).setValue( Integer.valueOf( atts.getValue( "value" ) ) );
                }

                int elem = Integer.parseInt( atts.getValue( "element" ) );
                int attr = Integer.parseInt( atts.getValue( "attribute" ) );

                RGCell c = grid.getCell( attr, elem );
                c.setRGValue( v );
            } catch ( NumberFormatException nfe ) {
            } catch ( RGValueException ve ) {
                charger.Global.error( "RGValueException: " + ve.getMessage() );
            }
        }
        if ( qName.equals( "element" ) ) {
            try {
                String val = atts.getValue( "id" );
                //Craft.say( "id is " + id );
                vectorInsertionPoint = Integer.parseInt( atts.getValue( "id" ) );
            } catch ( NumberFormatException nfe ) {
            }
        } else if ( qName.equals( "attribute" ) ) {
            try {
                vectorInsertionPoint = Integer.parseInt( atts.getValue( "id" ) );
            } catch ( NumberFormatException nfe ) {
            }
            grid.addAttribute( "DUMMY DUMMY", vectorInsertionPoint );
            currentAttribute = grid.getAttribute( "DUMMY DUMMY" );		// if we see this, something's wrong! :-)
        } else if ( qName.equals( "headerlabel" ) ) {
            charger.util.ObjectLocator loc = null;
            headertype = atts.getValue( "type" );
            String text = atts.getValue( "text" );
            if ( atts.getValue( "uri" ) != null ) {
                loc = charger.util.ObjectLocator.parseURI( atts.getValue( "uri" ) );
            }
            //craft.Craft.say( "header label type is " + headertype +  "; text is " + text + "; uri is " + atts.getValue( "uri" ) );
            if ( headertype.equals( "element" ) ) {
                grid.setElementLabel( text );
                grid.conceptOneLocator = loc;
                if ( loc != null ) {
                    grid.conceptOne = (charger.obj.Concept)loc.getObject( true );
                }
            } else if ( headertype.equals( "relation" ) ) {
                grid.setRelationLabel( text );
                grid.relationLocator = loc;
                if ( loc != null ) {
                    grid.relation = (charger.obj.GNode)loc.getObject( true );
                }
            } else if ( headertype.equals( "attribute" ) ) {
                grid.setAttributeLabel( text );
                grid.conceptTwoLocator = loc;
                if ( loc != null ) {
                    grid.conceptTwo = (charger.obj.Concept)loc.getObject( true );
                }
            } else if ( headertype.equals( "valuetype" ) ) {
                craft.Craft.say( "found a valuetype" );
                // these are capitalized because they are generated by Object.getClass().getName()
                if ( atts.getValue( "class" ).endsWith( "Boolean" ) ) {
                    grid.setValueType( new RGBooleanValue() );
                } else if ( atts.getValue( "class" ).endsWith( "Integer" ) ) {
                    grid.setValueType( new RGIntegerValueRange( 6 ) );
                }
            }
        } else if ( qName.equals( "wordnet" ) || ( qName.equals( "generic" ) ) ) {
            // this test tells whether we'll need a tracked attribute
            if ( !( currentAttribute instanceof TrackedAttribute ) ) {
                TrackedAttribute ta = new TrackedAttribute( currentAttribute );
                grid.replaceAttribute( currentAttribute, ta );
                currentAttribute = ta;
            }
            if ( qName.equals( "wordnet" ) ) {
                wordnetStack.push( charger.gloss.wn.WordnetTypeDescriptor.getInstanceFromXML( atts ) );
            } else if ( qName.equals( "generic" ) ) {
                genericStack.push( charger.gloss.GenericTypeDescriptor.getInstanceFromXML( atts ) );
            }

        }
        /*else if ( qName.equals( "word" ) )
         {
         // here's where we process any wordnet arguments
         }*/
        stringCollector = new StringBuilder( "" );
        genericString = "";
        parserStack.push( qName );
    }

    public void endElement( String uri, String name, String qName ) {
        if ( traceTags ) {
            if ( "".equals( uri ) ) {
                charger.Global.info( "End tag: " + qName + System.getProperty( "line.separator" ) );
            } else {
                charger.Global.info( "End tag:   {" + uri + "}" + name );
            }
        }

        if ( !parserStack.empty() && !parserStack.peek().equals( qName ) ) {
            charger.Global.error( "parser problem: tag \"" + parserStack.peek()
                    + "\" was terminated by tag \"" + qName + "\"" );
        } else {
            parserStack.pop();
        }
        genericString = stringCollector.toString().trim();
        stringCollector = new StringBuilder( "" );

        if ( qName.equals( "string" ) ) // Not used here
        {
        } else if ( qName.equals( "element" ) ) {
            grid.add( genericString, vectorInsertionPoint );
        } else if ( qName.equals( "attribute" ) ) {
            //grid.addAttribute( attributeLabel, vectorInsertionPoint );
            currentAttribute = grid.getAttribute( attributeLabel );
        } else if ( qName.equals( "label" ) ) {
            labelStack.push( genericString );
            if ( !wordnetStack.isEmpty() ) // a wordnet descriptor label
            {
                ( (charger.gloss.wn.WordnetTypeDescriptor)wordnetStack.peek() ).setLabel( (String)labelStack.pop() );
                genericString = "";
            } else if ( !genericStack.isEmpty() ) {
                ( (charger.gloss.GenericTypeDescriptor)genericStack.peek() ).setLabel( (String)labelStack.pop() );
                genericString = "";
            } else if ( parserStack.peek().equals( "attribute" ) ) // an attribute label
            {
                attributeLabel = (String)labelStack.pop();
                currentAttribute.setLabel( attributeLabel );
                genericString = "";
            } else // must be a term label
            {
                // process a term label here   unfinished
            }
        } else if ( qName.equals( "definition" ) ) {
            //definitionStack.push( genericString );
            if ( !wordnetStack.isEmpty() ) {
                ( (charger.gloss.wn.WordnetTypeDescriptor)wordnetStack.peek() ).setDefinition( genericString );
                //craft.Craft.say( "setting wordnet definition: " + genericString );
                genericString = "";
            } else if ( !genericStack.isEmpty() ) {
                ( (charger.gloss.GenericTypeDescriptor)genericStack.peek() ).setDefinition( genericString );
                //craft.Craft.say( "setting generic definition: " + genericString );
                genericString = "";
            }
        } else if ( qName.equals( "wordnet" ) ) {
            // assumes that the first wordnet start tag caused us to switch to a tracked attribute
            ( (TrackedAttribute)currentAttribute ).setTypeDescriptor(
                    ( (charger.gloss.wn.WordnetTypeDescriptor)wordnetStack.peek() ).getLabel(),
                    (charger.gloss.AbstractTypeDescriptor)wordnetStack.pop() );
        } else if ( qName.equals( "generic" ) ) {
            // assumes that the first generic start tag caused us to switch to a tracked attribute
            ( (TrackedAttribute)currentAttribute ).setTypeDescriptor(
                    ( (charger.gloss.GenericTypeDescriptor)genericStack.peek() ).getLabel(),
                    (charger.gloss.AbstractTypeDescriptor)genericStack.pop() );
        }
    }

    public void characters( char[] ch, int start, int length ) throws SAXException {
        String content = new String( ch, start, length );
        stringCollector.append( content );
        //Craft.say( "  characters: \"" + content + "\"" + System.getProperty( "line.separator" ) );
    }
}
