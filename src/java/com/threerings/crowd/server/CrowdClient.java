//
// $Id: CrowdClient.java,v 1.15 2002/11/01 00:39:18 shaper Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

/**
 * The crowd client extends the presents client with crowd-specific client
 * handling.
 */
public class CrowdClient extends PresentsClient
{
    // documentation inherited
    protected void wasUnmapped ()
    {
        super.wasUnmapped();

        if (_clobj != null) {
            // note that the user's disconnected
            BodyObject bobj = (BodyObject)_clobj;
            BodyProvider.updateOccupantStatus(
                bobj, bobj.location, OccupantInfo.DISCONNECTED);
        }
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // note that the user's active once more
        BodyObject bobj = (BodyObject)_clobj;
        BodyProvider.updateOccupantStatus(
            bobj, bobj.location, OccupantInfo.ACTIVE);
    }
}
