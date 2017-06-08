/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger;

import charger.EditingChangeState.EditChange;
import charger.obj.GraphObject;
import charger.util.FontChooser;
import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import javax.swing.JColorChooser;
import javax.swing.JLabel;

/**
 * Container for the controls that affect appearance, but not semantics.
 *
 * @author hsd
 */
public class FormatToolbar extends javax.swing.JPanel {

    EditManager emgr = null;
    Font font = null;
    boolean bold = false;
    boolean italic = false;

    /**
     * Creates new form FormatToolbar
     */
    public FormatToolbar( EditManager emgr ) {
        this.emgr = emgr;
        initComponents();
        FontChooser.initFontNameMenu( fontNameMenu, Global.showAllFonts );
        FontChooser.initFontSizeMenu( fontSizeMenu, 8, 9, 10, 11, 12, 14, 18, 24, 36, 48, 72 );

    }

    public void setDisplayForSelection( Color textColor, Color fillColor, Font font ) {
        alignHorizontallyButton.setEnabled( true );
        alignVerticallyButton.setEnabled( true );
        setDisplayFontForSelection( font );
        setDisplayColorsForSelection( textColor, fillColor );
    }

    public void setStyleButton( JLabel stylebutton, boolean set ) {
        if ( set ) {
            stylebutton.setForeground( Color.white );
            stylebutton.setBackground(Color.black);
        } else {
            stylebutton.setForeground( Color.black );
            stylebutton.setBackground(Color.white);
        }
        stylebutton.setEnabled( true );
    }

    public void setDisplayFontForSelection( Font font ) {
       this.font = font;
             fontNameMenu.setEnabled( false );
            fontSizeMenu.setEnabled( false );
            boldButton.setEnabled( false );
            italicButton.setEnabled( false );
        if ( font == null ) {
            return;
        } else {

            bold = font.isBold();
            setStyleButton( boldButton, bold );
            
            italic = font.isItalic();
            setStyleButton( italicButton, italic );

            fontNameMenu.setSelectedItem( font.getName() );
            fontSizeMenu.setSelectedItem( "" + font.getSize() );
            
             fontNameMenu.setEnabled( true );
            fontSizeMenu.setEnabled( true );
            boldButton.setEnabled( true );
            italicButton.setEnabled( true );
            
        }

    }

    public void setDisplayColorsForSelection( Color textColor, Color fillColor ) {

//        Global.info( "at setDisplayForSelection: textColor, fillColor " + textColor + fillColor );
        foregroundTile.setEnabled( true );
        backgroundTile.setEnabled( true );
        if ( textColor != null ) {
            foregroundTile.setBackground( textColor );
            
        } else {
            foregroundTile.setBackground( null );

//            Graphics g = foregroundTile.getGraphics();
//            g.clearRect( 0, 0, foregroundTile.getWidth(), foregroundTile.getHeight() );
//            g.setColor( Color.red );
//            g.drawLine( 0, 0, foregroundTile.getWidth(), foregroundTile.getHeight() );
//            g.drawLine( 0, foregroundTile.getHeight(), foregroundTile.getWidth(), 0 );
//            foregroundTile.repaint();
        }
        if ( fillColor != null ) {
            backgroundTile.setBackground( fillColor );
        } else {

            backgroundTile.setBackground( null );
//            Graphics g = backgroundTile.getGraphics();
//            g.setColor( Color.red );
//            g.drawLine( 0, 0, backgroundTile.getWidth(), backgroundTile.getHeight() );
//            g.drawLine( 0, backgroundTile.getHeight(), backgroundTile.getWidth(), 0 );
//            backgroundTile.repaint();
        }
    }

    public void setForNoSelection() {
        setDisplayForSelection( null, null, null );
        foregroundTile.setBackground(  Color.gray );
        backgroundTile.setBackground(  Color.gray );
        foregroundTile.setEnabled( false );
        backgroundTile.setEnabled( false );
        alignHorizontallyButton.setEnabled( false );
        alignVerticallyButton.setEnabled( false );
    }

    /**
     * Change the selected objects' font based on the current value of this.font. 
     * Also tell the edit manager that something's changed in the appearance that's undoable.
     */
    public void changeFont( ) {
        this.font = font;
        Iterator iter = emgr.ef.EFSelectedObjects.iterator();
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
            if ( go.isSelected ) {
                go.setLabelFont( font );
                go.resizeIfNecessary();
            } else {
                Global.warning( "There's an un-selected object on selection list: " + go );
            }
        }
        emgr.setChangedContent( EditChange.APPEARANCE, EditChange.UNDOABLE  );
    }

    private int getStyleOf( boolean bold, boolean italic ) {
        int myFontStyle = Font.PLAIN;
        if ( bold && !italic ) {
            myFontStyle = Font.BOLD;
        }
        if ( !bold && italic ) {
            myFontStyle = Font.ITALIC;
        }
        if ( bold && italic ) {
            myFontStyle = Font.BOLD + Font.ITALIC;
        }
        return myFontStyle;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        alignVerticallyButton = new javax.swing.JButton();
        alignHorizontallyButton = new javax.swing.JButton();
        fontNameMenu = new javax.swing.JComboBox();
        fontSizeMenu = new javax.swing.JComboBox();
        jLabel3 = new JLabel();
        backgroundTile = new javax.swing.JPanel();
        jLabel2 = new JLabel();
        foregroundTile = new javax.swing.JPanel();
        jLabel1 = new JLabel();
        jLabel4 = new JLabel();
        boldButton = new JLabel();
        italicButton = new JLabel();

        setBackground(Global.chargerBlueColor);
        setMaximumSize(new java.awt.Dimension(837, 31));
        setMinimumSize(new java.awt.Dimension(837, 31));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
        });

        alignVerticallyButton.setBackground(new Color(255, 255, 255));
        alignVerticallyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/AlignVIcon.gif"))); // NOI18N
        alignVerticallyButton.setToolTipText("Align selected items vertically");
        alignVerticallyButton.setAlignmentX(0.5F);
        alignVerticallyButton.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));
        alignVerticallyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        alignVerticallyButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/AlignVIconInverted.gif"))); // NOI18N
        alignVerticallyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignVerticallyButtonActionPerformed(evt);
            }
        });

        alignHorizontallyButton.setBackground(new Color(255, 255, 255));
        alignHorizontallyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/AlignHIcon.gif"))); // NOI18N
        alignHorizontallyButton.setToolTipText("Align selected items horizontally");
        alignHorizontallyButton.setAlignmentX(0.5F);
        alignHorizontallyButton.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));
        alignHorizontallyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        alignHorizontallyButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/AlignHIconInverted.gif"))); // NOI18N
        alignHorizontallyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignHorizontallyButtonActionPerformed(evt);
            }
        });

        fontNameMenu.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        fontNameMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        fontNameMenu.setToolTipText("Choose font for selected items");
        fontNameMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontNameMenuActionPerformed(evt);
            }
        });

        fontSizeMenu.setFont(new Font("Lucida Grande", 0, 14)); // NOI18N
        fontSizeMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        fontSizeMenu.setToolTipText("Choose font size for selected items");
        fontSizeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeMenuActionPerformed(evt);
            }
        });

        backgroundTile.setBackground(new Color(255, 255, 255));
        backgroundTile.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255, 255, 255), 2));
        backgroundTile.setToolTipText("Choose fill color for selected items");
        backgroundTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backgroundTileMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout backgroundTileLayout = new org.jdesktop.layout.GroupLayout(backgroundTile);
        backgroundTile.setLayout(backgroundTileLayout);
        backgroundTileLayout.setHorizontalGroup(
            backgroundTileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 32, Short.MAX_VALUE)
        );
        backgroundTileLayout.setVerticalGroup(
            backgroundTileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );

        jLabel2.setForeground(new Color(255, 255, 255));
        jLabel2.setText("Fill");
        jLabel2.setToolTipText("Choose fill color for selected items");

        foregroundTile.setBackground(new Color(255, 255, 255));
        foregroundTile.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255, 255, 255), 2));
        foregroundTile.setToolTipText("Choose text color for selected items");
        foregroundTile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                foregroundTileMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout foregroundTileLayout = new org.jdesktop.layout.GroupLayout(foregroundTile);
        foregroundTile.setLayout(foregroundTileLayout);
        foregroundTileLayout.setHorizontalGroup(
            foregroundTileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 32, Short.MAX_VALUE)
        );
        foregroundTileLayout.setVerticalGroup(
            foregroundTileLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 17, Short.MAX_VALUE)
        );

        jLabel1.setForeground(new Color(255, 255, 255));
        jLabel1.setText("Text");
        jLabel1.setToolTipText("Choose text color for selected items");

        boldButton.setFont(new Font("Times New Roman", 1, 24)); // NOI18N
        boldButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        boldButton.setText("B");
        boldButton.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));
        boldButton.setMaximumSize(new java.awt.Dimension(28, 28));
        boldButton.setMinimumSize(new java.awt.Dimension(28, 28));
        boldButton.setOpaque(true);
        boldButton.setPreferredSize(new java.awt.Dimension(28, 28));
        boldButton.setSize(new java.awt.Dimension(28, 28));
        boldButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boldButtonMouseClicked(evt);
            }
        });

        italicButton.setFont(new Font("Times New Roman", 3, 24)); // NOI18N
        italicButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        italicButton.setText("I");
        italicButton.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));
        italicButton.setMaximumSize(new java.awt.Dimension(28, 28));
        italicButton.setMinimumSize(new java.awt.Dimension(28, 28));
        italicButton.setOpaque(true);
        italicButton.setPreferredSize(new java.awt.Dimension(28, 28));
        italicButton.setSize(new java.awt.Dimension(28, 28));
        italicButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                italicButtonMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(18, 18, 18)
                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(alignVerticallyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(9, 9, 9)
                .add(alignHorizontallyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(fontNameMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(6, 6, 6)
                .add(fontSizeMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(boldButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(italicButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(44, 44, 44)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(foregroundTile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backgroundTile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 146, Short.MAX_VALUE)
                .add(jLabel4)
                .add(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(3, 3, 3)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(foregroundTile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(backgroundTile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(alignHorizontallyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(alignVerticallyButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(fontSizeMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fontNameMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(boldButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(italicButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void alignVerticallyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignVerticallyButtonActionPerformed
        emgr.performActionAlign( "vertical" );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_alignVerticallyButtonActionPerformed

    private void alignHorizontallyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignHorizontallyButtonActionPerformed
        emgr.performActionAlign( "horizontal" );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_alignHorizontallyButtonActionPerformed

    private void fontNameMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontNameMenuActionPerformed
       if ( ! fontNameMenu.isEnabled() ) return;
        if ( this.font == null ) {
            return;
        }
        font = new Font( (String)fontNameMenu.getSelectedItem(), this.font.getStyle(), this.font.getSize() );
        changeFont(  );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_fontNameMenuActionPerformed

    private void fontSizeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeMenuActionPerformed
       if ( ! fontSizeMenu.isEnabled() ) return;
        if ( this.font == null ) {
            return;
        }
        int fontSize = Integer.parseInt( (String)fontSizeMenu.getSelectedItem() );
        font = new Font( this.font.getName(), this.font.getStyle(), fontSize );
        changeFont(  );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_fontSizeMenuActionPerformed

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_formMouseExited

    private void backgroundTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backgroundTileMouseClicked
        if ( !backgroundTile.isEnabled() ) {
            return;
        }
        Color c = null;
        c = JColorChooser.showDialog( emgr.ef,
                "Choose fill color for selected objects", c );
        if ( c != null ) {
            backgroundTile.setBackground( c );
            emgr.performActionColorSelection( "fill", c );
        }
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_backgroundTileMouseClicked

    private void foregroundTileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foregroundTileMouseClicked
        if ( !foregroundTile.isEnabled() ) {
            return;
        }
        Color c = null;
        c = JColorChooser.showDialog( emgr.ef,
                "Choose text color for selected objects", c );
        if ( c != null ) {
            foregroundTile.setBackground( c );
            emgr.performActionColorSelection( "text", c );
        }
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_foregroundTileMouseClicked

    private void boldButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boldButtonMouseClicked
        if ( this.font == null ) return;
        bold = !bold;
        setStyleButton( boldButton, bold );
        font = new Font( this.font.getName(), getStyleOf( bold, italic ), this.font.getSize() );
        changeFont(  );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_boldButtonMouseClicked

    private void italicButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_italicButtonMouseClicked
        if ( this.font == null ) return;
        italic = !italic;
        setStyleButton( italicButton, italic );
        font = new Font( this.font.getName(), getStyleOf( bold, italic ), this.font.getSize() );
        changeFont(  );
        emgr.ef.cp.requestFocus();
    }//GEN-LAST:event_italicButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton alignHorizontallyButton;
    private javax.swing.JButton alignVerticallyButton;
    private javax.swing.JPanel backgroundTile;
    private JLabel boldButton;
    private javax.swing.JComboBox fontNameMenu;
    private javax.swing.JComboBox fontSizeMenu;
    private javax.swing.JPanel foregroundTile;
    private JLabel italicButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
