//
// $Id: PerformanceMonitor.java,v 1.4 2002/04/23 01:16:28 mdb Exp $

package com.threerings.media.util;

import java.util.HashMap;

import com.threerings.media.Log;

/**
 * Provides a simple mechanism for monitoring the number of times an
 * action takes place within a certain time period.
 *
 * <p> The action being tracked should be registered with a suitable name
 * via {@link #register}, and {@link #tick} should be called each time the
 * action is performed.
 *
 * <p> Whenever {@link #tick} is called and the checkpoint time interval
 * has elapsed since the last checkpoint (if any), the observer will be
 * notified to that effect by a call to {@link
 * PerformanceObserver#checkpoint}.
 *
 * <p> Note that this is <em>not</em> intended to be used as an
 * industrial-strength profiling or performance monitoring tool.  The
 * checkpoint time interval granularity is in milliseconds, not
 * microseconds, and the observer's <code>checkpoint()</code> method will
 * never be called until/unless a subsequent call to {@link #tick} is made
 * after the requested number of milliseconds have passed since the last
 * checkpoint.
 */
public class PerformanceMonitor
{
    /**
     * Register a new action with an observer, the action name, and
     * the milliseconds to wait between checkpointing the action's
     * performance.
     *
     * @param obs the action observer.
     * @param name the action name.
     * @param delta the milliseconds between checkpoints.
     */
    public static void register (PerformanceObserver obs, String name,
                                 long delta)
    {
        // get the observer's action hashtable
        HashMap actions = (HashMap)_observers.get(obs);
        if (actions == null) {
            // create it if it didn't exist
            _observers.put(obs, actions = new HashMap());
        }

        // add the action to the set we're tracking
        actions.put(name, new PerformanceAction(obs, name, delta));
    }

    /**
     * Un-register the named action associated with the given observer.
     *
     * @param obs the action observer.
     * @param name the action name.
     */
    public static void unregister (PerformanceObserver obs, String name)
    {
        // get the observer's action hashtable
        HashMap actions = (HashMap)_observers.get(obs);
        if (actions == null) {
            Log.warning("Attempt to unregister by unknown observer " +
                        "[observer=" + obs + ", name=" + name + "].");
            return;
        }

        // attempt to remove the specified action
        PerformanceAction action = (PerformanceAction)actions.remove(name);
        if (action == null) {
            Log.warning("Attempt to unregister unknown action " +
                        "[observer=" + obs + ", name=" + name + "].");
            return;
        }

        // if the observer has no actions left, remove the observer's action
        // hash in its entirety
        if (actions.size() == 0) {
            _observers.remove(obs);
        }
    }

    /**
     * Tick the named action associated with the given observer.
     *
     * @param obs the action observer.
     * @param name the action name.
     */
    public static void tick (PerformanceObserver obs, String name)
    {
        // get the observer's action hashtable
        HashMap actions = (HashMap)_observers.get(obs);
        if (actions == null) {
            Log.warning("Attempt to tick by unknown observer " +
                        "[observer=" + obs + ", name=" + name + "].");
            return;
        }

        // get the specified action
        PerformanceAction action = (PerformanceAction)actions.get(name);
        if (action == null) {
            Log.warning("Attempt to tick unknown value " +
                        "[observer=" + obs + ", name=" + name + "].");
            return;
        }

        // tick the action
        action.tick();
    }

    /** The observers monitoring some set of actions. */
    protected static HashMap _observers = new HashMap();
}

/**
 * This class represents the individual actions being tracked by the
 * <code>PerformanceMonitor</code> class.
 */
class PerformanceAction
{
    public PerformanceAction (PerformanceObserver obs, String name, long delta)
    {
        _obs = obs;
        _name = name;
        _delta = delta;
        _lastDelta = System.currentTimeMillis();
    }

    public void tick ()
    {
        _numTicks++;

        long now = System.currentTimeMillis();
        long passed = now - _lastDelta;
        if (passed >= _delta) {
            // update the last checkpoint time
            _lastDelta = now + (passed - _delta);

            // notify our observer of the checkpoint
            _obs.checkpoint(_name, _numTicks);

            // reset the tick count
            _numTicks = 0;
        }
    }

    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[obs=").append(_obs);
        buf.append(", name=").append(_name);
        buf.append(", delta=").append(_delta);
        buf.append(", lastDelta=").append(_lastDelta);
        buf.append(", numTicks=").append(_numTicks);
        return buf.append("]").toString();
    }

    /** The performance observer. */
    protected PerformanceObserver _obs;

    /** The action name. */
    protected String _name;

    /** The number of milliseconds between each checkpoint. */
    protected long _delta;

    /** The time the last time a checkpoint was made. */ 
    protected long _lastDelta;

    /** The number of ticks since the last checkpoint. */
    protected int _numTicks;
}
