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

package com.threerings.whirled.spot.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.spot.data.Location;

/**
 * Defines the mechanism by which the client can request to move around
 * within a scene and between scenes (taking exit and entry locations into
 * account). These services should not be used directly, but instead
 * should be accessed via the {@link SpotSceneDirector}.
 */
public interface SpotService extends InvocationService
{
    /**
     * Requests to traverse the specified portal.
     *
     * @param sceneId the player's current scene which is used to sanity
     * check things when the request actually arrives.
     * @param portalId the portal to be traversed.
     * @param destSceneVer the version of the destination scene data that
     * the client has in its local repository.
     */
    public void traversePortal (
        Client client, int sceneId, int portalId, int destSceneVer,
        SceneMoveListener listener);

    /**
     * Requests that this client's body be made to move to the specified
     * location. The user will be removed from any cluster from which they
     * are an occupant.
     *
     * @param sceneId the id of the scene in which to change location.
     * @param loc the location to which to move.
     */
    public void changeLocation (Client client, int sceneId, Location loc,
                                ConfirmListener listener);

    /**
     * Requests that this client start or join the specified cluster. They
     * will be relocated appropriately by the scene manager.
     *
     * @param friendOid the bodyOid of another user or the oid of an
     * existing cluster; the calling user will be made to join the cluster
     * or target user's cluster, or create a cluster with the target user
     * if they are not already in one.
     */
    public void joinCluster (Client client, int friendOid,
                             ConfirmListener listener);

    /**
     * Requests that the supplied message be delivered to listeners in the
     * cluster to which the specified location belongs.
     *
     * @param message the text of the message to be spoken.
     * @param mode an associated mode constant that can be used to
     * identify different kinds of "speech" (emote, thought bubble, etc.).
     */
    public void clusterSpeak (Client client, String message, byte mode);
}
