//
// $Id: AnimationManager.java,v 1.9 2002/04/23 01:16:28 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Shape;

import java.util.ArrayList;

import com.threerings.media.Log;
import com.threerings.media.MediaConstants;
import com.threerings.media.RegionManager;

/**
 * Manages a collection of animations, ticking them when the animation
 * manager itself is ticked and generating events when animations finish
 * and suchlike.
 */
public class AnimationManager
    implements MediaConstants
{
    /**
     * Construct and initialize the animation manager which readies itself
     * to manage animations.
     */
    public AnimationManager (RegionManager remgr)
    {
        _remgr = remgr;
    }

    /**
     * Registers the given {@link Animation} with the animation manager
     * for ticking and painting.
     */
    public void registerAnimation (Animation anim)
    {
        if (_anims.contains(anim)) {
            Log.warning("Attempt to register animation more than once " +
                        "[anim=" + anim + "].");
            return;
        }

        anim.setAnimationManager(this);
        _anims.add(anim);
    }

    /**
     * Un-registers the given {@link Animation} from the animation
     * manager. The bounds of the animation will automatically be
     * invalidated so that they are properly rerendered in the absence of
     * the animation.
     */
    public void unregisterAnimation (Animation anim)
    {
        // un-register the animation
        if (!_anims.remove(anim)) {
            Log.warning("Attempt to un-register animation that isn't " +
                        "registered [anim=" + anim + "].");
            return;
        }

        // invalidate its bounds
        _remgr.invalidateRegion(anim.getBounds());
    }

    /**
     * Provides access to the region manager that the animation manager is
     * using to collect invalid regions every frame. This should generally
     * only be used by animations that want to invalidate themselves.
     */
    public RegionManager getRegionManager ()
    {
        return _remgr;
    }

    /**
     * Handles updating animations and generating associated events.
     *
     * @param tickStamp the system clock at the time of the tick.
     */
    public void tick (long tickStamp)
    {
        // tick all of our animations
        int size = _anims.size();
        for (int ii = 0; ii < size; ii++) {
            ((Animation)_anims.get(ii)).tick(tickStamp);
        }

        // remove any finished animations
        for (int ii = size - 1; ii >= 0; ii--) {
            Animation anim = (Animation)_anims.get(ii);
            if (anim.isFinished()) {
                // let any animation observers know that we're done
                anim.notifyObservers(new AnimationCompletedEvent(anim));
                // un-register the animation
                unregisterAnimation(anim);
                // let the animation clean itself up as necessary
                anim.didFinish();
                // Log.info("Removed finished animation [anim=" + anim + "].");
            }
        }
    }

    /**
     * Renders all registered animations in the given layer that intersect
     * the supplied clipping rectangle to the given graphics context.
     *
     * @param layer the layer to render; one of {@link #FRONT}, {@link
     * #BACK}, or {@link #ALL}.  The front layer contains all animations
     * with a positive render order; the back layer contains all
     * animations with a negative render order; all, both.
     */
    public void renderAnimations (Graphics2D gfx, int layer, Shape clip)
    {
        int size = _anims.size();
        for (int ii = 0; ii < size; ii++) {
            Animation anim = (Animation)_anims.get(ii);
            int order = anim.getRenderOrder();
            if (((layer == ALL) ||
                 (layer == FRONT && order >= 0) ||
                 (layer == BACK && order < 0)) &&
                clip.intersects(anim.getBounds())) {
                anim.paint(gfx);
            }
        }
    }

    /** Used to accumulate dirty regions. */
    protected RegionManager _remgr;

    /** The list of animations. */
    protected ArrayList _anims = new ArrayList();
}
