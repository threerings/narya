//
// $Id: AdminService.java,v 1.3 2004/02/25 14:39:14 mdb Exp $

package com.threerings.admin.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Defines the client side of the admin invocation services.
 */
public interface AdminService extends InvocationService
{
    /**
     * Used to communicate a response to a {@link #getConfigInfo} request.
     */
    public static interface ConfigInfoListener extends InvocationListener
    {
        /**
         * Delivers a successful response to a {@link #getConfigInfo}
         * request.
         */
        public void gotConfigInfo (String[] keys, int[] oids);
    }

    /**
     * Requests the list of config objects.
     */
    public void getConfigInfo (Client client, ConfigInfoListener listener);
}
