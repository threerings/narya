//
// $Id: DisplaySceneFactory.java,v 1.1 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.SceneModel;

/**
 * When resolving a scene, the scene director loads a scene model from the
 * scene repository, but it needs an external entity to convert that scene
 * model into the appropriate implementation of {@link DisplayScene}. The
 * display scene factory interface provides the mechanism by which users
 * of the Whirled system can create the appropriate instances.
 */
public interface DisplaySceneFactory
{
    /**
     * This is called by the scene director after it has loaded the scene
     * model from the scene repository and obtained the place config from
     * a successful <code>moveTo</code> request.
     */
    public DisplayScene createScene (SceneModel model, PlaceConfig config);
}
