//
// $Id: EditableSceneImpl.java,v 1.5 2001/12/05 08:45:06 mdb Exp $

package com.threerings.whirled.tools;

import java.util.ArrayList;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.client.DisplaySceneImpl;
import com.threerings.whirled.data.SceneModel;

/**
 * A basic implementation of the {@link EditableScene} interface.
 */
public class EditableSceneImpl implements EditableScene
{
    /**
     * Creates an instance that will create and use a blank scene model.
     */
    public EditableSceneImpl ()
    {
        this(EditableSceneModel.blankSceneModel());
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made.
     */
    public EditableSceneImpl (EditableSceneModel model)
    {
        this(model, new DisplaySceneImpl(model.sceneModel, null));
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made. It will delegate to the
     * supplied display scene instead of creating its own delegate.
     */
    public EditableSceneImpl (
        EditableSceneModel model, DisplaySceneImpl delegate)
    {
        _model = model.sceneModel;
        _emodel = model;
        _delegate = delegate;
    }

    // documentation inherited
    public int getId ()
    {
        return _delegate.getId();
    }

    // documentation inherited
    public int getVersion ()
    {
        return _delegate.getVersion();
    }

    // documentation inherited
    public int[] getNeighborIds ()
    {
        return _delegate.getNeighborIds();
    }

    // documentation inherited
    public PlaceConfig getPlaceConfig ()
    {
        return _delegate.getPlaceConfig();
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
    public ArrayList getNeighborNames ()
    {
        return _emodel.neighborNames;
    }

    // documentation inherited
    public void addNeighbor (String neighborName)
    {
        if (!_emodel.neighborNames.contains(neighborName)) {
            _emodel.neighborNames.add(neighborName);
        }
    }

    // documentation inherited
    public boolean removeNeighbor (String neighborName)
    {
        return _emodel.neighborNames.remove(neighborName);
    }

    // documentation inherited
    public EditableSceneModel getSceneModel ()
    {
        return _emodel;
    }

    /** A reference to our scene model. */
    protected SceneModel _model;

    /** A reference to our editable scene model. */
    protected EditableSceneModel _emodel;

    /** Our display scene delegate. */
    protected DisplaySceneImpl _delegate;
}
