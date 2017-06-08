/*
 * Copyright (C) 2014 hsd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package kb.matching;

import charger.obj.Concept;
import kb.hierarchy.TypeHierarchy;

/**
 * A class to represent the abstraction of similarity between two CG concepts.
 * Takes into account types, supertypes, referents, etc.
 * Considers similarity to be a floating point number between 0 and 1 inclusive.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class SimilarityMeasure {

    double similarity = 0.0d;

    /**
     * Given two concepts, prepare their similarity measurements.
     * @param c1
     * @param c2 
     */
    public SimilarityMeasure( TypeHierarchy hierarchy, Concept c1, Concept c2 ) {
        
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity( double similarity ) {
        this.similarity = similarity;
    }
    
    /** Uses the hierarchy to determine similarity of two types. 
     * The strategy is to favor shorter numbers of "hops" up and down the hierarchy.
     * A straightforward calculation is to say the similarity is 
     * 1 / (number of hops + 1). If two types are the same, then hops is zero and the similarity is one.
     * If there's one hop difference, then hops is one and the similarity is one-half, etc.
     * @return 
     */
    private double calculateTypeSimilarity() {
        
        return 0.0;
    }
    
}
