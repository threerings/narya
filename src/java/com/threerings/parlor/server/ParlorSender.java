//
// $Id$

package com.threerings.parlor.server;

import com.threerings.parlor.client.ParlorDecoder;
import com.threerings.parlor.client.ParlorReceiver;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.util.Name;

/**
 * Used to issue notifications to a {@link ParlorReceiver} instance on a
 * client.
 */
public class ParlorSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * ParlorReceiver#gameIsReady} on a client.
     */
    public static void gameIsReady (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, ParlorDecoder.RECEIVER_CODE, ParlorDecoder.GAME_IS_READY,
            new Object[] { new Integer(arg1) });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * ParlorReceiver#receivedInvite} on a client.
     */
    public static void sendInvite (
        ClientObject target, int arg1, Name arg2, GameConfig arg3)
    {
        sendNotification(
            target, ParlorDecoder.RECEIVER_CODE, ParlorDecoder.RECEIVED_INVITE,
            new Object[] { new Integer(arg1), arg2, arg3 });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * ParlorReceiver#receivedInviteResponse} on a client.
     */
    public static void sendInviteResponse (
        ClientObject target, int arg1, int arg2, Object arg3)
    {
        sendNotification(
            target, ParlorDecoder.RECEIVER_CODE, ParlorDecoder.RECEIVED_INVITE_RESPONSE,
            new Object[] { new Integer(arg1), new Integer(arg2), arg3 });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * ParlorReceiver#receivedInviteCancellation} on a client.
     */
    public static void sendInviteCancellation (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, ParlorDecoder.RECEIVER_CODE, ParlorDecoder.RECEIVED_INVITE_CANCELLATION,
            new Object[] { new Integer(arg1) });
    }

}
