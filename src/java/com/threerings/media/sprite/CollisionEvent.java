//
// $Id: CollisionEvent.java,v 1.2 2002/12/04 02:45:09 shaper Exp $

package com.threerings.media.sprite;

/**
 * A collision event is dispatched when a sprite collides with another
 * sprite.
 */
public class CollisionEvent extends SpriteEvent
{
    /**
     * Constructs a collision event for the specified sprites.
     */
    public CollisionEvent (Sprite sprite, long when, Sprite other)
    {
        super(sprite, when);
        _other = other;
    }

    /**
     * Returns the other sprite that was involved in this collision (the
     * first sprite being available via {@link #getSprite}.
     */
    public Sprite getOther ()
    {
        return _other;
    }

    /** A reference to the other sprite. */
    protected Sprite _other;
}
