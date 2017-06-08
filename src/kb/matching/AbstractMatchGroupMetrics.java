//
//  AbstractMatchGroupMetrics.java
//  CharGer Project
//
//  Created by Harry Delugach on 1/2/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package kb.matching;

/**
    Metrics for of a group of matches. Although generally used for a set of matches all with the same criteria,
    * they might also be using different master graphs, or even different matching strategies. 
    For example, they might use different tuple matchers, with different protocols or strategies.
    The group may be named for convenience.
     @since 3.5b2
 */
public class AbstractMatchGroupMetrics 
{
    String name = null;
    public AbstractMatchGroupMetrics()
    {
        setup( null );
    }
    
    /**
        @param s Name of the match group (for convenience).
     */
    public AbstractMatchGroupMetrics( String s )
    {
        setup( s );
    }
    
    private void setup( String s )
    {
        name = s;
    }
    
    public String getName() { return name; }

}

