//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.client.LocationService;

/**
 * Defines the server-side of the {@link LocationService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from LocationService.java.")
public interface LocationProvider extends InvocationProvider
{
    /**
     * Handles a {@link LocationService#leavePlace} request.
     */
    void leavePlace (ClientObject caller);

    /**
     * Handles a {@link LocationService#moveTo} request.
     */
    void moveTo (ClientObject caller, int arg1, LocationService.MoveListener arg2)
        throws InvocationException;
}
