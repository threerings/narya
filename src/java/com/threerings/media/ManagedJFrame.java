//
// $Id: ManagedJFrame.java,v 1.3 2002/06/11 00:52:37 mdb Exp $

package com.threerings.media;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JFrame;
import javax.swing.RepaintManager;

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
    }

    /**
     * Constructs a managed frame with the specified title.
     */
    public ManagedJFrame (String title)
    {
        super(title);
    }

    /**
     * We catch update requests and forward them on to the repaint
     * infrastructure.
     */
    public void update (Graphics g)
    {
        Shape clip = clip = g.getClip();
        if (clip != null) {
            Rectangle cb = clip.getBounds();
            RepaintManager.currentManager(this).addDirtyRegion(
                getRootPane(), cb.x, cb.y, cb.width, cb.height);

        } else {
            RepaintManager.currentManager(this).addDirtyRegion(
                getRootPane(), 0, 0, getWidth(), getHeight());
        }
    }
}
