/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.prefs;

import charger.Global;
import charger.obj.Arrow;
import charger.obj.Concept;
import charger.obj.Coref;
import charger.obj.GenSpecLink;
import charger.obj.GraphObject;
import charger.obj.Relation;
import charger.obj.TypeLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Container that holds the controls needed to change node colors. Intended to
 * be callable from more than just the preferences frame, but for now will
 * probably set globals inadvertently.
 *
 * @author hsd
 */
public class ColorPreviewPanel extends JPanel {

    public enum ColorAspect {
        Fill, Text, Line
    };

    /**
     * An encapsulation of both the color and whether it is fill, text or a line color.
     * Not currently used, but will think about it in the future.
     */
    public class ColorResult {

        ColorAspect aspect;
        Color color;

        public ColorResult( ColorAspect aspect, Color color ) {
            this.aspect = aspect;
            this.color = color;
        }
    }
    
//    boolean changed = false;
    String title = "";
    
    EnumMap<ColorAspect, Color> currentColor = new EnumMap<>(ColorAspect.class );
    
    GraphObject samplego = null;
    GraphObject dummy1 = null;  // used for drawing really small nodes so that edges can be shown
    GraphObject dummy2 = null;  // used for drawing really small nodes so that edges can be shown
    
    PreferencesFrame frame = null;

    /**
     * Creates new form ColorPreviewPanel.
     * @param frame 
     */
    public ColorPreviewPanel( PreferencesFrame frame  ) {
        this.frame = frame;
        initComponents();
        graphObjectList.setMaximumRowCount( graphObjectList.getModel().getSize());
//        Global.info( "parent of color panel is " + getParent() );
    }
    
//    public void setColor( ColorAspect aspect, Color color ) {
//        currentColor.put( aspect, color );
//    }
//    
//    public ColorResult getColor( String kind ) {
//        switch ( kind.toLowerCase() ) {
//            case "text":
//            case "foreground":
//                return new ColorResult(ColorAspect.Text, currentColor.get(  ColorAspect.Text));
//            case "fill":
//            case "background":
//                return new ColorResult(ColorAspect.Fill, currentColor.get(  ColorAspect.Fill));
//            case "line":
//                return new ColorResult(ColorAspect.Line, currentColor.get(  ColorAspect.Line));
//        }
//        return null;
//    }
    
    public void setTitle( String title ) {
        this.title = title;
        this.repaint();
    }

    /**
     * Gets the enclosing appearance panel. This even further ties the color panel to appearing only in the preferences panel.
     * @return the cnlosing appearance panel.
     */
    public AppearancePrefPanel getAppearancePanel() {
        if ( frame != null ) {
            return frame.appearance;
        } else {
            return null;
        }
    }

    /**
     * Gets the controlling preferences frame. This method ties the color panel to appearing only in the preferences panel.
     * @return the enclosing preferences frame.
     */
    public PreferencesFrame getParentFrame() {
        return frame;
    }

    
    /**
     * Convenience method to let the color panel know when it needs to redraw the sample object.
     * This usually happens when some other changed preference affects the sample object's appearance.
     */
    public void refreshSampleObject() {
        initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
    }

    /**
     * Set up the object display for the panel, using the current default
     * colors.
     *
     *
     * @param classname
     */
    private void initSampleGraphObject( String classname ) {
        float edgeEndSpillover = 10;     // place the center of the edge's two nodes this far in from the sides of the panel
        //	Global.info( "initSampleGraphObject" );
        // NOTE: this method depends on the graph objects being in the charger.obj package
        try {
            dummy1 = null;
            dummy2 = null;
            Class c = null;
            if ( classname.equals( "Cut")) 
                c = Class.forName( "charger.obj." + "Graph");
            else
            c = Class.forName( "charger.obj." + classname );
//                     this.getGraphics().setFont( Global.defaultFont );
            Dimension d = sampleObjectPanel.getSize();
            if ( classname.equals( "Graph" ) || classname.equals( "Cut")) {
                samplego = new charger.obj.Graph( new charger.obj.Graph( null ) );
                samplego.displayRect = new Rectangle2D.Double( 15, 15, d.width - 30, d.height - 30 );
                samplego.setTextLabel( Global.defaultContextLabel );
                if ( classname.equals(  "Cut") ) {
//                    samplego.setTextLabel( "Cut");
                    samplego.setNegated( true);
                }
            } else {
                foreColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "text" ) );
                backColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "fill" ) );

                if ( classname.equals( "Concept" ) || classname.equals( "Relation" ) || classname.equals( "Actor" )
                        || classname.equals( "TypeLabel" ) || classname.equals( "RelationLabel" )
                        || classname.equals( "Note" ) ) {
                    samplego = (GraphObject)c.newInstance();        // should this pick up the default font and color?
                    samplego.setCenter( new Point2D.Double( d.width / 2, d.height / 2 ) );
                    samplego.setTextLabel( (String)( graphObjectList.getSelectedItem() ) );
                    samplego.setLabelFont( Global.defaultFont );
                    samplego.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), samplego.getCenter() );
                } else if ( classname.equals( "Arrow" ) ) {

                    dummy1 = new Concept();
                    dummy1.setCenter( new Point2D.Double( 0f + edgeEndSpillover, d.height / 2 ) );
                    dummy1.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy1.getCenter() );

                    dummy2 = new Relation();
                    dummy2.setCenter( new Point2D.Double( d.width - edgeEndSpillover, d.height / 2 ) );
                    dummy2.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy2.getCenter() );

                    samplego = new Arrow( dummy1, dummy2 );
                } else if ( classname.equals( "Coref" ) ) {
                    dummy1 = new Concept();
                    dummy1.setCenter( new Point2D.Double( 0f + edgeEndSpillover, d.height / 2 ) );
                    dummy1.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy1.getCenter() );

                    dummy2 = new Concept();
                    dummy2.setCenter( new Point2D.Double( d.width - edgeEndSpillover, d.height / 2 ) );
                    dummy2.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy2.getCenter() );

                    samplego = new Coref( dummy1, dummy2 );
                } else if ( classname.equals( "GenSpecLink" ) ) {
                    dummy1 = new TypeLabel();
                    dummy1.setCenter( new Point2D.Double( 0f + edgeEndSpillover, d.height / 2 ) );
                    dummy1.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy1.getCenter() );

                    dummy2 = new TypeLabel();
                    dummy2.setCenter( new Point2D.Double( d.width - edgeEndSpillover, d.height / 2 ) );
                    dummy2.resizeIfNecessary( this.getGraphics().getFontMetrics( Global.defaultFont ), dummy2.getCenter() );

                    samplego = new GenSpecLink( dummy1, dummy2 );
                }
            }
        } catch ( ClassNotFoundException cnfe ) {
            Global.error( "Class " + classname + " not found." );
        } catch ( InstantiationException ie ) {
            Global.error( "Class " + classname + " couldn't be instantiated." );
        } catch ( IllegalAccessException iae ) {
            Global.error( "Class " + classname + " couldn't be accessed." );
        }
        sampleObjectPanel.repaint();
    }

    /**
     * Used when clicking on one of the color tiles. Invokes the JColorChooser
     * so the user can select a color
     * @param classname one of the graph object classes in Charger that can have a color. This
     * is the simple class name, not a fully qualified one.
     * @param foreback One of "fill" or "text"
     * @param initial what initial setting to give the color chooser
     * @return the color the user selected; null if canceled. 
     * @see JColorChooser
     */
    private Color performColorSelect( String classname, String foreback, Color initial ) {
        Color c = JColorChooser.showDialog( getParentFrame(),
                "Choose " + foreback + " color for " + classname,
                initial );
        if ( c != null ) {
            Global.setDefaultColor( classname, foreback, c );
            sampleObjectPanel.repaint();
        }
        return c;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        restoreFactoryColors = new javax.swing.JButton();
        chooseBlackAndWhite = new javax.swing.JButton();
        chooseGrayscale = new javax.swing.JButton();
        sampleObjectPanel = new JPanel() {
            public void paintComponent( Graphics g ) {
                super.paintComponent( g );
                if ( g != null ) {
                    g.setFont( Global.defaultFont );
                    if ( samplego != null ) {
                        samplego.draw( (Graphics2D)g, false );
                    }
                    if ( dummy1 != null ) {
                        dummy1.draw( (Graphics2D)g, false );
                    }
                    if ( dummy2 != null ) {
                        dummy2.draw( (Graphics2D)g, false );
                    }
                }
            }
        };
        graphObjectList = new javax.swing.JComboBox();
        foreColorPanel = new JPanel();
        backColorPanel = new JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
            "Default Appearance", TitledBorder.LEFT,
            TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 11 ), Color.black ));

    restoreFactoryColors.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
    restoreFactoryColors.setText("Factory defaults");
    restoreFactoryColors.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            restoreFactoryColorsActionPerformed(evt);
        }
    });

    chooseBlackAndWhite.setBackground(new Color(255, 255, 255));
    chooseBlackAndWhite.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
    chooseBlackAndWhite.setText("Black & White");
    chooseBlackAndWhite.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            chooseBlackAndWhiteActionPerformed(evt);
        }
    });

    chooseGrayscale.setBackground(new Color(153, 153, 153));
    chooseGrayscale.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
    chooseGrayscale.setText("Grayscale");
    chooseGrayscale.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            chooseGrayscaleActionPerformed(evt);
        }
    });

    sampleObjectPanel.setBackground(new Color(204, 204, 204));
    sampleObjectPanel.setBorder(BorderFactory.createTitledBorder( Global.BeveledBorder,
        title, TitledBorder.LEFT,
        TitledBorder.TOP, new Font( "SansSerif", Font.BOLD + Font.ITALIC, 10 ), Color.black ));

org.jdesktop.layout.GroupLayout sampleObjectPanelLayout = new org.jdesktop.layout.GroupLayout(sampleObjectPanel);
sampleObjectPanel.setLayout(sampleObjectPanelLayout);
sampleObjectPanelLayout.setHorizontalGroup(
    sampleObjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
    .add(0, 160, Short.MAX_VALUE)
    );
    sampleObjectPanelLayout.setVerticalGroup(
        sampleObjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(0, 0, Short.MAX_VALUE)
    );

    graphObjectList.setBackground(new Color(255, 255, 255));
    graphObjectList.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
    graphObjectList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Concept", "Relation", "Actor", "Graph", "Cut", "TypeLabel", "RelationLabel", "Note", "Arrow", "Coref", "GenSpecLink" }));
    graphObjectList.setOpaque(true);
    graphObjectList.setSelectedItem( "Concept ");
    graphObjectList.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            graphObjectListItemStateChanged(evt);
        }
    });

    foreColorPanel.setBorder(Global.BeveledBorder);
    foreColorPanel.setForeground(Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "text" ));
    foreColorPanel.setPreferredSize(new Dimension(35, 35));
    foreColorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            foreColorPanelMouseClicked(evt);
        }
    });

    org.jdesktop.layout.GroupLayout foreColorPanelLayout = new org.jdesktop.layout.GroupLayout(foreColorPanel);
    foreColorPanel.setLayout(foreColorPanelLayout);
    foreColorPanelLayout.setHorizontalGroup(
        foreColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(0, 35, Short.MAX_VALUE)
    );
    foreColorPanelLayout.setVerticalGroup(
        foreColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(0, 35, Short.MAX_VALUE)
    );

    backColorPanel.setBorder(Global.BeveledBorder);
    backColorPanel.setForeground(Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "fill" ));
    backColorPanel.setPreferredSize(new Dimension(35, 35));
    backColorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            backColorPanelMouseClicked(evt);
        }
    });

    org.jdesktop.layout.GroupLayout backColorPanelLayout = new org.jdesktop.layout.GroupLayout(backColorPanel);
    backColorPanel.setLayout(backColorPanelLayout);
    backColorPanelLayout.setHorizontalGroup(
        backColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(0, 35, Short.MAX_VALUE)
    );
    backColorPanelLayout.setVerticalGroup(
        backColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(0, 35, Short.MAX_VALUE)
    );

    jLabel3.setText("Fill");

    jLabel4.setText("Text");

    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(16, 16, 16)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(graphObjectList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(sampleObjectPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(25, 25, 25)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(foreColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel3)
                        .add(backColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(15, 15, 15))
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(restoreFactoryColors, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .add(chooseGrayscale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                        .add(chooseBlackAndWhite, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(22, 22, 22))))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                .add(layout.createSequentialGroup()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                        .add(jLabel4))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, foreColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, backColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(5, 5, 5)
                    .add(chooseBlackAndWhite)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(chooseGrayscale)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(restoreFactoryColors))
                .add(layout.createSequentialGroup()
                    .add(14, 14, 14)
                    .add(graphObjectList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(sampleObjectPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addContainerGap(18, Short.MAX_VALUE))
    );
    }// </editor-fold>//GEN-END:initComponents

    private void restoreFactoryColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreFactoryColorsActionPerformed
        Global.showCutOrnamented = Global.Prefs.getProperty( "showCutOrnamented", "true" ).equals( "true" );
        Global.userForeground = (Hashtable)Global.factoryForeground.clone();
        Global.userBackground = (Hashtable)Global.factoryBackground.clone();
        foreColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "text" ) );
        backColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "fill" ) );
        if ( getParentFrame() != null && getParentFrame() instanceof PreferencesFrame ) {
            getParentFrame().appearance.showBorders.setSelected( false );
            getParentFrame().appearance.showShadows.setSelected( true );
            getParentFrame().appearance.showCutOrnamented.setSelected( false );
            initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
        }
        initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
    }//GEN-LAST:event_restoreFactoryColorsActionPerformed

    private void chooseBlackAndWhiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseBlackAndWhiteActionPerformed
        //        Global.info( "at choose black and white" );
        Global.userForeground = (Hashtable)Global.bwForeground.clone();
        Global.userBackground = (Hashtable)Global.bwBackground.clone();
        foreColorPanel.setBackground( Color.black );
        backColorPanel.setBackground( Color.white );
        if ( getParentFrame() != null && getParentFrame() instanceof PreferencesFrame ) {
            getParentFrame().appearance.showBorders.setSelected( true );
            getParentFrame().appearance.showShadows.setSelected( false );
            getParentFrame().appearance.showCutOrnamented.setSelected( false );
            initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
        }
    }//GEN-LAST:event_chooseBlackAndWhiteActionPerformed

    private void chooseGrayscaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseGrayscaleActionPerformed
        //        Global.info( "at choose grayscale" );
        Global.userForeground = (Hashtable)Global.grayForeground.clone();
        Global.userBackground = (Hashtable)Global.grayBackground.clone();
        foreColorPanel.setBackground( Color.black );
        backColorPanel.setBackground( Color.white );
        if ( getParentFrame() != null && getParentFrame() instanceof PreferencesFrame ) {
            getParentFrame().appearance.showBorders.setSelected( true );
            getParentFrame().appearance.showShadows.setSelected( false );
            getParentFrame().appearance.showCutOrnamented.setSelected( false );
            initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
        }
        initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
    }//GEN-LAST:event_chooseGrayscaleActionPerformed

    private void graphObjectListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_graphObjectListItemStateChanged
        foreColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "text" ) );
        backColorPanel.setBackground( Global.getDefaultColor( (String)( graphObjectList.getSelectedItem() ), "fill" ) );
        initSampleGraphObject( (String)( graphObjectList.getSelectedItem() ) );
    }//GEN-LAST:event_graphObjectListItemStateChanged

    private void foreColorPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foreColorPanelMouseClicked
        Color c =
                performColorSelect( (String)( graphObjectList.getSelectedItem() ),
                "text",
                foreColorPanel.getBackground() );
        if ( c != null ) {
            foreColorPanel.setBackground( c );	// yes, background (it's the selector swatch)
            samplego.setColor( "text", c );
        }
        sampleObjectPanel.repaint();
    }//GEN-LAST:event_foreColorPanelMouseClicked

    private void backColorPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backColorPanelMouseClicked
        Color c =
                performColorSelect( (String)( graphObjectList.getSelectedItem() ),
                "fill",
                foreColorPanel.getBackground() );
        if ( c != null ) {
            backColorPanel.setBackground( c );	
            samplego.setColor( "fill", c );
        }
        sampleObjectPanel.repaint();
    }//GEN-LAST:event_backColorPanelMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel backColorPanel;
    private javax.swing.JButton chooseBlackAndWhite;
    private javax.swing.JButton chooseGrayscale;
    private JPanel foreColorPanel;
    public javax.swing.JComboBox graphObjectList;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton restoreFactoryColors;
    public JPanel sampleObjectPanel;
    // End of variables declaration//GEN-END:variables
}
