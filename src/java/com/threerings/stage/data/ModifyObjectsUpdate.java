//
// $Id: AddObjectUpdate.java 15953 2004-06-11 23:40:47Z ray $

package com.threerings.stage.data;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A scene update that is broadcast when objects have been added to or removed
 * from the scene.
 */
public class ModifyObjectsUpdate extends SceneUpdate
{
    /** The objects added to the scene (or <code>null</code> for none). */
    public ObjectInfo[] added;
    
    /** The objects removed from the scene (or <code>null</code> for none). */
    public ObjectInfo[] removed;
    
    /**
     * Initializes this update with all necessary data.
     *
     * @param added the objects added to the scene, or <code>null</code> for
     * none
     * @param removed the objects removed from the scene, or <code>null</code>
     * for none
     */
    public void init (int targetId, int targetVersion, ObjectInfo[] added,
                      ObjectInfo[] removed)
    {
        init(targetId, targetVersion);
        this.added = added;
        this.removed = removed;
    }

    // documentation inherited
    public void apply (SceneModel model)
    {
        super.apply(model);

        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(model);

        // wipe out the objects that need to go
        if (removed != null) {
            for (int ii = 0; ii < removed.length; ii++) {
                mmodel.removeObject(removed[ii]);
            }
        }

        // add the new objects
        if (added != null) {
            for (int ii = 0; ii < added.length; ii++) {
                mmodel.addObject(added[ii]);
            }
        }
    }
}
