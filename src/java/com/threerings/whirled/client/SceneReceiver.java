//
// $Id: SceneReceiver.java,v 1.1 2002/08/14 19:07:57 mdb Exp $

package com.threerings.whirled.client;

import com.threerings.presents.client.InvocationReceiver;

/**
 * Defines, for the scene services, a set of notifications delivered
 * asynchronously by the server to the client.
 */
public interface SceneReceiver extends InvocationReceiver
{
    /**
     * Used to communicate a required move notification to the client. The
     * server will have removed the client from their existing scene
     * and the client is then responsible for generating a {@link
     * SceneService#moveTo} request to move to the new scene.
     */
    public void forcedMove (int sceneId);
}
