//
// $Id: SpotSceneDirector.java,v 1.1 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.client;

import java.util.Iterator;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.client.DisplayScene;
import com.threerings.whirled.client.DisplaySceneFactory;
import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.data.Location;

/**
 * Extends the standard scene director with facilities to move between
 * locations within a scene.
 */
public class SpotSceneDirector extends SceneDirector
    implements SpotCodes
{
    /**
     * Creates a new spot scene director with the specified context.
     *
     * @param ctx the active client context.
     * @param screp the entity from which the scene director will load
     * scene data from the local client scene storage.
     * @param dsfact the factory that knows which derivation of {@link
     * DisplayScene} to create for the current system.
     */
    public SpotSceneDirector (
        WhirledContext ctx, SceneRepository screp, DisplaySceneFactory dsfact)
    {
        super(ctx, screp, dsfact);
    }

    /**
     * Issues a request to change our location within the scene to the
     * location identified by the specified id.
     */
    public void changeLocation (int locationId)
    {
        // refuse if there's a pending location change
        if (_pendingLocId != -1) {
            return;
        }

        // make sure we're currently in a scene
        if (_sceneId == -1) {
            Log.warning("Requested to change locations, but we're not " +
                        "currently in any scene [locId=" + locationId + "].");
            return;
        }

        // make sure the specified location is in the current scene
        int locidx = -1;
        DisplaySpotScene sscene = (DisplaySpotScene)_scene;
        Iterator locs = sscene.getLocations().iterator();
        for (int i = 0; locs.hasNext(); i++) {
            Location loc = (Location)locs.next();
            if (loc.locationId == locationId) {
                locidx = i;
                break;
            }
        }
        if (locidx == -1) {
            Log.warning("Requested to change to a location that's not " +
                        "in the current scene [locs=" + StringUtil.toString(
                            sscene.getLocations().iterator()) +
                        ", locId=" + locationId + "].");
            return;
        }

        // make a note that we're changing to this location
        _pendingLocId = locationId;
        // and send the location change request
        SpotService.changeLoc(_ctx.getClient(), _sceneId, locationId, this);
    }

    /**
     * Called in response to a successful <code>changeLoc</code> request.
     */
    public void handleChangeLocSucceeded (int invid)
    {
    }

    /**
     * Called in response to a failed <code>changeLoc</code> request.
     */
    public void handleChangeLocFailed (int invid, String reason)
    {
    }

    /** The location id on which we have an outstanding change location
     * request. */
    protected int _pendingLocId = -1;
}
