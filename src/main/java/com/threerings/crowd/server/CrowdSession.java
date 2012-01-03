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

import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.chat.server.ChatHistory;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;

/**
 * Extends the presents session with crowd-specific session handling.
 */
public class CrowdSession extends PresentsSession
{
    @Override
    protected void sessionConnectionClosed ()
    {
        super.sessionConnectionClosed();

        if (_clobj != null) {
            // note that the user is disconnected
            BodyObject bobj = _locator.forClient(_clobj);
            if (bobj != null) {
                _bodyman.updateOccupantStatus(bobj, OccupantInfo.DISCONNECTED);
            }
        }
    }

    @Override
    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // note that the user's active once more
        BodyObject bobj = _locator.forClient(_clobj);
        if (bobj != null) {
            _bodyman.updateOccupantStatus(bobj, OccupantInfo.ACTIVE);
        }
    }

    @Override
    protected void sessionDidEnd ()
    {
        super.sessionDidEnd();

        BodyObject body = _locator.forClient(_clobj);

        // if we no longer have a body, there's nothing more to do
        if (body == null) {
            return;
        }

        // clear out our location so that anyone listening will know that we've left
        clearLocation(body);

        // reset our status in case this object remains around until they start their next session
        // (which could happen very soon)
        _bodyman.updateOccupantStatus(body, OccupantInfo.ACTIVE);

        // clear our chat history
        _chatHistory.clear(body.getVisibleName());
    }

    /**
     * When the user ends their session, this method is called to clear out any location they might
     * occupy. The default implementation takes care of standard crowd location occupancy, but
     * users of other services may which to override this method and clear the user out of a scene,
     * zone or other location-derived occupancy.
     */
    protected void clearLocation (BodyObject bobj)
    {
        _locman.leaveOccupiedPlace(bobj);
    }

    @Inject protected BodyLocator _locator;
    @Inject protected BodyManager _bodyman;
    @Inject protected LocationManager _locman;
    @Inject protected ChatHistory _chatHistory;
}
