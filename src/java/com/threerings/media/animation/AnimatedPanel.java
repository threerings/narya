//
// $Id: AnimatedPanel.java,v 1.17 2002/02/21 06:01:28 mdb Exp $

package com.threerings.media.animation;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;

import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import java.util.List;

import com.samskivert.util.Histogram;
import com.samskivert.util.StringUtil;

import com.threerings.media.Log;
import com.threerings.media.sprite.SpriteManager;

/**
 * The animated panel provides a useful extensible implementation of a
 * {@link Canvas} that implements the {@link AnimatedView} interface. It
 * takes care of automatically creating an animation manager and a sprite
 * manager to manage the animations that take place within this panel. If
 * a sprite manager is not needed, {@link #needsSpriteManager} can be
 * overridden to prevent it from being created.
 *
 * <p> Users of the animated panel must be sure to call {@link #stop} when
 * their panel is no longer being displayed and {@link #start} to start it
 * back up again when it is once again being displayed. These methods
 * deactivate and reactivate the underlying animation manager.
 *
 * <p> Sub-classes should override {@link #render} to draw their
 * panel-specific contents.
 */
public class AnimatedPanel extends Canvas implements AnimatedView
{
    /**
     * Constructs an animated panel.
     */
    public AnimatedPanel ()
    {
        // create our animation manager
        _animmgr = new AnimationManager(this);

        // create a sprite manager if we haven't been requested not to
        if (needsSpriteManager()) {
            _spritemgr = new SpriteManager();
            _animmgr.setSpriteManager(_spritemgr);
        }
    }

    /**
     * Derived classes can override this method to prevent the automatic
     * creation of a sprite manager to work with the animation manager
     * that is managing this panel.
     */
    protected boolean needsSpriteManager ()
    {
        return true;
    }        

    /**
     * Starts up the animation manager associated with this panel.
     */
    public void start ()
    {
        _animmgr.start();
    }

    /**
     * Shuts down the animation manager associated with this panel.
     */
    public void stop ()
    {
        _animmgr.stop();
    }

    /**
     * Instructs the view to begin scrolling with the specified velocities
     * in milliseconds per pixel. A setting of zero indicates that
     * scrolling should be deactivated in that direction. Negative values
     * mean that the view should be scrolled one pixel in the opposite
     * direction in the positive number of milliseconds.
     */
    public void setScrolling (int msppx, int msppy)
    {
        // set our scrolling parameters
        _msppx = msppx;
        _msppy = msppy;
        _lastx = _lasty = 0;

        // if the velocities are zero, stop the scrolling, otherwise make
        // a note of the time at which scrolling started
        if (msppx == 0 && msppy == 0) {
            _stime = 0;
            _animmgr.setScrolling(0);

        } else {
            _stime = System.currentTimeMillis();

            // set our scrolling speed to the (absolute value of the)
            // lower of the two velocities (but not to zero if either one
            // is zero)
            if (_msppx == 0) {
                _animmgr.setScrolling(Math.abs(msppy));
            } else if (_msppy == 0) {
                _animmgr.setScrolling(Math.abs(msppx));
            } else {
                _animmgr.setScrolling(
                    Math.min(Math.abs(msppx), Math.abs(msppy)));
            }
        }
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        // we don't paint directly any more, we pass the dirty rect to the
        // animation manager to queue up along with our next repaint
        Rectangle dirty = g.getClipBounds();
        // we need to adjust the dirty rect by our viewport offset before
        // passing it to the animation manager
        dirty.translate(_tx, _ty);
        _animmgr.addDirtyRect(dirty);
    }

    // documentation inherited
    public void update (Graphics g)
    {
        // the normal Canvas.update() fills itself with its background
        // color before calling paint() which we definitely don't want
        paint(g);
    }

    // documentation inherited
    public void validate ()
    {
        super.validate();

        // if we change size, clear out our old back buffer
        _backimg = null;

        // figure out our viewport offsets
        Dimension size = getSize(), vsize = getViewSize();
        _tx = (vsize.width - size.width)/2;
        _ty = (vsize.height - size.height)/2;
    }

    // documentation inherited
    public void invalidateRect (Rectangle invalidRect)
    {
        // pass it on to the animation manager
        _animmgr.addDirtyRect(invalidRect);
    }

    // documentation inherited
    public void paintImmediately (List invalidRects)
    {
        // no use in painting if we're not showing or if we've not yet
        // been validated
        if (!isValid() || !isShowing()) {
            return;
        }

        // track how long it was since we were last painted
        long now = System.currentTimeMillis();
//         if (_last != 0) {
//             int delta = (int)(now-_last);
//             _histo.addValue(delta);

//             // dump the histogram every ten seconds
//             if (_last % (10*1000) < 50) {
//                 Log.info("Render histogram.");
//                 Log.info(StringUtil.toMatrixString(_histo.getBuckets(), 10, 3));
//             }
//         }
//         _last = now;

        int width = getWidth(), height = getHeight();

        // if scrolling is enabled, determine the scrolling delta to be
        // used and do the business
        int dx = 0, dy = 0;
        if (_stime != 0) {
            // compute the total distance scrolled since we started (to
            // avoid rounding errors)
            // long now = System.currentTimeMillis();

            // determine how many pixels further along we've moved and
            // make a note of our latest position
            if (_msppx != 0) {
                int xdist = (int)((now - _stime) / _msppx);
                dx = (xdist - _lastx);
                _lastx = xdist;
            }
            if (_msppy != 0) {
                int ydist = (int)((now - _stime) / _msppy);
                dy = (ydist - _lasty);
                _lasty = ydist;
            }

            // and add invalid rectangles for the exposed areas
            if (dx > 0) {
                Rectangle dirty = new Rectangle(width - dx, 0, dx, height);
                dirty.translate(_tx, _ty);
                invalidRects.add(dirty);
            } else if (dx < 0) {
                Rectangle dirty = new Rectangle(0, 0, -dx, height);
                dirty.translate(_tx, _ty);
                invalidRects.add(dirty);
            }
            if (dy > 0) {
                Rectangle dirty = new Rectangle(0, height - dy, width, dy);
                dirty.translate(_tx, _ty);
                invalidRects.add(dirty);
            } else if (dy < 0) {
                Rectangle dirty = new Rectangle(0, 0, width, -dy);
                dirty.translate(_tx, _ty);
                invalidRects.add(dirty);
            }

            // make sure we're actually scrolling before telling people
            // about it
            if (dx != 0 || dy != 0) {
                // if we are working with a sprite manager, let it know
                // that we're about to scroll out from under its sprites
                // and allow it to provide us with more dirty rects
                if (_spritemgr != null) {
                    _spritemgr.viewWillScroll(dx, dy, invalidRects);
                }

                // let our derived classes do whatever they need to do to
                // prepare to be scrolled
                viewWillScroll(dx, dy, now, invalidRects);
            }
        }

        // if we didn't scroll and have no invalid rects, there's no need
        // to repaint anything
        if (invalidRects.size() == 0 && dx == 0 && dy == 0) {
            return;
        }

        // create our off-screen buffer if necessary
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (_backimg == null) {
            createBackBuffer(gc);
        }

        // render into our back buffer
        do {
            // make sure our back buffer hasn't disappeared
            int valres = _backimg.validate(gc);

            // if we've changed resolutions, recreate the buffer
            if (valres == VolatileImage.IMAGE_INCOMPATIBLE) {
                Log.info("Back buffer incompatible, recreating.");
                createBackBuffer(gc);
            }

            Graphics g = null;
            try {
                g = _backimg.getGraphics();

                // if the image wasn't A-OK, we need to rerender the whole
                // business rather than just the dirty parts
                if (valres != VolatileImage.IMAGE_OK) {
                    invalidRects.clear();
                    invalidRects.add(new Rectangle(_tx, _ty, width, height));
                    Log.info("Lost back buffer, redrawing.");

                } else if (dx != 0 || dy != 0) {
                    // if it was OK, we may need to do some scrolling
                    g.copyArea(0, 0, width, height, -dx, -dy);
                }

                // translate into happy space
                g.translate(-_tx, -_ty);

                // now do our actual rendering
                render((Graphics2D)g, invalidRects);

                // translate back out of happy space
                g.translate(_tx, _ty);

            } finally {
                g.dispose();
            }

            // draw the back buffer to the screen
            try {
                g = getGraphics();

                // if we're scrolling, we've got to copy the whole image
                // to the screen, otherwise we can just copy the dirty
                // regions
                if (dx != 0 || dy != 0) {
                    g.drawImage(_backimg, 0, 0, null);

                } else {
                    // iterate through the invalid rectangles, copying
                    // those areas from the back buffer to the display
                    int isize = invalidRects.size();
                    for (int i = 0; i < isize; i++) {
                        Rectangle rect = (Rectangle)invalidRects.get(i);
                        // we have to translate out of view coordinates
                        // before doing the actual copy
                        rect.translate(-_tx, -_ty);
                        g.setClip(rect);
                        g.drawImage(_backimg, 0, 0, null);
                    }
                }

            } finally {
                if (g != null) {
                    g.dispose();
                }
            }

        } while (_backimg.contentsLost());
    }

    /**
     * This is called, when the view is scrolling, just before a call to
     * {@link #render}. The animated panel will take care of scrolling the
     * contents of the offscreen buffer, but the derived class will need
     * to do whatever is necessary to prepare to repaint the exposed
     * regions as well as update it's own internal state accordingly.
     *
     * @param dx the distance (in pixels) that the view will scroll in the
     * x direction.
     * @param dy the distance (in pixels) that the view will scroll in the
     * y direction.
     * @param now the current time, provided because we have it and
     * scrolling views are likely to want to use it in calculating stuff.
     * @param invalidRects the list of invalid rectangles which will be
     * redrawn; rectangles can be added to this list if necessary.
     */
    protected void viewWillScroll (int dx, int dy, long now, List invalidRects)
    {
        // nothing to do here
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

    /**
     * Requests that the supplied list of invalid rectangles be redrawn in
     * the supplied graphics context. The rectangles will be in the view
     * coordinate system (which may differ from screen coordinates, see
     * {@link #getViewSize}. Sub-classes should override this method to do
     * the actual rendering for their display.
     */
    protected void render (Graphics2D gfx, List invalidRects)
    {
        // nothing for now
    }

    /**
     * Creates the off-screen buffer used to perform double buffered
     * rendering of the animated panel.
     */
    protected void createBackBuffer (GraphicsConfiguration gc)
    {
        _backimg = gc.createCompatibleVolatileImage(getWidth(), getHeight());
    }

    /** The animation manager we use in this panel. */
    protected AnimationManager _animmgr;

    /** The sprite manager in use by this panel. */
    protected SpriteManager _spritemgr;

    /** The image used to render off-screen. */
    protected VolatileImage _backimg;

    /** Our viewport offsets. */
    protected int _tx, _ty;

    /** The scrolling velocity in milliseconds per pixel. */
    protected int _msppx, _msppy;

    /** The time at which the scrolling velocity was set. */
    protected long _stime;

    /** Used to determine how many pixels we've scrolled since the
     * scrolling velocity was last set. */
    protected int _lastx, _lasty;

    /** The last time we were rendered. */
    protected long _last;

    /** A histogram for tracking how frequently we're rendered. */
    protected Histogram _histo = new Histogram(0, 1, 100);

    /** The number of buffers to use when rendering. */
    protected static final int BUFFER_COUNT = 2;
}
