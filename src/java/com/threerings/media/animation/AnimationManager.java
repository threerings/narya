//
// $Id: AnimationManager.java,v 1.6 2002/02/18 06:05:59 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.media.Log;
import com.threerings.media.sprite.SpriteManager;
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
     * Constructs and initializes an animation manager. If sprites are to
     * be used with this animation manager, the other constructor should
     * be used or the sprite manager should be set shortly after
     * construction via {@link #setSpriteManager}. The animation manager
     * will automatically start itself up, but must be explicitly shutdown
     * when the animated view is no longer in operation via a call to
     * {@link #stop}.
     */
    public AnimationManager (AnimatedView view)
    {
        this(null, view);
    }

    /**
     * Sets the sprite manager with which this animation manager should
     * coordinate.
     */
    public void setSpriteManager (SpriteManager spritemgr)
    {
        _spritemgr = spritemgr;
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
            _iid = IntervalManager.register(this, _refreshInterval, null, true);
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
     * Adds a rectangle to the dirty rectangle list.  Note that the
     * rectangle may be destructively modified by the animation manager at
     * some later date.
     *
     * @param rect the rectangle to add.
     */
    public void addDirtyRect (Rectangle rect)
    {
        _dirty.add(rect);
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
        // Log.info("Registered animation [anim=" + anim + "].");
    }

    /**
     * Un-registers the given {@link Animation} from the animation
     * manager.
     */
    public void unregisterAnimation (Animation anim)
    {
        // un-register the animation
        if (!_anims.remove(anim)) {
            Log.warning("Attempt to un-register animation that isn't " +
                        "registered [anim=" + anim + "].");
        }

        // dirty the animation bounds
        anim.invalidate();
        // Log.info("Un-registered animation [anim=" + anim + "].");
    }

    /**
     * Renders all registered animations to the given graphics context.
     */
    public void renderAnimations (Graphics2D gfx)
    {
        int size = _anims.size();
        for (int ii = 0; ii < size; ii++) {
            Animation anim = (Animation)_anims.get(ii);
            anim.paint(gfx);
        }
    }

    /**
     * Lets the animation manager know that the animated view is scrolling
     * at the specified rate (in milliseconds per pixel) so that it can
     * adjust its rendering loop interval to coincide with the scrolling
     * speed, and cause the animated view to be rendered every time
     * through the loop regardless of whether it has dirty regions (so
     * that it can scroll).
     */
    public void setScrolling (int mspp)
    {
        // sanity check
        if (mspp < 0) {
            String errmsg ="Negative scroll velocity illegal " +
                "[mspp=" + mspp + "]";
            throw new IllegalArgumentException(errmsg);
        }

        // make a note of our scrolling velocity
        _scrollvel = mspp;

        // we want to adjust our refresh interval at which we tick to
        // coincide with an even number of scrolled pixels
        long upperTarget = 1000 / MIN_FRAME_RATE;
        long lowerTarget = 1000 / MAX_FRAME_RATE;

        // start out assuming that we can refresh for every pixel
        _refreshInterval = _scrollvel;

        // if they've disabled scrolling, go back to the default refresh
        // interval
        if (_scrollvel == 0) {
            _refreshInterval = DEFAULT_REFRESH_INTERVAL;

            // if the interval is too quick, bump it up a bit
        } else if (_refreshInterval < lowerTarget) {
            // keep adding a pixel at a time until we're above our minimum
            // refresh interval
            while (_refreshInterval < lowerTarget) {
                _refreshInterval += mspp;
            }

            // if it's too slow, we'll want to refresh at some even
            // division of the desired rate
        } else if (_refreshInterval > upperTarget) {
            // try dividing the desired rate by larger and larger values
            // until we're under the upper target
            for (int i = 2; i < 100 && _refreshInterval > upperTarget; i++) {
                _refreshInterval = (mspp / i);
            }

            // if the desired velocity is more than one hundred times
            // slower than our desired framerate, then they're not going
            // to notice if things aren't perfectly in sync, so fuck 'em
            if (_refreshInterval > upperTarget) {
                _refreshInterval = DEFAULT_REFRESH_INTERVAL;
            }
        }

//         Log.info("Set scrolling velocity [velocity=" + _scrollvel +
//                  ", refresh=" + _refreshInterval + "].");

        // now stop and start ourselves to reregister our interval
        stop();
        start();
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
        if (_spritemgr != null) {
            _spritemgr.tick(now, _dirty);
        }

        // call tick on all animations
        tickAnimations(now);

        // perform a single pass merging overlapping rectangles.  note
        // that this will also clear out the contents of our internal
        // dirty rectangle list.
        List rects = mergeDirtyRects(_dirty);
        int rcount = rects.size();

        // invalidate screen-rects dirtied by sprites and/or animations
	if (rcount > 0 || _scrollvel > 0) {
	    // pass the dirty-rects on to the scene view
	    _view.invalidateRects(rects);

	    // refresh the display
            _view.paintImmediately(rcount);
	}

        // remove any finished animations
        removeFinishedAnimations();

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
     * Calls tick on all animations currently registered with the
     * animation manager.
     */
    protected void tickAnimations (long timestamp)
    {
        int size = _anims.size();
        for (int ii = 0; ii < size; ii++) {
            ((Animation)_anims.get(ii)).tick(timestamp);
        }
    }

    /**
     * Removes any finished animations from the list of animations and
     * notifies their respective animation observers, if any.
     */
    protected void removeFinishedAnimations ()
    {
        int size = _anims.size();
        for (int ii = size - 1; ii >= 0; ii--) {
            Animation anim = (Animation)_anims.get(ii);
            if (anim.isFinished()) {
                // let any animation observers know that we're done
                anim.notifyObservers(new AnimationCompletedEvent(anim));
                // un-register the animation
                unregisterAnimation(anim);
                // Log.info("Removed finished animation [anim=" + anim + "].");
            }
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

    /**
     * Returns a new list of dirty rectangles representing the given list
     * with any intersecting rectangles merged.  The given list is
     * destructively modified and cleared of all contents.
     *
     * @return the list of merged dirty rects.
     */
    protected List mergeDirtyRects (List rects)
    {
        ArrayList merged = new ArrayList();

        while (rects.size() > 0) {
            // pop the next rectangle from the dirty list
            Rectangle mr = (Rectangle)rects.remove(0);

            // merge in any overlapping rectangles
            for (int ii = 0; ii < rects.size(); ii++) {
                Rectangle r = (Rectangle)rects.get(ii);
                if (mr.intersects(r)) {
                    // remove the overlapping rectangle from the list
                    rects.remove(ii--);
                    // grow the merged dirty rectangle
                    mr.add(r);
                }
            }

            // add the merged rectangle to the list
            merged.add(mr);
        }

        return merged;
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
        Log.info(name + " [ticks=" + ticks + "].");
    }

    /** The list of animations. */
    protected ArrayList _anims = new ArrayList();

    /** The dirty rectangles. */
    protected ArrayList _dirty = new ArrayList();

    /** The ticker runnable that we put on the AWT thread periodically. */
    protected Runnable _ticker;

    /** The number of milliseconds in between our refreshes. */
    protected long _refreshInterval = DEFAULT_REFRESH_INTERVAL;

    /** The number of outstanding tick requests. */
    protected int _ticking = 0;

    /** The refresh interval id. */
    protected int _iid = -1;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;

    /** The view on which we are animating. */
    protected AnimatedView _view;

    /** The velocity at which we are scrolling (in milliseconds per pixel)
     * or zero if we're not scrolling. */
    protected int _scrollvel;

    /** The default number of refresh operations per second. */
    protected static final int DEFAULT_FRAME_RATE = 30;

    /** The minimum frame rate we'll adjust to when scrolling. */
    protected static final int MIN_FRAME_RATE = 20;

    /** The maximum frame rate we'll adjust to when scrolling. */
    protected static final int MAX_FRAME_RATE = 40;

    /** The milliseconds to sleep to obtain desired frame rate. */
    protected static final long DEFAULT_REFRESH_INTERVAL =
        1000 / DEFAULT_FRAME_RATE;
}
