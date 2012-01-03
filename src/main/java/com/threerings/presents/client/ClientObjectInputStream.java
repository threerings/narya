//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
