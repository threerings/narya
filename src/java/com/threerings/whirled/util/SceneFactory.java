//
// $Id: SceneFactory.java,v 1.1 2003/02/12 07:23:32 mdb Exp $

package com.threerings.whirled.util;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneModel;

/**
 * This is used by the Whirled services to obtain a {@link Scene}
 * implementation given a scene model and associated data.
 */
public interface SceneFactory
{
    /**
     * Creates a {@link Scene} implementation given the supplied scene
     * model and place config.
     */
    public Scene createScene (SceneModel model, PlaceConfig config);
}
