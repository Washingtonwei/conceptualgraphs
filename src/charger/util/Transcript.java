//
//  Transcript.java
//  CharGer 2003
//
//  Created by Harry Delugach on Tue May 06 2003.
//
package charger.util;

//import charger.*;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JOptionPane;


//import javax.swing.*;
//import java.awt.*;

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
	Gives us the ability to track a user's dialog, even though user interaction is actually scattered
	among all of the GUI options.
	Most of the dialog calls are merely wrappers to JOptionPane methods.
 */
public class Transcript  {

	StringBuilder log = new StringBuilder( "" );
	String eol = System.getProperty( "line.separator" );
	String tell = "SYSTEM: ";
	String user = "  USER: ";
	
	/**
		Create a new transcript with empty contents.
	 */
	public Transcript()
	{
		log = new StringBuilder( "" );
	}
	
	/** Set the transcript's contents to empty, without warning or saving. */
	public void clearTranscript() { log = new StringBuilder( "" ); }
	
	/** Get the transcript's contents as a string. */
	public String toString() { return "TRANSCRIPT:\n" + log.toString(); }
	
	/** Append the given string to the transcript, followed by a newline. */
	public void append( String s ) { log.append( s + eol ); }
	
	/** Append the given string to the transcript, with an annotation that it is a user prompt. */
	public void appendTell( String s ) { log.append( tell + s + eol ); }
	
	/** Append the given string to the transcript, with an annotation that it is a user's reply. */
	public void appendUser( String s ) { log.append( user + s + eol ); }
	
	/** @see JOptionPane#showMessageDialog */
	public void showMessageDialog( Component parentComponent, Object message )
	{
		log.append( tell + message + eol );
		JOptionPane.showMessageDialog( parentComponent, message );
	}

	/** @see JOptionPane#showMessageDialog */
	public void showMessageDialog( Component parentComponent, Object message, String title, int messageType )
	{
		log.append( tell + title + ": " + message + eol );
		JOptionPane.showMessageDialog( parentComponent, message, title, messageType );
	}

	/** @see JOptionPane#showInputDialog */
	public String showInputDialog( Component parentComponent, Object message )
	{
		log.append( tell + message + eol );
		String answer = JOptionPane.showInputDialog( parentComponent, message );
		log.append( user + answer + eol );
		return answer;
	}

	/** @see JOptionPane#showInputDialog */
	public String showInputDialog( Component parentComponent, Object message, Object initialValue )
	{
		log.append( tell + message + "(\"" + initialValue + "\" suggested)" + eol );
		String answer = JOptionPane.showInputDialog( parentComponent, message, initialValue );
		if ( answer != null && answer.equals( initialValue ) ) answer = "agrees with suggestion.";
		log.append( user + answer + eol );
		return answer;
	}

	/** @see JOptionPane#showInputDialog */
	public Object showInputDialog( Component parentComponent, Object message, String title,
			int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue)
	{
		log.append( tell + message + "\n   (\"" + initialSelectionValue + "\" suggested)" + eol );
		String answer = (String)JOptionPane.showInputDialog( parentComponent, message, title,
				messageType, icon, selectionValues, initialSelectionValue );
		String loganswer = answer;
		if ( answer != null && answer.equals( initialSelectionValue ) ) loganswer = "agrees with suggestion.";
		log.append( user + loganswer + eol );
		return answer;
	}

}
