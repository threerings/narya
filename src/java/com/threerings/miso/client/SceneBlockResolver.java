//
// $Id: SceneBlockResolver.java,v 1.5 2003/05/20 23:57:48 mdb Exp $

package com.threerings.miso.client;

import java.awt.EventQueue;

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
            Log.debug("Resolving block " + block + ".");
            if (block.resolve()) {
                Log.debug("Resolved block " + block + ".");
            }

            // queue it up on the AWT thread to complete its resolution
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    // let the block's panel know that it is resolved
                    block.wasResolved();
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
}
