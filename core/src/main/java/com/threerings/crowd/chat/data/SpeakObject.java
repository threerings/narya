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

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

import com.threerings.crowd.chat.data.UserMessage;

/**
 * Provides a mechanism by which the speak service can identify chat listeners so as to maintain a
 * recent history of all chat traffic on the server.
 */
public interface SpeakObject
{
    public static final String DEFAULT_IDENTIFIER = "default";

    /** Used in conjunction with {@link SpeakObject#applyToListeners}. */
    public static interface ListenerOp
    {
        /** Call this method if you only have access to body oids. */
        void apply (SpeakObject sender, int bodyOid);

        /** Call this method if you can provide usernames directly. */
        void apply (SpeakObject sender, Name username);
    }

    /**
     * Returns an identifier for what type of chat this speak object represents based on the message.
     */
    String getChatIdentifier (UserMessage message);

    /**
     * The speak service will call this every time a chat message is delivered on this speak object
     * to note the listeners that received the message.
     */
    void applyToListeners (ListenerOp op);
}
