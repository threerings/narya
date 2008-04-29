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

package com.threerings.bureau.data;

import com.threerings.bureau.client.BureauService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link BureauService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class BureauMarshaller extends InvocationMarshaller
    implements BureauService
{
    /** The method id used to dispatch {@link #agentCreated} requests. */
    public static final int AGENT_CREATED = 1;

    // from interface BureauService
    public void agentCreated (Client arg1, int arg2)
    {
        sendRequest(arg1, AGENT_CREATED, new Object[] {
            Integer.valueOf(arg2)
        });
    }

    /** The method id used to dispatch {@link #agentCreationFailed} requests. */
    public static final int AGENT_CREATION_FAILED = 2;

    // from interface BureauService
    public void agentCreationFailed (Client arg1, int arg2)
    {
        sendRequest(arg1, AGENT_CREATION_FAILED, new Object[] {
            Integer.valueOf(arg2)
        });
    }

    /** The method id used to dispatch {@link #agentDestroyed} requests. */
    public static final int AGENT_DESTROYED = 3;

    // from interface BureauService
    public void agentDestroyed (Client arg1, int arg2)
    {
        sendRequest(arg1, AGENT_DESTROYED, new Object[] {
            Integer.valueOf(arg2)
        });
    }

    /** The method id used to dispatch {@link #bureauInitialized} requests. */
    public static final int BUREAU_INITIALIZED = 4;

    // from interface BureauService
    public void bureauInitialized (Client arg1, String arg2)
    {
        sendRequest(arg1, BUREAU_INITIALIZED, new Object[] {
            arg2
        });
    }
}
