//
// $Id: AnimatedView.java,v 1.4 2001/08/23 00:23:58 shaper Exp $

package com.threerings.media.sprite;

import javax.swing.JComponent;

/**
 * A view that wishes to interact with the animation manager needs to
 * implement this interface to give the animation manager a means by which
 * to communicate the regions of the view that need to be repainted
 * because of the process of animating on top of the view.
 */
public interface AnimatedView
{
    /**
     * Invalidate a list of rectangles in screen pixel coordinates in the
     * scene view for later repainting.
     *
     * @param rects the list of {@link java.awt.Rectangle} objects.
     */
    public void invalidateRects (DirtyRectList rects);

    /**
     * Requests that the animated view paint itself immediately (that it
     * complete the painting process before returning from this function).
     * This will only be called on the AWT thread and when it is safe to
     * paint.
     */
    public void paintImmediately ();

    /**
     * Return the component associated with the view so that the
     * animation manager can restrict its animation to when the
     * component is actually visible.
     */
    public JComponent getComponent ();
}
