package com.threerings.presents.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationReceiver.Registration;

/**
 * Adds a receiver registration for a client that doesn't use DObject and thereby can't
 * use the registration set on ClientObject.
 */
public interface RegistrationService
{
    void registerReceiver(Registration registration);
}
