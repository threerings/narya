//
// $Id: AdminProvider.java,v 1.3 2004/02/25 14:39:14 mdb Exp $

package com.threerings.admin.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.admin.client.AdminService;

/**
 * Provides the server-side implementation of various administrator
 * services.
 */
public class AdminProvider implements InvocationProvider
{
    /**
     * Constructs an admin provider and registers it with the invocation
     * manager to handle admin services. This must be called by any server
     * that wishes to make use of the admin services.
     */
    public static void init (InvocationManager invmgr)
    {
        invmgr.registerDispatcher(
            new AdminDispatcher(new AdminProvider()), true);
    }

    /**
     * Handles a request for the list of config objects.
     */
    public void getConfigInfo (
        ClientObject caller, AdminService.ConfigInfoListener listener)
    {
        // we don't have to validate the request because the user can't do
        // anything with the keys or oids unless they're an admin (we put
        // the burden of doing that checking on the creator of the config
        // object because we would otherwise need some mechanism to
        // determine whether a user is an admin and we don't want to force
        // some primitive system on the service user)
        String[] keys = ConfObjRegistry.getKeys();
        int[] oids = new int[keys.length];
        for (int ii = 0; ii < keys.length; ii++) {
            oids[ii] = ConfObjRegistry.getObject(keys[ii]).getOid();
        }
        listener.gotConfigInfo(keys, oids);
    }
}
