//
// $Id: ParlorDecoder.java,v 1.3 2004/03/06 11:29:19 mdb Exp $

package com.threerings.parlor.client;

import com.threerings.parlor.client.ParlorReceiver;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.client.InvocationDecoder;
import com.threerings.util.Name;

/**
 * Dispatches calls to a {@link ParlorReceiver} instance.
 */
public class ParlorDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "5ef9ee0d359c42a9024498ee9aad119a";

    /** The method id used to dispatch {@link ParlorReceiver#gameIsReady}
     * notifications. */
    public static final int GAME_IS_READY = 1;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInvite}
     * notifications. */
    public static final int RECEIVED_INVITE = 2;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInviteResponse}
     * notifications. */
    public static final int RECEIVED_INVITE_RESPONSE = 3;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInviteCancellation}
     * notifications. */
    public static final int RECEIVED_INVITE_CANCELLATION = 4;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public ParlorDecoder (ParlorReceiver receiver)
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
        case GAME_IS_READY:
            ((ParlorReceiver)receiver).gameIsReady(
                ((Integer)args[0]).intValue()
            );
            return;

        case RECEIVED_INVITE:
            ((ParlorReceiver)receiver).receivedInvite(
                ((Integer)args[0]).intValue(), (Name)args[1], (GameConfig)args[2]
            );
            return;

        case RECEIVED_INVITE_RESPONSE:
            ((ParlorReceiver)receiver).receivedInviteResponse(
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (Object)args[2]
            );
            return;

        case RECEIVED_INVITE_CANCELLATION:
            ((ParlorReceiver)receiver).receivedInviteCancellation(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
