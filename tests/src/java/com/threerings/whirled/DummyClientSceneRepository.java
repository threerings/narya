//
// $Id: DummyClientSceneRepository.java,v 1.5 2001/11/18 04:09:21 mdb Exp $

package com.threerings.whirled;

import java.io.IOException;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The dummy scene repository just pretends to load and store scenes, but
 * in fact it just creates new blank scenes when requested to load a scene
 * and does nothing when requested to save one.
 */
public class DummyClientSceneRepository implements SceneRepository
{
    // documentation inherited
    public SceneModel loadSceneModel (int sceneId)
        throws IOException, NoSuchSceneException
    {
        Log.info("Creating dummy scene model [id=" + sceneId + "].");
        return new SceneModel();
    }

    // documentation inherited
    public void updateSceneModel (SceneModel scene)
        throws IOException
    {
        // nothing doing
    }
}
