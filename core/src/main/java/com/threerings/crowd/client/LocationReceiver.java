//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.presents.client.InvocationReceiver;

/**
 * Defines, for the location services, a set of notifications delivered
 * asynchronously by the server to the client.
 */
public interface LocationReceiver extends InvocationReceiver
{
    /**
     * Used to communicate a required move notification to the client. The
     * server will have removed the client from their existing location
     * and the client is then responsible for generating a {@link
     * LocationService#moveTo} request to move to the new location.
     */
    void forcedMove (int placeId);
}
