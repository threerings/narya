//
// $Id: BodyProvider.java,v 1.3 2002/11/06 04:12:39 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

/**
 * Provides the server-side side of the body-related invocation services.
 */
public class BodyProvider
    implements InvocationProvider
{
    /**
     * Constructs and initializes a body provider instance which will be
     * used to handle all body-related invocation service requests.
     */
    public static void init (InvocationManager invmgr)
    {
        // register a provider instance
        invmgr.registerDispatcher(new BodyDispatcher(new BodyProvider()), true);
    }

    /**
     * Handles a request to set the idle state of a client to the
     * specified value.
     */
    public void setIdle (ClientObject caller, boolean idle)
        throws InvocationException
    {
        BodyObject bobj = (BodyObject)caller;

        // determine the body's proposed new status
        byte nstatus = (idle) ? OccupantInfo.IDLE : OccupantInfo.ACTIVE;

        // report NOOP attempts
        if (bobj.status == nstatus) {
            throw new InvocationException(
                (idle) ? "m.already_idle" : "m.already_active");
        }

        // update their status!
        Log.info("Setting user idle state [user=" + bobj.username +
                 ", status=" + nstatus + "].");
        updateOccupantStatus(bobj, bobj.location, nstatus);
    }

    /**
     * Updates the connection status for the given body object's occupant
     * info in the specified location.
     */
    public static void updateOccupantStatus (
        BodyObject body, int locationId, byte status)
    {
        // no need to NOOP
        if (body.status == status) {
            // update the status in their body object
            body.setStatus(status);
            body.statusTime = System.currentTimeMillis();
        }

        // get the place manager for the specified location
        PlaceManager pmgr = CrowdServer.plreg.getPlaceManager(locationId);
        if (pmgr == null) {
            return;
        }

        // get the place object for the specified location (which is, in
        // theory, occupied by this user)
        PlaceObject plobj = pmgr.getPlaceObject();
        if (plobj == null) {
            return;
        }

        // update the occupant info with the new connection status
        OccupantInfo info = (OccupantInfo)
            plobj.occupantInfo.get(new Integer(body.getOid()));
        if (info != null && info.status != status) {
            info.status = status;
            pmgr.updateOccupantInfo(info);
        }
    }
}
