//
//  Reporter.java
//  CharGer 2003
//
//  Created by Harry Delugach on Tue Jul 08 2003.
//
package craft;

import charger.*;
import charger.obj.*;
import charger.util.*;

import javax.swing.*;
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
 * Manages the reporting of the acquired knowledge, relying on some linguistic
 * tricks and such.
 */
public class Reporter {

    static GenericTextFrame summary = null;
    static public TermMapping tmap = new TermMapping( "en" );
    static ArrayList visited = new ArrayList();

    public static void displaySummary( JFrame owner ) {
        Craft.say( "display summary" );
        if ( summary == null ) {
            summary = new GenericTextFrame(
                    owner,
                    "Knowledge summary", "Knowledge summary for " + Global.CRAFTGridFolderFile.getAbsolutePath(),
                    getKnowledgeSummary(),
                    new java.io.File( Global.CRAFTGridFolderFile.getAbsolutePath(),
                    "Knowledge-Summary.txt" ) );
        } else {
            summary.setTheText( getKnowledgeSummary() );
        }
        summary.setVisible( true );
    }
    public static String eol = System.getProperty( "line.separator" );

    public static String getKnowledgeSummary() {
        StringBuilder s = new StringBuilder( "" );
        /*javax.swing.text.rtf.RTFEditorKit kit = new javax.swing.text.rtf.RTFEditorKit();
         Action[] actions = kit.getActions();
         for ( int k = 0; k < actions.length; k++ )
         {
         s.append( actions[ k ].toString() + eol );
         s.append( ((String)actions[ k ].getValue( Action.NAME )) + ": " );
         s.append( ((String)actions[ k ].getValue( Action.LONG_DESCRIPTION )) + eol );
         }*/
        startVisiting();
        Iterator iterall = null;
        ArrayList all = null;

        all = kb.ConceptManager.getAll( new Graph() );
        iterall = all.iterator();
        while ( iterall.hasNext() ) {
            Graph g = (Graph)iterall.next();
            s.append( natLang( g, true ) );
        }

        all = kb.ConceptManager.getAll( new Relation() );
        iterall = all.iterator();
        while ( iterall.hasNext() ) {
            Relation r = (Relation)iterall.next();
            s.append( natLang( r, true ) );
        }

        all = kb.ConceptManager.getAll( new Actor() );
        iterall = all.iterator();
        while ( iterall.hasNext() ) {
            Actor a = (Actor)iterall.next();
            s.append( natLang( a, true ) );
        }

        all = kb.ConceptManager.getAll( new Concept() );
        iterall = all.iterator();
        while ( iterall.hasNext() ) {
            Concept c = (Concept)iterall.next();
            if ( !visited.contains( c ) ) {
                s.insert( 0, natLang( c, true ) );
            }
        }

        all = kb.ConceptManager.getAll( new TypeLabel() );
        iterall = all.iterator();
        while ( iterall.hasNext() ) {
            TypeLabel t = (TypeLabel)iterall.next();
            //if ( ! visited.contains( c ) )  // don't worry about whether we've visited them before
            s.insert( 0, natLang( t, true ) );
        }

        //s.insert( 0, "Knowledge summary:" + eol );
        return s.toString();
    }

    public static void startVisiting() {
        visited.clear();
    }

    public static String natLang( Graph g, boolean includeDefinitions ) {
        //if ( visited.contains( g ) ) return "";
        visited.add( g );

        String type = g.getTypeLabel();
        String ref = g.getReferent();

        StringBuilder s = new StringBuilder( "" );
        //if ( !  type.equalsIgnoreCase( "Proposition" ) || ! ref.equals( "" ) ) 
        {
            if ( ref.equals( "" ) ) {
                s.append( Util.a_or_an( type ) );
            } else {
                s.append( "the " + type );
            }
            if ( !ref.equals( "" ) ) {
                s.append( formatRef( ref ) );
            }
            if ( includeDefinitions && g.getTypeDescriptor() != null ) {
                s.append( " [" + g.getTypeDescriptor().getTrimmedString() + "]" );
            }

            boolean firstthrough = true;
            Iterator concepts = new ShallowIterator( g, GraphObject.Kind.CONCEPT_OR_GRAPH );
            while ( concepts.hasNext() ) {
                if ( firstthrough ) {
                    s.append( eol + " consists of " );
                }
                Concept c = (Concept)concepts.next();
                if ( !concepts.hasNext() ) {
                    s.append( " and " );
                } else if ( !firstthrough ) {
                    s.append( ", " );
                }
                firstthrough = false;
                //s.append( c.getTextLabel() );
                s.append( natLang( c, false ) );
            }
        }
        //else
        {
        }
        return s.toString() + eol;
    }

    private static String formatRef( String referent ) {
        String prefix = "";
        if ( Character.isDigit( referent.charAt( 0 ) ) || referent.charAt( 0 ) == '-' ) {
            prefix = "amount";
        }
        if ( referent.charAt( 0 ) == '*' && referent.length() > 1 ) {
            prefix = "called";
        }
        if ( prefix.length() > 0 ) {
            return " " + prefix + " " + stripRef( referent );
        } else if ( stripRef( referent ).length() > 0 ) {
            return " \"" + stripRef( referent ) + "\"";
        } else {
            return "";
        }
    }

    private static String stripRef( String referent ) {
        if ( referent.startsWith( "*" ) ) {
            return referent.substring( 1, referent.length() );
        } else {
            return referent;
        }
    }

    ;

	/**
		Prepare a natural language rendition of the given concept.
		@param c The concept to be rendered in natural language
		@param includeDefinitions whether to include definitions for this term if is has one
	 */
	public static String natLang( Concept c, boolean includeDefinitions ) {
        //if ( visited.contains( c ) ) return "";

        visited.add( c );
        String type = c.getTypeLabel();
        String ref = c.getReferent();

        StringBuilder s = new StringBuilder( "" );
        if ( ref.equals( "" ) ) {
            s.append( Util.a_or_an( type ) );
        } else {
            s.append( "the " + type );
        }
        if ( !ref.equals( "" ) ) {
            s.append( formatRef( ref ) );  //" called " + stripRef( ref ) );
        }
        if ( includeDefinitions && c.getTypeDescriptor() != null ) {
            s.append( " [" + c.getTypeDescriptor().getTrimmedString() + "]" );
        }
        return s.toString();
    }

    /**
     * Prepare a natural language rendition of the given type label, including
     * its supertype(s).
     *
     * @param t The type label to be rendered.
     * @param includeDefinitions whether to include definitions for terms if
     * they have one
     */
    public static String natLang( TypeLabel t, boolean includeDefinitions ) {
        if ( t.getTextLabel().equalsIgnoreCase( "t" ) ) {
            return "";
        }
        StringBuilder s = new StringBuilder( "" );
        s.append( tmap.type( t.getTextLabel() ) );
        if ( includeDefinitions && t.getTypeDescriptor() != null ) {
            s.append( " [" + t.getTypeDescriptor().getTrimmedString() + "]" + eol );
        }
        //s.append( eol );
        ArrayList ges = t.getLinkedNodes( GEdge.Direction.TO );
        if ( ges.size() == 0 ) {
            s.append( " is a class" + eol );
        } else {
            for ( int k = 0; k < ges.size(); k++ ) {
                s.append( " is a sub-class of " + tmap.type( ( (GNode)ges.get( k ) ).getTextLabel() ) + eol );
            }
        }
        return s.toString() + eol;
    }

    /**
     * Prepare a natural language rendition of the given relation, including its
     * concepts.
     *
     * @param r The relation around which this sentence will be arranged
     * @param includeDefinitions whether to include definitions for terms if
     * they have one
     */
    public static String natLang( GNode r, boolean includeDefinitions ) {
        if ( !( r instanceof Relation ) && !( r instanceof Actor ) ) {
            return "";
        }
        //if ( visited.contains( r ) ) return "";
        visited.add( r );

        String type = r.getTypeLabel();
        int nodenum = 0;
        StringBuilder s = new StringBuilder( "" );

        // do the output concepts first
        ArrayList outputs = r.getLinkedNodes( GEdge.Direction.TO );
        //Craft.say( "there are " + outputs.size() + " output concepts for relation " + r.getTextLabel() );
        for ( nodenum = 0; nodenum < outputs.size(); nodenum++ ) {
            s.append( natLang( (Concept)outputs.get( nodenum ), includeDefinitions ) );
            //s.append( " {" + outputs.get( nodenum ) + "}" );
            if ( nodenum != outputs.size() - 1 ) {
                s.append( " and " );
            }
            s.append( eol );
        }
        if ( outputs.size() > 0 ) {
            s.append( Reporter.tmap.cardinalize( Reporter.tmap.prefix( type, r instanceof Actor ), outputs.size() ) );
        }

        s.append( Reporter.tmap.alt( type, outputs.size() ) );
        if ( r.getTypeDescriptor() != null ) {
            s.append( " [" + r.getTypeDescriptor().getTrimmedString() + "]" );
        }

        // do the input concepts
        ArrayList inputs = r.getLinkedNodes( GEdge.Direction.FROM );
        //Craft.say( "there are " + inputs.size() + " input concepts for relation " + r.getTextLabel() );

        if ( inputs.size() > 0 ) {
            s.append( Reporter.tmap.suffix( type, r instanceof Actor ) );
        }
        for ( nodenum = 0; nodenum < inputs.size(); nodenum++ ) {
            s.append( natLang( (Concept)inputs.get( nodenum ), includeDefinitions ) );
            if ( nodenum != inputs.size() - 1 ) {
                s.append( " and " );
            }
            s.append( eol );
        }

        return s.toString() + eol;
    }
}
