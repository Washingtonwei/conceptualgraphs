/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb;

import charger.obj.*;

import kb.hierarchy.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements a CG knowledge base consisting of a concept hierarchy, a relation
 * hierarchy, and a marker set.
 * @author Harry S. Delugach
 */
public class KnowledgeBase {
    private String name = null;
    private TypeHierarchy conceptHierarchy;
    private TypeHierarchy relationHierarchy;
    private MarkerSet markerSet = null;

    /*
    By Bingyang Wei
    First of all, charger.obj.Concept (graphic shape) and concept (logic) are different things in this comment.
    equivalentConceptHashStore stores the coref info about a graph:

    key is string "type:referent", representing the only concept (logic)
    value is a collection of graphic shapes (charger.obj.Concept objects) that represent the same concept (logic).

    The charger.obj.Concept class is more of a graphic sense: we can have many charger.obj.Concept objects with the same type and referent in the same graph, but there exits only one concept,
    in other words, the equivalentConceptHashStore's length is the number of different concepts in the current graph, but one concept may appear at different context many times, but they all are one concept.
    For example:
    Person:tom -> Person:a, Person:b, Person:tom
    Person:jim -> Person:d
    Person:tim -> Person:e
     */
    private HashMap<String, ArrayList<Concept>> equivalentConceptHashStore = new HashMap<String, ArrayList<Concept>>(10);
    
    /**
     * Creates an "empty" knowledge base.
     * The concept hierarchy has top "T" and bottom "_t_".
     * The relation hierarchy has top "link" and bottom "_t_".
     * To change these, use methods from the TypeHierarchy.
     * see TypeHierarchy#setTop(TypeHierarchyNode)
     * see TypeHierarchy#setBottom(TypeHierarchyNode)
     */
    public KnowledgeBase( String newname ) {
        
        init( newname );
    }
    
    protected void init( String n ) {
        setName( n );
        conceptHierarchy = new TypeHierarchy( "Concept Hierarchy" , TypeHierarchy.KindOfHierarchy.Type);
        conceptHierarchy.getTop().setPosetKey( "T");
        conceptHierarchy.getBottom().setPosetKey( "_t_");
        
        relationHierarchy = new TypeHierarchy( "Relation Hierarchy", TypeHierarchy.KindOfHierarchy.Relation);
        relationHierarchy.getTop().setPosetKey( "link" );
        relationHierarchy.getBottom().setPosetKey( "_t_");
        
        markerSet = new MarkerSet();
        
        conceptHierarchy.setName( n + " " + conceptHierarchy.getName() );
        relationHierarchy.setName( n + " " + relationHierarchy.getName() );
    }
    
    
    public String showConceptTypeHierarchy(  ) {
        return getConceptTypeHierarchy().showHierarchy();
    }

    public String showRelationTypeHierarchy( ) {
        return getRelationTypeHierarchy().showHierarchy();
    }

    public TypeHierarchy getRelationTypeHierarchy() {
        return relationHierarchy;
    }

    public TypeHierarchy getConceptTypeHierarchy() {
        return conceptHierarchy;
    }

    public MarkerSet getMarkerSet() {
        return markerSet;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
    
    
    
    public void clear() {
        init( name );
    }
    
    /**
     * Method to "add" an object to this KnowledgeBase. The semantics may vary depending on what kind 
     * of object it is. Currently only TypeLabel, RelationLabel, Concept and GenSpecLink are supported.
     * @param go 
     */
    public void commit( Object go ) throws KBException {
        if      ( go instanceof TypeLabel ) ((TypeLabel)go).commitToKnowledgeBase( this );
        else if ( go instanceof Concept ) ((Concept)go).commitToKnowledgeBase( this );
        else if ( go instanceof GenSpecLink ) ((GenSpecLink)go).commitToKnowledgeBase( this );
//        else if ( go instanceof Relation ) ((Relation)go).commitToKnowledgeBase( this );
        else if ( go instanceof RelationLabel ) ((RelationLabel)go).commitToKnowledgeBase( this );
        else {
            // Ignore any other kind of objects
        }
    }

    /**
     * Method to "delete" an object from this KnowledgeBase. The semantics may
     * vary depending on what kind of object it is. Currently only TypeLabel,
     * RelationLabel Concept and GenSpecLink are supported.
     *
     * @param go
     */
    public void unCommit( Object go ) throws KBException {
        if ( go instanceof TypeLabel ) {
            ( (TypeLabel)go ).unCommitFromKnowledgeBase( this );
        } else if ( go instanceof Concept ) {
            ( (Concept)go ).unCommitFromKnowledgeBase( this );
        } else if ( go instanceof GenSpecLink ) {
            ( (GenSpecLink)go ).unCommitFromKnowledgeBase( this );
        } //        else if ( go instanceof Relation ) ((Relation)go).unCommitFromKnowledgeBase( this );
        else if ( go instanceof RelationLabel ) {
            ( (RelationLabel)go ).unCommitFromKnowledgeBase( this );
        } else {
            // Ignore any other kind of objects
        }
    }

}
