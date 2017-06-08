/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package charger;

import charger.obj.GraphObject;
import java.util.ArrayList;

/**
 * Encapsulates the notion of a rule regarding valid formation and semantics
 * in CGs. 
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class CGRule {
    private String name = null;
    private String description = null;
    private ArrayList<GraphObject> involvedObjects = new ArrayList<>();

    public CGRule( String name, String description ) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public ArrayList<GraphObject> getInvolvedObjects() {
        return involvedObjects;
    }

    public String toString() {
        String objects = "";
        for ( GraphObject go : involvedObjects ) {
            objects += go.getTextLabel() + " ";
}
        return "CG Rule " + getName() + " on " + objects + ". " + description;
    }
    
}
