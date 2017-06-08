/*
 * Copyright (C) 2015 hsd.
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

package mm;

import kb.matching.MatchDegree;
import kb.matching.SimilarityMeasure;

/**
 * Encapsulates the result of a match. Intended to be as general as possible,
 * capturing details of a partial match including a value for each kind of 
 * partial match.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class MatchingResult {
    
    MatchDegree matchDegree;
    
    boolean partialEnabled = false;
    
    /** Whether a synonym was used in obtaining the match */
    boolean synonymUsed;
    
    /** Whether a subtype/supertype was used in obtaining the match */
    boolean subtypeUsed;
    
    /** Keeps the similarity based on subtypes/supertypes */
    SimilarityMeasure similarity;

}
