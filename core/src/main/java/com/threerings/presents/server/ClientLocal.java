//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.data.ClientObject;

/**
 * Contains information about a client only tracked on the server. This is configured as a local
 * attribute on the {@link ClientObject}.
 *
 * <p> Note: this object implements streamable so that it can be cleanly passed between servers in
 * a peered environment. It is never sent to the client.
 */
public class ClientLocal extends SimpleStreamableObject
{
    /** A shared secret key used for encrypting data. */
    public byte[] secret;
}
