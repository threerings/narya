//
// $Id: YoMisoSceneModel.java 9680 2003-06-13 02:16:00Z mdb $

package com.threerings.stage.data;

import com.threerings.miso.data.SparseMisoSceneModel;

import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * Extends the {@link SparseMisoSceneModel} with the necessary interface
 * to wire it up to the Whirled auxiliary model system.
 */
public class StageMisoSceneModel extends SparseMisoSceneModel
    implements AuxModel
{
    /** The width (in tiles) of a scene section. */
    public static final int SECTION_WIDTH = 9;

    /** The height (in tiles) of a scene section. */
    public static final int SECTION_HEIGHT = 9;

    /**
     * Creates a completely uninitialized scene model.
     */
    public StageMisoSceneModel ()
    {
        super(SECTION_WIDTH, SECTION_HEIGHT);
    }

    /**
     * Locates and returns the {@link StageMisoSceneModel} among the
     * auxiliary scene models associated with the supplied scene model.
     * <code>null</code> is returned if no miso scene model could be
     * found.
     */
    public static StageMisoSceneModel getSceneModel (SceneModel model)
    {
        for (int ii = 0; ii < model.auxModels.length; ii++) {
            if (model.auxModels[ii] instanceof StageMisoSceneModel) {
                return (StageMisoSceneModel)model.auxModels[ii];
            }
        }
        return null;
    }

    /**
     * Returns the section key for the specified tile coordinate.
     */
    public int getSectionKey (int x, int y)
    {
        return key(x, y);
    }

    /**
     * Returns the section identified by the specified key, or null if no
     * section exists for that key.
     */
    public Section getSection (int key)
    {
        return (Section)_sections.get(key);
    }
}
