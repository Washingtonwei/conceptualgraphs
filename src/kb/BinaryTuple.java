package kb;

import java.util.*;
import charger.obj.*;
import charger.util.Tag;

/**
    An abstraction of the ubiquitous concept-relation-concept triple that often deserves its own manipulations.
     @since 3.5b2
 */
public class BinaryTuple
{
        /** Crude representation of tuple as an English sentence. */
    public String sentence = null;
        /** Complete text (including referent, etc.) of first ("left-hand") concept. */
    public String concept1_label = null;
        /** Text label of the relation. */
    public String relation_label = null;
        /** Complete text (including referent, etc.) of second ("right-hand") concept. */
    public String concept2_label = null;
    
        /** The actual first ("left hand") concept associated with this tuple. */
    public Concept concept1 = null;
        /** The actual relation (or actor) associated with this tuple. */
    public GNode relation = null;
        /** The actual second ("right hand") concept associated with this tuple. */
    public Concept concept2 = null;
        
    public BinaryTuple() { };
    
    public BinaryTuple( Concept c1, GNode r, Concept c2 )
    {
        /*if ( ConceptManager.swapBinaryRelation( c1, r, c2 ) )
        {
                            // craft.Craft.say( "swapping" );
            charger.obj.Concept temp = c1;
            c1 = c2;
            c2 = temp;
        }*/

        concept1 = c1;
        relation = r;
        concept2 = c2;
        concept1_label = c1.getTextLabel();
        relation_label = r.getTextLabel();
        concept2_label = c2.getTextLabel();
        sentence = ConceptManager.makeSentence( concept1, relation, concept2 );
    }
    
    
    /**
    Create the tuple from a makeTableEntry ArrayList form (the one used for DefaultTableModel):
        <ul>
        <li>element 0 - phrase,
        <li>element 1 - concept label,
        <li>element 2 - relation label, 
        <li>element 3 - concept label, 
        <li>element 4 - actual 1st (CharGer) concept
        <li>element 5 - actual (CharGer) relation
        <li>element 6 - actual 2nd (CharGer) concept
        </ul>

    @see ConceptManager#makeTableEntryArrayList
    */

    public BinaryTuple( ArrayList v )
    {
        sentence = (String) v.get( ConceptManager.COL_SENTENCE_LABEL );
	concept1_label = (String) v.get( ConceptManager.COL_CONCEPT_LABEL_1 );
	relation_label = (String) v.get( ConceptManager.COL_RELATION_LABEL );
	concept2_label = (String) v.get( ConceptManager.COL_CONCEPT_LABEL_2 );
	concept1 = (Concept) v.get( ConceptManager.COL_CONCEPT_1 );
	relation = (Relation) v.get( ConceptManager.COL_RELATION  );
	concept2 = (Concept) v.get( ConceptManager.COL_CONCEPT_2 );

    }        


    /**
            Returns the colum header labels when gathering tuples from subgraphs.
            The intent is that the caller can use these in a TableModel or JTable constructor to make a column model.
            The caller may decide to hide one or more of the columns in the table.
            @return a vector of strings, each of which is a column label.
            @see ConceptManager#getBinaryRelationTuples
            @see ConceptManager#getAllRelationTuples
     */
    public static ArrayList<String> getTupleColumnLabels()
    {
        ArrayList<String> list = new ArrayList<String>();
            list.add( "Phrase" );
            list.add( "Concept 1" );
            list.add( "Relation" );
            list.add( "Concept 2" );
            list.add( "Concept Object 1" );
            list.add( "Relation Object" );
            list.add( "Concept Object 2" );
            return list;
    }
    
    /**
        For compatibility with the old ConceptManager routines, as well as provide
            easy interface to DefaultTableModel which likes its model in ArrayList form.
        @return am  arraylist representing the tuple, in the style of makeTableEntry
        @see ConceptManager#makeTableEntryArrayList
     */
    public ArrayList toArrayList()
    {
        ArrayList list = new ArrayList();
        list.add( sentence );
        list.add( concept1_label );
        list.add( relation_label );
        list.add( concept2_label );
        list.add( concept1 );
        list.add( relation );
        list.add( concept2 );
        
        return list;
    }
    
    /**
        Converts this binary tuple to an HTML-compatible TABLE row; TABLE and TR tags must be
            provided external to this routine.
        @return the concepts and relation labels with TD tags.
     */
    public String toHTML()
    {
        String html = "";
                
        html += " <TD>" + 
            charger.xml.XMLGenerator.quoteForXML( concept1_label.toString().trim() ) + "\n";
        
        html += " <TD>" + 
            charger.xml.XMLGenerator.quoteForXML( relation_label.toString().trim() ) + "\n";
        
        html += " <TD>" + 
            charger.xml.XMLGenerator.quoteForXML( concept2_label.toString().trim() ) + "\n";
        return html;
    }
    
    public String toString() {
            return concept1_label.toString().trim() + " -&gt; " +
                   relation_label.toString().trim() + " -&gt; " +
                   concept2_label.toString().trim();
    }
    
    public static class TupleComparator implements Comparator
    {
     /**
        Used by sorting routines only; not useful for matching (which is a much more interesting operation).
        @param o1 one tuple or subclass
        @param o2 another tuple or subclass
        @return negative integer, zero or positive integer, depending on whether t1's relation label is less than, equal to, or greater
            than t2's relation label. Ignores case of labels. If relations are equal, compares concept1's label. If still equal, tries
            concept t2's label, then gives up.
     */
       public int compare( Object o1, Object o2 )
        {
            BinaryTuple t1 = (BinaryTuple) o1;
            BinaryTuple t2 = (BinaryTuple) o2;
            int comp = t1.relation_label.compareToIgnoreCase( t2.relation_label );
            if ( comp != 0 ) return comp;
            comp = t1.concept1_label.compareToIgnoreCase( t2.concept1_label );
            if ( comp != 0 ) return comp;
            return t1.concept2_label.compareToIgnoreCase( t2.concept2_label );
        }
    }

}