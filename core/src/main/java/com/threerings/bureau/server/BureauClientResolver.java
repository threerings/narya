//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.ClientResolver;

import com.threerings.bureau.data.BureauClientObject;

/**
 * Used to configure crowd-specific client object data.
 */
public class BureauClientResolver extends ClientResolver
{
    @Override // from ClientResolver
    public ClientObject createClientObject ()
    {
        return new BureauClientObject();
    }
}
