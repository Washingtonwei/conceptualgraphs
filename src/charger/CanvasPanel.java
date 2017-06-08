package charger;

import charger.EditingChangeState.EditChange;
import charger.act.GraphUpdater;
import charger.obj.Actor;
import charger.obj.GNode;
import charger.obj.GraphObject;
import charger.obj.Relation;
import charger.obj.RelationLabel;
import charger.obj.TypeLabel;
import charger.util.CGUtil;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;

import kb.KBException;
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
 * The drawing area for the edit frame. communicates with its enclosing
 * EditFrame by the ef variable.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class CanvasPanel extends JPanel implements Scrollable, ActionListener {

    /**
     * The enclosing editing frame.
     */
    public EditFrame ef = null;
    private int scalingAlgorithm = Image.SCALE_FAST;
    
    private boolean nodeEditingDialogUndecorated = false;
    /**
     * The "popup" text field for editing, because its setup and processing are
     * spread around between mouseUp, userStartedEditingText, action, and
     * userFinishedEditingText
     */
//    public JTextField textLabelEditField = null;
    /**
     * indicates which GNode whose text the user is currently editing; null if
     * no editing.
     */
    private GraphObject userEditedGraphObject = null;
    /**
     * Popup menu used when a text label is constrained to a list of known
     * labels for that kind
     */
    public JComboBox labelChooser = new JComboBox();
    public JDialog nodeEditingDialog = null;
    private Dimension preferredSize = new Dimension( 3000, 3000 );
    private BufferedImage image = null;
    // Save for teardown
    private FocusAdapter focusAdapter;
    private WindowAdapter nedWindowAdapter;
    
        /** Implements copy/cut/paste for text */
    KeyAdapter nodeEditingKeyAdapter = new KeyAdapter() {
        public void keyPressed( KeyEvent evt ) {
//            Global.info( "key event " + evt );
            if ( !evt.isMetaDown() ) {
                return;
            }
            if ( evt.getSource() instanceof JTextField ) {
                JTextField field = (JTextField)evt.getSource();
                switch ( evt.getKeyCode() ) {
                    case KeyEvent.VK_C:
                        field.copy();
                        break;
                    case KeyEvent.VK_X:
                        field.cut();
                        break;
                    case KeyEvent.VK_V:
                        field.paste();
                        break;
                }
            } 
        }
    };

    CanvasPanel( EditFrame outerFrame ) {
        if ( Global.useBufferedImage ) {
            image = new BufferedImage( 1500, 1500, BufferedImage.TYPE_INT_ARGB );
        }
        ef = outerFrame;
        setLayout( null );
        setup();
    }

    /**
     * Sets up the textLabelEditField and labelChooser components.
     */
    public void setup() {
        setSize( new Dimension( 1500, 1500 ) );
        setPreferredSize( new Dimension( 1500, 1500 ) );
        setOpaque( true );
        setBackground( new Color( 228, 228, 228 ) );

        createNodeEditingDialog();
//        createTextEditField( ef.currentFont );
//        createLabelChooserOLD();

        userEditedGraphObject = null;

        setFont( ef.currentFont );
        setAutoscrolls( true );

        addKeyListener( ef );

        focusAdapter = new FocusAdapter() {
            public void focusGained( FocusEvent e ) {
                // Global.info( "canvas panel focus gained" );
            }

            public void focusLost( FocusEvent e ) {
                // Global.info( "canvas panel focus lost" );
            }
        };

        addFocusListener( focusAdapter );

        //requestFocus();
    }

    public void teardown() {
        removeAll();
        this. // Remove nodeEditingDialog references
                nodeEditingDialog.removeWindowListener( nedWindowAdapter );
        nodeEditingDialog.removeAll();
        nedWindowAdapter = null;

        nodeEditingDialog.removeAll();
        nodeEditingDialog = null;

        // Remove Focus Listener
        removeFocusListener( focusAdapter );
        focusAdapter = null;

        // Remove EditForm reference
        removeKeyListener( ef );
        removeMouseListener( ef );
        removeMouseMotionListener( ef );
        ef = null;

        // Remove Label Field Listenters
//        textLabelEditField.removeActionListener( this );
//        labelChooser.removeActionListener( this );

        // Remove all window components
        setAutoscrolls( false );

    }

    private void createNodeEditingDialog() {
        nodeEditingDialog = new JDialog( ef, true );
        nodeEditingDialog.getContentPane().setLayout( new FlowLayout( FlowLayout.LEFT) );
        nodeEditingDialog.setVisible( false );
        nodeEditingDialog.setFont( ef.currentFont );
//        nodeEditingDialog.getContentPane().setBackground( getBackground() );
        nodeEditingDialog.getContentPane().setBackground( Global.chargerBlueColor );
        nodeEditingDialog.setOpacity( 1.0f );
        nodeEditingDialog.setUndecorated( nodeEditingDialogUndecorated );
        //nodeEditingDialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

        nedWindowAdapter = new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                nodeEditingDialog.dispose();
            }
        };
        

        nodeEditingDialog.addWindowListener( nedWindowAdapter );
    }

    /**
     * set up the text labeling field .
     */
    private JTextField createTextEditField( FontMetrics fontMetrics, String label, Color fore, Color back ) {
        JTextField textLabelEditField = new JTextField( "" );
        textLabelEditField.setMargin( new Insets( 4, 4, 4, 4 ) );
        textLabelEditField.setBorder( BorderFactory.createLoweredBevelBorder() );
        textLabelEditField.setVisible( true );
        textLabelEditField.setFont( fontMetrics.getFont() );
        textLabelEditField.setToolTipText( "Editing a label (Use \"Enter\" to apply changes)" );
        textLabelEditField.setSelectionColor( Color.lightGray );
        textLabelEditField.setEnabled( true );
        textLabelEditField.setEditable( true );
        textLabelEditField.addActionListener( this );
//        textLabelEditField.setForeground( fore );
//        textLabelEditField.setBackground( back );
//        textLabelEditField.setCaretColor( fore );
//        if ( textLabelEditField.getForeground().equals( textLabelEditField.getBackground() ) ) {
//            textLabelEditField.setBackground( getBackground() );
//        }

        Dimension size = getEditingTextSize( fontMetrics, label );
        if ( size.width < 100 ) size.setSize( 100, size.height );

        textLabelEditField.setSize( size.width, size.height );
        textLabelEditField.setPreferredSize( textLabelEditField.getSize() );
        textLabelEditField.setMinimumSize( textLabelEditField.getSize() );
                Global.info( "size of text edit field " + textLabelEditField.getSize());
        textLabelEditField.setText( label );
        textLabelEditField.selectAll();
        
        textLabelEditField.addKeyListener( nodeEditingKeyAdapter );

        return textLabelEditField;
    }
    
    /**
     * Factory method to create a label chooser that will set up appearance, based on the parameters and userEditedGraphObject
     * @param fontMetrics
     * @param label
     * @param fore
     * @param back
     * @return the created label chooser combo box
     * @see #userEditedGraphObject
     */
    public JComboBox createLabelChooser( FontMetrics fontMetrics, String label, Color fore, Color back ) {
        int minimumWidth = 100;
        JComboBox textLabelEditComboBox = new JComboBox();
        textLabelEditComboBox.setBorder( BorderFactory.createLoweredBevelBorder() );

        textLabelEditComboBox.setVisible( false );
        textLabelEditComboBox.addActionListener( this );
        textLabelEditComboBox.setBorder( BorderFactory.createLoweredBevelBorder() );
        textLabelEditComboBox.setFont( fontMetrics.getFont() );
        textLabelEditComboBox.getEditor().getEditorComponent().setFont( fontMetrics.getFont() );

        textLabelEditComboBox.setToolTipText( "Editing a label (Use \"Enter\" to apply changes)" );
        textLabelEditComboBox.setEditable( true );
        textLabelEditComboBox.addActionListener( this );
        textLabelEditComboBox.setForeground( Color.black );
        textLabelEditComboBox.setBackground( back );
//        textLabelEditComboBox.getEditor().getEditorComponent().setBackground( back );
//        textLabelEditComboBox.getEditor().getEditorComponent().setForeground( fore );
        textLabelEditComboBox.getEditor().selectAll();
        
         textLabelEditComboBox.getEditor().getEditorComponent().addKeyListener( nodeEditingKeyAdapter );

        // if the object is amenable to a choice list, then create and display one
        textLabelEditComboBox.setEnabled( false );		// to prevent premature events from being erroneously processed
        String[] namelist = null;
        if ( userEditedGraphObject instanceof Actor ) {
            namelist = GraphUpdater.getActorNames();
        } else if ( userEditedGraphObject instanceof TypeLabel ) {
            int numtypes = Global.sessionKB.getConceptTypeHierarchy().getKeys().size();
            namelist = Global.sessionKB.getConceptTypeHierarchy().getKeys().toArray( new String[ numtypes ] );
        } else if ( userEditedGraphObject instanceof Relation
                || userEditedGraphObject instanceof RelationLabel ) {
            int numtypes = Global.sessionKB.getRelationTypeHierarchy().getKeys().size();
            namelist = Global.sessionKB.getRelationTypeHierarchy().getKeys().toArray( new String[ numtypes ] );
        }
        // Since setting up the combo box generates action events before we need them,
        //   we disable the combo box. 
        //  NOTE: events can still be generated for a disabled component!
        //     I've told actionPerformed to ignore events from disabled components.

        int newWidth = CGUtil.fillChoiceList( textLabelEditComboBox, namelist, fontMetrics );
        if ( newWidth < minimumWidth ) newWidth = minimumWidth;
        
        CGUtil.loadSizedComboBox( textLabelEditComboBox, label, fontMetrics );
        textLabelEditComboBox.setSelectedItem( label );
        textLabelEditComboBox.setEnabled( true );

        Dimension size = getEditingTextSize( fontMetrics, label );
        
        if ( size.width < newWidth ) size.width = newWidth;
        newWidth += 30;         // to accommodate the combo box list control button
        textLabelEditComboBox.setSize( new Dimension( newWidth, size.height ) );
        textLabelEditComboBox.setPreferredSize( textLabelEditComboBox.getSize() );
        textLabelEditComboBox.setMinimumSize( textLabelEditComboBox.getSize() );
        Global.info( "size of label chooser  " + textLabelEditComboBox.getSize() );

        textLabelEditComboBox.setLocation( new Point( 5, 5 ) );
        textLabelEditComboBox.setVisible( true );
        return textLabelEditComboBox;
    }

    /**
     * Determines the width and height needed for the textediting activites.
     * Adds 25% padding to the height.
     * doubles the width of the minimum space needed for the label.
     * Adjust itself if the width is too short or too long.
     * @param fontMetrics
     * @param label
     * @return A size that will amply fit the label in both dimensions using the font metrics' font.
     */
    private Dimension getEditingTextSize( FontMetrics fontMetrics, String label ) {
        // Decide the HEIGHT OF THE EDITING FIELD
        // The height of the editing box -- depends only on the font to use
        int textEditingHeight = fontMetrics.getHeight() * 5 / 4;


        // Decide the WIDTH OF THE EDITING FIELD.
        int textEditingWidth = fontMetrics.stringWidth( label );
        textEditingWidth *= 2;      // use double the width if needed
        int minWidth = fontMetrics.stringWidth( "MMMM" );
        int maxWidth = ef.getWidth() - ef.editingToolbar.getWidth() - 10;
        if ( textEditingWidth < minWidth ) {
            textEditingWidth = minWidth;
        }
        if ( textEditingWidth > maxWidth ) {
            textEditingWidth = maxWidth;
        }
        return new Dimension( textEditingWidth, textEditingHeight );
    }

    /**
     * Dispose of the canvas panel; should not be called directly.
     */
    protected void finalize() throws Throwable {
        try {
            Global.info( "Canvas Panel finalized." );
            ef = null;
            super.finalize();
        } catch ( Throwable t ) {
            throw t;
        } finally {
            super.finalize();
        }
    }

    /**
     * Only handles changes to text. Other graphic changes are handled by the
     * EditFrame's mouse handlers.
     */
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        //Global.info( "action command is " + e.getActionCommand() );
        String newlabel = null;
        ef.clearStatus();
        //KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        //requestFocus();
//        if ( source == textLabelEditField ) {
        if ( source instanceof JTextField ) {
            JTextField tf = (JTextField)e.getSource();
            newlabel = tf.getText();
        } else if ( source instanceof JComboBox ) {
            JComboBox cb = (JComboBox)e.getSource();
            if ( cb.isEnabled() ) {
                String selected = (String)cb.getSelectedItem();
                if ( selected != null ) {
                    //Global.info( " get action command is " + e.getActionCommand() );
                    //Global.info( " combo box item chosen is " + cb.getSelectedItem() );
                    newlabel = selected;
                } else return;
            } else return;
        }
        if ( newlabel != null ) {
            userFinishedEditingText( newlabel );
        }
    }

    /**
     * Figures out how much canvas and buffered image is needed, and if the
     * current ones aren't sufficient, makes them big enough. Leaves 25% extra
     * room on the canvas for expansion.
     */
    public void adjustCanvasSize() {
        int neededHeight = (int)( ef.TheGraph.getContentBounds().height * ef.canvasScaleFactor * 1.25 );
        int neededWidth = (int)( ef.TheGraph.getContentBounds().height * ef.canvasScaleFactor * 1.25 );
        Dimension needed = new Dimension( neededWidth, neededWidth );
        if ( ( getWidth() < neededWidth ) || ( getHeight() < neededHeight ) ) {
            if ( Global.useBufferedImage ) {
                image = new BufferedImage( neededWidth, neededHeight, BufferedImage.TYPE_INT_ARGB );
            }
            //setPreferredSize( new Dimension( neededWidth, neededHeight ) );
            setPreferredSize( new Dimension( neededWidth, neededHeight ) );
            setSize( new Dimension( neededWidth, neededHeight ) );
            ef.sp.revalidate();
            //repaint();
        }
        //Global.info( "canvas size is " + getWidth() + " by " + getHeight() );
    }

    /**
     * Renders everything on the canvas.
     */
    public synchronized void paintComponent( Graphics g ) {
        RenderingHints rh = new RenderingHints( null );
        rh.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
        rh.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT );
        rh.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT );
        rh.put( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );

        super.paintComponent( g );
        //((Image)image).flush();
        //Graphics2D g2d = image.createGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.addRenderingHints( rh );

        //g2d.setTransform( new java.awt.geom.AffineTransform() );
        g2d.scale( ef.canvasScaleFactor, ef.canvasScaleFactor );
        //	Global.info( "at canvas panel's paint component" );

        g2d.setFont( ef.currentFont );

        ef.TheGraph.draw( g2d, false );	// not printing

        // draw a selection rectangle, if appropriate
        if ( ef.selectionRect != null && ef.getShowRubberBand() ) {
            Rectangle2D.Double r = ef.selectionRect;
            g2d.setColor( EditFrame.selectionRectColor );
            g2d.setStroke( new BasicStroke( (float)Global.userEdgeAttributes.getEdgeThickness() * 1.25f ) );
            g2d.draw( r );
            g2d.setStroke( Global.defaultStroke );
        }

        //Global.info( "selectionisbeingdragged " + ef.selectionIsBeingDragged + " " + ef.summarizeSelection() );
        // draw outlines of anything that is selected
        if ( ef.selectionIsBeingDragged ) {
            // for everything selected, drag its border relative to the cursor vs. cursorObject
            Iterator iter = ef.EFSelectedNodes.iterator();

            /*g2d.setColor( Color.green );
             double x = (double)ef.dragCurrPt.x;
             double y = (double)ef.dragCurrPt.y;
             g2d.draw( new Polygon( new double[] = {x, x, x, x }, new i
             */

            // trans is used when we're moving a rectangle selection
            Point2D.Double trans = new Point2D.Double( ef.dragCurrPt.x - ef.dragStartPt.x,
                    ef.dragCurrPt.y - ef.dragStartPt.y );
            double transx = ef.dragCurrPt.x - ef.dragStartPt.x;
            double transy = ef.dragCurrPt.y - ef.dragStartPt.y;
            while ( iter.hasNext() ) {
                // Global.info( "drawing each dragged object." );
                GraphObject go = (GraphObject)iter.next();
                if ( go.myKind != GraphObject.Kind.GEDGE ) // omit GEdges for showing
                {
                    Rectangle2D.Double r =
                            new Rectangle2D.Double( go.displayRect.x + transx, go.displayRect.y + transy,
                            go.displayRect.width, go.displayRect.height );
                    //r.translate( transx, transy ); 
                    g2d.setColor( go.backColor );
                    // draw 2-pixel border
                    //g2d.drawRect( r.x, r.y, r.width, r.height );
                    g2d.draw( r );
                    g2d.draw( new Rectangle.Double( r.x - 1, r.y - 1, r.width + 2, r.height + 2 ) );
                    // draw center point
                    g2d.fill( new Rectangle2D.Double( r.x + r.width / 2 - 2, r.y + r.height / 2 - 2, 5f, 5f ) );
                }
            }
        }

        // draw an edge under construction, if we're in the process of creating one
        if ( ef.dotIsBeingDragged ) {
            g2d.setColor( ef.lineDragColor );
            g2d.draw( new Line2D.Double( ef.dragStartPt.x, ef.dragStartPt.y,
                    ef.dragCurrPt.x, ef.dragCurrPt.y ) );
        }

        // show the editing text field, if there is one
        //if (userEditedGraphObject != null) {
        //	nodeEditingDialog.setLocation( ef.antiscaled( userEditedGraphObject.getPos() ) );
        //}

        if ( Global.ShowBoringDebugInfo ) {
            ef.LEDcolor = new Color( ( ef.LEDcolor.getRGB() * -1 ) | 0xff000000 );
            g2d.setColor( ef.LEDcolor );
            JViewport v = ef.sp.getViewport();
            g2d.fillRect( v.getViewPosition().x, v.getViewPosition().y, 4, 4 );


            //g.drawString( ef.getMyFileLocation(), 
            //		v.getViewRect().x+v.getViewRect().width, v.getViewRect().height-15 );
        }
        //boolean dontcare = g.drawImage( image, 0, 0, new Color(0,0,0,0), this );

        //Global.info( "after canvas panel paint components" );
    }

    /**
     * In case the user stops in the middle of something. Closes the editing
     * text box.
     */
    public void reset() {
        /*f ( userEditedGraphObject != null)  closeTextEditor(); */
        ef.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    /**
     * Determines whether a pull-down menu is possible for this object.
     *
     * @param go Any graph object
     * @return Whether it makes sense to do a pull-down menu for this object's
     * label.
     */
    public boolean choicePossible( GraphObject go ) {
        if ( go instanceof Actor ) {
            return true;
        } else if ( go instanceof TypeLabel ) {
            return true;
        } else if ( go instanceof Relation ) {
            return true;
        } else if ( go instanceof RelationLabel ) {
            return true;
        }
        return false;
    }

    /**
     * Sets up the text field for a user to edit. Control is then passed to the
     * user. Displays default prompt in the message box.
     *
     * @param go The object whose label the user is editing.
     * @param p Where the edit field dialog is to appear on the canvas.
     * @see CanvasPanel#userFinishedEditingText
     */
    public void userStartedEditingText( GraphObject go, Point2D.Double p ) {

        int minimumWidth = 35;
        createNodeEditingDialog();
        Global.info( "user started editing object " + go.getTextLabel());
        //Global.info( "user started editing. keyboard focus window is " + 
        //	KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow().toString());
        ef.clearStatus();

        // Decide what font metrics to use
        Font localFont = go.getLabelFont();
        FontMetrics localFontMetrics = getGraphics().getFontMetrics( localFont );

        // Decide what TEXT TO EDIT
        String currentTextLabel = go.getTextLabel();

        // Decide fore and back colors
        Color foregroundColor = go.getColor( "text " );
        Color backgroundColor = go.getColor( "fill " );


        // Decide the LOCATION OF THE EDITING DIALOG
        nodeEditingDialog.setLocationRelativeTo( ef.sp );
        int x = (int)( ef.antiscaled( go.getUpperLeft() ).getX()
                - (int)( ef.sp.getViewport().getViewPosition().getX() ) + ef.getLocationOnScreen().x
                + ef.sp.getX() );
        int y = (int)( ef.antiscaled( go.getUpperLeft() ).getY()
                - (int)( ef.sp.getViewport().getViewPosition().getY() ) + ef.getLocationOnScreen().y
                + ef.sp.getY() );
        nodeEditingDialog.setLocation( new Point( x, y ) );
        nodeEditingDialog.setResizable( false );

        // Set up DIALOG APPEARANCE. If it's set as undecorated, this title won't even appear
//        if ( go instanceof Note ) {
//            nodeEditingDialog.setTitle( "Change text of " + CGUtil.shortClassName( go ) );
//        } else {
//            nodeEditingDialog.setTitle( "Rename " + CGUtil.shortClassName( go ) );
//        }
        if ( nodeEditingDialogUndecorated ) nodeEditingDialog.setTitle( "" );

        // Remember which object is being edited
        userEditedGraphObject = go;

        int verticalPadding = nodeEditingDialogUndecorated ? 15 : 40;       // should be 30 if the dialog is decorated (i.e., title bar shows)

        if ( choicePossible( userEditedGraphObject ) ) // if using the choice list, use the label chooser
        {
            JComboBox labelChooser = createLabelChooser( localFontMetrics, currentTextLabel, foregroundColor, backgroundColor );

            labelChooser.setLocation( new Point( 5, 5 ) );

            int horizontalPadding = 25; // to account for the combo box control
            nodeEditingDialog.setSize( labelChooser.getWidth() + horizontalPadding, labelChooser.getHeight() + verticalPadding );
//            nodeEditingDialog.setMinimumSize( textLabelEditField.getWidth() + 10, textLabelEditField.getHeight() + 50 );
          nodeEditingDialog.setLayout( new BorderLayout());
          nodeEditingDialog.add( labelChooser, BorderLayout.CENTER );
            nodeEditingDialog.getContentPane().setBackground( backgroundColor );
            labelChooser.setEnabled( true );

        } else {    // Here for those case where there is no choice box
            JTextField textLabelEditField = createTextEditField( localFontMetrics, currentTextLabel, foregroundColor, backgroundColor );
            textLabelEditField.selectAll();
            // select all so that user can immediately replace
            //Global.info( "in user Started editing; Text edit field bounds is " + textLabelEditField.getBounds() +
            //		", visible is " + textLabelEditField.isVisible() );
            textLabelEditField.setLocation( new Point( 5, 5 ) );
            ef.setCursor( new Cursor( Cursor.TEXT_CURSOR ) );

            nodeEditingDialog.setSize( textLabelEditField.getWidth() + 10, textLabelEditField.getHeight() + verticalPadding );
//            nodeEditingDialog.setMinimumSize( textLabelEditField.getWidth() + 10, textLabelEditField.getHeight() + 50 );
            nodeEditingDialog.add( textLabelEditField );
        }
        ef.setMenuItems();
        nodeEditingDialog.setBackground( backgroundColor );
        nodeEditingDialog.setFocusableWindowState( true );
        nodeEditingDialog.setVisible( true );	// may invoke events on labelchooser or textLabelEditField
    }

    /**
     * Once the user leaves the edit field, take over and process the new
     * contents.
     *
     * @param userEditedLabel new label name; could have been obtained from a
     * choice list. null if user cancelled or otherwise didn't make a new label
     * name.
     * @see CanvasPanel#userStartedEditingText
     * @see CanvasPanel#actionPerformed
     */
    public void userFinishedEditingText( String userEditedLabel ) {
        ef.setMenuItems();
        ef.setEnabled( true );
        Global.info( "user finished editing " + userEditedLabel );
        if ( userEditedGraphObject == null ) {
            Global.info( "no object edited" );
            return;
        }

        // before trying to change the label, save its old contents
        String oldTextLabel = userEditedGraphObject.getTextLabel();

        if ( userEditedLabel == null ) {
            userEditedLabel = oldTextLabel;
        }
        if ( userEditedLabel.equals( "" ) ) {
            userEditedLabel = "null";
        }
        // set the edited object's text label here


        // if a graph name was edited by the user, set its pos here, to adjust its dimensions
        // compare old label to see if there was a change
        // note that if user only changes upper/lower case of label, nothing happens
        //if ( ! oldTextLabel.equalsIgnoreCase( userEditedLabel ) )
        if ( !oldTextLabel.equals( userEditedLabel ) ) {
            try {
                // Note that we don't call forgetObject, because we're just changing the name
                Global.sessionKB.unCommit( userEditedGraphObject );        // This should replace the old label, unless it's one of the builtins.
            } catch ( KBException ex ) {
                Global.warning( ex.getMessage() + " on object " + ex.getSource().toString() );

            }
            // AFter it's been uncommitted, change the label and continue 
            userEditedGraphObject.setTextLabel( userEditedLabel );
            if ( userEditedGraphObject instanceof GNode ) {
                ( (GNode)userEditedGraphObject ).setChanged( true );
                ( (GNode)userEditedGraphObject ).setActive( false );
            }
//            if ( ef.ToolMode.equals( "Note")) changeState = EditChange.UNDOABLE );
            // TODO: need to make sure that note changes don't imply changed semantics
            // Here is where any code that needs to verify the graph goes...
            ef.emgr.setChangedContent( EditChange.SEMANTICS, EditChange.UNDOABLE );
            try {
                Global.sessionKB.commit( userEditedGraphObject );
            } catch ( KBException ex ) {
                Global.warning( ex.getMessage() + " on object " + ex.getSource().toString() );
                //Logger.getLogger( CanvasPanel.class.getName() ).log( Level.SEVERE, null, ex );

            }
            ef.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
            if ( Global.enableActors ) {
                Global.info( "before update graph object " );
                GraphUpdater gu = new GraphUpdater( ef, userEditedGraphObject );
                // no need to check for null ef, since all canvas panels are in a frame
                new Thread( ef.threadgroup, gu, "started by user edit of " + userEditedLabel ).start();
                //Global.info( "after update graph object " );
            }
        }
        closeTextEditor();
        //ef.emgr.clearChanged( ef.TheGraph );
        requestFocus();
        //ef.setEnabled( true );
        if ( KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() != null ) {
//            Global.info( "end of user finished editing. keyboard focus window is "
//                    + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow().getClass().getName() );
        } else {
//            Global.info( "end of user finished editing. keyboard focus window not in this thread." );
        }
    }

    /**
     * Hide the editing field, pop-up menu, re-set the editing menu and cursor.
     * //Set all objects as unchanged (to disable erroneous display and/or
     */
    
     public void closeTextEditor() {
//        Global.info( "begin close text editing" );
        userEditedGraphObject = null;
        
        if ( nodeEditingDialog != null ) {
            nodeEditingDialog.removeAll();
            nodeEditingDialog.dispose();
        }

        if ( labelChooser != null ) {
            labelChooser.setEnabled( false );
            labelChooser.removeAllItems();
            labelChooser.setVisible( false );
            labelChooser = null;
        }
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /*
     Scrollable interface implemented here
     */
    public Dimension getPreferredScrollableViewportSize() {
        Global.info( "getPreferredScrollableViewportSize, size is "
                + getSize() + ", pref size is " + getPreferredSize() );
        return getPreferredSize();
    }
    // Returns the preferred size of the viewport for a view component.

    public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
        return 250;
    }
    //  Components that display logical rows or columns should compute the scroll increment that will completely
    // expose one block of rows or columns, depending on the value of orientation.

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    //Return true if a viewport should always force the height of this Scrollable to match the height of the viewport.

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    // Return true if a viewport should always force the width of this Scrollable to match the width of the viewport.

    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
        return 20;
    }
    // Components that display logical rows or columns should compute the scroll increment that will completely
    // expose one new row or column, depending on the value of orientation.

    public void keyReleased( KeyEvent e ) {
    }

    public void keyTyped( KeyEvent e ) {
        //Global.info( "key typed " + e );
        int key = e.getKeyCode();
        switch ( key ) {
        }
    }
} // class end

