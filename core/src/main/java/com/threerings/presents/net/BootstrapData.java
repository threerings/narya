//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.util.List;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.data.InvocationMarshaller;

/**
 * A <code>BootstrapData</code> object is communicated back to the client after authentication has
 * succeeded and after the server is fully prepared to deal with the client. It contains
 * information the client will need to interact with the server.
 */
public class BootstrapData extends SimpleStreamableObject
{
    /** The unique id of the client's connection (used to address datagrams). */
    public int connectionId;

    /** The oid of this client's associated distributed object. */
    public int clientOid;

    /** A list of handles to invocation services. */
    public List<InvocationMarshaller<?>> services;
}
