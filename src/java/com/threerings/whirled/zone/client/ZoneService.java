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

package com.threerings.whirled.zone.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Defines the client interface to the zone related invocation services
 * (e.g. moving between zones).
 */
public interface ZoneService extends InvocationService
{
    /** Used to deliver responses to {@link #moveTo} requests. */
    public static interface ZoneMoveListener extends InvocationListener
    {
        public void moveSucceeded (
            int placeId, PlaceConfig config, ZoneSummary summary);

        public void moveSucceededWithUpdates (
            int placeId, PlaceConfig config, ZoneSummary summary,
            SceneUpdate[] updates);

        public void moveSucceededWithScene (
            int placeId, PlaceConfig config, ZoneSummary summary,
            SceneModel model);
    }

    /**
     * Requests that that this client's body be moved to the specified
     * scene in the specified zone.
     *
     * @param zoneId the zone id to which we want to move.
     * @param sceneId the scene id to which we want to move.
     * @param version the version number of the scene object that we have
     * in our local repository.
     * @param listener the object that will receive the callback when the
     * request succeeds or fails.
     */
    public void moveTo (Client client, int zoneId, int sceneId,
                        int version, ZoneMoveListener listener);
}
