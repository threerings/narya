//
// $Id: WorldSceneController.java 9625 2003-06-11 04:17:18Z mdb $

package com.threerings.stage.client;

import com.samskivert.util.Tuple;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.client.SpotSceneController;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageLocation;
import com.threerings.stage.util.StageContext;

/**
 * Extends the {@link SpotSceneController} with functionality specific to
 * displaying Stage scenes.
 */
public class StageSceneController extends SpotSceneController
{
    /**
     * Called when the user clicks on a location within the scene.
     */
    public void handleLocationClicked (Object source, StageLocation loc)
    {
        Log.warning("handleLocationClicked(" + source + ", " + loc + ")");
    }

    /**
     * Handles a cluster clicked event.
     *
     * @param tuple a Tuple containing (Cluster, Point) with the Cluster
     * that was clicked and the Point being the screen coords of the click.
     */
    public void handleClusterClicked (Object source, Tuple tuple)
    {
        Log.warning("handleClusterClicked(" + source + ", " + tuple + ")");
    }

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
