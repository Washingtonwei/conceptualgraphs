package charger;

import charger.EditingChangeState.EditChange;
import charger.exception.CGContextException;
import kb.matching.BinaryRelationMatch;
import kb.matching.AbstractTupleMatcher;
import charger.util.*;
import charger.obj.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.io.*;
//import java.applet.*;
import javax.swing.*;
//import javax.swing.border.*;
//import java.awt.datatransfer.*;
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
 * Contains core conceptual graph operations, as manifested in CharGer. These
 * are generally invoked by the EditManager. Manages the "Examine" and
 * "Operation" menus communicates with its container EditFrame by the ef
 * variable.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 *         Harry S. Delugach.
 * @see EditFrame
 * @see CanvasPanel
 * @see EditManager
 */
public class OperManager {

    /**
     * Does a lot of communicating with the edit frame that owns it.
     */
    public EditFrame ef;
    public Action mmatAnalysisAction = null;
    public Action binaryRelationMatchingAction = null;
    public Action bestBinaryRelationMatchingAction = null;
    public static String ValidateCmdLabel = "Validate";
    public static String MakeGenericCmdLabel = "Make Generic";
    public static String VerifyInternalLabel = "Verify Internal Graph";
    public static String MaxJoinWithOpenGraphsLabel = "Maximal join with open graphs";
    public static String JoinWithOpenGraphsLabel = "Join selected nodes in open graphs";
    public static String CommitToKBLabel = "Commit to knowledge base";
    public static String ModifyMatchingSchemeLabel = "Modify matching scheme";
    public JMenuItem operationSummarize = new JMenuItem(Global.knowledgeManager.summarizeKnowledgeAction);
    public JMenuItem operationMakeTypeHierarchy = new JMenuItem(kb.ConceptManager.makeTypeHierarchyAction);
    public JMenuItem operationBinaryRelationMatching = null;
    public JMenuItem operationBestBinaryRelationMatching = null;

    public OperManager(EditFrame outerFrame) {
        // Link to the outer frame
        ef = outerFrame;
        makeMenus();
    }

    private void makeMenus() {

        /**
         * An action for comparing a set of CharGer graphs, suitable for adding
         * to menus and buttons. Really just a wrapper for
         * binaryRelationMatchScorer. Returns the Action.NAME Hub.strs(
         * "RelationMatchingLabel" ).
         *
         * @see Global#strs
         * @see #binaryRelationMatchScorer
         */
        binaryRelationMatchingAction = new AbstractAction() {
            public Object getValue(String s) {
                if (s.equals(Action.NAME)) {
                    return Global.strs("RelationMatchingLabel");
                }
                return super.getValue(s);
            }

            public void actionPerformed(ActionEvent e) {
                performActionRelationMatching();
            }
        };
        operationBinaryRelationMatching = new JMenuItem(binaryRelationMatchingAction);

        bestBinaryRelationMatchingAction = new AbstractAction() {
            public Object getValue(String s) {
                if (s.equals(Action.NAME)) {
                    return Global.strs("BestRelationMatchingLabel");
                }
                return super.getValue(s);
            }

            public void actionPerformed(ActionEvent e) {
                performActionBestRelationMatching();
            }
        };
        operationBestBinaryRelationMatching = new JMenuItem(bestBinaryRelationMatchingAction);

        // Here's where the "Examine" and "Operation" menu is arranged and initialized.

        if (Global.wordnetEnabled) {
            ef.emgr.makeNewMenuItem(ef.operateMenu, Global.strs("AttachOntologyLabel"), 0);
            // odd code because menu items are anonymous variables
            ef.operateMenu.getItem(ef.operateMenu.getMenuComponentCount() - 1).setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_A, Global.AcceleratorKey | InputEvent.SHIFT_MASK));
            ef.emgr.makeNewMenuItem(ef.operateMenu, Global.strs("DeleteOntologyLabel"), 0);
            ef.operateMenu.addSeparator();
        }

        // Here's where any new MMAT operations should be added! HSD 

        ef.operateMenu.add(operationBinaryRelationMatching);
        ef.operateMenu.add(operationBestBinaryRelationMatching);


        ef.operateMenu.addSeparator();

        if (!Global.OfficialRelease) {
            //ef.operateMenu.getItem(ef.operateMenu.getItemCount()-1).setEnabled( false );
            ef.emgr.makeNewMenuItem(ef.operateMenu, MaxJoinWithOpenGraphsLabel, 0);
        }
        ef.emgr.makeNewMenuItem(ef.operateMenu, JoinWithOpenGraphsLabel, 0);
        // REMOVE-NOTIO  ef.emgr.makeNewMenuItem( ef.operateMenu, MatchToOpenGraphsLabel, 0 );
        ef.emgr.makeNewMenuItem(ef.operateMenu, MakeGenericCmdLabel, 0);
        //ef.operateMenu.addSeparator();

        if (!Global.OfficialRelease) {
            ef.emgr.makeNewMenuItem(ef.examineMenu, "Show Internals", 0);
            ef.emgr.makeNewMenuItem(ef.examineMenu, ValidateCmdLabel, 0);
            ef.emgr.makeNewMenuItem(ef.examineMenu, VerifyInternalLabel, 0);
            ef.emgr.makeNewMenuItem(ef.examineMenu, "Select Rules", 0);
            ef.emgr.makeNewMenuItem(ef.operateMenu, CommitToKBLabel, 0);
            ef.operateMenu.getItem(ef.operateMenu.getItemCount() - 1).setEnabled(false);

            //makeNewMenuItem( ef.operateMenu, "Create KB... (unimplemented)", 0 );
            //ef.operateMenu.getItem(ef.operateMenu.getItemCount()-1).setEnabled( false );
        }
        ef.operateMenu.addSeparator();

        ef.operateMenu.add(operationMakeTypeHierarchy);
        ef.operateMenu.add(operationSummarize);

        ef.operateMenu.addSeparator();

        //REMOVE-NOTIO  ef.emgr.makeNewMenuItem( ef.operateMenu, ModifyMatchingSchemeLabel, 0 );

    }

//    /**
//     * @return the mode label telling which tool is active
//     */
//    public String getMode( JRadioButton source ) {
//        if ( source == ef.selectionTool ) {
//            return EditFrame.SelectToolMode;
//        }
//        if ( source == ef.conceptTool ) {
//            return EditFrame.ConToolMode;
//        }
//        if ( source == ef.relationTool ) {
//            return EditFrame.RelToolMode;
//        }
//        if ( source == ef.actorTool ) {
//            return EditFrame.ActorToolMode;
//        }
//        if ( source == ef.arrowTool ) {
//            return EditFrame.ArrowToolMode;
//        }
//        if ( source == ef.corefTool ) {
//            return EditFrame.CorefToolMode;
//        }
//        if ( source == ef.typeTool ) {
//            return EditFrame.TypeToolMode;
//        }
//        if ( source == ef.relTypeTool ) {
//            return EditFrame.RelTypeToolMode;
//        }
//        if ( source == ef.genSpecLinkTool ) {
//            return EditFrame.GenSpecLinkToolMode;
//        }
//        if ( source == ef.noteTool ) {
//            return EditFrame.NoteToolMode;
//        }
//        if ( source == ef.deleteTool ) {
//            return EditFrame.DeleteToolMode;
//        }
//
//        return "none";
//    }

    /**
     * Determines for each menu item whether to be enabled or disabled ("gray'ed
     * out" ) Many items are disabled if there is no selection; other items are
     * disabled if the clipboard is empty or nothing's changed, etc.
     *
     * @see EditFrame#somethingHasBeenSelected
     */
    public void setMenuItems() {

        for (int num = 0; num < ef.examineMenu.getItemCount(); num++) {
            JMenuItem mi = ef.examineMenu.getItem(num);
            if (mi == null) {
                continue;        // a separator
            }
            String s = mi.getText();
            if (s.equals(MakeGenericCmdLabel)) {
                mi.setEnabled(ef.somethingHasBeenSelected);
            }
        }

        for (int num = 0; num < ef.operateMenu.getItemCount(); num++) {
            JMenuItem mi = ef.operateMenu.getItem(num);
            if (mi == null) {
                continue;        // a separator
            }
            String s = mi.getText();
            if (s.equals(MakeGenericCmdLabel)) {
                mi.setEnabled(ef.somethingHasBeenSelected);
            } else if (s.equals(JoinWithOpenGraphsLabel)) {
                mi.setEnabled(ef.somethingHasBeenSelected);
            } else if (s.equals(Global.strs("AttachOntologyLabel"))) {
                mi.setEnabled(ef.somethingHasBeenSelected);
            } else if (s.equals(Global.strs("DeleteOntologyLabel"))) {
                mi.setEnabled(ef.somethingHasBeenSelected);
            }
        }

    }


    protected void finalize() throws Throwable {
        try {
            Global.info("finalizing oper manager of frame " + ef.editFrameNum);

            super.finalize();
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }


    /* REMOVE-NOTIO public notio.MatchingScheme currentMatchingScheme() {
        //if ( matchingschemeframe == null ) return NotioTrans.myMatchingScheme( null );
        //else return matchingschemeframe.getMatchingScheme();
        return Hub.getDefaultMatchingScheme();
    }
    * */

    /**
     * Performs some consistency checks and fixes problems. <ul> <li>Checks to
     * see if there are any duplicate edges; i.e., <ul> <li>more than one arrow
     * connecting same nodes in same direction. <li>any edge where either its
     * "to" object or "from" object is null. <li>more than one genspeclink
     * connecting same nodes in same direction (not yet implemented) <li>more
     * than one coref between same nodes (not yet implemented) </ul> </ul>
     *
     * @param g The graph to be checked.
     */
    public static void performActionValidate(Graph g) {
        //    Global.info( "  validating graph " + ((g.getOwnerFrame() != null) ? g.getOwnerFrame().getFilename() : "null") );


        // Remove incomplete edges that don't have two non-null ends
        DeepIterator edges = new DeepIterator(g, GraphObject.Kind.GEDGE);
        int numIncomplete = 0;

        while (edges.hasNext()) {
            GEdge edge = (GEdge) edges.next();        // possible incomplete edge
            boolean isIncomplete = false;
            if (edge.toObj == null || edge.fromObj == null) {
                edge.detachFromGNodes();
                edge.getOwnerGraph().forgetObject(edge);
                numIncomplete++;
            }
        }
        if (numIncomplete > 0) {
            Global.info("Removed " + numIncomplete + " incomplete edges (i.e., connecting null objects).");
        }

        // Remove duplicate arrows
        DeepIterator arrows = new DeepIterator(g, new Arrow());
        ArrayList<Arrow> noduplicates = new ArrayList<Arrow>();
        int numDuplicates = 0;

        while (arrows.hasNext()) {
            Arrow a = (Arrow) arrows.next();        // possible duplicate arrow
            boolean isDuplicate = false;
            int k = 0;
            while (k < noduplicates.size() && !isDuplicate) {
                //Global.info( "looking at arrow from id " + a.fromObj.getTextLabel() + " to id " + a.toObj.getTextLabel() );
//                if ( ( a.toObj.objectID == noduplicates.get( k ).toObj.objectID )
//                        && ( a.fromObj.objectID == noduplicates.get( k ).fromObj.objectID ) ) // duplicate arrow
                if ((a.toObj.objectID.equals(noduplicates.get(k).toObj.objectID))
                        && (a.fromObj.objectID.equals(noduplicates.get(k).fromObj.objectID))) // duplicate arrow
                {
                    isDuplicate = true;
                    numDuplicates++;
                    Global.info("Found DUPLICATE arrow from id " + a.fromObj.getTextLabel() + " to id " + a.toObj.getTextLabel());
                    a.detachFromGNodes();
                    a.getOwnerGraph().forgetObject(a);
                } else {
                    k++;
                }
            }
            if (!isDuplicate) {
                noduplicates.add(a);
            }
        }
        if (numDuplicates > 0) {
            Global.info("Removed " + numDuplicates + " duplicate arrows.");
        }
        // check edges:
        // perform placeEdge to make sure it's in the right place.
        // check for edges that aren't owned by the same graph as their linked nodes
        DeepIterator iter = new DeepIterator(g, GraphObject.Kind.GEDGE);
        while (iter.hasNext()) {
            GEdge edge = (GEdge) iter.next();
            edge.placeEdge();
            Graph owner = edge.getOwnerGraph();
            Graph fromOwner = edge.fromObj.getOwnerGraph();
            Graph toOwner = edge.toObj.getOwnerGraph();
            // if edge's owner is also both nodes' owner, then it's okay
            if (fromOwner == owner && toOwner == owner)
                continue;
            // if both nodes have same owner, it should be the edge's owner too
            if (fromOwner == toOwner)
                edge.setOwnerGraph(fromOwner);
            else {  // need to set the edge as owned by the 
                Graph dominantContext;
                try {
                    dominantContext = GraphObject.findDominantContext(new ArrayList<GraphObject>(Arrays.asList(fromOwner, toOwner)));
                    edge.setOwnerGraph(dominantContext);
                } catch (CGContextException ex) {
                    Global.error("Trying to validate graph." + ex.getMessage());
                }
            }
        }

        // check for arrow rectangle that out of whack
    }

    /**
     * Processes type definitions.
     */
    /* REMOVE-NOTIO public void performActionValidateOLD( Graph g ) {
        if ( g == null ) {
            return;
        }
        notio.Graph ng = g.nxGraph;
        notio.ConceptType deftype = Hub.KB.getConceptTypeHierarchy().getTypeByLabel( "Definition" );
        if ( deftype == null ) {
            JOptionPane.showMessageDialog( ef, "Type label \"Definition\" not in knowledge base.",
                    "CharGer internal error", JOptionPane.ERROR_MESSAGE );
        } else {
            notio.Concept[] cs = ng.getConceptsWithSuperType( deftype );
            if ( cs.length > 0 ) {
                for ( int k = 0; k < cs.length; k++ ) {
                    // process type definition for graph contained in concept cs[k]
                    Global.info( "Processing type definition for graph contained in concept " + k );

                }
            } else {
                Global.info( "No type definitions found." );
            }
        }
    }
    * */

    /**
     * Queries the user for an ontology label (e.g., Wordnet synset) for each of
     * the objects in the array list. If the object already has a label, make that
     * the default.
     *
     * @param objectList list of graph objects. Only GNodes are considered, others are
     *                   ignored.
     */
    public void performActionAttachOntologyLabel(ArrayList objectList) {
        // NEED to change this sometime so that it's connected to some more permanent wordnet mgr
        charger.gloss.wn.WordnetManager wnmgr = charger.gloss.wn.WordnetManager.getInstance(new Transcript());
        Iterator iter = objectList.iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            GraphObject go = (GraphObject) iter.next();
            if (go instanceof GNode) {
                charger.gloss.AbstractTypeDescriptor descr = ((GNode) go).getTypeDescriptor();
                //if ( descr == null )
                // NEED a parameter for charger preferences indicating whether to try generic
                // for now, force to try generic
                // NEED to pass type label as a phrase into this.
                changed = changed | wnmgr.attachDescriptor(
                        kb.ConceptManager.getSensesFromPhrase(((GNode) go).getTypeLabel(), wnmgr), (GNode) go);
                //else
                //	changed = changed | wnmgr.attachDescriptor(  
                //		wnmgr.queryForSense( ((GNode)go).getTypeLabel(), descr, true ), (GNode)go );
            }
        }
        if (changed) {
            ef.emgr.setChangedContent(EditChange.SEMANTICS, EditChange.UNDOABLE);
        }
    }

    /**
     * Shows a brief internal summary for the selected objects.
     *
     * @param objectList vector of graph objects. Only GNodes are considered, others are
     *                   ignored.
     */
    public void performActionShowInternals(ArrayList objectList) {
        // NEED to change this sometime so that it's connected to some more permanent wordnet mgr
        Iterator iter = objectList.iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            GraphObject go = (GraphObject) iter.next();
            if (go instanceof GNode) {
                Global.consoleMsg(go.toString());
            }
        }
    }

    /**
     * Deletes the ontology label (e.g., Wordnet synset) for each of the objects
     * in the vector.
     *
     * @param objectList vector of graph objects. Only GNodes are considered, others are
     *                   ignored.
     */
    public void performActionDeleteOntologyLabel(ArrayList objectList) {
        if (JOptionPane.NO_OPTION
                == JOptionPane.showConfirmDialog(ef, "You're deleting glossary entries. Are you sure?",
                "Deleting glossary text", JOptionPane.YES_NO_OPTION)) {
            return;
        }

        charger.gloss.wn.WordnetManager wnmgr = charger.gloss.wn.WordnetManager.getInstance(new Transcript());
        Iterator iter = objectList.iterator();
        boolean changed = false;
        int entrycount = 0;
        while (iter.hasNext()) {
            GraphObject go = (GraphObject) iter.next();
            if (go instanceof GNode) {
                boolean b = wnmgr.forgetDescriptors((GNode) go);
                if (b) {
                    entrycount++;
                }
                changed = changed | b;
            }
        }
        ef.emgr.setChangedContent(EditChange.SEMANTICS, EditChange.UNDOABLE);
        ef.displayOneLiner("Deleted " + entrycount + " glossary entries.");
    }

    /**
     * Operates the match operation between graphs. Uses the "current" graph as
     * the target, and looks for any open graph that matches, according to a
     * general matching scheme that is internally defined at present. If a match
     * is found, that matching graph is opened in a new window. Should probably
     * just make the matched graph "current".
     *
     * @param g The current graph
     *
     */
    /* REMOVE-NOTIO public void performActionMatchToOpenGraphs( Graph g )
     {
     Iterator opengraphs = Hub.editFrameList.values().iterator();
     boolean matchFound = false;
     while ( opengraphs.hasNext() && ! matchFound )
     {
     EditFrame next = ((EditFrame) opengraphs.next());
     if ( next != Hub.getCurrentEditFrame() )
     {
     //Global.info( "checking match for edit frame " + next.graphName );
     //Global.info( "current graph is " + Ops.showWholeCharGerGraph( g, false ) );
     //Global.info( "edit frame's graph is " + Ops.showWholeCharGerGraph( next.TheGraph, false ) );
     notio.MatchResult m = notio.Graph.matchGraphs( 
     g.nxConcept.getReferent().getDescriptor(), 
     next.TheGraph.nxConcept.getReferent().getDescriptor(), 
     currentMatchingScheme() );
     Global.info( "current match scheme is " + MatchingSchemeFrame.toString( currentMatchingScheme()  ) );
     if ( m.matchSucceeded() )
     {
     notio.Graph matchng = m.getMappings()[0].getFirstGraph(); 
     //Global.info( "matched graph was " + 
     //	NotioTrans.generateCGIFString(Hub.KB,new notio.TranslationContext(),  matchng ) );
     EditFrame newef = new EditFrame( );
     if ( newef != null ) 
     {
     if ( Hub.enableEditFrameThreads ) new Thread( Hub.EditFrameThreadGroup, newef ).start();
     Graph match = new charger.obj.Graph( null );
     //if ( matchng.getContextGraph() != null ) matchng = matchng.getContextGraph();
     match = NotioTrans.notioGraphToCharGer( matchng, new charger.obj.Graph( null ) );
     Global.info( "match = " + Ops.showWholeCharGerGraph( match, true ) );
     Global.info( "TheGraph = " + Ops.showWholeCharGerGraph( ef.TheGraph, true ) );
     newef.attachGraphToFrame( next.graphName + "-match", match, true );
     //Global.info( "attached graph is " + 
     //			Ops.showWholeCharGerGraph( newef.TheGraph, true ) );
     }
     matchFound = true;
     }
     }
     }
     if ( ! matchFound ) 
     {
     EditFrame ef = g.getOwnerFrame();
     if ( ef != null )
     JOptionPane.showMessageDialog( (JFrame)ef, "Sorry, no match for " + ef.graphName + " found.", "Match", JOptionPane.INFORMATION_MESSAGE );
     }
						
     }
     * */

    /**
     * Operates a maximal join operation between graphs. Attempts to join, in
     * sequence, all the open graphs, according to a general matching scheme
     * that is internally defined at present. The joined graph is opened in a
     * new window.
     *
     * @param g The current graph
     */
    public void performActionMaxJoinWithOpenGraphs(Graph g) {
        Iterator opengraphs = Global.editFrameList.values().iterator();
        boolean joinMade = false;
        //notio.Graph joinGraph = g.nxConcept.getReferent().getDescriptor();
        //notio.Graph joinGraph = g.nxGraph;
        while (opengraphs.hasNext() && !joinMade) {
            EditFrame next = ((EditFrame) opengraphs.next());
            if (next != Global.getCurrentEditFrame()) {
                /*try {
                 //Global.info( "joining 1st graph from joining edit frame " + next.graphName );
                 //Global.info( "current graph is " + Ops.showWholeCharGerGraph( g, false ) );
                 //Global.info( "edit frame's graph is " + Ops.showWholeCharGerGraph( next.TheGraph, false ) );
                 notio.Graph[] allGraphs = notio.Graph.maximalJoin( joinGraph,
                 //next.TheKB.getOutermostContext().getReferent().getDescriptor(),
                 NotioTrans.CharGerToNotioGraph( next.TheGraph, Hub.KB, true, false ),
                 currentMatchingScheme(),
                 NotioTrans.myCopyingScheme() );
                 if ( ( allGraphs != null ) && ( allGraphs.length > 0 ) )
                 {
                 //Global.info( "A maxjoin was found.");
                 joinGraph = allGraphs[0];
                 joinMade = true;
                 }
                 else
                 {
                 //Global.info( "NO maxjoin was found.");
                 JOptionPane.showMessageDialog( (JFrame)ef, "No maximal join graph was found.", "Maximal Join", JOptionPane.INFORMATION_MESSAGE );
                 }
                 } catch ( notio.JoinException je ) 
                 { 
                 //Global.info("join exception (duh!) " + je.getMessage() );
                 JOptionPane.showMessageDialog( ef, je.getMessage(), "Join exception", JOptionPane.ERROR_MESSAGE ); 
                 }
                 catch ( CGGraphFormationError cgfe ) { }
                 */
            }
        }
        if (joinMade) {
            //Global.info( "joined graph is " + NotioTrans.generateCGIFString( Hub.KB, //ef.TheKB, 
            //		new notio.TranslationContext(),  joinGraph ) );
            EditFrame newef = new EditFrame();
            if (newef != null) {
                if (Global.enableEditFrameThreads) {
                    new Thread(Global.EditFrameThreadGroup, newef).start();    // added
                }                //Graph join = NotioTrans.notioGraphToCharGer( joinGraph, new charger.obj.Graph( null ) );
                //Global.info( "join = " + Ops.showWholeCharGerGraph( join, true ) );
                //Global.info( "TheGraph = " + Ops.showWholeCharGerGraph( newef.TheGraph, true ) );
                //newef.attachGraphToFrame( ef.graphName + "-join", join, true );
            }
        }
    }

    /**
     * Performs a join operation between graphs. Tries to join all the open
     * graphs, one by one in sequence, using the result of a previous join (if
     * any) in the subsequent joins. It uses a general matching scheme that is
     * internally defined at present. Intended to operate on the selected nodes
     * in all open graphs. If an open graph has no selected nodes, no join is
     * attempted. The joined graph is opened in a new window.
     *
     * @param g The current graph
     */
    public void performActionJoinWithOpenGraphsBROKEN(Graph g) {
        Iterator opengraphs = Global.editFrameList.values().iterator();
        boolean joinMade = false;
        //notio.Graph joinGraph = g.nxConcept.getReferent().getDescriptor();
        // notio.Graph joinGraph = g.nxGraph;
        while (opengraphs.hasNext() && !joinMade) {
            EditFrame next = ((EditFrame) opengraphs.next());
            if (next != Global.getCurrentEditFrame()) {
                //notio.Concept[] selectedNotioNodes = CGUtil.setSelectedNotioNodes( ef );
                /*try {
                 //Global.info( "joining 1st graph from joining edit frame " + next.graphName );
                 //Global.info( "current graph is " + Ops.showWholeCharGerGraph( g, false ) );
                 //Global.info( "edit frame's graph is " + Ops.showWholeCharGerGraph( next.TheGraph, false ) );
                 notio.Graph resultGraph = notio.Graph.join( joinGraph, selectedNotioNodes,
                 NotioTrans.CharGerToNotioGraph( next.TheGraph, Hub.KB, true, false ),
                 CGUtil.setSelectedNotioNodes( next ),
                 currentMatchingScheme(),
                 NotioTrans.myCopyingScheme() );
                 if ( resultGraph != null ) 
                 {
                 joinGraph = resultGraph;
                 joinMade = true;
                 }
                 else
                 {
                 JOptionPane.showMessageDialog( ef, 
                 "No join graph was found.", "Join" , JOptionPane.INFORMATION_MESSAGE );
                 }
                 } catch ( notio.JoinException je ) 
                 {
                 //Global.info("join exception (duh!) " + je.getMessage() ); 
                 JOptionPane.showMessageDialog( ef, je.getMessage(), "Join exception", JOptionPane.ERROR_MESSAGE ); 
                 }
                 catch ( CGGraphFormationError cgfe ) { }
                 */
            }
        }
        if (joinMade) {
            //Global.info( "joined graph is " + NotioTrans.generateCGIFString( Hub.KB, //ef.TheKB, 
            //		new notio.TranslationContext(),  joinGraph ) );
            EditFrame newef = new EditFrame();
            if (newef != null) {
                if (Global.enableEditFrameThreads) {
                    new Thread(Global.EditFrameThreadGroup, newef).start();    // added
                }
                //Graph join = NotioTrans.notioGraphToCharGer( joinGraph, new charger.obj.Graph( null ) );
                //Global.info( "join = " + Ops.showWholeCharGerGraph( join, true ) );
                //Global.info( "TheGraph = " + Ops.showWholeCharGerGraph( newef.TheGraph, true ) );
                //newef.attachGraphToFrame( ef.graphName + "-join", join, true );
            }
        }
    }

    /**
     * Used whenever a co-referent link appears, to bring referents into
     * consistency.
     */
    public boolean joinReferents(GraphObject go1, GraphObject go2) {
        boolean change = false;
        String ref1 = ((Concept) go1).getReferent();
        String ref2 = ((Concept) go2).getReferent();
        if (!ref2.equals(ref1)) {
            if (ref2.equals("")) {
                ((Concept) go2).setReferent(ref1, true);
                change = true;
            }
            if (ref1.equals("")) {
                ((Concept) go1).setReferent(ref2, true);
                change = true;
            }
        }
        return change;
    }

    /**
     * Removes any referents of the nodes in the selection. Leaves them
     * selected.
     *
     * @see EditManager#actionPerformed
     */
    public void performActionMakeGeneric() {
        // algorithm:
        //		enumerate all selected objects
        //		if object is selected, then execute a delete operation on it
        if (ef.somethingHasBeenSelected) {
            if (!ef.emgr.useNewUndoRedo) ef.emgr.makeHoldGraph();
            for (int k = 0; k < ef.EFSelectedObjects.size(); k++) {
                GraphObject go = (GraphObject) ef.EFSelectedObjects.get(k);
                if (go instanceof Concept) {
                    ((Concept) go).setTextLabel(((Concept) go).getTypeLabel());
                }
                if (go instanceof Graph) {
                    ((Graph) go).setTextLabel(((Graph) go).getTypeLabel());
                }
            }
            ef.emgr.setChangedContent(EditChange.SEMANTICS, EditChange.UNDOABLE);
            //ef.resetSelection();	// shouldn't be necessary; we already forgot all the selected objects
        } else {
            ef.displayOneLiner("Please select some thing(s) you want to make generic.");
        }
        ef.repaint();
    }

    /**
     * At the moment, doesn't do anything. Should it?
     *
     * @param g
     */
    public void performActionCommitToKB(Graph g) {
    }

    /**
     * Invokes
     * <code>matchMasterToOpenGraphs</code> using the current open graph as the
     * master.
     *
     * @see #matchMasterToOpenGraphs
     */
    public void performActionRelationMatching() {
        kb.matching.BinaryRelationMatchGroupMetrics matches = matchMasterToOpenGraphs(ef);

        String detail = "";

        // display detail for the master once
        kb.matching.BinaryRelationMatch match = new BinaryRelationMatch(ef.TheGraph, ef.getGraphName());


        AbstractTupleMatcher tupleMatcher = Global.knowledgeManager.createCurrentTupleMatcher();

        match.setTupleMatcher(tupleMatcher);
        // Here is where the BinaryRelationMatch class needs to provide some interface to talk to the
        // concept matcher.


        detail += match.masterDetailToHTML();

        String summary = matches.getCompleteSummaryHTML();
        ArrayList listOfMatches = matches.getMatches();
        for (int k = 0; k < listOfMatches.size(); k++) {
            detail += ((BinaryRelationMatch) listOfMatches.get(k)).matchedDetailToHTML();
        }

        GenericTextFrame f = new GenericTextFrame(ef, "Binary relation matching", ef.getFilename(), null, null);
        //  render as HTML
        String suggestedPath =
                Util.stripFileExtension(ef.graphAbsoluteFile.getAbsolutePath());
        File suggestedFile = new File(suggestedPath + ".html");
        //         HSD  ef.textFormDisplay.setSuggestedFile( suggestedFile );
        f.setSuggestedFile(suggestedFile);

        f.theText.setContentType("text/html");
        f.setTheText("<font face=\"sans-serif\" size=\"-1\">\n" + summary + detail);
        f.setVisible(true);

    }

    /**
     * Consider the set of open graphs and prepare a match table in HTML.
     *
     * @param masterForMatch edit frame containing the graph to be considered as
     *                       the master.
     * @see GenericTextFrame
     */
    public kb.matching.BinaryRelationMatchGroupMetrics matchMasterToOpenGraphs(EditFrame masterForMatch) {
        kb.matching.BinaryRelationMatchGroupMetrics matches =
                new kb.matching.BinaryRelationMatchGroupMetrics("Matches based on " + masterForMatch.getGraphName() + " as master.");


        // for each edit frame (other than the current one!) 
        Iterator graphiter = Global.editFrameList.values().iterator();

        AbstractTupleMatcher currentTupleMatcher = Global.knowledgeManager.createCurrentTupleMatcher();
        // Here would be a good place to call a method defined on the abstract tuple matcher
        // so that any matcher can perform additional setup (e.g., finding its synonym list).


        while (graphiter.hasNext()) {
            // create a new match each time so that the match group can do stats
            BinaryRelationMatch match = new BinaryRelationMatch(masterForMatch.TheGraph, masterForMatch.getGraphName());
            match.setTupleMatcher(currentTupleMatcher);
            EditFrame nextef = (EditFrame) graphiter.next();

            if (nextef != masterForMatch) {
                Graph g = nextef.TheGraph;
                match.matchAGraph(g, nextef.getGraphName());
                matches.addMatch(match);
                //summary += match.getSummaryHTML();
            }
        }
        graphiter = null;
        return matches;
    }

    /**
     * Consider the set of open graphs, take each one of them as a master and
     * find the graph that gives the best overall score as "master", then
     * display the results in HTML. Algorithm is based on the current binary
     * relation matching strategy. <ul> <li>for each open graph G: <ul>
     * <li>assume G is a mster <li>perform the usual binary match of all the
     * graphs <li>if the overall score is max, then save it </ul> <li>end for
     * each graph </ul>
     *
     * @see GenericTextFrame
     */
    public void performActionBestRelationMatching() {
        float maxOverall = 0.0f;
        kb.matching.BinaryRelationMatchGroupMetrics bestMatch = null;

        String detail = "";
        // for each edit frame 
        Iterator graphiter = Global.editFrameList.values().iterator();

        while (graphiter.hasNext()) {
            EditFrame currentef = (EditFrame) graphiter.next();
            kb.matching.BinaryRelationMatchGroupMetrics matches = matchMasterToOpenGraphs(currentef);
            matches.scanAllMatches();
            float score = matches.getOverallScore();
            if (score >= maxOverall) {
                maxOverall = score;
                bestMatch = matches;
            }
            //detail += match.masterDetailToHTML();

        }
        graphiter = null;

        String summary = "Identified best \"master\" graph in a group based on average precision-recall composite.<br>\n";

        summary += bestMatch.getCompleteSummaryHTML();

        GenericTextFrame f = new GenericTextFrame(ef, "Best binary relation matching", bestMatch.getName(), null, null);
        //  render as HTML
        String suggestedPath =
                Util.stripFileExtension(ef.graphAbsoluteFile.getAbsolutePath());
        File suggestedFile = new File(suggestedPath + ".html");
        //  HSD  ef.textFormDisplay.setSuggestedFile( suggestedFile );
        f.setSuggestedFile(suggestedFile);
        f.theText.setContentType("text/html");
        f.setTheText("<font face=\"sans-serif\" size=\"-1\">\n" + summary + detail);
        f.setVisible(true);
    }

    /**
     * This method launches a RuleSelectionFrame so users can start picking which rules to apply to the current CG
     *
     * @param theGraph By Bingyang Wei
     */
    public void performActionSelectRules(Graph theGraph) {
        // first, generate concept subgraph for the current CG
        //concept subgraph is a new field I added to class Graph
        theGraph.generateConceptSubgraphs();
        // second, start up a wizard to select rules and pass the current Edit Frame to the new frame
        try {
            Global.RuleSelectionFrame = new RulesSelectionFrame(ef);
        } catch (ExceptionInInitializerError e) {
            charger.Global.error("RuleFrame.setup error: can't create rules frame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is called after the button "Apply
     * Selected" in the Applying Rules to CGs frame is clicked.
     *
     * @param g     the graph on which we apply a list of selected rules
     * @param rules list of rules we are about to apply to the CG g
     */
    public void performActionApplyRules(Graph g, ArrayList<Graph> rules) {
        int count = 1;
        for (Graph r : rules) {
            System.out.println("Applying rule " + count + " :");
            applyRule(g, r);
            count++;
        }
        System.out.println("Rules have all been applied to the graph.");
    }

    /**
     * Before we apply a rule to a CG, we need to verify the current CG doesn't already
     * have the consequent part of the rule to be applied.
     * If so, we apply the rule,
     * otherwise, we don't apply the rule
     *
     * @param graph
     * @param rule
     * @return true if the current CG has the consequent part of the rule to be applied
     */
    public boolean isConsequentExistent(Graph graph, Rule rule) {
        Iterator iter = rule.getConsequent().graphObjects();
        GraphObject go = null;

        while (iter.hasNext()) {
            go = (GraphObject) iter.next();
            //Since graph is a kind of concept, we need to rule it out first
            if (go instanceof Graph) {
                continue;
            } else if (go instanceof Concept) {
                break;
            } else {
                continue;
            }
        }
        Concept c = (Concept) go;

        // second, get the type of c
        String type = c.getTypeLabel();

        // third, look up this type in Graph graph's conceptHashStore, the returned value is a graph of
        // concepts that share the same type
        if (graph.conceptHashStore.containsKey(type)) {
            Graph cg = graph.conceptHashStore.get(type);
            System.out.println("Looking for Consequent pattern in the current CG:");
            for (GraphObject go1 : cg.objectHashStore.values()) {
                if (match((GNode) go1, c)) {
                    return true;
                }
            }
        }
        System.out.println("No consequent is found in the current CG");
        return false;
    }

    /**
     * Apply one rule to a CG
     *
     * @param graph the graph on which rule is applied
     * @param rule  rule is in the form of if...then...
     */
    public void applyRule(Graph graph, Graph rule) {

        //The rule graph is parsed to a Rule object r which contains an antecedent and a consequent
        Rule r = parseRuleGraph(rule);

        // This snippet can print the concepts and relations in Antecedent
        // Iterator iter = r.getAntecedent().graphObjects();
        // while (iter.hasNext()) {
        // GraphObject go = (GraphObject) iter.next();
        // if (go instanceof Graph) {
        //
        // } else if (go instanceof GNode) {
        // String text = go.getTextLabel();
        // System.out.println(text + " ");
        // }
        // }

        //if the consequent of a rule is already in the current CG, there is no need to apply the rule
        if (!isConsequentExistent(graph, r)) {
            // look for the structure of antecedent of r in the conceptHashStore of
            // graph and update the current graph with consequent of r

            // first, obtain a random concept from the antecedent of the rule
            Iterator iter = r.getAntecedent().graphObjects();
            GraphObject go = null;

            while (iter.hasNext()) {
                go = (GraphObject) iter.next();
                //Since graph is a kind of concept, we need to rule it out first
                if (go instanceof Graph) {
                    continue;
                } else if (go instanceof Concept) {
                    break;
                } else {
                    continue;
                }
            }
            Concept c = (Concept) go;

            // second, get the type of c
            String type = c.getTypeLabel();

            /*
            third, look up this type in Graph graph's conceptHashStore, the returned value is a graph of concepts           that share the same typeight now, we haven't consider subtype and supertype e.g. if a graph only has              subtype Student, and we are trying to apply a rule that only has supertype Person, we ought to apply              it, but this method is NOT doing that right now, this is a TODO
             */
            if (graph.conceptHashStore.containsKey(type)) {
                //cg has all concepts that share the exact same type
                Graph cg = graph.conceptHashStore.get(type);
                matchAll(cg, c, r);
            } else {
                System.out.println("Cannot apply this rule.");
            }

        } else {
            System.out.println("This rule doesn't need to be applied, either it has been applied or the consequent is already in the current CG");
        }
    }

    /**
     * The method goes over every cg's concept, which has the same type as c (from the antecedent of a rule)
     * The goal is to see if a pattern starts at a concept in cg can match the antecedent of the rule
     * An issue is we may unnecessarily apply the rule more times than needed!
     * TODO
     *
     * @param cg a graph of concepts with the same type C
     * @param c  a concept with type C from the antecedent of a rule
     */
    private void matchAll(Graph cg, Concept c, Rule rule) {
        // go is a concept in graph that has the same type of c
        // we want to investigate from concept go
        for (GraphObject go : cg.objectHashStore.values()) {
            //if there is a match, we need to add the consequent part of the rule to the current CG
            if (match((GNode) go, c)) {
                addConsequent(ef.TheGraph, rule);
            }
        }
    }

    /**
     * Given a CG, adding the consequent part graph of rule to it.
     *
     * @param currentGraph
     * @param rule
     */
    private void addConsequent(Graph currentGraph, Rule rule) {
        //we do have to clone the rule.consequent
        addAugmentedGraphObjects(currentGraph, rule.getConsequent());
        //after this, we make the edit frame bottom right corner "red", so user needs to save the new graph
        ef.emgr.setChangedContent(EditChange.SEMANTICS, EditChange.UNDOABLE);
    }

    /**
     * Clone consequent into currentGraph
     * Deep copy
     *
     * @param currentGraph
     * @param consequent
     */
    public void addAugmentedGraphObjects(Graph currentGraph, Graph consequent) {
        // first, obtain a random GNode from consequent graph
        Iterator iter = consequent.graphObjects();
        GraphObject go = null;

        while (iter.hasNext()) {
            go = (GraphObject) iter.next();
            //Since graph is a kind of concept, we need to rule it out first
            if (go instanceof Graph) {
                continue;
            } else if (go instanceof Concept) {
                break;
            } else {
                continue;
            }
        }
        Concept c = (Concept) go;//one concept in consequent graph

        Concept c_cpy = new Concept();
        c_cpy.setTextLabel(c.getTextLabel());
        c_cpy.setReferent(c.getReferent());
        c_cpy.setTypeLabel(c.getTypeLabel());
        c_cpy.setCenter(c.getCenter());
        c_cpy.setBackground(Color.gray);

        // before matching go and c, create data structures to keep track of
        // discovered and visited nodes
        //1 is for consequent graph
        //2 is for clone
        HashMap<String, GNode> discovered1 = new HashMap<String, GNode>();
        HashMap<String, GNode> discovered2 = new HashMap<String, GNode>();
        HashMap<String, GNode> visited1 = new HashMap<String, GNode>();
        HashMap<String, GNode> visited2 = new HashMap<String, GNode>();
        Queue<GNode> q1 = new LinkedList<GNode>();
        Queue<GNode> q2 = new LinkedList<GNode>();

        discovered1.put(c.objectID.toString(), c);
        discovered2.put(c_cpy.objectID.toString(), c_cpy);

        q1.add(c);
        q2.add(c_cpy);

        while (!q1.isEmpty()) {
            GNode n = q1.remove();
            GNode n_cpy = q2.remove();

            ArrayList edgesR = n.getEdges();//many edges here
            ArrayList edgesG = n_cpy.getEdges();//empty the first time

            //go over every edge
            for (Object o : edgesR) {
                GEdge edge = (GEdge) o;
                GEdge edge_cpy = null;

                if (n == edge.fromObj) {
                    //if the node at toObj is NOT already discovered
                    //we need create a copy of it
                    if (!visited1.containsKey(edge.toObj.objectID.toString()) && !discovered1.containsKey(edge.toObj.objectID.toString())) {
                        discovered1.put(edge.toObj.objectID.toString(), (GNode) edge.toObj);

                        if (edge.toObj instanceof Graph) {
                            //TODO
                            addAugmentedGraphObjects(currentGraph, (Graph) edge.toObj);
                        } else if (edge.toObj instanceof Concept) {
                            Concept conceptRuleNeighbor = (Concept) edge.toObj;// nodeRuleNeighbor is a neighbor of nRule
                            Concept concept_cpy = new Concept();
                            concept_cpy.setTextLabel(conceptRuleNeighbor.getTextLabel());
                            concept_cpy.setReferent(conceptRuleNeighbor.getReferent());
                            concept_cpy.setTypeLabel(conceptRuleNeighbor.getTypeLabel());
                            concept_cpy.setCenter(conceptRuleNeighbor.getCenter());
                            concept_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(n_cpy, concept_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(n_cpy, concept_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }

                            q1.add((Concept) edge.toObj);
                            q2.add(concept_cpy);

                        } else if (edge.toObj instanceof Relation) {
                            Relation relationRuleNeighbor = (Relation) edge.toObj;
                            Relation relation_cpy = new Relation();
                            relation_cpy.setTextLabel(relationRuleNeighbor.getTextLabel());
                            relation_cpy.setTypeLabel(relationRuleNeighbor.getTypeLabel());
                            relation_cpy.setCenter(relationRuleNeighbor.getCenter());
                            relation_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(n_cpy, relation_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(n_cpy, relation_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }

                            q1.add((Relation) edge.toObj);
                            q2.add(relation_cpy);

                        } else if (edge.toObj instanceof Actor) {
                            Actor actorRuleNeighbor = (Actor) edge.toObj;
                            Actor actor_cpy = new Actor();
                            actor_cpy.setTextLabel(actorRuleNeighbor.getTextLabel());
                            actor_cpy.setTypeLabel(actorRuleNeighbor.getTypeLabel());
                            actor_cpy.setCenter(actorRuleNeighbor.getCenter());
                            actor_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(n_cpy, actor_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(n_cpy, actor_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }
                            q1.add((Actor) edge.toObj);
                            q2.add(actor_cpy);
                        }
                    }
                } else {//if n == edge.toObj
                    //if the node at fromObj is NOT already discovered
                    //we need create a copy of it
                    if (!visited1.containsKey(edge.fromObj.objectID.toString()) && !discovered1.containsKey(edge.fromObj.objectID.toString())) {
                        discovered1.put(edge.fromObj.objectID.toString(), (GNode) edge.fromObj);
                        if (edge.fromObj instanceof Graph) {
                            //TODO
                            addAugmentedGraphObjects(currentGraph, (Graph) edge.fromObj);
                        } else if (edge.fromObj instanceof Concept) {
                            Concept conceptRuleNeighbor = (Concept) edge.fromObj;// nodeRuleNeighbor is a neighbor of nRule
                            Concept concept_cpy = new Concept();
                            concept_cpy.setTextLabel(conceptRuleNeighbor.getTextLabel());
                            concept_cpy.setReferent(conceptRuleNeighbor.getReferent());
                            concept_cpy.setTypeLabel(conceptRuleNeighbor.getTypeLabel());
                            concept_cpy.setCenter(conceptRuleNeighbor.getCenter());
                            concept_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(concept_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(concept_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }
                            q1.add((Concept) edge.fromObj);
                            q2.add(concept_cpy);

                        } else if (edge.fromObj instanceof Relation) {
                            Relation relationRuleNeighbor = (Relation) edge.fromObj;
                            Relation relation_cpy = new Relation();
                            relation_cpy.setTextLabel(relationRuleNeighbor.getTextLabel());
                            relation_cpy.setTypeLabel(relationRuleNeighbor.getTypeLabel());
                            relation_cpy.setCenter(relationRuleNeighbor.getCenter());
                            relation_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(relation_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(relation_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }
                            q1.add((Relation) edge.fromObj);
                            q2.add(relation_cpy);
                        } else if (edge.fromObj instanceof Actor) {
                            Actor actorRuleNeighbor = (Actor) edge.fromObj;
                            Actor actor_cpy = new Actor();
                            actor_cpy.setTextLabel(actorRuleNeighbor.getTextLabel());
                            actor_cpy.setTypeLabel(actorRuleNeighbor.getTypeLabel());
                            actor_cpy.setCenter(actorRuleNeighbor.getCenter());
                            actor_cpy.setBackground(Color.gray);

                            if (edge instanceof Arrow) {
                                edge_cpy = new Arrow(actor_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            } else if (edge instanceof Coref) {
                                edge_cpy = new Coref(actor_cpy, n_cpy);
                                currentGraph.insertObject(edge_cpy);
                            }
                            q1.add((Actor) edge.fromObj);
                            q2.add(actor_cpy);
                        }
                    }
                }
                //add edge_cpy to n_cpy
                if (edge_cpy != null)
                    n_cpy.getEdges().add(edge_cpy);
            }
            visited1.put(n.objectID.toString(), n);
            visited2.put(n_cpy.objectID.toString(), n_cpy);
            currentGraph.insertObject(n_cpy);//add this GNode to the current graph, edges have already been added in above code
        }//end of while
    }//end of method

    /**
     * Core subgraph isomorphism algorithm. Looks for the antecedent pattern in Graph cg
     * Starting from a concept c of the antecedent of the rule, this method traverse both graphs to see
     * if a match exists or not.
     *
     * @param nG a node in a conceptual graph
     * @param nR a node in the antecedent of a rule
     * @return true if a subgraph match is found
     */
    private boolean match(GNode nG, GNode nR) {

        int flag = 0;// flag is 0 if no mismatch is found, flag is 1 is a mismatch is found
        //then, stop immediately
        /*
            before matching nG and nR, create data structures to keep track of
            discovered and visited nodes
            1 is for graph
            2 is for antecedent of the rule
         */
        HashMap<String, GNode> discovered1 = new HashMap<String, GNode>();
        HashMap<String, GNode> discovered2 = new HashMap<String, GNode>();
        HashMap<String, GNode> visited1 = new HashMap<String, GNode>();
        HashMap<String, GNode> visited2 = new HashMap<String, GNode>();
        //FIFO queue
        Queue<GNode> q1 = new LinkedList<GNode>();
        Queue<GNode> q2 = new LinkedList<GNode>();

        //First nodes discovered in graph and antecedent are the nG and nR
        discovered1.put(nG.objectID.toString(), nG);
        discovered2.put(nR.objectID.toString(), nR);

        //Since they are also matched, we put them on queues
        q1.add(nG);
        q2.add(nR);

        //this while loop is used to find the match, but once a mismatch is found (flag == 1), break
        while (!q2.isEmpty() && flag == 0) {
            GNode nRule = q2.remove();
            GNode nGraph = q1.remove();

            // get all neighbors of nRule, FROM in fromListR and TO in toListR
            ArrayList fromListR = nRule.getLinkedNodes(GEdge.Direction.FROM);
            ArrayList toListR = nRule.getLinkedNodes(GEdge.Direction.TO);

            // get all neighbors of nGraph, FROM in fromListG and TO in toListG
            ArrayList fromListG = nGraph.getLinkedNodes(GEdge.Direction.FROM);
            ArrayList toListG = nGraph.getLinkedNodes(GEdge.Direction.TO);

            // match fromListR to fromListG
            /*
            There are two ways that this for loop terminate:
            1. for each concept in fromListR, we can find a match in fromListG
            2. for one concept in fromListR, we cannot find a match, this makes flag == 1
            so even though the next for loop aobut toListR is OK, we are not accepting it.
             */
            for (Object o : fromListR) {
                GNode nodeRuleNeighbor = (GNode) o;// nodeRuleNeighbor is a neighbor of nRule

                // if this is the first time we discover this node in rule graph
                if (!visited2.containsKey(nodeRuleNeighbor.objectID.toString())
                        && !discovered2.containsKey(nodeRuleNeighbor.objectID.toString())) {
                    discovered2.put(nodeRuleNeighbor.objectID.toString(), nodeRuleNeighbor);

                    // see if we can find a matching concept in fromListG for nodeRuleNeighbor
                    GNode n = null;
                    boolean isMatch = false;
                    for (Object object : fromListG) {
                        n = (GNode) object;
                        if (!visited1.containsKey(n.objectID.toString())
                                && !discovered1.containsKey(n.objectID.toString())
                                && n.getTypeLabel().equals(nodeRuleNeighbor.getTypeLabel())) {
                            discovered1.put(n.objectID.toString(), n);
                            isMatch = true;
                            break;//if we find one, break
                        }
                    }
                    // after we traverse through from list, we didn't find a
                    // match,terminate everything
                    if (!isMatch) {
                        flag = 1;
                        break;
                    }
                    q2.add(nodeRuleNeighbor);
                    q1.add(n);
                }
            }

            //no need to verify the toListR
            if (flag == 1)
                break;

            // match toListR to toListG
            for (Object o : toListR) {
                GNode nodeRuleNeighbor = (GNode) o;// nodeRule is a neighbor of nRule

                // if this is the first time we discover this node in rule graph
                if (!visited2.containsKey(nodeRuleNeighbor.objectID.toString())
                        && !discovered2.containsKey(nodeRuleNeighbor.objectID.toString())) {
                    discovered2.put(nodeRuleNeighbor.objectID.toString(), nodeRuleNeighbor);

                    // see if we can find a matching concept in toListG for nodeRuleNeighbor
                    GNode n = null;
                    boolean isMatch = false;
                    for (Object object : toListG) {
                        n = (GNode) object;
                        if (!visited1.containsKey(n.objectID.toString())
                                && !discovered1.containsKey(n.objectID.toString())
                                && n.getTypeLabel().equals(nodeRuleNeighbor.getTypeLabel())) {
                            discovered1.put(n.objectID.toString(), n);
                            isMatch = true;
                            break;
                        }
                    }
                    // after we traverse through tolist, we didn't find a
                    // match,terminate everything
                    if (!isMatch) {
                        flag = 1;
                        break;
                    }
                    q2.add(nodeRuleNeighbor);
                    q1.add(n);
                }
            }

            //After match fromListR to fromListG, toListR to toListG, we can add nRule and nGraph to visited queues
            visited2.put(nRule.objectID.toString(), nRule);
            visited1.put(nGraph.objectID.toString(), nGraph);
        }//end of while loop

        //if the flag is still 0, we find a match
        if (flag == 0)
            return true;
        else
            return false;
    }

    /**
     * return a rule object that contains the Antecedent and Consequent contexts
     * as two graphs
     * <p>
     * I think if there is a coref link from outside to inside of a context, it is treated as the same graph
     * We need to figure this out
     * TODO
     *
     * @param rule
     * @return
     */
    private Rule parseRuleGraph(Graph rule) {
        Rule result = new Rule();
        Iterator iter = rule.graphObjects();
        while (iter.hasNext()) {
            GraphObject go = (GraphObject) iter.next();
            // since we require that a file only contains one if...then... rule
            // there will be only two graphs, one if_then relation and maybe
            // other concepts used to coreference.
            if (go instanceof Graph) {
                Graph g = (Graph) go;
                // if the graph "points to" relation "if_then", this is
                // antecedent
                if (g.getLinkedNodes(GEdge.Direction.FROM).size() == 0) {
                    result.setAntecedent(g);
                } else {
                    result.setConsequent(g);
                }
            } else if (go instanceof Concept) {
                // figure out co-reference!!!
                // TODO
            }
        }
        return result;
    }
    // end by Bingyang Wei
} // class end
