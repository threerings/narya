//
// $Id: DefaultRuntimeSceneFactory.java,v 1.1 2001/11/12 20:56:56 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.whirled.data.DefaultSceneConfig;
import com.threerings.whirled.data.SceneModel;

/**
 * The default runtime scene factory creates {@link RuntimeSceneImpl}
 * instances with {@link DefaultSceneConfig} instances (which create the
 * default scene manager and controller).
 */
public class DefaultRuntimeSceneFactory implements RuntimeSceneFactory
{
    // documentation inherited
    public RuntimeScene createScene (SceneModel model)
    {
        return new RuntimeSceneImpl(model, new DefaultSceneConfig());
    }
}
