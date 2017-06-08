/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mm;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Contains some of the "globals" needed in the MM package.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MMConst {
     /**
     * Used for the analyzer to display its version.
     */
    public static final String MMATversion = " 2.4 [01 Oct 2014] ";
   /**
     * The folder containing the submitted models for analysis. When running
     * Charger for the purposes of doing MMAT analysis, this is the folder that
     * should be the 2nd argument of the "-p" parameter.
     *
     */
    public static final String MMSubmittedFolderName = "Submitted";
//    public static File MMSavedFolder = null;
//    
    /**
     * The file extension for MMAT editor formatted files; currently ".cgx" (as
     * of Aug 2013)
     */
    public static final String MMATModelExtension = "cgx";
    /**
     * The folder that is a "sibling" to the submittedFile folder (as of Aug
     * 2013). It is assumed that the MMAT Editor (the creator of the combinedModel
     * files) saves submittedFile files in [MMAnalysisFolderParent]/Submitted
     * whereas any un-submittedFile models are found in [MMSubmittedFolder]
     */
    public static final String MMSavedFolderName = "Saved";
//    /**
//     * The folder in which both the "submitted" and "saved" folders reside.
//     */
//    public static File MMAnalysisParent = null;
//
//    
//    public static String currentGroup = null;
//    public static String currentTeam = null;
//    public static String currentModelName = null;
//
    protected static ArrayList<MProject> projs = new ArrayList();  // for our current research, has just one project "mm"
    

    public static final boolean useNewFolderStructure = true;
    /**
     * Find the MProject object that goes with the name. Obviously no two projects are supposed to have the same name.
     * @param name
     * @return A (possibly empty) MProject instance.
     */
    public static MProject getProjectByName( String name ) {
        for ( MProject e : projs ) {
            if ( e.getName().equals( name ) ) {
                return e;
            }
        }
        return null;
    }
    
    /**
     * Clears the list, but does not release any resources beyond that.
     */
    public static void clearProjects() {
        projs.clear();
    }


}
