//
// $Id: Path.java,v 1.10 2003/01/17 22:57:08 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;

/**
 * A path is used to cause a {@link Pathable} to follow a particular path
 * along the screen. The {@link Pathable} is responsible for calling
 * {@link #tick} on the path with reasonable frequency (generally as a
 * part of the frame tick. The path is responsible for updating the
 * position of the {@link Pathable} based on the time that has elapsed
 * since the {@link Pathable} started down the path.
 *
 * <p> The path should call the appropriate callbacks on the {@link
 * Pathable} when appropriate (e.g. {@link Pathable#pathBeginning}, {@link
 * Pathable#pathCompleted}).
 */
public interface Path
{
    /**
     * Called once to let the path prepare itself for the process of
     * animating the supplied pathable. Path users should also call {@link
     * #tick} after {@link #init} with the same initialization timestamp.
     */
    public void init (Pathable pable, long tickStamp);

    /**
     * Called to request that this path update the position of the
     * specified pathable based on the supplied timestamp information. A
     * path should record its initial timestamp and determine the progress
     * of the pathable along the path based on the time elapsed since the
     * pathable began down the path.
     *
     * @param pable the pathable whose position should be updated.
     * @param tickStamp the timestamp associated with this frame.
     *
     * @return true if the pathable's position was updated, false if the
     * path determined that the pathable should not move at this time.
     */
    public boolean tick (Pathable pable, long tickStamp);

    /**
     * This is called if the pathable is paused for some length of time
     * and then unpaused. Paths should adjust any time stamps they are
     * maintaining internally by the delta so that time maintains the
     * illusion of flowing smoothly forward.
     */
    public void fastForward (long timeDelta);

    /**
     * Paint this path on the screen (used for debugging purposes only).
     */
    public void paint (Graphics2D gfx);

    /**
     * When a path is removed from a pathable, whether that is because the
     * path was completed or because it was replaced by another path, this
     * method will be called to let the path know that it is no longer
     * associated with this pathable.
     */
    public void wasRemoved (Pathable pable);
}
