//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.samskivert.util.RunAnywhere;

import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.Mirage;
import com.threerings.media.util.MathUtil;
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
    /** The code for the pathable following mode wherein we keep the view
     * centered on the pathable's location. */
    public static final byte CENTER_ON_PATHABLE = 0;

    /** The code for the pathable following mode wherein we ensure that
     * the marked pathable is always kept within the visible bounds of the
     * view. */
    public static final byte ENCLOSE_PATHABLE = 1;

    /** The code for the pathable following mode wherein we set the upper-left
     * corner of the view to the coordinates of the pathable. */
    public static final byte TRACK_PATHABLE = 2;

    /**
     * Constructs a virtual media panel.
     */
    public VirtualMediaPanel (FrameManager framemgr)
    {
        super(framemgr);
    }

    /**
     * Set a background image to tile the background of the media panel.
     */
    public void setBackground (Mirage background)
    {
        _background = background;
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
     * Returns the bounds of the viewport in virtual coordinates. The
     * returned rectangle must <em>not</em> be modified.
     */
    public Rectangle getViewBounds ()
    {
        return _vbounds;
    }

    /**
     * Adds an entity that will be informed when the view scrolls.
     */
    public void addViewTracker (ViewTracker tracker)
    {
        _trackers.add(tracker);
    }

    /**
     * Removes an entity from the view trackers list.
     */
    public void removeViewTracker (ViewTracker tracker)
    {
        _trackers.remove(tracker);
    }
    /**
     * Instructs the view to follow the supplied pathable; ensuring that
     * the view's coordinates are adjusted according to the follow mode.
     *
     * @param pable the pathable to follow.
     * @param followMode the strategy for keeping the pathable in view.
     */
    public void setFollowsPathable (Pathable pable, byte followMode)
    {
        _fmode = followMode;
        _fpath = pable;
        trackPathable(); // immediately update our location
    }

    /**
     * Clears out the pathable that was being enclosed or followed due to
     * a previous call to {@link #setFollowsPathable}.
     */
    public void clearPathable ()
    {
        _fpath = null;
        _fmode = (byte) -1;
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseEvent (MouseEvent event)
    {
        event.translatePoint(_vbounds.x, _vbounds.y);
        super.processMouseEvent(event);
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseMotionEvent (MouseEvent event)
    {
        event.translatePoint(_vbounds.x, _vbounds.y);
        super.processMouseMotionEvent(event);
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseWheelEvent (MouseWheelEvent event)
    {
        event.translatePoint(_vbounds.x, _vbounds.y);
        super.processMouseWheelEvent(event);
    }

    // documentation inherited
    protected void dirtyScreenRect (Rectangle rect)
    {
        // translate the screen rect into happy coordinates
        rect.translate(_vbounds.x, _vbounds.y);
        _remgr.addDirtyRegion(rect);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // we need to obtain our absolute screen coordinates to work
        // around the Windows copyArea() bug
        findRootBounds();
    }

    // documentation inherited
    public void setBounds (int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        // keep track of the size of the viewport
        _vbounds.width = getWidth();
        _vbounds.height = getHeight();

        // we need to obtain our absolute screen coordinates to work
        // around the Windows copyArea() bug
        findRootBounds();
    }

    /**
     * Determines the absolute screen coordinates at which this panel is
     * located and stores them for reference later when rendering.  This
     * is necessary in order to work around the Windows
     * <code>copyArea()</code> bug.
     */
    protected void findRootBounds ()
    {
        _abounds.setLocation(0, 0);
        FrameManager.getRoot(this, _abounds);
    }

    // documentation inherited
    protected void didTick (long tickStamp)
    {
        super.didTick(tickStamp);

        int width = getWidth(), height = getHeight();

        // adjusts our view location to track any pathable we might be
        // tracking
        trackPathable();

        // if we have a new target location, we'll need to generate dirty
        // regions for the area exposed by the scrolling
        if (_nx != _vbounds.x || _ny != _vbounds.y) {
            // determine how far we'll be moving on this tick
            int dx = _nx - _vbounds.x, dy = _ny - _vbounds.y;

//             Log.info("Scrolling into place [n=(" + _nx + ", " + _ny +
//                      "), t=(" + _vbounds.x + ", " + _vbounds.y +
//                      "), d=(" + dx + ", " + dy +
//                      "), width=" + width + ", height=" + height + "].");


            // Mac OS X's redraw breaks on scrolling, so we repaint the
            // entire panel 
            if (RunAnywhere.isMacOS()) {
                _remgr.invalidateRegion(_nx, _ny, width, height);
            } else {
                _dx = dx;
                _dy = dy;

                // these are used to prevent the vertical strip from
                // overlapping the horizontal strip
                int sy = _ny, shei = height;

                // and add invalid rectangles for the exposed areas
                if (dy > 0) {
                    shei = Math.max(shei - dy, 0);
                    _remgr.invalidateRegion(_nx, _ny + height - dy, width, dy);
                } else if (dy < 0) {
                    sy -= dy;
                    _remgr.invalidateRegion(_nx, _ny, width, -dy);
                }
                if (dx > 0) {
                    _remgr.invalidateRegion(_nx + width - dx, sy, dx, shei);
                } else if (dx < 0) {
                    _remgr.invalidateRegion(_nx, sy, -dx, shei);
                }
            }


            // now go ahead and update our location so that changes in
            // between here and the call to paint() for this tick don't
            // booch everything
            _vbounds.x = _nx; _vbounds.y = _ny;

            // let derived classes react if they so desire
            viewLocationDidChange(dx, dy);
        }
    }

    /**
     * Called during our tick when we have adjusted the view location. The
     * {@link _vbounds} will already have been updated to reflect our new
     * view coordinates.
     *
     * @param dx the delta scrolled in the x direction (in pixels).
     * @param dy the delta scrolled in the y direction (in pixels).
     */
    protected void viewLocationDidChange (int dx, int dy)
    {
        if (_perfRect != null) {
            Rectangle sdirty = new Rectangle(_perfRect);
            sdirty.translate(-dx, -dy);
            dirtyScreenRect(sdirty);
        }

        // inform our view trackers
        for (int ii = 0, ll = _trackers.size(); ii < ll; ii++) {
            ((ViewTracker)_trackers.get(ii)).viewLocationDidChange(dx, dy);
        }

        // let our sprites and animations know what's up
        _animmgr.viewLocationDidChange(dx, dy);
        _spritemgr.viewLocationDidChange(dx, dy);
    }

    /**
     * Implements the standard pathable tracking support. Derived classes
     * may wish to override this if they desire custom tracking
     * functionality.
     */
    protected void trackPathable ()
    {
        // if we're tracking a pathable, adjust our view coordinates
        if (_fpath == null) {
            return;
        }

        int width = getWidth(), height = getHeight();
        int nx = _vbounds.x, ny = _vbounds.y;

        // figure out where to move
        switch (_fmode) {
        case TRACK_PATHABLE:
            nx = _fpath.getX();
            ny = _fpath.getY();
            break;

        case CENTER_ON_PATHABLE:
            nx = _fpath.getX() - width/2;
            ny = _fpath.getY() - height/2;
            break;

        case ENCLOSE_PATHABLE:
            Rectangle bounds = _fpath.getBounds();
            if (nx > bounds.x) {
                nx = bounds.x;
            } else if (nx + width < bounds.x + bounds.width) {
                nx = bounds.x + bounds.width - width;
            }
            if (ny > bounds.y) {
                ny = bounds.y;
            } else if (ny + height < bounds.y + bounds.height) {
                ny = bounds.y + bounds.height - height;
            }
            break;

        default:
            Log.warning("Eh? Set to invalid pathable mode " +
                        "[mode=" + _fmode + "].");
            break;
        }

//         Log.info("Tracking pathable [mode=" + _fmode +
//                  ", pable=" + _fpath + ", nx=" + nx + ", ny=" + ny + "].");

        setViewLocation(nx, ny);
    }

    // documentation inherited
    protected void paint (Graphics2D gfx, Rectangle[] dirty)
    {
        // if we're scrolling, go ahead and do the business
        if (_dx != 0 || _dy != 0) {
            int width = getWidth(), height = getHeight();
            int cx = (_dx > 0) ? _dx : 0;
            int cy = (_dy > 0) ? _dy : 0;

            // set the clip to the bounds of the component (we can't
            // assume the clip is anything sensible upon entry to paint()
            // because the frame manager expects us to set our own clip)
            gfx.setClip(0, 0, width, height);

            // on windows, attempting to call copyArea() on a translated
            // graphics context results in boochness; so we have to
            // untranslate the graphics context, do our copyArea() and
            // then translate it back
            if (RunAnywhere.isWindows()) {
                gfx.translate(-_abounds.x, -_abounds.y);
                gfx.copyArea(_abounds.x + cx, _abounds.y + cy,
                             width - Math.abs(_dx),
                             height - Math.abs(_dy), -_dx, -_dy);
                gfx.translate(_abounds.x, _abounds.y);
            } else if (RunAnywhere.isMacOS()) {
                try {
                    gfx.copyArea(cx, cy,
                             width - Math.abs(_dx),
                             height - Math.abs(_dy), -_dx, -_dy);
                } catch (Exception e) {
                    // HACK when it throws an exception trying to do the
                    // copy area, just repaint everything
                    dirty = new Rectangle[] { new Rectangle(_vbounds) };
                }    
            } else {
                gfx.copyArea(cx, cy,
                             width - Math.abs(_dx),
                             height - Math.abs(_dy), -_dx, -_dy);
            }

            // and clear out our scroll deltas
            _dx = 0; _dy = 0;
        }

        // translate into happy space
        gfx.translate(-_vbounds.x, -_vbounds.y);

        // now do the actual painting
        super.paint(gfx, dirty);

        // translate back out of happy space
        gfx.translate(_vbounds.x, _vbounds.y);
    }

    // documentation inherited
    protected void constrainToBounds (Rectangle dirty)
    {
        SwingUtilities.computeIntersection(
            _vbounds.x, _vbounds.y, getWidth(), getHeight(), dirty);
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        // if we have a background image specified, tile it!
        if (_background != null) {
            // make sure it's aligned
            int iw = _background.getWidth();
            int ih = _background.getHeight();
            int lowx = iw * MathUtil.floorDiv(dirtyRect.x, iw);
            int lowy = ih * MathUtil.floorDiv(dirtyRect.y, ih);
            ImageUtil.tileImage(gfx, _background, lowx, lowy,
                dirtyRect.width + (dirtyRect.x - lowx),
                dirtyRect.height + (dirtyRect.y - lowy));
        }
    }

    /** Our viewport bounds in virtual coordinates. */
    protected Rectangle _vbounds = new Rectangle();

    /** Our target offsets to be effected on the next tick. */
    protected int _nx, _ny;

    /** Our scroll offsets. */
    protected int _dx, _dy;

    /** Our tiling background image. */
    protected Mirage _background;

    /** The mode we're using when following a pathable. */
    protected byte _fmode = -1;

    /** The pathable being followed. */
    protected Pathable _fpath;

    /** We need to know our absolute coordinates in order to work around
     * the Windows copyArea() bug. */
    protected Rectangle _abounds = new Rectangle();

    /** A list of entities to be informed when the view scrolls. */
    protected ArrayList _trackers = new ArrayList();
}
