//
// $Id: DummyClientSceneRepository.java,v 1.1 2001/08/14 06:51:07 mdb Exp $

package com.threerings.whirled.client.test;

import java.io.IOException;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.Scene;
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
        throws IOException, NoSuchSceneException
    {
        Log.info("Creating dummy scene [id=" + sceneId + "].");
        return new DummyScene(sceneId);
    }

    // documentation inherited
    public void updateScene (Scene scene)
        throws IOException
    {
        // nothing doing
    }
}
