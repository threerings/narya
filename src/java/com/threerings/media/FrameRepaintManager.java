//
// $Id: FrameRepaintManager.java,v 1.20 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media;

import java.applet.Applet;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

/**
 * Used to get Swing's repainting to jive with our active rendering
 * strategy.
 *
 * @see FrameManager
 */
public class FrameRepaintManager extends RepaintManager
{
    /**
     * Components that are rooted in this frame will be rendered into the
     * offscreen buffer managed by the frame manager. Other components
     * will be rendered into separate offscreen buffers and repainted in
     * the normal Swing manner.
     */
    public FrameRepaintManager (Frame frame)
    {
        _rootFrame = frame;
    }

    // documentation inherited
    public synchronized void addInvalidComponent (JComponent comp) 
    {
        Component vroot = null;
        if (DEBUG) {
            Log.info("Maybe invalidating " + toString(comp) + ".");
        }

        // locate the validation root for this component
        for (Component c = comp; c != null; c = c.getParent()) {
            // if the component is not part of an active widget hierarcy,
            // we can stop now; if the component is a cell render pane,
            // we're apparently supposed to ignore it as wel
            if (!c.isDisplayable() || c instanceof CellRendererPane) {
                return;
            }

            // skip non-Swing components
            if (!(c instanceof JComponent)) {
                continue;
            }

            // if we find our validate root, we can stop looking; NOTE:
            // JTextField incorrectly claims to be a validate root thereby
            // fucking up the program something serious; we jovially
            // ignore it's claims here and restore order to the universe;
            // see bug #403550 for more fallout from Sun's fuckup
            if (!(c instanceof JTextField) &&
                !(c instanceof JScrollPane) &&
                ((JComponent)c).isValidateRoot()) {
                vroot = c;
                break;
            }
        }

        // if we found no validation root we can abort as this component
        // is not part of any valid widget hierarchy
        if (vroot == null) {
            if (DEBUG) {
                Log.info("Skipping vrootless component: " + toString(comp));
            }
            
            return;
        }

        // make sure that the component is actually in a window or applet
        // that is showing
	if (getRoot(vroot) == null) {
            if (DEBUG) {
                Log.info("Skipping rootless component " +
                         "[comp=" + toString(comp) +
                         ", vroot=" + toString(vroot) + "].");
            }
	    return;
	}

        // add the invalid component to our list and we'll validate it on
        // the next frame
        if (!ListUtil.contains(_invalid, vroot)) {
            if (DEBUG) {
                Log.info("Invalidating " + toString(vroot) + ".");
            }
            _invalid = ListUtil.add(_invalid, vroot);
        }
    }

    // documentation inherited
    public synchronized void addDirtyRegion (
        JComponent comp, int x, int y, int width, int height) 
    {
        // ignore invalid requests
        if ((width <= 0) || (height <= 0) || (comp == null) ||
            (comp.getWidth() <= 0) || (comp.getHeight() <= 0)) {
//             Log.info("Skipping bogus region " + comp.getClass().getName() +
//                      ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ".");
            return;
        }

        // if this component is already dirty, simply expand their
        // existing dirty rectangle
	Rectangle drect = (Rectangle)_dirty.get(comp);
	if (drect != null) {
            drect.add(x, y);
            drect.add(x+width, y+height);
	    return;
	}

        // make sure this component has a valid root
        if (getRoot(comp) == null) {
//             Log.info("Skipping rootless repaint " + comp + ".");
            return;
        }

        if (DEBUG) {
            Log.info("Dirtying component [comp=" + toString(comp) +
                     ", drect=" + StringUtil.toString(
                         new Rectangle(x, y, width, height)) + "].");
        }

        // if we made it this far, we can queue up a dirty region for this
        // component to be repainted on the next tick
        _dirty.put(comp, new Rectangle(x, y, width, height));
    }

    /**
     * Returns the root component for the supplied component or null if it
     * is not part of a rooted hierarchy or if any parent along the way is
     * found to be hidden or without a peer.
     */
    protected Component getRoot (Component comp)
    {
	for (Component c = comp; c != null; c = c.getParent()) {
	    if (!c.isVisible() || !c.isDisplayable()) {
		return null;
	    }
            if (c instanceof Window || c instanceof Applet) {
		return c;
	    }
	}
        return null;
    }

    // documentation inherited
    public synchronized Rectangle getDirtyRegion (JComponent comp)
    {
	Rectangle drect = (Rectangle)_dirty.get(comp);
        // copy the rectangle if we found one, otherwise create an empty
        // rectangle because we don't want them leaving empty handed
        return (drect == null) ?
            new Rectangle(0, 0, 0, 0) : new Rectangle(drect);
    }
	    
    // documentation inherited
    public synchronized void markCompletelyClean (JComponent comp)
    {
        _dirty.remove(comp);
    }

    /**
     * Validates the invalid components that have been queued up since the
     * last frame tick.
     */
    public void validateComponents ()
    {
        // swap out our invalid array
        Object[] invalid = null;
        synchronized (this) {
            invalid = _invalid;
            _invalid = null;
        }

        // if there's nothing to validate, we're home free
        if (invalid == null) {
            return;
        }

        // validate everything therein
        int icount = invalid.length;
        for (int ii = 0; ii < icount; ii++) {
            if (invalid[ii] != null) {
                if (DEBUG) {
                    Log.info("Validating " + invalid[ii]);
                }
                ((Component)invalid[ii]).validate();
            }
        }
    }

    /**
     * Paints the components that have become dirty since the last tick.
     *
     * @return true if any components were painted.
     */
    public boolean paintComponents (Graphics g, FrameManager fmgr)
    {
        synchronized (this) {
            // exit now if there are no dirty rectangles to paint
            if (_dirty.isEmpty()) {
                return false;
            }

            // otherwise, swap our hashmaps
            HashMap tmap = _spare;
            _spare = _dirty;
            _dirty = tmap;
        }

        // scan through the list, looking for components for whom a parent
        // component is also dirty. in such a case, the dirty rectangle
        // for the parent component is expanded to contain the dirty
        // rectangle of the child and the child is removed from the
        // repaint list (painting the parent will repaint the child)
        Iterator iter = _spare.entrySet().iterator();
      PRUNE:
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            JComponent comp = (JComponent)entry.getKey();
            Rectangle drect = (Rectangle)entry.getValue();
            int x = comp.getX() + drect.x, y = comp.getY() + drect.y;

            // climb up the parent hierarchy, looking for the first opaque
            // parent as well as the root component
            for (Component c = comp.getParent(); c != null; c = c.getParent()) {
                // stop looking for combinable parents for non-visible or
                // non-JComponents
                if (!c.isVisible() || !c.isDisplayable() ||
                    !(c instanceof JComponent)) {
                    break;
                }

                // check to see if this parent is dirty
                Rectangle prect = (Rectangle)_spare.get(c);
                if (prect != null) {
                    // that we were going to merge it with its parent and
                    // blow it away
                    drect.x = x;
                    drect.y = y;

                    if (DEBUG) {
                        Log.info("Found dirty parent " +
                                 "[comp=" + toString(comp) +
                                 ", drect=" + StringUtil.toString(drect) +
                                 ", pcomp=" + toString(c) + 
                                 ", prect=" + StringUtil.toString(prect) +
                                 "].");
                    }
                    prect.add(drect);

                    if (DEBUG) {
                        Log.info("New prect " + StringUtil.toString(prect));
                    }

                    // remove the child component and be on our way
                    iter.remove();
                    continue PRUNE;
                }

                // translate the coordinates into this component's
                // coordinate system
                x += c.getX();
                y += c.getY();
            }
        }

        // now paint each of the dirty components, by setting the clipping
        // rectangle appropriately and calling paint() on the associated
        // root component
        iter = _spare.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            JComponent comp = (JComponent)entry.getKey();
            Rectangle drect = (Rectangle)entry.getValue();

            // get the root component, adjust the clipping (dirty)
            // rectangle and obtain the bounds of the client in absolute
            // coordinates
            Component root = null, ocomp = null;

            // start with the components bounds which we'll switch to the
            // opaque parent component's bounds if and when we find one
            _cbounds.setBounds(0, 0, comp.getWidth(), comp.getHeight());

            // climb up the parent hierarchy, looking for the first opaque
            // parent as well as the root component
            for (Component c = comp; c != null; c = c.getParent()) {
                if (!c.isVisible() || !c.isDisplayable()) {
                    break;
                }

                if (c instanceof JComponent) {
                    // make a note of the first opaque parent we find
                    if (ocomp == null && ((JComponent)c).isOpaque()) {
                        ocomp = c;
                        // we need to obtain the opaque parent's coordinates
                        // in the root coordinate system for when we repaint
                        _cbounds.setBounds(
                            0, 0, ocomp.getWidth(), ocomp.getHeight());
                    }

                } else {
                    // oh god the hackery. apparently the fscking
                    // JEditorPane wraps a heavy weight component around
                    // every swing component it uses when doing forms
                    Component tp = c.getParent();
                    if (!(tp instanceof JEditorPane)) {
                        root = c;
                        break;
                    }
                }

                // translate the coordinates into this component's
                // coordinate system
                drect.x += c.getX();
                drect.y += c.getY();
                _cbounds.x += c.getX();
                _cbounds.y += c.getY();

                // clip the dirty region to the bounds of this component
                SwingUtilities.computeIntersection(
                    c.getX(), c.getY(), c.getWidth(), c.getHeight(), drect);
            }

            // if we found no opaque parent, just paint the component
            // itself (this seems to happen with the top-level layered
            // pane)
            if (ocomp == null) {
                ocomp = comp;
            }

            // if this component is rooted in our frame, repaint it into
            // the supplied graphics instance
            if (root == _rootFrame) {
                if (DEBUG) {
                    Log.info("Repainting [comp=" + toString(comp) +
                             StringUtil.toString(_cbounds) +
                             ", ocomp=" + toString(ocomp) +
                             ", drect=" + StringUtil.toString(drect) + "].");
                }

                g.setClip(drect);
                g.translate(_cbounds.x, _cbounds.y);
                try {
                    // some components are ill-behaved and may throw an
                    // exception while painting themselves, and so we
                    // needs must deal with these fellows gracefully
                    ocomp.paint(g);

                } catch (Exception e) {
                    Log.warning("Exception while painting component " +
                                "[comp=" + ocomp + ", e=" + e + "].");
                    Log.logStackTrace(e);
                }
                g.translate(-_cbounds.x, -_cbounds.y);

                // we also need to repaint any components in this layer
                // that are above our freshly repainted component
                fmgr.renderLayers((Graphics2D)g, ocomp, _cbounds, _clipped);

            } else if (root != null) {
                if (DEBUG) {
                    Log.info("Repainting old-school " +
                             "[comp=" + toString(comp) +
                             ", ocomp=" + toString(ocomp) +
                             ", root=" + toString(root) +
                             ", bounds=" + StringUtil.toString(_cbounds) +
                             "].");
                    dumpHierarchy(comp);
                }

                // otherwise, repaint with standard swing double buffers
                Image obuf = getOffscreenBuffer(
                    ocomp, _cbounds.width, _cbounds.height);
                Graphics og = null, cg = null;
                try {
                    og = obuf.getGraphics();
                    ocomp.paint(og);
                    cg = ocomp.getGraphics();
                    cg.drawImage(obuf, 0, 0, null);

                } finally {
                    if (og != null) {
                        og.dispose();
                    }
                    if (cg != null) {
                        cg.dispose();
                    }
                }
            }
        }

        // clear out the mapping of dirty components
        _spare.clear();

        return true;
    }

    /**
     * Used to dump a component when debugging.
     */
    protected static String toString (Component comp)
    {
        return comp.getClass().getName() +
            StringUtil.toString(comp.getBounds());
    }

    /**
     * Dumps the containment hierarchy for the supplied component.
     */
    protected static void dumpHierarchy (Component comp)
    {
        for (String indent = ""; comp != null; indent += " ") {
            Log.info(indent + toString(comp));
            comp = comp.getParent();
        }
    }

    /** The managed frame. */
    protected Frame _rootFrame;

    /** A list of invalid components. */
    protected Object[] _invalid;

    /** A mapping of invalid rectangles for each widget that is dirty. */
    protected HashMap _dirty = new HashMap();

    /** A spare hashmap that we swap in while repainting dirty components
     * in the old hashmap. */
    protected HashMap _spare = new HashMap();

    /** Used to compute dirty components' bounds. */
    protected Rectangle _cbounds = new Rectangle();

    /** Used when rendering "layered" components. */
    protected boolean[] _clipped = new boolean[] { true };

    /** We debug so much that we have to make it easy to enable and
     * disable debug logging. Yay! */
    protected static final boolean DEBUG = false;
}
