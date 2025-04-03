//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.bureau.client.BureauService;

/**
 * Defines the server-side of the {@link BureauService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from BureauService.java.")
public interface BureauProvider extends InvocationProvider
{
    /**
     * Handles a {@link BureauService#agentCreated} request.
     */
    void agentCreated (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#agentCreationFailed} request.
     */
    void agentCreationFailed (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#agentDestroyed} request.
     */
    void agentDestroyed (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#bureauError} request.
     */
    void bureauError (ClientObject caller, String arg1);

    /**
     * Handles a {@link BureauService#bureauInitialized} request.
     */
    void bureauInitialized (ClientObject caller, String arg1);
}
