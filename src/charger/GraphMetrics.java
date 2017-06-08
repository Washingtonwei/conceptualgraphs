package charger;

import charger.obj.Actor;
import charger.obj.Concept;
import charger.obj.Coref;
import charger.obj.DeepIterator;
import charger.obj.GNode;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.obj.Relation;
import charger.obj.RelationLabel;
import charger.obj.ShallowIterator;
import charger.obj.TypeLabel;
import charger.util.Tag;
import java.util.Iterator;
import java.util.TreeSet;


/**
    Class to collect and organize various conceptual graph metrics for research purposes.
    Names of each metric don't use "camel-case" -- they use underscore-connected words so that each metric's name
        can be output to a text file and parsed as a single word with no spaces.
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

public class GraphMetrics
{
    Graph currentGraph = null;

    int concepts = 0;
    int relations = 0;
    int actors = 0;
    int contexts = 0;
    int types = 0;
    int reltypes = 0;
    int corefs = 0;
    
    int concept_degrees = 0;
    int all_node_degrees = 0;
    
    TreeSet<String> conceptTypeLabels = new TreeSet<String>();
    TreeSet<String> relationTypeLabels = new TreeSet<String>();
    
    java.text.NumberFormat nformat = java.text.NumberFormat.getNumberInstance();

                
    /**
        Instantiate a set of metrics for the given graph.
        @param g the top-level graph being measured. May include type/relation hierarchies, etc.
     */
    public GraphMetrics( Graph g )
    {
        nformat.setMaximumFractionDigits( 2 );
        nformat.setMinimumFractionDigits( 0 );
        currentGraph = g;
    }
    
    /**
        Create a text string containing tab-separated metrics and observations about the graph.
        The set of metrics is currently fixed and cannot be changed by the programmer.
        Text filters such as perl can be used to manipulate the results.
        *         * @param compress whether to suppress zero values or not

     */
    public String getGraphMetrics( boolean compress)
    {
          suppressZeroes = compress;
      
        makeAllMetrics();
        return showAllMetrics();
    }

    /**
        Perform all the counts necessary for displaying the metrics.
     */
    public void makeAllMetrics()
    {
        DeepIterator iter;
        
        iter = new DeepIterator( currentGraph );
        while ( iter.hasNext() )
        {
            GraphObject n = (GraphObject)iter.next();
            if ( n instanceof Concept ) concepts++;
            if ( n instanceof Relation ) relations++;
            if ( n instanceof Actor ) actors++;
            if ( n instanceof TypeLabel ) types++;
            if ( n instanceof RelationLabel ) reltypes++;
            if ( n instanceof Graph ) contexts++;
            if ( n instanceof Coref ) corefs++;
            
            if ( n instanceof Concept || n instanceof Relation || n instanceof Actor || n instanceof Graph )
                all_node_degrees += ((GNode)n).getEdges().size();
            if ( n instanceof Concept || n instanceof Graph )
            {
                concept_degrees += ((GNode)n).getEdges().size();
                conceptTypeLabels.add( ((Concept)n).getTypeLabel() );
            }
            if ( n instanceof Relation )
            {
                relationTypeLabels.add( ((Relation)n).getTypeLabel() );
            }
        }
    }
    
    protected String showString( String name, String value ) 
    { return Tag.tr(  Tag.td( name)  + Tag.td( value ));  }
    
    /**
     *
     * @param name The human-readable name of the metric
     * @param value The metric value to be printed
     * @return  HTML tagged string for a single metric
     * @see #suppressZeroes
     */
    protected String showOneMetric( String name, float value ) {
        if ( value == 0 && suppressZeroes ) {
            return "";
        } else {
            return Tag.tr( Tag.td( name ) + Tag.td( nformat.format( value ) ) );
        }
    }
    
    /**
        Display the results of collecting all the measurements.
        Will generate exception on an empty graph.
        The meaning of each of the metrics is as follows:
        <table width="600">
            <tr><td>total_concept_instances <td>Number of concept instances (boxes) in the graph
            <tr><td>total_relation_instances <td>Number of relation instances (ovals) in the graph
            <tr><td>total_actor_instances <td>Number of actor instances (diamonds) in the graph
            <tr><td>total_type_hierarchy_labels <td>Number of type label instances (underlined types) whether in hierarchies or not
            <tr><td>total_relation_hierarchy_labels <td>Number of relation label instances (underlined relations) whether in hierarchies or not
            <tr><td>total_lines_of_identity <td>Number of lines of identity (dashed lines) in the graph
            <tr><td>total_contexts  <td>Number of contexts (rectangular borders) in the graph
            <tr><td>average_node_degree <td>Average number of arrows/lines-of-identity connected to each concept/relation/actor/context/etc.
            <tr><td>average_concept_context_degree<td>Average number of arrows/lines-of-identity connected to concept or context only
            <tr><td>number_of_unique_concept_types <td>Number of unique concept types (regardless of referent)
            <tr><td>unique_concept_type_names <td>List of unique concept type names (ignoring referent)
            <tr><td>number_of_unique_relation_types <td>Number of unique relation types
            <tr><td>unique_relation_type_names <td>List of unique concept types (regardless of referent)
            <tr><td>concept_type_variability <td>total_concept_instances / number_of_unique_concept_types
            <tr><td>relation_type_variability <td>total_relation_instances / number_of_unique_relation_types
        </table>
     */ 
    public String showAllMetrics( )
    {
        try {
        return Tag.table( 750 ) +
            showOneMetric( "total_concept_instances", concepts ) +
            showOneMetric( "total_relation_instances", relations ) +
            showOneMetric( "total_actor_instances", actors ) +
            showOneMetric( "total_type_hierarchy_labels", types ) +
            showOneMetric( "total_relation_hierarchy_labels", reltypes ) +
            showOneMetric( "total_lines_of_identity", corefs ) +
            showOneMetric( "total_contexts", contexts ) +
      //      showOneMetric( "graph_diameter", (float)( diameter( currentGraph ) ) ) +
            showOneMetric( "average_node_degree", (float)all_node_degrees / (float)( concepts + relations + actors + contexts )   ) +
            showOneMetric( "average_concept_context_degree", (float)concept_degrees / (float)( concepts + contexts ) ) +
            showOneMetric( "number_of_unique_concept_types", conceptTypeLabels.size() ) +
            showString( "unique_concept_type_names", conceptTypeLabels.toString() ) +
            showOneMetric( "number_of_unique_relation_types", relationTypeLabels.size() ) +
            showString( "unique_relation_type_names", relationTypeLabels.toString() ) +
            showOneMetric( "concept_type_variability", (float)concepts / (float)conceptTypeLabels.size()  ) +
            showOneMetric( "relation_type_variability", (float)relations / (float) relationTypeLabels.size() ) +
            Tag._table;
        }
        catch ( ArithmeticException e )
        { return "***_graph_too_small_***"; }
    }
    
    private boolean suppressZeroes = false;
    
    /**
        Returns the "diameter" of the graph, which is defined as the longest shortest path between any two nodes of the graph.
        Based on Dijkstra's algorithm.
     */
    public int diameter( Graph g )
    {
        int longestPath = 0;
        Iterator starting_nodes = new ShallowIterator( currentGraph, GraphObject.Kind.GNODE );
        
        return 0;
    }
}