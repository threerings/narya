//
// $Id: SpotSceneManager.java,v 1.25 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.server;

import java.util.Iterator;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.IntIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.chat.SpeakProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.whirled.server.SceneManager;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.data.ClusterObject;
import com.threerings.whirled.spot.data.ClusteredBodyObject;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SceneLocation;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.data.SpotSceneObject;

/**
 * Handles the movement of bodies between locations in the scene and
 * creates the necessary distributed objects to allow bodies in clusters
 * to chat with one another.
 */
public class SpotSceneManager extends SceneManager
    implements SpotCodes
{
    /**
     * Move the specified body to the default portal, if possible.
     */
    public static void moveBodyToDefaultPortal (BodyObject body)
    {
        SpotSceneManager mgr = (SpotSceneManager)
            CrowdServer.plreg.getPlaceManager(body.location);
        if (mgr != null) {
            SpotScene scene = (SpotScene)mgr.getScene();
            try {
                Location eloc = scene.getDefaultEntrance().getLocation();
                mgr.handleChangeLocRequest(body, eloc, -1);
            } catch (InvocationException ie) {
                Log.warning("Could not move user to default portal " +
                            "[where=" + mgr.where() + ", who=" + body.who() +
                            ", error=" + ie + "].");
            }
        }
    }

    /**
     * Assigns a starting location for an entering body. This will happen
     * before the body is made to "occupy" the scene (defined by their
     * having an occupant info record). So when they do finally occupy the
     * scene, the client will know where to render them.
     */
    public void mapEnteringBody (BodyObject body, int portalId)
    {
        _enterers.put(body.getOid(), portalId);
    }

    /**
     * Called if a body failed to enter our scene after we assigned them
     * an entering position.
     */
    public void clearEnteringBody (BodyObject body)
    {
        _enterers.remove(body.getOid());
    }

    // documentation inherited
    protected void didStartup ()
    {
        // get a casted reference to our place object (we need to do this
        // before calling super.didStartup() because that will call
        // sceneManagerDidResolve() which may start letting people into
        // the scene)
        _ssobj = (SpotSceneObject)_plobj;

        super.didStartup();
    }

    // documentation inherited
    protected void gotSceneData ()
    {
        super.gotSceneData();

        // keep a casted reference around to our scene
        _sscene = (SpotScene)_scene;
    }

    // documentation inherited
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // clear out their location information
        _ssobj.removeFromOccupantLocs(new Integer(bodyOid));

        // clear any cluster they may occupy
        removeFromCluster(bodyOid);
    }

    // documentation inherited
    protected void populateOccupantInfo (OccupantInfo info, BodyObject body)
    {
        super.populateOccupantInfo(info, body);

        // we don't actually populate their occupant info, but instead
        // assign them their starting location in the scene
        int portalId = _enterers.remove(body.getOid());
        Portal entry;
        if (portalId != -1) {
            entry = _sscene.getPortal(portalId);
            if (entry == null) {
                Log.warning("Body mapped at invalid portal [where=" + where() +
                            ", who=" + body.who() +
                            ", portalId=" + portalId + "].");
                entry = _sscene.getDefaultEntrance();
            }
        } else {
            entry = _sscene.getDefaultEntrance();
        }

//         Log.info("Positioning entering body [who=" + body.who() +
//                  ", where=" + entry.getOppLocation() + "].");

        // create a scene location for them located on the entrance portal
        // but facing the opposite direction
        _ssobj.addToOccupantLocs(
            new SceneLocation(entry.getOppLocation(), body.getOid()));
    }

    /**
     * Called by the {@link SpotProvider} when we receive a request by a
     * user to occupy a particular location.
     *
     * @param source the body to be moved.
     * @param loc the location to which to move the body.
     * @param cluster if zero, a new cluster will be created and assigned
     * to the moving user; if -1, the moving user will be removed from any
     * cluster they currently occupy and not made to occupy a new cluster;
     * if the bodyOid of another user, the moving user will be made to
     * join the other user's cluster.
     *
     * @exception InvocationException thrown with a reason code explaining
     * the location change failure if there is a problem processing the
     * location change request.
     */
    protected void handleChangeLocRequest (
        BodyObject source, Location loc, int cluster)
        throws InvocationException
    {
        // make sure they are in our scene
        if (!_ssobj.occupants.contains(source.getOid())) {
            Log.warning("Refusing change loc from non-scene occupant " +
                        "[where=" + where() + ", who=" + source.who() +
                        ", loc=" + loc + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // update the user's location information in the scene which will
        // indicate to the client that their avatar should be moved from
        // its current position to their new position
        SceneLocation sloc = new SceneLocation(loc, source.getOid());
        if (!_ssobj.occupantLocs.contains(sloc)) {
            // complain if they don't already have a location configured
            Log.warning("Changing loc for occupant without previous loc " +
                        "[where=" + where() + ", who=" + source.who() +
                        ", nloc=" + loc + "].");
            _ssobj.addToOccupantLocs(sloc);
        } else {
            _ssobj.updateOccupantLocs(sloc);
        }

        // handle the cluster situation
        switch (cluster) {
        case 0:
            createNewCluster(source);
            break;
        case -1:
            removeFromCluster(source.getOid());
            break;
        default:
            addToCluster(cluster, source);
            break;
        }
    }

    /**
     * Creates a new cluster with the specified user being added to said
     * cluster.
     */
    protected void createNewCluster (BodyObject creator)
    {
        // remove them from any previous cluster
        removeFromCluster(creator.getOid());

        // create a cluster record and map this user to it
        ClusterRecord clrec = new ClusterRecord();
        if (clrec.addBody(creator)) {
            _clusters.put(creator.getOid(), clrec);
        }
        // if we failed to add the creator, the cluster record will
        // quietly off itself when it's done subscribing to its object
    }

    /**
     * Adds the specified user to the cluster occupied by the specified
     * other user.
     */
    protected void addToCluster (int memberOid, BodyObject joiner)
    {
        ClusterRecord clrec = (ClusterRecord)_clusters.get(memberOid);
        if (clrec == null) {
            // this isn't completely impossible as the last user might
            // leave a cluster just as another user is entering it, but
            // before the entering user's client was informed that the
            // cluster went away
            Log.info("Can't add user to non-existent cluster; " +
                     "creating a new one just for them " +
                     "[memberOid=" + memberOid +
                     ", joiner=" + joiner.who() + "].");
            clrec = new ClusterRecord();
        }
        if (clrec.addBody(joiner)) {
            _clusters.put(joiner.getOid(), clrec);
        }
    }

    /**
     * Removes the specified user from any cluster they occupy.
     */
    protected void removeFromCluster (int bodyOid)
    {
        ClusterRecord clrec = (ClusterRecord)_clusters.remove(bodyOid);
        if (clrec != null) {
            clrec.removeBody(bodyOid);
        }
    }

    /**
     * Called by the {@link SpotProvider} when we receive a cluster speak
     * request.
     */
    protected void handleClusterSpeakRequest (
        int sourceOid, String source, String bundle, String message, byte mode)
    {
        ClusterRecord clrec = (ClusterRecord)_clusters.get(sourceOid);
        if (clrec == null) {
            Log.warning("Non-clustered user requested cluster speak " +
                        "[where=" + where() + ", chatter=" + source +
                        " (" + sourceOid + "), msg=" + message + "].");
        } else {
            SpeakProvider.sendSpeak(clrec.getClusterObject(),
                                    source, bundle, message, mode);
        }
    }

    /**
     * Used to manage clusters which are groups of users that can chat to
     * one another.
     */
    protected static class ClusterRecord extends HashIntMap
        implements Subscriber
    {
        public ClusterRecord ()
        {
            CrowdServer.omgr.createObject(ClusterObject.class, this);
        }

        public boolean addBody (BodyObject body)
        {
            if (body instanceof ClusteredBodyObject) {
                if (_clobj != null) {
                    ((ClusteredBodyObject)body).setClusterOid(_clobj.getOid());
                }
                put(body.getOid(), body);
                _clobj.addToOccupants(body.getOid());
                return true;

            } else {
                Log.warning("Refusing to add non-clustered body to cluster " +
                            "[cloid=" + _clobj.getOid() +
                            ", size=" + size() + ", who=" + body.who() + "].");
                return false;
            }
        }

        public void removeBody (int bodyOid)
        {
            ClusteredBodyObject body = (ClusteredBodyObject)remove(bodyOid);
            if (body == null) {
                Log.warning("Requested to remove unknown body from cluster " +
                            "[cloid=" + _clobj.getOid() +
                            ", size=" + size() + ", who=" + bodyOid + "].");
            } else {
                body.setClusterOid(-1);
                _clobj.removeFromOccupants(bodyOid);
            }

            // if we've removed our last body; stick a fork in ourselves
            if (size() == 0) {
                destroy();
            }
        }

        public ClusterObject getClusterObject ()
        {
            return _clobj;
        }

        public void objectAvailable (DObject object)
        {
            // keep this feller around
            _clobj = (ClusterObject)object;

            // let any mapped users know about our cluster
            Iterator iter = values().iterator();
            while (iter.hasNext()) {
                ClusteredBodyObject body = (ClusteredBodyObject)iter.next();
                body.setClusterOid(_clobj.getOid());
            }

            // if we didn't manage to add our creating user when we first
            // started up, there's no point in our sticking around
            if (size() == 0) {
                destroy();
            }
        }

        public void requestFailed (int oid, ObjectAccessException cause)
        {
            Log.warning("Aiya! Failed to create cluster object " +
                        "[cause=" + cause + ", penders=" + size() + "].");

            // let any mapped users know that we're hosed
            Iterator iter = values().iterator();
            while (iter.hasNext()) {
                ClusteredBodyObject body = (ClusteredBodyObject)iter.next();
                body.setClusterOid(-1);
            }
        }

        protected void destroy ()
        {
            Log.info("Cluster empty, going away " +
                     "[cloid=" + _clobj.getOid() + "].");
            CrowdServer.omgr.destroyObject(_clobj.getOid());
        }

        protected ClusterObject _clobj;
    }

    /** A casted reference to our place object. */
    protected SpotSceneObject _ssobj;

    /** A casted reference to our scene instance. */
    protected SpotScene _sscene;

    /** Records with information on all clusters in this scene. */
    protected HashIntMap _clusters = new HashIntMap();

    /** A mapping of entering bodies to portal ids. */
    protected IntIntMap _enterers = new IntIntMap();
}
