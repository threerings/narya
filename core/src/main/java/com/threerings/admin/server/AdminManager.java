//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.admin.client.AdminService;
import com.threerings.admin.data.AdminCodes;
import com.threerings.admin.data.AdminMarshaller;

/**
 * Handles admin stuffs.
 */
@Singleton
public class AdminManager
    implements AdminProvider
{
    @Inject public AdminManager (InvocationManager invmgr)
    {
        invmgr.registerProvider(this, AdminMarshaller.class, AdminCodes.ADMIN_GROUP);
    }

    // from interface AdminProvider
    public void getConfigInfo (ClientObject caller, AdminService.ConfigInfoListener listener)
        throws InvocationException
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

    @Inject protected ConfigRegistry _registry;
}
