//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.google.inject.Injector;

import com.threerings.crowd.data.JabberConfig;

/**
 * A basic server that creates a single room and sticks everyone in it where they can chat with one
 * another.
 */
public class JabberServer extends CrowdServer
{
    public static void main (String[] args)
    {
        runServer(new CrowdModule(), new PresentsServerModule(JabberServer.class));
    }

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // create a single location
        _pmgr = _plreg.createPlace(new JabberConfig());
    }

    protected PlaceManager _pmgr;
}
