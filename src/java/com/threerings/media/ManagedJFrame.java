//
// $Id: ManagedJFrame.java,v 1.5 2003/03/30 00:00:00 mdb Exp $

package com.threerings.media;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JFrame;

import com.samskivert.util.StringUtil;
import com.threerings.media.Log;

/**
 * When using the {@link FrameManager}, one must use this top-level frame
 * class.
 */
public class ManagedJFrame extends JFrame
{
    /**
     * Constructs a managed frame with no title.
     */
    public ManagedJFrame ()
    {
        this("");
    }

    /**
     * Constructs a managed frame with the specified title.
     */
    public ManagedJFrame (String title)
    {
        super(title);
    }

    /**
     * Constructs a managed frame in the specified graphics configuration.
     */
    public ManagedJFrame (GraphicsConfiguration gc)
    {
        super(gc);
    }

    /**
     * Called by our frame manager when it's ready to go.
     */
    public void init (FrameManager fmgr)
    {
        _fmgr = fmgr;
    }

    /**
     * We catch paint requests and forward them on to the repaint
     * infrastructure.
     */
    public void paint (Graphics g)
    {
        update(g);
    }

    /**
     * We catch update requests and forward them on to the repaint
     * infrastructure.
     */
    public void update (Graphics g)
    {
        Shape clip = g.getClip();
        Rectangle dirty;
        if (clip != null) {
            dirty = clip.getBounds();
        } else {
            dirty = getRootPane().getBounds();
            // account for our frame insets
            Insets insets = getInsets();
            dirty.x += insets.left;
            dirty.y += insets.top;
        }

        if (_fmgr != null) {
            _fmgr.restoreFromBack(dirty);
        } else {
            Log.info("Dropping AWT dirty rect " + StringUtil.toString(dirty) +
                     " [clip=" + StringUtil.toString(clip) + "].");
        }
    }

    protected FrameManager _fmgr;
}
