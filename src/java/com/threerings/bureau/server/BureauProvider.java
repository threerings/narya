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

import com.threerings.bureau.client.BureauService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link BureauService}.
 */
public interface BureauProvider extends InvocationProvider
{
    /**
     * Handles a {@link BureauService#agentCreated} request.
     */
    public void agentCreated (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#agentCreationFailed} request.
     */
    public void agentCreationFailed (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#agentDestroyed} request.
     */
    public void agentDestroyed (ClientObject caller, int arg1);

    /**
     * Handles a {@link BureauService#bureauInitialized} request.
     */
    public void bureauInitialized (ClientObject caller, String arg1);
}
