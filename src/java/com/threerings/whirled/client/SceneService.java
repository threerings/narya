//
// $Id: SceneService.java,v 1.11 2004/08/27 02:20:40 mdb Exp $
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

package com.threerings.whirled.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * The scene service class provides the client interface to the scene
 * related invocation services (e.g. moving from scene to scene).
 */
public interface SceneService extends InvocationService
{
    /**
     * Used to communicate the response to a {@link #moveTo} request.
     */
    public static interface SceneMoveListener extends InvocationListener
    {
        /**
         * Indicates that a move succeeded.
         *
         * @param placeId the place object id of the newly occupied scene.
         * @param config metadata related to the newly occupied scene.
         */
        public void moveSucceeded (int placeId, PlaceConfig config);

        /**
         * Indicates that a move succeeded and that the client's cached
         * scene information should be updated with the supplied data.
         *
         * @param placeId the place object id of the newly occupied scene.
         * @param config metadata related to the newly occupied scene.
         * @param updates updates that must be applied to the client's
         * copy of a scene model to bring it up to date.
         */
        public void moveSucceededWithUpdates (int placeId, PlaceConfig config,
                                              SceneUpdate[] updates);

        /**
         * Indicates that a move succeeded and that the client's cached
         * scene information should be updated with the supplied data.
         *
         * @param placeId the place object id of the newly occupied scene.
         * @param config metadata related to the newly occupied scene.
         * @param model a fresh copy of the most recent scene data for the
         * newly occupied scene.
         */
        public void moveSucceededWithScene (int placeId, PlaceConfig config,
                                            SceneModel model);
    }

    /**
     * Requests that that this client's body be moved to the specified
     * scene.
     *
     * @param sceneId the scene id to which we want to move.
     * @param version the version number of the scene object that we have
     * in our local repository.
     */
    public void moveTo (Client client, int sceneId, int version,
                        SceneMoveListener listener);
}
