//
// $Id: AnimationManager.java,v 1.17 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media.animation;

import com.samskivert.util.ObserverList;

import com.threerings.media.AbstractMediaManager;
import com.threerings.media.RegionManager;

/**
 * Manages a collection of animations, ticking them when the animation
 * manager itself is ticked and generating events when animations finish
 * and suchlike.
 */
public class AnimationManager extends AbstractMediaManager
{
    /**
     * Construct and initialize the animation manager which readies itself
     * to manage animations.
     */
    public AnimationManager (RegionManager remgr)
    {
        super(remgr);
    }

    /**
     * Registers the given {@link Animation} with the animation manager
     * for ticking and painting.
     */
    public void registerAnimation (Animation anim)
    {
        insertMedia(anim);
    }

    /**
     * Un-registers the given {@link Animation} from the animation
     * manager. The bounds of the animation will automatically be
     * invalidated so that they are properly rerendered in the absence of
     * the animation.
     */
    public void unregisterAnimation (Animation anim)
    {
        removeMedia(anim);
    }

    // documentation inherited
    protected void tickAllMedia (long tickStamp)
    {
        super.tickAllMedia(tickStamp);

        for (int ii = _media.size() - 1; ii >= 0; ii--) {
            Animation anim = (Animation)_media.get(ii);
            if (!anim.isFinished()) {
                continue;
            }

            // as the anim is finished, remove it and notify observers
            anim.queueNotification(new AnimCompletedOp(anim, tickStamp));
            unregisterAnimation(anim);
            anim.didFinish();
            // Log.info("Removed finished animation " + anim + ".");
        }
    }

    /** Used to dispatch {@link AnimationObserver#animationCompleted}. */
    protected static class AnimCompletedOp implements ObserverList.ObserverOp
    {
        public AnimCompletedOp (Animation anim, long when) {
            _anim = anim;
            _when = when;
        }

        public boolean apply (Object observer) {
            ((AnimationObserver)observer).animationCompleted(_anim, _when);
            return true;
        }

        protected Animation _anim;
        protected long _when;
    }
}
