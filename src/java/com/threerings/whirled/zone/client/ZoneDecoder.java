//
// $Id: ZoneDecoder.java,v 1.2 2002/08/20 19:38:15 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.whirled.zone.client.ZoneReceiver;

/**
 * Dispatches calls to a {@link ZoneReceiver} instance.
 *
 * <p> Generated from <code>
 * $Id: ZoneDecoder.java,v 1.2 2002/08/20 19:38:15 mdb Exp $
 * </code>
 */
public class ZoneDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "2d900cf54355111b4bb4befcdff42b82";

    /** The method id used to dispatch {@link ZoneReceiver#forcedMove}
     * notifications. */
    public static final int FORCED_MOVE = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public ZoneDecoder (ZoneReceiver receiver)
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
            ((ZoneReceiver)receiver).forcedMove(
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }

    // Generated on 12:37:00 08/20/02.
}
