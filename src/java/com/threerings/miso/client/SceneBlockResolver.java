//
// $Id: SceneBlockResolver.java,v 1.9 2003/05/31 00:56:38 mdb Exp $

package com.threerings.miso.client;

import java.awt.EventQueue;

import com.samskivert.util.Histogram;
import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;

import com.threerings.miso.Log;

/**
 * A separate thread for resolving miso scene blocks.
 */
public class SceneBlockResolver extends LoopingThread
{
    /**
     * Queues up a scene block for resolution.
     */
    public void resolveBlock (SceneBlock block, boolean hipri)
    {
        Log.debug("Queueing block for resolution " + block +
                  " (" + hipri + ").");
        if (hipri) {
            _queue.prepend(block);
        } else {
            _queue.append(block);
        }
    }

    /**
     * Temporarily suspends the scene block resolution thread.
     */
    public synchronized void suspendResolution ()
    {
        _resolving = false;
    }

    /**
     * Restores the operation of the scene block resolution thread after a
     * previous call to {@link #suspendResolution}.
     */
    public synchronized void restoreResolution ()
    {
        _resolving = true;
        notify();
    }

    /**
     * Returns the number of scene blocks on the resolution queue.
     */
    public int queueSize ()
    {
        return _queue.size();
    }

    // documentation inherited
    public void iterate ()
    {
        final SceneBlock block = (SceneBlock)_queue.get();

        while (!_resolving) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    Log.info("Resolver interrupted.");
                }
            }
        }

        try {
            long start = System.currentTimeMillis();
            Log.debug("Resolving block " + block + ".");
            if (block.resolve()) {
                Log.debug("Resolved block " + block + ".");
            }
            long elapsed = System.currentTimeMillis() - start;
            _histo.addValue((int)elapsed);

            // warn if a block takes a long time to resolve
            if (elapsed > LONG_RESOLVE_TIME) {
                Log.warning("Block took long time to resolve [block=" + block +
                            ", elapsed=" + elapsed + "ms].");
            }

            // queue it up on the AWT thread to complete its resolution
            final boolean report = (_queue.size() == 0);
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    // let the block's panel know that it is resolved
                    block.wasResolved();
                    // report statistics
//                     if (report) {
//                         Log.info("Resolution histogram " +
//                                  _histo.summarize() + ".");
//                     }
                }
            });

        } catch (Exception e) {
            Log.warning("Block failed during resolution " + block + ".");
            Log.logStackTrace(e);
        }
    }

    /** The invoker's queue of units to be executed. */
    protected Queue _queue = new Queue();

    /** Indicates whether or not we are resolving or suspended. */
    protected boolean _resolving = true;

    /** Used to time block loading. */
    protected Histogram _histo = new Histogram(0, 25, 100);

    /** Blocks shouldn't take too long to resolve. */
    protected static final long LONG_RESOLVE_TIME = 500L;
}
