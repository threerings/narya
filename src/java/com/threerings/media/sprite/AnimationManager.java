//
// $Id: AnimationManager.java,v 1.3 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.sprite;

import java.awt.Component;
import java.util.ArrayList;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;
import com.threerings.miso.Log;
import com.threerings.miso.scene.SceneView;
import com.threerings.miso.util.PerformanceMonitor;
import com.threerings.miso.util.PerformanceObserver;

/**
 * The AnimationManager handles the regular refreshing of the scene
 * view to allow for animation.  It also may someday manage special
 * scene-wide animations, such as rain, fog, or earthquakes.
 */
public class AnimationManager implements Interval, PerformanceObserver
{
    /**
     * Construct and initialize the animation manager with a sprite
     * manager and the panel that animations will take place within.
     */
    public AnimationManager (SpriteManager spritemgr, Component target,
                             SceneView view)
    {
        // save off references to the objects we care about
        _spritemgr = spritemgr;
        _target = target;
        _view = view;

        // register to monitor the refresh action 
        PerformanceMonitor.register(this, "refresh", 1000);

        // register ourselves with the interval manager
        IntervalManager.register(this, REFRESH_INTERVAL, null, true);
    }

    public void intervalExpired (int id, Object arg)
    {
        // refresh the display
        _target.repaint();

        // call tick on all sprites
        _spritemgr.tick();

        // invalidate screen-rects dirtied by sprites
        ArrayList rects = _spritemgr.getDirtyRects();
        _view.invalidateRects(rects);

        // update frame-rate information
        //PerformanceMonitor.tick(this, "refresh");
    }

    public void checkpoint (String name, int ticks)
    {
        Log.info(name + "[ticks=" + ticks + "].");
    }

    /** The desired number of refresh operations per second. */
    protected static final int FRAME_RATE = 20;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long REFRESH_INTERVAL = 1000 / FRAME_RATE;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The component to refresh. */
    protected Component _target;

    /** The scene view. */
    protected SceneView _view;
}
