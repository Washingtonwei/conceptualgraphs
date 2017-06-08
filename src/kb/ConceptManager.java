//
//  kb.ConceptManager.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 11 2003.
//
package kb;

import charger.gloss.AbstractTypeDescriptor;
import charger.gloss.wn.WordnetTypeDescriptor;
import charger.*;
import charger.EditingChangeState.EditChange;
import charger.obj.*;
import craft.*;


import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;

// These are the Wordnet glue classes

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;

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
 * Manages the interface between CRAFT and the conceptual graphs of CharGer.
 * Responsible for locating and organizing concepts from CharGer graphs that
 * might be needed by CRAFT.
 */
public class ConceptManager {

    static JTable conceptTable = null;
    //static TermMapping tmap = new TermMapping( "en" );
    static int col = 0;
    public static final int COL_SENTENCE_LABEL = col++;
    public static final int COL_CONCEPT_LABEL_1 = col++;
    public static final int COL_RELATION_LABEL = col++;
    public static final int COL_CONCEPT_LABEL_2 = col++;
    public static final int COL_CONCEPT_1 = col++;
    public static final int COL_RELATION = col++;
    public static final int COL_CONCEPT_2 = col++;
    public static final int COL_SUBGRAPH = col++;

    /**
     * Which concepts are to be included in the relation table. <TABLE>
     * <tr><td>ALL <td> all concepts in open graphs will be returned
     * <tr><td>GENERIC_ONLY <td>only generic concepts in open graphs will be
     * returned </TABLE>
     */
    public enum ConceptsToInclude {

        ALL, GENERIC_ONLY
    }
    public static ArrayList conceptTypeList = new ArrayList();

    /**
     * Gather all concepts - same as getConceptTable( ALL )
     */
    public static JTable getConceptTable() {
        return getConceptTable( ConceptsToInclude.GENERIC_ONLY );
    }
    public static TypeLabel top = null;

    /**
     * A triple is a set of concept-relation-concept tuples.
     *
     * @param selector whether to gather all concepts or just generic ones<ul>
     * <li>ALL - for all concepts <li>GENERIC_ONLY - for just concepts without
     * referents </ul>
     * @see #getConceptTable
     * @see #getBinaryRelationTuples
     */
    public static JTable getBinaryTupleTable( ConceptsToInclude selector ) {
        TableModel readonlymodel = null;

        ArrayList<BinaryTuple> tuples = getBinaryRelationTuples( selector );
        // convert array list of lists to array of arrays
        Object[][] tableValues = new Object[ tuples.size() ][];

        int row = 0;
        for ( BinaryTuple tuple : tuples ) {
            ArrayList list = makeTableEntryArrayList( tuple.concept1, tuple.relation, tuple.concept2, selector );
            tableValues[ row++] = list.toArray();
        }
        // tableValues now holds a set of arrays, not array lists, but it's still an arraylist itself, to be converted via toArray

        if ( Global.CRAFTuseOnlyBinaryRelationsinCraft ) // TODO - change get..Tuples and getTupleColumnLabels back to Vector because that's what's needed.
        {
            readonlymodel = new DefaultTableModel( tableValues, BinaryTuple.getTupleColumnLabels().toArray() ) {
//            readonlymodel = new DefaultTableModel( binaryRelationTuples, tupleColumnLabels ) {
                public boolean isCellEditable( int rowIndex, int columnIndex ) {
                    return false;
                }
            };
        } else {
            Vector allRelationTuples = new Vector( getAllRelationTuples( selector ) );
//            readonlymodel = new DefaultTableModel( getAllRelationTuples( selector ), kb.BinaryTuple.getTupleColumnLabels().toArray() ) {
//                public boolean isCellEditable( int rowIndex, int columnIndex ) {
//                    return false;
//                }
//            };
        }
        JTable tripleTable = new JTable( readonlymodel );
        return tripleTable;
    }

    /**
     * A triple is a set of concept-relation-concept tuples.
     *
     * @param selector whether to gather all concepts or just generic ones<ul>
     * <li>ALL - for all concepts <li>GENERIC_ONLY - for just concepts without
     * referents </ul>
     * @see #getConceptTable
     * @see #getBinaryRelationTuples
     */
    public static TableModel getBinaryTupleModel( ConceptsToInclude selector ) {
        TableModel readonlymodel = new DefaultTableModel();

        ArrayList<BinaryTuple> tuples = null;
        Object[][] tableValues;
        // tableValues now holds a set of arrays, not array lists, but it's still an arraylist itself, to be converted via toArray

        if ( Global.CRAFTuseOnlyBinaryRelationsinCraft ) {
            tuples = getBinaryRelationTuples( selector );
            // convert array list of lists to array of arrays
        } else {
            tuples = getAllRelationTuples( selector );
            // convert array list of lists to array of arrays
        }

        tableValues = new Object[ tuples.size() ][];

        int row = 0;
        for ( BinaryTuple tuple : tuples ) {
            ArrayList list = makeTableEntryArrayList( tuple.concept1, tuple.relation, tuple.concept2, selector );
            tableValues[ row++] = list.toArray();
        }

        readonlymodel = new DefaultTableModel( tableValues, BinaryTuple.getTupleColumnLabels().toArray() ) {
//            readonlymodel = new DefaultTableModel( binaryRelationTuples, tupleColumnLabels ) {
            public boolean isCellEditable( int rowIndex, int columnIndex ) {
                return false;
            }
        };

        return readonlymodel;

    }

    /**
     * Gather all concepts according to the selector.
     *
     * @param selector tells which collection of concepts to gather: one of
     * GENERIC_ONLY or ALL
     * @see #getAllConcepts
     */
    public static JTable getConceptTable( ConceptsToInclude selector ) {
        TableModel readonlymodel = null;
        new DefaultTableModel( getAllConcepts( selector ), getConceptColumnLabels() ) {
            public boolean isCellEditable( int rowIndex, int columnIndex ) {
                return false;
            }
        };

        JTable conceptTable = new JTable( readonlymodel );
        return conceptTable;
    }

    /**
     *
     */
    public static Vector getConceptColumnLabels() {
        Vector v = new Vector( 3 );
        v.add( "Label" );
        v.add( "Graph Title" );
        v.add( "Object" );
        return v;
    }

    /**
     * Finds all instances of the given kind of object in all available graphs.
     *
     * @param pattern An empty instance indicating the class (or superclass) of
     * the objects to be collected.
     * @return A list of the objects desired. Any filtering has to be done
     * afterward (yes, it's inefficient!).
     */
    public static ArrayList getAll( GraphObject pattern ) {
        ArrayList all = new ArrayList();
        Iterator graphiter = Global.editFrameList.values().iterator();
        while ( graphiter.hasNext() ) {
            EditFrame ef = (EditFrame)graphiter.next();
            Graph g = ef.TheGraph;
            //craft.Craft.say( "graph g is " + g + "\n" + g.getBriefSummary() );
            Iterator iter = new DeepIterator( g, pattern );
            while ( iter.hasNext() ) {
                GraphObject go = (GraphObject)iter.next();
                all.add( go );
            }
        }
        return all;
    }

    /**
     * Prepares a vector of (concept label, filename, concept object ) from all
     * open graphs.
     *
     * @param selector one of GENERIC_ONLY or ALL
     * @return a vector, where each component of the vector is a nested vector
     * containing the concept label, ownerframe title (if any) and concept
     * object.
     */
    public static Vector getAllConcepts( ConceptsToInclude selector ) {
        Vector nameGraphIDvect = new Vector();
        //craft.Craft.say( "graph list has " + Hub.graphList.size() + " elements." );
        Iterator graphiter = Global.editFrameList.values().iterator();

        while ( graphiter.hasNext() ) {
            EditFrame ef = (EditFrame)graphiter.next();
            Graph g = ef.TheGraph;
            //craft.Craft.say( "graph g is " + g + "\n" + g.getBriefSummary() );
            Iterator iter = new DeepIterator( g, GraphObject.Kind.CONCEPT_OR_GRAPH );
            while ( iter.hasNext() ) {
                ArrayList v = new ArrayList( 2 );
                Concept c = (Concept)iter.next();
                Craft.say( "concept " + c.getTextLabel() + " accessed." );
                if ( selector == ConceptsToInclude.ALL || ( selector == ConceptsToInclude.GENERIC_ONLY && c.getReferent().equals( "" ) ) ) {
                    Craft.say( "concept " + c.getTextLabel() + " included." );
                    v.add( c.getTextLabel() );
                    v.add( g.getOwnerFrame().getTitle() );
                    v.add( c );
                    nameGraphIDvect.add( v );
                    //for ( int k = 0; k < v.size(); k++ ) 
                    //craft.Craft.say( "v " + k + " has label " + v.get( k ) );
                }
            }
        }
        Craft.say( nameGraphIDvect.size() + " concepts were gathered." );
        return nameGraphIDvect;
        // gathers all the concepts from open graphs
    }

    /**
     * Gathers together all the binary relation tuples in a given graph.
     *
     * @param g The charger graph whose relations are to be examined.
     * @param selector Which concepts to gather.
     * @return a vector of Table Entry vectors
     * @see #makeTableEntryArrayList
     */
    public static ArrayList<BinaryTuple> getBinaryRelationTuples( Graph g, ConceptsToInclude selector ) {
        ArrayList<BinaryTuple> tuples = new ArrayList<BinaryTuple>();
        DeepIterator iter = new DeepIterator( g );
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
            BinaryTuple bt = null;
            if ( go instanceof Relation ) {
                Concept c1 = (Concept)( (Relation)go ).getLinkedNodes( GEdge.Direction.FROM ).get( 0 );
                Concept c2 = (Concept)( (Relation)go ).getLinkedNodes( GEdge.Direction.TO ).get( 0 );
                if ( selector == ConceptsToInclude.ALL
                        || ( selector == ConceptsToInclude.GENERIC_ONLY
                        && ( c1.getReferent().equals( "" ) || c2.getReferent().equals( "" ) ) ) ) {
                    bt = new BinaryTuple( c1, (Relation)go, c2 );
                    tuples.add( bt );
                }
            }
        }
        return tuples;
    }

    /**
     * Prepares a list each of whose entries is a (nested) liat as described in
     * makeTableEntryArrayList. Tuples are obtained from every visible graph.
     *
     * @param selector Which concepts to gather.
     * @see #getConceptTable
     * @see #makeTableEntryArrayList
     */
    public static ArrayList<BinaryTuple> getBinaryRelationTuples( ConceptsToInclude selector ) {
        ArrayList<BinaryTuple> tupleArrayList = new ArrayList<BinaryTuple>();

        // for each open graph G, match P to G using the matching scheme
        Iterator graphiter = Global.editFrameList.values().iterator();

        while ( graphiter.hasNext() ) {
            EditFrame ef = (EditFrame)graphiter.next();
            // Note use of "add" not "addAll" because it returns a list of lists, one list for each graph
            tupleArrayList.addAll( getBinaryRelationTuples( ef.TheGraph, selector ) );
        }
        return tupleArrayList;
    }

    /**
     * Prepares a list each of whose entries is a (nested) arraylist as
     * described in makeTableEntryArrayList. Tuples are obtained from every
     * visible graph.
     *
     * @param selector Which concepts to gather.
     * @see #getConceptTable
     * @see #makeTableEntryArrayList
     */
    public static ArrayList<BinaryTuple> getAllRelationTuples( ConceptsToInclude selector ) {
        ArrayList<BinaryTuple> tupleList = new ArrayList<BinaryTuple>();

        // for each open graph G, match P to G using the matching scheme
        //Iterator graphiter = Hub.editFrameList.values().iterator();

        Graph[] allGraphs = Global.knowledgeManager.getAllGraphs();
        // for every available graph
        for ( int gnum = 0; gnum < allGraphs.length; gnum++ ) {
            Graph g = allGraphs[ gnum];
            {
                // for every relation node in the graph
                Iterator nodeiter = new DeepIterator( g, GraphObject.Kind.GNODE );
                while ( nodeiter.hasNext() ) {
                    GNode gn = (GNode)nodeiter.next();
                    if ( gn instanceof Relation || gn instanceof Actor ) {
                        //craft.Craft.say( " found a relation/actor " + gn.getTextLabel() );
                        GNode rel = gn;
                        GEdge[] edges = (GEdge[])rel.getEdges().toArray( new GEdge[ 0 ] );
                        // for every pair of nodes linked to that relation
                        for ( int n1 = 0; n1 < edges.length; n1++ ) {
                            for ( int n2 = n1 + 1; n2 < edges.length; n2++ ) {
                                Concept c1 = null;
                                Concept c2 = null;
                                if ( edges[ n1].howLinked( rel ) == GEdge.Direction.FROM ) {
                                    c1 = (Concept)edges[ n1].toObj;
                                } else {
                                    c1 = (Concept)edges[ n1].fromObj;
                                }
                                if ( edges[ n2].howLinked( rel ) == GEdge.Direction.FROM ) {
                                    c2 = (Concept)edges[ n2].toObj;
                                } else {
                                    c2 = (Concept)edges[ n2].fromObj;
                                }

                                if ( selector == ConceptsToInclude.ALL
                                        || ( selector == ConceptsToInclude.GENERIC_ONLY
                                        && ( c1.getReferent().equals( "" ) || c2.getReferent().equals( "" ) ) ) ) {


//                                ArrayList v = makeTableEntryArrayList( c1, rel, c2, selector );
                                    // Temporarily ignores the selector for testing
                                    BinaryTuple t = new BinaryTuple( c1, rel, c2 );
                                    if ( t != null ) {
                                        tupleList.add( t );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return tupleList;
    }

    /**
     * Constructs a single table entry from a charger concept-relation-concept
     * set.
     *
     * @return a single vector containing: <ul> <li>element 0 - phrase,
     * <li>element 1 - concept label, <li>element 2 - relation label,
     * <li>element 3 - concept label, <li>element 4 - actual 1st (CharGer)
     * concept <li>element 5 - actual (CharGer) relation <li>element 6 - actual
     * 2nd (CharGer) concept </ul>
     */
    protected static ArrayList makeTableEntryArrayList(
            Concept c1, GNode rel, Concept c2, ConceptsToInclude selector ) {
        if ( c1 == null || rel == null || c2 == null ) {
            return null;
        }

        if ( swapBinaryRelation( c1, rel, c2 ) ) {
            // craft.Craft.say( "swapping" );
            Concept temp = c1;
            c1 = c2;
            c2 = temp;
        }

        ArrayList v = new ArrayList( 7 );		// a component of the tuple vector
        if ( selector == ConceptsToInclude.ALL
                || ( selector == ConceptsToInclude.GENERIC_ONLY && c1.getReferent().equals( "" ) && c2.getReferent().equals( "" ) ) ) {
            //craft.Craft.say( "concept " + c1.getTextLabel() + " included." );
            v.add( COL_SENTENCE_LABEL, makeSentence( c1, rel, c2 ) );
            v.add( COL_CONCEPT_LABEL_1, c1.getTextLabel() );
            v.add( COL_RELATION_LABEL, rel.getTextLabel() );
            v.add( COL_CONCEPT_LABEL_2, c2.getTextLabel() );
            v.add( COL_CONCEPT_1, c1 );
            v.add( COL_RELATION, rel );
            v.add( COL_CONCEPT_2, c2 );
            //v.add( map.getSecondGraph(), COL_SUBGRAPH );
            //for ( int i = 0; i < v.size(); i++ ) 
            //		craft.Craft.say( "v " + i + " has label " + v.get( i ) );
            return v;
        } else {
            return null;
        }

    }

    /**
     * Converts a ArrayList of tableEntry's to an HTML work sheet (usable for
     * manual analysis of the relations), with a summary.
     *
     * @param triples a ArrayList of binary relation table entries.
     * @param summary string used to identify the table in HTML, but not
     * otherwise displayed.
     * @see #makeTableEntryArrayList
     */
    public static String convertTriplesToHTMLtable( ArrayList triples, String summary ) {
        String XMLSummary = charger.xml.XMLGenerator.quoteForXML( summary.trim() );

        String html = "";
        // html += "<font face=\"sans-serif\">\n";
        //html += "<br>&nbsp;\n";

        html += "<br><b>" + XMLSummary + "</b>\n";
        html += "<TABLE border=\"1\" bordercolor=\"blue\" summary=\"" + XMLSummary + "\">\n";

        for ( int row = 0; row < triples.size(); row++ ) {
            html += "<TR>\n";

            html += " <TD width=30> R" + (int)( row + 1 ) + "\n";

            html += binaryRelationTableEntryToHTML( (ArrayList)triples.get( row ) );

            html += " <TD width=60>&nbsp;\n";
            html += " <TD width=60>&nbsp;\n";
            html += " <TD width=80>&nbsp;\n";
            html += " <TD width=60>&nbsp;\n";
        }
        html += "<TR>\n";
        html += " <TD width=100 colspan=2><b>Precision</b>\n";
        html += " <TD width=100 colspan=2><b>Recall</b>\n";
        for ( int i = 0; i < 2; i++ ) {
            html += " <TD width=40>=sum(R[-1]C:R[-" + triples.size() + "]C)\n";
        }
        html += " <TD width=40>&nbsp;\n";
        html += " <TD width=40>=RC[-2]+RC[-3]\n";
        html += "</TABLE>";

        return html;
    }

    /**
     * Creates html TD tags that must be enclosed in TR and TABLE tags somewhere
     * else.
     *
     * @see #makeTableEntryArrayList
     */
    public static String binaryRelationTableEntryToHTML( ArrayList tableEntry ) {
        String html = "";

        int col = 0;

        col = ConceptManager.COL_CONCEPT_LABEL_1;
        html += " <TD>"
                + charger.xml.XMLGenerator.quoteForXML( tableEntry.get( col ).toString().trim() ) + "\n";

        col = ConceptManager.COL_RELATION_LABEL;
        html += " <TD>"
                + charger.xml.XMLGenerator.quoteForXML( tableEntry.get( col ).toString().trim() ) + "\n";

        col = ConceptManager.COL_CONCEPT_LABEL_2;
        html += " <TD>"
                + charger.xml.XMLGenerator.quoteForXML( tableEntry.get( col ).toString().trim() ) + "\n";
        return html;

    }

//    private static notio.Graph getBinaryRelationPattern() {
//        // create a pattern graph P of the form [c]->(r)->[c]
//        // create it entirely in Notio and use Notio's matching facility
//        notio.Graph pattern = new notio.Graph();
//        notio.Concept patternC1 = new notio.Concept();
//        patternC1.setType( new ConceptType( "C1" ) );
//        notio.Concept patternC2 = new notio.Concept();
//        patternC2.setType( new ConceptType( "C2" ) );
//        notio.Relation patternRel = new notio.Relation();
//        patternRel.setType( new RelationType( "RR" ) );
//
//        pattern.addConcept( patternC1 );
//        pattern.addConcept( patternC2 );
//        pattern.addRelation( patternRel );
//
//        patternRel.setArgument( 0, patternC1 );
//        patternRel.setArgument( 1, patternC2 );
//        return pattern;
//    }
//
    /**
     * Decides heuristically whether to exchange the sense of the relationship,
     * regardless of the arrow direction. Looks at the relation's label and if
     * it's one of a few selected ones, returns true.
     */
    private static boolean swapBinaryRelation( Concept c1, GNode rel, Concept c2 ) {
        //if ( rel.getTextLabel().equalsIgnoreCase( "agent" ) ) return true;
        // if rel is among the outputs from c1, then relation is in "proper" order; otherwise swap
        ArrayList outs = c1.getLinkedNodes( GEdge.Direction.TO );
        if ( outs.contains( rel ) ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Makes a matching schemme suitable for matching binary relations with the
     * target graph.
     */
//    public static notio.MatchingScheme getBinaryMatchingScheme() {
//        return new MatchingScheme(
//                MatchingScheme.GR_MATCH_SUBGRAPH, //no match // int newGraphFlag,
//                MatchingScheme.CN_MATCH_ANYTHING, // int newConceptFlag,
//                MatchingScheme.RN_MATCH_ALL, // int newRelationFlag,
//                MatchingScheme.CT_MATCH_ANYTHING, // int newConceptTypeFlag,
//                MatchingScheme.RT_MATCH_ANYTHING, // int newRelationTypeFlag,
//                MatchingScheme.QF_MATCH_ANYTHING, // int newQuantifierFlag,
//                MatchingScheme.DG_MATCH_ANYTHING, // int newDesignatorFlag,
//                MatchingScheme.MARKER_MATCH_ANYTHING, // int newMarkerFlag,
//                MatchingScheme.ARC_MATCH_VALENCE, // int newArcFlag,	// works on unary relation
//                //MatchingScheme.ARC_MATCH_ANYTHING,         // int newArcFlag,
//                // let's play with the valence; should we look at "partial" relationships?
//                MatchingScheme.COREF_AUTOMATCH_ON, // int newCorefAutoMatchFlag,
//                MatchingScheme.COREF_AGREE_OFF, // int newCorefAgreementFlag,
//                MatchingScheme.FOLD_MATCH_OFF, // int newFoldingFlag,
//                MatchingScheme.CONN_MATCH_ON, // int newConnectedFlag,
//                0, // int newMaxMatches,
//                null, // MarkerComparator newMarkerComparator,
//                null // MatchingScheme newNestedScheme
//                );
//    }
    /**
     * Makes a matching schemme suitable for matching binary relations with the
     * target graph.
     */
//    public static notio.MatchingScheme getAllRelationsMatchingScheme() {
//        return new MatchingScheme(
//                MatchingScheme.GR_MATCH_SUBGRAPH, //no match // int newGraphFlag,
//                MatchingScheme.CN_MATCH_ANYTHING, // int newConceptFlag,
//                MatchingScheme.RN_MATCH_ALL, // int newRelationFlag,
//                MatchingScheme.CT_MATCH_ANYTHING, // int newConceptTypeFlag,
//                MatchingScheme.RT_MATCH_ANYTHING, // int newRelationTypeFlag,
//                MatchingScheme.QF_MATCH_ANYTHING, // int newQuantifierFlag,
//                MatchingScheme.DG_MATCH_ANYTHING, // int newDesignatorFlag,
//                MatchingScheme.MARKER_MATCH_ANYTHING, // int newMarkerFlag,
//                MatchingScheme.ARC_MATCH_VALENCE, // int newArcFlag,
//                // let's play with the valence; should we look at "partial" relationships?
//                MatchingScheme.COREF_AUTOMATCH_ON, // int newCorefAutoMatchFlag,
//                MatchingScheme.COREF_AGREE_OFF, // int newCorefAgreementFlag,
//                MatchingScheme.FOLD_MATCH_OFF, // int newFoldingFlag,
//                MatchingScheme.CONN_MATCH_ON, // int newConnectedFlag,
//                0, // int newMaxMatches,
//                null, // MarkerComparator newMarkerComparator,
//                null // MatchingScheme newNestedScheme
//                );
        /*
     RN_MATCH_ALL    RN_MATCH_ANYTHING  ARCS  INSTANCE  TYPE
     ARC_MATCH_ANYTHING     crash             crash         crash
     ARC_CONCEPT             OK               crash         OK
     INSTANCE                OK (empty)       crash         OK ()
     VALENCE                 OK               crash         OK
     */
//    }
//    private static notio.MatchingScheme getMatchingScheme1() {
//        return new MatchingScheme(
//                MatchingScheme.GR_MATCH_SUBGRAPH, //no match // int newGraphFlag,
//                MatchingScheme.CN_MATCH_ANYTHING, // int newConceptFlag,
//                MatchingScheme.RN_MATCH_ANYTHING, // int newRelationFlag,
//                MatchingScheme.CT_MATCH_ANYTHING, // int newConceptTypeFlag,
//                MatchingScheme.RT_MATCH_ANYTHING, // int newRelationTypeFlag,
//                MatchingScheme.QF_MATCH_ANYTHING, // int newQuantifierFlag,
//                MatchingScheme.DG_MATCH_ANYTHING, // int newDesignatorFlag,
//                MatchingScheme.MARKER_MATCH_ANYTHING, // int newMarkerFlag,
//                MatchingScheme.ARC_MATCH_VALENCE, // int newArcFlag,
//                MatchingScheme.COREF_AUTOMATCH_ON, // int newCorefAutoMatchFlag,
//                MatchingScheme.COREF_AGREE_OFF, // int newCorefAgreementFlag,
//                MatchingScheme.FOLD_MATCH_OFF, // int newFoldingFlag,
//                MatchingScheme.CONN_MATCH_ON, // int newConnectedFlag,
//                30, // int newMaxMatches,
//                null, // MarkerComparator newMarkerComparator,
//                null // MatchingScheme newNestedScheme
//                );
//    }
    public static String makeSentence( Concept c1, GNode rel, Concept c2 ) {
        if ( rel instanceof Relation ) {
            return Reporter.natLang( (Relation)rel, false );
        } else {
            return makeSentenceOLD( c1, rel, c2 );
        }
    }

    public static String makeSentenceOLD( Concept c1, GNode rel, Concept c2 ) {
        String c1label = null;
        String c2label = null;
        String rlabel = null;

        String conn1 = null;		// 1st connecting phrase
        String conn2 = null;		// 2nd connecting phrase

        // should use rel's from and to arguments to make the sentence
        boolean swap = false;
        /*Iterator myedges = rel.getEdges().iterator();
         while ( myedges.hasNext() )
         {
         GEdge ge = (GEdge)myedges.next();
         if ( ge.linkedTo( c1 ) == rel ) 
         if ( ge.howLinked( c1 ) == GEdge.Direction.TO ) swap = true;
         if ( ge.linkedTo( c2 ) == rel ) 
         if ( ge.howLinked( c2 ) == GEdge.Direction.FROM ) swap = true;
         }
         */
        if ( swap ) {
            Concept temp = c1;
            c1 = c2;
            c2 = temp;
        }

        if ( c1 != null ) {
            c1label = Reporter.tmap.alt( c1.getTypeLabel() );
        } else {
            c1label = "";
        }
        if ( c2 != null ) {
            c2label = Reporter.tmap.alt( c2.getTypeLabel() );
        } else {
            c2label = "";
        }
        if ( rel != null ) {
            rlabel = Reporter.tmap.alt( rel.getTextLabel() );
        } else {
            rlabel = "";
        }

        if ( "".equals( c1label + c2label + rlabel ) ) {
            return "";
        }

        conn2 = Reporter.tmap.suffix( rlabel, rel instanceof Actor );
        conn1 = Reporter.tmap.prefix( rlabel, rel instanceof Actor );

        return ( c2label + conn1 + rlabel + conn2 + c1label );
    }

    /**
     * Try to establish senses for the attribute phrase for a given row of the
     * repertory grid table. NEEDS a way to bring in defaults; for now, we just
     * do a search of all the available type descriptors and hope something
     * turns up.
     */
    public static AbstractTypeDescriptor[] getSensesFromPhrase( String phrase, charger.gloss.wn.WordnetManager wnmgr ) {
        ArrayList descriptors = new ArrayList();
        StringTokenizer toks = null;

        toks = new StringTokenizer( phrase, charger.gloss.wn.WNUtil.delimiters );
        // prepare a vector with all the tokens in it
        ArrayList words = new ArrayList();
        while ( toks.hasMoreTokens() ) {
            String[] ws = charger.gloss.wn.WNUtil.guessWordsFromPhrase( toks.nextToken() );
            words.addAll( Arrays.asList( ws ) );
            //String word = toks.nextToken();
            //if ( wnmgr.wordExists( word ) ) words.add( word );
        }

        // process the words from left to right, trying to establish a wordnet sense
        // for each one. Start by looking for the longest already-established definition.
        int wordnumber = 0;
        int len = 0;	// length (in words) of whatever term was acquired
        boolean keepGoing = true;
        Craft.say( "===== getting senses for phrase of length " + words.size() );
        while ( wordnumber < words.size() && keepGoing ) {
            Craft.say( "looking for previous senses starting at word " + wordnumber );
            // look for a previous descriptor for initial word(s)
            AbstractTypeDescriptor descr = findLongestTermAlreadyDefined( words, wordnumber );
            if ( descr != null ) {
                Craft.say( "initial descr type is " + descr.getClass() );
                String alreadyDefinedLabel = descr.getLabel();
                Craft.say( "querying for sense with the label/phrase " + alreadyDefinedLabel + "/" + phrase );
                descr = wnmgr.queryForSense( alreadyDefinedLabel, phrase, descr, true );
                if ( descr != null ) // user didn't cancel
                {
                    StringTokenizer temptoks = new StringTokenizer( alreadyDefinedLabel, charger.gloss.wn.WNUtil.delimiters );
                    wordnumber = wordnumber + temptoks.countTokens();
                    //assignDescriptor( alreadyDefinedLabel, descr, row );
                    descriptors.add( descr );
                }
            }
            if ( descr == null ) // word wasn't found or user didn't say anything
            {
                Craft.say( "Querying for terms starting at word " + words.get( wordnumber ) );
                // try to find phrases, starting with max remaining length, then length - 1, etc.
                for ( int trynum = words.size() - 1; trynum >= wordnumber; trynum-- ) {
                    String newTerm = makeTerm( words, wordnumber, trynum );
                    len = trynum - wordnumber + 1;
                    Craft.say( "creating term " + newTerm + " of length " + len );
                    descr = wnmgr.queryForSense( newTerm, phrase, null, true );

                    if ( descr != null ) // if needing a descriptor, we may have to create a new tracked attribute
                    {
                        //assignDescriptor( newTerm, descr, row );
                        descriptors.add( descr );
                        //craft.Craft.say( "Descr identified is \n" + descr.toXML( "" ) );
                        wordnumber = wordnumber + len; // - 1;		// skip past described words
                    }
                }
            }
            if ( descr == null ) // still nothing, proceed to next word and keep trying
            {
                wordnumber++;
            }
        }
        return (AbstractTypeDescriptor[])descriptors.toArray(new AbstractTypeDescriptor[ 0 ] );
    }

    /**
     * create a sub-term by taking the words from startWord to endWord - 1
     */
    public static String makeTerm( ArrayList _words, int startWord, int endWord ) {
        Craft.say( "makeTerm: from words " + startWord + " to " + endWord + " of " + _words );
        StringBuilder term = new StringBuilder();
        for ( int k = startWord; k <= endWord; k++ ) {
            term.append( _words.get( k ) );
            if ( k < endWord ) {
                term.append( " " );
            }
        }
        Craft.say( "makeTerm: result is " + term.toString() );
        return term.toString();
    }

    /**
     * Starting with the word at the startWord position, find the longest term
     * that has a definition somewhere in the set of repertory grids.
     *
     * @return the descriptor of the longest term found; <code>null</code> if
     * none found.
     * @see KnowledgeManager#findTypeDescriptor
     */
    public static AbstractTypeDescriptor findLongestTermAlreadyDefined( ArrayList wordlist, int startWord ) {
        Craft.say( "findLongestTermAlreadyDefined: start at word " + startWord + " of wordlist " + wordlist );
        // NOT WORKING!!
        for ( int wordnum = wordlist.size() - 1; wordnum >= startWord; wordnum-- ) {
            String term = makeTerm( wordlist, startWord, wordnum );
            AbstractTypeDescriptor d[] = Global.knowledgeManager.findTypeDescriptor( term, false );
            Craft.say( "findLongestTermAlreadyDefined: found " + d.length + " terms of length " + ( wordnum - startWord + 1 ) );
            if ( d != null && d.length > 0 ) {
                return d[0];
            }
        }
        return null;
    }

    /**
     * Create a graph in a window that reflects the inferred type hierarchy.
     * Looks among the known knowledge sources for a graph that contains type
     * labels and picks one arbitrarily that will have new types and glossary
     * text possibly added to it. If the graph is already in a window, then a
     * refresh of that window should reveal the augmented hierarchy.
     *
     * @return The graph (typically consisting of type labels only) with as many
     * definitions filled in as possible.
     * @see KnowledgeManager#findGraphsWithTypeHierarchy
     */
    public static Graph makeTypeHierarchy() {
        Graph h = new Graph();
        Graph[] hierarchies = Global.knowledgeManager.findGraphsWithTypeHierarchy();
        if ( hierarchies.length > 0 ) {
            h = hierarchies[ 0];		// arbitrarily choose first graph in list
        }			// fill in hierarchy with type definitions that already exist
        fillInGlossaryDefinitions( h );
        addNewTypeLabels( h );
        linkTypeLabels( h );
        return h;
    }

    /**
     * Fills in a hierarchy graph with definitions found from any source.
     */
    private static void fillInGlossaryDefinitions( Graph g ) {
        // construct the hierarchy, starting with existing types
        Iterator iter = new DeepIterator( g, new TypeLabel() );
        while ( iter.hasNext() ) // for each type label in the type hierarchy graph
        {
            TypeLabel type = (TypeLabel)iter.next();
            AbstractTypeDescriptor[] mydescr = type.getTypeDescriptors();
            // if a term is found in the hierarchy without a definition, then find one somewhere.
            if ( mydescr.length == 0 ) {
                Graph[] gs = Global.knowledgeManager.getAllGraphs();
                for ( int gnum = 0; gnum < gs.length; gnum++ ) {
                    Iterator gns = new DeepIterator( gs[ gnum], GraphObject.Kind.GNODE );
                    while ( gns.hasNext() ) {
                        // if the sought type label appears (perhaps as a compound term), use it
                        GNode gn = (GNode)gns.next();
                        // probably should be smarter about comparing the text labels
                        if ( gn.getTextLabel().equalsIgnoreCase( type.getTextLabel() )
                                && gn.getTypeDescriptor() != null ) {
                            mydescr = gn.getTypeDescriptors();
                            break;		// quit looking in this graph
                        }
                    }
                    if ( mydescr != null ) {
                        break;		// quit looking in all graphs
                    }
                }
                // if a definition can't be found, then query user?
                if ( mydescr.length == 0 ) // still no definition found
                {
                    mydescr = Global.knowledgeManager.findTypeDescriptor( type.getTextLabel(), false );
                    if ( mydescr.length == 0 ) {
                        Global.info( "no definition found for " + type.getTextLabel() );
                    }
                }
            }
            if ( mydescr.length > 0 ) {
                type.setTypeDescriptors( mydescr );
                if ( g.getOwnerFrame() != null ) {
                    g.getOwnerFrame().somethingHasChanged = true;
                }
            }
        }
    }

    /**
     * Adds any new type labels (i.e., not already in the type label graph) to
     * the given type hierarchy graph. If a new type label is found in some
     * concept, that concept's label and type descriptor(s) (if any) are
     * assigned to a new type label, which is inserted into graph
     * <code>g</code> using a crude heuristic for layout. If the type hierarchy
     * graph is actually changed (i.e., if there are really any new type labels
     * added) then its ownerframe (if any) is marked as changed and brought to
     * the front. NEED a routine to first find multi-word phrases and turn them
     * into their own types. NEED to also check relations!!
     *
     * @param g original graph to be modified in-place.
     */
    private static void addNewTypeLabels( Graph g ) {
        Iterator typelabeliter = new DeepIterator( g, new TypeLabel() );
        ArrayList labelstrings = new ArrayList();
        ArrayList typelabelvector = new ArrayList();
        while ( typelabeliter.hasNext() ) {
            TypeLabel temp = (TypeLabel)typelabeliter.next();
            labelstrings.add( temp.getTypeLabel().toLowerCase() );
            typelabelvector.add( temp );
            if ( temp.getTypeLabel().equalsIgnoreCase( "t" ) ) {
                top = temp;
            }
        }
        if ( top == null ) {
            top = new TypeLabel( "T" );
            top.setUpperLeft( new Point2D.Double( 5, 300 ) );
            g.insertInCharGerGraph( top );
            if ( g.getOwnerFrame() != null ) {
                g.getOwnerFrame().somethingHasChanged = true;
            }
            typelabelvector.add( top );
            labelstrings.add( top.getTypeLabel() );
        }
        // decide where to physically place the new type label on the canvas
        Rectangle2D.Double bounds = g.getDisplayBounds();
        Point2D.Double currentPt = new Point2D.Double( bounds.x, bounds.y + bounds.height + 10 );	// start right below the current graph
        Point2D.Double startPt = new Point2D.Double( currentPt.x, currentPt.y );
        // for each type in every graph, see if its type label in is g. 
        Iterator gns = getAllConcepts( ConceptsToInclude.ALL ).iterator();
        while ( gns.hasNext() ) {
            ArrayList conceptVector = (ArrayList)gns.next();
            Concept c = (Concept)conceptVector.get( 2 );

            if ( okayToAddTypeLabel( c, typelabelvector ) ) {
                TypeLabel t = new TypeLabel( c.getTypeLabel() );
                t.setTypeDescriptors( c.getTypeDescriptors() );
                t.setUpperLeft( currentPt );
                t.setCenter();
                currentPt = new Point2D.Double( currentPt.x + t.getDim().width + 4, currentPt.y );
                g.insertInCharGerGraph( t );
                if ( g.getOwnerFrame() != null ) {
                    g.getOwnerFrame().somethingHasChanged = true;
                }
                typelabelvector.add( t );
                labelstrings.add( t.getTypeLabel() );
            }
        }
        // for each descriptor in a grid, see if its term is in the hierarchy

        currentPt = startPt;
        currentPt = new Point2D.Double( currentPt.x, currentPt.y + 40 );
        repgrid.tracks.TrackedRepertoryGrid[] grids = Global.knowledgeManager.getAllGrids();
        for ( int gridnum = 0; gridnum < grids.length; gridnum++ ) {
            AbstractTypeDescriptor[] ds = grids[ gridnum].getAllTypeDescriptors();
            for ( int dnum = 0; dnum < ds.length; dnum++ ) {
                if ( ds[ dnum].getPOS().equals( "preposition" ) ) {
                    continue;		// ignore prepositions for now
                }
                if ( !labelstrings.contains( ds[ dnum].getLabel() ) ) // warning! this assumes that the same type label always means the same thing
                // eliminates the possibility that two different senses of the label could be used
                // NEED to fix this
                {
                    TypeLabel t = new TypeLabel( ds[ dnum].getLabel() );
                    t.setTypeDescriptor( ds[ dnum] );
                    t.setUpperLeft( currentPt );
                    t.setCenter();
                    currentPt = new Point2D.Double( currentPt.x + t.getDim().width + 4, currentPt.y );
                    g.insertInCharGerGraph( t );
                    if ( g.getOwnerFrame() != null ) {
                        g.getOwnerFrame().somethingHasChanged = true;
                    }
                    typelabelvector.add( t );
                    labelstrings.add( t.getTypeLabel() );
                }
            }
        }
    }

    /**
     * Determine whether a given concept's type label should be added to a type
     * hierarchy. If the concept's type is already in the hierarchy, then don't
     * add it.
     *
     */
    private static boolean okayToAddTypeLabel( Concept c, ArrayList typelabelvector ) {
        // if the descriptor's label is not already in the graph, OR
        // if the label is in the graph but the new sense is different, then
        //  it's okay to add the new type label
        for ( int labelnum = 0; labelnum < typelabelvector.size(); labelnum++ ) {
            TypeLabel t = (TypeLabel)typelabelvector.get( labelnum );

            if ( t.getTypeLabel().equalsIgnoreCase( c.getTypeLabel() ) ) {
                return false;
            }

            // now figure out whether the descriptors are different; if they are, skip this object
            AbstractTypeDescriptor[] existingdescr = t.getTypeDescriptors();
            AbstractTypeDescriptor[] possiblenewdescr = c.getTypeDescriptors();
            if ( existingdescr.length != possiblenewdescr.length ) {
                continue;
            }
            for ( int k = 0; k < existingdescr.length; k++ ) {
                if ( existingdescr[ k] instanceof WordnetTypeDescriptor
                        && possiblenewdescr[ k] instanceof WordnetTypeDescriptor
                        && ( (AbstractTypeDescriptor)existingdescr[ k] ).equals( possiblenewdescr[ k] ) ) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void linkTypeLabels( Graph h ) {
        /* link the types together according to their ordering in Wordnet
         algorithm as follows:
         - for each type label, if it has a wordnet descriptor, get it
         - get the synset of that descriptor
         - find the set of hypernyms for that synset
         - see if any of the hypernyms are already used in type labels, 
         if so, then link the first type label as a subtype to those found.
         */
        Iterator typelabels = new DeepIterator( h, new TypeLabel() );
        while ( typelabels.hasNext() ) {
            //  for each type label source, if it has a wordnet descriptor, get it
            TypeLabel t = (TypeLabel)typelabels.next();
            AbstractTypeDescriptor[] ts = t.getTypeDescriptors();
            if ( ts.length == 0 || !( ts[ ts.length - 1] instanceof WordnetTypeDescriptor ) ) {
                continue;
            }

            WordnetTypeDescriptor wdescr = (WordnetTypeDescriptor)ts[ ts.length - 1];
            // get the synset of that descriptor
            Synset tsynset = wdescr.getSynset();
            Craft.say( "looking for hypernyms of synset " + tsynset );
            linkHypernyms( t, tsynset, h );
        }
    }

    /**
     * Looks for type labels possessing synsets that are hypernyms (either
     * direct or indirect) of the given synset. If any are found, appropriate
     * links are created. If it's possible to link to a type label, there's no
     * further looking upward from that type label.
     *
     * @param t original (possible subtype)
     * @param s t's synset to begin with, or a recursively-obtained one
     * @param h the graph to restrict searching
     * @see ConceptManager#linkTypeLabels
     */
    public static void linkHypernyms( TypeLabel t, Synset s, Graph h ) {
        //craft.Craft.say( "linkHypernyms with type label " + t.getTypeLabel() );
        try {
            // find the set of hypernyms for that synset
            Pointer[] pointers = s.getPointers( PointerType.HYPERNYM );
            if ( pointers.length == 0 ) {
                if ( !GEdge.areLinked( t, top ) ) {
                    if ( top.getOutermostGraph() == t.getOutermostGraph() ) {
                        GenSpecLink ge = new GenSpecLink( t, top );
                        h.insertInCharGerGraph( ge );
                        Craft.say( "added link to top from " + t.getTypeLabel() );
                    }
                }
            } else {
                for ( int p = 0; p < pointers.length; p++ ) {
                    //craft.Craft.say( "hypernym " + p + " of " + pointers.length + " is " + pointers[ p ].getTarget() );
                    // see if any of the hypernyms are already used in type labels, 
                    //	if so, then link the source type label as a subtype to those found.
                    // NEED to make this routine recursive, so that all hypernym's hypernym are searched, etc.
                    TypeLabel[] supertypes = findBySynset( (Synset)pointers[ p].getTarget(), h );
                    //craft.Craft.say( "Found " + supertypes.length + " super-typelabels of " + pointers[p].getTarget() );
                    //if ( supertypes.length == 0 ) continue;
                    for ( int supertypenum = 0; supertypenum < supertypes.length; supertypenum++ ) {
                        if ( t.getOutermostGraph() != supertypes[ supertypenum].getOutermostGraph() ) {
                            continue;
                        }
                        // if they're already linked, then do nothing
                        if ( GEdge.areLinked( t, supertypes[ supertypenum] ) ) {
                            continue;
                        }

                        GenSpecLink ge = new GenSpecLink( t, supertypes[ supertypenum] );
                        h.insertInCharGerGraph( ge );
                        Craft.say( "added link from " + t.getTypeLabel()
                                + " to " + supertypes[ supertypenum].getTypeLabel() );
                    }
                    // recursive call: find hypernyms of pointer p's target and see if this
                    // type can be linked to them.
                    linkHypernyms( t, (Synset)pointers[ p].getTarget(), h );
                }
            }
            //craft.Craft.say( "hypernym tree is:" );
            //PointerUtils.getHypernymTree( answer ).print();
        } catch ( JWNLException je ) {
            Global.error( "Wordnet exception: " + je.getMessage() );
        }

    }
    /**
     * An action for augmenting a CharGer type hierarchy, suitable for adding to
     * menus and buttons. Really just a wrapper for makeTypeHierarchy. Returns
     * the Action.NAME Hub.strs( "MakeTypeHierarchyLabel" ).
     *
     * @see Global#strs
     * @see #makeTypeHierarchy
     */
    public static Action makeTypeHierarchyAction = new AbstractAction() {
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return Global.strs( "MakeTypeHierarchyLabel" );
            }
            return super.getValue( s );
        }

        public void actionPerformed( ActionEvent e ) {
            Graph g = makeTypeHierarchy();
            if ( g.getOwnerFrame() != null ) {
                if ( !g.getOwnerFrame().emgr.useNewUndoRedo ) {
                    g.getOwnerFrame().emgr.makeHoldGraph();
                }
                g.getOwnerFrame().emgr.setChangedContent( EditChange.SEMANTICS );
                g.getOwnerFrame().repaint();
            }
        }
    };

    /**
     * Seeks out nodes one of whose type descriptors contains the synset given.
     *
     * @param s the synset whose GNode is sought.
     * @param g the graph where the type label search is *
     * limited; <code>null</code> if we want all type labels from all graphs.
     * @return the set of non-duplicated nodes with that synset
     */
    public static TypeLabel[] findBySynset( Synset s, Graph g ) {
        ArrayList results = new ArrayList();
        ArrayList types = null;
        if ( g == null ) {
            types = getAll( new TypeLabel() );
        } else {
            types = new ArrayList();
            Iterator iter = new DeepIterator( g, new TypeLabel() );
            while ( iter.hasNext() ) {
                types.add( iter.next() );
            }
        }

        for ( int tnum = 0; tnum < types.size(); tnum++ ) {
            TypeLabel t = (TypeLabel)types.get( tnum );
            AbstractTypeDescriptor[] ds = t.getTypeDescriptors();
            if ( ds.length == 0 ) {
                continue;
            }
            // have one or more type descriptors
            for ( int dnum = 0; dnum < ds.length; dnum++ ) {
                if ( ( ds[ dnum] instanceof WordnetTypeDescriptor )
                        && ( s.equals( ( (WordnetTypeDescriptor)ds[ dnum] ).getSynset() ) )
                        && ( !results.contains( t ) ) ) {
                    results.add( t );
                }
            }
        }

        return (TypeLabel[])results.toArray( new TypeLabel[ 0 ] );
    }
}
