/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kb;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class KBException extends Exception {
    private Object source = null;
    
    public KBException( String message, Object s ) {
        super( message );
        source = s;
    }

    public Object getSource() {
        return source;
    }

    public void setSource( Object source ) {
        this.source = source;
    }
    
    
            
}
