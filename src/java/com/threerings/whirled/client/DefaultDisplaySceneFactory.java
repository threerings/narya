//
// $Id: DefaultDisplaySceneFactory.java,v 1.1 2001/11/18 04:09:23 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;

/**
 * The default display scene factory creates {@link DisplaySceneImpl}
 * instances.
 */
public class DefaultDisplaySceneFactory implements DisplaySceneFactory
{
    // documentation inherited
    public DisplayScene createScene (SceneModel model, PlaceConfig config)
    {
        return new DisplaySceneImpl(model, config);
    }
}
