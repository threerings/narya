//
// $Id: RuntimeSceneFactory.java,v 1.1 2001/11/12 20:56:56 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.whirled.data.SceneModel;

/**
 * When resolving a scene, the scene registry loads scene models from the
 * scene repository, but it needs an external entity to convert those
 * scene models into the appropriate implementation of {@link
 * RuntimeScene}. The runtime scene factory interface provides the
 * mechanism by which users of the Whirled system can create the
 * appropriate instances.
 */
public interface RuntimeSceneFactory
{
    /**
     * This is called by the scene registry after it has loaded the scene
     * model from the scene repository. The factory implementation is
     * required to know what derivation of {@link
     * com.threerings.crowd.data.PlaceConfig} should go along with the
     * supplied scene model in order to create a {@link RuntimeScene}
     * implementation. The expectation is that such information is either
     * static or can be obtained from the scene model provided to this
     * method.
     */
    public RuntimeScene createScene (SceneModel model);
}
