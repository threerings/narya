//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.client.PeerService;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link PeerService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from PeerService.java.")
public interface PeerProvider extends InvocationProvider
{
    /**
     * Handles a {@link PeerService#generateReport} request.
     */
    void generateReport (ClientObject caller, String arg1, InvocationService.ResultListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PeerService#invokeAction} request.
     */
    void invokeAction (ClientObject caller, byte[] arg1);

    /**
     * Handles a {@link PeerService#invokeRequest} request.
     */
    void invokeRequest (ClientObject caller, byte[] arg1, InvocationService.ResultListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PeerService#ratifyLockAction} request.
     */
    void ratifyLockAction (ClientObject caller, NodeObject.Lock arg1, boolean arg2);
}
