/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.util.Tag;
import java.util.ArrayList;


/**
 * Abstraction of a collection of structures needed to perform metrics on a set of models.
 * Metrics output is in the form of strings.
 *      * Abstract class representing an instance of calculating and reporting metrics on models.
     *  * Strings that are returned are assumed to be HTML strings, but each caller is responsible
 * for providing &lt;html&gt; tags.

 * There are three kinds of output
 * <ul>
 * <li>Name and identifying information about the metric(s)</li>
 * <li>Identification of the input models, context, etc.</li>
 * <li>Results of the analysis
 * </ul>

* @author Harry Delugach
* @since Charger 3.8.0
 */
abstract public class MMetrics {
    
    /** Each metric instance gets  its own name */
    String caption = null;
    
    /** The set of models to be analyzed by a given metrics instance */
    ArrayList<MModel> models = null;
    
    
    /** Used in formatting floating point numbers for reports */
    public static java.text.DecimalFormat nformat = new java.text.DecimalFormat( "##0.000;0");
    
    /**

     * @param models The models being analyzed
    
     * @param caption A string identifying what kind of metrics are being calculated.
     */
    public MMetrics( ArrayList<MModel> models, String caption ) {
        setCaption( caption );
        this.models = models;
 
    }
    
    /** Provided for completeness; probably never used because sub-classes
     need something (e.g., models) to operate usefully. */
    public MMetrics() { };  // probably never used
    
    /**
     * Caller invokes this to trigger whatever processing is needed before getting results.
     * Also allows for changing metric parameters and then regenerating.
    */
    abstract public void generateMetrics();
    
    /**
     * Obtain an HTML version of the results of the analysis.
     * Metrics should not have to re-generated before calling this again.
     * @return the metrics class's version of whatever metrics it produces.
     */
    abstract public String getResults();

    /**
     * Lists the models being analyzed.
     * @return an HTML tagged list of model names
     */
    public String showModelNames() {
        String r = Tag.olist;
        for ( MModel m : models ) {
            r = r + Tag.item( m.getID().getName() );
        }
        return r + Tag._olist;
    }
    

    /** @return the name for this metrics object */
    public String getCaption() {
        return caption;
    }
    /** @param caption the name for this metrics object */

    public void setCaption(String caption) {
        this.caption = caption;
    }
    
}
