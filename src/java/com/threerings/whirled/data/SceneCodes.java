//
// $Id: SceneCodes.java,v 1.5 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.LocationCodes;

/**
 * Contains codes used by the scene invocation services.
 */
public interface SceneCodes extends LocationCodes
{
    /** The message identifier for scene update messages. */
    public static final String SCENE_UPDATE = "scene_update";
}
