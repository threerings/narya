//
// $Id: SceneCodes.java,v 1.2 2002/04/15 18:06:20 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.LocationCodes;
import com.threerings.whirled.client.SceneDirector;

/**
 * Contains codes used by the scene invocation services.
 */
public interface SceneCodes extends LocationCodes
{
    /** The module name for the scene services. */
    public static final String MODULE_NAME = "whirled!scene";

    /** The response identifier for a successful moveTo request that
     * includes an updated scene model. This is mapped by the invocation
     * services to a call to {@link
     * SceneDirector#handleMoveSucceededPlusUpdate}. */
    public static final String MOVE_SUCCEEDED_PLUS_UPDATE_RESPONSE =
        "MoveSucceededPlusUpdate";
}
