package charger.util;

    /** Used for holding basic statistics on a set of measurements.
        The statistics kept are a count (i.e., the cardinality of the set), the min and max, and the sum.
        The actual values (i.e., the series) are not stored.
    */
public class FloatStatistics
{
    String name = "";
    public int count = 0;      // number of values being collected.
    
    public float max = Float.NEGATIVE_INFINITY;
    public float min = Float.POSITIVE_INFINITY;
    public float sum = 0;
    private float square_sum = 0;
    
            /** @param n the name of the variable captured by these stats. */
    public FloatStatistics( String n )
    {
        name = n;
        reset();
    }
    
    /**
        Resets all the statistics associated with the statistics object.
        Sets the count to zero, max to a hugely negative number, min to a hugely positive number and the sum to 0.
     */
    public void reset()
    {
        count = 0;
        max = Float.NEGATIVE_INFINITY;
        min = Float.POSITIVE_INFINITY;
        sum = 0;
        square_sum = 0;
    }
    
    /**
     * Add a value to the set.
     * @param val 
     */
    public void addValue( float val )
    {
        count++;
        sum += val;
        square_sum += val*val;
        if ( val < min ) min = val;
        if ( val > max ) max = val;
    }
    
        /** Get the average of the series so far. If count is zero, returns 0.
         * @return the average of all the values in the series, zero if there are none. */
    public float getAverage()
    {
        if ( count == 0 ) return 0;
        return ( sum / count );
    }
    
            /** Get the standard deviation of the series so far. If count is zero, returns 0.
         * @return the standard deviation of all the values in the series, zero if there are none. */
    public float getStdDeviation()
    {
        if ( count == 0 ) return 0;
        float mean = getAverage();
        return (float)Math.sqrt( square_sum / count - mean*mean );
    }
}
