//
// $Id: BodyService.java,v 1.1 2002/11/01 00:39:18 shaper Exp $

package com.threerings.crowd.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * The client side of the body-related invocation services.
 */
public interface BodyService extends InvocationService
{
    /**
     * Requests to set the idle state of the client to the specified
     * value.
     */
    public void setIdle (Client client, boolean idle);
}
