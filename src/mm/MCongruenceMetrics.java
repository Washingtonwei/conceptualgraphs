/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.obj.Concept;
import charger.obj.DeepIterator;
import charger.obj.GEdge;
import charger.obj.GraphObject;
import charger.obj.Relation;
import charger.util.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import kb.BinaryTuple;
import kb.ObjectHistoryEvent;
import kb.ObjectHistoryEventType;
import kb.matching.MatchDegree;
import kb.matching.SynonymConceptMatcher;

/**
 * Encapsulates the calculating of congruence metrics for the Mental Models
 * research. Handles both the concept congruence and the relation congruence.
 * Strings that are returned are assumed to be HTML strings.
 *
 * @author Harry Delugach
 * @since Charger 3.8.0
 *
 */
public class MCongruenceMetrics extends MMetrics {

    /**
     * The highest level congruence possible for its set of models; i.e., the
     * number of models.
     */
    private int maxLevels = models.size();
    /**
     * Holds the concept congruence values. conceptCongruence[ 0 ] isn't used.
     * conceptCongruence[ 1 ] should always be 1.00 by definition.
     * conceptCongruence[ 2 ] through [ maxLevels ] are valid values.
     */
    private float[] conceptCongruence = new float[ maxLevels + 1 ];
    /**
     * @see #conceptCongruence
     */
    private float[] relationCongruence = new float[ maxLevels + 1 ];
    public boolean showAllCorefSets = false;
    public boolean showLevelCorefSets = false;
    /**
     * Tells congruence analysis whether to use a synonym list or not.
     */
    public boolean useSynonyms = false;
    /**
     * Tells congruence analysis whether to require referents to match in
     * addition to type labels.
     */
    public boolean matchReferents = false;
    /**
     * Tells congruence analysis whether to ignore relation direction when
     * matching concepts.
     */
    public boolean ignoreRelationDirection = false;
    /**
     * The matcher that will be used by congruence metrics
     */
    public SynonymConceptMatcher matcher = new SynonymConceptMatcher();
    /**
     * The synonyms (if any) that will be used in performing this analysis
     */
    ArrayList<ClusterCollection> clusterCollections = null;
    /**
     * List of all concepts used in all the models. Used for the matching of
     * concepts.
     */
    public ArrayList<GraphObject> cTotal = new ArrayList<>();
    /**
     * List of all distinct concepts (i.e., ones that do not match any other
     * according to the concept matcher rules). This set forms a "group" in
     * mathematical terms; all concepts in a group are considered counterparts
     * to each other, and never a counterpart to any other concept in any other
     * group. When there are no synonyms, the concepts'referents must match exactly.
     */
    public ArrayList<GraphObjectCounterpartSet> cDistinct = new ArrayList<>();
    /**
     * List of all relations used in all the models. Used for the matching of
     * relations.
     */
    public ArrayList<GraphObject> tTotal = new ArrayList<>();
    /**
     * List of all distinct relations (i.e., ones that have no match according
     * to the tcs matcher rules.)
     */
    public ArrayList<TupleCounterpartSet> tDistinct = new ArrayList<>();
    
    double[] singletonsConcepts = new double[ models.size() ];     // use double so that std dev will work for all
    double[] singletonsRelations = new double[ models.size() ];     // use double so that std dev will work for all


    /**
     * Creates a new instance with the given set of models and an identifying
     * caption. Upon instantiation, it constructs the main list of modeled
     * objects.
     *
     * @param models The models to be analyzed. Note that if there is any
     * special model (e.g., an "observer" or "combined" model) this class does
     * not recognize it as special. That is the responsibility of the caller.
     * @param caption An informative string identifying this set of metrics.
     * @see GraphObjectCounterpartSet
     */
    public MCongruenceMetrics( ArrayList<MModel> models, String caption ) {
        super( models, caption );
        matcher.setIgnoreSpaces( true );
    }

    /**
     * Tells whether to show the entire list of coreferent sets when reporting
     * these metrics.
     *
     * @param b if true, then show the entire list of coreferent sets; if false,
     * don't show them.
     * @see #setShowLevelCorefSets
     */
    public void setShowAllCorefSets( boolean b ) {
        showAllCorefSets = b;
    }

    /**
     * @param b if true, then show the coreferent sets counted for each level;
     * if false, don't show them.
     * @see #setShowAllCorefSets
     */
    public void setShowLevelCorefSets( boolean b ) {
        showLevelCorefSets = b;
    }

    /**
     * Sets whether congruence should take synonyms into account. This also
     * tells the matcher whether to use synonyms.
     *
     * @param useSynonyms true if synonyms are to be used in matching; false
     * otherwise.
     */
    public void setUseSynonyms( boolean useSynonyms ) {
        this.useSynonyms = useSynonyms;
        matcher.setSynonymsEnabled( useSynonyms );
    }

    /**
     * Sets whether concept referents must match in addition to the type (which
     * must always match) This also tells the matcher to match referents or not.
     *
     * @param matchReferents
     */
    public void setMatchReferents( boolean matchReferents ) {
        this.matchReferents = matchReferents;
        matcher.setMatchReferents( matchReferents );
    }

    public void setIgnoreRelationDirection( boolean ignore ) {
        ignoreRelationDirection = ignore;
    }

    /**
     * The list of all coreferent sets for this metrics object.
     *
     * @return the set of counterparts.
     */
    public ArrayList<GraphObjectCounterpartSet> getcDistinct() {
        return cDistinct;
    }

    /**
     * The heart of the metrics class, this method analyzes the models, creating
     * data structures for analysis, and builds an HTML string giving the
     * results of the metrics.
     *
     */
    @Override
    public void generateMetrics() {
        generateSharedConceptLists();
        generateSharedTupleLists();
        for ( int level = 1; level <= maxLevels; level++ ) {
            generateLevelCongruenceForConcepts( level );
            generateLevelCongruenceForTuples( level );
        }
    }

    /**
     * Creates a text string with the results of analysis. This is a
     * human-readable and somewhat wordy version, helpful for debugging and
     * explaining details of the analysis. There are other forms returned from
     * MTeamMetrics.
     *
     * @return a (rather long) string comprising the results of analysis. If
     * verbose is set, the string has even more information.
     * @param kind Whether to get the results for tuples or for concepts
     * @see #generateSharedConceptLists
     * @see #generateSharedTupleLists
     * @see MTeamMetrics
     */
    public String getResults( MMComponentKind kind ) {
        StringBuilder results = new StringBuilder( Tag.p( Tag.bold( getCaption() ) + Tag.br + "Results for " + models.size() + " models:" ) );
        results.append( showModelNames() ).append( "\n" );
        if ( models.size() < 2 ) {
            results.append( Tag.p( "Metric not applicable to " + models.size() + " model(s)." ) );
        } else {
            results.append( matcher.explainYourself() ).append( "\n" );
            switch ( kind ) {
                case CONCEPT:
                    results.append( Tag.p( "Item occurrences: " + cTotal.size() + Tag.br
                            + "Distinct item(s): " + cDistinct.size() + Tag.br
                            + Tag.bold( "Singleton count std dev = " )
                            + MMetrics.nformat.format( getSingletonStdDev( kind, false ) ) + Tag.br
                            + Tag.bold( "Singleton pct std dev = " )
                            + MMetrics.nformat.format( 100 * getSingletonStdDev( kind, true ) ) + " % " ) );
                    
                                // show how many singletons are in each model
                    results.append( Tag.table( 200) );
//                    results.append( Tag.tdspan( 2, "Singletons per model" ) );
                    results.append( Tag.tr( Tag.tdc( Tag.bold("Model"))  + Tag.tdr( Tag.bold("Singletons")) ) );
                    for ( int m = 0; m < models.size(); m++ ) {
                        results.append( Tag.tr (
                                Tag.tdr( models.get( m ).getID().getUser() ) +
                                Tag.tdr( Tag.sp(1) + (int)(singletonsConcepts[ m ]))
                                )
                                );
                    }
                    results.append( Tag._table+ Tag.br );

                    for ( int level = 2; level <= maxLevels; level++ ) {
                        results.append( showLevelCongruenceConcepts( level ) );
                        if ( showLevelCorefSets ) {
                            results.append( showMatchedGraphObjects( cDistinct, level ) );
                        }
                    }
                    if ( showAllCorefSets ) {
                        results.append( showMatchedGraphObjects( cDistinct, 1 ) );
                    }
                    break;
                case RELATION:
                    results.append( Tag.p( "Item occurrences: " + tTotal.size() + Tag.br
                            + "Distinct item(s): " + tDistinct.size() + Tag.br
                            + Tag.bold( "Singleton count std dev = " )
                            + MMetrics.nformat.format( getSingletonStdDev( kind, false ) ) + Tag.br
                            + Tag.bold( "Singleton pct std dev = " )
                            + MMetrics.nformat.format( 100 * getSingletonStdDev( kind, true ) ) + " % " ) );

                                // show how many singletons are in each model
                    results.append( Tag.table( 200) );
//                    results.append( Tag.tdspan( 2, "Singletons per model" ) );
                    results.append( Tag.tr( Tag.tdc( Tag.bold("Model"))  + Tag.tdr( Tag.bold("Singletons")) ) );
                    for ( int m = 0; m < models.size(); m++ ) {
                        results.append( Tag.tr (
                                Tag.tdr( models.get( m ).getID().getUser() ) +
                                Tag.tdr( Tag.sp(1) + (int)(singletonsRelations[ m ]))
                                )
                                );
                    }
                    results.append( Tag._table+ Tag.br );

                    for ( int level = 2; level <= maxLevels; level++ ) {
                        results.append( showLevelCongruenceTuples( level ) );
                        if ( showLevelCorefSets ) {
                            results.append( showMatchedGraphTuples( tDistinct, level ) );
                        }
                    }
                    if ( showAllCorefSets ) {
                        results.append( showMatchedGraphTuples( tDistinct, 1 ) );
                    }
                    break;
            }
        }
        return results.toString();
        //return results + Tag.p( Tag.bold( "Finished " + getName() ) ) + Tag.hr ;
    }

    /**
     * Required to implement this abstract method. Same as getResults(
     * MMComponentKind.CONCEPT ). Really shouldn't be used -- caller should
     * always specify which kind of results are being fetched.
     *
     * @return returns results for getResults( MMComponentKind.CONCEPT )
     */
    @Override
    public String getResults() {
        return getResults( MMComponentKind.CONCEPT );
    }

    /**
     * Populates the concept congruence value list with the concept congruence
     * values for a given level
     *
     * @see #getNCShared
     */
    public void generateLevelCongruenceForConcepts( int level ) {
        float congruence;

        if ( level > maxLevels ) {
            congruence = -1f;
        } else {
            float denom = (float)cDistinct.size();
            if ( denom > 0.0 ) {
                congruence = getNCShared( level ) / denom;
            } else {
                congruence = 0;
            }
        }
        conceptCongruence[ level] = congruence;
    }

    /**
     * @param level The level being analyzed. In general, the level represents
     * the number of models with agreement.
     * @return HTML string showing the concept congruence at this level.
     */
    public String showLevelCongruenceConcepts( int level ) {
        String r = "";

        if ( level > maxLevels ) {
            r = Tag.p_red( "Level " + level + " is not valid. Max value of " + maxLevels );
        } else {
            float denom = (float)cDistinct.size();
            float congruence;
            if ( denom > 0.0 ) {
                congruence = getNCShared( level ) / denom;
            } else {
                congruence = 0;
            }
            if ( congruence != 0 ) {
                r = Tag.bold( "Level " + level + " congruence: " + getNCShared( level ) + "/" + (int)denom ) + " = "
                        + nformat.format( congruence ) + Tag.br;
            }
        }

        return r;
    }

    /**
     * Populates the concept congruence value list with the concept congruence
     * values for a given level
     *
     * @see #getNCShared
     */
    public void generateLevelCongruenceForTuples( int level ) {
        float congruence;

        if ( level > maxLevels ) {
            congruence = -1f;
        } else {
            float denom = (float)tDistinct.size();
            if ( denom > 0.0 ) {
                congruence = getNTShared( level ) / denom;
            } else {
                congruence = 0;
            }
        }
        relationCongruence[ level] = congruence;
    }

    /**
     * @param level The level being analyzed. In general, the level represents
     * the number of models with agreement.
     * @return HTML string showing the relation congruence at this level.
     */
    public String showLevelCongruenceTuples( int level ) {
        String r = "";

        if ( level > maxLevels ) {
            r = Tag.p_red( "Level " + level + " is not valid. Max value of " + maxLevels );
        } else {
            float denom = (float)tDistinct.size();
            float congruence;
            if ( denom > 0.0 ) {
                congruence = getNTShared( level ) / denom;
            } else {
                congruence = 0;
            }
            if ( congruence != 0 ) {
                r = Tag.bold( "Level " + level + " congruence: " + getNTShared( level ) + "/" + (int)denom ) + " = "
                        + nformat.format( congruence ) + Tag.br;
            }
        }

        return r;
    }

    /**
     * Constructs both the cTotal (complete list of concepts) and cDistinct
     * (distinct concepts) lists <p>cTotal is the set of all mentioned concepts
     * in all models.
     *
     * <p>cDistinct is where each concept only appears once. <p>If there is a
     * synonym or other inexact match, only one member of the matched set will
     * appear in the set. That means if "head" and "supervisor" are deemed
     * synonyms, Role:head and Role:supervisor match and therefore only one (the
     * first one) will appear in this list. This requires all synonym or other
     * inexact matches to be reflexive. <p>This method also links each of the
     * models to the ModeledGraphObjects in the list.
     *
     * @see GraphObjectCounterpartSet
     */
    public void generateSharedConceptLists() {
        cTotal.clear();
        cDistinct.clear();

        DeepIterator iter;

        for ( MModel model : models ) {
            iter = new DeepIterator( model.getGraph() );
            while ( iter.hasNext() ) {
                GraphObject go = (GraphObject)iter.next();
                if ( go instanceof Concept ) {
                    // add to cTotal no matter what
                    cTotal.add( go );
//                    Global.info( "checking Concept " + go.getTextLabel() + " in model " + model.getID().getName());
                    GraphObjectCounterpartSet matched = null;  // if null later, then there was no match
                    // if there's already a counterpart to match, then add this model and object to that concept's list.
                    for ( GraphObjectCounterpartSet mgo : cDistinct ) {
//                            Global.info( "checking Concept " + go.getTextLabel() + " in model " + model.getID().getName() +
//                                    " against counterpart set " + mgo.obj.getTextLabel() );
                        if ( matcher.levelOfConceptMatch( (Concept)mgo.obj, (Concept)go ) == MatchDegree.BOTH ) {
                            // TODO: Note that matching is strictly boolean here. We need to accommodate 
                            //     matching that ranges from >0 to 1.0
                            matched = mgo;
//                            Global.info( "-- matched " + go.getTextLabel() + " in counterpart set.");
                            break;      // return the first matched object
                        }
                    }
                    if ( matched == null ) {
                        matched = new GraphObjectCounterpartSet();
                        matched.setModel( model );
                        matched.setObj( go );
                        cDistinct.add( matched );
//                            Global.info( "  create new counterpart set with concept " + go.getTextLabel() + " in model " + model.getID().getName() );
                    }
                    matched.addMatchedGraphObject( go, model );

                } else {
                    // Here if it's not a concept
                }
            }
        }
    }

    /**
     * Constructs both the tTotal and tDistinct Lists <p>tTotal is the set of
     * all mentioned relations in all models.
     *
     * <p>tDistinct is where each tuple only appears once. <p>If there is a
     * synonym or other inexact match with its corresponding concepts, only one
     * member of the matched set of tuples will appear in the set. This requires
     * all synonym or other inexact matches to be reflexive. <p>This method also
     * links each of the models to the ModeledGraphObjects in the list.
     *
     * @see GraphObjectCounterpartSet
     * @see TupleCounterpartSet
     */
    public void generateSharedTupleLists() {
        tTotal.clear();
        tDistinct.clear();


        DeepIterator iter;

        for ( MModel model : models ) {

            iter = new DeepIterator( model.getGraph() );
            while ( iter.hasNext() ) {
                GraphObject go = (GraphObject)iter.next();
                BinaryTuple bt = null;
                if ( go instanceof Relation ) {
                    // add to tTotal in all cases
                    tTotal.add( go );
                    ArrayList linkedNodes = null;

                    // Index Out of Bound exception here, probably because getLinkedNodes return empty
                    linkedNodes = ( (Relation)go ).getLinkedNodes( GEdge.Direction.FROM );
                    if ( linkedNodes.isEmpty() ) {
                        continue;  // if there's no from or to nodes, then we can't create a tuple
                    }
                    Concept c1 = (Concept)linkedNodes.get( 0 );

                    linkedNodes = ( (Relation)go ).getLinkedNodes( GEdge.Direction.TO );
                    if ( linkedNodes.isEmpty() ) {
                        continue;  // if there's no from or to nodes, then we can't create a tuple
                    }
                    Concept c2 = (Concept)linkedNodes.get( 0 );

                    bt = new BinaryTuple( c1, (Relation)go, c2 );   // make a tuple just in case it needs to be added.

                    // if there's already a match, then add this model to that tcs's list.
                    TupleCounterpartSet matched = null;  // if null later, then there was no match
                    for ( TupleCounterpartSet tcs : tDistinct ) { // look to see if we've already seen a match
                        // first see if the relation label matches
                        if ( matcher.stringsEqual( go.getTextLabel(), tcs.getRelationLabel() ) ) {
                            // If relation name matches, prepare for a tuple and get its two concepts.
                            // Note that this only considers a single input concept and single output concept

                            if ( matcher.isMatchReferents() ) {
                                if ( matcher.levelOfConceptMatch( tcs.tuple.concept1, c1 ) == MatchDegree.BOTH
                                        && matcher.levelOfConceptMatch( tcs.tuple.concept2, c2 ) == MatchDegree.BOTH ) {
                                    matched = tcs;
                                } else if ( ignoreRelationDirection ) {
                                    if ( matcher.levelOfConceptMatch( tcs.tuple.concept1, c2 ) == MatchDegree.BOTH
                                            && matcher.levelOfConceptMatch( tcs.tuple.concept2, c1 ) == MatchDegree.BOTH ) {
                                        matched = tcs;

                                    }
                                }
                            } else {
                                if ( matcher.levelOfConceptMatch( tcs.tuple.concept1, c1 ) == MatchDegree.TYPE
                                        && matcher.levelOfConceptMatch( tcs.tuple.concept2, c2 ) == MatchDegree.TYPE ) {
                                    matched = tcs;
                                }
                            }
                        }
                    }
                    if ( matched == null ) {
                        matched = new TupleCounterpartSet( model, bt );
                        matched.setModel( model );
                        tDistinct.add( matched );
                    }
                    if ( matched != null ) {
                        matched.addMatchedModel( model, bt );       // TODO bug here ... bt is null
                    }

                } else {
                    // Here if it's not a relation
                }
            }
        }
    }

    /**
     * Convenience method to return the number of distinct components appearing
     * in at least k models. As a check, note that getNShared(
     * MMComponentKind.CONCEPT, 1 ) would be the total number of entries in
     * cDistinct
     *
     * @see #getNCShared
     * @see #getNTShared
     *
     */
    public int getNShared( MMComponentKind kind, int k ) {
        switch ( kind ) {
            case CONCEPT:
                return getNCShared( k );
            case RELATION:
                return getNTShared( k );
        }
        return -1;
    }

    /**
     * Count of all concepts that appear (via constrained matching) in at least
     * k models
     *
     * @param k The min number of models where component appears in order to be
     * counted here.
     * @return the number of items whose number of shared models meets the
     * threshhold.
     */
    public int getNCShared( int k ) {

        int count = 0;  // the number of times each eligible concept appeared (>k for each eligible)
        for ( GraphObjectCounterpartSet mgo : cDistinct ) {
//            if ( mgo.getNumMatches() >= k ) 
            if ( mgo.getNumModels() >= k ) 
            {
                count = count + 1;
            }
        }
        return count;
    }

    /**
     * Count of all tuples that appear (via constrained matching) in at least k
     * models
     *
     * @param k The min number of models where component appears in order to be
     * counted here.
     * @return the number of items whose number of shared models meets the
     * threshhold.
     */
    public int getNTShared( int k ) {

        int count = 0;  // the number of times each eligible concept appeared (>k for each eligible)
        for ( GraphObjectCounterpartSet mgt : tDistinct ) {
//            if ( mgt.getNumMatches() >= k ) 
            if ( mgt.getNumModels() >= k ) 
            {
                count = count + 1;
            }
        }
        return count;
    }
    

    /**
     * Convenience method for getting either concept or relation singleton
     * standard deviation, by raw count or by percentage. We use two
     * distinctiveness measures for a given set of models: 
     * <ul><li>standard deviation
     * of the number of singletons - Singleton Count Std Dev - varies from 0 to
     * sqrt num singles</li>
     * <li>standard deviation of the percentage of singletons -
     * Singleton Percentage Std Dev - varies from 0 to 1
     * </li></ul>
     *
     * @param kind which components to gather singletons for
     * @param byPercentage whether to use the percentage of total singletons or
     * the raw count.
     * @return a standard deviation of either a set .
     */
    public double getSingletonStdDev( MMComponentKind kind, boolean byPercentage ) {
        ArrayList<GraphObjectCounterpartSet> distinctOnes = null;
        double[] singletons = null;
        switch ( kind ) {
            case CONCEPT:
                distinctOnes = (ArrayList)cDistinct;
                singletons = singletonsConcepts;
                break;
            case RELATION:
                distinctOnes = (ArrayList)tDistinct;
                singletons = singletonsRelations;
                break;
        }

//        singletons = new double[ models.size() ];     // use double so that std dev will work for all
        for ( int i = 0; i < models.size(); i++ ) singletons[i] = 0.0;
        int singletonTotal = 0;
        Iterator iter = distinctOnes.iterator();
        //for ( GraphObjectCounterpartSet mgo : distinctOnes ) {
        while ( iter.hasNext() ) {
            GraphObjectCounterpartSet mgo = (GraphObjectCounterpartSet)iter.next();
            if ( mgo.getNumModels() == 1 ) {
                singletonTotal += 1;     // keep track of the total number of singletons
                singletons[ models.indexOf( mgo.getModel() )] += 1.0; // keep each model's number of singletons
            }

        }
        
        double[] singletonsCalculated = Arrays.copyOf( singletons, singletons.length );
        if ( byPercentage ) {     // adjust each model value to reflect the percentage of singletons, not the count
            for ( int n = 0; n < singletonsCalculated.length; n++ ) {
                singletonsCalculated[ n] = singletonsCalculated[ n] / singletonTotal; // already a float so no problem here
            }
        }
        return standardDeviation( singletonsCalculated );
    }

    /**
     * Calculates the standard deviation of a set of values, according to
     * well-established definitions
     *
     * @param values the set over which to determine the standard deviation
     * @return the standard deviation in the same units as the values
     */
    private static double standardDeviation( double[] values ) {
        double sum = 0;
        int n;
        for ( n = 0; n < values.length; n++ ) {
            sum += values[ n];
        }
        double average = sum / values.length;
        double sumOfSqDiff = 0; // now it's the sum of the squares of the difference 
        for ( n = 0; n < values.length; n++ ) { // get each difference squared and add that
            sumOfSqDiff += Math.pow( average - (double)values[ n], 2 );
        }
        return Math.sqrt( sumOfSqDiff / n );
    }

    /**
     * Displays each of the tuples that appear in at least k models and shows
     * the number of models and id's that share the object.
     *
     * @param mgts The list of tuples
     * @param level The minimum number of models per tcs that are to be
     * displayed. If level is 1, then all items are displayed, because every
     * item is in at least one model. If level &lt; 1, returns empty string.
     * @return An HTML string listing each tcs and the number of models in which
     * it is shared.
     */
    public String showMatchedGraphTuples( ArrayList<TupleCounterpartSet> mgts, int level ) {
                    // TODO: Needs to update getNumMatches to use getNumModels instead
        String r = Tag.p( "List of models for items in at least " + level + " models:" ) + Tag.ulist;
        for ( TupleCounterpartSet mgt : mgts ) {
            if ( mgt.getNumMatches() >= level ) {
                r = r + Tag.item( ( mgt.getNumMatches() == 1 ? Tag.italic( mgt.model.getID().getUser() ) + ": " : "" )
                        + mgt.tuple.toString() + " = " + mgt.getNumMatches() );
                if ( mgt.getNumMatches() > 1 ) {    // show all the objects in this counterpart set.
                    r += Tag.ulist;
                    for ( int objnum = 0; objnum < mgt.getNumMatches(); objnum++ ) {
                        r += Tag.item( Tag.italic( mgt.matchedModels.get( objnum ).getID().getUser() ) + ": "
                                + mgt.matchedTuples.get( objnum ).toString() );
                    }
                    r += Tag._ulist;
                }
            }
        }
        return r + Tag._ulist;
    }

    /**
     * Displays each of the graph objects that appear in at least k models and
     * shows the number and id's of models that share the object.
     *
     * @param mgos The list of objects
     * @param level The minimum number of models per graph object that are to be
     * displayed. If level is 1, then all items are displayed, because every
     * item is in at least one model. If level &lt; 1, returns empty string.
     * @return An HTML string listing each object and the number of models in
     * which it is shared.
     */
    public String showMatchedGraphObjects( ArrayList<GraphObjectCounterpartSet> mgos, int level ) {
        String r = Tag.p( "Number of models for items in at least " + level + " models:" ) + Tag.ulist;
                // either we're missing one of the counterpart sets or we're neglecting to create one.
        for ( GraphObjectCounterpartSet mgo : mgos ) {
//            if ( mgo.getNumMatches() >= level ) {
            if ( mgo.getNumModels() >= level ) {
                r = r + Tag.item( ( mgo.getNumModels() == 1 ? Tag.italic( mgo.model.getID().getUser() ) + ": " : "" )
//                        + mgo.obj.getTextLabel() + " = " + mgo.getNumMatches() );
                        + mgo.obj.getTextLabel() + " = " + mgo.getNumModels() );
                if ( mgo.getNumModels() > 1 ) {   // just show user and label
//                           // show all the objects in this counterpart set.
                    r += Tag.ulist;
                    for ( int objnum = 0; objnum < mgo.getNumMatches(); objnum++ ) {

                        //                        r += Tag.item( Tag.italic( mgo.matchedModels.get( objnum ).getID().getUser() ) + ": "
                                // TODO: when showing combined models, want to use last event, but otherwise want to use the
                                    // the earlier model event showing what original model was used.
                        ObjectHistoryEvent he = mgo.matchedComponents.get( objnum ).getHistory().getLastEvent();
                        
                                    // For debugging ... LOTS of outout
//                        Global.info( mgo.matchedComponents.get(objnum).toString() + mgo.matchedComponents.get(objnum).getHistory().toString());
                        
                        String modelname = "unknown";
                        if ( he.getType().equals( ObjectHistoryEventType.MODEL) )
                                modelname = ((MMObjectHistoryEvent)he).getModelID().getUser();
                        r += Tag.item( Tag.italic( modelname ) + ": "
                                + mgo.matchedComponents.get( objnum ).getTextLabel() );
                    }
                    r += Tag._ulist;
                }
            }
        }
        return r + Tag._ulist;
    }

    /**
     * Load a set of synonyms for this congruence instance. Note this should be
     * done before generateMetrics to have any impact.
     *
     * @param collections Synonyms to be applied to both concept and tcs matching
     * @see #useSynonyms
     */
    public void setSynonymCollections( ArrayList<ClusterCollection> collections ) {
        this.clusterCollections = collections;
        matcher.setSynonyms( collections );
    }

    private float getCongruenceValueAt( float[] congruences, int level ) {
        if ( level >= congruences.length || level < 0 ) {
            return -1f;
        } else {
            return congruences[ level];
        }
    }

    /**
     * Look up the appropriate congruence value, after generateMetrics has been
     * called.
     *
     * @param kind What kind of congruence value to find
     * @param level What level congruence value to find
     * @return desired congruence value; -1 if out of range or otherwise not
     * found
     */
    public float getCongruence( MMComponentKind kind, int level ) {
        switch ( kind ) {
            case CONCEPT:
                return getCongruenceValueAt( conceptCongruence, level );
            case RELATION:
                return getCongruenceValueAt( relationCongruence, level );
        }
        return -1f;
    }
}
