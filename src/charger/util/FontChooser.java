/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.util;

import charger.Global;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.border.TitledBorder;

/**
 * A modal dialog to interactively select basic font characteristics.
 * 
 * @author hsd
 */
public class FontChooser extends javax.swing.JDialog {

    boolean showAll = false;
    Font myFont = null;
    String myName = null;
//    String myLogicalFontName = null;
    int myFontSize = 8;
    int myFontStyle = Font.PLAIN;
    boolean okay = true;

    /**
     * Creates new form FontChooser.
     * @param parent The frame invoking the chooser.
     * @param modal Whether to make it modal or not (required by JDialog)
     * @param font Initial settings for the dialog
     * @param label An informative string to label the dialog for different purposes
     * @param showAllFonts Whether to show the system's fonts or not.
     */
    public FontChooser( java.awt.Frame parent, boolean modal, Font font, String label, boolean showAllFonts ) {
        super( parent, modal );
        showAll = showAllFonts;
        setTheFont( font );
        initComponents();
        initFontNameMenu( fontNameMenu, Global.showAllFonts );
        initFontSizeMenu( fontSizeMenu, 8, 9, 10, 11, 12, 14, 18, 24, 36, 48, 72 );
        setSelectedItems( font );
        getContentPane().setBackground( Global.chargerBlueColor );

        chooserLabel.setText( label );
        setVisible( true );
    }

    /**
     * Load the dialog with a new font's characteristics.
     * @param f 
     */
    public void setTheFont( Font f ) {
        myName = f.getName();
        myFontStyle = f.getStyle();
        myFontSize = f.getSize();
        myFont = new Font( myName, myFontStyle, myFontSize );
    }

    /**
     * Get the font chosen by the user.
     *
     * @return chosen font; null if user canceled.
     */
    public Font getTheFont() {
        if ( okay ) {
            return myFont;
        } else {
            return null;
        }
    }

    /**
     * A list of logical font names (as in java.awt.Font)
     * If system fonts are enabled, then it will add all the system fonts at the bottom.
     *
     */
    public static void initFontNameMenu( JComboBox fontNameMenu, boolean showSystemFonts ) {
        fontNameMenu.setEnabled( false);
        fontNameMenu.removeAllItems();

        fontNameMenu.addItem( "Serif" );
        fontNameMenu.addItem( "SansSerif" );
        fontNameMenu.addItem( "Monospaced" );
        fontNameMenu.addItem( "Dialog" );
        fontNameMenu.addItem( "DialogInput" );
//        fontNameMenu.addItem( "Times New Roman" );
//        fontNameMenu.addItem( "Courier" );

        // TODO: If the current font is not found in the current five font list, then select show system fonts

        if ( showSystemFonts ) {
            fontNameMenu.addItem( "  -- System fonts --" );
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] familyNames = ge.getAvailableFontFamilyNames();
            for ( String family : familyNames ) {
                fontNameMenu.addItem( family );
            }
        }
                fontNameMenu.setEnabled( true);

    }
    
    public static void initFontSizeMenu( JComboBox fontSizeMenu, int... sizes ) {
        fontSizeMenu.setEnabled( false );
        fontSizeMenu.removeAllItems();
        for ( int size : sizes ) {
            fontSizeMenu.addItem(  "" + size );
        }
        fontSizeMenu.setEnabled( true );

    }

    /**
     * Produce a human-readable description of the font name, style and size
     * @param f the font to be described
     * @return a nice human readable string; e.g., "SansSerif, Bold, 14"
     */
    public static String getFontString( Font f ) {
        String style = null;
        if ( f.getStyle() == Font.PLAIN ) {
            style = new String( "Plain" );
        } else if ( f.getStyle() == Font.BOLD ) {
            style = new String( "Bold" );
        } else if ( f.getStyle() == Font.ITALIC ) {
            style = new String( "Italic" );
        } else if ( f.getStyle() == ( Font.BOLD + Font.ITALIC ) ) {
            style = new String( "Bold+Italic" );
        }
        return f.getFamily() + ", " + style + ", " + f.getSize();
    }

    /**
     * Set up the menus to reflect the current font characteristics.
     */
    private void setSelectedItems( Font f ) {
        fontNameMenu.setEnabled( false );
        fontStyleMenu.setEnabled( false );
        fontSizeMenu.setEnabled( false );
        setFontNameMenuSelection( f.getName() );
        setFontStyleMenuSelection( f.getStyle() );
        setFontSizeMenuSelection( f.getSize() );
        fontNameMenu.setEnabled( true );
        fontStyleMenu.setEnabled( true );
        fontSizeMenu.setEnabled( true );

    }

    /**
     * Set the chooser's font name selection menu to the current font name
     * @param name the font family name to be set. Should be restricted to one of the five platform-independent Java ones, 
     * but users may enable the system fonts on their system and have free reign.
     */
    public void setFontNameMenuSelection( String name) {
        fontNameMenu.setEnabled( false );
        fontNameMenu.setSelectedItem( name );
        fontNameMenu.setEnabled( true );
    }

    public void setFontStyleMenuSelection( int style) {

        String stylestring = (String)fontStyleMenu.getSelectedItem();
        String s = null;
        switch ( style ) {
            case Font.PLAIN:
                s = "Plain";
                break;
            case Font.BOLD:
                s = "Bold";
                break;
            case Font.ITALIC:
                s = "Italic";
                break;
            case Font.BOLD + Font.ITALIC:
                s = "Bold-Italic";
                break;
            default:
                s = "Plain";
                break;
        }

        if ( !stylestring.equals( s ) ) {
            fontStyleMenu.setEnabled( false );
            fontStyleMenu.setSelectedItem( s );
            fontStyleMenu.setEnabled( true );
        }
    }

    public void setFontSizeMenuSelection( int size ) {

            fontSizeMenu.setEnabled( false );
        fontSizeMenu.setSelectedItem( "" + size );
            fontSizeMenu.setEnabled( true );
//        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFontButton = new javax.swing.JButton();
        chooserLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sampleText = new javax.swing.JTextArea();
        fontStyleMenu = new JComboBox();
        fontSizeMenu = new JComboBox();
        fontNameMenu = new JComboBox();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Font Chooser");
        setLocation(getParent().getLocation());

        setFontButton.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        setFontButton.setText("Use Font");
        setFontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFontButtonActionPerformed(evt);
            }
        });

        chooserLabel.setFont(new Font("Lucida Grande", 0, 18)); // NOI18N
        chooserLabel.setForeground(new Color(255, 255, 255));
        chooserLabel.setText("jLabel1");

        sampleText.setColumns(20);
        sampleText.setRows(5);
        sampleText.setText("The quick brown fox jumps over the lazy dog.\nabcdefghijklmnopqrstuvwxyz\nABCDEFGHIJKLMNOPQRSTUVWXYZ");
        sampleText.setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
            "Sample Text", TitledBorder.LEFT,
            TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 12 ), Color.black ));
    jScrollPane1.setViewportView(sampleText);

    fontStyleMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italic", "Bold+Italic" }));
    fontStyleMenu.setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
        "Font Style", TitledBorder.LEFT,
        TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 12 ), Color.white ));
fontStyleMenu.addItemListener(new java.awt.event.ItemListener() {
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        fontStyleMenuItemStateChanged(evt);
    }
    });

    fontSizeMenu.setSelectedItem("10");
    fontSizeMenu.setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
        "Font Size", TitledBorder.LEFT,
        TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 12 ), Color.white ));
fontSizeMenu.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        fontSizeMenuActionPerformed(evt);
    }
    });

    fontNameMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    fontNameMenu.setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
        "Font Name", TitledBorder.LEFT,
        TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 12 ), Color.white ));
fontNameMenu.addItemListener(new java.awt.event.ItemListener() {
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        fontNameMenuItemStateChanged(evt);
    }
    });

    cancelButton.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButtonActionPerformed(evt);
        }
    });

    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(18, 18, 18)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .add(cancelButton)
                    .add(31, 31, 31)
                    .add(setFontButton))
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                .add(chooserLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .add(fontNameMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 256, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(18, 18, 18)
                    .add(fontSizeMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(18, 18, 18)
                    .add(fontStyleMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(48, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(15, 15, 15)
            .add(chooserLabel)
            .add(33, 33, 33)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
            .add(18, 18, 18)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(fontNameMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(fontSizeMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(fontStyleMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(51, 51, 51)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(setFontButton)
                .add(cancelButton))
            .add(25, 25, 25))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setFontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFontButtonActionPerformed
        okay = true;
        setVisible( false );
    }//GEN-LAST:event_setFontButtonActionPerformed

    private void fontStyleMenuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontStyleMenuItemStateChanged
        JComboBox cb = (JComboBox)evt.getSource();
        if ( !cb.isEnabled() ) {
            return;
        }
        String s = (String)cb.getSelectedItem();
        if ( s == null ) {
            myFontStyle = Font.PLAIN;
        } else if ( s.equals( "Plain" ) ) {
            myFontStyle = Font.PLAIN;
        } else if ( s.equals( "Bold" ) ) {
            myFontStyle = Font.BOLD;
        } else if ( s.equals( "Italic" ) ) {
            myFontStyle = Font.ITALIC;
        } else if ( s.equals( "Bold+Italic" ) ) {
            myFontStyle = Font.BOLD + Font.ITALIC;
        }
        myFont = new Font( myFont.getName(), myFontStyle, myFont.getSize() );
        myFontStyle = myFont.getStyle();

        sampleText.setFont( myFont );
        sampleText.repaint();
    }//GEN-LAST:event_fontStyleMenuItemStateChanged

    private void fontSizeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeMenuActionPerformed
        //        setFontSizeMenuSelection();
        myFontSize = Integer.parseInt( (String)fontSizeMenu.getSelectedItem() );
        myFont = new Font( myFont.getName(), myFont.getStyle(), myFontSize );
        sampleText.setFont( myFont );
    }//GEN-LAST:event_fontSizeMenuActionPerformed

    private void fontNameMenuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontNameMenuItemStateChanged
        JComboBox cb = (JComboBox)evt.getSource();
        if ( !cb.isEnabled() ) {
            return;
        }
        String fontname = (String)cb.getSelectedItem();
        //        setFontNameMenuSelection();
        if ( fontname != null ) {
            myName = new String( fontname );
            myFont = new Font( myName, myFont.getStyle(), myFont.getSize() );
            sampleText.setFont( myFont );
            sampleText.repaint();
        }
    }//GEN-LAST:event_fontNameMenuItemStateChanged

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        okay = false;
        setVisible( false );
    }//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel chooserLabel;
    private JComboBox fontNameMenu;
    private JComboBox fontSizeMenu;
    private JComboBox fontStyleMenu;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea sampleText;
    private javax.swing.JButton setFontButton;
    // End of variables declaration//GEN-END:variables
}
