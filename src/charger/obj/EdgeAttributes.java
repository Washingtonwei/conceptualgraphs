/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.obj;

import charger.Global;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
/**
 * Keeps whatever line attributes are specific to edges.
 */
public class EdgeAttributes {

    protected int arrowHeadWidth = 5;
    protected int arrowHeadHeight = 8;
    protected double edgeThickness = 1.0f;
    protected double arrowPointLocation = 1.0f;

    public EdgeAttributes( int arrowHeadWidth, int arrowHeadHeight, float edgeThickness, float arrowPointLocation) {
        this.arrowHeadWidth = arrowHeadWidth;
        this.arrowHeadHeight = arrowHeadHeight;
        this.edgeThickness = edgeThickness;
        this.arrowPointLocation = arrowPointLocation;
    }
    
    public EdgeAttributes() {
        this.arrowHeadWidth = Global.factoryEdgeAttributes.getArrowHeadWidth();
        this.arrowHeadHeight = Global.factoryEdgeAttributes.getArrowHeadHeight();
        this.edgeThickness = Global.factoryEdgeAttributes.getEdgeThickness();
        this.arrowPointLocation = arrowPointLocation;
    }
    
    


    public int getArrowHeadWidth() {
        return arrowHeadWidth;
    }

    public void setArrowHeadWidth( int arrowHeadWidth ) {
        this.arrowHeadWidth = arrowHeadWidth;
    }

    public int getArrowHeadHeight() {
        return arrowHeadHeight;
    }

    public void setArrowHeadHeight( int arrowHeadHeight ) {
        this.arrowHeadHeight = arrowHeadHeight;
    }

    public double getEdgeThickness() {
        return edgeThickness;
    }

    public void setEdgeThickness( double edgeThickness ) {
        this.edgeThickness = edgeThickness;
    }

    public double getArrowPointLocation() {
        return arrowPointLocation;
    }

    public void setArrowPointLocation( double arrowPointLocation ) {
        this.arrowPointLocation = arrowPointLocation;
    }
}
