//
// $Id: AnimatedPanel.java,v 1.5 2002/02/13 18:48:56 mdb Exp $

package com.threerings.media.animation;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;

import java.awt.image.BufferStrategy;

import java.util.List;

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
 * panel-specific contents, and may choose to override {@link
 * #invalidateRects} and {@link #invalidateRect} to optimize their
 * internal rendering.
 */
public class AnimatedPanel extends Canvas implements AnimatedView
{
    /**
     * Constructs an animated panel.
     */
    public AnimatedPanel ()
    {
	// set our attributes for optimal display performance
        // setIgnoreRepaint(true);

        // create our animation manager
        _animmgr = new AnimationManager(this);

        // create a sprite manager if we haven't been requested not to
        if (needsSpriteManager()) {
            _animmgr.setSpriteManager(new SpriteManager());
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

    // documentation inherited
    public void paint (Graphics g)
    {
        // we don't paint directly any more, we pass the dirty rect to the
        // animation manager to queue up along with our next repaint
        _animmgr.addDirtyRect(g.getClipBounds());
    }

    // documentation inherited
    public void update (Graphics g)
    {
        // the normal Canvas.update() fills itself with its background
        // color before calling paint() which we definitely don't want
        paint(g);
    }

    /**
     * Renders the panel to the given graphics object.  Sub-classes
     * should override this method to paint their panel-specific
     * contents.
     */
    protected void render (Graphics2D gfx)
    {
        // nothing for now
    }

    // documentation inherited
    public void invalidateRects (List rects)
    {
        // nothing for now
    }

    // documentation inherited
    public void invalidateRect (Rectangle rect)
    {
        // nothing for now
    }

    // documentation inherited
    public void paintImmediately ()
    {
        if (!isValid() || !isShowing()) {
            Log.warning("Attempt to paint unprepared panel " +
                        "[valid=" + isValid() +
                        ", showing=" + isShowing() + "].");
            return;
        }

        if (_strategy == null) {
            // create and obtain a reference to the buffer strategy
            createBufferStrategy(BUFFER_COUNT);
            _strategy = getBufferStrategy();
            Log.info("Created buffer strategy [strategy=" + _strategy + "].");
        }

        // render the panel
        Graphics g = null;
        try {
            g = _strategy.getDrawGraphics();
            render((Graphics2D)g);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        _strategy.show();
    }

    // documentation inherited
    public void createBufferStrategy (int numBuffers)
    {
        // explicitly avoid trying to create a page-flipping strategy, as
        // page-flipping seems to result in artifacts in certain
        // conditions, and the buffer strategy's volatile images are
        // irretrievably lost when the panel is hidden.

        // try an accelerated blitting strategy
        BufferCapabilities bufferCaps = new BufferCapabilities(
            new ImageCapabilities(true), new ImageCapabilities(true), null);
        try {
            createBufferStrategy(numBuffers, bufferCaps);
            Log.info("Created accelerated blitting strategy.");
            return;
        } catch (AWTException e) {
            // failed, fall through to the next potential strategy
        }

        // try an un-accelerated blitting strategy
        bufferCaps = new BufferCapabilities(
            new ImageCapabilities(false), new ImageCapabilities(false), null);
        try {
            createBufferStrategy(numBuffers, bufferCaps);
        } catch (AWTException e) {
            throw new InternalError("Could not create a buffer strategy");
        }
    }

    /** The number of buffers to use when rendering. */
    protected static final int BUFFER_COUNT = 2;

    /** The animation manager we use in this panel. */
    protected AnimationManager _animmgr;

    /** The buffer strategy used for optimal animation rendering. */
    protected BufferStrategy _strategy;
}
