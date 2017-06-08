/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kb.hierarchy;

import java.util.*;

/**
 *
 * @author hsd
 */
public class NodeOrderException extends Exception {

    private String message;
    ArrayList<POSetNode> nodes;

    public NodeOrderException( String s, ArrayList<POSetNode> nodes ) {
        this.message = s;
        this.nodes = nodes;
    }

    public String getMessage() {
        return this.getClass().getName() + this.message;
    }
}
