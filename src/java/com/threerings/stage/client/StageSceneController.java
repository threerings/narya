//
// $Id: WorldSceneController.java 9625 2003-06-11 04:17:18Z mdb $

package com.threerings.stage.client;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.client.SpotSceneController;

import com.threerings.stage.util.StageContext;

/**
 * Extends the {@link SpotSceneController} with functionality specific to
 * displaying Stage scenes.
 */
public class StageSceneController extends SpotSceneController
{
    // documentation inherited
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new StageScenePanel((StageContext)ctx, this);
    }

    // documentation inherited
    protected void sceneUpdated (SceneUpdate update)
    {
        super.sceneUpdated(update);

        // let the scene panel know to rethink everything
        ((StageScenePanel)_view).sceneUpdated(update);
    }
}
