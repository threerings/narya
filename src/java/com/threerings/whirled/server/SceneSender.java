//
// $Id$

package com.threerings.whirled.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.whirled.client.SceneDecoder;
import com.threerings.whirled.client.SceneReceiver;

/**
 * Used to issue notifications to a {@link SceneReceiver} instance on a
 * client.
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

}
