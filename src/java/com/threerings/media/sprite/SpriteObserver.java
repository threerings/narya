//
// $Id: SpriteObserver.java,v 1.1 2001/09/05 00:40:33 shaper Exp $

package com.threerings.media.sprite;

/**
 * An interface to be implemented by classes that would like to
 * observe a sprite and be notified of meaningful events as it, and
 * any other sprites, move about.
 */
public interface SpriteObserver
{
    /**
     * This method is called by the {@link SpriteManager} when
     * something interesting is accomplished by or happens to the
     * sprite.
     *
     * @param sprite the involved sprite.
     * @param eventCode the type of sprite event.
     * @param arg the argument associated with the event.
     */
    public void spriteChanged (Sprite sprite, int eventCode, Object arg);

    /**
     * Event code noting that the sprite completed a path node.  The
     * argument to the event is the {@link PathNode} completed.
     */
    public static final int FINISHED_PATH_NODE = 0;

    /**
     * Event code noting that the sprite completed its entire path.
     * The argument to the event is the {@link Path} completed.
     */
    public static final int FINISHED_PATH = 1;

    /**
     * Event code noting that the sprite collided with another
     * sprite.  The argument to the event is the {@link Sprite} the
     * observed sprite collided with.
     */
    public static final int COLLIDED_SPRITE = 2;
}
