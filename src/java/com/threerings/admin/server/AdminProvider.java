//
// $Id: AdminProvider.java,v 1.1 2002/06/07 06:22:24 mdb Exp $

package com.threerings.admin.server;

import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.presents.server.ServiceFailedException;

import com.threerings.crowd.data.BodyObject;

import com.threerings.admin.Log;
import com.threerings.admin.data.AdminCodes;

/**
 * Provides the server-side interface to various administrator services.
 */
public class AdminProvider extends InvocationProvider
    implements AdminCodes
{
    /**
     * Constructs an admin provider and registers it with the invocation
     * manager to handle admin services. This must be called by any server
     * that wishes to make use of the admin services.
     */
    public static void init (InvocationManager invmgr)
    {
        invmgr.registerProvider(MODULE_NAME, new AdminProvider());
    }

    /**
     * Processes a request from a client to obtain the configuration keys
     * and oids for all registered configuration objects.
     */
    public void handleGetConfigInfoRequest (BodyObject source, int invid)
        throws ServiceFailedException
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
        sendResponse(source, invid, CONFIG_INFO_RESPONSE, keys, oids);
    }
}
