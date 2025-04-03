//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.client;

import com.threerings.presents.client.InvocationReceiver;

import com.threerings.bureau.data.AgentObject;

/**
 * Hooks for controlling a previously launched bureau client.
 */
public interface BureauReceiver extends InvocationReceiver
{
    /**
     * Creates a new agent. Implementors should create a new {@link Agent} and give it access to
     * the {@link AgentObject} referred to by the <code>agentId</code> parameter and must notify
     * the service that the agent has been created using {@link BureauService#agentCreated}.
     * @param agentId the id of the <code>AgentObject</code> that needs an <code>Agent</code>
     */
    void createAgent (int agentId);

    /**
     * Destroys a previously created agent. Implementors should destroy the agent that was created
     * by the call to <code>createAgent</code> with the same agent id and must notify
     * the service that the agent has been created using {@link BureauService#agentDestroyed}.
     * @param agentId the id of the <code>AgentObject</code> whose <code>Agent</code>
     * should be destroyed
     */
    void destroyAgent (int agentId);
}
