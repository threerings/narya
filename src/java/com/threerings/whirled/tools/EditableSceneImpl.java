//
// $Id: EditableSceneImpl.java,v 1.3 2001/12/04 22:34:04 mdb Exp $

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
    public EditableSceneImpl (EditableSceneModel model)
    {
        super(model.sceneModel, null);
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
    public String getName ()
    {
        return _emodel.sceneName;
    }

    // documentation inherited
    public void setName (String name)
    {
        _emodel.sceneName = name;
    }

    // documentation inherited
    public String[] getNeighborNames ()
    {
        return _emodel.neighborNames;
    }

    // documentation inherited
    public void setNeighborNames (String[] neighborNames)
    {
        _emodel.neighborNames = neighborNames;
    }

    // documentation inherited
    public EditableSceneModel getSceneModel ()
    {
        return _emodel;
    }

    /** A reference to our editable scene model. */
    protected EditableSceneModel _emodel;
}
