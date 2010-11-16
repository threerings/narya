package com.threerings.presents.server;

import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;

/**
 * Adds receiver registrations for a client.  Must be added to an invocation dispatcher to be used.
 */
public class RegistrationManager implements RegistrationProvider
{
    public void registerReceiver (ClientObject caller, Registration reg)
    {
        if (caller.receivers.containsKey(reg.getKey())) {
            caller.updateReceivers(reg);
        } else {
            caller.addToReceivers(reg);
        }
    }
}
