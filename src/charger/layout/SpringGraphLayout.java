/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger.layout;

import charger.Global;
import charger.obj.*;
import charger.util.CGUtil;
import charger.util.Util;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mm.MMetrics;           // only used for a number format

/**
 * Implements a simple force-directed (spring) layout for Charger. The algorithm
 * treats Charger nodes as nodes in this algorithm, while Charger lines are
 * treated as springs. Calculations and changes are made on the basis of the
 * internally held nodePositions list. The actual graph and nodes aren't
 * supposed to be touched until the end of performLayout. <p> The main principle
 * behind its operation is that graph nodes are subject to two forces: a spring
 * force and a coulomb (electric charge) force. The edges are considered to be
 * springs subject to Hooke's law where the force is attraction when the string
 * is stretched (i.e., length greater than the equilibrium length desired) or
 * repulsion when the spring is compressed to less than the equilibrium length.
 * The nodes are considered to be electric point charges (all with the same
 * positive charge) that repel each other according to Coulomb's
 * reciprocal-of-the-distance law. There is a mass function which generally
 * returns the same for all nodes, but can be adjusted (e.g., for immovable
 * notes) depending on the kind of node. See the mass() method to override.
 * </p><p> In order to handle contexts, the algorithm works in a depth-first
 * recursive way. That is, nested contexts are laid out before their enclosing
 * graph. The context is treated as a point-node, whose mass is equal to the sum
 * of the masses of its constituents. If there is an edge that crosses into the
 * context, a fake "edge" is temporarily created to directly connect the context
 * itself for the purposes of determining the layout. This fake edge is only
 * considered when laying out the outer graph; the context's layout is unaware
 * of any edges that link to the outer context(s). </p><p> The displacement is
 * merely the force interpreted as a position translation operation. </p> For
 * unconnected components (in the graph theory sense), the algorithm will
 * ordinarily cause them to push apart (since there's no spring
 * force to balance), leaving lots of wide open space.
 * Therefore there's a step that identifies unconnected components, and connects
 * each one's center-most node with at least one other center-most node. This
 * ensures that they will have at least one attractive force to help them stay
 * together.
 * <p>
 * Fine tuning of the layout parameters is cruicial to the effective use of the algorithm. 
 * See comments on each of the layout parameters to better understand how it 
 * works.
 * </p>
 * 
 * <p> Future tweaks: 
 * <br>Tell the force calculators to "favor"
 * x-forces so that layouts will tend toward landscape orientation. 
 * <br>Tell the force calculators to "favor" rectilinear placement.
 * 
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class SpringGraphLayout extends GraphLayout {

        /** The "best" edge length. For the spring forces, this is the distance at which the force is zero. */
    private double equilibriumEdgeLength = 40;     // the "best" edge length
        /** How much extra space to provide on the top and left of the final laid out graph. */
    private double borderMargin = this.getEquilibriumEdgeLength(); // how much "extra" space on the top and left
        /** How much padding to provide between the bounding rectangle of the graph/context and its enclosed contents. */
    private float contextInnerPadding = Graph.contextInnerPadding;
        /** How much space this graph is allowed to occupy */
    private Rectangle.Double maxBoundsAvailable = new Rectangle.Double( 0, 0, 0, 0 );
    
        /** When to call it quits on iterating through the algorithm. 
         * Currently set to a user-settable value.
         * @see Global#springLayoutMaxIterations 
         * */
    public static int maxIterations =    Global.springLayoutMaxIterations;

        /** The minimum distance between nodes; if they are actually closer than this,
         * then assume they are at this distance. This is to prevent short distances
         * from overwhelming the repulsion forces for nodes */
    private  double MIN_DISTANCE = 3;
        /** The maximum distance between nodes; if they are farther apart than this,
         * bring them in to this distance. */
    private  double MAX_DISTANCE = 2000;
    
        /** Multiplier for the spring force */
    private static final double DAMPER_FOR_SPRING = 0.2;
        /** Multiplier for the coulomb (repulsion) force */
    private static final double DAMPER_FOR_COULOMB = 3;
        /** Used to further "dampen" the coulomb force. The physical constant
         * would set this to 2.0 (1/distance-squared) but it's set to 2.2 so that
         * node repulsion will drop off faster. */
    private double COULOMB_EXPONENT = 2.2;
    
        /** The minimum energy level that tells the algorithm it can stop. 
         * This value is actually set in performLayout so that it can be tuned
         * for the number of nodes and edges. */
    private  double ENERGY_THRESHHOLD = 0;
        /** To fine-tune the threshhold */
    private  double ENERGY_THRESHHOLD_PER_NODE = 1.0;    // to find tune the threshhold
    
        /** Double-precision values are considered equal if they are within this value of each other */
    private static double EPSILON = 0.1;
        /** Further damping for displacements; currently set to 1.0 */
    private static double DAMPING_FOR_DISPLACEMENTS = 1; //0.5;
        /** Used in determining the spring force using Hooke's law. Currently set to 1.0. 
         * @see SpringGraphLayout#DAMPER_FOR_SPRING */
    private static double SPRING_CONSTANT = DAMPER_FOR_SPRING * 1;
        /** Coulomb constant (numerator) for the distance-squared calculations. 
         * Currently set to Math.pow( equilibriumEdgeLength, COULOMB_EXPONENT ) 
         * @see SpringGraphLayout#COULOMB_EXPONENT
         * */
    private double COULOMB_CONSTANT = DAMPER_FOR_COULOMB * Math.pow( equilibriumEdgeLength, COULOMB_EXPONENT );
    private static final Point2D.Double zeroPoint = new Point2D.Double( 0.0, 0.0 );
//    boolean randomInitialPositions = false;
    /**
     * The arithmetic sign of an attractive force. Forces with this sign are
     * considered attractive forces. Forces with the opposite sign are
     * considered negative forces.
     */
    private int ATTRACTION_FORCE_SIGN = +1;
    /**
     * Set to -1 * the attraction force.
     */
    private int REPULSION_FORCE_SIGN = -1 * ATTRACTION_FORCE_SIGN;
    
    private static DecimalFormat nformat = new DecimalFormat( "##0.000;0" );
    /**
     * Displacement is represented as a "point" where the x value is dx and the
     * y value is dy.
     */
    private HashMap<GNode, Point2D.Double> nodeDisplacements = new HashMap<>();
        /**
     * Keeps track of each node's displacement (movement) at each iteration. 
     */

    private HashMap<GNode, Point2D.Double> nodePositions = new HashMap<>();
    private HashMap<GNode, Point2D.Double> nodeForces = new HashMap<>();
    
    private ArrayList<GNode> nodes = new ArrayList<GNode>();
    
    // contains all the original edges, plus any we have to temporarily create for the contexts.
    private ArrayList<GEdge> edges = new ArrayList<GEdge>();

    public SpringGraphLayout( Graph g ) {

        super( g );
        scanAndLoadGraph();
    }
    
    /**
     * Initialize the node and edges lists.
     * Treats the outer graph only after it has recursively called itself on
     * the inner graphs.
     */
    protected void scanAndLoadGraph() {
        ShallowIterator contextIterator = new ShallowIterator( graph, GraphObject.Kind.GRAPH );
        while ( contextIterator.hasNext() ) {
            Graph context = (Graph)contextIterator.next();
            // Note this call moves the original nodes in the context
            boolean b = performLayoutOnContext( context );
//                Global.info( "New displayRect for context " + context.getTextLabel() + ": " + context.getDisplayRect() );
        }

        ShallowIterator iter = new ShallowIterator( graph, GraphObject.Kind.GNODE );
        while ( iter.hasNext() ) {
            GraphObject go = (GraphObject)iter.next();
//            if ( go instanceof Graph ) continue;
            nodes.add( (GNode)go );
            nodeForces.put( (GNode)go, new Point2D.Double( 0.0, 0.0 ) );
            nodeDisplacements.put( (GNode)go, new Point2D.Double( 0.0, 0.0 ) );
//            }
        }
        if ( nodesInLine() ) {
            if ( verbose ) {
                Global.info( "Nodes in line = " + true );
            }
            // Here to adjust locations of nodes so they are not in line
            randomizePositions();
        }
        loadNodePositions();

        loadEdges();
    }

    /**
     * Using the original graph nodes, determines whether, within EPSILON, the
     * nodes are in line, either along an x-value or a y-value. Both of these
     * constitute pathological cases, and the solution is to slightly alter some
     * of their positions.
     *
     * @return true if the centers are within EPSILON of the same value (either
     * x or y); false otherwise.
     */
    protected boolean nodesInLine() {
        if ( nodes.size() == 0 ) {
            return false;
        }
        // "seed" with the first object's values
        double x = ( (GNode)nodes.get( 0 ) ).getCenter().x;
        double y = ( (GNode)nodes.get( 0 ) ).getCenter().y;
        boolean xinline = true;
        boolean yinline = true;
        for ( GNode node : nodes ) {
            Point2D.Double center = node.getCenter();
            if ( Math.abs( center.x - x ) > 2 * EPSILON ) {
                xinline = false;
            }
            if ( Math.abs( center.y - y ) > 2 * EPSILON ) {
                yinline = false;
            };
        }
        return xinline || yinline;
    }

    /**
     * Not really random; moves each node one pixel right and down from the
     * previous node. Warning: modifies the original graph!
     */
    protected void randomizePositions() {
        int ran = 0;
        for ( GNode node : nodes ) {

//            if ( verbose ) Global.info( "node " + node.getTextLabel() + " before pos " + node.getCenter() );
            node.setCenter( new Point2D.Double( node.getCenter().x + ran++, node.getCenter().y + ran++ ) );
//            if ( verbose ) Global.info( "node " + node.getTextLabel() + "  after pos " + node.getCenter() );
        }
    }

    /**
     * convenience method for re-setting the hashmaps after each relaxation.
     *
     * @param points
     */
    private void zeroOutVectors( HashMap<GNode, Point2D.Double> points ) {
        for ( GNode node : points.keySet() ) {
            points.put( node, new Point2D.Double( 0, 0 ) );
        }
    }

    /**
     * Perform the layout operation on the graph. Does not update the original
     * graph, just in case there's an error.
     *
     * @see #copyToGraph()
     */
    public boolean performLayout() {
        Global.info( "Starting with " + nodes.size() + " nodes and " + edges.size() + " edges..." );

        int numRelaxations = 0;
                        // Try to adjust the max iterations and constants for the number of nodes
        int numNodes = nodes.size();
        int numEdges = edges.size();
        this.ENERGY_THRESHHOLD = Math.sqrt( numNodes + numEdges ) * ENERGY_THRESHHOLD_PER_NODE; 
//        this.ENERGY_THRESHHOLD = Math.pow( numNodes, 1.2 ) * ENERGY_THRESHHOLD_PER_NODE; 
//        this.ENERGY_THRESHHOLD =  (numNodes + numEdges) * ENERGY_THRESHHOLD_PER_NODE; 

        double total_energy = ENERGY_THRESHHOLD * 2;  // some non-zero number > ENERGY_THRESHHOLD
        double previous_energy = 0;
        double minenergy = Double.MAX_VALUE;
        double maxenergy = Double.MIN_VALUE;
        double minchange = Double.MAX_VALUE;
        double maxchange = Double.MIN_VALUE;
        int numIncreases = 0;
        try {
            while ( total_energy /* / nodes.size() */ > ENERGY_THRESHHOLD && numRelaxations < maxIterations ) {
                numRelaxations++;
                previous_energy = total_energy;
//             Global.info( "\n          Relax " + numRelaxations + " starting... ");
                total_energy = performOneRelaxation();
//                if ( verbose ) {
                    if ( total_energy < minenergy ) minenergy = total_energy;
                    if ( total_energy > maxenergy ) maxenergy = total_energy;
                    double change = total_energy - previous_energy;
                    if ( total_energy < minchange ) minchange = total_energy;
                    if ( total_energy > maxchange ) maxchange = total_energy;
                    if ( total_energy > previous_energy ) numIncreases++;
                    double changepct = ( total_energy - previous_energy ) / previous_energy;

//                        Global.info( "Relax " + numRelaxations + " - total_kinetic_energy: "
//                                + nformat.format( total_energy )
//                                + " Change: " + nformat.format( change )
//                                + " (" + nformat.format( 100 * ( changepct ) )
//                                + " %)" );
//                }
                moveToUpperLeft( true );

            }
        } catch ( StackOverflowError ex ) {
            Global.info( "Sorry graph too big. Stopping here." );
            ex.printStackTrace();
            return false;
        }
//            copyToGraph();
        Global.info( "With " + nodes.size() + " nodes and " + edges.size() + " edges, layout used " + numRelaxations + " iterations." );
        Global.info ("  Energy: decreases: " + (numRelaxations - numIncreases) 
                + " (" + nformat.format( 100.0*(double)(numRelaxations - numIncreases)/(double)numRelaxations ) + " %)"
                + ", min: " + nformat.format( minenergy) + ", max: " +  nformat.format( maxenergy) );
        
        return true;
    }

    /**
     * One iteration of the algorithm, starting with zero forces all around.
     * If verbose is set, then will announce some of its progress along the way.
     * calculated.
     * @return total energy for the collection of nodes for this iteration
     */
    public double performOneRelaxation() {
        zeroOutVectors( nodeForces );
        zeroOutVectors( nodeDisplacements );
        if ( verbose ) {
//              showAllForces( "at start...");
            Global.info( "\nRELAX Phase 1: Calculate coulomb forces." );
        }
        addNewCoulombForces();
        if ( verbose ) {
            showAllForces( "after the coulomb calculations..." );
            Global.info( "\nRELAX Phase 2: Calculate spring forces." );
        }
        addNewSpringForces();
        if ( verbose ) {
            showAllForces( "after the spring calculations..." );
            Global.info( "\nRELAX Phase 3: Calculate displacements and make new positions." );
        }
        return makeNewPositions();
    }

    /**
     * Invokes the spring algorithm recursively on any nested graph. Takes its
     * parameters from the caller, except of course for the bounds of the
     * context. Updates the original nodes inside the context, and adjusts its
     * size, but leaves the position to be determined by its enclosing graph's
     * layout. Does not yet handle undisplayed contexts, which are probably
     * going to fail here.
     *
     * @param g the context to be laid out
     */
    public boolean performLayoutOnContext( Graph g ) {
        SpringGraphLayout layout = new SpringGraphLayout( g );
        layout.setVerbose( verbose );
        layout.setEquilibriumEdgeLength( equilibriumEdgeLength );
        // For empty contexts, will need to let the algorithm find the right bounds for us
        // propose a very large bounds, with same upper left corner
        Rectangle2D.Double largeBounds = new Rectangle2D.Double(
                g.getDisplayRect().x, g.getDisplayRect().y,
                this.getMaxBoundsAvailable().width - g.getRegionAvailableForContent().x,
                this.getMaxBoundsAvailable().height - g.getRegionAvailableForContent().y );
        layout.setMaxBoundsAvailable( largeBounds );      // use a huge size, but later set display rect to the displayed bounds only
//        layout.setMaxBounds( this.inferCurrentDisplayRect( g ) );
//        layout.setMaxBounds( Util.make2DDouble( g.getBounds()) );
        layout.setBorderMargin( contextInnerPadding );
        layout.performLayout();
        layout.copyToGraph();
        g.setRegionAvailableForContent( g.getContentBounds());
        // commented 09-17-2014
//        Rectangle2D.Double newRect = g.getDisplayBounds();
//        CGUtil.grow( newRect, contextInnerPadding, contextInnerPadding );

//        g.setDisplayRect( newRect  );
//            if ( verbose ) 
        Global.info( "after context layout; display rect of " + g.getTextLabel() + " = " + g.getDisplayRect() );
        return true;
    }

    /**
     * From the existing forces vectors, calculate a displacement for each node,
     * assume a unit time interval and calculate a new position for each node.
     * Does NOT actually change the positions of the graph nodes.
     *
     * @return total energy of the displacements times the masses added up.
     */
    public double makeNewPositions() {
        double totalEnergy = 0;

        // Now that we have the forces for every node, apply them and get new displacements and positions.
        for ( GNode gn : nodes ) {
//                    showDisplacements( "before adding netforce ");
            nodeDisplacements.get( gn ).x += nodeForces.get( gn ).x * DAMPING_FOR_DISPLACEMENTS / mass( gn );
            nodePositions.get( gn ).x += nodeDisplacements.get( gn ).x;
            nodeDisplacements.get( gn ).y += nodeForces.get( gn ).y * DAMPING_FOR_DISPLACEMENTS / mass( gn );
            nodePositions.get( gn ).y += nodeDisplacements.get( gn ).y;

            // total energy = length of displacement vector, use Pythogorean theorem
            totalEnergy += Math.sqrt( Math.pow( nodeDisplacements.get( gn ).x * mass( gn ), 2 )
                    + Math.pow( nodeDisplacements.get( gn ).y * mass( gn ), 2 ) );
//                    Global.info( "node " + thisnode.getTextLabel() + " total energy " + totalEnergy );

//            showDisplacements( "after adding netforce " );
        }

        return totalEnergy;     // for now force quit
    }

    /**
     * For each node in the graph, calculate the new coulomb force vectors (as
     * "points") and add them to the nodeForces list.
     */
    public void addNewCoulombForces() {
        for ( int sourceNodeNum = 0; sourceNodeNum < nodes.size(); sourceNodeNum++ ) {
//            Point2D.Double netForce = new Point2D.Double( 0, 0 );
            GNode sourceNode = nodes.get( sourceNodeNum );
//            if ( verbose ) {
//                Global.info( "NODE focus number " + nodenum1 + " " + thisnode.getTextLabel() );
//            }
            Point2D.Double nodeCoulombTotal = new Point2D.Double( 0.0, 0.0 );
            for ( int targetNodeNum = 0; targetNodeNum < nodes.size(); targetNodeNum++ ) {
                if ( sourceNodeNum != targetNodeNum ) {       // Don't try to add infinite repulsion with itself!
                    GNode targetNode = nodes.get( targetNodeNum );
                    Point2D.Double coulombForce = coulomb_force( sourceNode, targetNode );
                    if ( verbose ) {
                        Global.info( "From node: " + sourceNode.getTextLabel()
                                + " to " + targetNode.getTextLabel() + " coulomb force " + showForce( coulombForce ) );
                    }
                    nodeCoulombTotal.x += coulombForce.x;       // the force acting on thisnode
                    nodeCoulombTotal.y += coulombForce.y;
//                    if ( verbose ) {
//                        Global.info( "Node \"" + thisnode.getTextLabel() + "\" net force " + showForce( netForce ) );
//                    }
                }
//                if ( verbose ) Global.info( "Node \"" + thisnode.getTextLabel() + "\"" + showForce( nodeCoulombTotal ) );
                // add these forces to the current forces for the node
            }
            addForceToNodeForces( nodeCoulombTotal, sourceNode );
        }
    }

    public void addNewSpringForces() {
//        Point2D.Double nodeSpringTotal = new Point2D.Double( 0.0, 0.0 );

//            ArrayList<GEdge> edges = thisnode.getEdges();
//        DeepIterator edges = new DeepIterator( graph, GraphObject.Kind.GEDGE );
        Iterator<GEdge> iter = edges.iterator();

//            for ( GEdge e : edges ) {
        while ( iter.hasNext() ) {
//            nodeSpringTotal = new Point2D.Double( 0.0, 0.0 );
            GEdge e = (GEdge)iter.next();
            Point2D.Double springForce = spring_force( e, true );
            if ( verbose ) {
                Global.info( "From node: " + e.fromObj.getTextLabel()
                        + " to " + e.toObj.getTextLabel() + " spring force " + showForce( springForce ) );
            }
            if ( !springForce.equals( zeroPoint ) ) {
//        if ( verbose ) {
////            Global.info( "net node force " + showForce( netForce ) );
//            Global.info( " node spring total " + showForce( nodeSpringTotal ) );
//        }
                addForceToNodeForces( springForce, e.fromObj );

                springForce = spring_force( e, false );
                addForceToNodeForces( springForce, e.toObj );
            }
        }
    }

// loop
//     total_energy := 0 // running sum of total kinetic energy over all particles
//     for each node
//         net-force := (0, 0) // running sum of total force on this particular node
//         
//         for each other node
//             net-force := net-force + Coulomb_repulsion( this_node, other_node )
//         next node
//         
//         for each spring connected to this node
//             net-force := net-force + Hooke_attraction( this_node, spring )
//         next spring
//         
//         // without damping, it moves forever
//         this_node.displacement :=  net-force * damping
//         this_node.position := this_node.position + timestep * this_node.displacement
//         total_energy := total_energy + this_node.mass * (this_node.displacement)^2
//     next node
// until total_energy is less than some small number  // the simulation has stopped moving
    /**
     * Given two nodes, calculates the coulomb force that repels them. The sign
     * of this force is always -1 * ATTRACTIVE_FORCE_SIGN. Distances of less
     * than MIN_DISTANCE are calculated as though they were at MIN_DISTANCE
     * apart.
     *
     * @param thisnode The node from which the other is being repelled
     * @param other
     * @return the repulsive force between the nodes
     */
    public Point2D.Double coulomb_force( GNode thisnode, GNode other ) {
        Point2D.Double thisF = nodePositions.get( thisnode );
        Point2D.Double otherF = nodePositions.get( other );
//        Point2D.Double thisF = thisnode.getCenter();
//        Point2D.Double otherF = other.getCenter();
        Point2D.Double pos1 = new Point2D.Double( thisF.x, thisF.y );
        Point2D.Double pos2 = new Point2D.Double( otherF.x, otherF.y );
        double distance = pos1.distance( pos2 );
        distance = this.getClippedLength( thisnode, other );
        if ( distance < MIN_DISTANCE ) {
            distance = MIN_DISTANCE;
        }
        double force = REPULSION_FORCE_SIGN * mass( thisnode ) * mass( other ) * 
                COULOMB_CONSTANT / Math.pow( Math.abs(distance), COULOMB_EXPONENT ); // negate because we're being pushed away
                // Make sure to use Math.abs here because negative numbers raised to non-integer powers returns NaN

        Point2D.Double result = getXYForce( force, pos1, pos2 );
//        result = reverseForce( result );
//        if ( verbose ) {
//            Global.info( "Node \"" + thisnode.getTextLabel() + "\" to node \"" + other.getTextLabel()
//                    + "\" -- repulsion force is " + showForce( result ) );
//        }
        return result;
    }

    /**
     * Models the Hooke's law spring force along edges. Unlike the coulomb
     * force, which is always repulsive, this force can be attractive or
     * repulsive, depending on whether the spring is compressed (repulsive) or
     * extended (attractive). Considered as the force between the from object
     * and the to object
     *
     * @param edge The edge whose forces are to be determined
     * @param fromTo Whether to calculate force from the from object to the to
     * object or vice versa
     * @return an x,y pair that represent the force relative to that node
     */
    public Point2D.Double spring_force( GEdge edge, boolean fromTo ) {
        // Difference from equilibrium
        // Another bug found! edge.getLength() shouldn't be used, because it does not use the nodePositions and so gets out of sync
        // by using the ORIGINAL edge's nodes' positions
        GNode fromObj = (GNode)edge.fromObj;
        GNode toObj = (GNode)edge.toObj;

//        Point2D.Double fromPt = new Point2D.Double( (double)edge.fromObj.getCenter().x, (double)edge.fromObj.getCenter().y );
//        Point2D.Double toPt = new Point2D.Double( (double)edge.toObj.getCenter().x, (double)edge.toObj.getCenter().y );
        Point2D.Double fromPt = nodePositions.get( fromObj );
        Point2D.Double toPt = nodePositions.get( toObj );

        // This will occur when there's a context. 
        if ( fromPt == null || toPt == null ) {
            return new Point2D.Double( 0.0, 0.0 );
        }

//        double length = fromPt.distance( toPt );
        double length = this.getClippedLength( fromObj, toObj );

        double displacement = length - equilibriumEdgeLength;
        // If displacement is positive (spring is stretched), should be attractive force
//        double force = ATTRACTION_FORCE_SIGN * SPRING_CONSTANT * displacement;
        double force = ATTRACTION_FORCE_SIGN * SPRING_CONSTANT * 
                Math.signum(displacement) * Math.pow( Math.abs(displacement), 0.75) ;

        Point2D.Double result = null;
        if ( fromTo ) {
            result = getXYForce( force, fromPt, toPt );
        } else {
            result = getXYForce( force, toPt, fromPt );
        }
//        if ( verbose ) {
//            Global.info( "Edge -- spring force is " + showForce( result ) );
//        }
        return result;

    }

    /**
     * Allows the algorithm to adjust for varying "sizes" of nodes. For example,
     * the note shouldn't move much, so it gets a very low mass.
     *
     * @param gn
     * @return the computed "mass" for an object - 1.0 for regular nodes, 0.1
     * for a note, so that it doesn't move much.
     */
    public double mass( GNode gn ) {
        if ( gn instanceof Note ) {
            return 0.1;
        } else if ( gn instanceof Graph ) {
            return 1 + 0.5 * ( (Graph)gn ).getGraphObjects().size();
        }
        return 1.0;
    }

    public void addForceToNodeForces( Point2D.Double force, GraphObject node ) {
        Point2D.Double newForce = nodeForces.get( node );
        newForce.x += force.x * mass( (GNode)node );
        newForce.y += force.y * mass( (GNode)node );;
        nodeForces.put( (GNode)node, newForce );
    }

    /**
     * Makes sure there are no negative-coordinate positions. Adjusts the
     * positions as found in the nodePositions list. Does not alter the original
     * graph. Allows for an equilibrium edge length margin on the top and left.
     * If this is a nested graph (i.e., ownergraph != null) then take into account
     * the context label during the y-calculation.
     *
     * @param force Whether to force the graph into the upper left corner
     * whether it already fits or not.
     */
    public void moveToUpperLeft( boolean force ) {

//        Rectangle2D.Double currentRect = this.getPositionBounds();
        Rectangle2D.Double currentRect = this.getDisplayRectBounds();

        if ( !force && getMaxBoundsAvailable().contains( currentRect ) ) {
            return;
        }

        double diffx, diffy, textheight = 0.0;

        
        diffx = getMaxBoundsAvailable().x + borderMargin - currentRect.x;
        if ( graph.getOwnerGraph() != null ) textheight = graph.getTextLabelSize().getHeight();
        diffy = getMaxBoundsAvailable().y + borderMargin + textheight - currentRect.y;

//        if ( verbose ) {
//            Global.info( "at moveToUpperLeft, current display bounds of graph " + currentRect );
//            Global.info( "  moveToUpperLeft, max bounds is " + getMaxBoundsAvailable() );
//            Global.info( "  moveToUpperLeft; shift by x = " + diffx + ";  y = " + diffy );
//        }
//        showPositions( "Before moving to upper left...");
        for ( GNode gn : nodePositions.keySet() ) {  // move all the points  in the node positions list
            Point2D.Double newpos = new Point2D.Double(
                    (double)( nodePositions.get( gn ).x + diffx ),
                    (double)( nodePositions.get( gn ).y + diffy ) );
            nodePositions.put( gn, newpos );
        }
//                showPositions( "After shifting...");
    }

    /**
     * Calculates both the x and y components of a force between two points. The
     * direction of the force is considered to be from the source to the pushed
     * one ... that is, if the force is positive in that direction, the sign of
     * each component will depend on their geometric alignment to the original
     * force. Forces are with respect to the Java coordinate system; i.e., if
     * the y force is positive, it is pushed downward, not upward.
     *
     * @param force
     * @param source
     * @param dest
     * @return a point containing x,y values representing the force's horizontal
     * and vertical components respectively.
     */
    public static Point2D.Double getXYForce( double force, Point2D.Double source, Point2D.Double dest ) {
        double xForce;
        double yForce;

        double dx, dy;  // used for calculating the angle between the force and the x-axis
        double theta;   // angle between x-axis and the force

        // convert y values to Cartesian from the Java coordinate system.
        // BUG fixed: this formerly modified the original points!!!

        double sourcey = -1 * source.y;
        double desty = -1 * dest.y;
//        source.y *= -1;
//        dest.y *= -1;

        dx = dest.x - source.x;
        dy = desty - sourcey;

//                Global.info( "dy/dx " + dy + "/" + dx  );

        // Handle boundary conditions for vertical forces
        if ( Math.abs( dx ) < EPSILON ) {
            if ( dy > 0 ) {
                theta = Math.PI / 2;
            } else {
                theta = 3 * Math.PI / 2;
            }
        } else {
            theta = Math.atan( dy / dx );       // don't worry about divide by zero b/c we already checked if dx < EPSILON
            if ( dest.x < source.x ) {
//                if ( dest.y < source.y ) {
                theta = Math.PI + theta;
//                } 
            }
        }

        if ( theta < 0 ) {
            theta += 2 * Math.PI;
        }
        // since atan returns only values from -PI/2 to PI/2, correct for the left quadrants

//        System.out.println( "THETA " + Math.toDegrees( theta ) );

        xForce = force * Math.cos( theta );
        if ( Math.abs( xForce ) < EPSILON ) {
            xForce = 0;
        }
        // Convert Cartesian y value to Java's y value.
        yForce = -1 * force * Math.sin( theta );
        if ( Math.abs( yForce ) < EPSILON ) {
            yForce = 0;
        }

//        if ( Double.isNaN(xForce) ) {
//            Global.info( "xForce NaN: source, dest are: " + source + "  " + dest );
//        }
//        if ( Double.isNaN(yForce) ) {
//            Global.info( "yForce NaN: source, dest are: " + source + "  " + dest );
//        }
        return new Point2D.Double( xForce, yForce );
    }

    /**
     * Using the nodes list, get the actual positions from the original objects
     * and load up nodePositions list.
     */
    public void loadNodePositions() {
        if ( nodes.isEmpty() ) {
            return;
        }
        for ( GNode node : nodes ) {
            nodePositions.put( node, new Point2D.Double( (double)node.getCenter().x, (double)node.getCenter().y ) );
        }
    }

    /**
     * Initializes the edges list. If an edge is connected to an object that is
     * NOT in the node list, that means it must be in a node that is nested in a
     * context, whose internal content is not handled at this level. In order to
     * account for the edge forces, we create an invisible edge that links to
     * the context rather than the internal node. This should not affect copying
     * to the graph, since edges aren't copied, but simply used to determine
     * forces on the nodes.
     */
    public void loadEdges() {
        ShallowIterator iter = new ShallowIterator( graph, GraphObject.Kind.GEDGE );
        // add the regular eges
        while ( iter.hasNext() ) {
            GEdge edge = (GEdge)iter.next();
            // if both of the edge's ends are  in this graph, then no problem in adding it
            if ( edge.fromObj.getOwnerGraph() == graph && edge.toObj.getOwnerGraph() == graph ) {
                edges.add( edge );
            } else {
                GraphObject fromObj = null;
                GraphObject toObj = null;
                //  one of the nodes is not in this graph. Make up a new edge that connects the edge
                // to the an enclosing context that DOES have the same owner.
                // first, find out which end needs to be faked
                if ( edge.toObj.getOwnerGraph() != graph ) {
                    fromObj = edge.fromObj;
                } else {
                    toObj = edge.toObj;
                }
                // next, find the context that is owned by the top-level graph
                GraphObject nestedNode = ( fromObj == null ) ? edge.fromObj : edge.toObj;
                GraphObject nonNestedNode = ( toObj == null ) ? edge.fromObj : edge.toObj;
                Graph owner = nestedNode.getOwnerGraph();
                while ( owner.getOwnerGraph() != graph ) {
                    owner = owner.getOwnerGraph();
                }
                // finally, add a new edge between the context and 
                // the outer context that's owned by the top level graph
                CustomEdge tempEdge = new CustomEdge( (GraphObject)owner, nonNestedNode );
                graph.insertInCharGerGraph( tempEdge );
                edges.add( tempEdge );
                // Note that while the edge is fake, the object (context) is valid
            }
        }

        // Now, add any custom edges between unconnected components
        addEdgesFromUnconnectedComponents();

    }

    /**
     * Add custom edges between unconnected graph components.
     */
    public void addEdgesFromUnconnectedComponents() {
        ArrayList<GNode> centerNodes = new ArrayList<>();
        ArrayList< ArrayList<GNode>> nodeLists = CGUtil.getConnectedComponents( graph );
        if ( nodeLists.size() == 1 ) {
            return;        // if all connected, yay!
        } else {
            // for each component, find its display rect, then get its center node
            for ( ArrayList<GNode> list : nodeLists ) {
                Point2D.Double centerPt = CGUtil.getCenterPoint( list );
                GNode centerNode = CGUtil.closestObject( list, centerPt );
                if ( centerNode != null ) {
                    centerNodes.add( centerNode );
                }
            }
            GNode previousNode = null;
            for ( GNode node : centerNodes ) {
                if ( previousNode != null ) {
                CustomEdge tempEdge = new CustomEdge( (GraphObject)node, previousNode );
                graph.insertInCharGerGraph( tempEdge );
                edges.add( tempEdge );
                }
                previousNode = node;
            }
        }
    }

    /**
     * Update the actual graph to reflect the positions calculated by the
     * layout. Also deletes the custom edges which could cause problems if left
     * behind. algorithm. Repositions the graph to the upper left corner.
     */
    public synchronized void copyToGraph() {
        for ( GEdge edge : edges ) {
            if ( edge instanceof CustomEdge ) {
                        // WARNING! Need to un-comment for production!
                edge.forgetObject();
            }
        }
        moveToUpperLeft( true );
        for ( GNode gn : nodePositions.keySet() ) {
            Point2D.Double oldCenter = gn.getCenter();
            Point2D.Double pos = nodePositions.get( gn );
            Point2D.Double translationVector = new Point2D.Double( pos.x - oldCenter.x, pos.y - oldCenter.y );
            if ( !( gn instanceof Graph ) ) {
//                gn.setCenter( new Point2D.Double( pos.x, pos.y ) );
                gn.forceMove( translationVector );
            } else {
                ((Graph)gn).forceDeepMove( translationVector );
//                ( (Graph)gn ).moveGraph( translationVector );
            }
            gn.adjustEdges();
        }
//        if (verbose) 
//            Global.info( "after copyToGraph, graph is\n" + CGXGenerator.generateXML( graph ));
    }

    private void showAllForces( String s ) {
        Global.info( "show forces " + s );
        for ( GNode gn : nodeForces.keySet() ) {
            Global.info( "node " + gn.getTextLabel() + " forces " + showForce( nodeForces.get( gn ) ) );
        }
    }

    private void showDisplacements( String s ) {
        Global.info( "show displacements " + s );
        for ( GNode gn : nodeDisplacements.keySet() ) {
            Global.info( "node " + gn.getTextLabel() + " displacement " + showForce( nodeDisplacements.get( gn ) ) );
        }
    }

    private void showPositions( String s ) {
        Global.info( "show positions " + s );
        for ( GNode gn : nodePositions.keySet() ) {
            Global.info( "node " + gn.getTextLabel() + " position: " + showPoint( nodePositions.get( gn ) ) );
        }
    }

    private String showPoint( Point2D.Double point ) {
        return "x = " + nformat.format( point.x ) + ",  y = " + nformat.format( point.y );
    }

    public static String showForce( Point2D.Double p ) {
        String updown = p.y >= 0 ? "down" : "up";
        if ( Math.abs( p.y - 0 ) < EPSILON ) {
            updown = "straight";
        }
        String leftright = p.x >= 0 ? "right" : "left";
        if ( Math.abs( p.x - 0 ) < EPSILON ) {
            leftright = "straight";
        }
        double force = Math.sqrt( Math.pow( p.x, 2 ) + Math.pow( p.y, 2 ) );
        return "Total: " + MMetrics.nformat.format( force ) + ", x: " + nformat.format( p.x )
                + ", y: " + nformat.format( p.y ) + " " + updown + " " + leftright;
    }

    public static Point2D.Double reverseForce( Point2D.Double force ) {
        return new Point2D.Double( -1 * force.x, -1 * force.y );
    }

    /**
     * Find the bounds of all the node positions (not the original graph).
     *
     * @param displayRects whether to consider the displayrects (an
     * approximation for the shapes)
     * @return the bounds all added up
     */
    protected Rectangle2D.Double getBounds( boolean displayRects ) {
        Rectangle2D.Double currentRect = null;

        new Rectangle2D.Double( 0, 0, 0, 0 );

//        for ( Point2D.Double pos : nodePositions.values() ) {
        for ( GNode node : nodes ) {
            if ( currentRect == null ) {
                currentRect = new Rectangle2D.Double( nodePositions.get( node ).x, nodePositions.get( node ).y,
                        nodePositions.get( node ).x, nodePositions.get( node ).y );
            }
            if ( displayRects ) {
                currentRect.add( inferCurrentDisplayRect( node ) );
            } else {
                currentRect.add( nodePositions.get( node ) );
            }
        }
        return currentRect;
    }

    /**
     * Find the bounds of all the node positions (not the original graph).
     *
     * @return a rectangle that encloses all the 
     * currently-calculated center points of the objects in the graph.
     */
    protected Rectangle2D.Double getPositionBounds() {
        return getBounds( false );
    }

    /**
     * Find the bounds of all the node display rectangles (using the original
     * graph).
     *
     * @return a rectangle that encloses all the 
     * currently-calculated display rectangles of the objects in the graph.
     */
    protected Rectangle2D.Double getDisplayRectBounds() {
        return getBounds( true );
    }

    /**
     * Uses the height and width of the node's display rectangle, but infers
     * (from its new proposed center point) the rectangle it would form in its
     * nodePositions location. Don't use the original display rect, because
     * we've moved its center.
     *
     * @param node
     * @return what the layout thinks will be the displayrect of the node 
     * if the layout stops right now.
     */
    private Rectangle2D.Double inferCurrentDisplayRect( GNode node ) {
        double width = node.getDisplayRect().getWidth();
        double height = node.getDisplayRect().getHeight();
        Point2D.Double center = nodePositions.get( node );
        // when node is an enclosing graph, its center point isn't in the list.
        if ( center == null ) {
            center = Util.make2DDouble( node.getCenter() );
        }
//        if ( center == null ) {
//            center.x = node.getDisplayRect().getX();
//            center.y = node.getDisplayRect().getY();
//        }
        double x = center.x - width / 2;
        double y = center.y - height / 2;
        return new Rectangle2D.Double( x, y, width, height );
    }

    /**
     * Considers the shape (from the original graph) returns the distance
     * between the clipping points of a center-to-center line between the
     * objects. Uses the nodePositions list, not the original graph objects'
     * positions.
     * There is a problem with this approach -- what if the two
     * nodes' rectangles overlap? That would mean that the clipped length might
     * be negative.
     */
    public double getClippedLength( GNode node1, GNode node2 ) {
        Point2D.Double pos1 = Util.make2DDouble( nodePositions.get( node1 ) );
        Point2D.Double pos2 = Util.make2DDouble( nodePositions.get( node2 ) );
        //First see if the shapes overlap -- if they do, then return a default length
        Rectangle2D.Double node1Rect = inferCurrentDisplayRect( node1 );
        Rectangle2D.Double node2Rect = inferCurrentDisplayRect( node2 );
        Area area = new Area( node1Rect );
        area.intersect( new Area( node2Rect ) );
        if ( !area.isEmpty() ) {        // they overlap
            return MIN_DISTANCE;
        }

        Point2D.Double clipPointTo =
                Util.make2DDouble( GEdge.findClippingPoint( pos1, pos2, node2Rect ) );
        Point2D.Double clipPointFrom =
                Util.make2DDouble( GEdge.findClippingPoint( pos2, pos1, node1Rect ) );
        
        if ( Double.isNaN( clipPointTo.x ) || Double.isNaN( clipPointTo.y ) || Double.isNaN( clipPointFrom.x ) || Double.isNaN( clipPointFrom.y )) {
            int a = 0;
        } 

                //        Point2D.Double clipPointTo = 
//                Util.make2DDouble( GEdge.findClippingPoint(  pos1, pos2, node2.getShape() ));
//        Point2D.Double clipPointFrom = 
//                Util.make2DDouble( GEdge.findClippingPoint(  pos2, pos1, node1.getShape() ));

//        Point2D.Double clipPointFrom = GEdge.findClippingPoint( clipPointTo, (float)nodePositions.get(node1), node1.getShape());
        double distance = clipPointFrom.distance( clipPointTo );
        if ( Double.isNaN( distance) || Double.isInfinite( distance) || distance > MAX_DISTANCE ) {
            distance = MAX_DISTANCE;
        }
        return distance;
    }

    /**
     * Find out the maximum size of the area in which nodes can be placed.
     *
     * @return the rectangle
     */
    public Rectangle2D.Double getMaxBoundsAvailable() {
        return maxBoundsAvailable;
    }

    public void setMaxBounds( Rectangle rect ) {
        setMaxBoundsAvailable( new Rectangle2D.Double( rect.x, rect.y, rect.width, rect.height ) );
    }

    public void setMaxBoundsAvailable( Rectangle2D.Double maxBoundsAvailable ) {
        this.maxBoundsAvailable = maxBoundsAvailable;
    }

    /**
     * The preferred length of an edge to be sought by the algorithm.
     *
     * @return the preferred length of an edge to be sought by the algorithm
     */
    public double getEquilibriumEdgeLength() {
        return equilibriumEdgeLength;
    }

    public void setEquilibriumEdgeLength( double equilibriumEdgeLength ) {
        this.equilibriumEdgeLength = equilibriumEdgeLength;
        COULOMB_CONSTANT = DAMPER_FOR_COULOMB * Math.pow( equilibriumEdgeLength, 2.3 );
    }

    public double getBorderMargin() {
        return borderMargin;
    }

    public void setBorderMargin( double borderMargin ) {
        this.borderMargin = borderMargin;
    }
}
