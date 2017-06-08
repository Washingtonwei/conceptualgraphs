/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public enum MFileType {

    MODEL( "cgx", "MM model file" ),
    GROUPREPORT( "html", "Detailed report for one group" ),
    TEAMREPORT( "html", "Detailed report for one team" ),
    GROUPSYNONYM( "txt", "Synonyms for an entire group" ),
    TEAMSYNONYM( "txt", "Synonyms for one team" ),
    SUMMARY( "html", "Report for an entire project" ),
    SPREADSHEET( "xls", "Spreadsheet for an entire project (for stats analysis)" ),
    ADMIN_LOG ("txt", "Logs for the administrator to see"),
    OTHER( "txt", "Unknown or not relevant to MMAT processing" );
    
    private String extension;
    private String description;

    private MFileType( String e, String d ) {
        extension = e;
        description = d;

    }

    public String extension() {
        return extension;
    }

    public String description() {
        return description;
    }
}
