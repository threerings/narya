//
// $Id: ClusterChatMessageHandler.java,v 1.1 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.whirled.server.WhirledServer;
import com.threerings.whirled.spot.Log;

/**
 * The cluster chat message handler handles chat messages that are issued
 * to the cluster associated with a particular location, with the
 * intention of speaking only to bodies in that cluster.
 */
public class ClusterChatMessageHandler
    implements PlaceManager.MessageHandler
{
    /**
     * Handles {@link SpotCodes#CLUSTER_SPEAK_REQUEST} messages.
     */
    public void handleEvent (MessageEvent event, PlaceManager pmgr)
    {
        // presently we do no ratification of chat messages, so we just
        // generate a chat notification with the message and name of the
        // speaker
        int soid = event.getSourceOid();
        BodyObject source = (BodyObject)WhirledServer.omgr.getObject(soid);
        if (source == null) {
            Log.info("Chatter went away. Dropping cluster chat request " +
                     "[req=" + event + "].");
            return;
        }

        // parse our incoming arguments
        Object[] inargs = event.getArgs();
        int reqid = ((Integer)inargs[0]).intValue();
        int locid = ((Integer)inargs[1]).intValue();
        String message = (String)inargs[2];

        // pass this request on to the spot scene manager as it will need
        // to check that the location exists and that the requester
        // occupies it and so on
        ((SpotSceneManager)pmgr).handleClusterChatRequest(
            source, locid, message);
    }
}
