//
// $Id$

package com.threerings.whirled.zone.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.whirled.zone.client.ZoneDecoder;
import com.threerings.whirled.zone.client.ZoneReceiver;

/**
 * Used to issue notifications to a {@link ZoneReceiver} instance on a
 * client.
 */
public class ZoneSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * ZoneReceiver#forcedMove} on a client.
     */
    public static void forcedMove (
        ClientObject target, int arg1, int arg2)
    {
        sendNotification(
            target, ZoneDecoder.RECEIVER_CODE, ZoneDecoder.FORCED_MOVE,
            new Object[] { new Integer(arg1), new Integer(arg2) });
    }

}
