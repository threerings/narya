//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.client;

import com.threerings.bureau.data.AgentObject;

/**
 * Represents an agent running within a bureau client.
 */
public abstract class Agent
{
    /**
     * Initializes the Agent with the distributed agent object.
     */
    public void init (AgentObject agentObj)
    {
        _agentObj = agentObj;
    }

    /**
     * Starts the code running in the agent.
     */
    public abstract void start ();

    /**
     * Stops the code running in the agent.
     */
    public abstract void stop ();

    /**
     * The shared agent object.
     */
    protected AgentObject _agentObj;
}
