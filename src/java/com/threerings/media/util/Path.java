//
// $Id: Path.java,v 1.3 2002/04/15 23:09:10 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Graphics2D;

/**
 * A path is used to cause a sprite to follow a particular path along the
 * screen. The sprite will call into the path every time it is ticked by
 * the animation manager and the path is responsible for updating the
 * position of the sprite based on the time that has elapsed since the
 * sprite started down the path. The animation manager attempts to tick
 * once per frame at the desired frame rate, but may tick less often if
 * CPU resources are limited, thus paths should not rely on tick counts
 * but instead use elapsed time to determine progress.
 *
 * <p> The path should call back to the sprite (via {@link
 * Sprite#pathCompleted}) to let it know when the path has been completed.
 */
public interface Path
{
    /**
     * Called once to let the path prepare itself for the process of
     * animating the supplied sprite.
     */
    public void init (Sprite sprite, long timestamp);

    /**
     * Called to request that this path update the position of the
     * specified sprite based on the supplied timestamp information. A
     * path should record its initial timestamp and determine the progress
     * of the sprite along the path based on the time elapsed since the
     * sprite began down the path.
     *
     * @return true if the sprite's position was updated, false if the
     * path determined that the sprite should not move at this time.
     */
    public boolean updatePosition (Sprite sprite, long timestamp);

    /**
     * Sets the velocity of this sprite in pixels per millisecond. The
     * velocity is measured as pixels traversed along the path that the
     * sprite is traveling rather than in the x or y directions
     * individually.  Note that the sprite velocity should not be changed
     * while a path is being traversed; doing so may result in the sprite
     * position changing unexpectedly.
     *
     * @param velocity the sprite velocity in pixels per millisecond.
     */
    public void setVelocity (float velocity);

    /**
     * Called when the view that contains the sprite following this path
     * is scrolling by the specified amount. Gives the path an opportunity
     * to adjust its internal coordinates by the scrolled amount.
     */
    public void viewWillScroll (int dx, int dy);

    /**
     * Paint this path on the screen (used for debugging purposes only).
     */
    public void paint (Graphics2D gfx);
}
