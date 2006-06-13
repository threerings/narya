//
// $Id$

package com.threerings.media;

/**
 * An interface used by entities that wish to respond to the scrolling of a
 * {@link VirtualMediaPanel}.
 *
 * @see VirtualMediaPanel#addViewTracker
 */
public interface ViewTracker
{
    /**
     * Called by a {@link VirtualMediaPanel} when it scrolls.
     */
    public void viewLocationDidChange (int dx, int dy);
}
