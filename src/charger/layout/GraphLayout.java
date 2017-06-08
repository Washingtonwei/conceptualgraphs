/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger.layout;

import charger.obj.Graph;

/**
 * Abstract class to specify the basics of a layout class.
 * Perhaps it should be an interface, but this will do for the present.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public abstract class GraphLayout {
    
    protected Graph graph;
    protected boolean verbose;

    /**
     * Create an instance of a layout manager. 
     * @param g the graph to be laid out.
     */
    public GraphLayout( Graph g ) {
        graph = g;
    }

    /**
     * Whether to display debugging and tracing output on the console.
     * @return true if verbose is turned on; false otherwise 
     */
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose( boolean verbose ) {
        this.verbose = verbose;
    }
    
    

    /**
     * Execute whatever layout procedure is implemented by the class.
     * This procedure is responsible for performing the layout only.
     * @see #copyToGraph
     * @return true if all went well; false if something went wrong.
     */
    abstract public boolean performLayout();
    
       
    /**
     * Use the results of the layout procedure and update the graph to reflect the new positions.
     * If no layout has been performed, then do nothing.
     */
    abstract public void copyToGraph();
}
