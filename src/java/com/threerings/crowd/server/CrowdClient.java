//
// $Id: CrowdClient.java,v 1.11 2002/10/26 02:40:30 shaper Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;

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
            BodyObject bobj = (BodyObject)_clobj;

            // get the location the user is occupying
            PlaceObject plobj = (PlaceObject)
                CrowdServer.omgr.getObject(bobj.location);
            if (plobj != null) {
                // update their occupant info with their new connection
                // status
                OccupantInfo info = (OccupantInfo)plobj.occupantInfo.get(
                    new Integer(bobj.getOid()));
                if (info != null) {
                    info.status = OccupantInfo.DISCONNECTED;
                    plobj.updateOccupantInfo(info);
                }
            }
        }
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        BodyObject bobj = (BodyObject)_clobj;

        // get the location the user is occupying
        PlaceObject plobj = (PlaceObject)
            CrowdServer.omgr.getObject(bobj.location);
        if (plobj != null) {
            // update their occupant info with their new connection status
            OccupantInfo info = (OccupantInfo)plobj.occupantInfo.get(
                new Integer(bobj.getOid()));
            if (info != null) {
                info.status = OccupantInfo.ACTIVE;
                plobj.updateOccupantInfo(info);
            }
        }
    }
}
