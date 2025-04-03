//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;

/**
 * Adds a receiver registration for a client that doesn't use DObject and thereby can't use the
 * registration set on ClientObject.
 */
public interface RegistrationService
    extends InvocationService<ClientObject>
{
    void registerReceiver(Registration registration);
}
