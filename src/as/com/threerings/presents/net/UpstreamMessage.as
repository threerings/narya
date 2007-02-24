//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.net {

import com.threerings.io.Streamable;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.Short;

public /* abstract */ class UpstreamMessage
    implements Streamable
{
    /** This is a unique (within the context of a reasonable period
    * of time) identifier assigned to each upstream message. The message ids
    * are used to correlate a downstream response message to the 
    * appropriate upstream request message. */
    public var messageId :int;

    /**
     * Construct an upstream message.
     */
    public function UpstreamMessage ()
    {
        // automatically generate a valid message id
        this.messageId = nextMessageId();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeShort(messageId);
    }

    // documentation inherited from interface Streamable
    public final function readObject (ins :ObjectInputStream) :void
    {
        throw new Error(); // abstract: not needed
    }

    /**
     * Returns the next message id suitable for use by an upstream message./
     */
    protected static function nextMessageId () :int
    {
        _nextMessageId = (_nextMessageId + 1) % Short.MAX_VALUE;
        return _nextMessageId;
    }

    /** This is used to generate monotonically increasing message ids on the
     * client as new messages are generated. */
    protected static var _nextMessageId :int;
}
}
