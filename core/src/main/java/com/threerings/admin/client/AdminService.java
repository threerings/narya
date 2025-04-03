//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

/**
 * Defines the client side of the admin invocation services.
 */
public interface AdminService extends InvocationService<ClientObject>
{
    /**
     * Used to communicate a response to a {@link AdminService#getConfigInfo} request.
     */
    public static interface ConfigInfoListener extends InvocationListener
    {
        /**
         * Delivers a successful response to a {@link AdminService#getConfigInfo} request.
         */
        void gotConfigInfo (String[] keys, int[] oids);
    }

    /**
     * Requests the list of config objects.
     */
    void getConfigInfo (ConfigInfoListener listener);
}
