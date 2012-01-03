//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyMarshaller;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.OccupantInfo;

import static com.threerings.crowd.Log.log;

/**
 * Handles body related services.
 */
@Singleton
public class BodyManager
    implements BodyProvider
{
    /**
     * Constructs and initializes the body manager.
     */
    @Inject public BodyManager (InvocationManager invmgr)
    {
        invmgr.registerProvider(this, BodyMarshaller.class, CrowdCodes.CROWD_GROUP);
    }

    /**
     * Locates the specified body's occupant info in the specified location, applies the supplied
     * occupant info operation to it and then broadcasts the updated info (assuming the occop
     * returned true indicating that an update was made).
     */
    public <T extends OccupantInfo> boolean updateOccupantInfo (
        BodyObject body, OccupantInfo.Updater<T> updater)
    {
        PlaceManager pmgr = _plreg.getPlaceManager(body.getPlaceOid());
        return (pmgr == null) ? false : pmgr.updateOccupantInfo(body.getOid(), updater);
    }

    /**
     * Updates the connection status for the given body object's occupant info in the specified
     * location.
     */
    public void updateOccupantStatus (BodyObject body, final byte status)
    {
        // no need to NOOP
        if (body.status != status) {
            // update the status in their body object
            body.setStatus(status);
            body.getLocal(BodyLocal.class).statusTime = System.currentTimeMillis();
        }

        updateOccupantInfo(body, new OccupantInfo.Updater<OccupantInfo>() {
            public boolean update (OccupantInfo info) {
                if (info.status == status) {
                    return false;
                }
                info.status = status;
                return true;
            }
        });
    }

    // from interface BodyProvider
    public void setIdle (ClientObject caller, boolean idle)
    {
        BodyObject bobj = _locator.forClient(caller);

        // determine the body's proposed new status
        byte nstatus = (idle) ? OccupantInfo.IDLE : OccupantInfo.ACTIVE;
        if (bobj == null || bobj.status == nstatus) {
            return; // ignore NOOP attempts
        }

        // update their status!
        log.debug("Setting user idle state", "user", bobj.username, "status", nstatus);
        updateOccupantStatus(bobj, nstatus);
    }

    /** Provides access to place managers. */
    @Inject protected PlaceRegistry _plreg;
    @Inject protected BodyLocator _locator;
}
