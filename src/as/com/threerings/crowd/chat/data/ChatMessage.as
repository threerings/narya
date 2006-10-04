//
// $Id: ChatMessage.java 3098 2004-08-27 02:12:55Z mdb $
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

package com.threerings.crowd.chat.data {

import com.threerings.util.ClassUtil;
import com.threerings.util.Long;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * The abstract base class of all the client-side ChatMessage objects.
 */
public /*abstract*/ class ChatMessage
    implements Streamable
{
    /** The actual text of the message. */
    public var message :String;

    /** The bundle to use when translating this message. */
    public var bundle :String;

    /** The client side 'localtype' of this chat, set to the type
     * registered with an auxiliary source in the ChatDirector. */
    public var localtype :String;

    /** The client time that this message was created. */
    public var timestamp :Long;

    public function ChatMessage (msg :String = null, bundle :String = null)
    {
        this.message = msg;
        this.bundle = bundle;
    }

    /**
     * Once this message reaches the client, the information contained within
     * is changed around a bit.
     */
    public function setClientInfo (msg :String, localtype :String) :void
    {
        message = msg;
        this.localtype = localtype;
        bundle = null;
        //timestamp = System.currentTimeMillis();
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return ClassUtil.shortClassName(this) +
            " [message=" + message + ", bundle=" + bundle + "]";
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        message = (ins.readField(String) as String);
        bundle = (ins.readField(String) as String);
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeField(message);
        out.writeField(bundle);
    }
}
}
