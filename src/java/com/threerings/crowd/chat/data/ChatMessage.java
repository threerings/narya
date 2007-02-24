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

package com.threerings.crowd.chat.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

/**
 * The abstract base class of all the client-side ChatMessage objects.
 */
public abstract class ChatMessage
    implements Streamable
{
    /** The actual text of the message. */
    public String message;

    /** The bundle to use when translating this message. */
    public String bundle;

    /** The client side 'localtype' of this chat, set to the type
     * registered with an auxiliary source in the ChatDirector. */
    public transient String localtype;

    /** The client time that this message was created. */
    public transient long timestamp;

    /**
     * For all your unserialization needs.
     */
    public ChatMessage ()
    {
    }

    /**
     * Construct a ChatMessage.
     */
    public ChatMessage (String message, String bundle)
    {
        this.message = message;
        this.bundle = bundle;
    }

    /**
     * Once this message reaches the client, the information contained within
     * is changed around a bit.
     */
    public void setClientInfo (String msg, String localtype)
    {
        message = msg;
        this.localtype = localtype;
        bundle = null;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Get the appropriate message format for this message.
     */
    public String getFormat ()
    {
        return null;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.shortClassName(this) +
            StringUtil.fieldsToString(this);
    }
}
