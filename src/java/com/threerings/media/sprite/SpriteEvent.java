//
// $Id: SpriteEvent.java,v 1.1 2001/09/07 23:01:53 shaper Exp $

package com.threerings.media.sprite;

/**
 * A sprite event is sent to all sprite observers for a sprite
 * whenever one of the various possible sprite events takes place.
 */
public class SpriteEvent
{
    /**
     * Create a sprite event.
     *
     * @param sprite the involved sprite.
     * @param eventCode the event code.
     * @param arg the event argument.
     */
    public SpriteEvent (Sprite sprite, int eventCode, Object arg)
    {
	_sprite = sprite;
	_eventCode = eventCode;
	_arg = arg;
    }

    /**
     * Return the sprite involved in the event.
     */
    public Sprite getSprite ()
    {
	return _sprite;
    }

    /**
     * Return the event code for the event.
     */
    public int getEventCode ()
    {
	return _eventCode;
    }

    /**
     * Return the argument associated with the event.
     */
    public Object getArgument ()
    {
	return _arg;
    }

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

    /** The sprite associated with this event. */
    protected Sprite _sprite;

    /** The event code. */
    protected int _eventCode;

    /** The argument associated with this event. */
    protected Object _arg;
}
