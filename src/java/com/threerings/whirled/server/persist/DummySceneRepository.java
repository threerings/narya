//
// $Id: DummySceneRepository.java,v 1.7 2001/11/12 20:56:56 mdb Exp $

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

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

        // create a blank scene model
        SceneModel model = new SceneModel();
        model.sceneId = sceneId;
        model.version = 1;
        model.neighborIds = new int[0];

        return model;
    }

    // documentation inherited
    public void updateSceneModel (SceneModel model)
        throws PersistenceException
    {
        // nothing doing
    }
}
