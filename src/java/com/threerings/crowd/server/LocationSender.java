//
// $Id: LocationSender.java,v 1.3 2004/02/25 14:41:47 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.crowd.client.LocationDecoder;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * Used to issue notifications to a {@link LocationReceiver} instance on a
 * client.
 *
 * <p> Generated from <code>
 * $Id: LocationSender.java,v 1.3 2004/02/25 14:41:47 mdb Exp $
 * </code>
 */
public class LocationSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * LocationReceiver#forcedMove} on a client.
     */
    public static void forcedMove (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, LocationDecoder.RECEIVER_CODE, LocationDecoder.FORCED_MOVE,
            new Object[] { new Integer(arg1) });
    }

    // Generated on 12:36:59 08/20/02.
}
