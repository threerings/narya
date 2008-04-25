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

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/** 
 * Interface for the bureau to communicate with the server.
 */
public interface BureauService extends InvocationService
{
    /**
     * Notifies the server that the bureau is up and running and ready to receive
     * requests via the <code>BureauReceiver</code>.
     * @see BureauReceiver
     */
    void bureauInitialized (Client client, String bureauId);

    /**
     * Notify the server that a previosuly requested agent is not created and ready to use.
     * @see BureauReceiver#createAgent
     */
    void agentCreated (Client client, int agentId);

    /**
     * Notify the server that an agent is no longer running. Normally called in response
     * to a call to <code>destroyAgent</code>
     * @see BureauReceiver#destroyAgent
     */
    void agentDestroyed (Client client, int agentId);
}

