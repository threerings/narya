//
// $Id: AdminService.java,v 1.1 2002/06/07 06:22:24 mdb Exp $

package com.threerings.admin.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.admin.Log;
import com.threerings.admin.data.AdminCodes;

/**
 * Handles the client side of the admin invocation services.
 */
public class AdminService
     implements AdminCodes
{
    /**
     * Requests the list of config objects. This will result in a call to
     *
     * <pre>
     *   public void handleConfigInfoResponse (int invid, String[] keys,
     *                                         int[] oids)
     * </pre>
     *
     * on the response target.
     */
    public static void getConfigInfo (Client client, Object rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        invdir.invoke(MODULE_NAME, GET_CONFIG_INFO_REQUEST, null, rsptarget);
        Log.debug("Sent getConfigInfo request.");
    }
}
