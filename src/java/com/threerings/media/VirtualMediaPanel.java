//
// $Id: VirtualMediaPanel.java,v 1.2 2002/06/18 23:34:34 mdb Exp $

package com.threerings.media;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.samskivert.util.StringUtil;

import com.threerings.media.util.Path;
import com.threerings.media.util.Pathable;

/**
 * Extends the base media panel with the notion of a virtual coordinate
 * system. All entities in the virtual media panel have virtual
 * coordinates and the virtual media panel displays a window into that
 * virtual view. The panel can be made to scroll by adjusting the view
 * offset slightly at the start of each tick and it will efficiently copy
 * the unmodified view data and generate repaint requests for the exposed
 * regions.
 */
public class VirtualMediaPanel extends MediaPanel
{
    /**
     * Constructs a virtual media panel.
     */
    public VirtualMediaPanel (FrameManager framemgr)
    {
        super(framemgr);
    }

    /**
     * Sets the upper-left coordinate of the view port in virtual
     * coordinates. The view will be as efficient as possible about
     * repainting itself to achieve this new virtual location (meaning
     * that if we need only to move one pixel to the left, it will use
     * {@link Graphics#copyArea} to move our rendered view over one pixel
     * and generate a dirty region for the exposed area). The new location
     * will not take effect until the view is {@link #tick}ed, so only the
     * last call to this method during a tick will have any effect.
     */
    public void setViewLocation (int x, int y)
    {
        // make a note of our new x and y offsets
        _nx = x;
        _ny = y;
    }

    /**
     * Instructs the view to follow the supplied pathable; ensuring that
     * the pathable always remains in the center of the view. The virtual
     * coordinates will be adjusted after every tick to center the view on
     * the sprite.
     */
    public void setFollowsPathable (Pathable pable)
    {
        _fpath = pable;
    }

    /**
     * Configures a region of interest which will be displayed in the
     * center of the viewport in situations where the media panel's actual
     * size is smaller than its view size.
     *
     * @param region the region of interest or null if there is to be no
     * region of interest (in which case the view will simply be
     * centered).
     */
    public void setRegionOfInterest (Rectangle region)
    {
        _interest = region;
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseEvent (MouseEvent event)
    {
        event.translatePoint(_tx, _ty);
        super.processMouseEvent(event);
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseMotionEvent (MouseEvent event)
    {
        event.translatePoint(_tx, _ty);
        super.processMouseMotionEvent(event);
    }

    // documentation inherited
    protected void dirtyScreenRect (Rectangle rect)
    {
        // translate the screen rect into happy coordinates
        rect.translate(_tx, _ty);
        _remgr.addDirtyRegion(rect);
    }

    // documentation inherited
    protected void didTick (long tickStamp)
    {
        super.didTick(tickStamp);

        int width = getWidth(), height = getHeight();

        // if we're following a pathable, adjust our view coordinates such
        // that the pathable is at the center of the view
        if (_fpath != null) {
            setViewLocation(_fpath.getX() - width/2, _fpath.getY() - height/2);
        }

        // if we have a new target location, we'll need to generate dirty
        // regions for the area exposed by the scrolling
        if (_nx != _tx || _ny != _ty) {
            // determine how far we'll be moving on this tick
            _dx = _nx - _tx; _dy = _ny - _ty;

            // these are used to prevent the vertical strip from
            // overlapping the horizontal strip
            int sy = _ny, shei = height;

            // and add invalid rectangles for the exposed areas
            if (_dy > 0) {
                shei -= _dy;
                _remgr.invalidateRegion(_nx, _ny + height - _dy, width, _dy);
            } else if (_dy < 0) {
                sy -= _dy;
                _remgr.invalidateRegion(_nx, _ny, width, -_dy);
            }
            if (_dx > 0) {
                _remgr.invalidateRegion(_nx + width - _dx, sy, _dx, shei);
            } else if (_dx < 0) {
                _remgr.invalidateRegion(_nx, sy, -_dx, shei);
            }

            // now go ahead and update our location so that changes in
            // between here and the call to paint() for this tick don't
            // booch everything
            _tx = _nx; _ty = _ny;

//             Log.info("Scrolling into place " +
//                      "[dx=" + _dx + ", dy=" + _dy + "].");
        }
    }

    // documentation inherited
    protected void paint (Graphics2D gfx, Rectangle[] dirty)
    {
        int width = getWidth(), height = getHeight();
        int dcount = dirty.length;

        // if we're scrolling, go ahead and do the business
        if (_dx != 0 || _dy != 0) {
            int cx = (_dx > 0) ? _dx : 0;
            int cy = (_dy > 0) ? _dy : 0;
            gfx.copyArea(cx, cy, width - Math.abs(_dx),
                         height - Math.abs(_dy), -_dx, -_dy);
            // and clear out our scroll deltas
            _dx = 0; _dy = 0;
        }

        // clip the dirty regions to the viewport
        for (int ii = 0; ii < dcount; ii++) {
            SwingUtilities.computeIntersection(
                _tx, _ty, width, height, dirty[ii]);
//             Log.info("Intersected [rect=" + StringUtil.toString(dirty[ii]) +
//                      ", tx=" + _tx + ", ty=" + _ty + "].");
        }

        // translate into happy space
        gfx.translate(-_tx, -_ty);

        // now do the actual painting
        super.paint(gfx, dirty);

        // translate back out of happy space
        gfx.translate(_tx, _ty);
    }

    /**
     * Used to compute our viewport offsets.
     */
    protected int centerWithInterest (int len, int vlen, int ix, int ilen)
    {
        // start out by centering on the region of interest
        int tx = (ix - (len - ilen)/2);
        // make sure that didn't push us off of the screen
        return Math.min(Math.max(tx, 0), vlen-len);
    }

    /** Our viewport offsets. */
    protected int _tx, _ty;

    /** Our target offsets to be effected on the next tick. */
    protected int _nx, _ny;

    protected int _dx, _dy;

    /** A region of interest which we'll try to keep visible. */
    protected Rectangle _interest;

    /** The pathable being followed. */
    protected Pathable _fpath;
}
