//
//  WNUtil.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Jun 25 2003.
//
package charger.gloss.wn;

import charger.gloss.GenericTypeDescriptor;
import charger.gloss.AbstractTypeDescriptor;
import repgrid.*;
import charger.obj.*;
import repgrid.tracks.*;

import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

/* 
 $Header$ 
 */
/*
 CharGer - Conceptual Graph Editor
 Copyright reserved 1998-2014 by Harry S. Delugach
        
 This package is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version. This package is 
 distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 details. You should have received a copy of the GNU Lesser General Public
 License along with this package; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 * A collection of static utilities for use by various Wordnet-related routines.
 */
public class WNUtil {

    /**
     * Word delimiters used by various routines. Current set is any of the
     * characters: " ;,\t."
     */
    public static String delimiters = " ;,\t.";

    /**
     * Tries a few heuristics to break up a "word" into its parts. Some examples
     * are:
     * <table>
     * <tr>
     * <td>Phrase <td>Result
     * <tr>
     * <td><code>Physical_System</code><td><code>{ "physical", "system" }</code>
     * <tr>
     * <td><code>tempSensor</code><td><code>{ "temp", "sensor" }</code>
     * </table>
     *
     * @param phrase to be guessed from.
     * @return its best guess as the words
     * @see #delimiters
     */
    public static String[] guessWordsFromPhrase( String phrase ) {
        ArrayList words = new ArrayList();
        char[] chars = phrase.toCharArray();
        StringBuilder s = new StringBuilder( "" );
        boolean upper = Character.isUpperCase( chars[0] );
        int runlength = 0;
        for ( int k = 0; k < chars.length; k++ ) {
            char c = chars[k];
            if ( c == '_' || c == '-' ) {
                words.add( s.toString().toLowerCase() );
                s = new StringBuilder( "" );
                upper = false;
            } else {
                if ( Character.isUpperCase( c ) && !upper ) {
                    if ( s.length() > 0 ) {
                        words.add( s.toString().toLowerCase() );
                    }
                    s = new StringBuilder( "" );
                }
                upper = Character.isUpperCase( c );
                s.append( chars[k] );
            }
        }
        if ( s.length() > 0 ) {
            words.add( s.toString().toLowerCase() );
        }

        return (String[])words.toArray( new String[ 0 ] );
    }

    /**
     * Counts up the number of words in a term, where a word is considered any
     * sequence of characters that doesn't include the delimiter set from
     * WNUtil.delimiters.
     *
     * @param s string where terms will be found
     * @param smart try to break up words by guessing
     * @return number of words
     * @see #guessWordsFromPhrase
     * @see #delimiters
     */
    public static int wordCount( String s, boolean smart ) {
        if ( smart ) {
            String[] ws = guessWordsFromPhrase( s );
            return ws.length;
        } else {
            StringTokenizer t = new StringTokenizer( s, WNUtil.delimiters );
            return t.countTokens();
        }
    }

    /**
     * Builds a JTable showing a list of type descriptors.
     *
     * @param descriptors zero or more type descriptors.
     * @return a JTable with a backing table model.
     */
    public static JTable getDescriptorTable( AbstractTypeDescriptor[] descriptors ) {
        Vector dvector = new Vector();
        for ( int k = 0; k < descriptors.length; k++ ) {
            dvector.add( getDescriptorVector( k, descriptors[k] ) );
        }

        TableModel readonlymodel
                = new DefaultTableModel( dvector, getDescriptorColumnLabels() ) {
                    public boolean isCellEditable( int rowIndex, int columnIndex ) {
                        return false;
                    }
                };
				//craft.Craft.say( "table created with " + dvector.size() + " rows" );
        //JTable tab = new JTable( readonlymodel );
        //charger.util.Util.adjustTableColumnWidths( tab );
        charger.util.TableSorter sorter = new charger.util.TableSorter( readonlymodel );
        JTable tab = new JTable( sorter );
        sorter.addMouseListenerToHeaderInTable( tab );

        charger.util.Util.setTableColumnWidth( tab.getColumn( "Type" ), 60 );
        charger.util.Util.setTableColumnWidth( tab.getColumn( "Term" ), 120 );
        charger.util.Util.setTableColumnWidth( tab.getColumn( "POS" ), 60 );
        charger.util.Util.setTableColumnWidth( tab.getColumn( "Seq" ), 60 );

        TableColumn col = null;
        for ( int c = 0; c < tab.getColumnCount(); c++ ) {
            col = tab.getColumnModel().getColumn( c );
            Component comp = tab.getTableHeader().getDefaultRenderer().
                    getTableCellRendererComponent(
                            tab, col.getHeaderValue(),
                            false, false, -1, c );
            //((JLabel)comp).setHorizontalAlignment( SwingConstants.CENTER );
            for ( int r = 0; r < tab.getRowCount(); r++ ) {
                comp = tab.getDefaultRenderer( String.class ).
                        getTableCellRendererComponent(
                                tab, tab.getModel().getValueAt( r, c ),
                                false, false, r, c );
				//comp.setBackground( new Color( 255, 240, 240 ) );
                //((JLabel)comp).setHorizontalAlignment( SwingConstants.CENTER );
                //colWidth = Math.max( colWidth, comp.getPreferredSize().width );
                //col.setPreferredWidth(  colWidth );
            }
        }
        return tab;

    }

    private static Vector getDescriptorColumnLabels() // Vector is required by DefaultTableModel
    {
        Vector v = new Vector();
        v.add( "Seq" );
        v.add( "Term" );
        v.add( "POS" );
        v.add( "Definition" );
        v.add( "Type" );
        return v;
    }

    private static Vector getDescriptorVector( int sequence, AbstractTypeDescriptor d ) {
        Vector v = new Vector();
        v.add( Integer.toString( sequence ) );
        v.add( d.getLabel() );
        v.add( d.getPOS() );
        v.add( d.getDefinition() );
        if ( d instanceof WordnetTypeDescriptor ) {
            v.add( "Wordnet" );
        }
        if ( d instanceof GenericTypeDescriptor ) {
            v.add( "generic" );
        }
        //craft.Craft.say( "get descriptor vector, v size is " + v.size() );
        return v;
    }

    /**
     * Query the user for a new graphs folder.
     *
     * @param f parent frame for the dialog.
     */
    public static void queryForDictFolder( JFrame f ) {
        File newF = charger.Global.queryForFolder(f, new File( charger.gloss.wn.WordnetManager.wordnetDictionaryFilename ),
                "Choose any file to use its parent directory as Wordnet's dictionary file" );
        if ( newF == null ) {
            return;
        } else {
            charger.gloss.wn.WordnetManager.wordnetDictionaryFilename = newF.getAbsolutePath();
        }
    }

}
