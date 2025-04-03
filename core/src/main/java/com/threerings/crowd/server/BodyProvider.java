//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.client.BodyService;

/**
 * Defines the server-side of the {@link BodyService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from BodyService.java.")
public interface BodyProvider extends InvocationProvider
{
    /**
     * Handles a {@link BodyService#setIdle} request.
     */
    void setIdle (ClientObject caller, boolean arg1);
}
