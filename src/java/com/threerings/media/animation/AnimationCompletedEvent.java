//
// $Id: AnimationCompletedEvent.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

package com.threerings.media.animation;

/**
 * An animation completed event is dispatched when an animation has
 * completed all of its business.
 */
public class AnimationCompletedEvent extends AnimationEvent
{
    public AnimationCompletedEvent (Animation anim)
    {
        super(anim);
    }
}
