//
// $Id: SpotService.java,v 1.1 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.whirled.spot.Log;

/**
 * Provides a mechanism by which the client can request to move between
 * locations within a scene. These services should not be used directly,
 * but instead should be accessed via the {@link SpotSceneDirector}.
 */
public class SpotService implements SpotCodes
{
    /**
     * Requests that that this client's body be made to occupy the
     * specified location.
     */
    public static void changeLoc (Client client, int sceneId, int locationId,
                                  SpotSceneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(sceneId),
                                       new Integer(locationId) };
        invdir.invoke(MODULE_NAME, CHANGE_LOC_REQUEST, args, rsptarget);
        Log.info("Sent changeLoc request [sceneId=" + sceneId +
                 ", locId=" + locationId + "].");
    }
}
