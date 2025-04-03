//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Sets up Guice so that presents things can be tested.
 */
public class PresentsTestBase
{
    protected <T> T getInstance (Class<T> clazz)
    {
        if (_injector == null) {
            _injector = Guice.createInjector(new PresentsServer.PresentsModule());
        }
        return _injector.getInstance(clazz);
    }

    protected Injector _injector;
}
