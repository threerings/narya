//
// $Id: AnimationManager.java,v 1.16 2001/09/13 19:10:26 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.media.Log;
import com.threerings.miso.util.PerformanceMonitor;
import com.threerings.miso.util.PerformanceObserver;

/**
 * The AnimationManager handles the regular refreshing of the scene
 * view to allow for animation.  It also may someday manage special
 * scene-wide animations, such as rain, fog, or earthquakes.
 */
public class AnimationManager
    implements Interval, PerformanceObserver, AncestorListener
{
    /**
     * Construct and initialize the animation manager with a sprite
     * manager and the view in which the animations will take place.
     */
    public AnimationManager (SpriteManager spritemgr, AnimatedView view)
    {
        // save off references to the objects we care about
        _spritemgr = spritemgr;
        _view = view;

        // register to monitor the refresh action 
        PerformanceMonitor.register(this, "refresh", 1000);

	// register the refresh interval immediately if the component
	// is already showing on-screen
	JComponent target = _view.getComponent();
  	if (target.isShowing()) {
	    registerInterval();
	}

	// listen to the view's ancestor events
	target.addAncestorListener(this);

        // create a ticker for queueing up tick requests on the AWT thread
        _ticker = new Runnable() {
            public void run ()
            {
                tick();
            }
        };
    }

    /**
     * Register the animation manager's refresh interval with the
     * interval manager.
     */
    protected void registerInterval ()
    {
	_iid = IntervalManager.register(this, REFRESH_INTERVAL, null, true);
    }

    /**
     * Called by our interval when we'd like to begin a tick.  Returns
     * whether we're already ticking, and notes that we've requested
     * another tick.
     */
    protected synchronized boolean requestTick ()
    {
        if (_ticking++ > 0) return false;
        return true;
    }

    /**
     * Called by the tick task when it's finished with a tick.
     * Returns whether a new tick task should be created immediately.
     */
    protected synchronized boolean finishedTick ()
    {
        if (--_ticking > 0) {
            _ticking = 1;
            return true;
        }

        return false;
    }

    /**
     * The <code>IntervalManager</code> calls this method as often as
     * we've requested to obtain our desired frame rate.  Since we'd
     * like to avoid messy thread-synchronization between the AWT
     * thread and other threads, here we just add the tick task to the
     * AWT thread for later execution.
     */
    public void intervalExpired (int id, Object arg)
    {
        if (requestTick()) {
            // throw the tick task on the AWT thread task queue
            SwingUtilities.invokeLater(_ticker);
        }
    }

    /**
     * The <code>tick</code> method handles updating sprites and
     * repainting the target display.
     */
    protected void tick ()
    {
        // every tick should have a timestamp associated with it
        long now = System.currentTimeMillis();

        // call tick on all sprites
        _spritemgr.tick(now);

        // invalidate screen-rects dirtied by sprites
        DirtyRectList rects = _spritemgr.getDirtyRects();
	if (rects.size() > 0) {
	    // pass the dirty-rects on to the scene view
	    _view.invalidateRects(rects);

	    // refresh the display
            _view.paintImmediately();
	}

	// update refresh-rate information
	//PerformanceMonitor.tick(AnimationManager.this, "refresh");

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

    /** AncestorListener interface methods. */

    public void ancestorAdded (AncestorEvent event)
    {
	if (_iid == -1) {
	    // register the refresh interval since we're now visible
	    registerInterval();
	}
    }

    public void ancestorRemoved (AncestorEvent event)
    {
	// un-register the refresh interval since we're now hidden
	IntervalManager.remove(_iid);
	_iid = -1;
    }

    public void ancestorMoved (AncestorEvent event) { }

    /** The ticker runnable that we put on the AWT thread periodically. */
    protected Runnable _ticker;

    /** The desired number of refresh operations per second. */
    protected static final int FRAME_RATE = 70;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long REFRESH_INTERVAL = 1000 / FRAME_RATE;

    /** The number of outstanding tick requests. */
    protected int _ticking = 0;

    /** The refresh interval id. */
    protected int _iid = -1;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The view on which we are animating. */
    protected AnimatedView _view;
}
