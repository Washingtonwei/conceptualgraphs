package charger.obj;

import charger.gloss.AbstractTypeDescriptor;
import charger.*;
import charger.act.*;
import charger.util.CGUtil;
import kb.KnowledgeBase;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
//import notio.*;

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
 * A GNode is the abstract superclass to handle concepts, relations, actors and
 * nested graphs.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 *         Harry S. Delugach.
 */
abstract public class GNode extends GraphObject {

    static Font showBoringDebugInfoFont = new Font("Arial", Font.BOLD, 11);
    /**
     * List of GEdges by which this GNode is attached to other nodes.
     */
    protected ArrayList myedges = new ArrayList();
    // used in drawing dashed border for type and relation labels
    protected static float dashpattern[] = {1.0f, 4.0f};
    /**
     * If "outlines" are selected (i.e., turn on Hub.showBorders) this is how
     * wide the border should be
     */
    public static float borderOutlineWidth = 2.5f;
    GMark mark = new GMark();
    String typeLabel = null;
    /**
     * Some reference that helps describe this node. Currently used for a
     * TypeDescriptor
     * <p>
     * see charger.obj.AbstractTypeDescriptor
     */
    protected ArrayList descriptors = new ArrayList();

    public GNode() {
        if (myKind == GraphObject.Kind.ALL) {
            myKind = GraphObject.Kind.GNODE;
        }
        displayRect.setFrame(displayRect.x, displayRect.y, (float) GraphObject.defaultDim.width, (float) GraphObject.defaultDim.height);
    }

    /**
     * Does the marker indicate object has changed?
     *
     * @return true if ready
     */
    public boolean isChanged() {
        return mark.isChanged();
    }

    /**
     * Does the marker indicate an active input concept to an actor?
     */
    public boolean isActive() {
        return mark.isActive();
    }

    /**
     * Sets this node's mark to indicate that it's been changed.
     *
     * @param b whether to tell the mark that the node is changed or not.
     * @see GMark#setChanged
     */
    public void setChanged(boolean b) {
        mark.setChanged(b);
    }

    /**
     * Used when activating CG actors to mark this node as active in a sequence
     * of firings.
     *
     * @param b
     */
    public void setActive(boolean b) {
        mark.setActive(b);
    }

    /**
     * Returns the GEdges associated with this GNode.
     *
     * @return vector of GEdges.
     */
    public ArrayList getEdges() {
        return myedges;
    }

    /**
     * Returns a list of GNodes that are either "input" or "output" to the
     * target node.
     *
     * @param direction either <code>GEdge.Direction.FROM</code> (for nodes linked with
     *                  arrows directed toward the target node, or <code>GEdge.Direction.TO</code> (for
     *                  nodes linked with arrows directed away from the target node).
     * @return the list of nodes that are connected in the appropriate direction
     * @see GEdge.Direction
     */
    public ArrayList getLinkedNodes(GEdge.Direction direction) {
        ArrayList holder = new ArrayList();
        Iterator iter = getEdges().iterator();
        while (iter.hasNext()) {
            GEdge ge = (GEdge) iter.next();
            if (ge.howLinked(this) == GEdge.Direction.TO) {
                if (direction == GEdge.Direction.FROM) {
                    holder.add(ge.fromObj);
                }
            } else if (ge.howLinked(this) == GEdge.Direction.FROM) {
                if (direction == GEdge.Direction.TO) {
                    holder.add(ge.toObj);
                }
            }
        }
        return holder;
    }

    /**
     * By Bingyang Wei
     * Returns a list of GNodes that are either "input" or "output" to the
     * target node.
     *
     * @return the list of nodes that are connected
     */
    public ArrayList getLinkedNodes() {
        ArrayList holder = new ArrayList();
        Iterator iter = getEdges().iterator();
        while (iter.hasNext()) {
            GEdge ge = (GEdge) iter.next();
            if (ge.howLinked(this) == GEdge.Direction.TO) {
                holder.add(ge.fromObj);
            } else if (ge.howLinked(this) == GEdge.Direction.FROM) {
                holder.add(ge.toObj);
            }
        }
        return holder;
    }

    /**
     * Returns a list of GNodes that are either "input" or "output" to the
     * target node, except coref linked nodes
     * This one is different from get LinkedNodes which returns everything
     *
     * @param direction
     * @return
     */
    public ArrayList getArrowLinkedNodes(GEdge.Direction direction) {
        ArrayList holder = new ArrayList();
        Iterator iter = getEdges().iterator();
        while (iter.hasNext()) {
            GEdge ge = (GEdge) iter.next();
            if (!(ge instanceof Coref)) {
                if (ge.howLinked(this) == GEdge.Direction.TO) {
                    if (direction == GEdge.Direction.FROM) {
                        holder.add(ge.fromObj);
                    }
                } else if (ge.howLinked(this) == GEdge.Direction.FROM) {
                    if (direction == GEdge.Direction.TO) {
                        holder.add(ge.toObj);
                    }
                }
            }
        }
        return holder;
    }

    /**
     * Allocate and initialize a notio node that is consistent with the CharGer
     * node. Assumes that the CharGer node is correct.
     *
     * @param kb Notio knowledge base, to be used for consistency checking. HSD
     * this should be replaced by a new call to the charger kb
     */
    //REMOVE-NOTIO abstract void updateForNotio(  notio.KnowledgeBase kb );

    /**
     * Handles the translation necessary for providing links to a CGIF form.
     * Assembled the input,output referents, and string-ifies them in order.
     *
     * @return Completed string, suitable for inserting after a CGIF
     * relation/actor label.
     */
    protected String CGIFStringGNodeEdges() {
        String returnstring = "";
        Iterator edges = myedges.iterator();
        ArrayList ins = new ArrayList();
        ArrayList outs = new ArrayList();

        // assemble all the input and output object id ? referents 
        while (edges.hasNext()) {
            GEdge ge = (GEdge) edges.next();
            if (ge.toObj == this) {
                ins.add("?x" + ge.fromObj.objectID);
            } else {
                outs.add("?x" + ge.toObj.objectID);
            }
        }

        Iterator identifiers;

        // construct referent list for the input id referents
        identifiers = ins.iterator();
        while (identifiers.hasNext()) {
            returnstring = returnstring + " " + identifiers.next();
        }
        // separator between input and output id referents
        returnstring = returnstring + " | ";

        // construct referent list for the output id referents
        identifiers = outs.iterator();
        while (identifiers.hasNext()) {
            returnstring = returnstring + " " + identifiers.next();
        }

        return returnstring;
    }

    /**
     * Sets the position of this node to be its center. Used to force
     * re-evaluation of its dimensions and the dimensions of its links. Same as
     * setCenter( getCenter() ).
     *
     * @see GNode#setCenter
     */
    public void setCenter() {
        this.setCenter(this.getCenter());
    }

    /**
     * Sets the position of a node and makes the rest of its dimensions
     * consistent. Assumes that the current dimensions are to be preserved.
     * Should probably check here to see if the pos is within some reasonable
     * rectangle.
     *
     * @param    p    the new center point, although internally the top left corner is
     * saved as the position.
     */
    public void setCenter(Point2D.Double p) {
        super.setCenter(p);

        adjustEdges();
    }

    /**
     * Adjust all this node's connecting edges so that they are positioned
     * correctly.
     */
    public void adjustEdges() {
        //charger.Global.info( "adjusting " + myedges.size() + " edges for node " + getTextLabel() );
        int k;
        for (k = 0; k < myedges.size(); k++) {
            ((GEdge) myedges.get(k)).placeEdge();
        }
    }

    /**
     * Disconnects itself from any other GNodes, by telling each of its edges to
     * erase itself.
     */
    public void abandonObject() {
        // tell all connected edges to disconnect themselves
        while (!myedges.isEmpty()) {
            GEdge ToBeRemoved = (GEdge) (myedges.get(0));
            ToBeRemoved.getOwnerGraph().forgetObject(ToBeRemoved);
        }
    }

    protected void finalize() throws Throwable {
        try {

            super.finalize();
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }

    /**
     * Includes the already-allocated GEdge in the target node
     *
     * @param ge the allocated and set-up GEdge
     */
    public void attachGEdge(GEdge ge) {
        // if ge has a name, insert the edge in order of its name.
        int walker = 0;
        while (walker < myedges.size()
                && ((GEdge) myedges.get(walker)).getTextLabel().compareTo(ge.getTextLabel()) <= 0) {
            walker++;
        }
        myedges.add(walker, ge);
    }

    /**
     * Removes the GEdge from the node to which it is attached.
     */
    public void deleteGEdge(GEdge ge) {
        if (myedges.contains(ge)) {
            boolean b = myedges.remove(ge);
            //Global.info( "deleteGEdge, object id " + ge.objectID );
        }
    }

    /**
     * Handles generic drawing for all nodes, and usually invokes the node's
     * class method "draw"
     */
    public void draw(Graphics2D g, boolean printing) {
        if (isChanged()) {
            //drawHighlighted( g, Color.green );
        }
        if (Global.showBorders) {
            drawBorder(g, getColor("text"));
        }

        if (Global.ShowBoringDebugInfo) {
            drawDebuggingInfo(g);
        }

        if (isSelected && !printing) {
//            drawBorder( g, Color.black );
            drawBorder(g, EditFrame.selectionRectColor);
        } else if (isActive()) {
            //drawActivated( g );
            //drawBorder( g, Color.red );
            // draw a semi-transparent red shape over the node
            Shape s = getShape();
            Color save = g.getColor();
            g.setColor(new Color(255, 0, 0, 127));
            g.fill(s);
            g.setColor(Color.red);
            g.draw(s);
            g.setColor(save);
        }
    }

    public void drawBorder(Graphics2D g, Color borderColor) {
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2.5f));
        g.draw(getShape());
        g.setStroke(Global.defaultStroke);
    }

    /* protected void drawHighlighted( Graphics2D g, Color highlightColor )
     {
     Color save = g.getColor();
     g.setColor( highlightColor );
     g.drawString( "XXXXXXX", getCenter().x, getCenter().y );
     g.setColor( save );
     }
     */

    /**
     * Swap the fore and background, draw again, then restore fore and back
     */
    public void drawActivated(Graphics2D g) {
        Color temp = getColor("text");
        foreColor = getColor("fill");
        backColor = temp;
        draw(g, false);
        setColor("fill", getColor("text"));
        setColor("text", temp);
    }

    public void drawDebuggingInfo(Graphics2D g) {
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setFont(showBoringDebugInfoFont);
        g.setColor(Color.BLACK);
        int fontHeight = g.getFontMetrics().getHeight() + 2;
        // use floats because that's how drawString is defined
        g.drawString("id:" + objectID.getShort(), (float) getUpperLeft().x - 2, (float) getUpperLeft().y + fontHeight);
        CGUtil.showPoint(g, getUpperLeft());
//            g.drawString( "(" + Math.round( getUpperLeft().x) + "," + Math.round(getUpperLeft().y) + ")",
//                    (float)getUpperLeft().x, (float)getUpperLeft().y - 2 );
        g.drawString(Math.round(getDisplayRect().width) + "",
                (float) (getUpperLeft().x + getDisplayRect().width / 2), (float) (getUpperLeft().y + getDisplayRect().height + fontHeight));
        g.drawString(Math.round(getDisplayRect().height) + "",
                (float) (getUpperLeft().x + getDisplayRect().width + 3), (float) (getUpperLeft().y + getDisplayRect().height / 2));
        g.setFont(oldFont);
        g.setColor(oldColor);
    }


    public Dimension getSize() {
        return getDim();
    }

    public Rectangle2D.Double getDisplayRect() {
        return super.getDisplayRect();
    }

    /**
     * Get just the type label portion of the concept's text label.
     *
     * @return a type label (for GNodes, the default is the text label;
     * subclasses must over-ride )
     */
    public String getTypeLabel() {
        return textLabel;
    }

    public void setTypeLabel(String s, boolean resize) {
        textLabel = s;
        typeLabel = s;
        if (resize) {
            resizeIfNecessary();
        }

    }

    public void setTypeLabel(String s) {
        boolean resize = false;
        if (getOwnerGraph() != null && getOwnerGraph().getOwnerFrame() != null) {
            resize = true;
        }
        setTypeLabel(s, resize);
    }

    public AbstractTypeDescriptor[] getTypeDescriptors() {
        return (AbstractTypeDescriptor[]) descriptors.toArray(new AbstractTypeDescriptor[0]);
    }

    public void clearDescriptors() {
        descriptors.clear();
    }

    public void setTypeDescriptors(AbstractTypeDescriptor[] ds) {
        clearDescriptors();
        descriptors.addAll(Arrays.asList(ds));
    }

    /**
     * Convenience method to return the first type descriptor.
     */
    public AbstractTypeDescriptor getTypeDescriptor() {
        if (descriptors.size() == 0) {
            return null;
        } else {
            return (AbstractTypeDescriptor) descriptors.get(0);
        }
    }

    /**
     * Sets the first type descriptor of this node.
     */
    public void setTypeDescriptor(AbstractTypeDescriptor o) {
        if (o == null) {
            descriptors.clear();
        }
        if (descriptors.size() == 0) {
            descriptors.add(o);
        } else {
            descriptors.set(0, o);
        }
    }

    /**
     * By Bingyang Wei
     * Returns true if this node is linked to equivalenet nodes through a coref link
     *
     * @return
     */
    public boolean hasCorefEdge() {
        ArrayList<GEdge> edges = getEdges();
        for (GEdge e : edges) {
            if (e instanceof Coref)
                return true;
        }
        return false;
    }

//    public boolean commitToKnowledgeBase( kb.KnowledgeBase kb ) {
//        return true;
//    }
//
//    public boolean unCommitFromKnowledgeBase( kb.KnowledgeBase kb ) {
//        return true;
//    }
}
