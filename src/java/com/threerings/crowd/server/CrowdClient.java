//
// $Id: CrowdClient.java,v 1.21 2002/12/05 16:39:21 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.data.ClientObject;
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
    protected void sessionConnectionClosed ()
    {
        super.sessionConnectionClosed();

        if (_clobj != null) {
            // note that the user is disconnected
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

    // documentation inherited
    protected void sessionDidEnd ()
    {
        super.sessionDidEnd();

        // clear out our location so that anyone listening for such things
        // will know that we've left
        clearLocation((BodyObject)_clobj);
    }

    /**
     * When the user ends their session, this method is called to clear
     * out any location they might occupy. The default implementation
     * takes care of standard crowd location occupancy, but users of other
     * services may which to override this method and clear the user out
     * of a scene, zone or other location-derived occupancy.
     */
    protected void clearLocation (BodyObject bobj)
    {
        CrowdServer.plreg.locprov.leaveOccupiedPlace(bobj);
    }
}
