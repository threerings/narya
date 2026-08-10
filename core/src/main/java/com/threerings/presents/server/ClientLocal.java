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

    /**
     * The session that currently owns this client object, or null if it is not owned by a session
     * on this server. Maintained by {@link PresentsSession}; this is the only reliable way to get
     * from a {@link ClientObject} back to its session, as a session's {@link
     * PresentsSession#getAuthName} need not match its client object's username (see {@link
     * PresentsSession#setUsername}).
     *
     * <p> Transient because a session is meaningful only on the server hosting it; a client object
     * passed to a peer arrives with no session.
     */
    public transient PresentsSession session;
}
