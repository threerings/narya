//
// $Id: AnimatedView.java,v 1.3 2002/02/19 01:23:56 mdb Exp $

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
     * Requests that the animated view paint itself immediately (that it
     * complete the painting process before returning from this function).
     * This will only be called on the AWT thread and when it is safe to
     * paint.
     *
     * @param invalidRects the list of rectangles that have been
     * invalidated since the last call to this method.
     */
    public void paintImmediately (List invalidRects);
}
