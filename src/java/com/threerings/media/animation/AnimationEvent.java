//
// $Id: AnimationEvent.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

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
     */
    public AnimationEvent (Animation anim)
    {
	_anim = anim;
    }

    /**
     * Returns the animation involved in the event.
     */
    public Animation getAnimation ()
    {
	return _anim;
    }

    /** The animation associated with this event. */
    protected Animation _anim;
}
