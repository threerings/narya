//
// $Id: SpotSceneManager.java,v 1.17 2002/08/20 18:21:45 mdb Exp $

package com.threerings.whirled.spot.server;

import com.samskivert.util.IntIntMap;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.chat.SpeakProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.whirled.server.SceneManager;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotOccupantInfo;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.util.SpotSceneUtil;

/**
 * Handles the movement of bodies between locations in the scene and
 * creates the necessary distributed objects to allow bodies in clusters
 * to chat with one another.
 */
public class SpotSceneManager extends SceneManager
    implements SpotCodes
{
    /**
     * Prepares a mapping for an entering body, indicating that they will
     * be entering at the specified location id. When the time comes to
     * prepare that body's occupant info, we will use the supplied
     * location id as their starting location.
     */
    public void mapEnteringBody (int bodyOid, int locationId)
    {
        _entering.put(bodyOid, locationId);
    }

    /**
     * If scene entry fails, this can be called to undo a scene entry
     * mapping.
     */
    public void clearEnteringBody (int bodyOid)
    {
        _entering.remove(bodyOid);
    }

    /**
     * Returns the locationId of an unoccupied location in this scene
     * (portals are not included when selecting). If no locations are
     * unoccupied, this method returns -1. This will mark the location as
     * pending so that subsequent calls to
     * <code>getUnoccupiedLocation()</code> do not return the previously
     * returned location as unoccupied, giving the caller a chance to
     * actually occupy the location. However, if another user moves to
     * that location bewteen the call to this method and the caller's own
     * request to move to the location, the caller's move request will
     * fail.
     */
    public int getUnoccupiedLocation (boolean preferClusters)
    {
        int locId = SpotSceneUtil.getUnoccupiedLocation(
            _sscene.getModel(), _locationOccs, preferClusters);
        if (locId != -1) {
            // mark the location as pending
//             Log.info("Earmarked location [scene=" + where() +
//                      ", locId=" + locId + "].");
            _locationOccs[_sscene.getLocationIndex(locId)] = -1;
        }
        return locId;
    }

    /**
     * Returns true if the location in question is occupied by another
     * body (or reserved for someone who's on their way).
     */
    public boolean isLocationOccupied (int locId)
    {
        return (_locationOccs[_sscene.getLocationIndex(locId)] != 0);
    }

    // documentation inherited
    protected void gotSceneData ()
    {
        // keep a casted reference around to our scene
        _sscene = (RuntimeSpotScene)_scene;

        // now that we have our scene, we create chat objects for each of
        // the clusters in the scene
        _clusterObjs = new DObject[_sscene.getClusterCount()];

        // create a subscriber that will grab the oids when we hear back
        // about object creation
        Subscriber sub = new Subscriber() {
            public void objectAvailable (DObject object) {
                _clusterObjs[_index++] = object;
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                Log.warning("Failed to create cluster object " +
                            "[where=" + where() + ", cluster=" + _index +
                            ", oid=" + oid + ", cause=" + cause + "].");
                // skip to the next cluster in case others didn't fail
                _index++;
            }

            protected int _index = 0;
        };

        // now issue the object creation requests
        for (int i = 0; i < _clusterObjs.length; i++) {
            _omgr.createObject(DObject.class, sub);
        }

        // create an array in which to track the occupants of each
        // location
        _locationOccs = new int[_sscene.getLocationCount()];

        _isPortal = new boolean[_sscene.getLocationCount()];
        SpotSceneModel model = _sscene.getModel();
        for (int ii=0; ii < model.locationIds.length; ii++) {
            _isPortal[ii] = IntListUtil.contains(model.portalIds,
                                                 model.locationIds[ii]);
        }
    }

    /**
     * When a user is entering a scene, we populate their occupant info
     * with the location prepared by the portal traversal code or with the
     * default entrance for the scene if no preparation was done.
     */
    protected void populateOccupantInfo (OccupantInfo info, BodyObject body)
    {
        super.populateOccupantInfo(info, body);

        // we have a table for tracking the locations of entering bodies
        // which is populated by the portal traversal code when a body
        // requests to enter our scene. if there's a mapped entrance
        // location for this body, use it, otherwise assume they're coming
        // in at the default entrance
        int entryLocId = _entering.remove(body.getOid());
        if (entryLocId == -1) {
            entryLocId = _sscene.getDefaultEntranceId();
        }
        ((SpotOccupantInfo)info).locationId = entryLocId;
    }

    /**
     * Called by the {@link SpotProvider} when we receive a request by a
     * user to occupy a particular location.
     *
     * @return the oid of the chat object associated with the cluster to
     * which this location belongs or -1 if the location is not part of a
     * cluster.
     *
     * @exception InvocationException thrown with a reason code explaining
     * the location change failure if there is a problem processing the
     * location change request.
     */
    protected int handleChangeLocRequest (BodyObject source, int locationId)
        throws InvocationException
    {
        // make sure no one is already in the requested location
        int locidx = _sscene.getLocationIndex(locationId);
        if (locidx == -1) {
            Log.warning("Ignoring request to move to non-existent location " +
                        "[where=" + where() + ", user=" + source.who() +
                        ", locId=" + locationId + "].");
            throw new InvocationException(LOCATION_OCCUPIED);

        } else if (_locationOccs[locidx] > 0) {
            Log.info("Ignoring request to move to occupied location " +
                     "[where=" + where() + ", user=" + source.who() +
                     ", locId=" + locationId +
                     ", occupantOid=" + _locationOccs[locidx] + "].");
            throw new InvocationException(LOCATION_OCCUPIED);
        }

        // make sure they have an occupant info object in the place
        int bodyOid = source.getOid();
        SpotOccupantInfo soi = (SpotOccupantInfo)
            _plobj.occupantInfo.get(new Integer(bodyOid));
        if (soi == null) {
            Log.warning("Aiya! Can't update non-existent occupant info " +
                        "with new location [where=" + where() +
                        ", body=" + source.who() + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // clear out any location they previously occupied
        if (soi.locationId > 0) {
            int oldlocidx = _sscene.getLocationIndex(soi.locationId);
            if (oldlocidx == -1) {
                Log.warning("Changing location for body that was " +
                            "previously in an invalid location " +
                            "[where=" + where() + ", info=" + soi + "].");
            } else {
                _locationOccs[oldlocidx] = 0;
            }
        }

        // stick our new friend into that location, if it's not a portal
        if (!_isPortal[locidx]) {
            _locationOccs[locidx] = bodyOid;
        }

        // update a clone of their occupant info, we need to clone because
        // the original could still be in the queue as part of another
        // event going out to the clients.
        soi = (SpotOccupantInfo) soi.clone();
        soi.locationId = locationId;
        // and broadcast the update to the place
        _plobj.updateOccupantInfo(soi);

        // figure out the cluster chat oid
        int clusterIdx = _sscene.getClusterIndex(locidx);
        return (clusterIdx == -1) ? -1 : _clusterObjs[clusterIdx].getOid();
    }

    /**
     * Called by the {@link SpotProvider} when we receive a cluster speak
     * request.
     */
    protected void handleClusterSpeakRequest (
        int sourceOid, String source, int locationId,
        String bundle, String message, byte mode)
    {
        // make sure this user occupies the specified location
        int locidx = _sscene.getLocationIndex(locationId);
        if (locidx == -1 || _locationOccs[locidx] != sourceOid) {
            Log.warning("User not in specified location for CCREQ " +
                        "[where=" + where() + ", chatter=" + source +
                        " (" + sourceOid + "), locId=" + locationId +
                        ", locidx=" + locidx + ", message=" + message + "].");
            return;
        }

        // make sure there's a cluster associated with this location
        int clusterIndex = _sscene.getClusterIndex(locidx);
        if (clusterIndex == -1) {
            Log.warning("User in clusterless location sent CCREQ " +
                        "[where=" + where() + ", chatter=" + source +
                        " (" + sourceOid + "), locId=" + locationId +
                        ", message=" + message + "].");
            return;
        }

        // all is well, generate a chat notification
        DObject clusterObj = _clusterObjs[clusterIndex];
        if (clusterObj != null) {
            SpeakProvider.sendSpeak(clusterObj, source, bundle, message, mode);

        } else {
            Log.warning("Have no cluster object for CCREQ " +
                        "[where=" + where() + ", cidx=" + clusterIndex +
                        ", chatter=" + source + " (" + sourceOid +
                        "), message=" + message + "].");
        }
    }

    /**
     * @return true if the specified user is in the scene and
     * in a valid location (not on a portal).
     */
    protected boolean inValidLocation (BodyObject body)
    {
        int bodyOid = body.getOid();
        for (int ii=0; ii < _locationOccs.length; ii++) {
            if (_locationOccs[ii] == bodyOid) {
                return true;
            }
        }
        return false;
    }

    /**
     * When an occupant leaves the room, we want to clear out any location
     * they may have occupied.
     */
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        for (int i = 0; i < _locationOccs.length; i++) {
            if (_locationOccs[i] == bodyOid) {
                _locationOccs[i] = 0;
                break;
            }
        }
    }

    /**
     * We need our own extended occupant info to keep track of what
     * location each occupant occupies.
     */
    protected Class getOccupantInfoClass ()
    {
        return SpotOccupantInfo.class;
    }

    /** A casted reference to our runtime scene instance. */
    protected RuntimeSpotScene _sscene;

    /** Our cluster chat objects. */
    protected DObject[] _clusterObjs;

    /** Oids of the bodies that occupy each of our locations. */
    protected int[] _locationOccs;

    /** Tracks if each location is a portal. */
    protected boolean[] _isPortal;

    /** A table of mappings from body oids to entry location ids for
     * bodies that are entering our scene. */
    protected IntIntMap _entering = new IntIntMap();
}
