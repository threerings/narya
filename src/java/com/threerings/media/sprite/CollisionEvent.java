//
// $Id: CollisionEvent.java,v 1.1 2001/09/13 19:36:20 mdb Exp $

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
    public CollisionEvent (Sprite sprite, Sprite other)
    {
        super(sprite);
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
