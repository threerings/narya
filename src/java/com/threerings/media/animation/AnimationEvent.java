//
// $Id: AnimationEvent.java,v 1.2 2002/11/05 20:51:13 mdb Exp $

package com.threerings.media.animation;

/**
 * An animation event is sent to all of an animation's observers whenever
 * one of the various possible animation events takes place.
 */
public class AnimationEvent
{
    /**
     * Create an animation event.
     *
     * @param animation the involved animation.
     * @param when the time at which this event took place.
     */
    public AnimationEvent (Animation anim, long when)
    {
	_anim = anim;
        _when = when;
    }

    /**
     * Returns the animation involved in the event.
     */
    public Animation getAnimation ()
    {
	return _anim;
    }

    /**
     * Returns the time associated with this event.
     */
    public long getWhen ()
    {
        return _when;
    }

    /** The animation associated with this event. */
    protected Animation _anim;

    /** The time associated with this event. */
    protected long _when;
}
