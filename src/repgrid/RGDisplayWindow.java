//
//  RGDisplayWindow.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sat Apr 26 2003.
//
package repgrid;

import repgrid.tracks.*;

import charger.util.*;
import charger.Global;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import craft.*;

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
	Window used for interaction between a user and a repertory grid.
 */
public class RGDisplayWindow extends JFrame 
	implements TableModelListener, ManagedWindow, Printable {

	/** The repertory grid being managed by this window. */
	public TrackedRepertoryGrid grid = null;

	int selectedRow = -1;
	
	boolean removePreviousDescriptors = false;

		//  COMPONENTS of the layout
		/*
			The layout consists of a single holder Box, with three components -
			- a tool panel
				- elicitor component
				- button Panel
					- contains the buttons
			- a table panel
				- the grid explanatory sentence (e.g., "cat function Trouble")
				- the scrollpane with the grid table and row headers
					- the grid table
					- the rowHeader list as the scroll pane's row header
					- the attribute label as the scoll pane's upper left corner element
				- the explanatory message for each cell value (e.g, "yes if applies, no otherwise")
			- a descriptor panel
				- a scrollpane with the descriptor table
		 */
	private Box holder = Box.createVerticalBox();
	
	public JLabel attributeCornerComponent = new JLabel();
	
	public JComponent elicitorComponent = new JPanel();
		
	
	public	JPanel		toolPanel = new JPanel();
	private	JPanel 		buttonPanel = new JPanel();
	public	JTextField	gridname = new JTextField();
	public	JLabel 		valueLabel = new JLabel();
	public	Graphics	graphics = null;
	private JScrollPane sp = new JScrollPane();
	private RGTableModel model = null;
	public	JTable		gridTable = null;
	JList				rowHeader = null;
	
	private static final int colWidth = 75;
	private int tableWidth = getWidth();

	public JPanel tablePanel = new JPanel();
	
	public JPanel descriptorPanel = new JPanel();
	private JScrollPane dsp = new JScrollPane();

		// MENU INFORMATION
	public JMenuBar menuBar = new JMenuBar();
	public JMenu fileMenu = new JMenu( charger.Global.strs( "FileMenuLabel" ) );
	public JMenu editMenu = new JMenu( charger.Global.strs( "EditMenuLabel" ) );
	public JMenu windowMenu = new JMenu( charger.Global.strs( "WindowMenuLabel" ));
	public JMenuItem fileMenuNew = new JMenuItem( charger.Global.strs( "NewWindowLabel" ) );
	public JMenuItem fileMenuOpen = new JMenuItem( charger.Global.strs( "OpenLabel" ) );
	public JMenuItem fileMenuSave = new JMenuItem( charger.Global.strs( "SaveLabel" ) );
	public JMenuItem fileMenuSaveAs = new JMenuItem( charger.Global.strs( "SaveAsLabel" ) );
	public JMenuItem fileMenuSaveTranscript = new JMenuItem( "Save Transcript..." );
	public JMenuItem fileMenuExport = new JMenuItem( charger.Global.strs( "ExportLabel" ) );
		public JMenu exportOptions = new JMenu( "Export" );
	public JMenuItem fileMenuPageSetup = new JMenuItem( charger.Global.strs( "PageSetupLabel" ) );
	public JMenuItem fileMenuPrint = new JMenuItem( charger.Global.strs( "PrintLabel" ) );
	public JMenuItem fileMenuClose = new JMenuItem( charger.Global.strs( "CloseLabel" ) );
	public JMenuItem fileMenuQuit = new JMenuItem( charger.Global.strs( "QuitLabel" ) );
	
	private static javax.swing.filechooser.FileFilter RGXMLFileFilter = new javax.swing.filechooser.FileFilter() {
		    public boolean accept( File f ) {  
				return acceptRGFilename( f );
		    }
		    public String getDescription() { return "Repertory Grid (*" + craft.Craft.rgxmlSuffix + ")"; }
		};
		
	private String fillinLabel = "Fill in all blanks";
	private String diadicLabel = "Fill in by two's";
	private String addAttrLabel = "Add attribute";
	private String addElemLabel = "Add element";
	private String resolveSimLabel = "Check similarity";
	private String getSensesLabel = charger.Global.strs( "AttachOntologyLabel" );
	private String removeSensesLabel = charger.Global.strs( "DeleteOntologyLabel" );
		
		// EDIT MENU (mostly duplicates the buttons)
	public JMenuItem editMenuFillin = new JMenuItem( fillinLabel );
	public JMenuItem editMenuDiadic = new JMenuItem( diadicLabel);
	public JMenuItem editMenuAddAttr = new JMenuItem( addAttrLabel );
	public JMenuItem editMenuShowSubgraph = new JMenuItem( charger.Global.strs( "ShowSubgraphLabel" )  );
	public JMenuItem editMenuAddElem = new JMenuItem( addElemLabel );
	public JMenuItem editMenuResolveSim = new JMenuItem( resolveSimLabel );
	public JMenuItem editMenuGetSenses = new JMenuItem( getSensesLabel );
	public JMenuItem editMenuMakeSpecializations = new JMenuItem();
	public JMenuItem editMenuRemoveDescriptors = new JMenuItem();
	
	public Action makeSpecializationsAction = new AbstractAction()
	{
		public Object getValue( String s )
		{
			if ( s.equals( Action.NAME ) ) return "Make Specializations";
			return super.getValue( s );
		}
		public void actionPerformed( ActionEvent e )
		{
			performMakeSpecializations();
		}
	};
	
	public Action removeDescriptorsAction = new AbstractAction()
	{
		public Object getValue( String s )
		{
			if ( s.equals( Action.NAME ) ) return "Remove Definitions";
			return super.getValue( s );
		}
		public void actionPerformed( ActionEvent e )
		{
			performRemoveDescriptors( selectedRow );
		}
	};

	
		// ELICITATION STUFF
	public RGElicitor elicitor = null;

	public CGButton fillinButton = new CGButton();
	public CGButton diadicButton = new CGButton();
	public CGButton addAttrButton = new CGButton();
	public CGButton showSubgraphButton = new CGButton();
	public CGButton addElemButton = new CGButton();
	public CGButton resolveSimButton = new CGButton();
	public CGButton getSenses = new CGButton();
	public CGButton makeSpecializationsButton = new CGButton( makeSpecializationsAction );

		// BOOKKEEPING STUFF
	public File sourceAbsoluteFile = null;
		
	private boolean somethingChanged = false;
	private boolean showFooterOnPrint = charger.Global.showFooterOnPrint;

		// WORDNET STUFF
	private Hashtable wordSynsetTable = new Hashtable();
	public charger.gloss.wn.WordnetManager wnmgr = null;
	
	public RGDisplayWindow( TrackedRepertoryGrid inrg, RGElicitor e )
	{
		elicitor = e;
		setupWindow();
		setupToolPanel();
		setRepertoryGrid( inrg );
		setupTablePanel();
		setupTable();
		setupDescriptorPanel();
		setChanged( false );
		Craft.addRGWindow( this );
		WindowManager.manageWindow( this );
		Global.knowledgeManager.addKnowledgeSource( grid );
		//getElicitorComponent().setVisible( true );
		setVisible( true );
		validate();
		if ( elicitor == null ) elicitor = new RGDiadicElicitor( grid, model );
				// Initialize WordNet stuff
		wnmgr = charger.gloss.wn.WordnetManager.getInstance( elicitor.getTranscript() );
	}
	
	/**
		Associates a particular grid with this window. Creates a new table model for the grid, a new table, a new elicitor,
			and refreshes the window. 
	 */
	public void setRepertoryGrid( TrackedRepertoryGrid r )
	{
		grid = r;
		model = new RGTableModel( grid );	   
		rowHeader = new JList( model.rowListModel );
		gridTable = new JTable( model );
		if ( elicitor == null ) elicitor = new RGDiadicElicitor( grid, model );
		setElicitorComponent( elicitor.getHeaderComponent( this ) );
		sp.setViewportView( gridTable );
		//setupTablePanel();
		refreshTablePanel();
		setFile( grid.sourceAbsoluteFile );
	}
	
	public void setFile( File f ) 
	{
		sourceAbsoluteFile = f; 
		grid.setFile( f );
		if ( sourceAbsoluteFile != null ) setTitle( f.getAbsolutePath() );
		else setTitle( grid.getName() );
		WindowManager.changeFilename( this, getTitle() );
	}

	
	/**
		Mark the repertory grid as being changed since the last save, or since the original read.
		Adds "*" to the title and marks the window as needing saved before closing.
		@param b whether repertory grid content has changed.
	 */
	public void setChanged( boolean b ) 
	{ 
		somethingChanged = b;
		if ( b )
		{ 
			if ( ! getTitle().endsWith( "*" ) )
			{
				setTitle( getTitle() + "*" );
			}
		}
	}
	
	/**
		Sets up the components of the table panel. Should not do any setting of content.
	 */
	public void setupTablePanel()
	{
		//int tableWidth = ( model.getColumnCount() + 3 ) * colWidth;
		//if ( tableWidth > 500 ) tableWidth = 500;
		
		tablePanel.setSize( new Dimension( tableWidth, 320 ) );
		tablePanel.setLayout( new BorderLayout() );
		gridTable.setBorder(  BorderFactory.createLineBorder( Color.blue) );	
		
		gridname.setLocation( new Point( 100, 5) );
		gridname.setBackground( new Color( 230, 230, 255 ) );
		//gridname.setSize( new Dimension( tableWidth, 25 ) );
		gridname.setFont( new Font( "SansSerif", Font.BOLD, 16 ) );
		gridname.setHorizontalAlignment( SwingConstants.CENTER );
		gridname.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				//grid.setQuestionLabel( gridname.getText() );
				repaint();
			}
		});

		tablePanel.add( gridname, BorderLayout.NORTH );
				
				// set up row header to scroll with the table
		rowHeader.setCellRenderer( new CustomListCellRenderer() );
		rowHeader.setFixedCellWidth( colWidth * 3 );
		//rowHeader.setFixedCellWidth( rowHeader.getPreferredSize().width );
		rowHeader.setFixedCellHeight( gridTable.getRowHeight() );	
				//rowHeader.setBorder(  BorderFactory.createLineBorder( Color.blue) );
		
		sp.setCorner( JScrollPane.UPPER_LEFT_CORNER, attributeCornerComponent );
		attributeCornerComponent.setFont( attributeCornerComponent.getFont().deriveFont( Font.BOLD ) );
		attributeCornerComponent.setHorizontalAlignment( SwingConstants.CENTER );
		attributeCornerComponent.setBorder(  BorderFactory.createLineBorder( Color.blue) );
		

				// set up the scroll pane's characteristics
		sp.setPreferredSize( new Dimension( tableWidth, 300 ) );
		sp.setBackground( Color.white );
		sp.getViewport().setBackground( new Color( 230, 230, 255 ) );
		//sp.setSize( new Dimension( tableWidth, 300 ) );
		sp.setLocation( new Point( 0, 25 ) );
		sp.setOpaque( true );

		sp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		sp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );

		tablePanel.add( sp, BorderLayout.CENTER );
		//tablePanel.setBackground( Color.black );
		tablePanel.setOpaque( true );
		//setupTable();
		
		valueLabel.setPreferredSize( new Dimension( tableWidth, 20 ) );
		valueLabel.setHorizontalAlignment( SwingConstants.CENTER );
		valueLabel.setVisible( true );
		valueLabel.setOpaque( true );
		valueLabel.setBackground( Color.white );
		tablePanel.add( valueLabel, BorderLayout.SOUTH );
		
		
		tablePanel.setVisible( true );
	}
	
	
	/**
		Sets up both the tool panel and its enclosed button panel.
		 */
	private void setupToolPanel()
	{
		// instead of using layout, figure out my own coordinates :-(
		int vmargin = 5;
		int hmargin = 10;
		int vspace = fillinButton.getSize().height + vmargin;
		int hspace = 100;
		int startx = 5;
		int starty = 5;
		int bigbuttonwidth = 3 * hspace / 2;
		
	
		toolPanel.setBackground( craft.Craft.craftPink );
		toolPanel.setOpaque( true );
		toolPanel.setLayout( null );
		
		elicitorComponent.setBackground( toolPanel.getBackground() );
		elicitorComponent.setOpaque( true ); 
		elicitorComponent.setLocation( new Point( 5, 5 ) );
		toolPanel.add( elicitorComponent );

		buttonPanel.setLayout( null );
		
				// all the following locations are relative to the button panel, not the tool panel
				
		addElemButton.setBackground( charger.Global.chargerBlueColor );
		addElemButton.setForeground( Color.white );
		addElemButton.setSize( new Dimension( bigbuttonwidth, fillinButton.getSize().height ) );
		addElemButton.setLocation( new Point( startx, starty ) );
		addElemButton.setText( "Add Element" );
		ActionListener addElemAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				performNewElement();
			}
		};
		addElemButton.addActionListener( addElemAL );
		editMenuAddElem.addActionListener( addElemAL );
		addElemButton.setVisible( true );
		buttonPanel.add( addElemButton );

		showSubgraphButton.setBackground( charger.Global.chargerBlueColor );
		showSubgraphButton.setForeground( Color.white );
		showSubgraphButton.setSize( new Dimension( bigbuttonwidth, fillinButton.getSize().height ) );
		showSubgraphButton.setLocation( new Point( startx, starty + 1 * vspace ) );
		showSubgraphButton.setText( charger.Global.strs( "ShowSubgraphLabel" ) );
		ActionListener showSubgraphAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				if ( grid != null )
					craft.Craft.craftWindow.performShowSubgraph( grid.conceptOne, grid.relation, grid.conceptTwo );
			}
		};
		showSubgraphButton.addActionListener( showSubgraphAL );
		editMenuShowSubgraph.addActionListener( showSubgraphAL );
		showSubgraphButton.setVisible( true );
		buttonPanel.add( showSubgraphButton );
		
		addAttrButton.setBackground( charger.Global.chargerBlueColor );
		addAttrButton.setForeground( Color.white );
		addAttrButton.setSize( new Dimension( bigbuttonwidth, fillinButton.getSize().height ) );
		addAttrButton.setLocation( new Point( startx, starty + 2 * vspace ) );
		addAttrButton.setText( "Add Attribute" );
		ActionListener addAttrAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				performNewAttribute();
			}
		};
		addAttrButton.addActionListener( addAttrAL );
		editMenuAddAttr.addActionListener( addAttrAL );
		addAttrButton.setVisible( true );
		buttonPanel.add( addAttrButton );		// makes for a bad layout here


		fillinButton.setBackground( charger.Global.chargerBlueColor );
		fillinButton.setForeground( Color.white );
		fillinButton.setPreferredSize( new Dimension( 175, fillinButton.getSize().height ) );
		fillinButton.setMinimumSize( new Dimension( 175, fillinButton.getSize().height ) );
		fillinButton.setLocation( new Point( startx + hmargin + hspace * 3 / 2, starty + 0 * vspace ) );
		fillinButton.setText( fillinLabel );
		ActionListener fillinAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				if ( grid.gridComplete() )
					JOptionPane.showMessageDialog( null, "No blanks need to be filled in." );
				else
				{
					elicitor.fillInGridByQuery();
					repaint();
				}
			}
		};
		fillinButton.addActionListener( fillinAL );
		editMenuFillin.addActionListener( fillinAL );
		fillinButton.setVisible( true );
		buttonPanel.add( fillinButton );

		diadicButton.setBackground( charger.Global.chargerBlueColor );
		diadicButton.setForeground( Color.white );
		fillinButton.setPreferredSize( new Dimension( 225, fillinButton.getSize().height ) );
		fillinButton.setMinimumSize( new Dimension( 175, fillinButton.getSize().height ) );
		diadicButton.setLocation( new Point( startx + hmargin + hspace * 3 / 2, starty + 1 * vspace ) );
		diadicButton.setText( diadicLabel );
		ActionListener diadicAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				performDiadicElicitation();
			}
		};
		diadicButton.addActionListener( diadicAL );
		editMenuDiadic.addActionListener( diadicAL );
		diadicButton.setVisible( true );
		buttonPanel.add( diadicButton );

		resolveSimButton.setBackground( charger.Global.chargerBlueColor );
		resolveSimButton.setForeground( Color.white );
		resolveSimButton.setPreferredSize( new Dimension( 100, fillinButton.getSize().height ) );
		resolveSimButton.setLocation( new Point( startx + hmargin + 2 * hspace, starty + 2 * vspace ) );
		resolveSimButton.setText( resolveSimLabel );
		ActionListener resolveSimAL = new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
					performResolveSimilarities();
				}
			};
		resolveSimButton.addActionListener( resolveSimAL );
		editMenuResolveSim.addActionListener( resolveSimAL );
		resolveSimButton.setVisible( true );
		buttonPanel.add( resolveSimButton );
		
		getSenses.setBackground( charger.Global.chargerBlueColor );
		getSenses.setForeground( Color.white );
		//getSenses.setPreferredSize( new Dimension( 100, fillinButton.getSize().height ) );
		getSenses.setLocation( new Point( startx + hmargin + 3 * hspace + hmargin, starty + 2 * vspace ) );
		getSenses.setText( getSensesLabel );
		getSenses.setSize( (int)(getSenses.getWidth()*1.5), getSenses.getHeight() );
		ActionListener getSensesAL = new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
					performGetSenses( selectedRow, wnmgr );
			}
		};
		getSenses.addActionListener( getSensesAL );
		editMenuGetSenses.setAccelerator( 
				KeyStroke.getKeyStroke(KeyEvent.VK_A, charger.Global.AcceleratorKey | InputEvent.SHIFT_MASK ) );
		editMenuGetSenses.addActionListener( getSensesAL );
		getSenses.setVisible( true );
		
		editMenuMakeSpecializations.setAction( makeSpecializationsAction );
		editMenu.add( editMenuMakeSpecializations );
		makeSpecializationsButton.setBackground( charger.Global.chargerBlueColor );
		makeSpecializationsButton.setForeground( Color.white );
		//makeSpecializationsButton.setPreferredSize( new Dimension( 100, fillinButton.getSize().height ) );
		makeSpecializationsButton.setLocation( new Point( startx + hmargin + 3 * hspace + hmargin, starty + 0 * vspace ) );
		makeSpecializationsButton.setSize( (int)(makeSpecializationsButton.getWidth()*1.5), getSenses.getHeight() );
		buttonPanel.add( makeSpecializationsButton );
		
		if ( charger.Global.wordnetEnabled )
		{
			editMenuRemoveDescriptors.setAction( removeDescriptorsAction );
			editMenu.add( editMenuGetSenses );
			editMenu.add( editMenuRemoveDescriptors );
			buttonPanel.add( getSenses );
		}

		// next button .setLocation( new Point( startx + hmargin + 2 * hspace + hmargin, starty + 0 * vspace ) );

		buttonPanel.setVisible( true );
		buttonPanel.setOpaque( true );
		buttonPanel.setBackground( toolPanel.getBackground() );
		//buttonPanel.setBorder( BorderFactory.createRaisedBevelBorder() );


		toolPanel.add( buttonPanel );

		refreshToolPanel();
		
		toolPanel.setVisible( true );
	}
	
	/**
	
	 */
	public synchronized void refreshToolPanel()
	{
		if ( grid == null ) return;

		toolPanel.setPreferredSize( new Dimension( getWidth(), elicitorComponent.getSize().height + 75 ) );
		buttonPanel.setSize(
					new Dimension( getWidth() - elicitorComponent.getWidth(), elicitorComponent.getSize().height + 200 ) );
		buttonPanel.setLocation( new Point( elicitorComponent.getLocation().x + elicitorComponent.getWidth(),
									elicitorComponent.getLocation().y ) );

		/*int vspace = 35;
		int hspace = 120;
		int xstart = elicitorComponent.getSize().width + 20;
		int ystart = elicitorComponent.getLocation().y;

		addElemButton.setLocation( new Point( xstart + 0*hspace, ystart + 0*vspace ) );
		addAttrButton.setLocation( new Point( xstart + 0*hspace, ystart + 2*vspace ) );
		
		fillinButton.setLocation( new Point( xstart + 20 + addElemButton.getWidth() + 0*hspace, ystart + 1*vspace ) );
		diadicButton.setLocation( new Point( xstart + 20 + addElemButton.getWidth() + 1*hspace, ystart + 1*vspace ) );
		resolveSimButton.setLocation( new Point( xstart + 20 + addElemButton.getWidth() + 2*hspace, ystart + 1*vspace ) );
		*/
		if ( grid.attributeLabel != null && grid.elementLabel != null )
		{
			diadicButton.setVisible( true );
			editMenuDiadic.setEnabled( true );
			fillinButton.setVisible( true );
			editMenuFillin.setEnabled( true );
		}
		else
		{
			diadicButton.setVisible( false );
			editMenuDiadic.setEnabled( false );
			fillinButton.setVisible( false );
			editMenuFillin.setEnabled( false );
		}
		
		
		if ( grid.attributeLabel != null && ! grid.attributeLabel.equals( "" ) ) 
		{
			addAttrButton.setText( "Add " + grid.attributeLabel );
			editMenuAddAttr.setText( "Add " + grid.attributeLabel );

			addAttrButton.setPreferredSize( addAttrButton.getPreferredSize() );

			addAttrButton.setVisible( true );
			editMenuAddAttr.setEnabled( true );
		}
		else 
		{
			addAttrButton.setVisible( false );
			editMenuAddAttr.setEnabled( false );
		}
		
		if ( grid.elementLabel != null && ! grid.elementLabel.equals( "" ) ) 
		{
			addElemButton.setText( "Add " + grid.elementLabel );
			editMenuAddElem.setText( "Add " + grid.elementLabel );

			addElemButton.setPreferredSize( addElemButton.getPreferredSize() );
			
			addElemButton.setVisible( true );
			editMenuAddElem.setEnabled( true );
		}
		else
		{
			addElemButton.setVisible( false );
			editMenuAddElem.setEnabled( false );
		}
		
				// if at least two columns and one row, we could potentially differentiate the elements
		if ( (gridTable.getModel().getColumnCount() >= 2 ) &&  (gridTable.getModel().getRowCount() >= 1 ) )
		{
			resolveSimButton.setPreferredSize( resolveSimButton.getPreferredSize() );
			resolveSimButton.setVisible( true );
			editMenuResolveSim.setEnabled( true );
		}
		else
		{
			resolveSimButton.setVisible( false );
			editMenuResolveSim.setEnabled( false );
		}

		if ( selectedRow < 0 )
		{
			getSenses.setVisible( false );
			editMenuGetSenses.setEnabled( false );
		}
		else
		{
			getSenses.setVisible( true );
			editMenuGetSenses.setEnabled( true );
		}
		
		if ( grid != null && grid.conceptOne != null )
		{
			showSubgraphButton.setVisible( true );
			editMenuShowSubgraph.setEnabled( true );
		}
		else
		{
			showSubgraphButton.setVisible( false );
			editMenuShowSubgraph.setEnabled( false );
		}
		
		toolPanel.validate();
	}
	
	/**
		Provides access to the place this window will display whatever the elicitor wants to display.
		@return some component that this display window owns, but is managed by the elicitor.
		@see RGElicitor#getHeaderComponent
	 */
	public JComponent getElicitorComponent( )
	{
		elicitorComponent.setVisible( true );
		elicitorComponent.setSize( new Dimension( 300, 150 ) );
		return elicitorComponent;
	}	

	/**
		
		*/
	public void setElicitorComponent( JComponent comp )
	{
		JComponent c = getElicitorComponent();
		c.removeAll();
		c.add( comp );
		c.validate();
	}	

	/**
		Assumes that grid has been initialized.
		*/
	public void setupTable()
	{
		if ( grid.valuetype == null ) return;
		TableCellEditor editor = grid.valuetype.getEditor();
		if ( editor != null )
			gridTable.setDefaultEditor( grid.valuetype.getValueClass(), grid.valuetype.getEditor() );

		gridTable.getTableHeader().setBackground( Color.white );
		gridTable.getTableHeader().setOpaque( true );
		gridTable.setDefaultRenderer( Boolean.class, new CustomTableCellRenderer() );
		gridTable.setDefaultRenderer( Integer.class, new CustomTableCellRenderer() );
		gridTable.setGridColor( Color.blue );
		gridTable.setShowGrid( true );
		gridTable.getModel().addTableModelListener( this );
		
		gridTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//gridTable.setDefaultRenderer( Object.class, new CustomTableCellRenderer() );
		
		//if ( gridTable.getDefaultRenderer( Boolean.class ) == null )
		//	charger.Hub.warning( "table's default renderer for boolean is null" );
		rowHeader.setValueIsAdjusting( true );
		ListSelectionModel rowSM = rowHeader.getSelectionModel();
		rowSM.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		rowSM.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( ListSelectionEvent e ) {
				performAttributeSelected( e );
			}
		});

		MouseListener rowMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = rowHeader.locationToIndex(e.getPoint());
				if (e.getClickCount() == 2)
					performRowHeaderEdit( row );
				else
					performRowHeaderSelect( row );
			}
		};
		rowHeader.addMouseListener( rowMouseListener );

		MouseAdapter colMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 ) {
                    // charger.Global.info("Sorting ...");
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
					performColHeaderEdit( e );
                }
            }
        };
        JTableHeader th = gridTable.getTableHeader();
        th.addMouseListener( colMouseListener );
		
		refreshTable();
	}
	
	public void performDiadicElicitation()
	{
		if ( elicitor == null ) elicitor = new RGDiadicElicitor( grid, model );
		validate();
		elicitor.elicit();
		model.fireTableStructureChanged();
		validate();
		grid.summary();
		craft.Craft.say( elicitor.getTranscript().toString() );
		model.fireTableStructureChanged();
	}
	
	public void performResolveSimilarities()
	{
		elicitor = getElicitor( grid, model); 
		validate();
		elicitor.resolveSimilarities();
		model.fireTableStructureChanged();
		validate();
		//grid.summary();
		//		Craft.say( elicitor.getTranscript().toString() );
		model.fireTableStructureChanged();	
	}
	
	private void performRemoveDescriptors( int _row )
	{
		String prompt = null;
		if ( _row == -1 )
			prompt = "Remove all definitions in this repertory grid?";
		else
			prompt = "Remove all definitions for " + grid.attributeLabel + " \"" +
				determineAttribute( _row).getLabel() + "\"?";

		int d = JOptionPane.showConfirmDialog( this,
					prompt,
					"Remove descriptor(s)", 
					JOptionPane.YES_NO_OPTION );
		if ( d == JOptionPane.YES_OPTION )
		{
			if ( _row == -1 )
			{
				ArrayList as = grid.getAttributes();
				for ( int k = 0; k < as.size(); k++ )
				{
					( (TrackedAttribute)as.get( k ) ).removeAllTypeDescriptors();
				}
			}
			else
			{
				TrackedAttribute ta = determineAttribute( _row );
				if ( ta != null ) ta.removeAllTypeDescriptors();
			}
		}
		refreshDescriptorPanel();
	}

	/**
		Looks for the longest phrases possible. First look for already-chosen senses (longest phrase first)
			by checking all the other attributes in the grid to see if they have one defined.
		Then attempts finding  Wordnet synsets (longest phrase first), but if there aren't
			any, or if Wordnet isn't available, queries the user for the meaning.
		@param row The (0-based) row number for the table (not including the header row).
	*/
	public void performGetSenses( int row, charger.gloss.wn.WordnetManager wnmgr )
	{
		String phrase = (String)rowHeader.getModel().getElementAt( row );
		charger.gloss.AbstractTypeDescriptor[] ds = kb.ConceptManager.getSensesFromPhrase( phrase, wnmgr );
		if ( ds.length > 0 ) ((TrackedAttribute)determineAttribute( row )).removeAllTypeDescriptors();
		for ( int dnum = 0; dnum < ds.length; dnum++ )
			assignDescriptor( ds[ dnum ].getLabel(), ds[ dnum ], row );
		refreshDescriptorPanel();
	}


	private void assignDescriptor( String _term, charger.gloss.AbstractTypeDescriptor _descr, int _row )
	{
		TrackedAttribute a = determineAttribute( _row );
		setChanged( true );
		a.setTypeDescriptor( _term, _descr );
	}
	
	/**
		Finds the right attribute for this row. If there is already a tracked attribute in the row, 
			thenn just return it; otherwise, replace the existing attribute with a tracked one.
	*/
	private TrackedAttribute determineAttribute( int _row )
	{
		TrackedAttribute a = null;
				charger.Global.info( "attr is " + grid.getAttributes().get( _row ) );
		if ( ! (grid.getAttributes().get( _row ) instanceof TrackedAttribute ) )
		{
			a = new TrackedAttribute( (RGAttribute)grid.getAttributes().get( _row ) );
			grid.replaceAttribute( (RGAttribute)grid.getAttributes().get( _row ), a );
					//charger.Global.info( "should have just replaced attr with tracked one." );
		}
		else
			a = (TrackedAttribute)grid.getAttributes().get( _row );
		return a;
	}


	/**
		Action associated with the add attribute command 
	 */
	public void performNewAttribute()
	{
		elicitor = getElicitor( grid, model);
		elicitor.queryNewAttribute( "" );
		elicitor.fillInGridByQuery();
		refreshTablePanel();
	}

	public void performNewElement()
	{
		elicitor = getElicitor( grid, model);
		elicitor.queryNewElement( "" );
		elicitor.fillInGridByQuery();
		refreshTablePanel();
	}
	
	public void performAttributeSelected( ListSelectionEvent e )
	{
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		if (lsm.isSelectionEmpty())
		{
			// charger.Hub.warning("No rows are selected.");
		}
		else
		{
			selectedRow = lsm.getMinSelectionIndex();
				Craft.say( "row " + selectedRow + " is selected, contains " + rowHeader.getModel().getElementAt( selectedRow ) );
			performRowHeaderSelect( selectedRow );			
		}
				//Craft.say( "Intentional dump:" );
				//Thread.dumpStack();
	}

	/**
		Double-clicking on the row header starts a simple edit procedure.
	 */
	public void performColHeaderEdit( MouseEvent e )
	{
		TableColumnModel columnModel = gridTable.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		int column = gridTable.convertColumnIndexToModel(viewColumn);
		if ( column != -1 )
		{
						Craft.say("Double clicked on column " + column);
			String newlabel = elicitor.transcript.showInputDialog( this, "Rename " + grid.elementLabel,
						gridTable.getColumnName( column ) );
			if ( newlabel == null )
			{
				// user cancelled
			} 
			else if ( newlabel.equals( "" ) )
			{
				// delete element
				grid.deleteElement( (RGElement)(grid.getElements().get( column ) ) );
				elicitor.getTableModel().fireTableStructureChanged();
				setChanged( true );
				refreshTablePanel();
			}
			else if ( ((RGElement)(grid.getElements().get( column ))).getLabel().equals(
						gridTable.getColumnName( column ) ) )
			{
				((RGElement)grid.getElements().get( column )).setLabel( newlabel );
				elicitor.getTableModel().fireTableStructureChanged();
				setChanged( true );
				refreshTablePanel();
							grid.summary( "after column edit" );
			}
		}
	}

	/**
		Double-clicking on the row header starts a simple edit proceudre.
	 */
	private void performRowHeaderEdit( int selected )
	{
		selectedRow = selected;
		Craft.say("Double clicked on Item " + selectedRow);
		String newlabel = elicitor.transcript.showInputDialog( this, "Rename " + grid.attributeLabel,
						rowHeader.getModel().getElementAt( selectedRow ) );
		if ( newlabel != null &&
	   ((RGAttribute)(grid.getAttributes().get( selectedRow ))).getLabel().equals(
						rowHeader.getModel().getElementAt( selectedRow ) ) )
		{
			((RGAttribute)grid.getAttributes().get( selectedRow )).setLabel( newlabel );
			elicitor.getTableModel().fireTableStructureChanged();
			setChanged( true );
			refreshTablePanel();
		}
	}
	
	private void performRowHeaderSelect( int selected )
	{
		selectedRow = selected;
		Craft.say("Single clicked on Item " + selectedRow);
		refreshDescriptorPanel();
		refreshToolPanel();
	}

	private RGElicitor getElicitor( TrackedRepertoryGrid g, RGTableModel m )
	{
		if ( elicitor == null ) return new RGDiadicElicitor( grid, model );
		else return elicitor;
	}

	private void refreshDescriptorPanel()
	{
		if ( selectedRow < 0 )
		{
			validate();
			return;
		}
		JTable descriptorTable = null;
		if ( selectedRow > -1 && grid.getAttributes().get( selectedRow ) instanceof TrackedAttribute )
		{
			descriptorTable = charger.gloss.wn.WNUtil.getDescriptorTable(
				((TrackedAttribute)grid.getAttributes().get( selectedRow )).getTypeDescriptors() );
			descriptorPanel.setVisible( true );
		}
		else 
		{
			descriptorTable = charger.gloss.wn.WNUtil.getDescriptorTable( new charger.gloss.AbstractTypeDescriptor[0] );
			descriptorPanel.setVisible( false );
		}
		dsp.setViewportView( descriptorTable );
		descriptorTable.setGridColor( Color.gray );
		descriptorTable.setShowGrid( true );

		descriptorPanel.validate();
	}
	
	private void setupDescriptorPanel( )
	{
		int panelHeight = 125;
		descriptorPanel.setSize( new Dimension( tableWidth, panelHeight ) );
		descriptorPanel.setLayout( new BorderLayout() );
		descriptorPanel.setPreferredSize( new Dimension( tableWidth, panelHeight ) );
		descriptorPanel.setOpaque( true );
				// set up the scroll pane's characteristics
		dsp.setPreferredSize( new Dimension( tableWidth, panelHeight ) );
		dsp.setBackground( Color.white );
		dsp.getViewport().setBackground( craft.Craft.craftPink );
		//dsp.setLocation( new Point( 0, 0 ) );
		dsp.setOpaque( true );

		dsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		dsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		descriptorPanel.add( dsp );
		
		refreshDescriptorPanel();
		descriptorPanel.validate();
		descriptorPanel.setVisible( false );
	}

	
	public void refreshTablePanel()
	{
				//Craft.say( "value type is " + grid.valuetype );
				//Craft.say( "row header size is " + rowHeader.getModel().getSize() );
		if ( grid.valuetype != null ) valueLabel.setText( grid.valuetype.explainMe() );
		else valueLabel.setText( "No value type set" );
		gridname.setText( grid.getQuestionLabel() );
		refreshTable();
		refreshToolPanel();
		sp.setRowHeaderView( rowHeader );
		attributeCornerComponent.setText( grid.attributeLabel );
		tablePanel.validate();
	}
	
	/**
		Resets table size and columnn widths for the table.
	 */
	public void refreshTable()
	{
		TableColumn col = null;
		for ( int k = 0; k < gridTable.getColumnCount(); k++ )
		{
			col = gridTable.getColumnModel().getColumn( k );
			//col.setPreferredWidth( 50 );

			Component comp = gridTable.getTableHeader().getDefaultRenderer().
							getTableCellRendererComponent(
								gridTable, col.getHeaderValue(), 
								false, false, -1, k);
			int headerWidth = comp.getPreferredSize().width;
			col.setPreferredWidth(  Math.max( colWidth,  headerWidth ) );
			comp.setBackground( new Color( 255, 230, 230 ) );
		}
		
		//gridTable.doLayout();
		gridTable.validate();
	}
	
	/**
		Implements the TableModelListener interface. Tries to minimize re-drawing and refreshing.
	 */
	public void tableChanged( TableModelEvent e )
	{
				//Craft.say( "calling table changed with somethingChanged = " + somethingChanged );
		if ( ! somethingChanged ) return;
		//rowHeader = new JList( model.rowListModel );
				//Craft.say( "INTENTIONAL DUMP at table changed");
				//Thread.dumpStack();
		refreshTablePanel();
		//gridTable.doLayout();
		setChanged( true );
	}


	private void setupWindow()
	{
		setSize( new Dimension( 800, 600 ) );

		//getContentPane().setLayout( new FlowLayout() );
		
		setTitle( "Repertory Grid Worksheet" );
		getContentPane().setBackground( Color.white );

		holder.add( toolPanel );
		holder.add( tablePanel );
		holder.add( descriptorPanel );
		
		getContentPane().add( holder );
		
		setJMenuBar( menuBar );
		
		menuBar.add( fileMenu );
		fileMenuNew.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, charger.Global.AcceleratorKey ) );
		fileMenu.add( fileMenuNew );
		fileMenuOpen.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, charger.Global.AcceleratorKey ) );
		fileMenu.add( fileMenuOpen );
		fileMenuSave.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, charger.Global.AcceleratorKey ) );
		fileMenu.add( fileMenuSave );
		fileMenu.add( fileMenuSaveAs );
		fileMenu.add( fileMenuSaveTranscript );
		//fileMenu.add( fileMenuExport );
		fileMenu.add( exportOptions );
		fileMenuClose.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_W, charger.Global.AcceleratorKey ) );
		fileMenu.add( fileMenuPageSetup );
		fileMenu.add( fileMenuPrint );
		fileMenuPrint.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P, charger.Global.AcceleratorKey ) );
		fileMenu.add( fileMenuClose );
		fileMenuQuit.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_W, charger.Global.AcceleratorKey ) );
		//fileMenu.add( fileMenuQuit );		// no quit option yet
		fileMenu.add( CraftWindow.fileMenuPreferences );

		
		fileMenuNew.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				fileMenuNewActionPerformed();
			}
		} );

		fileMenuOpen.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				fileMenuOpenActionPerformed();
			}
		} );
		
		JMenuItem exportAsBurmeister = new JMenuItem( "Burmeister (CXT) Format" );
		exportOptions.add( exportAsBurmeister );
		exportAsBurmeister.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
                                if ( sourceAbsoluteFile == null )
                                {
                                        String name = getName() + craft.Craft.rgxmlSuffix;
                                        if ( sourceAbsoluteFile != null )
                                                name = sourceAbsoluteFile.getAbsolutePath();
                                }
				File f = new File( 
					charger.util.Util.stripFileExtension( sourceAbsoluteFile.getAbsolutePath() ) + ".cxt" );
				performActionExportAsBurmeister( grid, f);
			}
		} );
		
		fileMenuPageSetup.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				charger.Global.performActionPageSetup();
			}
		} );

		fileMenuPrint.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				performActionPrint();
			}
		} );

		fileMenuClose.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				thisWindowClosing();
			}
		} );

		fileMenuQuit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				thisWindowClosing();
			}
		} );
		
		fileMenuSave.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				fileMenuSaveActionPerformed( false );
			}
		} );

		fileMenuSaveAs.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				fileMenuSaveActionPerformed( true );
			}
		} );

		fileMenuSaveTranscript.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				fileMenuSaveTranscriptActionPerformed( true );
			}
		} );

		editMenu.add( editMenuFillin );
		editMenu.add( editMenuDiadic );
		editMenu.add( editMenuAddAttr );
		editMenu.add( editMenuAddElem );
		editMenu.add( editMenuResolveSim );
		editMenu.add( editMenuShowSubgraph );

		menuBar.add( editMenu );
		menuBar.add( windowMenu );

				

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    thisWindowClosing();
			}
			public void windowActivated(WindowEvent e) {
			    thisWindowActivated();
			}
			public void windowOpened(WindowEvent e) {
			    thisWindowOpened( e);
			}
			public void windowDeactivated(WindowEvent e) {
			    thisWindowDeactivated( e);
			}

		});
		
		/*addFocusListener( new FocusAdapter() {
			public void focusGained( FocusEvent e ) {
				thisFocusGained(e);
			}
			public void focusLost( FocusEvent e ) {
				thisFocusLost(e);
			}
		});
		*/
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//thisComponentResized(e);
				refreshToolPanel();
			}
		});
		
	}

	/**
		Checks whether graph in the edit frame has been saved. If not, then prompts user for
		whether to save it or not.  
		@return JOptionPane's CANCEL_OPTION, CLOSED_OPTION (NO) or YES_OPTION.
		@see JOptionPane
	 */
	public int performCheckSaved()
	{
		int filenameSubstringLength = 27;
			 Craft.say( "at perform checksaved; anything changed is " + somethingChanged );
		if ( ! somethingChanged ) return JOptionPane.NO_OPTION;
		requestFocus();
		if ( sourceAbsoluteFile == null )
			sourceAbsoluteFile = new File ( charger.Global.GraphFolderFile.getAbsolutePath(), 
					getName() + craft.Craft.rgxmlSuffix );
		String displayableFilename =  sourceAbsoluteFile.getAbsolutePath();

		Object[] possibleValues = { charger.Global.strs( "SaveLabel" ), 
					charger.Global.strs( "DontSaveLabel" ), 
					charger.Global.strs( "CancelLabel" ) };
		int answer = JOptionPane.showOptionDialog(this, 
				displayableFilename + "\nnot saved.\n\nDo you want to save it?",
				"File Not Saved", 
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null,
				possibleValues,  charger.Global.strs( "SaveLabel" ));
		
		if ( answer == JOptionPane.CANCEL_OPTION ) return answer; // @bug -- cancel acts like NO
		else if ( answer == JOptionPane.CLOSED_OPTION ) 
		{
			//setChanged( false );
			return JOptionPane.CANCEL_OPTION;
		}
		else if ( answer == JOptionPane.YES_OPTION ) {
			if ( sourceAbsoluteFile.getName().startsWith( "Untitled" ) )
				fileMenuSaveActionPerformed( true );
			else
				fileMenuSaveActionPerformed( false );
			setChanged( false );
		}
		//return JOptionPane.YES_OPTION;
		return answer;
     }


	/**
		Close the window, after first checking whether to save the existing grid.
		If the user chooses to cancel, then don't finish closing the window.
	 */
	public void thisWindowClosing( ) 
	{
		//if ( somethingChanged )
		//{
			if ( performCheckSaved() == JOptionPane.CANCEL_OPTION ) 
			{
				Craft.say( "check saved equals cancel" );
				setVisible( true );
				setEnabled( true );
				repaint();
				return;
			}
		//}
				// actually get rid of this window
		Craft.removeRGWindow( this );
		WindowManager.forgetWindow( this );
		Global.knowledgeManager.forgetKnowledgeSource( grid );
				// dump the transcript
			Craft.say( elicitor.getTranscript().toString() );
		setVisible( false );
		dispose();
	}

	public void thisWindowActivated() 
	{
		charger.Global.refreshWindowMenuList( windowMenu, this ); 
	}
	
	public void thisWindowOpened( WindowEvent e )
	{
	}

	public void thisWindowDeactivated(WindowEvent e) 
	{
	}

	public void thisFocusGained( FocusEvent e)
	{

	}
	
	public void thisFocusLost( FocusEvent e)
	{

	}
		
	public class CustomTableCellRenderer extends DefaultTableCellRenderer
	{		
		public Component getTableCellRendererComponent( JTable   table,
                                    Object   value,
                                    boolean  isSelected,
									boolean	 hasFocus,
                                    int      row, 
                                    int      column)
		{
			Component   result   =
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
													row, column);
				RGCell c = grid.getCell( row, column );
				result.setForeground( Color.black );
				//((JComponent)result).setBorder(  BorderFactory.createLineBorder( Color.blue) );
				((JLabel)result).setHorizontalAlignment( SwingConstants.CENTER );
				if ( ! c.hasValue() ) 
				{
					setValue( "" );
					result.setBackground( new Color( 175, 175, 255 ) );
				}
				else
				{
					if ( 2*(row/2) == row ) result.setBackground( new Color( 225, 225, 255 ) );
					else result.setBackground( Color.white );
					if ( value instanceof Boolean )
					{
						if ( ((Boolean)value).booleanValue() )
							//setValue( "+" );
							setValue( "YES" );
						else
							//setValue( "-" );
							setValue( "no" );
					}
				}
			return (result);
		}
		
   }
   
   /**
		Used for rendering the attributes in a list on the left-hand side of the grid.
    */
   	public class CustomListCellRenderer extends DefaultListCellRenderer
	{		
		public Component getListCellRendererComponent( JList   list,
                                    Object   value,
									int	index,
                                    boolean  isSelected,
									boolean	 cellHasFocus)
		{
			Component   result   =
				super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
			result.setForeground( Color.black );
			//((JComponent)result).setBorder(  BorderFactory.createLineBorder( Color.blue) );
			((JLabel)result).setHorizontalAlignment( SwingConstants.RIGHT );
			if ( 2*(index/2) == index ) result.setBackground( new Color( 225, 225, 255 ) );
			else result.setBackground( Color.white ); 
			return (result);
		}
		
   }
   
	public static void fileMenuNewActionPerformed()
	{
		Craft.createNewRGWindow();
	}
	
	/**
		Save a repertory grid into a file in XML form.
		@param saveAs whether to prompt the user for a file name or not
	 */
	public void fileMenuSaveActionPerformed( boolean saveAs )
	{
		if ( saveAs || sourceAbsoluteFile == null )
		{
			String name = getName() + craft.Craft.rgxmlSuffix;
			if ( sourceAbsoluteFile != null )
				name = sourceAbsoluteFile.getAbsolutePath();
			File outfile = charger.util.Util.chooseOutputFile( "Save repertory grid file", name );
						craft.Craft.say( "outfile is " + outfile );
			if ( outfile == null ) 
				return;
			else 
				sourceAbsoluteFile = outfile;
		}
		try 
		{
			BufferedWriter out  = 
				new BufferedWriter( new OutputStreamWriter( new FileOutputStream ( sourceAbsoluteFile ) ));
			if (out == null) {
				charger.Global.error( "can't open " + sourceAbsoluteFile.getAbsolutePath() );
			} else 
			{
				out.write( repgrid.xml.RGXMLGenerator.XML( grid ) );
				out.close();
				setChanged( false );
				setFile( sourceAbsoluteFile );
				valueLabel.setText( "Grid saved to " + sourceAbsoluteFile.getAbsolutePath() );
			}
		} catch ( IOException ee) { 
			charger.Global.error( "IO Exception: " + ee.getMessage() );
		}
	
	}

	/**
		Save the current transcript as text.
		@param saveAs whether to prompt the user for a file name or not
	 */
	public void fileMenuSaveTranscriptActionPerformed( boolean saveAs )
	{
		File outfile = null;
		if ( saveAs || sourceAbsoluteFile == null )
		{
			String name = getName() + ".txt";
			if ( sourceAbsoluteFile != null )
			{
				outfile = new File( sourceAbsoluteFile.getParent(), "Transcript.txt" );
			}
			outfile = charger.util.Util.chooseOutputFile( "Save repertory grid file", outfile.getAbsolutePath() );
						craft.Craft.say( "outfile is " + outfile );
			if ( outfile == null ) 
				return;
		}
		try 
		{
			BufferedWriter out  = 
				new BufferedWriter( new OutputStreamWriter( new FileOutputStream ( outfile ) ));
			if (out == null) {
					charger.Global.error( "can't open " + outfile.getAbsolutePath() );
			} else 
			{
				out.write( elicitor.getTranscript().toString() );
				out.close();
			}
		} catch ( IOException ee) { 
			charger.Global.error( "IO Exception: " + ee.getMessage() );
		}
	
	}


	public static void fileMenuOpenActionPerformed()
	{
		String filename = openRepertoryGrid( null );
	}
	
	   /**
	Opens a new graph in its own editframe.
	@param filename the file from which to open a graph; null if a dialog is to be invoked.
		Called from various open menus, buttons and routines.
		Has two parts: getting the graph from the file and then setting up the editing window.
		Not responsible for focus, etc.
	@return the (short) filename of the file actually opened, generally only useful when passed "null" to
	    let the invoker know what file the user chose. Returns null if the user cancelled a dialog or there 
	    was any kind of error.
	
	Should return a full File descriptor so that we can use it all over the place
    */
    public synchronized static String openRepertoryGrid( String filename )
    {
		RGDisplayWindow newWindow = null;
		File infile = charger.util.Util.chooseInputFile( "Open existing repertory grid file",  
					filename, Global.CRAFTGridFolderFile, RGXMLFileFilter );
							craft.Craft.say( "infile is " + infile );
		if ( infile == null )
		{
			return null;
		}
		repgrid.xml.RGXMLParser parser = new repgrid.xml.RGXMLParser();
		try 
		{
			InputStreamReader instream  = new InputStreamReader( new FileInputStream ( infile ));
			if (instream == null) {
				charger.Global.error( "can't open file \"" + infile.getAbsolutePath() + "\"" );
				return null;
			} 
			else
			{
				TrackedRepertoryGrid newgrid = parser.parseXMLRepertoryGridFile( instream, infile );
				instream.close();
				//newgrid.summary( "\nNewly-parsed repertory grid is " );
				newWindow = new RGDisplayWindow( newgrid, null );
				newWindow.setFile( infile );
			}
		} catch ( IOException ee) { 
			charger.Global.error( "IO Exception: " + ee.getMessage() );
			return  null;
		}
		if ( newWindow != null ) 
		{
			newWindow.bringToFront();
		}
		return infile.getAbsolutePath();

	}

	/**
		Checks for strings ending in rgxmlSuffix, but not starting with ".".
		Used in <b>accept</b> methods required in various file filters.
		@return true if the file meets the criteria; false otherwise.
	 */
	public static boolean acceptRGFilename( File f )
	{
		if ( f.isDirectory() ) return true;
	    if ( f.getName().startsWith( "." ) ) return false;
		if ( f.getName().toLowerCase().endsWith( craft.Craft.rgxmlSuffix ) ) return true;
		else return false;
	}

   	/** Part of the ManagedWindow interface 
	@see charger.util.ManagedWindow
	*/
	public void bringToFront()
	{
		charger.util.WindowManager.bringToFront( this );
	}

	/** Part of the ManagedWindow interface 
	@see charger.util.ManagedWindow
	*/
	public String getMenuItemLabel()
	{
		return getTitle();
	}

	public String getFilename()
	{
		if ( sourceAbsoluteFile != null )
			return sourceAbsoluteFile.getAbsolutePath();
		else
			return "";
	}


	protected void performActionExportAsBurmeister( TrackedRepertoryGrid grid, File f )
	{
		String burmeister = grid.toBurmeisterCXTString( f.getAbsolutePath() );
		File outfile = Util.chooseOutputFile( "Export to Burmeister (CXT) file", f.getAbsolutePath() );
		FileOutputStream fos = null;
		BufferedWriter out = null;
		try {
			fos = new FileOutputStream( f );
			out   = new BufferedWriter(new OutputStreamWriter( fos ));
			if (out != null) {
				out.write( burmeister );
				out.close();
			}
		}
		catch (IOException ee)
		{
			JOptionPane.showMessageDialog( this,
							f.getAbsolutePath() + ": " + ee.getMessage(),
							"File error on save", JOptionPane.ERROR_MESSAGE );
		};
				//Craft.say( "output file is " + outfile.getAbsolutePath() );
				//Craft.say( "burmeister form is:\n" + burmeister );
	}


		public int print2(Graphics g, PageFormat pf, int pi)
							throws PrinterException 
		{
		
			JPanel printingPanel = new JPanel(); //setupPrintingPanel();
			
			Paper p = pf.getPaper();
			Point graphImageableOffset = new Point( 
						(int) p.getImageableX() + 0, 
						(int) p.getImageableY() + 0 );
			g.translate( graphImageableOffset.x,  graphImageableOffset.y );
					//Craft.say( "clip rect is " + g.getClipBounds().toString() );

			double scaleFactor = 1.00;	// assume we don't need to scale the printed picture
			int widthUnitsNeeded = printingPanel.getWidth();
			int heightUnitsNeeded = printingPanel.getHeight();
			
						// if landscape, then swap width and height
			if ( pf.getOrientation() == PageFormat.LANDSCAPE ) 
			{
				int temp = widthUnitsNeeded;
				widthUnitsNeeded = heightUnitsNeeded;
				heightUnitsNeeded = temp;
			}
						// if height pixels won't fit, then scale to the height
			if ( p.getImageableHeight() < heightUnitsNeeded)
				scaleFactor =  p.getImageableHeight() / heightUnitsNeeded;
						// if width pixels won't fit (whether scaled or not) then scale the width
			if ( p.getImageableWidth()  < (widthUnitsNeeded * scaleFactor) )
				scaleFactor =  p.getImageableWidth() / widthUnitsNeeded;
							Craft.say( "scale factor for printing is: " + scaleFactor );
			if (pi >= 1) 
			{
				return Printable.NO_SUCH_PAGE;
			}
			printingPanel.paint( g);
			return Printable.PAGE_EXISTS;
		}
	
	   /**
   	Queues a graphic representation of the repertory grid to a printer.
        @see RGDisplayWindow#print  
    */
    public void performActionPrint() {
                    // initial setup of print job
				Craft.say( "at perform action print" );
		setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        PrinterJob pjob = PrinterJob.getPrinterJob( );
        pjob.setJobName( "Dummy name" );
                // if page setup hasn't been called, make it so
       if ( charger.Global.pformat == null )
        {
            charger.Global.performActionPageSetup();
        }
        boolean ok = pjob.printDialog();
        if (ok) {
            pjob.setPrintable( this, charger.Global.pformat );	// renders graph on page
            try {
                pjob.print();	// queues raster image to printer
            } catch ( PrinterException pe ) { 
				JOptionPane.showMessageDialog( 
						this, "Printer error: " + pe.getMessage(), "Printer Error", 
						JOptionPane.ERROR_MESSAGE );
            }
        }
		setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
   }
   
   public void performMakeSpecializations()
   {
		craft.Craft.say( "At Make Specializations" );
   }
   
   /**
	Prints the row header, the column headers and the actual table, scaling the width as
	necessary and breaking into multiple pages as needed.
	Code borrowed from the Internet.
    */
	public int print(Graphics g, PageFormat pageFormat,
										int pageIndex) throws PrinterException
	{
			Graphics2D  g2 = (Graphics2D) g;
			g2.setColor(Color.black);


			double pageWidth = pageFormat.getImageableWidth();
			double tableWidth = (double)rowHeader.getWidth() +
					(double) gridTable.getColumnModel().getTotalColumnWidth();
			double scale = 1;
			if (tableWidth >= pageWidth) {
					scale =  pageWidth / tableWidth;
			}
						Craft.say( "scale factor is " + scale );
					/* The strategy is to draw everything on the graphics context, assuming
						that it's the larger size with everything un-scaled, then afterward
						scale everything to fit.
					*/

					// prepare the footer, whether printing or not
			String footer = sourceAbsoluteFile.getAbsolutePath() + " - Page " + (pageIndex + 1);
					// scale the font size upward so that the possibly scaled result is the right size
			g2.setFont( g2.getFont().deriveFont( (float)g2.getFont().getSize() / (float)scale ) );
			int swidth = g2.getFontMetrics().stringWidth( footer );
			int fontHeight = g2.getFontMetrics().getHeight();
			int fontDescent = g2.getFontMetrics().getDescent();

					//leave room for page number
			double pageHeight  =  pageFormat.getImageableHeight()-fontHeight;
			
			double logicalPageHeight = pageHeight / scale;
			double logicalPageWidth = tableWidth;
			if ( scale == 1.0 ) logicalPageWidth = pageWidth;
			
			double xtranslate = 0f;
			double ytranslate = 0f;

				// use tableWidth as the page width, draw everything, and then scale
			//JLabel sentenceLabel = new JLabel( gridname.getText() );
			JTextField sentenceLabel = new JTextField( gridname.getText() );
			sentenceLabel.setFont( gridname.getFont() );
			sentenceLabel.setSize( (int)logicalPageWidth, gridname.getHeight() );
			sentenceLabel.setHorizontalAlignment( SwingConstants.CENTER );

			double headerHeightOnPage = (sentenceLabel.getHeight() +
							gridTable.getTableHeader().getHeight() + 4 );
			double tableWidthOnPage = tableWidth;

			double oneRowHeight = (gridTable.getRowHeight()+
											gridTable.getRowMargin());
			int numRowsOnAPage =  (int)((pageHeight-headerHeightOnPage) / oneRowHeight);
			double pageHeightForTable = oneRowHeight*numRowsOnAPage;
			int totalNumPages =  (int)Math.ceil(( (double)gridTable.getRowCount())/numRowsOnAPage);
			if (pageIndex >= totalNumPages) {
					return NO_SUCH_PAGE;
			}

					// go to the top left corner of the logical page
			g2.scale(scale,scale);
			g2.translate( pageFormat.getImageableX() / scale , pageFormat.getImageableY() / scale );

			/*g2.draw( new Rectangle2D.Double( 
					pageFormat.getImageableX()/scale, 
					pageFormat.getImageableY()/scale, 
					logicalPageWidth, logicalPageHeight ) );*/

					// draw the sentence part of the grid
			//g2.translate(0f, headerHeightOnPage);
			//TODO this next line treats the last page as a full page
			//g2.setClip(0, (int)(pageHeightForTable*pageIndex),
			//		(int) Math.ceil(tableWidthOnPage)*2, (int) Math.ceil(pageHeightForTable));

			sentenceLabel.paint(g2);
			
			
					// draw the attribute type label (from scroll pane)
			JComponent corner = (JComponent)sp.getCorner( JScrollPane.UPPER_LEFT_CORNER );
			Border cornerborder = corner.getBorder();
			corner.setBorder( null );
			xtranslate = 0f;
			ytranslate = sentenceLabel.getHeight(); 
			g2.translate( xtranslate, ytranslate );
			//g2.setClip( 0, 0,(int) Math.ceil(tableWidth) * 2,
			//		(int)Math.ceil(headerHeightOnPage));
			corner.paint(g2);
			corner.setBorder( cornerborder );
			g2.translate( -1 * xtranslate, -1 * ytranslate );


					// draw the list of row labels
					//Border rowheaderborder = rowHeader.getBorder();
					//rowHeader.setBorder( null );
			xtranslate = -3f;
			ytranslate = headerHeightOnPage;
			g2.translate( xtranslate, ytranslate );
			//g2.translate(0f,-pageIndex*pageHeightForTable);
			//TODO this next line treats the last page as a full page
			//g2.setClip(0, (int)(pageHeightForTable*pageIndex),
			//		(int) Math.ceil(tableWidthOnPage), (int) Math.ceil(pageHeightForTable));
			rowHeader.clearSelection();
			rowHeader.paint(g2);
			g2.translate( -1 * xtranslate, -1 * ytranslate );
					//rowHeader.setBorder( rowheaderborder );


					// draw the table itself, without its headers
			xtranslate = corner.getWidth();
			ytranslate = headerHeightOnPage;
			//g2.translate(0f,-pageIndex*pageHeightForTable);
			g2.translate( xtranslate, ytranslate );
			//g2.setClip( 0, (int)(pageHeightForTable*pageIndex),
			//		(int) Math.ceil(tableWidthOnPage), (int) Math.ceil(pageHeightForTable));
			//g2.setClip( 0, (int)(pageHeightForTable*pageIndex),
			//		(int) Math.ceil(tableWidth)*2, (int) Math.ceil( pageHeightForTable ));

			gridTable.paint( g2 );
			g2.translate( -1 * xtranslate, -1 * ytranslate );

					// draw the element labels (i.e., the table column headers)
			
			//g2.translate( 0f, pageIndex*pageHeightForTable);
			//g2.translate( 0f, -headerHeightOnPage);
			//g2.setClip( 0, 0,(int) Math.ceil(tableWidthOnPage) * 2,
			//		(int)Math.ceil(headerHeightOnPage));
			xtranslate = corner.getWidth();
			ytranslate = headerHeightOnPage - corner.getHeight();
			g2.translate( xtranslate, ytranslate );
			gridTable.getTableHeader().paint(g2);
			g2.translate( -1 * xtranslate, -1 * ytranslate );

					// actually draw the footer
			if ( charger.Global.showFooterOnPrint )
				g2.drawString( footer, (int)logicalPageWidth/2 - swidth/2,
						(int)(logicalPageHeight+fontHeight-fontDescent));      //bottom center

			return Printable.PAGE_EXISTS;
		}


}
