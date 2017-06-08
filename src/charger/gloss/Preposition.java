//
//  Preposition.java
//  CharGer 2003
//
//  Created by Harry Delugach on Tue Jun 24 2003.
//

package charger.gloss;

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
	Abstraction to represent the notion of a preposition in a phrase.
 */
public class Preposition {

private static String[] prepositionList = {
		"aboard",
		"about",
		"above",
		"across",
		"after",
		"against",
		"along",
		"amid",
		"among",
		"anti",
		"around",
		"as",
		"at",
		"before",
		"behind",
		"below",
		"beneath",
		"beside",
		"besides",
		"between",
		"beyond",
		"but",
		"by",
		"concerning",
		"considering",
		"despite",
		"down",
		"during",
		"except",
		"excepting",
		"excluding",
		"following",
		"for",
		"from",
		"in",
		"inside",
		"into",
		"like",
		"minus",
		"near",
		"of",
		"off",
		"on",
		"onto",
		"opposite",
		"outside",
		"over",
		"past",
		"per",
		"plus",
		"regarding",
		"round",
		"save",
		"since",
		"than",
		"through",
		"to",
		"toward",
		"towards",
		"under",
		"underneath",
		"unlike",
		"until",
		"up",
		"upon",
		"versus",
		"via",
		"with",
		"within",
		"without"
		};
	
	/**
		Tells whether the given string is on the preposition list or not. Only works for English.
		
		@param p any string
		@return <code>true</code> if the string is on a list of approximately 60 English
			prepositions; <code>false</code> otherwise.
	 */	
	public static boolean isPreposition( String p )
	{
		for ( int k = 0; k < prepositionList.length; k++ )
			if ( prepositionList[ k ].equalsIgnoreCase( p ) ) return true;
		return false;
	}
}
