//
// $Id: AnimatedPanel.java,v 1.5 2002/01/07 23:05:39 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.threerings.media.Log;

/**
 * The animated panel provides a useful extensible implementation of a
 * Swing {@link JPanel} that implements the {@link AnimatedView}
 * interface.  Sub-classes should override {@link #render} to draw
 * their panel-specific contents, and may choose to override {@link
 * #invalidateRects} to optimize their internal rendering.
 */
public class AnimatedPanel extends JPanel implements AnimatedView
{
    /**
     * Constructs an animated panel.
     */
    public AnimatedPanel ()
    {
	// set our attributes for optimal display performance
        setDoubleBuffered(false);
        setOpaque(true);
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
         // create the offscreens if they don't yet exist
 	if (_offimg == null && !createOffscreen()) {
             return;
        }

        // give sub-classes a chance to do their thing
        render(_offg);

        // Rectangle bounds = getBounds();
        // Log.info("paintComponent [bounds=" + bounds + "].");

        // draw the offscreen to the screen
        g.drawImage(_offimg, 0, 0, null);
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

    /**
     * Creates the offscreen image and graphics context to which we draw
     * the scene for double-buffering purposes.  Returns whether the
     * offscreen image was successfully created.
     */
    protected boolean createOffscreen ()
    {
	Dimension d = getSize();
        try {
            _offimg = createImage(d.width, d.height);
            _offg = _offimg.getGraphics();
            return true;

        } catch (Exception e) {
            Log.warning("Failed to create offscreen [e=" + e + "].");
            Log.logStackTrace(e);
            return false;
        }
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

    /**
     * Paints this panel immediately. Since we know that we are always
     * opaque and not dependent on Swing's double-buffering, we bypass the
     * antics that <code>JComponent.paintImmediately()</code> performs in
     * the interest of better performance.
     */
    public void paintImmediately ()
    {
        if (!isValid()) {
            // don't paint anything until we've been fully laid out
            // Log.warning("Attempted to paint invalid panel.");
            return;
        }

        Graphics g = null;

        try {
            Graphics pcg = getGraphics();
            // apparently getGraphics() can fail if we are removed from
            // the UI between the time that we queued up the code that
            // calls this method and the time that it's called
            if (pcg != null) {
                g = pcg.create();
                pcg.dispose();
                paintComponent(g);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // documentation inherited
    public JComponent getComponent ()
    {
	return this;
    }

    /** The offscreen image used for double-buffering. */
    protected Image _offimg;

    /** The graphics context for the offscreen image. */
    protected Graphics _offg;
}
