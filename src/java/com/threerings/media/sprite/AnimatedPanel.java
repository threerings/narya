//
// $Id: AnimatedPanel.java,v 1.6 2002/01/08 22:16:58 shaper Exp $

package com.threerings.media.sprite;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;

import java.awt.image.BufferStrategy;

import com.threerings.media.Log;

/**
 * The animated panel provides a useful extensible implementation of a
 * {@link Canvas} that implements the {@link AnimatedView} interface.
 * Sub-classes should override {@link #render} to draw their
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
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        update(g);
    }

    // documentation inherited
    public void update (Graphics g)
    {
        paintImmediately();
    }

    /**
     * Renders the panel to the given graphics object.  Sub-classes
     * should override this method to paint their panel-specific
     * contents.
     */
    protected void render (Graphics g)
    {
        // nothing for now
    }

    // documentation inherited
    public void invalidateRects (DirtyRectList rects)
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
            render(g);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        _strategy.show();
    }

    public void createBufferStrategy (int numBuffers)
    {
        // for now, always use un-accelerated blitting.  page-flipping
        // seems to result in artifacts in certain conditions, and the
        // buffer strategy's volatile images are irretrievably lost when
        // the panel is hidden.
        BufferCapabilities bufferCaps = new BufferCapabilities(
            new ImageCapabilities(false), new ImageCapabilities(false), null);
        try {
            createBufferStrategy(numBuffers, bufferCaps);
        } catch (AWTException e) {
            throw new InternalError("Could not create a buffer strategy");
        }
    }

    /** The number of buffers to use when rendering. */
    protected static final int BUFFER_COUNT = 2;

    /** The buffer strategy used for optimal animation rendering. */
    protected BufferStrategy _strategy;
}
