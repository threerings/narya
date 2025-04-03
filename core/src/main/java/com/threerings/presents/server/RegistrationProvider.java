//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.client.RegistrationService;
import com.threerings.presents.data.ClientObject;

/**
 * Defines the server-side of the {@link RegistrationService}.
 */
@Generated(value={"com.threerings.presents.tools.cpp.GenCPPServiceTask"},
           comments="Derived from RegistrationService.java.")
public interface RegistrationProvider extends InvocationProvider
{
    /**
     * Handles a {@link RegistrationService#registerReceiver} request.
     */
    void registerReceiver (ClientObject caller, InvocationReceiver.Registration arg1);
}
