//
//  RGElicitor.java
//  CharGer 2003
//
//  Created by Harry Delugach on Wed Apr 30 2003.
//

package repgrid;
import repgrid.tracks.*;

import charger.util.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
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
	Abstraction for all repertory grid elicitation classes.
 */
abstract public class RGElicitor {

		/** The grid for which this elicitor will acquire entries. */
	protected TrackedRepertoryGrid grid = null;
		/** The table model for the grid, in case we're displaying it */
	protected RGTableModel model = null;

	JLabel questionLabel = new JLabel();
	
	Transcript transcript = new Transcript();	// a dummy one 
	

	JTextField elemField = new JTextField( );
	JLabel elemLabel = new JLabel( "Concept 1 type " );

	JTextField relField = new JTextField(  );
	JLabel relLabel = new JLabel( "Relation " );

	JTextField attrField = new JTextField(  );
	JLabel attrLabel = new JLabel( "Concept 2 type " );
	
	JComboBox valuetypeMenu = new JComboBox();
	String notypechosen = "...choose type...";
	String booleanchosen = "YES / no";
	String range16chosen = "1 to 6 scaled";
	JLabel valuetypeLabel = new JLabel( "Use" );
	
	protected JPanel headerPanel = null;
	protected RGDisplayWindow window = null;

	// Initial exploration of WORDNET stuff
	protected net.didion.jwnl.dictionary.Dictionary wnDict = null;

		

    class MyDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
				//refreshheaderPanel( );
				commitChanges(e);
				//window.setupTablePanel();
        }
        public void removeUpdate(DocumentEvent e) {
				//refreshheaderPanel( );
				commitChanges(e);
				//window.setupTablePanel();
        }
        public void changedUpdate(DocumentEvent e) {
				//refreshheaderPanel( );
				commitChanges(e);
				//window.setupTablePanel();
        }
		
		private void commitChanges( DocumentEvent e)
		{
			if ( e.getDocument().getProperty( "name" ).equals( "elemField" ) )
				grid.elementLabel = elemField.getText();
			if ( e.getDocument().getProperty( "name" ).equals( "relField" ) )
				grid.relationLabel = relField.getText();
			if ( e.getDocument().getProperty( "name" ).equals( "attrField" ) )
				grid.attributeLabel = attrField.getText();
			window.setupTablePanel();
			window.refreshTablePanel();
			/*window.validate();
				 craft.Craft.say( "after commit changes:" );
			try {
			craft.Craft.say( "source of change is " + e.getDocument().getProperty( "name" ) + ": ("
				+ e.getDocument().getLength() + " chars): " +
				e.getDocument().getText( e.getDocument().getStartPosition().getOffset(), e.getDocument().getLength() ) );
			} catch ( BadLocationException ee ) {}
			grid.summary();
			*/
			headerPanel.validate();
			if ( window != null ) window.setChanged( true );
		}
    }
	MyDocumentListener docListener = new MyDocumentListener();
	
	/**
		General constructor for any elicitor. 
		@param rg The grid to be elicited.
		@param m the table model for this grid; <code>null</code> if there isn't any
	 */
	public RGElicitor( TrackedRepertoryGrid rg, RGTableModel m )
	{
		grid = rg;
		model = m;

		valuetypeMenu.setEnabled( false );
		valuetypeMenu.addItem( notypechosen );
		valuetypeMenu.addItem( booleanchosen );
		valuetypeMenu.addItem( range16chosen);
		valuetypeMenu.setBackground( Color.white );
		valuetypeMenu.getEditor().getEditorComponent().setBackground( Color.white );


		//fillInHeaders( );
	}
	
	/**
		Accesses the transcript associated with this grid.
		An elicitor keeps a log of what it has done, both its queries to the user and the user's responses.
		This method allows clients to enter additional information in the session log.
		@return a transcript instance
		@see charger.util.Transcript
	 */
	public Transcript getTranscript() { return transcript; }
	
	public RGTableModel getTableModel()
	{
		return model;
	}

	/**
		Constructs a header component for some display window. 
		@param w lets this elicitor know what window is controlling the component it is handing out.
		@return a header component which is managed by this elicitor instance.
	 */
	public JComponent getHeaderComponent( RGDisplayWindow w )
	{
		window = w;
		initializeHeaderPanel();
		refreshHeaderPanel();
		return headerPanel;
	}
	
	private void fillInHeaders()
	{
		{
			initializeHeaderPanel( );
			refreshHeaderPanel( );
			headerPanel.repaint();
		}
	}	
	
	/**
		Perform whatever elicitation is called for by this technique, on the grid with which it 
			was instantiated.
		Overridden in sub-classes to implement particular elicitation strategies.
	 */
	abstract public void elicit();
	
	/**
		Looks for columns with identical values and queries for additional attributes that
			can differentiate them.
		If the grid is not complete, first calls <code>fillInGridByQuery</code> to complete it.
	 */
	public void resolveSimilarities()
	{
		if ( ! grid.gridComplete() ) fillInGridByQuery();
		ArrayList elems = grid.getElements();
		boolean allDifferent = true;
				// for each element, compare it with every other element
		for ( int e1num = 0; e1num < elems.size(); e1num++ )
		{
			RGElement e1 = (RGElement)elems.get( e1num );
			for ( int e2num = e1num + 1; e2num < elems.size(); e2num++ )
						// iterate through all the rest of the elements
			{
				RGElement e2 = (RGElement)elems.get( e2num );
				boolean alike = true;
							// compare every attribute's cell value for the two elements
				Iterator iterAtts = grid.getAttributes().iterator();
				while ( iterAtts.hasNext() )
				{
					RGAttribute a = (RGAttribute)iterAtts.next();
					RGCell c1 = grid.getCell( a, e1 );
					RGCell c2 = grid.getCell( a, e2 );
					if ( ! c1.equals( c2 ) ) alike = false;
				}
				if ( alike ) 
				{
					allDifferent = false;
					craft.Craft.say( grid.elementLabel + " \"" + e1.getLabel() + 
						"\" and " + grid.elementLabel + " \"" + e2.getLabel() + " are alike." );
					queryNewAttribute( " where " + grid.elementLabel + " \"" + e1.getLabel() + 
						"\" and " + grid.elementLabel + " \"" + e2.getLabel() + "\" are different" );
				}
			}
		}
		if ( allDifferent )
			JOptionPane.showMessageDialog( window, "Each " + grid.elementLabel + " is different." );
	}
	
	protected void refreshHeaderPanel( )
	{
			//grid.summary( "refresh header panel");
		elemField.setText( grid.elementLabel );
		relField.setText( grid.relationLabel );
		attrField.setText( grid.attributeLabel );
		if ( grid.valuetype != null )
		{
			valuetypeMenu.setEnabled( false );
			if ( grid.valuetype instanceof RGBooleanValue ) valuetypeMenu.setSelectedItem( booleanchosen );
			else if ( grid.valuetype instanceof RGIntegerValueRange ) valuetypeMenu.setSelectedItem( range16chosen );
			valuetypeMenu.setEnabled( true );
		}
	}
	
	private void initializeHeaderPanel()
	{
		headerPanel = new JPanel();
		headerPanel.setOpaque( true );
		headerPanel.setBackground( craft.Craft.craftPink );
		headerPanel.setFont( new Font( "SansSerif", Font.PLAIN, 10 ) );
		elemLabel.setHorizontalAlignment( SwingConstants.RIGHT );

        elemField.getDocument().addDocumentListener( docListener );
        elemField.getDocument().putProperty( "name", "elemField" );
		elemField.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				grid.elementLabel = elemField.getText();
				refreshHeaderPanel( );
				window.refreshTable();
			}
		});

		relLabel.setHorizontalAlignment( SwingConstants.RIGHT );
        relField.getDocument().addDocumentListener( docListener );
        relField.getDocument().putProperty( "name", "relField" );
		relField.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				grid.relationLabel = relField.getText();
				refreshHeaderPanel( );
				window.refreshTable();
			}
		});
		
		attrLabel.setHorizontalAlignment( SwingConstants.RIGHT );
        attrField.getDocument().addDocumentListener( docListener );
        attrField.getDocument().putProperty( "name", "attrField" );
		attrField.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				grid.attributeLabel = attrField.getText();
				refreshHeaderPanel( );
				window.refreshTable();
			}
		});
		
		valuetypeLabel.setHorizontalAlignment( SwingConstants.RIGHT );
		valuetypeMenu.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if ( ! valuetypeMenu.isEnabled() ) return;
				String s = (String)valuetypeMenu.getSelectedItem();
				if ( s == null || s.equals( notypechosen) ) return;
				else if ( s.equals( range16chosen) ) 
					grid.setValueType( new RGIntegerValueRange( 6 ) );
				else if ( s.equals( booleanchosen ) )
					grid.setValueType( new RGBooleanValue() );
				if ( window != null ) window.refreshTable();
				refreshHeaderPanel( );
				grid.summary();
			}
		});
		
		headerPanel.setLayout( new GridLayout(4, 2) );
		headerPanel.add( elemLabel );
		headerPanel.add( elemField );
		headerPanel.add( relLabel );
		headerPanel.add( relField );
		headerPanel.add( attrLabel );
		headerPanel.add( attrField );
		headerPanel.add( valuetypeLabel );
		headerPanel.add( valuetypeMenu );
		valuetypeMenu.setEnabled( true );
		
		headerPanel.setSize( new Dimension( 280, 100 ) );
		headerPanel.setVisible( true );
	}
	
	/**
		Queries the user for a new attribute in the repertory grid. If it's a duplicate, a warning dialog appears. 
			If it's valid, then it is added to the repertory grid.
		@param s hopefully helpful string appended to the prompt.
		@return the new attribute's label; <code>null</code> if user cancelled or it's a duplicate.
	 */
	public String queryNewAttribute( String s )
	{
		String newAttr = transcript.showInputDialog( window, 
			"Enter " + Util.a_or_an( grid.attributeLabel ) + s + ":" );
		if ( newAttr != null ) 
		{
			if ( grid.getAttribute( newAttr ) != null ) 
			{
				transcript.showMessageDialog( null, 
					grid.attributeLabel + " " + newAttr + " is already in the table.", "Error",  JOptionPane.ERROR_MESSAGE );
				return null;	// already in the grid
			}
			grid.addAttribute( newAttr );
			model.fireTableStructureChanged();
			window.setChanged( true );
		}
		return newAttr;
	}
	
	/**
		Queries the user for a new element in the repertory grid. If it's a duplicate, a warning dialog appears. 
			If it's valid, then it is added to the repertory grid.
		@param s hopefully helpful string appended to the prompt.
		@return the new element's label; <code>null</code> if user cancelled or it's a duplicate.
	 */
	public String queryNewElement( String s )
	{
		String newElem = transcript.showInputDialog( window, 
			"Enter example of " + Util.a_or_an( grid.elementLabel ) + s + ":" );
		if ( newElem != null ) 
		{
			if ( grid.getElement( newElem ) != null ) 
			{
				transcript.showMessageDialog( null, 
					grid.elementLabel + " " + newElem + " is already in the table.", "Error",  JOptionPane.ERROR_MESSAGE );
				return null;	// already in the grid
			}
			grid.add( newElem );
			if ( model != null ) model.fireTableStructureChanged();
			if ( window != null ) window.setChanged( true );
		}
		return newElem;
	}
	
	/**
		Queries a user to fill in values for the grid.
		@see RGValue#queryCellValue
	 */
	public boolean fillInGridByQuery()
	{
		Iterator iter = grid.getCells().iterator();
		boolean okay = true;
		boolean anythingDone = false;
		while ( iter.hasNext() && okay )
		{
			RGCell c = (RGCell)iter.next();
			if ( ! c.hasValue() )
			{
				//String prompt = new String( grid.elementLabel + " \"" + c.getElement().getLabel()
				//	+ "\" " + grid.relationLabel + " "
				//	+ c.getAttribute().getLabel()  );
						// NEED to use some kind of sentence construction here
						// problem is: we need the Graph Objects to invoke makeSentence
				String prompt = "Is " + grid.attributeLabel + " \"" + c.getAttribute().getLabel() + 
                            "\" " + grid.relationLabel 
				 + " of " + grid.elementLabel + " \"" + c.getElement().getLabel() + "\"?"; 
				okay = c.getRGValue().queryCellValue( prompt );
				transcript.appendTell( prompt );
				if ( okay )
				{	
					anythingDone = true;
					craft.Craft.say( "after fillingridbyquery" );
					if ( window != null ) window.setChanged( true );
					transcript.appendUser( c.getRGValue().toString() );
				}
				else transcript.appendUser( " <stop>" );
			}
		}
		//if ( ! anythingDone )
		//	transcript.showMessageDialog( null, "Current questions are already answered" );
		return okay;
	}

	
}
