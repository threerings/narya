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

package com.threerings.bureau.server;

import java.util.Map;
import java.util.Set;

import com.threerings.bureau.data.AgentObject;
import com.threerings.bureau.data.BureauCodes;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationManager;

/** 
 *  Abstracts the launching and termination of external processes (bureaus) that host instances of 
 *  server-side code (agents).
 *  TODO: this class needs lots of fleshing out and as such is not 100% documented
 */
public class BureauRegistry 
    implements BureauProvider
{
    /**
     * The binary executable for the external vm being used.
     * TODO: what args are needed?
     * TODO: how to format the args?
     * TODO: where does the binary path really come from - a bureau specification?
     */
    public String vmBinary = "java %{jar} %{host}:%{port} %{agentclassname}";

    /**
     * Creates a new registry, prepared to provide bureau services.
     */
    public BureauRegistry (InvocationManager invmgr, RootDObjectManager omgr)
    {
        _invmgr = invmgr;
        _omgr = omgr;

        invmgr.registerDispatcher(
            new BureauDispatcher(this), 
            BureauCodes.BUREAU_GROUP);
    }

    /** 
     * Starts a new agent using the data in the given object, creating a new bureau if necessary.
     */
    public void startAgent (AgentObject agent)
    {
        // TODO: create new bureau if needed
        // TODO: otherwise call BureauSender.createAgent and pend
    }

    /** 
     * Destorys a previously started agent using the data in the given object.
     */
    public void destroyAgent (AgentObject agent)
    {
        // TODO: call BureauSender.destroyAgent and pend
        // TODO: schedule a shutdown event for the bureau if this is the last agent
    }

    @Override // inherited documentation
    public void bureauInitialized (ClientObject client, String bureauId)
    {
        // TODO: create pending agents
        // TODO: synchronization
    }

    @Override // inherited documentation
    public void agentCreated (ClientObject client, int agentId)
    {
        // TODO: remove from pending
    }

    @Override // inherited documentation
    public void agentDestroyed (ClientObject caller, int arg1)
    {
        // TODO: remove from pending destroyed
    }

    // first stab at the data structures for holding bureaus and agents
    protected static class Bureau
    {
        Process process;
        ClientObject clientObj;
        Set<AgentObject> agents;
    }

    protected InvocationManager _invmgr;
    protected RootDObjectManager _omgr;
    protected Map<String, Bureau> _bureaus;
    protected Map<String, Set<AgentObject>> _pending;
}
