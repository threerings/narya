//
// $Id: DummySceneRepository.java,v 1.3 2001/09/21 03:15:02 mdb Exp $

package com.threerings.whirled.server.test;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The dummy scene repository just pretends to load and store scenes, but
 * in fact it just creates new blank scenes when requested to load a scene
 * and does nothing when requested to save one.
 */
public class DummySceneRepository implements SceneRepository
{
    // documentation inherited
    public Scene loadScene (int sceneId)
        throws PersistenceException, NoSuchSceneException
    {
        Log.info("Creating dummy scene [id=" + sceneId + "].");
        return new DummyScene(sceneId);
    }

    // documentation inherited
    public void updateScene (Scene scene)
        throws PersistenceException
    {
        // nothing doing
    }
}
