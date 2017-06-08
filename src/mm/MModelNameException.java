/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

/**
 * Used in case there's some sort of problem with the Model name
 *
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class MModelNameException extends Exception {

    String offendingName = null;

    public MModelNameException( String s ) {
        offendingName = s;
    }

    public String getMessage() {
        return "Model Name Exception: " + offendingName + "\n";
    }
}
