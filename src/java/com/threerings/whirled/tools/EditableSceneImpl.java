//
// $Id: EditableSceneImpl.java,v 1.2 2001/11/13 02:25:36 mdb Exp $

package com.threerings.whirled.tools;

import com.threerings.whirled.client.DisplaySceneImpl;
import com.threerings.whirled.data.SceneModel;

/**
 * A basic implementation of the {@link EditableScene} interface.
 */
public class EditableSceneImpl
    extends DisplaySceneImpl
    implements EditableScene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made.
     */
    public EditableSceneImpl (SceneModel model)
    {
        super(model, null);
    }

    // documentation inherited
    public void setId (int sceneId)
    {
        _model.sceneId = sceneId;
    }

    // documentation inherited
    public void setVersion (int version)
    {
        _model.version = version;
    }

    // documentation inherited
    public void setNeighborIds (int[] neighborIds)
    {
        _model.neighborIds = neighborIds;
    }

    // documentation inherited
    public SceneModel getSceneModel ()
    {
        return _model;
    }
}
