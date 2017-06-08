/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.util;

import charger.Global;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jdesktop.layout.GroupLayout;

/**
 *
 * @author hsd
 */
public class GenericTextFrame extends JFrame implements ClipboardOwner, ManagedWindow {

    JFrame ownerFrame = null;
    File fullPathFile = null;
    
    boolean editable = true;

    /**
     * Creates new form GenericTextFrame
     */
    public GenericTextFrame( JFrame owner ) {
        initComponents();
        getContentPane().setBackground( Global.chargerBlueColor);
        setup( owner, "Untitled", "Display Text", "", null );

    }
    
        /**
     * Creates a new TextDisplayFrame with no text in it; use setText to fill
     * it. The frame is not visible; this gives a chance to set various
     * characteristics before showing it.
     *
     * @param owner the frame which spawned this one
     * @param title the window's title
     * @param label a descriptive header label (not the window title)
     * @param text the text to be included in the text area
     * @param suggestedFile a file to save the text to (null if not used)
     */
    public GenericTextFrame( JFrame owner, String title, String label, String text, File suggestedFile ) {
        setup( owner, title, label, text, suggestedFile );
    }

    /**
     * Allows subclasses to add their own option panel to the form without inconvenience.
     * @param optionPane The new panel to be added just above the text area.
     */
    public void setOptionPanel( JPanel optionPane ) {
        ((GroupLayout)getLayout()).replace(optionPlaceholderPanel, optionPane);
    }

    

    private void setup( JFrame owner, String title, String label, String text, File suggestedFile ) {
        //Global.info( "suggested file is " + suggestedFile.toString() );
        ownerFrame = owner;
        WindowManager.manageWindow( this );

        setTheText( text );
        setLabel( label );
        setSuggestedFile( suggestedFile );
        setTitle( title );

        // set scrolling to look at the beginning of the text
        scroller.getViewport().setViewPosition( new Point( 0, 0 ) );

        if ( owner.getFont() != null ) {
            setFont( owner.getFont() );
            theText.setFont( owner.getFont() );
        }
        
        setMenuItems();

        repaint();
        //setVisible( true );
    }

    public void setTheText( String text ) {
        theText.setText( text );
        theText.setCaretPosition( 0);
        setMenuItems();
    }

    /**
     * Sets the descriptive label appearing above the text (not the title). Can
     * be changed at any time.
     *
     * @param label the label to be displayed.
     */
    public void setLabel( String label ) {
        if ( label != null ) {
            mainLabel.setText( label );
        } else {
            mainLabel.setText( "Displaying text:" );
        }
    }
    
    public void setEditable( boolean b ) {
        editable = b;
    }

    /**
     * Sets the suggested file path in case the user wants to save. Can be
     * changed at any time. Invokes a dialog where the user can change it too.
     *
     * @param suggestedFile a reasonably named file to start the user off.
     */
    public void setSuggestedFile( File suggestedFile ) {
        if ( suggestedFile != null ) {
            fullPathFile = suggestedFile.getAbsoluteFile();
        } else {
            fullPathFile = ( new File( "Untitled.txt" ) ).getAbsoluteFile();
        }
    }

    /**
     * Sets up the given font for the text display part of the frame only.
     *
     * @param f the font to be displayed.
     */
    public void setTextFont( Font f ) {
        theText.setFont( f );
        repaint();
    }

    public void setContentType( String type ) {
        theText.setContentType( type );
    }
    
        /**
     * Determines for each menu item whether to be enabled or disabled ("gray'ed
     * out" )
     */
    public void setMenuItems()  {
        // NEED TO use Toolkit.getDefaultToolkit().getSystemClipboard
        //Global.info("set menu items, somethingHasBeenSelected = " + somethingHasBeenSelected );
        for ( int num = 0; num < editMenu.getItemCount(); num++ ) {
            JMenuItem mi = editMenu.getItem( num );
            String s = mi.getText();
            if ( s.equals( "Copy" ) ) {
                mi.setEnabled( ( theText.getSelectedText() != null ) && ( !theText.getSelectedText().equals( "" ) ) );
            } else if ( s.equals( "Cut" ) ) {
                mi.setEnabled( editable && ( theText.getSelectedText() != null ) && ( !theText.getSelectedText().equals( "" ) )  );
            } else if ( s.equals( "Clear" ) ) {
                mi.setEnabled( ( editable && theText.getSelectedText() != null ) && ( !theText.getSelectedText().equals( "" ) ) );
            } else if ( s.equals( "Paste" ) ) {
                if ( !editable ) {
                    mi.setEnabled( false );
                } else if ( Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable( DataFlavor.stringFlavor ) ) {
                    String clips;
                    try {
                        clips = (String)( Toolkit.getDefaultToolkit().getSystemClipboard().getData( DataFlavor.stringFlavor ) );
                        if ( clips != null && clips.length() > 0 ) {
                            mi.setEnabled( true );
                        }
                    } catch ( UnsupportedFlavorException ex ) {
                        Global.error( "problem with system clipboard; " + ex.getMessage() );
                    } catch ( IOException ex ) {
                        Global.error( "problem with system clipboard; " + ex.getMessage() );
                    }
                }
            } else {
                mi.setEnabled( true );
            }
        }
    }

    /**
     * Tells a window manager what label to put on the menu to select this
     * window
     */
    public String getMenuItemLabel() {
        return getTitle();
    }

    /**
     * Method to be invoked when this window is chosen to be the current window.
     *
     * @see WindowManager#bringToFront
     */
    public void bringToFront() {
        WindowManager.bringToFront( this );
    }

    /**
     * If there's a file associated with the window, return its name; null
     * otherwise.
     */
    public String getFilename() {
        if ( fullPathFile != null ) {
            return fullPathFile.getAbsolutePath();
        } else {
            return getTitle();
        }
    }

    public void lostOwnership( Clipboard clipboard, Transferable contents ) {
        //Global.info( ef.graphName + " lost ownership of clipboard " + clipboard.getName() );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroller = new javax.swing.JScrollPane();
        theText = new javax.swing.JEditorPane();
        mainLabel = new javax.swing.JLabel();
        optionPlaceholderPanel = new JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuFileSave = new JMenuItem();
        menuFileClose = new JMenuItem();
        editMenu = new javax.swing.JMenu();
        menuEditCut = new JMenuItem();
        menuEditCopy = new JMenuItem();
        menuEditPaste = new JMenuItem();
        menuEditClear = new JMenuItem();
        menuEditSelectAll = new JMenuItem();
        windowMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(Global.chargerBlueColor);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        scroller.setBorder(javax.swing.BorderFactory.createBevelBorder(0));

        theText.setFont(new Font("Arial", 0, 12)); // NOI18N
        theText.setPreferredSize(new Dimension(700, 500));
        theText.setSelectionColor(new java.awt.Color(153, 153, 153));
        theText.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                theTextCaretUpdate(evt);
            }
        });
        scroller.setViewportView(theText);

        mainLabel.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        mainLabel.setForeground(new java.awt.Color(255, 255, 255));
        mainLabel.setText("jLabel1");

        optionPlaceholderPanel.setOpaque(false);

        org.jdesktop.layout.GroupLayout optionPlaceholderPanelLayout = new org.jdesktop.layout.GroupLayout(optionPlaceholderPanel);
        optionPlaceholderPanel.setLayout(optionPlaceholderPanelLayout);
        optionPlaceholderPanelLayout.setHorizontalGroup(
            optionPlaceholderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 689, Short.MAX_VALUE)
        );
        optionPlaceholderPanelLayout.setVerticalGroup(
            optionPlaceholderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        fileMenu.setText("File");

        menuFileSave.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_S, Global.AcceleratorKey ));
        menuFileSave.setText("Save Text...");
        menuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileSaveActionPerformed(evt);
            }
        });
        fileMenu.add(menuFileSave);

        menuFileClose.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_W, Global.AcceleratorKey ));
        menuFileClose.setText("Close");
        menuFileClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileCloseActionPerformed(evt);
            }
        });
        fileMenu.add(menuFileClose);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");

        menuEditCut.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_X, Global.AcceleratorKey ));
        menuEditCut.setText("Cut");
        menuEditCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCutActionPerformed(evt);
            }
        });
        editMenu.add(menuEditCut);

        menuEditCopy.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_C, Global.AcceleratorKey ));
        menuEditCopy.setText("Copy");
        menuEditCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCopyActionPerformed(evt);
            }
        });
        editMenu.add(menuEditCopy);

        menuEditPaste.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_V, Global.AcceleratorKey ));
        menuEditPaste.setText("Paste");
        editMenu.add(menuEditPaste);

        menuEditClear.setText("Clear");
        menuEditClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditClearActionPerformed(evt);
            }
        });
        editMenu.add(menuEditClear);

        menuEditSelectAll.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_A, Global.AcceleratorKey ));
        menuEditSelectAll.setText("Select All");
        menuEditSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditSelectAllActionPerformed(evt);
            }
        });
        editMenu.add(menuEditSelectAll);

        jMenuBar1.add(editMenu);

        windowMenu.setText("Window");
        jMenuBar1.add(windowMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, scroller, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, optionPlaceholderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(mainLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionPlaceholderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scroller, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                .add(10, 10, 10))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuFileCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFileCloseActionPerformed
        WindowManager.forgetWindow( this );
        ownerFrame.toFront();
        setVisible( false );
    }//GEN-LAST:event_menuFileCloseActionPerformed

    private void menuEditClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditClearActionPerformed
        theText.setText( Util.removeSubstring( theText.getText(), theText.getSelectionStart(), theText.getSelectionEnd() ) );
    }//GEN-LAST:event_menuEditClearActionPerformed

    private void menuEditCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditCopyActionPerformed
        StringSelection sel = new StringSelection( theText.getSelectedText() );
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents( sel, this );
    }//GEN-LAST:event_menuEditCopyActionPerformed

    private void menuEditSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditSelectAllActionPerformed
        theText.selectAll();
        setMenuItems();
    }//GEN-LAST:event_menuEditSelectAllActionPerformed

    private void menuFileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFileSaveActionPerformed
        String promptstring = new String( "Save " + mainLabel.getText() + " as " );
        File folder = fullPathFile.getParentFile();
        File chosenOne = Util.queryForOutputFile( promptstring, folder, fullPathFile.getName() );
        if ( chosenOne != null ) {
            try {
                FileWriter writer = new FileWriter( chosenOne );
                writer.write( theText.getText());
                writer.close();
            } catch ( IOException ex ) {
                JOptionPane.showMessageDialog( this, "File not written; " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_menuFileSaveActionPerformed

    private void menuEditCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditCutActionPerformed
        StringSelection sel = new StringSelection( theText.getSelectedText() );
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents( sel, this );
        theText.setText( Util.removeSubstring( theText.getText(), theText.getSelectionStart(), theText.getSelectionEnd()) );
    }//GEN-LAST:event_menuEditCutActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        WindowManager.forgetWindow( this );
        ownerFrame.toFront();
        setVisible( false );
    }//GEN-LAST:event_formWindowClosing

    private void theTextCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_theTextCaretUpdate
        setMenuItems();
    }//GEN-LAST:event_theTextCaretUpdate

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        int margin = 10;
//        theText.s( new Dimension(getContentPane().getWidth() - 2*margin, getContentPane().getHeight() - 10*margin));
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar jMenuBar1;
    public javax.swing.JLabel mainLabel;
    private JMenuItem menuEditClear;
    private JMenuItem menuEditCopy;
    private JMenuItem menuEditCut;
    private JMenuItem menuEditPaste;
    private JMenuItem menuEditSelectAll;
    private JMenuItem menuFileClose;
    private JMenuItem menuFileSave;
    public JPanel optionPlaceholderPanel;
    protected javax.swing.JScrollPane scroller;
    public javax.swing.JEditorPane theText;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables
}
