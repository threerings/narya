//
// $Id: SpotProvider.java,v 1.21 2003/10/25 00:10:35 mdb Exp $

package com.threerings.whirled.spot.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.server.CommunicationAuthorizer;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.ScenedBodyObject;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.spot.Log;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotScene;

/**
 * Provides the server-side implementation of the spot services.
 */
public class SpotProvider
    implements SpotCodes, InvocationProvider
{
    /**
     * Creates a spot provider that can be registered with the invocation
     * manager to handle spot services.
     */
    public SpotProvider (RootDObjectManager omgr, PlaceRegistry plreg,
                         SceneRegistry screg)
    {
        // we'll need these later
        _plreg = plreg;
        _screg = screg;
        _omgr = omgr;
    }

    /**
     * Set the authorizer we will use to see if the user is allowed to
     * perform various chatting actions.
     */
    public static void setCommunicationAuthorizer (
        CommunicationAuthorizer comAuth)
    {
        _comAuth = comAuth;
    }

    /**
     * Processes a {@link SpotService#traversePortal} request.
     */
    public void traversePortal (ClientObject caller, int portalId,
                                int destSceneVer, SceneMoveListener listener)
        throws InvocationException
    {
        int sceneId = getCallerSceneId(caller);

        // avoid cluttering up the method declaration with final keywords
        final BodyObject fsource = (BodyObject)caller;
        final int fportalId = portalId;
        final int fsceneVer = destSceneVer;
        final SceneMoveListener flistener = listener;

        // obtain the source scene
        final SpotSceneManager srcmgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (srcmgr == null) {
            Log.warning("Traverse portal missing source scene " +
                        "[user=" + fsource.who() + ", sceneId=" + sceneId +
                        ", portalId=" + portalId + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // obtain the destination scene and location id
        SpotScene rss = (SpotScene)srcmgr.getScene();
        final Portal fdest = rss.getPortal(portalId);

        // give the source scene manager a chance to do access control
        String errmsg = srcmgr.mayTraversePortal(fsource, fdest);
        if (errmsg != null) {
            throw new InvocationException(errmsg);
        }

        // make sure this portal has valid info
        if (fdest == null || !fdest.isValid()) {
            Log.warning("Traverse portal with invalid portal " +
                        "[user=" + fsource.who() + ", scene=" + srcmgr.where() +
                        ", pid=" + portalId + ", portal=" + fdest +
                        ", portals=" + StringUtil.toString(rss.getPortals()) +
                        "].");
            throw new InvocationException(NO_SUCH_PORTAL);
        }

        // resolve their destination scene
        SceneRegistry.ResolutionListener rl =
            new SceneRegistry.ResolutionListener() {
                public void sceneWasResolved (SceneManager scmgr) {
                    // make sure our caller is still around; under heavy
                    // load, clients might end their session while the
                    // scene is resolving
                    if (!fsource.isActive()) {
                        Log.info("Abandoning portal traversal, client gone " +
                                 "[who=" + fsource.who()  +
                                 ", dest=" + scmgr.where() + "].");
                        return;
                    }

                    // let the source manager know that this guy is
                    // departing via the specified portal
                    srcmgr.willTraversePortal(fsource, fdest);

                    SpotSceneManager sscmgr = (SpotSceneManager)scmgr;
                    finishTraversePortalRequest(
                        fsource, sscmgr, fsceneVer, fdest, flistener);
                }

                public void sceneFailedToResolve (
                    int rsceneId, Exception reason) {
                    Log.warning("Unable to resolve target scene " +
                                "[sceneId=" + rsceneId +
                                ", reason=" + reason + "].");
                    // pretend like the scene doesn't exist to the client
                    flistener.requestFailed(NO_SUCH_PLACE);
                }
            };
        _screg.resolveScene(fdest.targetSceneId, rl);
    }

    /**
     * This is called after the scene to which we are moving is guaranteed
     * to have been loaded into the server.
     */
    protected void finishTraversePortalRequest (
        BodyObject source, SpotSceneManager destmgr, int sceneVer,
        Portal dest, SceneMoveListener listener)
    {
        // let the destination scene manager know that we're coming in
        destmgr.mapEnteringBody(source, dest.targetPortalId);

        try {
            // move to the place object associated with this scene
            _screg.sceneprov.effectSceneMove(
                source, destmgr, sceneVer, listener);
        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());
            // and let the destination scene manager know that we're no
            // longer coming in
            destmgr.clearEnteringBody(source);
        }
    }

    /**
     * Processes a {@link SpotService#changeLocation} request.
     */
    public void changeLocation (ClientObject caller, int sceneId, Location loc,
                                SpotService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject source = (BodyObject)caller;
        int cSceneId = getCallerSceneId(caller);
        if (cSceneId != sceneId) {
            Log.warning("Rejecting changeLocation for invalid scene " +
                        "[user=" + source.who() + ", insid=" + cSceneId +
                        ", wantsid=" + sceneId + ", loc=" + loc + "].");
            throw new InvocationException(INVALID_LOCATION);
        }

        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested to change location from " +
                        "non-existent scene [user=" + source.who() +
                        ", sceneId=" + sceneId + ", loc=" + loc +"].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // pass the buck to yon scene manager
        smgr.handleChangeLoc(source, loc);

        // if that method finished, we're good to go
        listener.requestProcessed();
    }

    /**
     * Processes a {@link SpotService#joinCluster} request.
     */
    public void joinCluster (ClientObject caller, int friendOid,
                             SpotService.ConfirmListener listener)
        throws InvocationException
    {
        int sceneId = getCallerSceneId(caller);
        BodyObject source = (BodyObject)caller;

        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested to join cluster from " +
                        "non-existent scene [user=" + source.who() +
                        ", sceneId=" + sceneId + ", foid=" + friendOid +"].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // pass the buck to yon scene manager
        smgr.handleJoinCluster(source, friendOid);

        // if that method finished, we're good to go
        listener.requestProcessed();
    }

    /**
     * Handles request to generate a speak message in the specified cluster.
     */
    public void clusterSpeak (ClientObject caller, String message, byte mode)
        throws InvocationException
    {
        // make sure the caller is authorized to perform this action
        if ((_comAuth != null) && (!_comAuth.authorized(caller))) {
            return;
        }

        BodyObject source = (BodyObject)caller;
        sendClusterChatMessage(getCallerSceneId(caller), source.getOid(),
                               source.username, null, message, mode);
    }

    /**
     * Sends a cluster chat notification to the specified location in the
     * specified place object originating with the specified speaker (the
     * speaker can be a server entity that wishes to fake a "speak"
     * message, in which case the bundle argument should be non-null and
     * should contain the id of the bundle to be used to translate the
     * message text) and with the supplied message content.
     *
     * @param sceneId the scene id in which to deliver the chat message.
     * @param speakerOid the body object id of the speaker (used to verify
     * that they are in the cluster in question).
     * @param speaker the username of the user that generated the message
     * (or some special speaker name for server messages).
     * @param bundle the bundle identifier that will be used by the client
     * to translate the message text (or null if the message originated
     * from a real live human who wrote it in their native tongue).
     * @param message the text of the chat message.
     */
    public void sendClusterChatMessage (
        int sceneId, int speakerOid, String speaker,
        String bundle, String message, byte mode)
    {
        // look up the scene manager for the specified scene
        SpotSceneManager smgr = (SpotSceneManager)
            _screg.getSceneManager(sceneId);
        if (smgr == null) {
            Log.warning("User requested cluster chat in non-existent scene " +
                        "[user=" + speaker + ", sceneId=" + sceneId +
                        ", message=" + message + "].");
            return;
        }

        // pass this request on to the spot scene manager
        smgr.handleClusterSpeakRequest(
            speakerOid, speaker, bundle, message, mode);
    }

    /**
     * Obtains the scene id occupied by the supplied caller.
     *
     * @exception InvocationException thrown if the caller does not
     * implement {@link ScenedBodyObject}.
     */
    protected static int getCallerSceneId (ClientObject caller)
        throws InvocationException
    {
        if (caller instanceof ScenedBodyObject) {
            return ((ScenedBodyObject)caller).getSceneId();
        } else {
            Log.warning("Can't get scene from non-scened caller " +
                        caller.who() + ".");
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /** The place registry with which we interoperate. */
    protected PlaceRegistry _plreg;

    /** The scene registry with which we interoperate. */
    protected SceneRegistry _screg;

    /** The object manager we use to do dobject stuff. */
    protected RootDObjectManager _omgr;

    /** The entity that will authorize our speaker. */
    protected static CommunicationAuthorizer _comAuth;
}
