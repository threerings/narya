//
// $Id: MediaPanel.java,v 1.2 2002/04/23 03:10:39 mdb Exp $

package com.threerings.media;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.event.AncestorEvent;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.StringUtil;

import com.threerings.media.FrameManager;
import com.threerings.media.Log;

import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationManager;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;

/**
 * Provides a useful extensible framework for rendering animated displays
 * that use sprites and animations. Sprites and animations can be added to
 * this panel and they will automatically be ticked and rendered (see
 * {@link #addSprite} and {@link #addAnimation}).
 *
 * <p> To facilitate optimized sprite and animation rendering, the panel
 * provides a dirty region manager which is used to only repaint dirtied
 * regions on each frame. Derived classes can add dirty regions to the
 * scene and/or augment the dirty regions created by moving sprites and
 * changing animations.
 *
 * <p> Sprite and animation rendering is done in two layers: front and
 * back. Callbacks are provided to render behind the back layer ({@link
 * #paintBehind}), in between front and back ({@link #paintBetween}) and
 * in front of the front layer ({@link #paintInFront}).
 *
 * <p> The animated panel automatically registers with the {@link
 * FrameManager} to participate in the frame tick. It only does so while
 * it is a visible part of the UI hierarchy, so animations and sprites are
 * paused while the animated panel is hidden.
 */
public class MediaPanel extends JComponent
    implements FrameParticipant, MediaConstants
{
    /**
     * Constructs an animated panel.
     */
    public MediaPanel (FrameManager framemgr)
    {
        // keep this for later
        _framemgr = framemgr;

        // create our region manager
        _remgr = new RegionManager();

        // create our animation and sprite managers
        _animmgr = new AnimationManager(_remgr);
        _spritemgr = new SpriteManager(_remgr);

        // we don't want to hear about repaints
        setIgnoreRepaint(true);

        // participate in the frame when we're visible
        addAncestorListener(new AncestorAdapter() {
            public void ancestorAdded (AncestorEvent event) {
                _framemgr.registerFrameParticipant(MediaPanel.this);
            }
            public void ancestorRemoved (AncestorEvent event) {
                _framemgr.removeFrameParticipant(MediaPanel.this);
            }
        });
    }

    /**
     * Instructs the view to scroll by the specified number of pixels
     * (which can be negative if it should scroll in the negative x or y
     * direction) in the specified number of milliseconds. While the view
     * is scrolling, derived classes can hear about the scrolled
     * increments by overriding {@link #viewWillScroll} and they can find
     * out when scrolling is complete by overriding {@link
     * #viewFinishedScrolling}.
     *
     * @param dx the number of pixels in the x direction to scroll.
     * @param dy the number of pixels in the y direction to scroll.
     * @param millis the number of milliseconds in which to do it. The
     * scrolling is calculated such that we will "drop frames" in order to
     * scroll the necessary distance by the requested time.
     */
    public void setScrolling (int dx, int dy, long millis)
    {
        // if dx and dy are zero, we've got nothing to do
        if (dx == 0 && dy == 0) {
            return;
        }

        // set our scrolling parameters
        _scrollx = dx;
        _scrolly = dy;
        _last = System.currentTimeMillis();
        _ttime = _last + millis;

//         // figure out the lesser (but non-zero) of the two scroll deltas
//         int absx = Math.abs(dx), absy = Math.abs(dy);
//         int mindist = absx;
//         if (absx == 0) {
//             mindist = absy;
//         } else if (absy == 0) {
//             mindist = absx;
//         } else {
//             mindist = Math.min(absx, absy);
//         }

//         // let the animation manager know how "fast" we'll be scrolling so
//         // that it can determine its frame rate
//         int mspp = (int)(millis/mindist);
//         _animmgr.setScrolling(mspp);

//         Log.info("Scrolling [dx=" + dx + ", dy=" + dy +
//                  ", millis=" + millis + "ms, mspp=" + mspp + "].");
    }

    /**
     * Returns a reference to the region manager that is used to
     * accumulate dirty regions each frame.
     */
    public RegionManager getRegionManager ()
    {
        return _remgr;
    }

    /**
     * Adds a sprite to this panel.
     */
    public void addSprite (Sprite sprite)
    {
        _spritemgr.addSprite(sprite);
    }

    /**
     * Removes a sprite from this panel.
     */
    public void removeSprite (Sprite sprite)
    {
        _spritemgr.removeSprite(sprite);
    }

    /**
     * Adds an animation to this panel.
     */
    public void addAnimation (Animation anim)
    {
        _animmgr.registerAnimation(anim);
    }

    /**
     * Removes an animation from this panel.
     */
    public void removeAnimation (Animation anim)
    {
        _animmgr.unregisterAnimation(anim);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // figure out our viewport offsets
        Dimension size = getSize(), vsize = getViewSize();
        _tx = (vsize.width - size.width)/2;
        _ty = (vsize.height - size.height)/2;

//         Log.info("Size: " + size + ", vsize: " + vsize +
//                  ", tx: " + _ty + ", ty: " + _ty + ".");
    }

    // documentation inherited
    public void invalidate ()
    {
        super.invalidate();
        // invalidate our bounds with the region manager
        _remgr.invalidateRegion(getBounds());
    }

    // documentation inherited from interface
    public void tick (long tickStamp)
    {
        int width = getWidth(), height = getHeight();

        // if scrolling is enabled, determine the scrolling delta to be
        // used and do the business
        _dx = 0;
        _dy = 0;
        if (_ttime != 0) {
            // if we've blown past our allotted time, we want to scroll
            // the rest of the way
            if (tickStamp > _ttime) {
                _dx = _scrollx;
                _dy = _scrolly;

//                 Log.info("Scrolling rest [dx=" + dx + ", dy=" + dy + "].");

            } else {
                // otherwise figure out how many milliseconds have gone by
                // since we last scrolled and scroll the requisite amount
                float dt = (float)(tickStamp - _last);
                float rt = (float)(_ttime - _last);

                // our delta is the remaining distance multiplied by the
                // time delta divided by the remaining time
                _dx = Math.round((float)(_scrollx * dt) / rt);
                _dy = Math.round((float)(_scrolly * dt) / rt);

//                 Log.info("Scrolling delta [dt=" + dt + ", rt=" + rt +
//                          ", dx=" + dx + ", dy=" + dy + "].");
            }

            // and add invalid rectangles for the exposed areas
            if (_dx > 0) {
                _remgr.invalidateRegion(width - _dx + _tx, _ty, _dx, height);
            } else if (_dx < 0) {
                _remgr.invalidateRegion(_tx, _ty, -_dx, height);
            }
            if (_dy > 0) {
                _remgr.invalidateRegion(_tx, height - _dy + _ty, width, _dy);
            } else if (_dy < 0) {
                _remgr.invalidateRegion(_tx, _ty, width, -_dy);
            }

            // make sure we're actually scrolling before telling people
            // about it
            if (_dx != 0 || _dy != 0) {
                // if we are working with a sprite manager, let it know
                // that we're about to scroll out from under its sprites
                // and allow it to provide us with more dirty rects
                if (_spritemgr != null) {
                    _spritemgr.viewWillScroll(_dx, _dy);
                }

                // let our derived classes do whatever they need to do to
                // prepare to be scrolled
                viewWillScroll(_dx, _dy, tickStamp);

                // keep track of the last time we scrolled
                _last = tickStamp;

                // subtract our scrolled deltas from the distance remaining
                _scrollx -= _dx;
                _scrolly -= _dy;

                // if we've reached our desired position, finish the job
                if (_scrollx == 0 && _scrolly == 0) {
                    _ttime = 0;
                    viewFinishedScrolling();
                }
            }
        }

        // now tick our animations and sprites
        _animmgr.tick(tickStamp);
        _spritemgr.tick(tickStamp);

        // make a note that the next paint will correspond to a call to
        // tick()
        _tickPaintPending = true;
    }

    /**
     * We want to be painted every tick.
     */
    public Component getComponent ()
    {
        return this;
    }

    // documentation inherited
    public void repaint (long tm, int x, int y, int width, int height)
    {
        _remgr.invalidateRegion(x, y, width, height);
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;

        // no use in painting if we're not showing or if we've not yet
        // been validated
        if (!isValid() || !isShowing()) {
            return;
        }

        // if this isn't a tick paint, then we need to grab the clipping
        // rectangle and mark it as dirty
        if (!_tickPaintPending) {
            Shape clip = g.getClip();
            Rectangle dirty = (clip == null) ? getBounds() : clip.getBounds();
            _remgr.invalidateRegion(dirty);

        } else {
            _tickPaintPending = false;
        }

        // if we didn't scroll and have no invalid rects, there's no need
        // to repaint anything
        if (!_remgr.haveDirtyRegions() && _dx == 0 && _dy == 0) {
            return;
        }

        // we may need to do some scrolling
        if (_dx != 0 || _dy != 0) {
            gfx.copyArea(0, 0, getWidth(), getHeight(), -_dx, -_dy);
        }

        // get our dirty rectangles
        Rectangle[] dirty = _remgr.getDirtyRegions();
        int dcount = dirty.length;

        // translate into happy space
        gfx.translate(-_tx, -_ty);
        for (int ii = 0; ii < dcount; ii++) {
            dirty[ii].translate(_tx, _ty);
        }

        // paint the behind the scenes stuff
        paintBehind(gfx, dirty);

        // paint back sprites and animations
        for (int ii = 0; ii < dcount; ii++) {
            paintBits(gfx, AnimationManager.BACK, dirty[ii]);
        }

        // paint the between the scenes stuff
        paintBetween(gfx, dirty);

        // paint front sprites and animations
        for (int ii = 0; ii < dcount; ii++) {
            paintBits(gfx, AnimationManager.FRONT, dirty[ii]);
        }

        // paint anything in front
        paintInFront(gfx, dirty);

        // translate back out of happy space
        gfx.translate(_tx, _ty);
    }

    /**
     * Paints behind all sprites and animations. The supplied list of
     * invalid rectangles should be redrawn in the supplied graphics
     * context. The rectangles will be in the view coordinate system
     * (which may differ from screen coordinates, see {@link
     * #getViewSize}. Sub-classes should override this method to do the
     * actual rendering for their display.
     */
    protected void paintBehind (Graphics2D gfx, Rectangle[] dirtyRects)
    {
    }

    /**
     * Paints between the front and back layer of sprites and animations.
     * The supplied list of invalid rectangles should be redrawn in the
     * supplied graphics context. The rectangles will be in the view
     * coordinate system (which may differ from screen coordinates, see
     * {@link #getViewSize}. Sub-classes should override this method to do
     * the actual rendering for their display.
     */
    protected void paintBetween (Graphics2D gfx, Rectangle[] dirtyRects)
    {
    }

    /**
     * Paints in front of all sprites and animations. The supplied list of
     * invalid rectangles should be redrawn in the supplied graphics
     * context. The rectangles will be in the view coordinate system
     * (which may differ from screen coordinates, see {@link
     * #getViewSize}. Sub-classes should override this method to do the
     * actual rendering for their display.
     */
    protected void paintInFront (Graphics2D gfx, Rectangle[] dirtyRects)
    {
    }

    /**
     * Renders the sprites and animations that intersect the supplied
     * clipping region in the specified layer. Derived classes can
     * override this method if they need to do custom sprite or animation
     * rendering (if they need to do special sprite z-order handling, for
     * example). This method also takes care of setting the clipping
     * region in the graphics object which an overridden method should
     * also do to preserve performance.
     */
    protected void paintBits (Graphics2D gfx, int layer, Rectangle clip)
    {
        Shape oclip = gfx.getClip();
        gfx.clipRect(clip.x, clip.y, clip.width, clip.height);
        _animmgr.renderAnimations(gfx, layer, clip);
        _spritemgr.renderSprites(gfx, layer, clip);
        gfx.setClip(oclip);
    }

    /**
     * This is called, when the view is scrolling, during the tick
     * processing phase. The animated panel will take care of scrolling
     * the contents of the offscreen buffer when the time comes to render,
     * but the derived class will need to do whatever is necessary to
     * prepare to repaint the exposed regions as well as update its own
     * internal state accordingly.
     *
     * @param dx the distance (in pixels) that the view will scroll in the
     * x direction.
     * @param dy the distance (in pixels) that the view will scroll in the
     * y direction.
     * @param now the current time, provided because we have it and
     * scrolling views are likely to want to use it in calculating stuff.
     */
    protected void viewWillScroll (int dx, int dy, long tickStamp)
    {
        // nothing to do here
    }

    /**
     * Called during the same frame that we scrolled into the final
     * desired position. This method is called after {@link
     * #viewWillScroll} is called with the final scrolling deltas.
     */
    protected void viewFinishedScrolling ()
    {
//         Log.info("viewFinishedScrolling");
    }

    /**
     * Derived classes that wish to operate in a coordinate system based
     * on a view size that is larger or smaller than the viewport size
     * (the actual dimensions of the animated panel) can override this
     * method and return the desired size of the view. The animated panel
     * will take this size into account and translate into the view
     * coordinate system before calling {@link #render}.
     */
    protected Dimension getViewSize ()
    {
        return getSize();
    }

    /** The frame manager with whom we register. */
    protected FrameManager _framemgr;

    /** The animation manager in use by this panel. */
    protected AnimationManager _animmgr;

    /** The sprite manager in use by this panel. */
    protected SpriteManager _spritemgr;

    /** Used to accumulate and merge dirty regions on each tick. */
    protected RegionManager _remgr;

    /** Our viewport offsets. */
    protected int _tx, _ty;

    /** How many pixels we have left to scroll. */
    protected int _scrollx, _scrolly;

    /** Our scroll offsets for this frame tick. */
    protected int _dx, _dy;

    /** The time at which we expect to stop scrolling. */
    protected long _ttime;

    /** The last time we were scrolled. */
    protected long _last;

    /** Used to correlate tick()s with paint()s. */
    protected boolean _tickPaintPending = false;
}
