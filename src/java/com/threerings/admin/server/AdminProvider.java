//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.admin.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.admin.client.AdminService;
import com.threerings.admin.data.AdminCodes;

/**
 * Provides the server-side implementation of various administrator services.
 */
public class AdminProvider implements InvocationProvider
{
    /**
     * Constructs an admin provider and registers it with the invocation manager to handle admin
     * services. This must be called by any server that wishes to make use of the admin services.
     */
    public static void init (InvocationManager invmgr, ConfigRegistry registry)
    {
        invmgr.registerDispatcher(
            new AdminDispatcher(new AdminProvider(registry)), AdminCodes.ADMIN_GROUP);
    }

    /**
     * Handles a request for the list of config objects.
     */
    public void getConfigInfo (ClientObject caller, AdminService.ConfigInfoListener listener)
    {
        // we don't have to validate the request because the user can't do anything with the keys
        // or oids unless they're an admin (we put the burden of doing that checking on the creator
        // of the config object because we would otherwise need some mechanism to determine whether
        // a user is an admin and we don't want to force some primitive system on the service user)
        String[] keys = _registry.getKeys();
        int[] oids = new int[keys.length];
        for (int ii = 0; ii < keys.length; ii++) {
            oids[ii] = _registry.getObject(keys[ii]).getOid();
        }
        listener.gotConfigInfo(keys, oids);
    }

    protected AdminProvider (ConfigRegistry registry)
    {
        _registry = registry;
    }

    protected ConfigRegistry _registry;
}
