//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.Log;
import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
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
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // configure a specific access controller for the client object
        _clobj.setAccessController(CrowdObjectAccess.USER);
    }

    // documentation inherited
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        if (_clobj != null) {
            // note that the user's active once more
            BodyObject bobj = (BodyObject)_clobj;
            BodyProvider.updateOccupantStatus(
                bobj, bobj.location, OccupantInfo.ACTIVE);

        } else {
            Log.warning("Session resumed but we have no client object!? " +
                        "[client=" + this + "].");
        }
    }

    // documentation inherited
    protected void sessionDidEnd ()
    {
        super.sessionDidEnd();

        BodyObject body = (BodyObject)_clobj;

        // clear out our location so that anyone listening for such things
        // will know that we've left
        clearLocation(body);

        // reset our status in case this object remains around until they
        // start their next session (which could happen very soon)
        BodyProvider.updateOccupantStatus(body, -1, OccupantInfo.ACTIVE);

        // clear our chat history
        if (body != null) {
            SpeakProvider.clearHistory(body.getVisibleName());
        }
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
