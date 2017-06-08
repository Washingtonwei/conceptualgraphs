/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.undo.UndoStateManager;
import charger.undo.Undoable;
import charger.undo.UndoableState;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultEditorKit;

/**
 * Manages the editing of an MMAT type-label-capable synonym group. A synonym
 group consists of one or more synonym clusters, each of which is akin to a synset
 in Wordnet -- i.e., a list of terms that are all considered synonymClusters to each
 other. There can be more than two terms in a cluster. At present, this editor is
 constrained to handle only typed synonym cluster objects, although clusters without a
 type (empty string) work.
 *
 * @author hsd
 */
public class SynonymEditor extends javax.swing.JFrame implements Undoable {

    public ClusterCollection allSynonyms = new ClusterCollection();
    public DefaultListModel listModel = new DefaultListModel();
    public DefaultListModel duplicateListModel = new DefaultListModel();
    private boolean needToSave = false;
//    private SynonymEditStateMgr stateMgr = new SynonymEditStateMgr( this, 15 );
    public UndoStateManager stateMgr = new UndoStateManager( this, 15 );
    /**
     * Used to compare so that we can tell when something has actually changed
     * in the pane
     */
    public String loadedEditorPaneText = null;
    /** To reflect whether the editor pane has been modified. */
    boolean editorNeedsSaving = false;

    /**
     * Creates new form SynonymEditor. Initializes the components, sets the
     * title, and unit name.
     */
    public SynonymEditor( String unitTypeName ) {
        initComponents();
        this.getContentPane().setBackground( Global.chargerBlueColor );
        listModel = (DefaultListModel)synonymList.getModel();
        setTitle( unitTypeName + " synonyms" );
        unitNameField.setText( unitTypeName );
//        Action[] actions = this.contentEditorPane.getActions();
        setVisible( true );
    }

    /**
     * Fills the editor's synonym group with the clusters from the file. Sets the
     * editor window title to the filename. Identifies and displays any
     * duplicate terms.
     *
     * @param f
     * @see SynonymEditor#fillListOfDuplicates() 
     */
    public void loadSynonyms( File f ) {
        allSynonyms.loadFromFile( f );
        setTitle( f.getName() );
        fillListFromSynonymGroup();
        stateMgr.resetAndMark();
    }

    /**
     * Initializes the editor fields. Assumes that synonymClusters have been correctly
 loaded into the synonym group.
     *
     */
    public void fillListFromSynonymGroup() {
        listModel.clear();
        for ( MTypedSynonymCluster cluster : allSynonyms.synonymClusters ) {
            listModel.addElement( cluster.toString() );
        }
        contentEditorPane.setText( "" );
        editorNeedsSaving = false;
        loadedEditorPaneText = "";
        fillListOfDuplicates();

    }

    /**
     * Re-load the list of duplicate terms.
     */
    public void fillListOfDuplicates() {
        duplicateListModel.clear();
        for ( String s : allSynonyms.findDuplicates() ) {
            duplicateListModel.addElement( s );
        }
    }

    /**
     * Get this editor's local synonym group.
     */
    public ClusterCollection getSynonymGroup() {
        return allSynonyms;
    }

    /**
     * Converts the JList of synonymClusters to a single string.
     *
     * @return a text version of the synonymClusters, same format as the file 
     */
    public String getSynonymsFromList() {
        String out = "";
        for ( int snum = 0; snum < listModel.size(); snum++ ) {
            out += listModel.getElementAt( snum ) + "\n";
        }
        return out;
    }

    /** Prompts the user if the editor needs saving before some other operation. */
    public void checkForSavingEditor() {
        if ( editorNeedsSaving && !contentEditorPane.getText().trim().equals( "" ) ) {
            int result = JOptionPane.showConfirmDialog( null, "Update this synonym set before editing another one?" );
            if ( result == JOptionPane.YES_OPTION ) {
                updateListModelFromEditor();
            }
        }
    }
    /**
     * Abstraction of the steps needed to create a new synonym cluster within the
     * edited synonym group.
     */
    public Action newClusterAction = new AbstractAction() {
        @Override
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return "New synonym cluster";
            }
            return super.getValue( s );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            checkForSavingEditor();

            synonymList.clearSelection();
//            selectedIndex = -1;
            contentEditorPane.setText( formatForEditing( "TYPE: term1, term2" ) );
            needToSave = true;
//            updateEditorFromListModel();
            stateMgr.markAfterUndoableStep();

        }
    };
    /**
     * Action to remove the selected cluster from the editor and from the local
     * synonym group.
     */
    public Action deleteClusterAction = new AbstractAction() {
        @Override
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return "Delete synonym cluster...";
            }
            return super.getValue( s );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if ( synonymList.getSelectedValue() == null ) {
                JOptionPane.showMessageDialog( null, "No synonym cluster is selected." );
                return;
            }

            int result = JOptionPane.showConfirmDialog( null, "Delete synonyms?\n" + synonymList.getSelectedValue() );
            if ( result == JOptionPane.YES_OPTION ) {
                allSynonyms.deleteSynonymCluster( synonymList.getSelectedIndex() );
                needToSave = true;
                synonymList.clearSelection();
//                selectedIndex = synonymList.getSelectedIndex();
                fillListFromSynonymGroup();
                stateMgr.markAfterUndoableStep();
                            // TODO: Undo framework is trashing the state that exists prior to deleting the last line!!
            }
        }
    };

    /**
     * Performs whatever clean up is needed when the window is requested to
     * close. If there are unsaved edits, the user is prompted to save them. If
     * canceled, then the close is canceled. If user picks no, then unsaved
     * changes are lost. If user picks yes, changes are saved and window closes.
     */
    public void closeOut() {
        if ( !needToSave && !editorNeedsSaving ) {
            dispose();
        } else {
            int result = JOptionPane.showConfirmDialog( this, "Changes not saved. Do you want to save them?" );
            if ( result == JOptionPane.YES_OPTION ) {
                if ( editorNeedsSaving ) 
                    updateListModelFromEditor();
                allSynonyms.writeCollectionToFile();
                dispose();
            } else if ( result == JOptionPane.CANCEL_OPTION ) {
                return;
            } else {
                dispose();
            }
        }
                // TODO: Somehow notify the caller that this window has closed.
    }

    /**
     * Assumes that selectedIndex hasn't changed since user selected something.
     * Updates the list model and also updates the original synonym list.
     * If the synonym cluster is a new one, adds it to the end of the list model 
     * and also adds it to the synonym group.
     */
    public void updateListModelFromEditor() {
        if ( contentEditorPane.getText().equals( loadedEditorPaneText ) ) {
            return;
        }
        if ( synonymList.isSelectionEmpty() ) {      // new synonym cluster should be added
            MTypedSynonymCluster cluster = new MTypedSynonymCluster( formatForList( contentEditorPane.getText() ) );
            allSynonyms.addSynonymCluster( cluster );
            listModel.addElement( formatForList( cluster.toString() ) );

        } else {        // modifying current synonym cluster
            loadedEditorPaneText = contentEditorPane.getText();
            Global.info( "at update list model: selected index is " + synonymList.getSelectedIndex() );
            listModel.setElementAt( formatForList( contentEditorPane.getText() ), synonymList.getSelectedIndex() );
            allSynonyms.synonymClusters.get( synonymList.getSelectedIndex() ).fromString( formatForList( contentEditorPane.getText() ) );
            needToSave = true;
            fillListOfDuplicates();
        }
        editorNeedsSaving = false;
        stateMgr.markAfterUndoableStep();
    }

    /**
     * Loads the editor pane with the editable synonym list, formatted for easy
     * viewing and searching. Does not change any internal synonym structures.
     */
    private void updateEditorFromListModel() {
        if ( synonymList.getSelectedValue() == null ) {
            return;
        }
        checkForSavingEditor();
//                Global.info( "at update editor: selected index is " + selectedIndex );
        loadedEditorPaneText = formatForEditing( (String)synonymList.getSelectedValue() );
        contentEditorPane.setText( loadedEditorPaneText );
        editorNeedsSaving = false;
        contentEditorPane.requestFocus();
    }

    /**
     * Alphabetizes and puts one synonym per line. This makes it easier to see
 whether synonymClusters have been used.
     *
     * @param s
     * @return a legal synonym string, but with comma and newline separating each term.
     */
    private String formatForEditing( String s ) {
        String indent = "  ";
        // break into type and terms
        String parts[] = s.split( ":" );
        String terms[];
        String type = "";
        if ( parts.length == 2 ) {
            type = parts[0];
            terms = parts[1].split( "," );
        } else {
            terms = parts[0].split( "," );
        }

        for ( int k = 0; k < terms.length; k++ ) {
            terms[k] = terms[k].trim();
        }

        Arrays.sort( terms );

        String out = type.equals( "" ) ? "" : type + ":\n";
        for ( int k = 0; k < terms.length; k++ ) {
            out += indent + terms[k] + ( k == ( terms.length - 1 ) ? "\n" : ",\n" );
        }
        return out; // + formatForList( out );
    }

    /**
     * Prepare a single synonym cluster for display in a JList.
     *
     * @param s
     * @return formatted synonym line suitable for display in a JList.
     */
    private String formatForList( String s ) {
        String parts[] = s.split( ":" );
        String termpart = null;
        String type = "";
        if ( parts.length == 2 ) {
            type = parts[0];
            termpart = parts[1].replace( "\n", "," );
        } else {
            termpart = parts[0].replace( "\n", "," );
        }
        MTypedSynonymCluster tempCluster = new MTypedSynonymCluster( ( type.equals( "" ) ? "" : type + ": " ) + termpart );
        return tempCluster.toString();
    }
    
    /** Make a complete copy of the current editor state (for use in possible undo/redo) */
    public SynonymEditorState currentState() {
        SynonymEditorState state = new SynonymEditorState();
        state.setSynonyms( allSynonyms );
        state.setEditorPaneText( contentEditorPane.getText() );
        return state;
    }
    
        /** Restore current state from a complete copy  (for use in possible undo/redo) */
    public void restoreState( UndoableState astate ) {
        SynonymEditorState state = (SynonymEditorState)astate;
        allSynonyms = state.getSynonyms();
        fillListFromSynonymGroup();
        contentEditorPane.setText( state.getEditorPaneText() );
        loadedEditorPaneText = contentEditorPane.getText();
    }
    
    public void setupMenus() {
//        showStatus();
        menuUndo.setEnabled( stateMgr.undoAvailable() );
        menuRedo.setEnabled( stateMgr.redoAvailable() );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        unitNameField = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        synonymList = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentEditorPane = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        buttonAccept = new javax.swing.JButton();
        buttonRevert = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        menuRevertToSaved = new javax.swing.JMenuItem();
        menuSave = new javax.swing.JMenuItem();
        menuClose = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        menuUndo = new javax.swing.JMenuItem();
        menuRedo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuCut = new javax.swing.JMenuItem();
        menuCopy = new javax.swing.JMenuItem();
        menuPaste = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        menuNewCluster = new javax.swing.JMenuItem();
        menuDeleteCluster = new javax.swing.JMenuItem();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocation(new java.awt.Point(150, 150));
        setPreferredSize(new java.awt.Dimension(760, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        unitNameField.setBackground(Global.chargerBlueColor);
        unitNameField.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        unitNameField.setForeground(new java.awt.Color(255, 255, 255));
        unitNameField.setText("Unit name");
        unitNameField.setOpaque(true);
        getContentPane().add(unitNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 147, -1));

        synonymList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        synonymList.setModel(listModel);
        synonymList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        synonymList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                synonymListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(synonymList);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 20, 540, 150));

        contentEditorPane.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        contentEditorPane.setPreferredSize(new java.awt.Dimension(300, 300));
        contentEditorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                contentEditorPaneFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(contentEditorPane);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 340, 290));

        jLabel1.setBackground(Global.chargerBlueColor);
        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Synonym Editor");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 150, -1));

        jLabel2.setBackground(Global.chargerBlueColor);
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Current sets:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, 140, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, -1, -1));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, -1, -1));
        getContentPane().add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 100, -1, -1));

        jLabel3.setBackground(Global.chargerBlueColor);
        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Duplicate terms in more than one cluster");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 220, 260, -1));

        jLabel4.setBackground(Global.chargerBlueColor);
        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Clusters:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 90, 130, -1));

        jList1.setModel(duplicateListModel);
        jScrollPane3.setViewportView(jList1);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 250, 270, 200));

        jLabel5.setBackground(Global.chargerBlueColor);
        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Edit the cluster's terms:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 180, -1));

        buttonAccept.setText("Accept");
        buttonAccept.setToolTipText("Commit editor changes to the list");
        buttonAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonAcceptActionPerformed(evt);
            }
        });
        getContentPane().add(buttonAccept, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 190, 80, 30));

        buttonRevert.setText("Revert");
        buttonRevert.setToolTipText("Start this set over");
        buttonRevert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buttonRevertActionPerformed(evt);
            }
        });
        getContentPane().add(buttonRevert, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 230, 80, 30));

        jMenu3.setText("File");

        menuRevertToSaved.setText("Revert to saved");
        menuRevertToSaved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuRevertToSavedActionPerformed(evt);
            }
        });
        jMenu3.add(menuRevertToSaved);

        menuSave.setText("Save");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        jMenu3.add(menuSave);

        menuClose.setText("Close");
        menuClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuCloseActionPerformed(evt);
            }
        });
        jMenu3.add(menuClose);

        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");

        menuUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.META_MASK));
        menuUndo.setText("Undo");
        menuUndo.setEnabled(false);
        menuUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuUndoActionPerformed(evt);
            }
        });
        jMenu4.add(menuUndo);

        menuRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        menuRedo.setText("Redo");
        menuRedo.setEnabled(false);
        menuRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuRedoActionPerformed(evt);
            }
        });
        jMenu4.add(menuRedo);
        jMenu4.add(jSeparator1);

        menuCut.setAction(contentEditorPane.getActionMap().get(DefaultEditorKit.cutAction));
        menuCut.setText("Cut");
        jMenu4.add(menuCut);

        menuCopy.setAction(contentEditorPane.getActionMap().get(DefaultEditorKit.copyAction));
        menuCopy.setText("Copy");
        jMenu4.add(menuCopy);

        menuPaste.setAction(contentEditorPane.getActionMap().get(DefaultEditorKit.pasteAction));
        menuPaste.setText("Paste");
        jMenu4.add(menuPaste);
        jMenu4.add(jSeparator6);

        menuNewCluster.setAction(newClusterAction);
        menuNewCluster.setText("New synonym cluster");
        menuNewCluster.setActionCommand("New synonym set");
        menuNewCluster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuNewClusterActionPerformed(evt);
            }
        });
        jMenu4.add(menuNewCluster);

        menuDeleteCluster.setAction(deleteClusterAction);
        menuDeleteCluster.setText("Delete synonym cluster...");
        jMenu4.add(menuDeleteCluster);

        jMenuBar2.add(jMenu4);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void synonymListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_synonymListMouseClicked
        updateEditorFromListModel();
    }//GEN-LAST:event_synonymListMouseClicked

    private void contentEditorPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_contentEditorPaneFocusLost
//        if ( synonymList.isSelectionEmpty() ) {
//            return;
//        }
//        updateListModelFromEditor();
        if ( !contentEditorPane.getText().equals( loadedEditorPaneText ) ) {
            editorNeedsSaving = true;
            stateMgr.markAfterUndoableStep();
        }
    }//GEN-LAST:event_contentEditorPaneFocusLost

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeOut();
    }//GEN-LAST:event_formWindowClosing

    private void menuRevertToSavedActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuRevertToSavedActionPerformed
        int result = JOptionPane.showConfirmDialog( this, "Revert to saved synonyms cannot be undone. Continue?" );
        if ( result == JOptionPane.YES_OPTION ) {
            allSynonyms.loadFromFile( allSynonyms.getSynonymFile() );
            needToSave = false;
            editorNeedsSaving = false;
            fillListFromSynonymGroup();
            stateMgr.resetAndMark();
        }
    }//GEN-LAST:event_menuRevertToSavedActionPerformed

    private void menuSaveActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        updateListModelFromEditor();
        //getSynonymsFromList();        // this may not be necessary
        allSynonyms.writeCollectionToFile();
        contentEditorPane.setText( "" );
        needToSave = false;
        editorNeedsSaving = false;
        stateMgr.resetAndMark();
    }//GEN-LAST:event_menuSaveActionPerformed

    private void menuRedoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuRedoActionPerformed
        stateMgr.doRedo();
    }//GEN-LAST:event_menuRedoActionPerformed

    private void menuNewClusterActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuNewClusterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menuNewClusterActionPerformed

    private void menuCloseActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuCloseActionPerformed
        closeOut();
    }//GEN-LAST:event_menuCloseActionPerformed

    private void menuUndoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuUndoActionPerformed
        stateMgr.doUndo();
    }//GEN-LAST:event_menuUndoActionPerformed

    private void buttonAcceptActionPerformed(ActionEvent evt) {//GEN-FIRST:event_buttonAcceptActionPerformed
//        contentEditorPane.setText( formatForEditing( contentEditorPane.getText() ) );
        updateListModelFromEditor();
    }//GEN-LAST:event_buttonAcceptActionPerformed

    private void buttonRevertActionPerformed(ActionEvent evt) {//GEN-FIRST:event_buttonRevertActionPerformed
        if ( contentEditorPane.getText().equals( loadedEditorPaneText ) ) {
            JOptionPane.showMessageDialog( this, "There are no un-accepted edits." );
        } else {
            int result = JOptionPane.showConfirmDialog( this, "Revert to original line of synonyms cannot be undone. Continue?" );
            if ( result == JOptionPane.YES_OPTION ) {
                contentEditorPane.setText( loadedEditorPaneText );
            }
        }
    }//GEN-LAST:event_buttonRevertActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton buttonAccept;
    public javax.swing.JButton buttonRevert;
    public javax.swing.JEditorPane contentEditorPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JMenuItem menuClose;
    private javax.swing.JMenuItem menuCopy;
    private javax.swing.JMenuItem menuCut;
    private javax.swing.JMenuItem menuDeleteCluster;
    private javax.swing.JMenuItem menuNewCluster;
    private javax.swing.JMenuItem menuPaste;
    public javax.swing.JMenuItem menuRedo;
    private javax.swing.JMenuItem menuRevertToSaved;
    private javax.swing.JMenuItem menuSave;
    public javax.swing.JMenuItem menuUndo;
    public javax.swing.JList synonymList;
    private javax.swing.JLabel unitNameField;
    // End of variables declaration//GEN-END:variables
}
