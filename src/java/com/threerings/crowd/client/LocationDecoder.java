//
// $Id$

package com.threerings.crowd.client;

import com.threerings.crowd.client.LocationReceiver;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link LocationReceiver} instance.
 */
public class LocationDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "58f2830e027f4f3377e100ef12332497";

    /** The method id used to dispatch {@link LocationReceiver#forcedMove}
     * notifications. */
    public static final int FORCED_MOVE = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public LocationDecoder (LocationReceiver receiver)
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
            ((LocationReceiver)receiver).forcedMove(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
