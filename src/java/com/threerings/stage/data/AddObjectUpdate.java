//
// $Id: AddObjectUpdate.java 15953 2004-06-11 23:40:47Z ray $

package com.threerings.stage.data;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A scene update that is broadcast when an object has been added to a
 * scene.
 */
public class AddObjectUpdate extends SceneUpdate
{
    /** The info on the object. */
    public ObjectInfo info;

    /** If non-null, a list of objects to remove from the scene. */
    public ObjectInfo[] casualties;

    /**
     * Initializes this update with all necessary data.
     *
     * @param casualties optional, a list of objects to remove.
     */
    public void init (int targetId, int targetVersion, ObjectInfo info,
                      ObjectInfo[] casualties)
    {
        init(targetId, targetVersion);
        this.info = info;
        this.casualties = casualties;
    }

    // documentation inherited
    public void apply (SceneModel model)
    {
        super.apply(model);

        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(model);

        // wipe out the objects that need to go
        if (casualties != null) {
            for (int ii = 0; ii < casualties.length; ii++) {
                mmodel.removeObject(casualties[ii]);
            }
        }

        // add the new object
        mmodel.addObject(info);
    }
}
