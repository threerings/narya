//
// $Id: SpotProvider.java,v 1.1 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.server;

import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.client.SpotCodes;

/**
 * This class provides the server side of the spot services.
 */
public class SpotProvider extends InvocationProvider
    implements SpotCodes
{
    /**
     * Constructs a spot provider and registers it with the invocation
     * manager to handle spot services. This need be done by a server that
     * wishes to make use of the Spot services.
     */
    public static void init (
        InvocationManager invmgr, SceneRegistry screg)
    {
        // we'll need this later
        _screg = screg;

        // register a spot provider instance
        invmgr.registerProvider(MODULE_NAME, new SpotProvider());
    }

    /**
     * Processes a request from a client to move to a new location.
     */
    public void handleChangeLocRequest (
        BodyObject source, int invid, int sceneId, int locationId)
    {
        try {
            // look up the scene manager for the specified scene
            SpotSceneManager smgr = (SpotSceneManager)
                _screg.getSceneManager(sceneId);
            if (smgr == null) {
                Log.warning("User requested to change location in " +
                            "non-existent scene [user=" + source.username +
                            ", sceneId=" + sceneId +
                            ", locId=" + locationId + "].");
                throw new ServiceFailedException(INTERNAL_ERROR);
            }

            smgr.handleChangeLocRequest(source, locationId);
            sendResponse(source, invid, CHANGE_LOC_SUCCEEDED_RESPONSE);

        } catch (ServiceFailedException sfe) {
            sendResponse(source, invid, CHANGE_LOC_FAILED_RESPONSE,
                         sfe.getMessage());
        }
    }

    /** The scene registry with which we interoperate. */
    protected static SceneRegistry _screg;
}
