package charger;import java.awt.Color;import java.awt.Cursor;import java.awt.Font;import javax.swing.JFrame;import javax.swing.JLabel;/*    CharGer - Conceptual Graph Editor    Copyright reserved 1998-2014 by Harry S. Delugach            This package is free software; you can redistribute it and/or modify    it under the terms of the GNU Lesser General Public License as    published by the Free Software Foundation; either version 2.1 of the    License, or (at your option) any later version. This package is     distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;     without even the implied warranty of MERCHANTABILITY or FITNESS FOR A     PARTICULAR PURPOSE. See the GNU Lesser General Public License for more     details. You should have received a copy of the GNU Lesser General Public    License along with this package; if not, write to the Free Software    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA*//**	@author Harry S. Delugach ( delugach@uah.edu ) Copyright reserved 1998-2014 by Harry S. Delugach */public class CGSplashFrame extends JFrame {	private JLabel MainNameLabel = new JLabel();	private JLabel copyrightLabel = new JLabel();	private JLabel logoPanel = new JLabel();	private JLabel startingUp = new JLabel( "Starting up..." );		public CGSplashFrame(  )	{		Color fore = Color.white;		Color back = Global.chargerBlueColor;	    // the following code sets the frame's initial state	    MainNameLabel.setText("CharGer - version " + Global.CharGerVersion );	    MainNameLabel.setHorizontalAlignment( JLabel.CENTER );	    MainNameLabel.setForeground( fore );	    MainNameLabel.setLocation(new java.awt.Point(0, 20));	    MainNameLabel.setVisible(true);	    MainNameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));	    MainNameLabel.setSize(new java.awt.Dimension(500, 44));	    copyrightLabel.setText("Copyright reserved 1998-2014 by Harry S. Delugach");	    copyrightLabel.setHorizontalAlignment( JLabel.CENTER );	    copyrightLabel.setForeground( fore);	    copyrightLabel.setLocation(new java.awt.Point(0, 70));	    copyrightLabel.setVisible(true);	    copyrightLabel.setFont(new Font("SansSerif", Font.BOLD, 14));	    copyrightLabel.setSize(new java.awt.Dimension(500, 30));	    startingUp.setHorizontalAlignment( JLabel.CENTER );	    startingUp.setForeground( fore);	    startingUp.setLocation(new java.awt.Point(50, 125));	    startingUp.setVisible(true);	    startingUp.setFont(new Font("SansSerif", Font.BOLD, 12));	    startingUp.setSize(new java.awt.Dimension(500, 30));	    logoPanel.setLocation(new java.awt.Point(0, 70));	    logoPanel.setVisible(true);	    logoPanel.setSize(new java.awt.Dimension(500, 110));	    setLocation(new java.awt.Point(200, 150));	    setForeground( fore );	    setTitle("CharGer Startup");	    getContentPane().setBackground( back );	    getContentPane().setLayout(null);	    setSize(new java.awt.Dimension(500, 200));	    getContentPane().add(MainNameLabel);	    getContentPane().add( copyrightLabel );	    getContentPane().add( startingUp );	    getContentPane().add(logoPanel);	    addFocusListener(new java.awt.event.FocusAdapter() {		    public void focusGained(java.awt.event.FocusEvent e) {			    thisFocusGained(e);		    }	    });	    addWindowListener(new java.awt.event.WindowAdapter() {		    public void windowClosing(java.awt.event.WindowEvent e) {			    thisWindowClosing(e);		    }	    });	}  	// Close the window when the close box is clicked	public void thisWindowClosing(java.awt.event.WindowEvent e)	{		//setVisible(false);		dispose();		//System.exit(0);	}		public void thisFocusGained(java.awt.event.FocusEvent e)	{		refresh();	}	public void refresh()	{		setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );	}		}