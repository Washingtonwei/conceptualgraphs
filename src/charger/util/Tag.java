/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.util;

/**
 * Some static values and methods to help with html formatting
 * In general values beginnig with an underscore "_" are ending tags.
 * @author Harry Delugach
 * @since Charger 3.8.0
 */
public class Tag {
        /** The &lt;p&gt; tag */
  public static final String p = "<p>";
         /** The &lt;/p&gt; tag  with a regular ASCII newline */
 public static final String _p = "</p>\n";
  
  public static final String p_red = "<p style=\"color:red;\">";
  
  public static final String italic = "<em>";
  public static final String _italic = "</em>";
  
         /** The &lt;br&gt; tag  with a regular ASCII newline */
  public static final String br = "<br>\n";
  
         /** The &lt;/strong&gt; tag   */
  public static final String bold = "<strong>";
         /** The &lt;/strong&gt; tag   */
  public static final String _bold = "</strong>";
  
          /** The &lt;ol&gt; tag  with a regular ASCII newline */
 public static final String olist = "<ol>\n";
          /** The &lt;ol&gt; tag  with a regular ASCII newline */
 public static final String _olist = "</ol>\n";

         /** The &lt;ul&gt; tag  with a regular ASCII newline */
  public static final String ulist = "<ul>\n";
         /** The &lt;/ul&gt; tag  with a regular ASCII newline */
  public static final String _ulist = "</ul>\n";
  

         /** The &lt;li&gt; tag  */
  public static final String li = "<li>";
         /** The &lt;/li&gt; tag  with a regular ASCII newline */
  public static final String _li = "</li>\n";
  
        /** a non-breaking space */
  public static final String sp = "&nbsp;";
  
  public static final String hr = "<hr style=\"size=6px\">";
  
  public static final String body = "<body style=\"font-family:Arial; \">";
  
  
  public static final String _body = "</body>";
  
  public static final String _table = "</table>";
  
  public static final String lightPink = "#FFCCCC";
  public static final String lighterPink = "#FFDDDD";
  public static final String lightYellow = "#FFFFBB";
  public static final String lighterYellow = "#FFFFDD";
  public static final String lightGreen = "#CCFFCC";
  public static final String lighterGreen = "#DDFFDD";
  
  
    
  /** return a paragraph-tagged-terminated string */
  public static String p( String s ) {
      return p + s + _p;
  }
  
  public static String p_red( String s ) {
      return p_red + s + _p;
  }

  /** return a bold-tagged-terminated string */
  public static String bold( String s ) {
      return bold + s + _bold;
  }
  /** return an italic-tagged-terminated string */
  public static String italic( String s ) {
      return italic + s + _italic;
  }
  
  public static String item( String s ) {
      return li + s + _li;
  }
  
  public static String sp( int spaces ) {
      StringBuilder r = new StringBuilder("");
      for ( int i = 0; i < spaces; i++) 
          r.append( sp );
      return r.toString();
  }
  
  public static String comment( String s ) {
      String news = s;
      /*news = news.replace( "\"", "&quot;");
      news = news.replace( "<", "&lt;" );
      news = news.replace( ">", "&gt;" );*/
      return "<!-- " + news + " -->\n";
  }
  
  /**
   * Creates a begin table tag with the width specified. The end tag is NOT accounted for.
   * @param width
   * @return The tag with a width
   */
  public static String table( int width ) {
      return "<table width=" + width + " >\n";
      
  }
  
  /** a  table row begin-end tag */
  public static String tr( String s ) {
      return "<tr>\n" + s + "\n</tr>\n";
  }
  
  /** a table cell begin-end tag */
  public static String td( String s ) {
      return "<td>" + s + "</td>\n";
  }
  
  /** Brackets a &lt;div&gt; begin-end tag, setting a color style. 
   * @param color Any string suitable for HTML color values
   * @param s The string to be bracketed in color
   * */
  
  public static String colorDiv( String color, String s ) {
      return "\n<div style=\"background: " + color + "\">\n" + s + "</div>\n";
  }
  
  /** A table cell that is centered */
  public static String tdc( String s ) {
      return "<td><div style=\"text-align:center;\">" + s + "</div></td>\n";
  }
  
  /** A table cell that is right justified */
  public static String tdr( String s ) {
      return "<td><div style=\"text-align:right;\">" + s + "</div></td>\n";
  }
  
  /**
   * Returns a table data element that spans cols number of columns around the string 
   * @param s
   * @param cols
   * @return HTML tagged string suitable for inclusion in an HTML table
   */
  public static String tdspan( int cols, String s ) {
      return "<td colspan=\"" + cols + "\"> " + s + "</td>\n";
  }
  
  /** Creates an html header at the level specified */
  public static String h( int level, String s ) {
      return "<h" + level + "> " + s + "</h" + level + ">\n";
  }

}
