//
// $Id: SceneBlockResolver.java,v 1.2 2003/04/25 22:13:14 mdb Exp $

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
    public void resolveBlock (SceneBlock block)
    {
        _queue.append(block);
    }

    // documentation inherited
    public void iterate ()
    {
        final SceneBlock block = (SceneBlock)_queue.get();

        try {
            // resolve the block
            block.resolve();
//             if (block.resolve()) {
//                 Log.info("Resolved block " + block + ".");
//             }

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
}
