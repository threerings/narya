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

package com.threerings.whirled.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.data.ScenedBodyObject;

/**
 * The scene provider handles the server side of the scene related
 * invocation services (e.g. moving from scene to scene).
 */
public class SceneProvider
    implements InvocationProvider, SceneCodes
{
    /**
     * Constructs a scene provider that will interact with the supplied
     * scene registry.
     */
    public SceneProvider (LocationProvider locprov, SceneRegistry screg)
    {
        _locprov = locprov;
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a new scene.
     */
    public void moveTo (ClientObject caller, int sceneId,
                        final int sceneVer, final SceneMoveListener listener)
    {
        final BodyObject source = (BodyObject)caller;

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl = null;
        rl = new SceneRegistry.ResolutionListener() {
            public void sceneWasResolved (SceneManager scmgr) {
                // make sure our caller is still around; under heavy load,
                // clients might end their session while the scene is
                // resolving
                if (!source.isActive()) {
                    Log.info("Abandoning scene move, client gone " +
                             "[who=" + source.who()  +
                             ", dest=" + scmgr.where() + "].");
                    InvocationMarshaller.setNoResponse(listener);
                    return;
                }
                finishMoveToRequest(source, scmgr, sceneVer, listener);
            }

            public void sceneFailedToResolve (int rsceneId, Exception reason) {
                Log.warning("Unable to resolve scene [sceneid=" + rsceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                listener.requestFailed(NO_SUCH_PLACE);
            }
        };

        // make sure the scene they are headed to is actually loaded into
        // the server
        _screg.resolveScene(sceneId, rl);
    }

    /**
     * This is called after the scene to which we are moving is guaranteed
     * to have been loaded into the server.
     */
    protected void finishMoveToRequest (
        BodyObject source, SceneManager scmgr, int sceneVersion,
        SceneMoveListener listener)
    {
        try {
            effectSceneMove(source, scmgr, sceneVersion, listener);

        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());

        } catch (RuntimeException re) {
            Log.logStackTrace(re);
            listener.requestFailed(INTERNAL_ERROR);
        }
    }

    /**
     * Moves the supplied body into the supplied (already resolved) scene
     * and informs the supplied listener if the move is successful.
     *
     * @exception InvocationException thrown if a failure occurs
     * attempting to move the user into the place associated with the
     * scene.
     */
    public void effectSceneMove (BodyObject source, SceneManager scmgr,
                                 int sceneVersion, SceneMoveListener listener)
        throws InvocationException
    {
        // move to the place object associated with this scene
        int ploid = scmgr.getPlaceObject().getOid();
        PlaceConfig config = _locprov.moveTo(source, ploid);

        // now that we've finally moved, we can update the user object
        // with the new scene id
        ((ScenedBodyObject)source).setSceneId(scmgr.getScene().getId());

        // check to see if they need a newer version of the scene data
        SceneModel model = scmgr.getScene().getSceneModel();
        if (sceneVersion < model.version) {
            // try getting updates
            SceneUpdate[] updates = scmgr.getUpdates(sceneVersion);
            if (updates != null) {
                listener.moveSucceededWithUpdates(ploid, config, updates);
            } else {
                listener.moveSucceededWithScene(ploid, config, model);
            }
        } else {
            listener.moveSucceeded(ploid, config);
        }
    }

    /**
     * Ejects the specified body from their current scene and sends them a
     * request to move to the specified new scene. This is the
     * scene-equivalent to {@link LocationProvider#moveBody}.
     */
    public void moveBody (BodyObject source, int sceneId)
    {
        // first remove them from their old place
        _locprov.leaveOccupiedPlace(source);

        // then send a forced move notification
        SceneSender.forcedMove(source, sceneId);
    }

    /**
     * Ejects the specified body from their current scene and zone. This
     * is the zone equivalent to {@link
     * LocationProvider#leaveOccupiedPlace}.
     */
    public void leaveOccupiedScene (ScenedBodyObject source)
    {
        // remove them from their occupied place
        _locprov.leaveOccupiedPlace((BodyObject)source);

        // and clear out their scene information
        source.setSceneId(-1);
    }

    /** The location provider we use to handle low-level location stuff. */
    protected LocationProvider _locprov;

    /** The scene registry with which we interact. */
    protected SceneRegistry _screg;
}
