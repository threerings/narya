//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

/**
 * Interface for the bureau to communicate with the server.
 */
public interface BureauService extends InvocationService<ClientObject>
{
    /**
     * Notifies the server that the bureau is up and running and ready to receive
     * requests via the <code>BureauReceiver</code>.
     * @see BureauReceiver
     */
    void bureauInitialized (String bureauId);

    /**
     * Notifies the server that this bureau has encountered a critical error and needs to be shut
     * down.
     */
    void bureauError (String message);

    /**
     * Notify the server that a previosuly requested agent is now created and ready to use.
     * @see BureauReceiver#createAgent
     */
    void agentCreated (int agentId);

    /**
     * Notify the server that a previosuly requested agent could not be created.
     * @see BureauReceiver#createAgent
     */
    void agentCreationFailed (int agentId);

    /**
     * Notify the server that an agent is no longer running. Normally called in response
     * to a call to <code>destroyAgent</code>
     * @see BureauReceiver#destroyAgent
     */
    void agentDestroyed (int agentId);
}

