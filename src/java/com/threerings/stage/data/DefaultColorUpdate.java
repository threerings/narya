//
// $Id: DefaultColorUpdate.java 16551 2004-07-27 20:53:28Z ray $

package com.threerings.stage.data;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Update to change the default colorization for objects in a scene which
 * do not define their own colorization.
 */
public class DefaultColorUpdate extends SceneUpdate
{
    /** The class id of the colorization we're changing. */
    public int classId;

    /** The color id to set as the new default, or -1 to remove the default. */
    public int colorId;

    /**
     * Initializes this update.
     */
    public void init (int targetId, int targetVersion, int classId, int colorId)
    {
        init(targetId, targetVersion);
        this.classId = classId;
        this.colorId = colorId;
    }

    // documentation inherited
    public void apply (SceneModel model)
    {
        super.apply(model);

        StageSceneModel smodel = (StageSceneModel)model;
        smodel.setDefaultColor(classId, colorId);
    }
}
