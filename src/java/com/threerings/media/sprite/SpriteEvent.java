//
// $Id: SpriteEvent.java,v 1.3 2002/12/04 02:45:09 shaper Exp $

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
     * @param when the time at which this event took place.
     */
    public SpriteEvent (Sprite sprite, long when)
    {
	_sprite = sprite;
        _when = when;
    }

    /**
     * Returns the sprite involved in the event.
     */
    public Sprite getSprite ()
    {
	return _sprite;
    }

    /**
     * Returns the time associated with this event.
     */
    public long getWhen ()
    {
        return _when;
    }

    /** The sprite associated with this event. */
    protected Sprite _sprite;

    /** The time associated with this event. */
    protected long _when;
}
