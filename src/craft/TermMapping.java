//
//  TermMapping.java
//  CharGer 2003
//
//  Created by Harry Delugach on Tue Jul 08 2003.
//
package craft;

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
 * Stores the strings needed to map some terms to useful alternate terms. So far
 * only English is supported (volunteers welcome!).
 */
public class TermMapping {

    // stores original-alternative pairs

    java.util.Properties alternatives = new java.util.Properties();

    // stores prefix connectors for various relations
    java.util.Properties prefixes = new java.util.Properties();

    // stores suffix connectors for various relations
    java.util.Properties suffixes = new java.util.Properties();

    // stores plural versions of various singular terms
    java.util.Properties plurals = new java.util.Properties();

    // stores alternative versions of various type labels
    java.util.Properties types = new java.util.Properties();

    /**
     * @param language an ISO abbreviation for the language desired. So far,
     * only "en" is supported. Volunteers are welcome for other languages.
     */
    public TermMapping( String language ) {
        super();
        if ( language.equals( "en" ) ) {
            setupEN();
        } else {
            setupEN();
        }
    }

    /**
     * Looks up alternative words for purposes of creating natural language.
     *
     * @param key some term whose alternative form is sought.
     * @param cardinality the quantity of the term being considered, especially
     * useful for distinguishing singular from plural.
     * @return alternative form if there is one; otherwise the <code>key</code>
     * itself is returned.
     */
    public String alt( String key, int cardinality ) {
        String prop = alternatives.getProperty( key );
        if ( prop == null ) {
            return cardinalize( key, cardinality );
        } else {
            return cardinalize( prop, cardinality );
        }
    }

    /**
     * Looks up alternative words for purposes of creating natural language.
     * Assumes cardinality of 1.
     *
     * @param key some term whose alternative form is sought.
     * @return alternative form if there is one; otherwise the <code>key</code>
     * itself is returned.
     */
    public String alt( String key ) {
        return alt( key, 1 );
    }

    /**
     * Determines a word or phrase to follow a given relation. For example, for
     * the relation "parent" might produce the word "of". For convenience, blank
     * padding is provided for the returned word/phrase.
     *
     * @param relationlabel some relation whose appropriate suffix term is
     * sought.
     * @return blank-padded suffix for that relation.
     */
    public String suffix( String relationlabel, boolean isActor ) {
        String prop = suffixes.getProperty( relationlabel );
        if ( prop == null ) {
            return " of ";
        } else {
            return prop;
        }
    }

    /**
     * Determines a word or phrase to precede a given relation. For example, for
     * the relation "parent" might produce the word "is the". For convenience,
     * blank padding is provided for the returned word/phrase.
     *
     * @param relationlabel some relation whose appropriate suffix term is
     * sought.
     * @return blank-padded suffix for that relation.
     */
    public String prefix( String relationlabel, boolean isActor ) {
        String prop = prefixes.getProperty( relationlabel );
        if ( prop == null ) {
            if ( isActor ) {
                return " results from ";
            } else {
                return " is the ";
            }
        } else {
            return prop;
        }
    }

    /**
     * Determines an alternative type label for certain labels. For example, for
     * the label "T" might produce the word "THING".
     *
     * @param type some label whose appropriate type label is sought.
     * @return label for the type.
     */
    public String type( String type ) {
        String prop = types.getProperty( type );
        if ( prop == null ) {
            return type;
        } else {
            return prop;
        }
    }

    /**
     * Handles singular/plural issues for terms.
     *
     * @param s the term to be examined
     * @param cardinality the number of things to which the term applies.
     * Normally a 1 means singular, a number greater than one means plural.
     */
    public String cardinalize( String s, int cardinality ) {
        if ( cardinality <= 1 ) {
            return s;
        } else {
            if ( ( s.charAt( 0 ) == ' ' ) && ( s.charAt( s.length() - 1 ) == ' ' ) ) {
                return " " + cardinalize( s.substring( 1, s.length() - 1 ), cardinality ) + " ";
            }
            String p = plurals.getProperty( s );
            if ( p != null ) {
                return p;
            } else {
                char c = s.charAt( s.length() - 1 );	// last character determines pluralization
                if ( "hjosvxz".indexOf( c ) == -1 ) {
                    return s + "s";		// not one of the "exceptions"
                } else {
                    return s + "es";
                }
            }
        }
    }

    private void setupEN() {
        setupCombinations();
        setupAlternatives();
        setupSuffixes();
        setupPrefixes();
        setupPlurals();
        setupTypes();
    }

    private void putCombination( String original, String prefix, String alternative, String suffix ) {
        prefixes.put( original, " " + prefix + " " );
        alternatives.put( original, alternative );
        suffixes.put( original, " " + suffix + " " );
    }

    private void setupAlternatives() {
        // alternatives
        alternatives.put( "agnt", "agent" );
        alternatives.put( "attr", "attribute" );
        alternatives.put( "char", "characteristic" );
    }

    private void setupSuffixes() {

    }

    private void setupPrefixes() {

    }

    private void setupPlurals() {
        plurals.put( "is", "are" );
        plurals.put( "is the", "are the" );
        plurals.put( "has", "have" );
    }

    private void setupTypes() {
        types.put( "T", "THING" );
        types.put( "null", "NOTHING" );
    }

    /**
     * For relations, these allow more natural sounding phrases to be
     * constructed.
     */
    private void setupCombinations() {
        putCombination( "affect", "is", "affected", "by" );
        putCombination( "attribute", "is an", "attribute", "of" );
        putCombination( "characteristic", "is a", "characteristic", "of" );
        putCombination( "copy", "results from", "copying", "" );
        putCombination( "dbfind", "results from", "looking up", "" );
        putCombination( "exp", "results from", "raising to the power of e", "" );
        putCombination( "has", "is", "possessed", "by" );
        putCombination( "incorporate", "results from", "incorporating", "" );
        putCombination( "link", "is", "linked", "to" );
        putCombination( "lookup", "results from", "looking up", "" );
        putCombination( "neg", "is", "not true", "" );
        putCombination( "own", "is the", "owner", "of" );
        putCombination( "plus", "results from", "adding", "" );
        putCombination( "produce", "is", "produced", "by" );
        putCombination( "subtract", "results from", "subtracting", "" );
        putCombination( "minus", "results from", "subtracting", "" );
    }

}
