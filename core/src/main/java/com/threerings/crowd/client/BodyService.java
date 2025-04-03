//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

/**
 * The client side of the body-related invocation services.
 */
public interface BodyService extends InvocationService<ClientObject>
{
    /**
     * Requests to set the idle state of the client to the specified
     * value.
     */
    void setIdle (boolean idle);
}
