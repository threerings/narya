//
// $Id: AnimationManager.java,v 1.21 2001/12/18 09:46:07 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.media.Log;
import com.threerings.media.util.PerformanceMonitor;
import com.threerings.media.util.PerformanceObserver;

/**
 * The animation manager handles the regular refreshing of the scene view
 * to allow for animation.  It also may someday manage special scene-wide
 * animations, such as rain, fog, or earthquakes.
 */
public class AnimationManager
    implements Interval, PerformanceObserver
{
    /**
     * Construct and initialize the animation manager with a sprite
     * manager and the view in which the animations will take place. The
     * animation manager will automatically start itself up, but must be
     * explicitly shutdown when the animated view is no longer in
     * operation via a call to {@link #stop}.
     */
    public AnimationManager (SpriteManager spritemgr, AnimatedView view)
    {
        // save off references to the objects we care about
        _spritemgr = spritemgr;
        _view = view;

        // register to monitor the refresh action 
        PerformanceMonitor.register(this, "refresh", 1000);

        // start ourselves up
        start();
    }

    /**
     * Starts the animation manager to doing its business.
     */
    public synchronized void start ()
    {
        if (_ticker == null) {
            // create ticker for queueing up tick requests on AWT thread
            _ticker = new Runnable() {
                public void run () {
                    tick();
                }
            };
            // register the refresh interval
            _iid = IntervalManager.register(this, REFRESH_INTERVAL, null, true);
        }
    }

    /**
     * Instructs the animation manager to stop doing its business.
     */
    public synchronized void stop ()
    {
        if (_ticker != null) {
            _ticker = null;
            // un-register the refresh interval since we're now hidden
            IntervalManager.remove(_iid);
            _iid = -1;
        }
    }

    /**
     * Called by our interval when we'd like to begin a tick.  Returns
     * whether we're already ticking, and notes that we've requested
     * another tick.
     */
    protected synchronized boolean requestTick ()
    {
        return !(_ticking++ > 0);
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
            queueTick();
        }
    }

    /**
     * The <code>tick</code> method handles updating sprites and
     * repainting the target display.
     */
    protected void tick ()
    {
        synchronized (this) {
            // see if we were shutdown since we were last queued up
            if (_ticker == null) {
                return;
            }
        }

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
	// PerformanceMonitor.tick(AnimationManager.this, "refresh");

        if (finishedTick()) {
            // finishedTick returning true means there's been a
            // request for at least one more tick since we started
            // this tick, so we want to queue up another tick
            // immediately
            queueTick();
        }
    }

    /**
     * Queues up a tick on the AWT event handler thread, iff we are still
     * operating.
     */
    protected synchronized void queueTick ()
    {
        if (_ticker != null) {
            SwingUtilities.invokeLater(_ticker);
        }
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
        Log.info(name + " [ticks=" + ticks + "].");
    }

    /** The ticker runnable that we put on the AWT thread periodically. */
    protected Runnable _ticker;

    /** The number of outstanding tick requests. */
    protected int _ticking = 0;

    /** The refresh interval id. */
    protected int _iid = -1;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The view on which we are animating. */
    protected AnimatedView _view;

    /** The desired number of refresh operations per second. */
    protected static final int FRAME_RATE = 70;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long REFRESH_INTERVAL = 1000 / FRAME_RATE;
}
