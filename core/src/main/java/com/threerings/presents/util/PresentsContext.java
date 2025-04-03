//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import com.samskivert.util.Config;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

/**
 * Provides access to standard services needed by code that is part of or uses the Presents
 * package.
 */
public interface PresentsContext
{
    /**
     * Provides a configuration object from which various services can obtain configuration values
     * and via the properties file that forms the basis of the configuration object, those services
     * can be customized.
     */
    Config getConfig ();

    /**
     * Returns a reference to the client. This reference should be valid for the life of the
     * application.
     */
    Client getClient ();

    /**
     * Returns a reference to the distributed object manager. This reference is only valid for the
     * duration of a session.
     */
    DObjectManager getDObjectManager ();
}
