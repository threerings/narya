//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import java.io.InputStream;

import com.threerings.io.ObjectInputStream;

/**
 * A specialized {@link ObjectInputStream} used in conjunction with {@link Client} to allow
 * instances that are read from the stream to obtain a client reference "on their way in". We use
 * this to allow invocation marshallers to get a reference to the client with which they are
 * associated when they are streamed in over the network.
 */
public class ClientObjectInputStream extends ObjectInputStream
{
    /** The client with which this input stream is associated. */
    public final Client client;

    public ClientObjectInputStream (Client client, InputStream source)
    {
        super(source);
        this.client = client;
    }
}
