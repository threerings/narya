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

package com.threerings.bureau.data {

import com.threerings.bureau.client.BureauService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.util.Integer;

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
    public static const AGENT_CREATED :int = 1;

    // from interface BureauService
    public function agentCreated (arg1 :Client, arg2 :int) :void
    {
        sendRequest(arg1, AGENT_CREATED, [
            Integer.valueOf(arg2)
        ]);
    }

    /** The method id used to dispatch {@link #agentCreationFailed} requests. */
    public static const AGENT_CREATION_FAILED :int = 2;

    // from interface BureauService
    public function agentCreationFailed (arg1 :Client, arg2 :int) :void
    {
        sendRequest(arg1, AGENT_CREATION_FAILED, [
            Integer.valueOf(arg2)
        ]);
    }

    /** The method id used to dispatch {@link #agentDestroyed} requests. */
    public static const AGENT_DESTROYED :int = 3;

    // from interface BureauService
    public function agentDestroyed (arg1 :Client, arg2 :int) :void
    {
        sendRequest(arg1, AGENT_DESTROYED, [
            Integer.valueOf(arg2)
        ]);
    }

    /** The method id used to dispatch {@link #bureauInitialized} requests. */
    public static const BUREAU_INITIALIZED :int = 4;

    // from interface BureauService
    public function bureauInitialized (arg1 :Client, arg2 :String) :void
    {
        sendRequest(arg1, BUREAU_INITIALIZED, [
            arg2
        ]);
    }
}
}
