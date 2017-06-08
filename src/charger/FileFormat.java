/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger;

/**
 * Encapsulates the notion of a file format with which Charger has to deal.
 * This class does not capture the syntax of the format, only its extension and its description.
 * The image formats are taken from one typical image writer's list of supported
 * formats.
 * @since 3.8.3
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public enum FileFormat {

    DEFAULT( "cgx", "Charger v3/v4 (.cgx)", Family.TEXT ), // same as CHARGER4
    CHARGER4( "cgx", "Charger v3/v4 (.cgx)", Family.TEXT ), //  format based on XML
    CHARGER2( "cg", "Charger v1.0-v2.9", Family.TEXT ), // "native" text format prior to version 3.0
    CHARGER3( "cgx", "Charger v3.0-v3.8", Family.TEXT), // "native" XML based format up to version 3.9
    CGIF2007( "cgif", "CGIF 2007 (.cgif)", Family.TEXT ), // CGIF format based on the ISO/IEC 24707:2007 Annex B standard
    PREF("conf", "CharGer preference file", Family.TEXT ), // preferences file in the output form of Properties.write() 
    BMP( "bmp", "Bitmap (BMP)", Family.BITMAP ),
    GIF( "gif", "GIF image", Family.BITMAP ),
    JPG( "jpg", "JPG image", Family.BITMAP ),
    JPEG( "jpeg", "JPEG image", Family.BITMAP ),
    PNG( "png", "Portable Network Graphic (PNG) image", Family.BITMAP ),
    WBMP( "wbmp", "Windows Bitmap (BMP) image", Family.BITMAP ),
    PDF( "pdf", "Portable Document Format (PDF)", Family.VECTOR ),
    SVG( "svg", "Scalable Vector Graphics (SVG))", Family.VECTOR ),
    EPS( "eps", "Postscript (EPS)", Family.VECTOR ),
    UNKNOWN( "unk", "Unknown", Family.UNKNOWN );
    
    public enum Family {
        BITMAP,     // images as pixels
        VECTOR,     // images as vector graphics images
        TEXT,       // not images, but text (usually an exported or regular graph)
        UNKNOWN     // anything else (isn't supposed to happen)
    }
    

    private String extension;
    private String description;
    private Family family;

    private FileFormat( String extension, String description, Family family ) {
        this.extension = extension;
        this.description = description;
        this.family = family;
    }

    /**
     * @return a single file extension associated with that file format. 
     * Does NOT include the leading "." of the extension.
     */
    public String extension() {
        return extension;
    }

    /**
     * @return A short description of the format's meaning.
     */
    public String description() {
        return description;
    }
    
    public Family family() {
        return family;
    }

    /**
     * @param s An extension (w/o the period) or a description
     * @return the FileFormat value corresponding to the extension or
     * description; UNKNOWN if none is found.
     */
    public static FileFormat FileFormatOf( String s ) {
        for ( FileFormat f : FileFormat.values() ) {
            if ( f.extension().equalsIgnoreCase( s ) ) {
                return f;
            }
            if ( f.description().equalsIgnoreCase( s ) ) {
                return f;
            }
        }
        return UNKNOWN;
    }
}
