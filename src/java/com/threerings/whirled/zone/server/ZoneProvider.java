//
// $Id: ZoneProvider.java,v 1.19 2004/10/30 04:33:22 mdb Exp $
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

package com.threerings.whirled.zone.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.data.ScenedBodyObject;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.zone.Log;
import com.threerings.whirled.zone.client.ZoneService.ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.data.ZonedBodyObject;

/**
 * Provides zone related services which are presently the ability to move
 * from zone to zone.
 */
public class ZoneProvider
    implements ZoneCodes, InvocationProvider
{
    /**
     * Constructs a zone provider that will interoperate with the supplied
     * zone and scene registries. The zone provider will automatically be
     * constructed and registered by the {@link ZoneRegistry}, which a
     * zone-using system must create and initialize in their server.
     */
    public ZoneProvider (LocationProvider locprov, ZoneRegistry zonereg,
                         SceneRegistry screg)
    {
        _locprov = locprov;
        _zonereg = zonereg;
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a scene in a new zone.
     *
     * @param caller the user requesting the move.
     * @param zoneId the qualified zone id of the new zone.
     * @param sceneId the identifier of the new scene.
     * @param sceneVer the version of the scene model currently held by
     * the client.
     * @param listener the entity to inform of success or failure.
     */
    public void moveTo (ClientObject caller, int zoneId, int sceneId,
                        int sceneVer, ZoneMoveListener listener)
        throws InvocationException
    {
        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = (BodyObject)caller;
        final int fsceneId = sceneId;
        final int fsceneVer = sceneVer;
        final ZoneMoveListener flistener = listener;

        // look up the zone manager for the zone
        ZoneManager zmgr = _zonereg.getZoneManager(zoneId);
        if (zmgr == null) {
            Log.warning("Requested to enter a zone for which we have no " +
                        "manager [user=" + fsource.who() +
                        ", zoneId=" + zoneId + "].");
            throw new InvocationException(NO_SUCH_ZONE);
        }

        // resolve the zone!
        ZoneManager.ResolutionListener zl = new ZoneManager.ResolutionListener()
        {
            public void zoneWasResolved (ZoneSummary summary) {
                continueMoveTo(
                    fsource, summary, fsceneId, fsceneVer, flistener);
            }

            public void zoneFailedToResolve (int zoneId, Exception reason) {
                Log.warning("Unable to resolve zone [zoneId=" + zoneId +
                            ", reason=" + reason + "].");
                flistener.requestFailed(NO_SUCH_ZONE);
            }
        };
        zmgr.resolveZone(zoneId, zl);
    }

    /**
     * This is called after we have resolved our zone.
     */
    protected void continueMoveTo (
        BodyObject source, ZoneSummary summary, int sceneId, int sceneVer,
        ZoneMoveListener listener)
    {
        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = source;
        final ZoneSummary fsum = summary;
        final int fsceneVer = sceneVer;
        final ZoneMoveListener flistener = listener;

        // give the zone manager a chance to veto the request
        ZoneManager zmgr = _zonereg.getZoneManager(summary.zoneId);
        String errmsg = zmgr.ratifyBodyEntry(source, summary.zoneId);
        if (errmsg != null) {
            listener.requestFailed(errmsg);
            return;
        }

        // create a callback object that will handle the resolution or
        // failed resolution of the scene
        SceneRegistry.ResolutionListener rl = null;
        rl = new SceneRegistry.ResolutionListener() {
            public void sceneWasResolved (SceneManager scmgr) {
                // make sure our caller is still around; under heavy load,
                // clients might end their session while the scene is
                // resolving
                if (!fsource.isActive()) {
                    Log.info("Abandoning zone move, client gone " +
                             "[who=" + fsource.who()  +
                             ", dest=" + scmgr.where() + "].");
                    return;
                }
                finishMoveTo(fsource, fsum, scmgr, fsceneVer, flistener);
            }

            public void sceneFailedToResolve (int sceneId, Exception reason) {
                Log.warning("Unable to resolve scene [sceneid=" + sceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                flistener.requestFailed(NO_SUCH_PLACE);
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
    protected void finishMoveTo (
        BodyObject source, ZoneSummary summary, SceneManager scmgr,
        int sceneVersion, ZoneMoveListener listener)
    {
        // move to the place object associated with this scene
        PlaceObject plobj = scmgr.getPlaceObject();
        int ploid = plobj.getOid();

        try {
            // try doing the actual move
            PlaceConfig config = _locprov.moveTo(source, ploid);

            // now that we've finally moved, we can update the user object
            // with the new scene and zone ids
            source.startTransaction();
            try {
                ((ScenedBodyObject)source).setSceneId(scmgr.getScene().getId());
                ((ZonedBodyObject)source).setZoneId(summary.zoneId);
            } finally {
                source.commitTransaction();
            }

            // check to see if they need a newer version of the scene data
            SceneModel model = scmgr.getScene().getSceneModel();
            if (sceneVersion < model.version) {
                SceneUpdate[] updates = scmgr.getUpdates(sceneVersion);
                if (updates != null) {
                    listener.moveSucceededWithUpdates(
                        ploid, config, summary, updates);
                } else {
                    listener.moveSucceededWithScene(
                        ploid, config, summary, model);
                }

            } else {
                // then send the moveTo response
                listener.moveSucceeded(ploid, config, summary);
            }

            // let the zone manager know that someone just came on in
            ZoneManager zmgr = _zonereg.getZoneManager(summary.zoneId);
            zmgr.bodyDidEnterZone(source, summary.zoneId);

        } catch (InvocationException ie) {
            listener.requestFailed(ie.getMessage());
        }
    }

    /**
     * Ejects the specified body from their current scene and sends them a
     * request to move to the specified new zone and scene. This is the
     * zone-equivalent to {@link LocationProvider#moveBody}.
     */
    public void moveBody (ZonedBodyObject source, int zoneId, int sceneId)
    {
        if (source.getZoneId() == zoneId) {
            // handle the case of moving somewhere in the same zone
            _screg.sceneprov.moveBody((BodyObject) source, sceneId);

        } else {
            // first remove them from their old location
            leaveOccupiedZone(source);

            // then send a forced move notification
            ZoneSender.forcedMove((BodyObject)source, zoneId, sceneId);
        }
    }

    /**
     * Ejects the specified body from their current scene and zone. This
     * is the zone equivalent to {@link
     * LocationProvider#leaveOccupiedPlace}.
     */
    public void leaveOccupiedZone (ZonedBodyObject source)
    {
        // remove them from their occupied scene
        _screg.sceneprov.leaveOccupiedScene(source);

        // and clear out their zone information
        source.setZoneId(-1);
    }

    /** The entity that handles basic location changes. */
    protected LocationProvider _locprov;

    /** The zone registry with which we communicate. */
    protected ZoneRegistry _zonereg;

    /** The scene registry with which we communicate. */
    protected SceneRegistry _screg;
}
