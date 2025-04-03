//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
            caller.removeFromReceivers(reg.getKey());
        }
        caller.addToReceivers(reg);
    }
}
