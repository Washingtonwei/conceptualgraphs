package charger.act;

import charger.*;
import charger.EditingChangeState.EditChange;
import charger.obj.*;
import charger.exception.*;
import charger.util.*;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import javax.swing.*;

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
 * Implements the actor updating ("firing") mechanism of conceptual graphs. In
 * general, is activated by a referent change somewhere in a graph.
 *
 * @see CanvasPanel#userFinishedEditingText
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach.
 */
public class GraphUpdater implements Runnable {

    private Graph GraphToUpdate = null;
    private GNode GNodeToUpdate = null;
    private GraphObject GraphObjectToUpdate = null;
    /**
     * Contains an entry for each actor, specifying its input concepts in order
     */
    public static Hashtable InputSignatures = new Hashtable();
    /**
     * Contains an entry for each actor, specifying its input concepts in order
     */
    public static Hashtable OutputSignatures = new Hashtable();
    /**
     * Contains an entry for each actor consisting of a ArrayList with
     * properties we care about. Valid properties (all strings) are: <table
     * width="100%" cellspacing="5"> <tr><td width="80" align="right"
     * valign="top"><strong><font size="-1" face="Courier New, Courier,
     * mono">"executable" </font></strong> <td width="75%"> actor has an
     * executable definition, either built-in or defined <tr><td width="80"
     * align="right" valign="top"><strong><font size="-1" face="Courier New,
     * Courier, mono">"commutative" </font></strong> <td width="75%"> order of
     * actor's inputs and outputs does not matter <tr><td width="80"
     * align="right" valign="top"><strong><font size="-1" face="Courier New,
     * Courier, mono">"primitive"</font></strong> <td width="75%"> if a
     * primitive, to be executed by GraphUpdater; otherwise, look for plugin
     * <tr><td width="80" align="right" valign="top"><strong><font size="-1"
     * face="Courier New, Courier,
     * mono">"varyingInputCardinality"</font></strong> <td width="75%"> number
     * of inputs may vary <tr><td width="80" align="right"
     * valign="top"><strong><font size="-1" face="Courier New, Courier,
     * mono">"varyingOutputCardinality"</font></strong> <td width="75%"> number
     * of inputs may vary <tr><td width="80" align="right"
     * valign="top"><strong><font size="-1" face="Courier New, Courier,
     * mono">"autonomous"</font></strong> <td width="75%"> acts on its own,
     * tells updaters that this actor may fire on its own, without waiting for
     * an input to change. <tr><td width="80" align="right"
     * valign="top"><strong><font size="-1" face="Courier New, Courier,
     * mono">"trigger"</font></strong> <td width="75%"> always fire when the
     * updater has the opportunity, regardless of actual changes. </table>
     */
    public static Hashtable GraphObjectAttributes = new Hashtable();
    /**
     * key is actor name, entry is Class object corresponding to the plugin
     */
    public static Hashtable pluginRegistry = new Hashtable();
    /**
     * key is specific charger.obj.Actor, entry is the plugin instance
     * associated with this actor
     */
    public static Hashtable pluginInstances = new Hashtable();
    /**
     * The editframe whose canvas panel spawned this updater thread. Null if
     * there isn't one.
     */
    public EditFrame ownerFrame = null;

    /**
     * Creates an instance of the updater for a given graph object.
     *
     * @param ef If the graph is in an editing window, the updater may use it
     * (especially for animation)
     * @param go The original object that changed, causing the need for
     * updating. The node may not have actually changed; the updater pretends
     * that it has.
     */
    public GraphUpdater( EditFrame ef, GraphObject go ) {
        ownerFrame = ef;
        if ( go instanceof Graph ) {
            setGraph( ( (Graph)go ) );
        } else if ( go instanceof GNode ) {
            setGNode( ( (GNode)go ) );
        } else {
            setGraphObject( go );
        }
    }

    /**
     * Spawn an updater based on the originating node that changed. Decides what
     * component to start with and creates a thread to update actors in
     * sequence. If there are multiple threads running, then wait until the last
     * remaining thread is over before marking the graph as having been
     * completely changed, then repaint the frame if there is one.
     */
    public synchronized void run() {
        if ( Global.traceActors ) {
            Global.info( "starting graph updater: " + Thread.currentThread() );
        }
        try {
            if ( GraphToUpdate != null ) {
                updateGraph( GraphToUpdate );
                //GraphToUpdate.setActive( false );		// resetting active should be each actor's responsibility
            } else if ( GNodeToUpdate != null ) {
                updateGNode( GNodeToUpdate );
                //GNodeToUpdate.setActive( false );		// resetting active should be each actor's responsibility
            } else if ( GraphObjectToUpdate != null ) {
                updateGraphObject( GraphObjectToUpdate );
            }
        } catch ( CGActorException e ) {
            //Global.info("CGActor Exception: " +  e.getMessage() ); 
            JOptionPane.showMessageDialog( ownerFrame, "Actor " + e.getMessage(), "Actor Problem", JOptionPane.ERROR_MESSAGE );
        }
        if ( ownerFrame != null ) {
            Thread currentthread = Thread.currentThread();
            ThreadGroup tg = currentthread.getThreadGroup();
            // only clear the changed status if we are the last thread running in the updater group.
            if ( tg.activeCount() == 1 ) {
                ownerFrame.emgr.clearChanged( ownerFrame.TheGraph );
            }
            ownerFrame.repaint();
        }

        if ( Global.traceActors ) {
            Global.info( "end of graph updater in " + Thread.currentThread() );
        }
        //if ( ownerFrame != null ) ownerFrame.toFront();
    }

    private void setGraph( Graph g ) {
        GraphToUpdate = g;
        GNodeToUpdate = null;
        GraphObjectToUpdate = null;
    }

    private void setGNode( GNode gn ) {
        GraphToUpdate = null;
        GNodeToUpdate = gn;
        GraphObjectToUpdate = null;
    }

    private void setGraphObject( GraphObject go ) {
        GraphToUpdate = null;
        GNodeToUpdate = null;
        GraphObjectToUpdate = go;
    }

    /**
     * Given any graph object on which to wait, pause the current thread for the
     * AnimationDelay interval.
     *
     * @param milliseconds the wait time between "ticks"
     */
    public void WaitWhenAnimating( int milliseconds ) {
        if ( Global.ActorAnimation ) {
            //Global.info( "wait when animating " + Thread.currentThread() );
            if ( ownerFrame != null ) {
                //Global.info( "wait thinks it's repainting the canvas and yielding" );
                ownerFrame.cp.repaint();
                Thread.yield();
            }
            //cp.requestFocus();
            try {
                //Global.info( "sleeping for " + Hub.AnimationDelay + " msec" );
                Thread.sleep( milliseconds );
            } catch ( InterruptedException ie ) {
                Global.info( "sleep was interrupted (yawn!)" );
            }
        }
    }

    /**
     * Records each actor's input and output signature for actor activation.
     * Uses a clone of the signature lists so that further changes to the list
     * don't affect it.
     *
     * @param actorName Name by which the actor will be known throughout the
     * system
     * @param inputs List of input concepts (or graph), each with a constraining
     * type (or "T" )
     * @param outputs List of output concepts (or graph), each with a
     * constraining type (or "T")
     * @param attributes List of strings indicating various properties of the
     * actor
     * @see GraphUpdater#GraphObjectAttributes
     */
    public static void registerActorSignature(
            String actorName, ArrayList inputs, ArrayList outputs, ArrayList attributes ) {
        ArrayList realInputs = (ArrayList)inputs.clone();
        ArrayList realOutputs = (ArrayList)outputs.clone();
        InputSignatures.put( actorName.toLowerCase(), realInputs );
        OutputSignatures.put( actorName.toLowerCase(), realOutputs );
        GraphObjectAttributes.put( actorName.toLowerCase(), attributes );
        //Global.info( "actor " + actorName + ": " +
        //	inputs.size() + " in, " + outputs.size() + " out; " + attributes.toString() );
    }

    /**
     * For a given graph, start up all its actors. Spawn a new graph updater
     * (attached to the graph's editing window), and fire up a new thread for
     * that updater.
     */
    public static void startupAllActors( Graph g ) {
        if ( g == null ) {
            return;
        }
        EditFrame ef = g.getOwnerFrame();
        Iterator actors = new DeepIterator( g, new Actor() );
        while ( actors.hasNext() ) {
            Actor a = (Actor)actors.next();
            //if ( GraphUpdater.hasAttribute( a, "autonomous" ) )
            {
                a.setChanged( true );
                //Global.info( "about to fire autonomous actor " + a.getTextLabel() );
                GraphUpdater gu = new GraphUpdater( ef, a );
                ThreadGroup tg;
                if ( ef != null ) {
                    tg = ef.threadgroup;
                } else {
                    tg = Global.orphanUpdaters;
                }
                // no need to check for null ef, since all canvas panels are in a frame
                new Thread( tg, gu, "fired autonomous actor " + a.getTextLabel() ).start();
                //Global.info( "after firing autonomous actor " + a.getTextLabel() );
            }
        }
    }

    /**
     * With no arguments, find all the edit frames and start up all the actors!
     * Note: doesn't start up actors for any graph that is not in a frame.
     */
    public static void startupAllActors() {
        Iterator framelist = Global.editFrameList.values().iterator();
        while ( framelist.hasNext() ) {
            EditFrame ef = (EditFrame)framelist.next();
            startupAllActors( ef.TheGraph );
        }
    }

    /**
     * For a given graph, stop all its actors.
     *
     * @see GraphUpdater#pluginInstances
     */
    public static void shutdownAllActors( Graph g ) {
        if ( g == null ) {
            return;
        }
        Iterator actors = new DeepIterator( g, new Actor() );
        while ( actors.hasNext() ) {
            Actor a = (Actor)actors.next();
            a.setActive( false );
            a.selfCleanup();
        }
    }

    /**
     * With no arguments, find all the edit frames and shut down all the actors!
     * Note: does not shut down actors for any graph that is not in a frame.
     */
    public static void shutdownAllActors() {
        Iterator framelist = Global.editFrameList.values().iterator();
        while ( framelist.hasNext() ) {
            EditFrame ef = (EditFrame)framelist.next();
            shutdownAllActors( ef.TheGraph );
        }
    }

    /**
     * Notifies this graph updater that a particular node has changed and
     * requires updating.
     *
     * @param gn Node that needs updating.
     */
    public synchronized void propagate( GNode gn ) throws CGActorException {
        gn.setChanged( true );
        gn.setActive( false );  // 03-26-03 changed to false
        Global.info( "about to PROPAGATE on node " + gn.toString() );
        updateGNode( gn );
        //gn.setActive( false );
    }

    /**
     * Stores the signatures and attributes for all primitive actors. Has no
     * relationship whatsoever with the (in)famous Microsoft Windows "registry"
     * or anything like it.
     *
     * @see #registerActorSignature
     * @see #verifyActor
     */
    public static void registerPrimitives() {
        ArrayList inputs = new ArrayList( 2 );
        ArrayList outputs = new ArrayList( 1 );
        // attribute list for actors that are executable
        ArrayList vExec = new ArrayList( 1 );
        vExec.add( (Object)"executable" );
        vExec.add( (Object)"primitive" );

        // attribute list for actors that are both executable and commutative
        ArrayList vExecCommute = (ArrayList)(Object)vExec.clone();
        vExecCommute.add( (Object)"commutative" );
        ArrayList vExecCommuteVaryingInputCardinality = (ArrayList)(Object)vExec.clone();
        vExecCommuteVaryingInputCardinality.add( (Object)"varyingInputCardinality" );
        ArrayList vExecCommuteVaryingOutputCardinality = (ArrayList)(Object)vExec.clone();
        vExecCommuteVaryingOutputCardinality.add( (Object)"varyingOutputCardinality" );

        // all actors with 2 inputs and one output
        AddStringAsConceptType( inputs, "Number" );
        AddStringAsConceptType( inputs, "Number" );
        AddStringAsConceptType( outputs, "Number" );
        registerActorSignature( "plus", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingInputCardinality.clone() );
        registerActorSignature( "minus", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        registerActorSignature( "multiply", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingInputCardinality.clone() );
        registerActorSignature( "divide", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        // later, these should work for any two objects that can be compared
        registerActorSignature( "greaterthan", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        registerActorSignature( "greaterequal", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        registerActorSignature( "lessthan", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        registerActorSignature( "lessequal", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        if ( Global.use_1_0_actors ) {
            registerActorSignature( "greaterthan_1_0", inputs, outputs, (ArrayList)(Object)vExec.clone() );
            registerActorSignature( "greaterequal_1_0", inputs, outputs, (ArrayList)(Object)vExec.clone() );
            registerActorSignature( "lessthan_1_0", inputs, outputs, (ArrayList)(Object)vExec.clone() );
            registerActorSignature( "lessequal_1_0", inputs, outputs, (ArrayList)(Object)vExec.clone() );
        }

        // actors with 2 inputs where one is a database, and one output
        inputs = new ArrayList( 2 );
        outputs = new ArrayList( 1 );
        AddStringAsConceptType( inputs, "Database" );
        AddStringAsConceptType( inputs, "T" );
        AddStringAsConceptType( outputs, "T" );
        registerActorSignature( "dbfind", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingOutputCardinality.clone() );
        registerActorSignature( "lookup", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingOutputCardinality.clone() );
        //   11-16-2005 hsd : needs to use varying output cardinality, not input
        //registerActorSignature( "dbfind", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingInputCardinality.clone() );
        //registerActorSignature( "lookup", inputs, outputs, (ArrayList)(Object)vExecCommuteVaryingInputCardinality.clone() );

        // actors with 2 inputs, one output
        inputs = new ArrayList( 2 );
        outputs = new ArrayList( 1 );
        AddStringAsConceptType( inputs, "T" );
        AddStringAsConceptType( inputs, "T" );
        AddStringAsConceptType( outputs, "T" );
        registerActorSignature( "equal", inputs, outputs, (ArrayList)(Object)vExecCommute.clone() );
        registerActorSignature( "notequal", inputs, outputs, (ArrayList)(Object)vExecCommute.clone() );
        if ( Global.use_1_0_actors ) {
            registerActorSignature( "equal_1_0", inputs, outputs, (ArrayList)(Object)vExecCommute.clone() );
            registerActorSignature( "notequal_1_0", inputs, outputs, (ArrayList)(Object)vExecCommute.clone() );
        }

        // general actors with 1 input, and one output
        inputs = new ArrayList( 1 );
        outputs = new ArrayList( 1 );
        AddStringAsConceptType( inputs, "T" );
        AddStringAsConceptType( outputs, "T" );
        registerActorSignature( "copy", inputs, outputs, (ArrayList)(Object)vExec.clone() );

        // numeric actors with 1 input, and one output
        inputs = new ArrayList( 1 );
        outputs = new ArrayList( 1 );
        AddStringAsConceptType( inputs, "Number" );
        AddStringAsConceptType( outputs, "Number" );
        registerActorSignature( "exp", inputs, outputs, (ArrayList)(Object)vExec.clone() );
    }

    /**
     * Gets the names of all the plugins. Looks first for a jar file then for a
     * regular set of classes in a class path. Only accepts names of files that
     * start with "plugin" and end with ".class" that do NOT have a '$' in them.
     * Returns a list of names without the leading "plugin/" in case file
     * separator is different.
     */
    public static String[] getPluginList() {
        String[] plugins = null;
        String classpath = System.getProperty( "java.class.path" );

        Global.consoleMsg( "Class path is \"" + classpath + "\"" );

        // DRP: split the classpath
        String classPathSegment[] = classpath.split( File.pathSeparator );

        for ( int cpsIndex = 0; cpsIndex < classPathSegment.length; cpsIndex++ ) {
            // Hub.consoleMsg("looking for plugin in "
            //		+ classPathSegment[cpsIndex]);

            if ( classPathSegment[cpsIndex].toLowerCase().endsWith( ".jar" ) ) {
                ArrayList classfiles = new ArrayList();
                JarFile myJar = null;
                try {
                    myJar = new JarFile( classPathSegment[cpsIndex] );
                } catch ( IOException e ) {
                    Global.warning( "can't open jarfile " + e.getMessage() );
                }
                Enumeration entries = myJar.entries();
                while ( entries.hasMoreElements() ) {
                    JarEntry j = (JarEntry)( entries.nextElement() );
                    // Global.info( "jar entry name is " + j.getName() );
                    String name = j.getName();
                    if ( name.startsWith( "plugin" ) && name.endsWith( ".class" )
                            && ( name.indexOf( '$' ) == -1 ) ) {
                        // classfiles.add( name.substring( name.indexOf(
                        // '/' )+1 ) );
                        classfiles.add( name );
                    }
                }
                plugins = new String[ classfiles.size() ];
                Object o[] = classfiles.toArray();
                for ( int k = 0; k < o.length; k++ ) {
                    plugins[k] = (String)o[k];
                }
                if ( classfiles.size() > 0 ) {
                    return plugins;
                }
            } else {

                File pluginDirectoryFile = new File( "Bad_Path_Name" );
                pluginDirectoryFile = new File( classPathSegment[cpsIndex]
                        + File.separator + "plugin" );

                if ( pluginDirectoryFile != null ) {
                    Global.consoleMsg( "plugin directory is "
                            + pluginDirectoryFile.getAbsolutePath() );
                } else {
                    Global.consoleMsg( "plugin directory is not found" );
                }

                // get the list of plugins
                plugins = pluginDirectoryFile.list( new FilenameFilter() {
                    public boolean accept( File f, String name ) {
                        if ( name.endsWith( ".class" ) ) {
                            if ( name.indexOf( '$' ) == -1 ) {
                                return true;
                            }
                        }
                        return false;
                    }
                } );

                return plugins;
            }
        }

        return plugins;
    }

    /**
     * Load plugins from an array of plugin class names. Expects that classes
     * will be named <name>.class and strips extension from the name.
     */
    public static void registerPlugins( String[] plugins ) {
        String classpath = System.getProperty( "java.class.path" );
        //  String separator = File.separator;
        // if ( classpath.toLowerCase().endsWith( ".jar" ) )
        String separator = ".";


        //Global.info( "found " + plugins.length + " plugins" );
        for ( int n = 0; n < plugins.length; n++ ) {
            Class pluginClass = null;
            String className = Util.stripFileExtension( plugins[ n] );
            //Global.info( "plugin number " + n + " is " + className );
            try {
                String name = className;
                // in a jar file, still has prefix, but not when classes are in separate files
                if ( !className.startsWith( "plugin" ) ) {
                    name = "plugin" + separator + name;
                } else {
                    name = name.replaceAll( "/", separator );
                }
                //Global.info( "Looking for plugin named \"" + name + "\" in path " + classpath );
                pluginClass = ClassLoader.getSystemClassLoader().loadClass( name );
                //		Global.info( "found plugin " + className );
            } catch ( ClassNotFoundException cnfe ) {
                Global.info( "Plugin class not found exception: " + cnfe.getMessage() );
            }

            if ( pluginClass != null ) {
                // put the plugin class into the hub's registry
                registerPlugin( pluginClass );
            }

        }
    }

    /**
     *
     */
    public static void registerPlugin( Class pluginClass ) {
        ActorPlugin thePlugin = null;
        try {
            thePlugin = (ActorPlugin)( pluginClass.newInstance() );
        } catch ( InstantiationException ie ) {
            Global.info( "instantiation exception while registering plugin " + pluginClass.getName() + " " + ie.getMessage() );
        } catch ( IllegalAccessException iae ) {
            Global.info( "access exception while registering for plugin " + pluginClass.getName() + " " + iae.getMessage() );
        }

        ArrayList attrs = thePlugin.getPluginActorAttributes();
        if ( attrs.contains( (Object)"primitive" ) ) {
            attrs.remove( (Object)"primitive" );
        }
        registerActorSignature( thePlugin.getPluginActorName().toLowerCase(),
                thePlugin.getPluginActorInputConceptList(),
                thePlugin.getPluginActorOutputConceptList(), attrs );
        pluginRegistry.put( thePlugin.getPluginActorName().toLowerCase(), pluginClass );
        Global.consoleMsg( "Using actor plugin \"" + thePlugin.getPluginActorName()
                + "\": " + thePlugin.getPluginActorInputConceptList().size() + " in, "
                + thePlugin.getPluginActorOutputConceptList().size() + " out; " + attrs.toString() + " -- " + thePlugin.getSourceInfo() );
    }

    /**
     * For all concepts or graphs in a graph, update each of them. Needs: to
     * handle changes to the graph itself; e.g., referent, etc.
     */
    public synchronized void updateGraph( Graph g ) {

        //Global.info( "Start update graph " + g.getTextLabel() );

        try {
            updateGNode( g );
        } catch ( CGActorException e ) {
            Global.error( "CG Actor Exception in updateGraph (graph itself): " + e.getMessage() );
        }

        Iterator nodes = new ShallowIterator( g, GraphObject.Kind.GNODE );
        GNode gn = null;
        while ( nodes.hasNext() ) {
            gn = (GNode)nodes.next();
            try {
                updateGNode( gn );
            } catch ( CGActorException e ) {
                Global.error( "CG Actor Exception in updateGraph: " + e.getMessage() );
            }
            gn.setTextLabel( gn.getTextLabel() );
        }
    }

    /**
     * General routine for verifying/updating/validating any node. If node of
     * any kind is changed/ready to fire/whatever, then fire any actors for
     * which it is an input, then do something?? about actors for which it is an
     * input...
     *
     * @param go Object to be updated, insert your own code if you want to
     * provide behavior.
     */
    public synchronized void updateGraphObject( GraphObject go ) {
        if ( Global.traceActors ) {
            Global.info( "Start updateGraphObject" );
        }
        try {
            if ( go instanceof GNode ) {
                updateGNode( (GNode)go );
            } else if ( go instanceof GEdge ) {
                updateGEdge( (GEdge)go );
            }
        } catch ( CGActorException e ) {
            EditFrame ef = go.getOwnerFrame();
            JFrame current = Global.CharGerMasterFrame;
            if ( ef != null ) {
                current = ef;
            }
            JOptionPane.showMessageDialog( current,
                    "Actor Exception:\n" + e.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Performs whatever update or validate routine required for the given node.
     * At present only does useful work for concepts or graphs connected to
     * actors or through a coreferent link. If a concept or graph is being
     * updated, it does the following: <ul><li> copies this node's referent into
     * any concepts linked via coreferent links, <li>activates any actors for
     * which this concept/graph is an OUTPUT, making sure it's consistent,
     * <li>activates any actors for which this concept/graph is an INPUT.
     * <li>tells its enclosing context if it has changed, so that outer actors
     * can fire. </ul> In other words, after following coreferent it goes
     * upstream first, then downstream, then out of context.
     *
     * @param gn the node to be updated
     */
    public synchronized void updateGNode( GNode gn ) throws CGActorException {
        //Global.info( "Start update gnode " + gn.toString()  + " in " + Thread.currentThread() );
        //if ( ! Hub.enableActors ) return;     // since it's possible to have corefs enabled, do this after corefs

        GEdge link = null;	// a link by which this node may be connected to another

        // COREFS: Handle all corefs first!
        Iterator connections = gn.getEdges().iterator();
        gn.setActive( true );		// protect gn from recursive calls
        while ( Global.enableCopyCorefs && connections.hasNext() ) {
            link = (GEdge)connections.next();	// ConcurrentModificationException occurs here!!!
            // Global.info( "got next Gline ... it is a " + link.getClass().getName() );
            if ( link instanceof Coref ) {
                updateCoref( gn, (Coref)link );
            }
        }
        gn.setActive( false );		// un-protect gn, let others be responsible!

        if ( !Global.enableActors ) {
            return;
        }

        // ACTORS: Handle all possible links to actors, but only if actors are enabled		
        if ( gn instanceof Actor ) {
            initiateActorUpdate( (Actor)gn );
        } else if ( Global.enableActors && gn.isChanged() && ( gn instanceof Concept || gn instanceof Graph ) ) {
            //Global.info( "ready to try and activate actors" );

            gn.setActive( true );		// first protect gn from being altered by nested calls

            // OUTPUTS: Treat as an OUTPUT concept -- all actors for which gn is an output concept
            connections = gn.getEdges().iterator();
            while ( connections.hasNext() ) {
                Global.info( "thread is " + Thread.currentThread().toString() );
                gn.setActive( true );
                link = (GEdge)connections.next();

                if ( ( link.toObj == gn ) && // this node is an output
                        ( CGUtil.shortClassName( link.fromObj ).equalsIgnoreCase( "Actor" ) ) ) // to an actor
                {
                    // if the actor is executable, execute that actor
                    if ( isExecutable( ( (Actor)link.fromObj ) ) ) {
                        try {
                            // then execute that actor
                            // Global.info( "update input node " + gn.getTextLabel() + 
                            //	" init actor update on actor " + link.toObj.getTextLabel() );
                            if ( ownerFrame != null ) {
                                WaitWhenAnimating( Global.AnimationDelay );
                            }
                            initiateActorUpdate( (Actor)link.fromObj );
                        } catch ( CGActorException e ) {
                            throw e;
                        }
                        gn.setActive( false );
                    }
                }
            }

            // INPUTS: Treat as an INPUT concept -- all actors for which gn is an input concept
            connections = gn.getEdges().iterator();
            while ( connections.hasNext() ) {
                gn.setActive( true );
                link = (GEdge)connections.next();
                if ( ( link.fromObj == gn ) && // if this node is an input to an actor
                        ( CGUtil.shortClassName( link.toObj ).equalsIgnoreCase( "Actor" ) ) ) {
                    // if actor is not executable, then skip all this
                    if ( isExecutable( ( (Actor)link.toObj ) ) ) {
                        try {
                            // then execute that actor
                            Global.info( "update input node " + gn.getTextLabel()
                                    + " about to initiate actor update on actor " + link.toObj.getTextLabel() );
                            if ( ownerFrame != null ) {
                                WaitWhenAnimating( Global.AnimationDelay );
                            }
                            initiateActorUpdate( (Actor)link.toObj );
                        } catch ( CGActorException e ) {
                            throw e;
                        }
                    }
                }
            }
            // after all changes have been made, this is a good time to adjust dimensions
        }
        // finally un-protect gn from being altered by nested calls
        gn.setActive( false );
    }

    /**
     * Performs whatever update or validate routine required for the given edge.
     * Deleting an edge is handled by the EditFrame; this is for updating the
     * text label. Easiest way to do this is by deleting and then re-inserting.
     * NEEDED: When changing a line's label, re-sort the myedges of its ends!
     * This will probably involve stopping and restarting any plugin actors
     * affected.
     */
    public synchronized void updateGEdge( GEdge gn ) throws CGActorException {
        GNode f = (GNode)gn.fromObj;
        GNode t = (GNode)gn.toObj;

        ( (GNode)gn.fromObj ).deleteGEdge( gn );
        ( (GNode)gn.toObj ).deleteGEdge( gn );
        //if ( gn instanceof Arrow ) ((Arrow)gn).BreakArrowForNotio();

        f.attachGEdge( gn );
        t.attachGEdge( gn );
        //if ( gn instanceof Arrow ) ((Arrow)gn).MakeArrowForNotio( f, t, gn.getTextLabel() );
    }

    /**
     * If a referent is changed, then we update the referents of any
     * coreferenced concepts. In a way, this may be incorrect: should
     * MorningStar be copied to EveningStar? Certain referents ( forall, exists,
     * etc.) aren't copied, no matter what. This is ultimately controlled by a
     * user-selectable option "enableCopyCorefs"
     *
     * @param gn The node that was changed
     * @param c The coref that points us to a new node that is subject to being
     * changed.
     */
    public synchronized void updateCoref( GNode gn, Coref c ) throws CGActorException {
//        Global.info( "updating coref from " + gn.toString() );
        GNode nodeToUpdate = null;
        if ( c.fromObj == gn ) {
            nodeToUpdate = (GNode)c.toObj;
        } else {
            nodeToUpdate = (GNode)c.fromObj;
        }

        String myref = ( (Concept)gn ).getReferent();
        if ( myref.startsWith( "@" ) ) {
            return;
        }
        if ( !( (Concept)nodeToUpdate ).getReferent().equals( myref ) ) {
            ( (Concept)nodeToUpdate ).setReferent( myref, true );
            if ( nodeToUpdate.getOwnerFrame() != null ) {
                nodeToUpdate.getOwnerFrame().emgr.setChangedContent( EditChange.SEMANTICS  );
            }
            // uncommented 10-05-03, to account for taking setchanged out of setreferent
            updateGNode( nodeToUpdate );
            gn.setActive( false );
            propagate( nodeToUpdate );
        }
    }

    /**
     * Sets up an actor for execution by first seeing if it is executable and
     * then finding its associated input and output concepts which it passes to
     * the actual updater.
     */
    public synchronized void initiateActorUpdate( Actor a )
            throws CGActorException {
        //Global.info( "initiate actor update " + a.getTextLabel() );
        EditFrame ef = a.getOwnerFrame();
        ArrayList inputs = new ArrayList();
        ArrayList outputs = new ArrayList();
        if ( isExecutable( a ) && !a.isActive() ) {
            // mark the actor as being in-process
            a.setActive( true );
            // construct two lists: one for input and output concepts
            GEdge ge;
            Iterator iter = a.getEdges().iterator();
            while ( iter.hasNext() ) {
                ge = (GEdge)iter.next();
                // if edge goes from this actor, then its destination is an output
                if ( ge.fromObj == a ) {
                    outputs.add( ge.toObj );
                }
                // if edge goes to this actor, then its source is an input
                if ( ge.toObj == a ) {
                    inputs.add( ge.fromObj );
                }
                // if neither then something has gone wrong that we'll check in the future.
            }
            a.setActive( true );
            // pass list to actor updating
            fireActor( a, inputs, outputs );
            a.setActive( false );		// 03-26-03
        }
    }

    /**
     * Given an actor's actual signature, verifies that it matches the actor's
     * definition signature. For now, also make sure that no input or output is
     * a context; we don't handle those yet.
     *
     * @param actorNamePassed	Label from actual actor that appears in graph.
     * @param inputs	The list of input concepts that appear.
     * @param outputs The list of output concepts that appear.
     * @see #compareGraphObjectList
     */
    public synchronized static void verifyActor( String actorNamePassed, ArrayList inputs, ArrayList outputs )
            throws CGActorException {
        String actorName = actorNamePassed.toLowerCase();
        // make sure that input are all simple concepts (i.e., not contexts)
        Iterator iter;
        iter = inputs.iterator();
        while ( iter.hasNext() ) {
            GNode go = (GNode)iter.next();
            if ( go instanceof Graph ) {
                throw new CGActorException( "Context as input not yet implemented." );
            }
            if ( !Global.AllowNullActorArguments ) {
                if ( ( (Concept)go ).getReferent().equalsIgnoreCase( "null" ) || ( (Concept)go ).getTypeLabel().equalsIgnoreCase( "null" ) ) {
                    throw new CGActorException(
                            "Null input type or referent not allowed as actor input (change Preference to allow them).",
                            go );
                }
            }
        }

        iter = outputs.iterator();
        while ( iter.hasNext() ) {
            GNode go = (GNode)iter.next();
            if ( go instanceof Graph ) {
                throw new CGActorException( "Context as output not yet implemented.", go );
            }
        }


        // verify number of inputs and outputs, and the types as well.
        try {
            if ( !hasAttribute( actorName, "varyingInputCardinality" ) ) {
                ArrayList indefs = (ArrayList)InputSignatures.get( actorName );
                compareGraphObjectList( indefs, inputs, "inputs" );
            }
            if ( !hasAttribute( actorName, "varyingOutputCardinality" ) ) {
                ArrayList outdefs = (ArrayList)OutputSignatures.get( actorName );
                compareGraphObjectList( outdefs, outputs, "outputs" );
            }
        } catch ( CGGraphFormationError x ) {
            throw new CGActorException( x.getMessage() );
        }
    }

    /**
     * Compares a list of graph objects to another. Currently just checks to
     * make sure the cardinality agrees. In the future, will also check types,
     * referents, etc. No return; just throw a formation exception.
     *
     * @param defs Pattern list of objects
     * @param actuals List of objects to be checked against the pattern
     * @see CGGraphFormationError
     */
    public synchronized static void compareGraphObjectList( ArrayList defs, ArrayList actuals, String whichList )
            throws CGGraphFormationError {
        if ( defs.size() != actuals.size() ) {
            throw new CGGraphFormationError( "wrong number of " + whichList
                    + ": expected " + defs.size() + "; has " + actuals.size() );
        }

        Object def;
        Object actual;

        Iterator iterdefs = defs.iterator();
        Iterator iteractuals = actuals.iterator();
        while ( iterdefs.hasNext() ) {
            def = iterdefs.next();
            actual = iteractuals.next();
            // here check for type compatibility, etc. in the future.
        }
    }

    /**
     * Actually "fires" the given actor.
     *
     * @see ActorPrimitive
     */
    public synchronized void fireActor( Actor a, ArrayList inputs, ArrayList outputs )
            throws CGActorException {
        boolean possibleChange = false;
        String actorName = a.getTextLabel().toLowerCase();
        if ( Global.traceActors ) {
            Global.info( "Start update actor " + actorName + " id " + a.objectID + " named "
                    + a.getTextLabel() + " in " + Thread.currentThread() );
        }
        // Verify the actor name is on the "registered" list and that args conform
        try {
            verifyActor( actorName, inputs, outputs );
        } catch ( CGActorException x ) {
            a.setActive( false );
            throw new CGActorException( actorName + ": " + x.getMessage() );
        }

        if ( Global.traceActors ) {
            Global.info( "Actor: " + actorName + " beginning update..." );
        }
        // handle primitive actors here
        if ( hasAttribute( (GraphObject)a, "primitive" ) && isExecutable( a ) ) {
            try {
                possibleChange = true;
                ActorPrimitive.performActorOperation( this, actorName, inputs, outputs );
            } catch ( CGActorException x ) {
                a.setActive( false );
                throw new CGActorException( actorName + ": " + x.getMessage() );
            }
        } else // look for a plugin actor
        {
            Class c = null;
            ActorPlugin thePlugin = (ActorPlugin)( GraphUpdater.pluginInstances.get( a ) );
            if ( thePlugin == null ) // if it hasn't been instantiated yet, then make one
            {
                c = (Class)( pluginRegistry.get( actorName ) );
                if ( c == null ) {
                    Global.info( "fireActor couldn't find actor " + actorName );
                } else {
                    try {
                        thePlugin = (ActorPlugin)( c.newInstance() );
                        pluginInstances.put( a, thePlugin );
                        Global.info( "made new instance of plugin " + actorName );
                    } catch ( InstantiationException ie ) {
                        Global.info( "exception instantiating plugin " + actorName + " " + ie.getMessage() );
                    } catch ( IllegalAccessException iae ) {
                        Global.info( "access exception for plugin " + actorName + " " + iae.getMessage() );
                    }
                }
            }
            // should have an instantiated valid plugin by now
            try {
                // Establish access to the correct performActorOperation, with 1 graphupdater and
                //   2 list arguments
                Class parameterClasses[] = new Class[ 3 ];
                parameterClasses[ 0] = this.getClass();
                parameterClasses[ 1] = ( new ArrayList() ).getClass();
                parameterClasses[ 2] = ( new ArrayList() ).getClass();
                java.lang.reflect.Method actionMethod =
                        thePlugin.getClass().getMethod( "performActorOperation", parameterClasses );

                // Set up the actual parameters to the established method
                Object parameters[] = new Object[ 3 ];
                parameters[0] = this;
                parameters[1] = inputs;
                parameters[2] = outputs;

                // Save the old outputs for comparison after activation
                Object oldoutputs[] = outputs.toArray();

                // Here's where the plug-in's performActorOperation is actually invoked!
                Object returnvalue = actionMethod.invoke( thePlugin, parameters );
                //Global.info( "performed actor function for " + actorName );

                // Check for changes to the output referents
                Global.info( "checking " + outputs.size() + " outputs." );
                Object newoutputs[] = ( (ArrayList)( parameters[2] ) ).toArray();
                for ( int k = 0; k < newoutputs.length; k++ ) {
                    GNode new1 = (GNode)newoutputs[ k];
                    GNode old1 = (GNode)oldoutputs[ k];
                    if ( new1.isChanged() ) {
                        continue;	// don't override an already-changed node
                    }
                    if ( new1 instanceof Concept && old1 instanceof Concept ) // CHANGES NEED TO BE MADE HERE TO CHECK FOR CHANGED REFERENTS
                    {
                        if ( hasAttribute( thePlugin.getPluginActorName(), "trigger" )
                                || !( (Concept)new1 ).getReferent().equalsIgnoreCase( ( (Concept)old1 ).getReferent() ) ) {
                            ( (Concept)new1 ).setChanged( true );
                            ( (Concept)new1 ).setActive( false );
                            propagate( new1 );
                        }
                    }
                }
            } catch ( IllegalAccessException iae ) {
                Global.info( "access exception for plugin " + actorName + " " + iae.getMessage() );
            } catch ( NoSuchMethodException nsme ) {
                Global.info( "no such method " + nsme.getMessage() );
            } catch ( java.lang.reflect.InvocationTargetException ite ) {
                Global.info( "plugin " + actorName + " threw an exception; message: " + ite.getMessage() );
            }
            /*
             catch ( IllegalArgumentException iarge )
             {
             Global.info( "illegal argument for plugin " + actorName + " " + iarge.getMessage());
             }
             */
            if ( hasAttribute( actorName, "displayable" ) ) {
                if ( ownerFrame != null ) {
                    ownerFrame.transferFocusBackward();
                }
            }
        }
        if ( possibleChange ) {
            // resize all output concepts, just in case
        }
    }

    /**
     * Checks to see if the graph object has an executable procedure associated
     * with it.
     *
     * @return true if the object has the attribute "executable" in its
     * attribute ArrayList.
     */
    public static boolean hasAttribute( GraphObject go, Object attr ) {
        String goname = go.getTextLabel();
        ArrayList attributes = (ArrayList)GraphUpdater.GraphObjectAttributes.get( goname );
        if ( attributes != null ) {
            if ( !attributes.isEmpty() ) {
                if ( attributes.contains( (String)attr ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks to see if the graph object has an executable procedure associated
     * with it.
     *
     * @return true if the object has the attribute "executable" in its
     * attribute ArrayList.
     */
    public static boolean hasAttribute( String actorName, Object attr ) {
        ArrayList attributes = (ArrayList)GraphUpdater.GraphObjectAttributes.get( actorName.toLowerCase() );
        if ( attributes != null ) {
            if ( !attributes.isEmpty() ) {
                if ( attributes.contains( (String)attr ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tells whether the given actor is executable; i.e., can be activated. Not
     * all actors are executable. If the user creates an actor from his own
     * knowledge (i.e., outside of the list already defined in Charger) then
     * there's nothing the system can execute. When an actor is registered, it
     * has a list of attributes, one of which indicates whether it's executable
     * or not.
     */
    public static boolean isExecutable( Actor a ) {
        return hasAttribute( (GraphObject)a, (Object)"executable" );
    }

    /**
     * Convenience method for actor builders. Constructs a list of
     * charger.obj.Concept suitable for use in an actor signature. If differing
     * type labels are needed, then the concept list has to be created element
     * by element.
     *
     * @see ActorPlugin#getPluginActorInputConceptList
     * @see ActorPlugin#getPluginActorOutputConceptList
     * @param numberOfConcepts how many concepts desired in this list
     * @param typeLabel what label each of the concepts will have.
     * @return ArrayList of charger.obj.Concept instances, all with the same
     * type label
     */
    public static ArrayList createConceptList( int numberOfConcepts, String typeLabel ) {
        ArrayList v = new ArrayList();
        for ( int n = 0; n < numberOfConcepts; n++ ) {
            AddStringAsConceptType( v, typeLabel );
        }
        return v;
    }

    public static void AddStringAsConceptType( ArrayList v, String s ) {
        Concept c = new Concept();
        c.setTypeLabel( s );
        v.add( c );
    }

    /**
     * Similar to getting the concept or relation names in effect, but since the
     * actors are all registered with Global, that's the easiest place to get them.
     *
     * @return possibly empty list of actor types, sorted according to name
     */
    public static String[] getActorNames() {
        Iterator keys = GraphUpdater.InputSignatures.keySet().iterator();
        String[] svalues = new String[ GraphUpdater.InputSignatures.size() ];
        int valuenum = 0;
        while ( keys.hasNext() ) {
            svalues[ valuenum++] = (String)keys.next();
        }
        Arrays.sort( svalues, Global.ignoreCase );
        return svalues;
    }
}	// class

/*
 Tons of issues to be considered!
 What is the proper order of actor updating?
 How to start "execution"? Possibly through a "Start" actor.
 How to control infinite looping? Possibly through a "Stop" actor.
 */
