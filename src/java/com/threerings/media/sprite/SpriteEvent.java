//
// $Id: SpriteEvent.java,v 1.2 2001/09/13 19:36:20 mdb Exp $

package com.threerings.media.sprite;

/**
 * A sprite event is sent to all of a sprite's observers whenever one of
 * the various possible sprite events takes place.
 */
public class SpriteEvent
{
    /**
     * Create a sprite event.
     *
     * @param sprite the involved sprite.
     */
    public SpriteEvent (Sprite sprite)
    {
	_sprite = sprite;
    }

    /**
     * Returns the sprite involved in the event.
     */
    public Sprite getSprite ()
    {
	return _sprite;
    }

    /** The sprite associated with this event. */
    protected Sprite _sprite;
}
