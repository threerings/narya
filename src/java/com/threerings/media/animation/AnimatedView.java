//
// $Id: AnimatedView.java,v 1.2 2002/02/17 23:39:32 mdb Exp $

package com.threerings.media.animation;

import java.awt.Rectangle;
import java.util.List;

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
    public void invalidateRects (List rects);

    /**
     * Invalidates a rectangle in screen pixel coordinates in the scene
     * view for later repainting.
     *
     * @param rect the {@link java.awt.Rectangle} to dirty.
     */
    public void invalidateRect (Rectangle rect);

    /**
     * Requests that the animated view paint itself immediately (that it
     * complete the painting process before returning from this function).
     * This will only be called on the AWT thread and when it is safe to
     * paint.
     *
     * @param invalidRectCount the number of invalide regions that
     * motivated the animation manager to repaint the animated view (which
     * could be zero if the view is scrolling and otherwise has no dirty
     * regions)
     */
    public void paintImmediately (int invalidRectCount);
}
