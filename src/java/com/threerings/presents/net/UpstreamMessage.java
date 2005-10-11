//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.presents.net;

import com.threerings.io.SimpleStreamableObject;

/**
 * This class encapsulates a message in the distributed object protocol
 * that flows from the client to the server.  Upstream messages include
 * object subscription, event forwarding and session management.
 */
public abstract class UpstreamMessage extends SimpleStreamableObject
{
    /**
     * This is a unique (within the context of a reasonable period of
     * time) identifier assigned to each upstream message. The message ids
     * are used to correlate a downstream response message to the
     * appropriate upstream request message.
     */
    public short messageId;

    /** A timestamp indicating when this upstream message was received. */
    public transient long received;

    /**
     * Each upstream message derived class must provide a zero argument
     * constructor so that it can be unserialized when read from the
     * network.
     */
    public UpstreamMessage ()
    {
        // automatically generate a valid message id; on the client, this
        // will be used, on the server it will be overwritten by the
        // unserialized value
        this.messageId = nextMessageId();
    }

    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }

    /**
     * Returns the next message id suitable for use by an upstream
     * message.
     */
    protected static synchronized short nextMessageId ()
    {
        _nextMessageId = (short)((_nextMessageId + 1) % Short.MAX_VALUE);
        return _nextMessageId;
    }

    /**
     * This is used to generate monotonically increasing message ids on
     * the client as new messages are generated.
     */
    protected static short _nextMessageId;
}
