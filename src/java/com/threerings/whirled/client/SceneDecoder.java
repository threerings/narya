//
// $Id$

package com.threerings.whirled.client;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.whirled.client.SceneReceiver;

/**
 * Dispatches calls to a {@link SceneReceiver} instance.
 */
public class SceneDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "c4d0cf66b81a6e83d119b2d607725651";

    /** The method id used to dispatch {@link SceneReceiver#forcedMove}
     * notifications. */
    public static final int FORCED_MOVE = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public SceneDecoder (SceneReceiver receiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    public void dispatchNotification (int methodId, Object[] args)
    {
        switch (methodId) {
        case FORCED_MOVE:
            ((SceneReceiver)receiver).forcedMove(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
