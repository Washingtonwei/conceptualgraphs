package charger.exception;import charger.*;/* 	$Header$ *//*    CharGer - Conceptual Graph Editor    Copyright reserved 1998-2014 by Harry S. Delugach            This package is free software; you can redistribute it and/or modify    it under the terms of the GNU Lesser General Public License as    published by the Free Software Foundation; either version 2.1 of the    License, or (at your option) any later version. This package is     distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;     without even the implied warranty of MERCHANTABILITY or FITNESS FOR A     PARTICULAR PURPOSE. See the GNU Lesser General Public License for more     details. You should have received a copy of the GNU Lesser General Public    License along with this package; if not, write to the Free Software    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA*//**	General class of exceptions defined by the CharGer System	@author Harry S. Delugach ( delugach@uah.edu ) Copyright reserved 1998-2014 by Harry S. Delugach */public class CGException extends CGThrowable {	public CGException( String s ) {		super( s );	}	public CGException( String s, Object o ) {		super( s, o );	}}