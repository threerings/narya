//
// $Id: ManagedJFrame.java,v 1.1 2002/04/23 01:16:27 mdb Exp $

package com.threerings.media;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.RepaintManager;

import com.threerings.media.Log;

/**
 * When using the {@link FrameManager}, one must use this top-level frame
 * class (or the {@link ManagedFrame} class if one is not using Swing.
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
        Rectangle clip = g.getClip().getBounds();
        if (clip != null) {
            RepaintManager.currentManager(this).addDirtyRegion(
                getRootPane(), clip.x, clip.y, clip.width, clip.height);
        }
    }
}
