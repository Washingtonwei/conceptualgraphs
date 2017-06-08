//
//  QuantityBar.java
//  CharGer 2003
//
//  Created by Harry Delugach on Mon Jan 06 2003.
//

package charger.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

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
	Displays a quantity bar, with upper and lower bounds and the quantity itself labeled.
 */
public class QuantityBar extends JFrame implements ActionListener
{

	protected static Point nextLocation = new Point( 0, 0 );
	private Font defaultFont = new Font( "Arial", Font.BOLD, 12 );
	private Font largerFont = defaultFont.deriveFont( (float) 16 );
	private int largerFontHeight = 0;
	//private Color paintColor = charger.Hub.chargerBlueColor;
	private static Color paintColor = new Color( 0, 0, 127 );
	double max = 0;
	double min = 0;
	double value = 0;
	double previous_value = 0;
	double first_value = 0;
	boolean firstRun = true;
	int barwidth = 0;	// width of the value bar in pixels
	int barheight = 0;  // height of the value bar in pixels
	String legend = new String();
	private NumberFormat nformat = NumberFormat.getNumberInstance();
	private NumberFormat vformat = NumberFormat.getNumberInstance();		// format for the value only
	private int decimalPlaces = 4;
	//public JTextField decimalPlacesField = new JTextField();
	//private JFrame display = new JFrame();
	
	JMenuBar mainMenuBar = new JMenuBar();
	JMenu optionsMenu = new JMenu( "Options" );
	JMenu decimalPlacesMenu = new JMenu( "Decimal Places" );
	JMenu showPreviousMenu = new JMenu( "Show Previous Value" );
	JCheckBoxMenuItem showPreviousOff = new JCheckBoxMenuItem( "Off");
	JCheckBoxMenuItem showPreviousOn = new JCheckBoxMenuItem( "On");
	JMenu showFirstMenu = new JMenu( "Show First Value" );
	JCheckBoxMenuItem showFirstOff = new JCheckBoxMenuItem( "Off");
	JCheckBoxMenuItem showFirstOn = new JCheckBoxMenuItem( "On");
	
	JCheckBoxMenuItem decimalItems[] = new JCheckBoxMenuItem[ 10 ];
	
	boolean showPrevious = true;
	boolean showFirst = false;

	WindowEvent we = new WindowEvent( this, WindowEvent.COMPONENT_RESIZED );

		private String formatDouble( double val )
		{
			val = val * 1000;
			val = (double)(Math.round( val )) / 1000.0;
			
			String s = "" + val;
				// if no decimal point, then okay
			if ( s.indexOf( '.' ) == -1 ) return s;
			else
			{
				while ( s.endsWith( "00" ) ) 
				{
					s = s.substring( 0, s.length() - 1 );
				}
				if ( s.endsWith( ".0" ) ) return s.substring( 0, s.indexOf( '.' ) );
						// trim to 6 significant digits at most
				if ( (s.length() > 6 ) && s.indexOf( '.' ) < 7 ) 
					return s.substring( 0, 6 );
				else 
					return s;
			}
		}
	
	/**
		Panel that contains the actual quantity bar.
	 */
	class barPanel extends JPanel
	{
		public barPanel()
		{
			setMinimumSize( new Dimension( 100, 300 ) );
			setOpaque( true );
		}
		
		public void paintComponent( Graphics g )
//		public void paint( Graphics g )
		{
			super.paintComponent( g );

			Font holdfont = g.getFont();
			g.setFont( largerFont );
			largerFontHeight = g.getFontMetrics().getHeight();

			Dimension bardim = getSize();
			barheight = bardim.height - largerFontHeight;
			barwidth = bardim.width;
			

					// draw the actual value bar as a rectangle
			g.setColor( paintColor );
			double fraction = (double)(value - min) / (double)(max - min );
			g.fillRect(
				1,
				barheight - (int)(Math.round( fraction * barheight )),
				barwidth - 1,
				(int)(Math.round( fraction * barheight ) + 1) );

			double first_fraction = (double)(first_value - min) / (double)(max - min );
			g.setColor( Color.green );
			if ( showFirst )
				g.fillRect( 
					1, 1 + barheight - (int)(Math.round( first_fraction * barheight )),
					barwidth - 1, 2 );

			double previous_fraction = (double)(previous_value - min) / (double)(max - min );
			g.setColor( Color.red );
			if ( showPrevious )
				//g.fillRect( 
				//	1, 1 + barheight - (int)(Math.round( previous_fraction * barheight )),
				//	barwidth - 1, 2 );
			{
				float yval = barheight - Math.round( previous_fraction * barheight );
				((Graphics2D)g).setStroke( new BasicStroke( 2.0f ) );
				((Graphics2D)g).draw( 
					new java.awt.geom.Line2D.Double( 
						new java.awt.geom.Point2D.Double( 1f, yval ),
						new java.awt.geom.Point2D.Double( barwidth - 1, yval )
					) );
				((Graphics2D)g).setStroke( charger.Global.defaultStroke );

			}

					// show the numeric value below the bar
			g.setColor( Color.black );
			vformat.setMaximumFractionDigits( decimalPlaces );
			vformat.setMinimumFractionDigits( decimalPlaces );
			String valstring = vformat.format( value );
			g.drawString( valstring, 
				barwidth/2 - g.getFontMetrics().stringWidth( valstring )/2,
				barheight + g.getFontMetrics().getAscent() );
			g.setFont( holdfont );
		}
	}
	
	private barPanel bar = null;
	
	class labelPanel extends JPanel
	{
		private final int leftMargin = 4;
		private final int tickLength = 6;
		private JTextField maxField = new JTextField();
		private JTextField minField = new JTextField();
		
		public labelPanel()
		{


			nformat.setMaximumFractionDigits( 4 );
			nformat.setMinimumFractionDigits( 0 );
			nformat.setGroupingUsed( true );
			vformat.setParseIntegerOnly( true );

			maxField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try
					{
						setMax( Double.parseDouble( maxField.getText() ), false );						
					} catch ( NumberFormatException nfe ) { setMax( getMax() ); }
				}
			});
			minField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try
					{
						setMin( Double.parseDouble( minField.getText() ), false );
					} catch ( NumberFormatException nfe ) { setMin( getMin() ); }
				}
			});
			
			/*decimalPlacesField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try
					{
						setDecimalPlaces( (nformat.parse( decimalPlacesField.getText() )).intValue() );
					} catch ( ParseException nfe ) { setDecimalPlaces( decimalPlaces ); }
				}
			});
			*/
			
			setOpaque( true );
			//maxField.setSize( new Dimension( 75, 10 ) );
			maxField.setForeground( paintColor );
			maxField.setBackground( Color.white );
			maxField.setFont( defaultFont );
			minField.setForeground( paintColor );
			minField.setBackground( Color.white );
			minField.setFont( defaultFont );
			minField.setSize( new Dimension( 75, 10 ) );
			add( maxField );
			add( minField );
			//add( decimalPlacesField );
		}
		
		
		public void paintComponent( Graphics g )
//		public void paint( Graphics g )
		{
					// set the height of the bottom margin
			super.paintComponent( g );
			Font holdfont = g.getFont();
			g.setFont( largerFont );
			largerFontHeight = g.getFontMetrics().getHeight();
			g.setFont( defaultFont );

					// set the bar dimensions, subtracting the bottom margin
			Dimension bardim = getSize();
			barheight = bardim.height - largerFontHeight;
			g.setColor( paintColor );
			
			maxField.setLocation( leftMargin + tickLength + 1, 2 );
			minField.setLocation( leftMargin + tickLength + 1, barheight - minField.getHeight() );
			//decimalPlacesField.setLocation( 
			//	tickLength + g.getFontMetrics().stringWidth( "decimal places" ) + leftMargin, barheight - 4 );
			
					// draw the vertical line beside the bar
			g.fillRect( 2, 0, leftMargin - 2, barheight + 1 );
					// draw the tick marks
			drawTicks( g, 5 );

			//g.drawString( "decimal places", leftMargin, decimalPlacesField.getLocation().y + largerFontHeight );

			g.setFont( holdfont );
		}
		
		/**
			Draws the tick marks for each interval on the scale.
			@param g context on which to draw the ticks
			@param nt initial number of ticks (will be adjusted if the scale is too spread out)
		 */
		public void drawTicks( Graphics g, int nt )
		{
			int numTicks = 0;
			float gap = 1000;
			while ( gap > 100 ) 
			{
				numTicks = numTicks + nt;
				gap = (float)barheight / (float)numTicks;
			}


			g.setColor( paintColor );
			
			int X = leftMargin;
			float Y = (float)barheight;
					// draw the tick marks
			for ( int k = 0; k <= numTicks; k++ )
			{
				g.drawLine( X, Math.round( Y ), X + tickLength, Math.round( Y ) );
				Y = Y - gap;
			}
					// draw the numeric labels on the tick marks
			double interval = ( max - min ) / numTicks;	// for drawing tick labels
			Point p = new Point( leftMargin, barheight );
			for ( int k = 1; k < numTicks; k++ )
			{
				p.translate( 0, Math.round(-1 * gap) );
				double tickval = (k * interval) + min;
				//g.drawString( formatDouble( tickval ), p.x + tickLength + 3, p.y );
				g.drawString( nformat.format( tickval ), p.x + tickLength + 3, p.y );
			}
		
		}
		
	}
	
	private labelPanel label = null; 

	/**
		Creates a new instance of the quantity bar, with title "display quantity" and values zero.
		@param min the initial minimum value of the range
		@param max the inital maximum value of the range
	 */
	public QuantityBar( double min, double max )
	{
		setTitle( "Display quantity" );
		getContentPane().setLayout( new GridLayout( 1, 2 ) );
		bar = new barPanel();
		label = new labelPanel();

		bar.setBackground( new Color( 245, 245, 255 )  );
		label.setBackground( new Color( 245, 245, 255 ) );

		getContentPane().add( bar );
		getContentPane().add( label );

		setMax( max, false );
		setMin( min, false );
		setSize( 200, 400 );
		
		setupMenus();
		
		setDecimalPlaces( 4 );
		//decimalPlacesField.setText( "" + decimalPlaces );
		setLocation( nextLocation );
		nextLocation.translate( 75, 50 );
	}
	
	/**
		@param m the new maximum value; if less than current value, then ignored.
		@param inform whether to display a message that maximum has changed.
	 */
	public void setMax( double m, boolean inform )
	{
		//if ( m >= value ) 
		{
			max = m;
					// adjust max to be a nicer number
			if ( max > 10 ) max = Math.round( max );
			label.maxField.setText( nformat.format( max ) );
			validate();
			repaint();
			if ( inform ) 
				JOptionPane.showMessageDialog( this, "Maximum value of range changed to " + nformat.format( max ) );
		}
	}

	/**
		@param m the new maximum value; if less than current value, then ignored. same as setMax( m, false )
	 */
	public void setMax( double m ) { setMax( m, false ); }
	

	/**
		@param m the new minimum value; if greater than current value, then ignored.
		@param inform whether to display a message that minimum has changed.
	 */
	public void setMin( double m, boolean inform )
	{
		//if ( (m <= value ) )
		{
			min = m;
			label.minField.setText( nformat.format( min ) );
			validate();
			repaint();
			if ( inform ) 
				JOptionPane.showMessageDialog( this, "Minimum value of range changed to " + nformat.format( min ) );
		}
	}

	/**
		@param m the new minimum value; if greater than current value, then ignored. same as setMin( m, false )
	 */
	public void setMin( double m ) { setMin( m, false ); }
	

	public void setValue( double m )
	{
		if ( firstRun ) 
		{
			first_value = m;
			firstRun = false;
		}
		double increment = (double)max / 2;
		previous_value = value;
		value = m;
		if ( value > max )
		{
			while ( value > max )
				max = Math.round( max + increment );
			setMax( max, true );
		}
		repaint();
	}

	public void setDecimalPlaces( int d )
	{
		if ( d < 0 ) d = 0;
		if ( d > 9 ) d = 9;
		decimalItems[ decimalPlaces ].setState( false );
		decimalPlaces = d;
		decimalItems[ decimalPlaces ].setState( true );
		//decimalPlacesField.setText( "" + decimalPlaces );
		validate();
		repaint();
	}
	
	public void setShowPrevious( boolean b )
	{
		showPrevious = b;
		showPreviousOn.setState( b );
		showPreviousOff.setState( ! b );
		repaint();
	}
	
	public void setShowFirst( boolean b )
	{
		showFirst = b;
		showFirstOn.setState( b );
		showFirstOff.setState( ! b );
		repaint();
	}
	
	public double getValue()
	{
		return value;
	}
	
	public double getMax()
	{
		return max;
	}
	
	public double getMin()
	{
		return min;
	}
	
	public void setLegend( String s )
	{
		legend = s;
		setTitle( legend );
		validate();
		repaint();
	}
	
	public String getLegend()
	{
		return legend;
	}	

	private void setupMenus()
	{
		setJMenuBar( mainMenuBar );
		mainMenuBar.add( optionsMenu );
		for ( int i = 0; i < decimalItems.length; i++ )
		{
			decimalItems[ i ] = new JCheckBoxMenuItem( "" + i );
			decimalPlacesMenu.add( decimalItems[ i ] );
			decimalItems[ i ].addActionListener( this );
		}
		decimalItems[ decimalPlaces ].setState( true );
		optionsMenu.add( decimalPlacesMenu );
		
		showPreviousOn.addActionListener( this );
		showPreviousOff.addActionListener( this );
		
		showPreviousMenu.add( showPreviousOn );
		showPreviousMenu.add( showPreviousOff );
		if ( showPrevious ) showPreviousOn.setState( true );
		else showPreviousOff.setState( true );
		optionsMenu.add( showPreviousMenu );
		
		showFirstOn.addActionListener( this );
		showFirstOff.addActionListener( this );

		showFirstMenu.add( showFirstOn );
		showFirstMenu.add( showFirstOff );
		if ( showFirst ) showFirstOn.setState( true );
		else showFirstOff.setState( true );
		optionsMenu.add( showFirstMenu );
		
		JMenuItem chooseColor = new JMenuItem( "Choose Color..." );
		chooseColor.addActionListener( this );
		optionsMenu.add( chooseColor );
		
		JMenuItem closeItem = new JMenuItem( "Close" );
		optionsMenu.add( closeItem );
		closeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_W, charger.Global.AcceleratorKey ) );
		closeItem.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ) {
				menuCloseActionPerformed();
			}
		
		} );
		

	}


	public void menuCloseActionPerformed()
	{
		setVisible( false );
		dispose();
	}

	
	// Close the window when the close box is clicked
	public void thisWindowClosing(WindowEvent e)
	{
		menuCloseActionPerformed();
	}
	
	
	/**
		Handle the menu events here.
	 */
	public void actionPerformed ( ActionEvent e ) {
   	// handle all menu events here
	   	Object source = e.getSource();
		if ( source == showPreviousOn ) 
			setShowPrevious( true );
		else if ( source == showPreviousOff )
			setShowPrevious( false );
		else if ( source == showFirstOff )
			setShowFirst( false );
		else if ( source == showFirstOn )
			setShowFirst( true );
		else 
		for ( int i = 0; i < decimalItems.length ; i++ )
		{
			if ( decimalItems[ i ] == source ) setDecimalPlaces( i );
		}
		if ( e.getActionCommand().equals( "Choose Color..." ) )
		{
			Color c = JColorChooser.showDialog( this,
				"Choose color for quantity bar.", paintColor );
			if ( c != null )
			{
				paintColor = c;
				label.maxField.setForeground( paintColor );
				label.minField.setForeground( paintColor );
			}
		}
		repaint();
	}


}
