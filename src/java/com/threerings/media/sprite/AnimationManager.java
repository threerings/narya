//
// $Id: AnimationManager.java,v 1.2 2001/07/31 01:38:28 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.Component;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

/**
 * The AnimationManager handles the regular refreshing of the scene
 * view to allow for animation.  It also may someday manage special
 * scene-wide animations, such as rain, fog, or earthquakes.
 */
public class AnimationManager
{
    /**
     * Construct and initialize the animation manager with a sprite
     * manager and the panel that animations will take place within.
     */
    public AnimationManager (SpriteManager spritemgr, Component target)
    {
        _spritemgr = spritemgr;
        _target = target;

        // create the interval for refreshing the display
        Interval refresher = new Interval() {
            public void intervalExpired (int id, Object arg)
            {
                // refresh the display
                _target.repaint();

                // call tick on all sprites
                _spritemgr.tick();
            }
        };

        // register ourselves with the interval mgr
        IntervalManager.register(refresher, REFRESH_INTERVAL, null, true);
    }

    /** The desired number of refresh operations per second. */
    protected static final int FRAME_RATE = 60;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long REFRESH_INTERVAL = 1000 / FRAME_RATE;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The component to refresh. */
    protected Component _target;
}
