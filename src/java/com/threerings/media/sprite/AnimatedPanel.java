//
// $Id: AnimatedPanel.java,v 1.4 2001/12/16 08:04:25 mdb Exp $

package com.threerings.media.sprite;

import java.awt.*;
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
	if (_offimg == null) {
	    createOffscreen();
	}

        // give sub-classes a chance to do their thing
        render(_offg);

        // Rectangle bounds = getBounds();
        // Log.info("paintComponent [bounds=" + bounds + "].");
        // _offg.drawRect(bounds.x, bounds.y, bounds.width-1, bounds.height-1);

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
     * Creates the offscreen image and graphics context to which we
     * draw the scene for double-buffering purposes.
     */
    protected void createOffscreen ()
    {
	Dimension d = getSize();
	_offimg = createImage(d.width, d.height);
	_offg = _offimg.getGraphics();
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
        Graphics g = null;

        try {
            Graphics pcg = getGraphics();
            // apparently getGraphics() can fail if we are removed from
            // the UI between the time that we queued up the code that
            // calls this method and the time that it's called
            if (pcg != null) {
                g = pcg.create();
                pcg.dispose();
                paint(g);
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
