//
// $Id: AnimationManager.java,v 1.15 2002/11/05 20:51:50 mdb Exp $

package com.threerings.media.animation;

import com.samskivert.util.ObserverList;

import com.threerings.media.AbstractMediaManager;
import com.threerings.media.Log;
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

        // remove any finished animations
        for (int ii=_media.size() - 1; ii >= 0; ii--) {
            Animation anim = (Animation)_media.get(ii);
            if (anim.isFinished()) {
                // let any animation observers know that we're done
                anim.notifyObservers(
                    new AnimationCompletedEvent(anim, tickStamp));
                // un-register the animation
                unregisterAnimation(anim);
                // let the animation clean itself up as necessary
                anim.didFinish();
                // Log.info("Removed finished animation [anim=" + anim + "].");
            }
        }
    }

    // documentation inherited
    protected void dispatchEvent (ObserverList observers, Object event)
    {
        _dispatchOp.init((AnimationEvent)event);
        observers.apply(_dispatchOp);
    }

    /** Used by {@link #dispatchEvent}. */
    protected static class DispatchOp implements ObserverList.ObserverOp
    {
        public void init (AnimationEvent event)
        {
            _event = event;
        }

        public boolean apply (Object observer)
        {
            ((AnimationObserver)observer).handleEvent(_event);
            return true;
        }

        protected AnimationEvent _event;
    };

    protected DispatchOp _dispatchOp = new DispatchOp();
}
