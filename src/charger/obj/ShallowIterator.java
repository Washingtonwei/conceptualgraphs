package charger.obj;

import charger.*;
import java.util.*;
import java.awt.*;
import charger.*;/* 	$Header$ *//*    CharGer - Conceptual Graph Editor    Copyright reserved 1998-2014 by Harry S. Delugach            This package is free software; you can redistribute it and/or modify    it under the terms of the GNU Lesser General Public License as    published by the Free Software Foundation; either version 2.1 of the    License, or (at your option) any later version. This package is     distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;     without even the implied warranty of MERCHANTABILITY or FITNESS FOR A     PARTICULAR PURPOSE. See the GNU Lesser General Public License for more     details. You should have received a copy of the GNU Lesser General Public    License along with this package; if not, write to the Free Software    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA*/



/**
 * Collects only the immediately enclosed objects of a graph (i.e., not
 * recursively) to form an iterator. The Graph itself is not included. Can
 * simulate DeepIterator by recursively calling itself for every nested grpah.
 * If there are no nested contexts, then works identically to DeepIterator.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 * @see DeepIterator
 */
public class ShallowIterator extends GraphObjectIterator {

    /**
     * @param g Graph all of whose elements are collected to form the iterator
     * @see DeepIterator
     */
    public ShallowIterator( Graph g ) {
        super( g, null, GraphObject.Kind.ALL, false );
    }

    /**
     * @param g graph whose elements of one kind are collected to form the
     * iterator
     * @param kind	one of the GraphObject GNODE, GEDGE or GRAPH
     * @see DeepIterator
     */
    public ShallowIterator( Graph g, GraphObject.Kind kind ) {
        super( g, null, kind, false );
    }

    /**
     * @param g graph whose elements of one class are collected to form the
     * iterator
     * @param go	one of the GraphObject subclasses
     * @see DeepIterator
     */
    public ShallowIterator( Graph g, GraphObject go ) {
        super( g, go, GraphObject.Kind.ALL, false );
    }
}