//
// $Id: CrowdClient.java,v 1.12 2002/10/27 02:04:50 shaper Exp $

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
                bobj.getOid(), bobj.location, OccupantInfo.DISCONNECTED);
        }
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // note that the user's active once more
        BodyObject bobj = (BodyObject)_clobj;
        updateOccupantStatus(bobj.getOid(), bobj.location, OccupantInfo.ACTIVE);
    }

    /**
     * Updates the connection status for the given body object's occupant
     * info in the specified location.
     */
    protected void updateOccupantStatus (
        int bodyOid, int locationId, byte status)
    {
        // get the place object for the specified location
        PlaceObject plobj = (PlaceObject)CrowdServer.omgr.getObject(locationId);
        if (plobj != null) {
            // update the occupant info with the new connection status
            OccupantInfo info = (OccupantInfo)plobj.occupantInfo.get(
                new Integer(bodyOid));
            if (info != null && info.status != status) {
                info.status = status;
                plobj.updateOccupantInfo(info);
            }
        }
    }
}
