package charger.util;

import charger.*;

import java.util.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.geom.*;
import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.text.DateFormat;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.table.*;

/**
 * General Utilities, a collection of static methods rather independent of
 * CharGer itself.
 */
public final class Util {

    /**
     * Extracts the suffixed filename part of a supposed file name string, incl
     * extensions.
     *
     * @param s supposed file name string
     * @return filename part of the string; if no path, then returns s
     */
    public static String getSimpleFilename( String s ) {
        // assume s has no pathname attached; fname is simple filename with no path
        if ( s == null ) {
            return "";
        }
        String fname = s;
        String pathname = "";
        // first, strip off any pre-pended path information
        int fileSepIsAt = s.lastIndexOf( System.getProperty( "file.separator" ) );
        if ( fileSepIsAt > 0 ) { 	// will fail on pathnames like ":temp"
            fname = new String( s.substring( fileSepIsAt + 1, s.length() ) );
        }
        fileSepIsAt = fname.lastIndexOf( File.separator );
        if ( fileSepIsAt > 0 ) { 	// will fail on pathnames like ":temp"
            fname = new String( fname.substring( fileSepIsAt + 1, fname.length() ) );
        }
        return fname;
    }

    /**
     * Extracts the prepended pathname part of a supposed file name string
     *
     * @param s supposed file name string
     * @return pathname part of the string (including the trailing file
     * separator); if no path, then returns "" (not null!)
     */
    public static String getPathname( String s ) {
        // assume s has no pathname attached; fname is simple filename with no path
        if ( s == null ) {
            return "";
        }
        String fname = s;
        String pathname = "";
        // first, strip off any pre-pended path information
        int fileSepIsAt = s.lastIndexOf( System.getProperty( "file.separator" ) );
        if ( fileSepIsAt > 0 ) { 	// will fail on pathnames like ":temp"
            pathname = new String( s.substring( 0, fileSepIsAt + 1 ) );
            // includes file separator
        }
        return pathname;
    }

    /**
     * Identifies the extension part of a filename string.
     *
     * @param filename any string
     * @return suffix extension (not including'.'); null if no suffix
     */
    public static String getFileExtension( String filename ) {
        int dot = filename.lastIndexOf( "." );
        if ( dot == -1 ) {
            return null;
        }
        return filename.substring( dot + 1, filename.length() );
    }

    /**
     * Identifies the extension part of a filename string.
     *
     * @param filename any string
     * @return suffix extension (not including'.'); null if no suffix
     */
    public static String stripFileExtension( String filename ) {
        int dot = filename.lastIndexOf( "." );
        if ( dot == -1 ) {
            return filename;
        }
        return filename.substring( 0, filename.lastIndexOf( '.' ) );
    }

    /**
     * Makes sure that no "illegal" characters remain in the string. Strips out
     * any illegal characters silently.
     *     
*/
    public static String makeLegalChars( String s ) {
        // TODO: update for a set of legal characters that are stripped out.
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" // alphabetric characters
                + "0123456789" /// digits
                + "@#&*{}\\-_\\./?" // punctuation
                + " " // space
                ;
        String t = new String( s.replaceAll( "[^" + validChars + "]", " " ) );
//        if ( t.indexOf('&'))
//        if ( t.indexOf( '.' ) >= 0 ) {
//            t = t.replace( '.', '_' );
//        }
//        if ( t.indexOf( '-' ) >= 0 ) {
//            t = t.replace( '-', '_' );
//        }
//        if ( t.indexOf( ' ' ) >= 0 ) {
//            t = t.replace( ' ', '_' );
//        }
//        if ( t.indexOf( '/' ) >= 0 ) {
//            t = t.replace( '/', '_' );
//        }
        return t;
    }

    /**
     * Tells whether there's a path prepended or not
     *
     * @param s supposed file name string
     * @return true if there seems to be a pathname, false otherwise
     */
    public static boolean hasPathName( String s ) {
        // assume s has no pathname attached; fname is simple filename with no path
        if ( s == null ) {
            return false;
        }
        String fname = s;
        String pathname = "";
        // first, strip off any pre-pended path information
        int fileSepIsAt = s.lastIndexOf( System.getProperty( "file.separator" ) );
        if ( fileSepIsAt > 0 ) { 	// will fail on pathnames like ":temp"
            return true;
        }
        return false;
    }

    public static String getFormattedCurrentDateTime() {
        Date now = Calendar.getInstance().getTime();
        String today = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale( "en", "us" ) ).format( now );
        return today;
    }

    /**
     * Creates a vector of a given size where every element has the same value.
     *
     * @param theSize The desired vector size
     * @param value The value of each element
     * @return a vector with the appropriate values.
     */
    public static ArrayList repeatingVector( int theSize, Object value ) {
        ArrayList rVector = new ArrayList();
        int k;

        for ( k = 0; k < theSize; k++ ) {
            rVector.add( value );	// should be a clone of value here!!!
        }
        return rVector;
    }

    public static void showArrayList( ArrayList v ) {
        charger.Global.info( "ArrayList has " + v.size() + " elements." );
        for ( int k = 0; k < v.size(); k++ ) {
            charger.Global.info( "ArrayList Element " + k + " = " + v.get( k ) );
        }
        charger.Global.info( "ArrayList finished " + v.size() + " elements." );
    }

    /**
     * Returns a printable string to show the time elapsed since a given time.
     *
     * @param time Start time
     * @return number of millisecons since the start time
     */
    public static long elapsedTime( long time ) {
        return System.currentTimeMillis() - time;
    }

    /**
     * Returns the icon image after first deciding whether to load from a jar
     * file or from a directory and class hierarchy. If a jar file, then see if
     * there's a directory named for Hub.gifpath. If Charger is run by invoking
     * its .jar file (the usual way), then this function assumes the JAR file
     * was created on a Mac or Linux system with Mac or Linux path element
     * separators. That is, on a Windows platform, it's still looking for
     * GIF/filename even though its real file name ought to be GIF\filename.
     *
     * @param gifname file name of the icon's image, relative to the classpath
     */
    public static ImageIcon getIconFromClassPath( String gifname ) {
        String classpath = System.getProperty( "java.class.path" );

        // DRP: split the classpath
        String classPathSegment[] = classpath.split( File.pathSeparator );

        ImageIcon ii = new ImageIcon();

        // For each path in the classpath, see if it's a .jar file
        for ( int cpsIndex = 0; cpsIndex < classPathSegment.length; cpsIndex++ ) {

            if ( classPathSegment[cpsIndex].endsWith( ".jar" ) ) {
                try {
                    JarResources jar = new JarResources( classPathSegment[cpsIndex] );
                    // Since the jarfiles are produced using Linux filenames at present, internal file names
                    // are separated with '/', regardless of the host where the jar is read
                    // on Mac or Linux systems, this char replacement is superfluous
                    String osXgifname = gifname.replace( File.separator.charAt( 0 ), '/' );
                    byte[] im = jar.getResource( osXgifname );
                    if ( im != null ) {
                        // ii = new ImageIcon( Toolkit.getDefaultToolkit().createImage( im ) );
                        ii = new ImageIcon( ImageIO.read( new ByteArrayInputStream( im ) ) );
                        im = null;
//                        Global.info( "Found \"" + gifname + "\" in jar file \"" + classPathSegment[cpsIndex] + "\".");
                        return ii;
                    }
                } catch ( IOException e ) {
                    Global.info( "problem loading icon from jarfile " + classPathSegment[cpsIndex]
                            + ": " + e.getMessage() );
                }
            } else {
                File imageFile = new File( "Bad_Path_Name" );
                imageFile = new File( classPathSegment[cpsIndex] + File.separator + gifname );

                if ( imageFile != null ) {
                    ii = new ImageIcon( imageFile.getAbsolutePath() );
//                    Global.info( "Found \"" + gifname + "\" in  folder \"" + classPathSegment[cpsIndex] + "\".");
                    return ii;
                } else {
                    Global.consoleMsg( "image file directory not found for "
                            + gifname );
                }

            }
        }
        return ( ii );
    }

    /**
     * Returns the contents of a file after first deciding whether to load from
     * a jar file or from a directory and class hierarchy. If Charger is run by
     * invoking its .jar file (the usual way), then this function assumes the
     * JAR file was created on a Mac or Linux system with Mac or Linux path
     * element separators. That is, on a Windows platform, it's still looking
     * for folder/filename even though its real file name ought to be
     * folder\filename.
     *
     * @param desiredFilename file name of the text file, relative to the
     * classpath
     * @param desiredFoldername folder name containing the text file
     * @return contents of the file as a string
     */
    public static String getFileFromClassPath( String desiredFoldername, String desiredFilename ) {
        String classpath = System.getProperty( "java.class.path" );

        // DRP: split the classpath
        String classPathSegment[] = classpath.split( File.pathSeparator );

        ImageIcon ii = new ImageIcon();

        // For each path in the classpath, see if it's a .jar file
        for ( int cpsIndex = 0; cpsIndex < classPathSegment.length; cpsIndex++ ) {

            if ( classPathSegment[cpsIndex].endsWith( ".jar" ) ) {
                Global.info( "jar file named: " + classPathSegment[cpsIndex] );
                try {
                    JarFile jarfile = new JarFile( classPathSegment[cpsIndex] );
                    Enumeration entries = jarfile.entries();
                    while ( entries.hasMoreElements() ) {
                        ZipEntry entry = (ZipEntry)entries.nextElement();
                        Global.info( "entry " + entry.getName() );
                    }

                    JarResources jar = new JarResources( classPathSegment[cpsIndex] );
                    // Since the jarfiles are produced using Linux filenames at present, internal file names
                    // are separated with '/', regardless of the host where the jar is read
                    // on Mac or Linux systems, this char replacement is superfluous
                    String filename = desiredFilename.replace( File.separator.charAt( 0 ), '/' );
                    String foldername = desiredFoldername;
////                    byte[] im = jar.getResource( fileName );
//                    if ( im != null ) {
//                       // ii = new ImageIcon( Toolkit.getDefaultToolkit().createImage( im ) );
//                        ii = new ImageIcon( ImageIO.read(new ByteArrayInputStream(im)));
//                        im = null;
//                        Global.info( "Found \"" + desiredFoldername + "\" in jar file \"" + classPathSegment[cpsIndex] + "\".");
////                        return ii;
//                    }
                } catch ( IOException e ) {
                    Global.info( "problem loading icon from jarfile " + classPathSegment[cpsIndex]
                            + ": " + e.getMessage() );
                }
            } else {
//                File imageFile = new File( "Bad_Path_Name" );
//                imageFile = new File( classPathSegment[cpsIndex] + File.separator + gifname );
//
//                if ( imageFile != null ) {
//                    ii = new ImageIcon( imageFile.getAbsolutePath() );
////                    Global.info( "Found \"" + gifname + "\" in  folder \"" + classPathSegment[cpsIndex] + "\".");
//                    return ii;
//                } else {
//                    Global.consoleMsg( "image file directory not found for "
//                            + gifname );
//                }
//
            }
        }
        return ( "" );
    }

    /**
     * Creates a clone of the rectangle.
     *
     * @param rect
     * @return a new instance
     */
    public static Rectangle2D.Double make2DDouble( Rectangle2D.Double rect ) {
        return (Rectangle2D.Double)rect.clone();
    }

    /**
     * Creates a clone of the point.
     *
     * @param point
     * @return a new instance
     */
    public static Point2D.Double make2DDouble( Point2D.Double point ) {
        return (Point2D.Double)point.clone();
    }

//    /**
//     * Make a new 2D rectangle (double) from a float one.
//     *
//     * @param r a float rectangle
//     * @return a 2D rectangle
//     */
//    public static Rectangle2D.Double make2DDouble( Rectangle2D.Float r ) {
//        return new Rectangle2D.Double(
//                (double)r.x,
//                (double)r.y,
//                (double)r.width,
//                (double)r.height );
//    }
//    /**
//     * Make a new 2D rectangle (float) from a double one.
//     *
//     * @param r a float rectangle
//     * @return a double rectangle
//     */
//    public static Rectangle2D.Float make2DFloat( Rectangle2D.Double r ) {
//        return new Rectangle2D.Float(
//                (float)r.x,
//                (float)r.y,
//                (float)r.width,
//                (float)r.height );
//    }
    public static Point2D.Double make2DDouble( Point2D.Float p ) {
        return new Point2D.Double( p.x, p.y );
    }

//    public static Point2D.Float make2DFloat( Point2D.Double p) {
//        return new Point2D.Float( (float)p.x, (float)p.y );
//    }
//
    /**
     * Prints a string with a prefix, deciding whether to use "a" or "an" for
     * the article, based on the string. For example
     * <code>a_or_an( "element" )</code> returns
     * <code> "an element"</code>, whereas
     * <code>a_or_an( "person" )</code> returns
     * <code> "a person"</code>
     */
    public static String a_or_an( String s ) {
        String sl = s.toLowerCase();
        String article = "a";
        if ( sl.startsWith( "a" ) ) {
            article = "an";
        }
        if ( sl.startsWith( "e" ) ) {
            article = "an";
        }
        if ( sl.startsWith( "i" ) ) {
            article = "an";
        }
        if ( sl.startsWith( "o" ) ) {
            article = "an";
        }
        if ( sl.startsWith( "u" ) ) {
            article = "an";
        }
        return article + " " + s;
    }

    /**
     * Handles the choosing of an input file by the user, with possible user
     * intervention.
     *
     * @param query the prompt string for the user
     * @param filename the initial selection of a file; <code>null</code> if
     * none is known. If it's an absolute path name, then the directory is
     * obtained from it, otherwise some implementation-dependent directory is
     * chosen.
     * @return an absolute <code>File</code>, possibly permission-protected.
     */
    public static File chooseOutputFile( String query, String filename ) {
        File destinationFile = null;
        File destinationAbsoluteFile = null;
        File destinationDirectoryFile = null;

        if ( filename != null ) {
            destinationFile = new File( filename );
            if ( destinationFile.isAbsolute() ) {
                destinationAbsoluteFile = destinationFile;
            } else {
                destinationAbsoluteFile = new File( destinationFile.getAbsoluteFile().getAbsolutePath() );
            }
            destinationDirectoryFile = destinationAbsoluteFile.getParentFile();
        }
        //else
        {
            destinationAbsoluteFile = queryForOutputFile( query, destinationDirectoryFile, filename );
        }
        return destinationAbsoluteFile;
    }

    /**
     * Handles the choosing of an input file.
     *
     * @param filename the initial selection of a file; <code>null</code> if
     * none is known
     * @param sourceDirectoryFile initial directory (user may change)
     * @param filter allows the viewing of only certain files.
     * @return an absolute <code>File</code>, possibly permission-protected.
     */
    public static File chooseInputFile( String query,
            String filename, File sourceDirectoryFile, javax.swing.filechooser.FileFilter filter ) {
        File sourceFile = null;
        File sourceAbsoluteFile = null;

        if ( filename != null ) {
            sourceFile = new File( filename );
            if ( sourceFile.isAbsolute() ) {
                sourceAbsoluteFile = sourceFile;
            } else {
                sourceAbsoluteFile = new File( sourceDirectoryFile, sourceFile.getName() );
            }
            sourceDirectoryFile = sourceAbsoluteFile.getParentFile();
        } else {
            sourceAbsoluteFile = queryForInputFile( query, sourceDirectoryFile, filter );
        }

        return sourceAbsoluteFile;
    }

    /**
     * Queries the user to find a file to be used for input.
     *
     * @param sourceDirectoryFile starting point directory (user may change)
     * @param filter allows the viewing of only certain files.
     * @return an existing file, possibly unreadable; <code>null</code> if user
     * cancels.
     */
    public static File queryForInputFile( String query, File sourceDirectoryFile, javax.swing.filechooser.FileFilter filter ) {
        File sourceFile = null;

        JFileChooser filechooser = new JFileChooser( sourceDirectoryFile );
        filechooser.setDialogTitle( query );
        filechooser.setFileFilter( filter );
        filechooser.setCurrentDirectory( charger.Global.LastFolderUsedForOpen );


        int returned = filechooser.showOpenDialog( null );

        // if approved, then continue
        if ( returned == JFileChooser.APPROVE_OPTION ) {
            sourceFile = filechooser.getSelectedFile();
            charger.Global.LastFolderUsedForOpen = sourceFile.getAbsoluteFile().getParentFile();
            //sourceDirectoryFile = filechooser.getSelectedFile().getParentFile();
            return sourceFile.getAbsoluteFile();
        } else {
            return null; // throw new CGFileException( "user cancelled." );
        }
    }

    /**
     * Queries the user to find a file to be used for input.
     *
     * @param query the title for the querying dialog
     * @param initialDirectory path for the directory where we'll start trying
     * to query
     * @param filename initial choice of where to point the saving operation
     * (user may change)
     * @return an existing file, possibly unreadable; <code>null</code> if user
     * cancels.
     */
    public static File queryForOutputFile( String query, File initialDirectory, String filename ) {
        File destinationFile = null;
        JFileChooser filechooser = new JFileChooser( initialDirectory );
        //filechooser.setFileFilter( filter );
        // filechooser.setCurrentDirectory( charger.Hub.LastFolderUsedForOpen );

        boolean fileOkay = false;
        filechooser.setDialogTitle( query );
        if ( filename != null ) {
            filechooser.setSelectedFile( new File( filename ) );
        }

        int userAnswer = JOptionPane.CLOSED_OPTION;
        File chosenOne = null;
        while ( !fileOkay ) {
            userAnswer = JOptionPane.CLOSED_OPTION;
            //Global.info( "set initially chosen file to " + filechooser.getSelectedFile().getAbsolutePath() );
            File dummy = filechooser.getSelectedFile();
            filechooser.setSelectedFile( dummy );
            userAnswer = filechooser.showSaveDialog( null );
            if ( userAnswer == JFileChooser.APPROVE_OPTION ) {
                chosenOne = filechooser.getSelectedFile();
                if ( chosenOne.exists() ) {
                    JTextArea wrappedName =
                            new JTextArea( "The file \n\"" + chosenOne.getAbsolutePath() + "\"\n\nalready exists. Replace it?", 0, 30 );
                    wrappedName.setLineWrap( true );
                    userAnswer = JOptionPane.showConfirmDialog(
                            filechooser,
                            wrappedName,
                            query,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE );

                    if ( userAnswer == JOptionPane.YES_OPTION ) {
                        fileOkay = true;
                    }
                } else {
                    fileOkay = true;
                }
            } else // user cancelled
            {
                fileOkay = true;
                chosenOne = null;
            }
        }


        // if approved, then continue
        if ( userAnswer == JFileChooser.APPROVE_OPTION ) {
            destinationFile = filechooser.getSelectedFile();
            //charger.Hub.LastFolderUsedForOpen = sourceFile.getAbsoluteFile().getParentFile();
            //sourceDirectoryFile = filechooser.getSelectedFile().getParentFile();
            return destinationFile.getAbsoluteFile();
        } else {
            return null; // throw new CGFileException( "user cancelled." );
        }
    }

    /**
     * For each column of a JTable, scans every entry and determines the maximum
     * width for the column. Also checks the header row labels.
     *
     * @param table the table to be adjusted.
     */
    public static void adjustTableColumnWidths( JTable table ) {
        TableColumn col = null;
        for ( int c = 0; c < table.getColumnCount(); c++ ) {
            col = table.getColumnModel().getColumn( c );
            Component comp = table.getTableHeader().getDefaultRenderer().
                    getTableCellRendererComponent(
                    table, col.getHeaderValue(),
                    false, false, -1, c );
            int colWidth = 0;
            for ( int r = 0; r < table.getRowCount(); r++ ) {
                //col.setPreferredWidth( 50 );


                comp = table.getDefaultRenderer( String.class ).
                        getTableCellRendererComponent(
                        table, table.getModel().getValueAt( r, c ),
                        false, false, r, c );
                colWidth = Math.max( colWidth, comp.getPreferredSize().width );
                col.setPreferredWidth( colWidth );
            }
            //comp.setBackground( new Color( 255, 240, 240 ) );
        }
    }

    public static void hideTableColumn( TableColumn col ) {
        setTableColumnWidth( col, 0 );
    }

    public static void setTableColumnWidth( TableColumn col, int width ) {
        col.setWidth( width );
        col.setMaxWidth( width );
        col.setMinWidth( width );
        col.setPreferredWidth( width );

    }

    /**
     * Splits a string according to each occurrence of a regular expression,
     * inserting a separator between each part of the string.
     *
     * @param original The original string to be split
     * @param regex A regular expression to be found in the string. Follows the
     * same rules as String#split
     * @param sep The string to be inserted between the parts (if null, then
     * this method has no effect)
     * @return a new string with the appropriate separators inserted if the
     * regex was found
     */
    public static String splitWithSeparator( String original, String regex, String sep ) {
        String[] ss = original.split( regex );

        String newString = "";
        for ( int k = 0; k < ss.length; k++ ) {
            newString += ss[ k];
            if ( k != ss.length - 1 ) // if not the last part of the string
            {
                newString += sep;
            }
        }
        return newString;
    }

    /**
     * A generic file writer, usable by any Charger class
     */
    public static void writeToFile( File f, String s, boolean append ) {
        if ( f != null ) {
            try {
                BufferedWriter out = new BufferedWriter( new FileWriter( f, append ) );
                out.write( s );
                //  Global.info( toString() );
                out.close();
            } catch ( Exception e ) {
                Global.error( e.getMessage() );
            }
        }
    }

    /**
     * Does a complete erasure of a JMenu. For each item in the menu, gets rid
     * of all listeners and removes item from menu.
     *
     * @param menu
     */
    public static void tearDownMenu( JMenu menu ) {
//            Global.info( "Tearing down " + menu.getText() );
        Component items[] = menu.getMenuComponents();
        for ( Component item : items ) {
            // remove all the component's listeners
            ComponentListener listeners[] = item.getComponentListeners();
            for ( ComponentListener listener : listeners ) {
                item.removeComponentListener( listener );
                listener = null;
            }
            // remove the menu item from the menu
            menu.remove( item );
        }
    }

    /**
     *
     * @param s
     * @param startPos the index (starting at 0) of the start of the string to
     * remove
     * @param endPos the index (starting at 0) of the start of the string to
     * remove
     * @return The resulting string after removal
     */
    public static String removeSubstring( String s, int startPos, int endPos ) {
        if ( endPos <= startPos ) {
            return s;
        }
        if ( startPos == 0 ) {
            return s.substring( endPos + 1 );
        }
        String s1 = s.substring( 0, startPos - 1 );
        String s2 = s.substring( endPos + 1 );
        return s1 + s2;
    }

    public static Point2D.Double midPoint( Point2D.Double p1, Point2D.Double p2 ) {
        return new Point2D.Double( ( p1.x + p2.x ) / 2.0f, ( p1.y + p2.y ) / 2.0f );
    }

    public static Point2D.Double get_rectangle_line_intersection( Rectangle2D.Double rect, Line2D.Double line ) {
        // get the four sides of the rectangle and find which one it is
        Line2D.Double border;
        Point2D.Double intersectPt;
        // top
        border = new Line2D.Double( rect.x, rect.y, rect.x + rect.width, rect.y );
        intersectPt = get_line_intersection( border, line );
        if ( intersectPt != null ) {
            return intersectPt;
        }
        // right

        // bottom

        // left

        return null;
    }

    public static Point2D.Double get_line_intersection( Line2D.Double pLine1, Line2D.Double pLine2 ) {

        Point2D.Double result = null;

        double s1_x = pLine1.x2 - pLine1.x1,
                s1_y = pLine1.y2 - pLine1.y1,
                s2_x = pLine2.x2 - pLine2.x1,
                s2_y = pLine2.y2 - pLine2.y1,
                s = ( -s1_y * ( pLine1.x1 - pLine2.x1 ) + s1_x * ( pLine1.y1 - pLine2.y1 ) ) / ( -s2_x * s1_y + s1_x * s2_y ),
                t = ( s2_x * ( pLine1.y1 - pLine2.y1 ) - s2_y * ( pLine1.x1 - pLine2.x1 ) ) / ( -s2_x * s1_y + s1_x * s2_y );

        if ( s >= 0 && s <= 1 && t >= 0 && t <= 1 ) {
            // Collision detected
            result = new Point2D.Double(
                    (int)( pLine1.x1 + ( t * s1_x ) ),
                    (int)( pLine1.y1 + ( t * s1_y ) ) );
        }   // end if

        return result;
    }
}
