//
// $Id: DummySceneRepository.java,v 1.8 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.UpdateList;

/**
 * The dummy scene repository just pretends to load and store scenes, but
 * in fact it just creates new blank scenes when requested to load a scene
 * and does nothing when requested to save one.
 */
public class DummySceneRepository implements SceneRepository
{
    // documentation inherited
    public SceneModel loadSceneModel (int sceneId)
        throws PersistenceException, NoSuchSceneException
    {
        Log.info("Creating dummy scene [id=" + sceneId + "].");
        return SceneModel.blankSceneModel();
    }

    // documentation inherited from interface
    public UpdateList loadUpdates (int sceneId)
        throws PersistenceException, NoSuchSceneException
    {
        return new UpdateList();
    }

    // documentation inherited from interface
    public void addUpdate (SceneUpdate update)
        throws PersistenceException
    {
        // nothing doing
    }
}
