//
// $Id: YoSceneModel.java 17643 2004-10-28 22:58:30Z mdb $

package com.threerings.stage.data;

import com.threerings.util.StreamableArrayList;
import com.threerings.util.StreamableIntIntMap;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * Extends the basic scene model with the notion of a scene type and
 * incorporates the necessary auxiliary models used by the Stage system.
 */
public class StageSceneModel extends SceneModel
{
    /** A scene type code. */
    public static final String WORLD = "world";

    /** This scene's type which is a string identifier used to later
     * construct a specific controller to handle this scene. */
    public String type;

    /** The zone id to which this scene belongs. */
    public int zoneId;

    /** If non-null, contains default colorizations to use for objects
     * that do not have colorizations defined. */
    public StreamableIntIntMap defaultColors;

    /**
     * Get the default color to use for the specified colorization
     * classId, or -1 if no default is set for that colorization.
     */
    public int getDefaultColor (int classId)
    {
        if (defaultColors != null) {
            return defaultColors.get(classId);
        }
        return -1;
    }

    /**
     * Set the default colorId to use for a specified colorization
     * classId, or -1 to clear the default for that colorization.
     */
    public void setDefaultColor (int classId, int colorId)
    {
        if (colorId == -1) {
            if (defaultColors != null) {
                defaultColors.remove(classId);
                if (defaultColors.size() == 0) {
                    defaultColors = null;
                }
            }

        } else {
            if (defaultColors == null) {
                defaultColors = new StreamableIntIntMap();
            }
            defaultColors.put(classId, colorId);
        }
    }

    /**
     * Creates and returns a blank scene model.
     */
    public static StageSceneModel blankStageSceneModel ()
    {
        StageSceneModel model = new StageSceneModel();
        populateBlankStageSceneModel(model);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankStageSceneModel (StageSceneModel model)
    {
        populateBlankSceneModel(model);
        model.addAuxModel(new SpotSceneModel());
        model.addAuxModel(new StageMisoSceneModel());
    }
}
