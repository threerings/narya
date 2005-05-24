//
// $Id: StageSceneProvider.java 14426 2004-03-12 12:12:32Z mdb $

package com.threerings.stage.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.stage.client.StageSceneService;

/**
 * Defines the server side of the {@link StageSceneService}.
 */
public interface StageSceneProvider extends InvocationProvider
{
    /**
     * Handles a {@link StageSceneService#addObject} request.
     */
    public void addObject (ClientObject caller, ObjectInfo info,
                           StageSceneService.ConfirmListener listener)
        throws InvocationException;
    
    /**
     * Handles a {@link StageSceneService#removeObject} request.
     */
    public void removeObject (ClientObject caller, ObjectInfo info,
                              StageSceneService.ConfirmListener listener)
        throws InvocationException;
}
