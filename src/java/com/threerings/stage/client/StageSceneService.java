//
// $Id: WorldSceneService.java 14426 2004-03-12 12:12:32Z mdb $

package com.threerings.stage.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.miso.data.ObjectInfo;

/**
 * Provides services relating to Stage scenes.
 */
public interface StageSceneService extends InvocationService
{
    /**
     * Requests to add the supplied object to the current scene.
     */
    public void addObject (Client client, ObjectInfo info,
                           ConfirmListener listener);
}
