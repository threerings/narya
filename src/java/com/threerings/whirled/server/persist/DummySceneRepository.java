//
// $Id: DummySceneRepository.java,v 1.1 2001/08/11 04:09:50 mdb Exp $

package com.threerings.whirled.server.persist;

import java.io.IOException;
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
    public Scene getScene (int sceneid)
        throws IOException, NoSuchSceneException
    {
        return null;
    }

    // documentation inherited
    public void updateScene (Scene scene)
        throws IOException
    {
        // nothing doing
    }
}
