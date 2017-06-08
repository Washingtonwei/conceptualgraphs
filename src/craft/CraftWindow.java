//
//  CraftWindow.java
//  CharGer 2003
//
//  Created by Harry Delugach on Mon May 05 2003.
//
package craft;

import charger.EditFrame;
import charger.Global;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import repgrid.*;
import repgrid.tracks.*;

import charger.obj.Concept;
import charger.obj.GNode;

import charger.util.CGButton;
import charger.util.ManagedWindow;
import charger.util.Util;
import charger.util.WindowManager;

import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
 * The controlling frame for the repertory grid elicitation subsytem. In some
 * ways, it mimics the HubFrame, since both of them are the "hub" window for a
 * bunch of other windows. All the RGDisplayWindow instances are instantiated by
 * this window.
 */
public class CraftWindow extends JFrame implements ManagedWindow {

    //  COMPONENTS of the layout
		/*
     The layout consists of a single holder Box, with two components -
     - banner, with an identifying string
     - a table panel with a border layout
     - north pane
     - instruction label announcing what the tabl is for
     - buttons for operating on the table (only visible when some row is selected)
     - clear selection 
     - show subgraph (visible when there's a selection)
     - start grid
     - a south pane
     - a sentence summarizing the selection
     - a center scroll pane
     - contains the table of relationships
					
     */
    private JLabel banner = new JLabel( "CRAFT - Conceptual Requirements Acquisition and Formation Tool " );
    private JScrollPane sp = new JScrollPane();
    public JTable conceptTable = null;
    public JPanel panel = new JPanel();
    public JPanel southPanel = new JPanel();
    private Concept conceptOne = null;		// first of two concepts to be elicited
    private Concept conceptTwo = null;		// second of two concepts to be elicited
    private Concept selectedConcept = null;
    private GNode relation = null;
    private int selectedRow = -1; 	// the row of the conceptTable that's selected (if any)
    JTextField c1field = new JTextField();
    JTextField c2field = new JTextField();
    JTextField rfield = new JTextField();
    public JLabel sentenceLabel = new JLabel();
    private JMenuBar craftMenuBar = new JMenuBar();
    public JMenu fileMenu = new JMenu( Global.strs( "FileMenuLabel" ) );
    public Action OpenAllAction = new AbstractAction() {
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return Global.strs( "OpenAllLabel" );
            }
            return super.getValue( s );
        }

        public void actionPerformed( ActionEvent e ) {
            performOpenAll();
        }
    };
    public Action refreshAction = new AbstractAction() {
        public Object getValue( String s ) {
            if ( s.equals( Action.NAME ) ) {
                return "Refresh listing";
            }
            return super.getValue( s );
        }

        public void actionPerformed( ActionEvent e ) {
            refresh();
        }
    };
    private static JMenuItem fileMenuNew = new JMenuItem( Global.strs( "NewWindowLabel" ) );
    private static JMenuItem fileMenuOpen = new JMenuItem( Global.strs( "OpenLabel" ) );
    private JMenuItem fileMenuOpenAll = new JMenuItem( OpenAllAction );
    public static JMenuItem fileMenuPreferences = new JMenuItem( Global.strs( "PreferencesLabel" ) );
    private static JMenuItem fileMenuClose = new JMenuItem( Global.strs( "CloseLabel" ) );
    public JMenu windowMenu = new JMenu( Global.strs( "WindowMenuLabel" ) );
    public JMenu operationMenu = new JMenu( Global.strs( "OperationMenuLabel" ) );
    public JMenuItem refreshItem = new JMenuItem( refreshAction );
    public JMenuItem operationMakeTypeHierarchy = new JMenuItem( kb.ConceptManager.makeTypeHierarchyAction );
    public static JMenuItem operationSummarize = new JMenuItem( Global.knowledgeManager.summarizeKnowledgeAction );
    private CGButton clearSelection = new CGButton();
    private CGButton showSubgraph = new CGButton();
    private CGButton startGrid = new CGButton();
    private CGButton chooseConcept1 = new CGButton();
    private CGButton chooseConcept2 = new CGButton();
    private JLabel instruction = new JLabel( "Select a row that you want to say something about" );
    private boolean somethingChanged = false;		// not used for now

    /**
     * Create the single craft window that controls elicitation.
     */
    public CraftWindow() {
        WindowManager.manageWindow( this, KeyStroke.getKeyStroke( KeyEvent.VK_1, Global.AcceleratorKey ) );
        setupWindow();
        setVisible( true );
    }

    /**
     * Re-loads the concept table from all open graphs, and put the window
     * visible and in front.
     */
    public void refresh() {
        Craft.say( "refresh in Craft window called" );
        setSize( new Dimension( Global.CharGerMasterFrame.getSize().width, 400 ) );

        //conceptTable = kb.ConceptManager.getConceptTable( CRConceptManager.GENERIC_ONLY );		
        // gather only generic concepts
        //conceptTable = kb.ConceptManager.getBinaryTupleTable( kb.ConceptManager.GENERIC_ONLY );		
        //conceptTable = kb.ConceptManager.getBinaryTupleTable( kb.ConceptManager.ConceptsToInclude.ALL );
//        if ( Craft.useOnlyGenericConceptsinCraft ) {
//            conceptTable = kb.ConceptManager.getBinaryTupleTable( kb.ConceptManager.ConceptsToInclude.GENERIC_ONLY );
//        } else {
//            conceptTable = kb.ConceptManager.getBinaryTupleTable( kb.ConceptManager.ConceptsToInclude.ALL );
//        }
        
        

        // gather only generic concepts
//		TableSorter sorter = new TableSorter( conceptTable.getModel() );
        TableModel readonlymodel = new DefaultTableModel( new Object[1][], kb.BinaryTuple.getTupleColumnLabels().toArray() ) {
//            readonlymodel = new DefaultTableModel( binaryRelationTuples, tupleColumnLabels ) {
            public boolean isCellEditable( int rowIndex, int columnIndex ) {
                return false;
            }
        };

        //TableSorter sorter = new TableSorter( readonlymodel );
//        conceptTable = new JTable( sorter );
//        sorter.addMouseListenerToHeaderInTable( conceptTable );
        conceptTable = new JTable( readonlymodel );
//        readonlymodel.addMouseListenerToHeaderInTable( conceptTable );
        conceptTable.setAutoCreateRowSorter( true );
        conceptTable.setGridColor( Color.blue );
        conceptTable.setShowGrid( true );
        if ( conceptTable.getRowCount() == 0 ) {
            instruction.setText( "Table is empty because no graphs are open or available." );
            performClearTupleSelection();
        } else {
            instruction.setText( "Select a row that you want to say something about" );
        }
//        conceptTable.sortByColumn( 0 );
        // TODO: will probably want to figure out how to do this some other way.

        if ( Global.CRAFTuseOnlyGenericConceptsinCraft ) {
            conceptTable.setModel(kb.ConceptManager.getBinaryTupleModel( kb.ConceptManager.ConceptsToInclude.GENERIC_ONLY ));
        } else {
            conceptTable.setModel(kb.ConceptManager.getBinaryTupleModel( kb.ConceptManager.ConceptsToInclude.ALL ));
        }


        // copied from http://java.sun.com/docs/books/tutorial/uiswing/components/example-swing/SimpleTableSelectionDemo.java
        ListSelectionModel rowSM = conceptTable.getSelectionModel();
        rowSM.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
                performTupleSelection( e );
            }
        } );
        rowSM.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        // If there aren't any graph windows open, then don't bother with the rest.
        if ( Global.editFrameList.size() == 0 )
            return;

        //conceptTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 150 );
        //conceptTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 150 );
        Util.adjustTableColumnWidths( conceptTable );

        // these columns are used to hold the actual graph objects to be associated with the grid
        Util.hideTableColumn( conceptTable.getColumnModel().getColumn( kb.ConceptManager.COL_CONCEPT_1 ) );
        Util.hideTableColumn( conceptTable.getColumnModel().getColumn( kb.ConceptManager.COL_RELATION ) );
        Util.hideTableColumn( conceptTable.getColumnModel().getColumn( kb.ConceptManager.COL_CONCEPT_2 ) );
        //Util.hideTableColumn( conceptTable.getColumnModel().getColumn( kb.ConceptManager.COL_SUBGRAPH ) );


        conceptTable.setPreferredScrollableViewportSize( new Dimension( 450, 150 ) );
        //Craft.say( "CraftWindow: refresh(): model row count is " + conceptTable.getModel().getRowCount() );
        //+ "; model col count is " + conceptTable.getModel().getColumnCount() );
        sp.setViewportView( conceptTable );
        
        performClearTupleSelection();

        toFront();
        requestFocus();
        setVisible( true );
    }

    private void setupWindow() {
        setLocation( new Point( Global.CharGerMasterFrame.getLocation().x,
                Global.CharGerMasterFrame.getSize().height + Global.CharGerMasterFrame.getLocation().y ) );
        //getContentPane().setLayout( new FlowLayout() );

        setPreferredSize( new Dimension( 800, 400 ) );

        //setTitle( "CRAFT: Conceptual Requirements Acquisition and Formation Tool" );
        setTitle( "CRAFT - Requirements Acquisition" );
        getContentPane().setBackground( new Color( 255, 200, 200 ) );
        getContentPane().setLayout( null );

        banner.setLocation( new Point( 5, 5 ) );
        banner.setFont( new Font( "SansSerif", Font.BOLD + Font.ITALIC, 16 ) );
        banner.setForeground( Global.chargerBlueColor );
        banner.setSize( banner.getPreferredSize() );
        getContentPane().add( banner );

        setupScrollPane();
        setupNorthPanel();
        setupSouthPanel();
        setupMenuBar();

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                thisWindowClosing();
            }

            public void windowActivated( WindowEvent e ) {
                thisWindowActivated( e );
            }

            public void windowOpened( WindowEvent e ) {
                thisWindowOpened( e );
            }

            public void windowDeactivated( WindowEvent e ) {
                thisWindowDeactivated( e );
            }
        } );

        addFocusListener( new FocusAdapter() {
            public void focusGained( FocusEvent e ) {
                thisFocusGained( e );
            }

            public void focusLost( FocusEvent e ) {
                thisFocusLost( e );
            }
        } );
        validate();
    }

    private void setupMenuBar() {
        setJMenuBar( craftMenuBar );
        craftMenuBar.add( fileMenu );
        fileMenuNew.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, Global.AcceleratorKey ) );
        fileMenu.add( fileMenuNew );
        fileMenuOpen.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, Global.AcceleratorKey ) );
        fileMenu.add( fileMenuOpen );
        fileMenu.add( fileMenuOpenAll );

        fileMenu.add( fileMenuPreferences );

        craftMenuBar.add( operationMenu );
        operationMenu.add( refreshItem );
        operationMenu.add( operationMakeTypeHierarchy );
        operationMenu.add( operationSummarize );

        fileMenuClose.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_W, Global.AcceleratorKey ) );

        fileMenu.add( fileMenuClose );
        fileMenuClose.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                thisWindowClosing();
            }
        } );

        fileMenuNew.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                RGDisplayWindow.fileMenuNewActionPerformed();
            }
        } );

        fileMenuOpen.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
                RGDisplayWindow.fileMenuOpenActionPerformed();
                setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
            }
        } );

        fileMenuPreferences.setText( Global.strs( "PreferencesLabel" ) );
        fileMenuPreferences.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                fileMenuPreferencesActionPerformed( e );
            }
        } );

        craftMenuBar.add( windowMenu );

    }

    /**
     * The concept-relation-concept tuples are shown in a table in this scroll
     * pane.
     */
    private void setupScrollPane() {
        //sp.setPreferredSize( new Dimension( getSize().width-10, 100 ) );
        sp.setBackground( Color.white );
        sp.getViewport().setBackground( new Color( 245, 245, 255 ) );
        //sp.setSize( new Dimension( getSize().width-10, 300 ) );
        //sp.setPreferredSize( new Dimension( 750, 300 ) );
        sp.setOpaque( true );

        refresh();

        panel.setLayout( new BorderLayout() );
        panel.setPreferredSize( new Dimension( 750, 350 ) );

        // seems to have no effect // panel.setPreferredSize( new Dimension( getSize().width-10, 300 ) );
        panel.setSize( new Dimension( getSize().width - 20, getSize().height - 100 ) ); //banner.getSize().height*2 - 10 ) );
        panel.setOpaque( true );
        panel.setLocation( new Point( 5, 40 ) );
        panel.add( sp );
        getContentPane().add( panel );
        //panel.validate();
        //Global.info( "panel is " + panel );
    }

    private void setupNorthPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout( new FlowLayout() );
        northPanel.setBackground( Color.white );

        instruction.setPreferredSize( instruction.getPreferredSize() );

        clearSelection.setText( "Clear Selection" );
//        clearSelection.setBackground( Global.chargerBlueColor );
//        clearSelection.setForeground( Color.white );
        clearSelection.setPreferredSize( new Dimension( 150, chooseConcept1.getSize().height ) );
        clearSelection.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performClearTupleSelection();
            }
        } );

        chooseConcept1.setText( "Select Concept 1" );
//        chooseConcept1.setBackground( Global.chargerBlueColor );
//        chooseConcept1.setForeground( Color.white );
        chooseConcept1.setPreferredSize( new Dimension( 150, chooseConcept1.getSize().height ) );
        chooseConcept1.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( selectedConcept == null ) {
                    JOptionPane.showMessageDialog( null, "Please select a concept for elicitation.",
                            "Missing Concept", JOptionPane.ERROR_MESSAGE );
                } else {
                    //performChooseConceptForElicitation( selectedConcept, true );
                    performChooseConceptForElicitation( selectedConcept );
                }
            }
        } );

        chooseConcept2.setText( "Select 2nd Concept" );
//        chooseConcept2.setBackground( Global.chargerBlueColor );
//        chooseConcept2.setForeground( Color.white );
        chooseConcept2.setPreferredSize( new Dimension( 150, chooseConcept1.getSize().height ) );
        if ( conceptOne == null ) {
            chooseConcept2.setVisible( false );
        } else {
            chooseConcept2.setVisible( true );
        }
        chooseConcept2.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( selectedConcept == null ) {
                    JOptionPane.showMessageDialog( null, "Please select a concept for elicitation.",
                            "Missing Concept", JOptionPane.ERROR_MESSAGE );
                } else {
                    //performChooseConceptForElicitation( selectedConcept, false );
                    performChooseConceptForElicitation( selectedConcept );
                }
            }
        } );

        showSubgraph.setText( Global.strs( "ShowSubgraphLabel" ) );
//        showSubgraph.setBackground( Global.chargerBlueColor );
//        showSubgraph.setForeground( Color.white );
        showSubgraph.setPreferredSize( new Dimension( 150, chooseConcept1.getSize().height ) );
        showSubgraph.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performShowSubgraph( conceptOne, relation, conceptTwo );
            }
        } );

        startGrid.setText( "Start Grid" );
//        startGrid.setBackground( Global.chargerBlueColor );
//        startGrid.setForeground( Color.white );
        startGrid.setPreferredSize( new Dimension( 150, chooseConcept1.getSize().height ) );
        startGrid.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performStartGrid();
            }
        } );

        northPanel.add( instruction );
        northPanel.add( clearSelection );
        //northPanel.add( chooseConcept1 );
        //northPanel.add( chooseConcept2 );
        northPanel.add( showSubgraph );
        northPanel.add( startGrid );

        if ( Global.editFrameList.size() > 0 )
            performClearTupleSelection();

        panel.add( northPanel, BorderLayout.NORTH );
    }

    private void setupSouthPanel() {
        southPanel.setLayout( new FlowLayout() );
        southPanel.setBackground( Color.white );
        sentenceLabel.setForeground( Global.chargerBlueColor );
        sentenceLabel.setFont( sentenceLabel.getFont().deriveFont( sentenceLabel.getFont().getSize() + 2 ) );
        southPanel.add( sentenceLabel );
        refreshSouthPanel();
        southPanel.setVisible( false );

        panel.add( southPanel, BorderLayout.SOUTH );
    }

    /**
     * The south panel contains a paraphrase of the relation selected, or blank
     * if none is selected.
     */
    private void refreshSouthPanel() {
        if ( conceptOne != null ) {
            c1field.setText( conceptOne.getTypeLabel() );
        }
        if ( conceptTwo != null ) {
            c2field.setText( conceptTwo.getTypeLabel() );
        }
        if ( relation != null ) {
            rfield.setText( relation.getTextLabel() );
        }
        sentenceLabel.setText( kb.ConceptManager.makeSentence( conceptOne, relation, conceptTwo ) );
        southPanel.setVisible( true );
    }

    public void thisWindowClosing() {
        if ( somethingChanged ) {
        }
        performClearTupleSelection();
        // actually get rid of this window
        WindowManager.forgetWindow( this );
        setVisible( false );
        //dispose();
    }

    public void thisWindowActivated( WindowEvent e ) {
        Global.refreshWindowMenuList( windowMenu, this );
        refresh();
    }

    public void thisWindowOpened( WindowEvent e ) {
    }

    public void thisWindowDeactivated( WindowEvent e ) {
    }

    public void thisFocusGained( FocusEvent e ) {
    }

    public void thisFocusLost( FocusEvent e ) {
    }

    /**
     * Manage the preferences for CRAFT. Invokes CharGer's preference window and
     * chooses the CRAFT pane.
     *
     * @param e ignored
     */
    public void fileMenuPreferencesActionPerformed( ActionEvent e ) {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        Global.managePreferencesFrame();
                //TODO: Fix so that CRAFT can call up the preferences
//        Global.pw.mainPane.setSelectedComponent( Global.pw.CraftPanel );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    /**
     *
     */
    public void performTupleSelection( ListSelectionEvent e ) {
        //Ignore extra messages.
        if ( e.getValueIsAdjusting() ) {
            return;
        }

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if ( lsm.isSelectionEmpty() ) {
            //charger.Hub.warning("No rows are selected.");
        } else {
            selectedRow = lsm.getMinSelectionIndex();
            conceptOne = (Concept)conceptTable.getModel().getValueAt( selectedRow, kb.ConceptManager.COL_CONCEPT_1 );
            relation = (GNode)conceptTable.getModel().getValueAt( selectedRow, kb.ConceptManager.COL_RELATION );
            conceptTwo = (Concept)conceptTable.getModel().getValueAt( selectedRow, kb.ConceptManager.COL_CONCEPT_2 );
            //selectedConcept = (Concept)conceptTable.getModel().getValueAt( selectedRow, 2 );
            Craft.say( "Row " + selectedRow + " is now selected." );
            refreshSouthPanel();

            clearSelection.setVisible( true );
            showSubgraph.setVisible( true );
            startGrid.setVisible( true );
            instruction.setVisible( false );

            validate();
        }

    }

    /**
     * Reset everything in the window that may have been based on a previous
     * selection. Sets the selected concepts and relation to null, clears the
     * displayed sentence, and hides the selection examination buttons.
     */
    public void performClearTupleSelection() {
        conceptOne = null;
        conceptTwo = null;
        relation = null;
        c1field.setText( "" );
        c2field.setText( "" );
        rfield.setText( "" );

        clearSelection.setVisible( false );
        showSubgraph.setVisible( false );
        startGrid.setVisible( false );
        instruction.setVisible( true );
        southPanel.setVisible( false );

        conceptTable.getSelectionModel().clearSelection();
    }

    /**
     * Adds given concept to the binary relation being elicited in the
     * succeeding repertory grid. If conceptOne is null, then assume the concept
     * is being selected as the first concept, otherwise assume it's the second
     * concept and we're nnow ready to elicit the grid.
     */
    public void performChooseConceptForElicitation( Concept c ) {
        if ( conceptOne == null ) {
            conceptOne = c;
            // prompt for concept two
            Craft.say( "one concept acquired: " + conceptOne.toString() );
        } else {
            if ( c == conceptOne ) {
                JOptionPane.showMessageDialog( this,
                        "Concept \"" + conceptOne.getTextLabel() + "\" in \""
                        + conceptOne.getOwnerFrame().getTitle() + "\" is already chosen."
                        + "\nPlease choose another concept.", "Error in selecting concepts",
                        JOptionPane.ERROR_MESSAGE );
            } else {
                conceptTwo = c;
                // prompt to set up repertory grid based on two concepts
                Craft.say( "two concepts acquired: " + conceptOne.toString() + " and " + conceptOne.toString() );
            }
        }
    }

    /**
     * Locate the conceptual relation represented in the table and "select" it
     * in its corresponding EditFrame.
     */
    public void performShowSubgraph( Concept c1, GNode rel, Concept c2 ) {
        if ( c1 == null || c1 == null ) {
            JOptionPane.showMessageDialog( this, "Please choose a relation first.",
                    "Error in selecting concepts", JOptionPane.ERROR_MESSAGE );
            return;
        }
        EditFrame ef = c1.getOutermostGraph().getOwnerFrame();
        if ( c2.getOutermostGraph().getOwnerFrame() != ef ) {
            JOptionPane.showMessageDialog( this, "Chosen concepts are in different windows.\nConcept \""
                    + c1.getTextLabel() + "\" is in \"" + ef.getTitle() + "\";\nConcept \""
                    + c2.getTextLabel() + "\" is in window \""
                    + c2.getOutermostGraph().getOwnerFrame().getTitle() + "\"",
                    "Error in selecting concepts", JOptionPane.ERROR_MESSAGE );
            return;
        }
        if ( ef == null ) {
            JOptionPane.showMessageDialog( this, "No editing window seems to be available",
                    "Error in selecting concepts", JOptionPane.ERROR_MESSAGE );
            return;
        }
        Global.setCurrentEditFrame( ef );
        ef.resetSelection();
        ef.addToSelection( c1 );
        ef.addToSelection( rel );
        ef.addToSelection( c2 );
        //Craft.say( "concepts c1 and c2 are " + c1 + " " + c2 + "; rel is " + rel );
        Rectangle2D.Double rectToShow = (Rectangle2D.Double)rel.getDisplayRect().createUnion(
                c1.getDisplayRect().createUnion( c2.getDisplayRect() ) );
        //rectToShow.grow( 25, 25 );
        rectToShow.setFrameFromCenter( new Point2D.Double( rectToShow.x, rectToShow.y ),
                new Point2D.Double( rectToShow.x - 25, rectToShow.y - 25 ) );
        ef.cp.scrollRectToVisible( ef.antiscaled( rectToShow ).getBounds() );
        //rectToShow.grow( -5, -5 );
        rectToShow.setFrameFromCenter( new Point2D.Double( rectToShow.x, rectToShow.y ),
                new Point2D.Double( rectToShow.x + 5, rectToShow.y + 5 ) );

        ef.selectionRect = rectToShow;
        ef.showRubberBand = true;
        ef.repaint();
        ef.toFront();
    }

    /**
     * Use the selected concepts/relation to establish an
     * element-relation-attribute triple from which to form a repertory grid.
     */
    public void performStartGrid() {
        if ( conceptOne == null || conceptTwo == null || relation == null ) {
            JOptionPane.showMessageDialog( this, "Please choose a relation first.",
                    "Error in selecting concepts", JOptionPane.ERROR_MESSAGE );
            return;
        }


        TrackedRepertoryGrid g = new TrackedRepertoryGrid( conceptOne, relation, conceptTwo, new RGBooleanValue() );
        //g.elementLabel = conceptOne.getTypeLabel();
        //g.attributeLabel = conceptTwo.getTypeLabel();
        //g.relationLabel = relation.getTextLabel();
        //g.setValueType( new RGBooleanValue() );
        // make up a new file name for the grid, but leave it un-saved
        StringBuilder newname = new StringBuilder();
        if ( conceptOne.getOwnerFrame() != null ) {
            newname.append( Util.stripFileExtension(
                    conceptOne.getOwnerFrame().graphAbsoluteFile.getAbsolutePath() ) );
        } else {
            newname.append( Util.stripFileExtension( g.name ) );
        }
        // append some strings to make a unique name
        newname.append( "-" + makePrefix( conceptOne.getTypeLabel(), 5 ) );
        newname.append( "-" + makePrefix( relation.getTypeLabel(), 4 ) );
        newname.append( "-" + makePrefix( conceptTwo.getTypeLabel(), 5 ) );
        // append the suffix
        newname.append( Craft.rgxmlSuffix );
        g.setFile( new File( newname.toString() ) );
        RGDisplayWindow win = new RGDisplayWindow( g, null );
        win.bringToFront();
        win.performDiadicElicitation();
    }

    /**
     * Converts a JTable to an HTML version, with a summary.
     */
    /*
     public String convertTableToHTML( JTable jt, String summary )
     {
     String XMLSummary = charger.xml.XMLGenerator.quoteForXML( summary.trim() );
            
     String html = "<P>" + XMLSummary + "</P>\n";
            
     html += "<TABLE summary=\"" + XMLSummary + "\">\n";
     
     for(int row = 0; row < jt.getRowCount(); row++)
     {
     html += "<TR>";
     for(int col = 0; col < jt.getColumnCount(); col++)
     {
     if ( jt.getCellRect( row, col, false ).getWidth() > 0 )
     html += " <TD>" + 
     charger.xml.XMLGenerator.quoteForXML( jt.getValueAt( row, col).toString().trim() ) + "\n";
     }
     // html += "</row>\n";
     }
     html += "</TABLE>";
            
     return html;
     }
     */
    /**
     * Converts a JTable to an XML version. Not really implemented.
     */
    /*
     public String convertTableToXML( JTable jt )
     {
     String html = "<TABLE summary=\" + summary + \">\n";
     
     for(int row = 0; row < jt.getRowCount(); row++)
     {
     html += "<row number = '" + row + "'>\n";
     for(int col = 0; col < jt.getColumnCount(); col++)
     {
     if ( jt.getCellRect( row, col, false ).getWidth() > 0 )
     html += "  <col number = '" + col + "'>" + 
     charger.xml.XMLGenerator.quoteForXML( jt.getValueAt( row, col).toString() ) + "</col>\n";
     }
     html += "</row>\n";
     }
     html += "</TABLE>";
            
     return html;
     }
     */
    private String makePrefix( String s, int numchars ) {
        return s.substring( 0, Math.min( s.length(), numchars ) );
    }

    /**
     * Part of the ManagedWindow interface
     *
     * @see ManagedWindow
     */
    public void bringToFront() {
        //refresh();
        WindowManager.bringToFront( this );
    }

    /**
     * Part of the ManagedWindow interface
     *
     * @see ManagedWindow
     */
    public String getMenuItemLabel() {
        // add a space in front for sorting purposes
        return "~ " + Global.strs( "BackToCraftCmdLabel" );
    }

    /**
     * Always returns
     * <code>null</code>for the CraftWindow class.
     */
    public String getFilename() {
        return null;
    }

    /**
     *
     */
    private void performOpenAll() {
        JOptionPane.showMessageDialog( this, Global.strs( "OpenAllLabel" ) + " is not yet implemented." );
    }
}
