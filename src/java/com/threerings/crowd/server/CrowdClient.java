//
// $Id: CrowdClient.java,v 1.13 2002/10/30 00:42:37 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;

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
            updateOccupantStatus(bobj, OccupantInfo.DISCONNECTED);
        }
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // note that the user's active once more
        BodyObject bobj = (BodyObject)_clobj;
        updateOccupantStatus(bobj, OccupantInfo.ACTIVE);
    }

    /**
     * Updates the connection status for the given body object's occupant
     * info in the specified location.
     */
    protected void updateOccupantStatus (BodyObject body, byte status)
    {
        // no need to NOOP
        if (body.status == status) {
            return;
        }

        // update the status in their body object
        body.setStatus(status);
        body.statusTime = System.currentTimeMillis();

        // get the place object for the location occupied by the user
        PlaceObject plobj = (PlaceObject)
            CrowdServer.omgr.getObject(body.location);
        if (plobj == null) {
            return;
        }

        // update the occupant info with the new connection status
        OccupantInfo info = (OccupantInfo)
            plobj.occupantInfo.get(new Integer(body.getOid()));
        if (info != null) {
            info.status = status;
            plobj.updateOccupantInfo(info);
        }
    }
}
