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
