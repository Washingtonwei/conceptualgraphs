/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package charger.obj;

import cgif.parser.javacc.CGIFParser;
import cgif.parser.javacc.ParseException;
import charger.Global;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstraction of a CG referent, encapsulating all the various options.
 * The actual parsing of the referent is delegated to the CGIFParser, which
 * has a "referent" method that populates this object correctly.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class Referent {
    
    /** The forall symbol that can serve as a referent */
    public final static String forall = "@every";
    public final static String dist = "@dist";
    public final static String coll = "@coll";
    
    private  InputStream is; // = new ByteArrayInputStream( "".getBytes() );
    private    CGIFParser parser; // = new CGIFParser( is );


    /** The referent string as entered (un-edited, un-parsed) by a user or external system. */
    private String asEntered = null;
    /** anything preceded by a star or question mark */
    private String variable = null; 
    
    /** If the referent is an actual individual marker */
    private String marker = null;
    
    /** The variable reference for CGIF. Might be a generated name for some
     * generic referents.
     */
    private String cgifVariableReference = null;
    
    /** Whether this referent denotes a set or not */
    private boolean isASet = false;
    
    /** If a set, these are the members of the referent set */
    private ArrayList<Referent> setMembers = new ArrayList<Referent>();
    
    /** If a set, this is where its cardinality is kept. Note that
     * it may be a string, since it might be of the form {  }@M
     */
    private String cardinality = null;
    
    /** If referent denotes an actual number, here's where it's kept. */
    private Double number = null;
    
    public Referent() {
//        this( "" );
    }
    
    public Referent( String s ) {
        asEntered = s;
//        if ( asEntered != null && asEntered.length() > 0 ) 
//            parseReferentString();
    }
    
    /** For debugging purposes to describe what the referent is. */
    public String explain( ) {
        StringBuffer s = new StringBuffer( "ref: " + asEntered + " ");
        
        if ( isASet ) {
            s.append( " is a set with members: ");
            for ( Referent ref : setMembers ) {
                s.append(ref.explain() );
            }
            if ( cardinality != null ) {
                s.append( "cardinality: " + cardinality + " " );
            }
        } else {
            if ( variable != null ) {
                s.append( "variable \"" + variable + "\" ");
            }
            if ( marker != null ) {
                s.append( "marker \"" + marker + "\" ");
            }
        }
       return s.toString();
    }

    /**
     * Return the original string referent as entered by a user or external
     * system.
     */
    public String getReferentString() {
        if ( asEntered == null )
            return "";
        return asEntered;
    }

    /**
     * Set the referent original string but do not parse
     */
    public void setReferentString( String ref ) {
        setReferentString( ref, false );
    }

    /** Set the referent original string and optionally parse for all possible elements
     * @param ref The referent to be set
     * @param parse Whether to parse it or not (used primarily by the parser itself and this class
     */
    public void setReferentString( String ref, boolean parse ) {
        asEntered = ref;
        if ( parse ) parseReferentString();
    }

    /** Get the variable portion of a referent, if present. Include a leading ? or * */
    public String getVariable() {
        return variable;
    }
    
    /** 
     * See the referent as if it were a bound or unbound variable, with prefix ? or *  
     */
    public String getAsVariable( boolean bound ) {
        if ( getVariable() == null ) return null;
        if ( bound )
                    // Make sure first char is *
            return "*" + variable.substring( 1 );
        else
            return "?" + variable.substring( 1 );
    }

    public void setVariable( String variable ) {
        this.variable = variable;
    }

    public String getCardinality() {
        return cardinality;
    }
    
    /**
     * Parses the cardinality as a string and returns an integer if one is found.
     * @return the integer value of the cardinality string; -1 if not a number.
     */
    public int getCardinalityAsNumber() {
        try {
            int cardinality = Integer.parseInt( getCardinality() );
            return cardinality;
            
        } catch ( NumberFormatException ex ) {
            return -1;
        }
    }

    public void setCardinality( String cardinality ) {
        this.cardinality = cardinality;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker( String marker ) {
        this.marker = marker;
    }
    
    
    
    public void addSetMember( Referent ref ) {
        setMembers.add( ref );
        isASet = true;
    }

    public String getCgifVariableReference() {
        makeCGIFVariableReference();
        return cgifVariableReference;
    }

    public void setCgifVariableReference( String cgifVariableReference ) {
        this.cgifVariableReference = cgifVariableReference;
    }

    public Double getNumber() {
        return number;
    }

    public void setNumber( Double number ) {
        this.number = number;
    }
    
    private void makeCGIFVariableReference() {
        if ( asEntered == null ) {
            // generate a variable reference
        } else if ( asEntered.startsWith( "*" ) || asEntered.startsWith( "?" ))
            cgifVariableReference = new String( asEntered );
        else
            cgifVariableReference = new String( asEntered );
    }
    
    /** Consider a list of referents to be set members. */
    public void setMemberList( ArrayList<Referent> members ) {
        for ( Referent ref : members ) {
            addSetMember( ref );
        }
    }
    
    protected void copyFromReferent( Referent ref ) {
        this.asEntered = ref.asEntered;
        this.cardinality = ref.cardinality;
        this.cgifVariableReference = ref.cgifVariableReference;
        this.isASet = ref.isASet;
        this.marker = ref.marker;
        this.setMembers = (ArrayList<Referent>)ref.setMembers.clone();
        this.number = ref.number;
    }
    
    private void parseReferentString()  {
        if ( asEntered != null  && asEntered.length() > 0 )
        try {
            parser = new CGIFParser( new ByteArrayInputStream( asEntered.getBytes() ) );
            Referent test = parser.referent( );
            copyFromReferent( test );
        } catch ( ParseException ex ) {
            Global.warning( "Referent \"" + asEntered + "\" not recognized.");
        }

    }
    
//    /** Determine whether the referent is a set, whether it's bound or un-bound, whether a variable or not, etc. */
//    private void parseReferentString() {
//        if ( asEntered.contains( "{") ) {
//            isASet = true;
//        }
//        if ( asEntered.contains( "@")) {
//            cardinality = asEntered.substring( asEntered.indexOf( "@") + 1);
//        }
//        if ( asEntered.contains( "*")) {
////            cgifVariableReference
////            variable = asEntered.substring( asEntered.indexOf("*") + 1,  )
//        }
//    }
    
}
