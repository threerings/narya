//
// $Id: SpotSceneManager.java,v 1.44 2003/09/22 23:42:49 mdb Exp $

package com.threerings.whirled.spot.server;

import java.awt.Point;
import java.util.Iterator;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.IntIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.whirled.server.SceneManager;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.data.Cluster;
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
                mgr.handleChangeLoc(body, eloc);
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

    /**
     * This is called when a user requests to traverse a portal from this
     * scene to another scene. The manager may return an error code string
     * that will be reported back to the caller explaining the failure or
     * <code>null</code> indicating that it is OK for the caller to
     * traverse the portal.
     */
    public String mayTraversePortal (BodyObject body, Portal portal)
    {
        return null;
    }

    /**
     * This is called to let this scene manager know that the user is
     * about to traverse the specified portal. The default implementation
     * relocates the user to the location associated with the portal. It
     * is still possible that the traversal will fail, so don't do
     * anything too crazy.
     */
    public void willTraversePortal (BodyObject body, Portal portal)
    {
        updateLocation(body, portal.getLocation());
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

        // let's make damned sure they're not in any cluster
        Iterator cliter = _clusters.values().iterator();
        while (cliter.hasNext()) {
            ClusterRecord clrec = (ClusterRecord)cliter.next();
            if (clrec.containsKey(bodyOid)) {
                Log.info("Pruning departed body from cluster [boid=" + bodyOid +
                         ", cluster=" + clrec + "].");
                clrec.removeBody(bodyOid);
            }
        }
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

//         Log.debug("Positioning entering body [who=" + body.who() +
//                   ", where=" + entry.getOppLocation() + "].");

        // create a scene location for them located on the entrance portal
        // but facing the opposite direction
        _ssobj.addToOccupantLocs(computeEnteringLocation(body, entry));
    }

    /**
     * Called when the supplied body is entering our scene via the
     * specified portal. The default location is the one associated with
     * the portal, but derived classes may wish to adjust this.
     */
    protected SceneLocation computeEnteringLocation (
        BodyObject body, Portal entry)
    {
        return new SceneLocation(entry.getOppLocation(), body.getOid());
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
     * the failure if there is a problem processing the request.
     */
    protected void handleChangeLoc (BodyObject source, Location loc)
        throws InvocationException
    {
        // make sure they are in our scene
        if (!_ssobj.occupants.contains(source.getOid())) {
            Log.warning("Refusing change loc from non-scene occupant " +
                        "[where=" + where() + ", who=" + source.who() +
                        ", loc=" + loc + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // let our derived classes decide if this is an OK place to stand
        if (!validateLocation(source, loc)) {
            throw new InvocationException(INVALID_LOCATION);
        }

        // update the user's location information in the scene which will
        // indicate to the client that their avatar should be moved from
        // its current position to their new position
        updateLocation(source, loc);

        // remove them from any cluster as they've departed
        removeFromCluster(source.getOid());
    }

    /**
     * Derived classes can override this method and validate that the
     * specified body can stand in the requested location. The default
     * implementation returns <code>true</code> in all circumstances;
     * stand where ye may!
     */
    protected boolean validateLocation (BodyObject source, Location loc)
    {
        return true;
    }

    /**
     * Updates the location of the specified body.
     */
    protected void updateLocation (BodyObject source, Location loc)
    {
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
    }

    /**
     * Called by the {@link SpotProvider} when we receive a request by a
     * user to join a particular cluster.
     *
     * @param joiner the body to be moved.
     * @param targetOid the bodyOid of another user or the oid of an
     * existing cluster; the moving user will be made to join the other
     * user's cluster.
     *
     * @exception InvocationException thrown with a reason code explaining
     * the failure if there is a problem processing the request.
     */
    protected void handleJoinCluster (BodyObject joiner, int targetOid)
        throws InvocationException
    {
        // if the cluster already exists, add this user and be done
        ClusterRecord clrec = (ClusterRecord)_clusters.get(targetOid);
        if (clrec != null) {
            clrec.addBody(joiner);
            return;
        }

        // otherwise see if they sent us the user's oid
        DObject tobj = CrowdServer.omgr.getObject(targetOid);
        if (!(tobj instanceof BodyObject)) {
            Log.warning("Can't join cluster, missing target " +
                        "[creator=" + joiner.who() +
                        ", targetOid=" + targetOid + "].");
            throw new InvocationException(NO_SUCH_CLUSTER);
        }

        // make sure we're in the same scene as said user
        BodyObject friend = (BodyObject)tobj;
        if (friend.location != joiner.location) {
            throw new InvocationException(NO_SUCH_CLUSTER);
        }

        // see if the friend is already in a cluster
        clrec = getCluster(friend.getOid());
        if (clrec != null) {
            clrec.addBody(joiner);
            return;
        }

        // confirm that they can start a cluster with this unsuspecting
        // other person
        checkCanCluster(joiner, friend);

        // otherwise we create a new cluster and add our charter members!
//         Log.debug("Creating cluster [starter=" + joiner.who() +
//                   ", target=" + friend.who() + "].");
        clrec = new ClusterRecord();
        clrec.addBody(friend);
        clrec.addBody(joiner);
    }

    /**
     * Gives derived classes an opportunity to veto a user's attempt to
     * start a cluster with another user. If the attempt should be vetoed,
     * this method should throw an {@link InvocationException} indicating
     * the reason for veto.
     */
    protected void checkCanCluster (BodyObject initiator, BodyObject target)
        throws InvocationException
    {
        // nothing to do by default
    }

    /**
     * Removes the specified user from any cluster they occupy.
     */
    protected void removeFromCluster (int bodyOid)
    {
        ClusterRecord clrec = getCluster(bodyOid);
        if (clrec != null) {
            clrec.removeBody(bodyOid);
        }
    }

    /**
     * Fetches the cluster record for the specified body.
     */
    protected ClusterRecord getCluster (int bodyOid)
    {
        ClusteredBodyObject bobj = (ClusteredBodyObject)
            CrowdServer.omgr.getObject(bodyOid);
        return (bobj == null) ? null : 
            (ClusterRecord)_clusters.get(bobj.getClusterOid());
    }

    /**
     * Called by the {@link SpotProvider} when we receive a cluster speak
     * request.
     */
    protected void handleClusterSpeakRequest (
        int sourceOid, String source, String bundle, String message, byte mode)
    {
        ClusterRecord clrec = getCluster(sourceOid);
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
     * Returns the location of the specified body or null if they have no
     * location in this scene.
     */
    protected SceneLocation locationForBody (int bodyOid)
    {
        return (SceneLocation)_ssobj.occupantLocs.get(new Integer(bodyOid));
    }

    /**
     * Verifies that the specified cluster can be expanded to include
     * another body.
     */
    protected boolean canAddBody (ClusterRecord clrec, BodyObject body)
    {
        return true;
    }

    /**
     * Called when a user is added to a cluster. The scene manager
     * implementation should take this opportunity to rearrange everyone
     * in the cluster appropriately for the new size.
     */
    protected void bodyAdded (ClusterRecord clrec, BodyObject body)
    {
    }

    /**
     * Called when a user is removed from a cluster. The scene manager
     * implementation should take this opportunity to rearrange everyone
     * in the cluster appropriately for the new size.
     */
    protected void bodyRemoved (ClusterRecord clrec, BodyObject body)
    {
    }

    /**
     * Used to manage clusters which are groups of users that can chat to
     * one another.
     */
    protected class ClusterRecord extends HashIntMap
        implements Subscriber
    {
        public ClusterRecord ()
        {
            CrowdServer.omgr.createObject(ClusterObject.class, this);
        }

        public boolean addBody (BodyObject body)
            throws InvocationException
        {
            if (!(body instanceof ClusteredBodyObject)) {
                Log.warning("Refusing to add non-clustered body to cluster " +
                            "[cloid=" + _clobj.getOid() +
                            ", size=" + size() + ", who=" + body.who() + "].");
                throw new InvocationException(INTERNAL_ERROR);
            }

            // if they're already in the cluster, do nothing
            if (containsKey(body.getOid())) {
                return false;
            }

            // make sure we can add this body
            if (!canAddBody(this, body)) {
//                 Log.debug("Cluster full, refusing growth " + this + ".");
                throw new InvocationException(CLUSTER_FULL);
            }

            // make sure our intrepid joiner is not in any another cluster
            removeFromCluster(body.getOid());

            put(body.getOid(), body);
            _ssobj.startTransaction();
            try {
                body.startTransaction();
                try {
                    bodyAdded(this, body); // do the hokey pokey

                    if (_clobj != null) {
                        ((ClusteredBodyObject)body).setClusterOid(
                            _clobj.getOid());
                        _clobj.addToOccupants(body.getOid());
                        _ssobj.updateClusters(_cluster);
                    }

                } finally {
                    body.commitTransaction();
                }
            } finally {
                _ssobj.commitTransaction();
            }

//             Log.debug("Added " + body.who() + " to "+ this + ".");
            return true;
        }

        public void removeBody (int bodyOid)
        {
            BodyObject body = (BodyObject)remove(bodyOid);
            if (body == null) {
                Log.warning("Requested to remove unknown body from cluster " +
                            "[cloid=" + _clobj.getOid() +
                            ", size=" + size() + ", who=" + bodyOid + "].");
                return;
            }

            body.startTransaction();
            try {
                _ssobj.startTransaction();
                try {
                    ((ClusteredBodyObject)body).setClusterOid(-1);
                    bodyRemoved(this, body); // do the hokey pokey

                    if (_clobj != null) {
                        _clobj.removeFromOccupants(bodyOid);
                        _ssobj.updateClusters(_cluster);
                    }

                } finally {
                    _ssobj.commitTransaction();
                }
            } finally {
                body.commitTransaction();
            }

//             Log.debug("Removed " + bodyOid + " from "+ this + ".");

            // if we've removed our last body; stick a fork in ourselves
            if (size() == 0) {
                destroy();
            }
        }

        public ClusterObject getClusterObject ()
        {
            return _clobj;
        }

        public Cluster getCluster ()
        {
            return _cluster;
        }

        public void objectAvailable (DObject object)
        {
            // keep this feller around
            _clobj = (ClusterObject)object;
            _clusters.put(_clobj.getOid(), this);

            // let any mapped users know about our cluster
            Iterator iter = values().iterator();
            while (iter.hasNext()) {
                ClusteredBodyObject body = (ClusteredBodyObject)iter.next();
                body.setClusterOid(_clobj.getOid());
                _clobj.addToOccupants(((BodyObject)body).getOid());
            }

            // configure our cluster record and publish it
            _cluster.clusterOid = _clobj.getOid();
            _ssobj.addToClusters(_cluster);

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

        public String toString ()
        {
            return "[cluster=" + _cluster + ", size=" + size() + "]";
        }

        protected void destroy ()
        {
//             Log.debug("Cluster empty, going away " +
//                       "[cloid=" + _clobj.getOid() + "].");
            _ssobj.removeFromClusters(_cluster.getKey());
            _clusters.remove(_clobj.getOid());
            CrowdServer.omgr.destroyObject(_clobj.getOid());
        }

        protected ClusterObject _clobj;
        protected Cluster _cluster = new Cluster();
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
