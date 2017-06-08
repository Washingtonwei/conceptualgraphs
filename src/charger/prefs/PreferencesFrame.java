/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.prefs;

import charger.EditFrame;
import charger.EditManager;
import charger.Global;
import charger.exception.CGFileException;
import charger.obj.GEdge;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.util.FontChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * The enclosing window for all the preference panels. 
 * It has some of its own functionality: the save preferences and close buttons, for example.
 * It also displays the configuration file's path name.
 * @author hsd
 */
public class PreferencesFrame extends javax.swing.JFrame {

    AppearancePrefPanel appearance = new AppearancePrefPanel( this );
    CompatibilityPrefPanel compat = new CompatibilityPrefPanel();
    ActorsPrefPanel actors = new ActorsPrefPanel();
    CraftPrefPanel craftPanel = new CraftPrefPanel();
    AdminPrefPanel adminPanel = new AdminPrefPanel();
    
    PreferencesWriter writer = new PreferencesWriter(appearance, compat, actors, craftPanel, adminPanel );

    /**
     * Creates new form PreferencesFrame
     */
    public PreferencesFrame() {
        initComponents();
        prefFileName.setText( writer.getPrefFile().getAbsolutePath());
        getContentPane().setBackground( Global.chargerBlueColor );
//                getContentPane().setBackground( Color.white )6;
        mainPane.addTab( "Appearance", appearance );
        mainPane.addTab( "Compatibility", compat );
        mainPane.addTab( "Actors", actors );
        enableCraftPanel( Global.craftEnabled );
        if ( Global.infoOn )
            mainPane.addTab( "Admin", adminPanel );
    }

    public void enableCraftPanel( boolean setting ) {
        if ( setting ) {
            mainPane.addTab( "CRAFT Settings", craftPanel );
        } else {
            mainPane.remove( craftPanel );
        }
    }

    public void fontChanged() {
        appearance.colorPanel.refreshSampleObject();
    }
    /**
     * The summarize action that can be re-used in other places
     */
    public Action chooseWorkingDirectoryAction = new AbstractAction() {
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return charger.Global.strs( "ChooseWorkingDirectoryLabel" );
            }
            return super.getValue( s );
        }

        public void actionPerformed( ActionEvent e ) {
            performQueryForWorkingDirectory();
        }
    };

    private void performQueryForWorkingDirectory() {
        File newF = Global.queryForFolder( this, Global.GraphFolderFile,
                "Choose any file to use its parent directory for graph, grids and database (txt) files" );
        if ( newF == null ) {
            return;
        } else {
            Global.GraphFolder = newF.getAbsolutePath();
//            Global.GraphFolderFile = new File( Global.GraphFolder );
            Global.setGraphFolder(  Global.GraphFolder, true );
            compat.GraphFolderField.setText( Global.GraphFolder );

            Global.DatabaseFolder = newF.getAbsolutePath();
            Global.DatabaseFolderFile = new File( Global.GraphFolder );
            compat.wordnetDictField.setText( Global.GraphFolder );

            if ( Global.craftEnabled ) {
                Global.craftModule.setGridFolderFile( newF );
                craftPanel.gridFolderField.setText( Global.GraphFolder );
            }
        }
        this.validate();
        repaint();
    }

    /**
     * Converts the text in a field to its non-negative integer value.
     *
     * @param f the textfield containing the original string
     * @param defaultvalue Value to return if there's something wrong
     * @return the integer value for the string.
     */
    public static int getNonNegativeIntFromField( JTextField f, int defaultvalue ) {
        try {
            int inputnum = Integer.parseInt( f.getText() );
            if ( inputnum < 0 ) {
                inputnum = 0;
            }
            f.setText( "" + inputnum );
            return inputnum;
            //Global.info( " contextBorderWidth set to " + Graph.contextBorderWidth );
        } catch ( Exception exc ) {
            f.setText( "" + defaultvalue );
        }
        return defaultvalue;
    }

//    /**
//     * Converts the text in a field to its non-negative integer value.
//     *
//     * @param f
//     * @param defaultvalue Value to return if there's something wrong
//     * @return
//     */
//    public static float getNonNegativeFloatFromField( JTextField f, float defaultvalue ) {
//        try {
//            float inputnum = Float.parseFloat( f.getText() );
//            if ( inputnum < 0 ) {
//                inputnum = 0;
//            }
//            f.setText( "" + inputnum );
//            return inputnum;
//            //Global.info( " contextBorderWidth set to " + Graph.contextBorderWidth );
//        } catch ( Exception exc ) {
//            f.setText( "" + defaultvalue );
//        }
//        return defaultvalue;
//    }

    /**
     * Converts the text in a field to its non-negative integer value.
     *
     * @param f the textfield containing the original string
     * @param defaultvalue Value to return if there's something wrong
     * @return the double value for the string.
     */
    public static double getNonNegativeDoubleFromField( JTextField f, double defaultvalue ) {
        try {
            double inputnum = Double.parseDouble( f.getText() );
            if ( inputnum < 0 ) {
                inputnum = 0;
            }
            f.setText( "" + inputnum );
            return inputnum;
            //Global.info( " contextBorderWidth set to " + Graph.contextBorderWidth );
        } catch ( Exception exc ) {
            f.setText( "" + defaultvalue );
        }
        return defaultvalue;
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        savePreferencesButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        mainPane = new javax.swing.JTabbedPane();
        setAllFolders = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        prefFileName = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("CharGer Preferences");
        setBackground(Global.chargerBlueColor);
        setMaximumSize(new java.awt.Dimension(800, 700));
        setMinimumSize(new java.awt.Dimension(800, 700));
        setPreferredSize(new java.awt.Dimension(800, 700));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        savePreferencesButton.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        savePreferencesButton.setText("Make Preferences Permanent");
        savePreferencesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                savePreferencesButtonActionPerformed(evt);
            }
        });

        jLabel1.setBackground(Global.chargerBlueColor);
        jLabel1.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        jLabel1.setForeground(new Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("CharGer Preferences File:");

        mainPane.setBackground(new Color(255, 255, 255));
        mainPane.setMaximumSize(new java.awt.Dimension(800, 500));
        mainPane.setMinimumSize(new java.awt.Dimension(800, 500));
        mainPane.setOpaque(true);
        mainPane.setPreferredSize(new java.awt.Dimension(800, 500));

        setAllFolders.setText("Set all folders (graphs, databases, grids, etc.)");
        setAllFolders.setToolTipText("");
        setAllFolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setAllFoldersActionPerformed(evt);
            }
        });

        closeButton.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        prefFileName.setBackground(new Color(255, 255, 255));
        prefFileName.setText("jLabel2");
        prefFileName.setOpaque(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(setAllFolders, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 319, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(49, 49, 49)
                        .add(savePreferencesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 234, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(59, 59, 59)
                        .add(closeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mainPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 749, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(prefFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 574, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(prefFileName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(setAllFolders, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(savePreferencesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(closeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        FontChooser chooser = appearance.getFontChooser();
        if ( chooser != null ) {
            if ( chooser.isVisible() ) {
                chooser.toFront();
                chooser.setFont( Global.defaultFont );
            }
        }
    }//GEN-LAST:event_formWindowActivated

    private void savePreferencesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_savePreferencesButtonActionPerformed
        try {
            //BufferedWriter out = null;
//            String prefsFilename = "Test.conf";
            String prefsFilename = Global.userPreferencesFilename;
            writer.savePreferences( prefsFilename );
        } catch ( CGFileException ex ) {
            Global.error( "Error while saving preferences: " + ex.getMessage() );
        }
    }//GEN-LAST:event_savePreferencesButtonActionPerformed

    private void setAllFoldersActionPerformed(ActionEvent evt) {//GEN-FIRST:event_setAllFoldersActionPerformed
        File newF = Global.queryForFolder( this, Global.GraphFolderFile,
                "Choose any file to use its parent directory for graph, grids and database (txt) files" );
        if ( newF == null ) {
            return;
        } else {
            Global.GraphFolder = newF.getAbsolutePath();
//            Global.GraphFolderFile = new File( Global.GraphFolder );
            Global.setGraphFolder(  Global.GraphFolder, true );
            compat.GraphFolderField.setText( Global.GraphFolder );


            Global.DatabaseFolder = Global.GraphFolder;
            Global.DatabaseFolderFile = Global.GraphFolderFile;
            actors.DatabaseFolderField.setText( Global.DatabaseFolder );

            if ( Global.craftEnabled ) {
                Global.craftModule.setGridFolderFile( Global.GraphFolderFile );
                craftPanel.gridFolderField.setText( Global.GraphFolder );

            }
        }
        this.validate();
        repaint();
    }//GEN-LAST:event_setAllFoldersActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        setVisible( false );
        if ( appearance.getFontChooser() != null ) {
            appearance.getFontChooser().setVisible( false );
            appearance.getFontChooser().dispose();
        }
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        setVisible( false );
        if ( appearance.getFontChooser() != null ) {
            appearance.getFontChooser().setVisible( false );
            appearance.getFontChooser().dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    private void closeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible( false );
        if ( appearance.getFontChooser() != null ) {
            appearance.getFontChooser().setVisible( false );
            appearance.getFontChooser().dispose();
        }
    }//GEN-LAST:event_closeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTabbedPane mainPane;
    private javax.swing.JLabel prefFileName;
    private javax.swing.JButton savePreferencesButton;
    private javax.swing.JButton setAllFolders;
    // End of variables declaration//GEN-END:variables
}
