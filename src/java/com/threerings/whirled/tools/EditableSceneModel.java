//
// $Id: EditableSceneModel.java,v 1.1 2001/12/04 22:34:04 mdb Exp $

package com.threerings.whirled.tools;

import com.threerings.whirled.data.SceneModel;

/**
 * The editable scene model contains information above and beyond the
 * regular scene model that is necessary for editing and loading scenes.
 */
public class EditableSceneModel
{
    /** The scene model that we extend. */
    public SceneModel sceneModel;

    /** The human readable name of this scene. */
    public String sceneName;

    /** The human readable name of this scene's neighbors. */
    public String[] neighborNames;
}
