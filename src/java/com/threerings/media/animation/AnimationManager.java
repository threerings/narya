//
// $Id: AnimationManager.java,v 1.18 2004/08/18 01:33:32 mdb Exp $

package com.threerings.media.animation;

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
            anim.willFinish(tickStamp);
            unregisterAnimation(anim);
            anim.didFinish(tickStamp);
            // Log.info("Removed finished animation " + anim + ".");
        }
    }
}
