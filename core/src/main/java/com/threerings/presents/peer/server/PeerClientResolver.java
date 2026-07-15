//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server;

import com.threerings.presents.server.ClientResolver;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.PeerClientObject;

/**
 * Handles the resolution of peer server client data.
 */
public class PeerClientResolver extends ClientResolver
{
    @Override
    public ClientObject createClientObject ()
    {
        return new PeerClientObject();
    }
}
