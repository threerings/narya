//
// $Id: AnimationManager.java,v 1.6 2001/08/04 01:41:02 shaper Exp $

package com.threerings.miso.sprite;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

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
    public AnimationManager (SpriteManager spritemgr, JComponent target,
                             SceneView view)
    {
        // save off references to the objects we care about
        _spritemgr = spritemgr;
        _target = target;
        _view = view;

        // create a ticker for queueing up tick requests on the AWT thread
        _ticker = new Runnable() {
            public void run ()
            {
                tick();
            }
        };

        // register to monitor the refresh action 
        PerformanceMonitor.register(this, "refresh", 1000);

        // register ourselves with the interval manager
        IntervalManager.register(this, REFRESH_INTERVAL, null, true);
    }

    protected synchronized boolean requestTick ()
    {
        if (_ticking++ > 0) return false;
        return true;
    }

    protected synchronized boolean finishedTick ()
    {
        if (--_ticking > 0) {
            _ticking = 1;
            return true;
        }

        return false;
    }

    public void intervalExpired (int id, Object arg)
    {
        if (requestTick()) {
            // throw the tick task on the AWT thread task queue
            SwingUtilities.invokeLater(_ticker);
        }
    }

    protected void tick ()
    {
        // call tick on all sprites
        _spritemgr.tick();

        // invalidate screen-rects dirtied by sprites
        ArrayList rects = _spritemgr.getDirtyRects();
        _view.invalidateRects(rects);

        // update frame-rate information
        //PerformanceMonitor.tick(AnimationManager.this, "refresh");

        // refresh the display
        _target.paintImmediately(_target.getBounds());

        if (finishedTick()) {
            // finishedTick returning true means there's been a
            // request for at least one more tick since we started
            // this tick, so we want to queue up another tick
            // immediately
            SwingUtilities.invokeLater(_ticker);
        }
    }

    public void checkpoint (String name, int ticks)
    {
        Log.info(name + " [ticks=" + ticks + "].");
    }

    /** The ticker runnable that we put on the AWT thread periodically. */
    protected Runnable _ticker;

    /** The desired number of refresh operations per second. */
    protected static final int FRAME_RATE = 20;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long REFRESH_INTERVAL = 1000 / FRAME_RATE;

    /** The number of outstanding tick requests. */
    protected int _ticking = 0;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The component to refresh. */
    protected JComponent _target;

    /** The scene view. */
    protected SceneView _view;
}
