//
// $Id: FlipFrameManager.java,v 1.2 2003/05/01 22:06:52 mdb Exp $

package com.threerings.media;

import java.awt.BufferCapabilities;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import com.threerings.media.timer.MediaTimer;

/**
 * A {@link FrameManager} extension that uses a flip-buffer (via {@link
 * BufferStrategy} to do its rendering.
 */
public class FlipFrameManager extends FrameManager
{
    // documentation inherited
    protected void paint (long tickStamp)
    {
        // create our buffer strategy if we don't already have one
        if (_bufstrat == null) {
            BufferCapabilities cap = new BufferCapabilities(
                new ImageCapabilities(true), new ImageCapabilities(true),
                BufferCapabilities.FlipContents.COPIED);
            _frame.createBufferStrategy(2);
            _bufstrat = _frame.getBufferStrategy();
        }

        // start out assuming we can do an incremental render
        boolean incremental = true;

        do {
            Graphics2D gfx = (Graphics2D)_bufstrat.getDrawGraphics();

            // dirty everything if we're not incrementally rendering
            if (!incremental) {
                Log.info("Doing non-incremental render; contents lost.");
                _frame.getRootPane().revalidate();
                _frame.getRootPane().repaint();
            }

            // request to paint our participants and components and bail
            // if they paint nothing
            if (!paint(gfx)) {
                return;
            }

            // flip our buffer to visible
            _bufstrat.show();

            // if we loop through a second time, we'll need to rerender
            // everything
            incremental = false;

        } while (_bufstrat.contentsLost());
    }

    // documentation inherited
    protected void restoreFromBack (Rectangle dirty)
    {
        // nothing doing
    }

    /** The buffer strategy used to do our rendering. */
    protected BufferStrategy _bufstrat;
}
