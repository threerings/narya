//
// $Id: FrameRepaintManager.java,v 1.4 2002/04/27 19:12:00 mdb Exp $

package com.threerings.media;

import java.applet.Applet;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.CellRendererPane;
import javax.swing.JButton;
import javax.swing.JComponent;
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

        // locate the validation root for this component
        for (Component c = comp; c != null; c = c.getParent()) {
            // if the component is not part of an active widget hierarcy,
            // we can stop now; if the component is a cell render pane,
            // we're apparently supposed to ignore it as wel
            if (c.getPeer() == null || c instanceof CellRendererPane) {
                return;
            }

            // skip non-Swing components
            if (!(c instanceof JComponent)) {
                continue;
            }

            // if we find our validate root, we can stop looking
            if (((JComponent)c).isValidateRoot()) {
                vroot = c;
                break;
            }
        }

        // if we found no validation root we can abort as this component
        // is not part of any valid widget hierarchy
        if (vroot == null) {
//             Log.info("Skipping vrootless component: " + comp);
            return;
        }

        // make sure that the component is actually in a window or applet
        // that is showing
	if (getRoot(vroot) == null) {
//             Log.info("Skipping rootless component: " + comp + "/" + vroot);
	    return;
	}

        // add the invalid component to our list and we'll validate it on
        // the next frame
        if (!ListUtil.contains(_invalid, vroot)) {
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
	    if (!c.isVisible() || c.getPeer() == null) {
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
//                 Log.info("Validating " + invalid[ii]);
                ((Component)invalid[ii]).validate();
            }
        }
    }

    /**
     * Paints the components that have become dirty since the last tick.
     */
    public void paintComponents (Graphics g)
    {
        // swap out our hashmap
        synchronized (this) {
            HashMap tmap = _spare;
            _spare = _dirty;
            _dirty = tmap;
        }

        // now paint each of the dirty components, by setting the clipping
        // rectangle appropriately and calling paint() on the associated
        // root component
        Iterator iter = _spare.entrySet().iterator();
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

            // climb up the parent heirarchy, looking for the first opaque
            // parent as well as the root component
            for (Component c = comp; c != null; c = c.getParent()) {
                if (!c.isVisible() || c.getPeer() == null) {
                    break;
                }

                if (!(c instanceof JComponent)) {
                    root = c;
                    break;
                }

                // make a note of the first opaque parent we find
                if (ocomp == null && ((JComponent)c).isOpaque()) {
                    ocomp = c;
                    // we need to obtain the opaque parent's coordinates
                    // in the root coordinate system for when we repaint
                    _cbounds.setBounds(
                        0, 0, ocomp.getWidth(), ocomp.getHeight());
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
//                 Log.info("Repainting [comp=" + comp.getClass().getName() +
//                          StringUtil.toString(_cbounds) +
//                          ", drect=" + StringUtil.toString(drect) + "].");
                g.setClip(drect);
                g.translate(_cbounds.x, _cbounds.y);
                ocomp.paint(g);
                g.translate(-_cbounds.x, -_cbounds.y);

            } else if (root != null) {
//                 Log.info("Repainting old-school " +
//                          "[comp=" + comp.getClass().getName() + "].");
                // otherwise, repaint with standard swing double buffers
                Image obuf = getOffscreenBuffer(
                    comp, _cbounds.width, _cbounds.height);
                Graphics og = null, cg = null;
                try {
                    og = obuf.getGraphics();
                    ocomp.paint(og);
                    cg = comp.getGraphics();
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
    }

    /**
     * Locates the root component for the supplied component, translates
     * the supplied dirty rectangle into the root components' coordinate
     * system and fills in the component's bounds in the root coordinate
     * system.
     */
    protected Component getRoot (JComponent comp, Rectangle dirty,
                                 Rectangle bounds)
    {

        return null;
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
}
