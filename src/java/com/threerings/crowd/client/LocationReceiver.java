//
// $Id: LocationReceiver.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

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
    public void forcedMove (int placeId);
}
