//
// $Id: StageSceneConfig.java 8478 2003-05-07 05:12:26Z mdb $

package com.threerings.stage.data;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceConfig;

import com.threerings.stage.client.StageSceneController;

/**
 * Place configuration for the main isometric scenes in Stage.
 */
public class StageSceneConfig extends PlaceConfig
{
    // documentation inherited
    public PlaceController createController ()
    {
        return new StageSceneController();
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.stage.server.StageSceneManager";
    }
}
