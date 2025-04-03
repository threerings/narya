//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.ClientLocal;
import com.threerings.presents.server.ClientResolver;

import com.threerings.crowd.data.BodyObject;

/**
 * Used to configure crowd-specific client object data.
 */
public class CrowdClientResolver extends ClientResolver
{
    @Override // from ClientResolver
    public ClientObject createClientObject ()
    {
        return new BodyObject();
    }

    @Override
    public ClientLocal createLocalAttribute ()
    {
        return new BodyLocal();
    }
}
