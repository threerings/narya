//
// $Id: ZoneSender.java,v 1.2 2002/08/20 19:38:16 mdb Exp $

package com.threerings.whirled.zone.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.whirled.zone.client.ZoneDecoder;
import com.threerings.whirled.zone.client.ZoneReceiver;

/**
 * Used to issue notifications to a {@link ZoneReceiver} instance on a
 * client.
 *
 * <p> Generated from <code>
 * $Id: ZoneSender.java,v 1.2 2002/08/20 19:38:16 mdb Exp $
 * </code>
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

    // Generated on 12:37:00 08/20/02.
}
