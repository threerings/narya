//
// $Id: SceneController.java,v 1.2 2003/06/11 04:14:11 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.WhirledContext;

/**
 * The base scene controller class. It is expected that users of the
 * Whirled services will extend this controller class when creating
 * specialized controllers for their scenes.
 */
public abstract class SceneController extends PlaceController
{
    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        super.init(ctx, config);
        _ctx = (WhirledContext)ctx;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);
        plobj.addListener(_updateListener);
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        plobj.removeListener(_updateListener);
    }

    /**
     * This method is called if a scene update is recorded while we
     * currently occupy a scene. The default implementation will update
     * our local scene and scene model, but derived classes will likely
     * want to ensure that the update is properly displayed.
     */
    protected void sceneUpdated (SceneUpdate update)
    {
        // apply the update to the scene
        _ctx.getSceneDirector().getScene().updateReceived(update);

        // we don't persistify these updates in this circumstance, but
        // next time we come to this scene we'll redownload the update and
        // apply it to the repository; as the updates are meant to be very
        // small, this shouldn't be horribly less efficient
    }

    /** Used to listen for scene updates. */
    protected MessageListener _updateListener = new MessageListener() {
        public void messageReceived (MessageEvent event) {
            if (event.getName().equals(SceneCodes.SCENE_UPDATE)) {
                sceneUpdated((SceneUpdate)event.getArgs()[0]);
            }
        }
    };

    protected WhirledContext _ctx;
}
