//
// $Id: SceneSender.java,v 1.2 2002/08/20 19:38:15 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.whirled.client.SceneDecoder;
import com.threerings.whirled.client.SceneReceiver;

/**
 * Used to issue notifications to a {@link SceneReceiver} instance on a
 * client.
 *
 * <p> Generated from <code>
 * $Id: SceneSender.java,v 1.2 2002/08/20 19:38:15 mdb Exp $
 * </code>
 */
public class SceneSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * SceneReceiver#forcedMove} on a client.
     */
    public static void forcedMove (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, SceneDecoder.RECEIVER_CODE, SceneDecoder.FORCED_MOVE,
            new Object[] { new Integer(arg1) });
    }

    // Generated on 12:37:01 08/20/02.
}
