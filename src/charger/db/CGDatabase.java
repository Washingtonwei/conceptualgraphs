package charger.db;

    /**
	Defines a "database" for purposes of the &lt;dbfind&gt; or &lt;lookup&gt; actors.
	Subclasses 
     */
abstract public interface CGDatabase
{
	    /** Accesses the name by which the database will be known to graphs.
		This means a valid referent for a "Database" concept in an editor frame.
	     @return name of the database
	     @see charger.act.GraphUpdater
	     */
	abstract public String getName();
	
	    /**
		Opens and resets the given database so that it is ready to be accessed from its beginning.
	     */
	abstract public void resetDB();
	
	    /**
		Makes the database no longer accessible to CharGer.
	     */
	abstract public void closeDB();

	    /**
		Perform the actual lookup for a key. The keytype and targettype can be considered as field names in a typical
		relation. doLookup find the target value that corresponds with the keyvalue that appears in the keytype field
		of a record. Returns the first one that matches if there are muliple occurrences of the keyvalue.
		@param keytype Actually the CharGer type for what a particular key is; e.g., "Birth Date" or "Gender"
		@param keyvalue The CharGer referent value for a concept of type keytype
		@param targettype the CharGer type for the key value to be looked up, as in keytype
		@return target value that corresponds to the targettype associated with the keytype and value
	     */
	abstract public String doLookup(String keytype, String keyvalue, String targettype);
}