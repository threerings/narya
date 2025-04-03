//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.presents.server.net.AuthingConnection;

/**
 * Handles certain special kinds of authentications and passes the remainder through to the default
 * authenticator.
 */
public abstract class ChainedAuthenticator extends Authenticator
{
    /**
     * Derived classes should implement this method and return true if the supplied connection is
     * one that they should authenticate.
     */
    public abstract boolean shouldHandleConnection (AuthingConnection conn);
}
