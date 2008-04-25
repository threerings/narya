//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.bureau.client;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.data.ClientObject;

/**
 * Hooks for controlling a previously launched bureau client.
 */
public interface BureauReceiver extends InvocationReceiver
{
    /**
     * Creates a new agent. Implementors should create a new {@link Agent} and give it access to 
     * the {@link AgentObject} referred to by the <code>agentId</code> parameter and must notify
     * the service that the agent has been created using {@link BureauService#agentCreated}.
     * @param client the client receiving the request
     * @param agentId the id of the <code>AgentObject</code> that needs an <code>Agent</code>
     */
    void createAgent (ClientObject client, int agentId);

    /**
     * Destroys a previously created agent. Implementors should destroy the agent that was created 
     * by the call to <code>createAgent</code> with the same agent id and must notify
     * the service that the agent has been created using {@link BureauService#agentDestroyed}.
     * @param client the client receiving the request
     * @param agentId the id of the <code>AgentObject</code> whose <code>Agent</code> 
     * should be destroyed
     */
    void destroyAgent (ClientObject client, int agentId);
}
