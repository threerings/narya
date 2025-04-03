//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.admin.client.AdminService;

/**
 * Defines the server-side of the {@link AdminService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from AdminService.java.")
public interface AdminProvider extends InvocationProvider
{
    /**
     * Handles a {@link AdminService#getConfigInfo} request.
     */
    void getConfigInfo (ClientObject caller, AdminService.ConfigInfoListener arg1)
        throws InvocationException;
}
