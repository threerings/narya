//
// $Id: CrowdClient.java,v 1.14 2002/10/30 00:47:19 mdb Exp $

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
            updateOccupantStatus(
                bobj, bobj.location, OccupantInfo.DISCONNECTED);
        }
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // note that the user's active once more
        BodyObject bobj = (BodyObject)_clobj;
        updateOccupantStatus(bobj, bobj.location, OccupantInfo.ACTIVE);
    }

    /**
     * Updates the connection status for the given body object's occupant
     * info in the specified location.
     */
    protected void updateOccupantStatus (
        BodyObject body, int locationId, byte status)
    {
        // no need to NOOP
        if (body.status == status) {
            return;
        }

        // update the status in their body object
        body.setStatus(status);
        body.statusTime = System.currentTimeMillis();

        // get the place object for the specified location (which is, in
        // theory, occupied by this user)
        PlaceObject plobj = (PlaceObject)
            CrowdServer.omgr.getObject(locationId);
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
