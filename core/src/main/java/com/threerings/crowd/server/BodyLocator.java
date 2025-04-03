//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.data.ClientObject;
import com.threerings.util.Name;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.server.ClientManager;

import com.threerings.crowd.data.BodyObject;

/**
 * Used to lookup {@link BodyObject} instances by name.
 */
@Singleton
public class BodyLocator
{
    /**
     * Returns the body object for the user with the specified visible name, or null if they are
     * not online.
     */
    @EventThread
    public BodyObject lookupBody (Name visibleName)
    {
        // by default visibleName is username
        return forClient(_clmgr.getClientObject(visibleName));
    }

    /**
     * Returns the body object to be used for the given client. This is the reverse operation of
     * {@link BodyObject#getClientObject} and the two should match. The default implementation
     * assumes they are one and the same. This method should return null if the client is not
     * currently controlling a body.
     */
    @EventThread
    public BodyObject forClient (ClientObject client)
    {
        return (BodyObject)client;
    }

    @Inject protected ClientManager _clmgr;
}
