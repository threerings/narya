//
// $Id: AnimationCompletedEvent.java,v 1.2 2002/11/05 20:51:13 mdb Exp $

package com.threerings.media.animation;

/**
 * An animation completed event is dispatched when an animation has
 * completed all of its business.
 */
public class AnimationCompletedEvent extends AnimationEvent
{
    public AnimationCompletedEvent (Animation anim, long when)
    {
        super(anim, when);
    }
}
