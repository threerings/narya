//
// $Id: SceneCodes.java,v 1.4 2003/06/11 04:14:11 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.LocationCodes;
import com.threerings.whirled.client.SceneDirector;

/**
 * Contains codes used by the scene invocation services.
 */
public interface SceneCodes extends LocationCodes
{
    /** The message identifier for scene update messages. */
    public static final String SCENE_UPDATE = "scene_update";
}
